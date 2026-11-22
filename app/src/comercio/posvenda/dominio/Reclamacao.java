package comercio.posvenda.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import comercio.nucleo.AcessoNegadoException;
import comercio.nucleo.Dinheiro;
import comercio.nucleo.RegraDeNegocioException;

/**
 * Reclamacao do comprador sobre o envio de um vendedor (UC18 a UC21).
 *
 * Origem: classe Reclamacao do Diagrama de Classes da Entrega 2 e o Diagrama
 * de Atividades do fluxo de reclamacao (Subequipe 01), cujas tres raias sao a
 * estrutura do Rich Picture: o comprador nunca fala direto com o vendedor;
 * tudo passa pela plataforma. Por isso cada transicao confere QUEM a pede.
 *
 * Ciclo de vida:
 *
 *   ABERTA --responder (vendedor)--> RESPONDIDA
 *   ABERTA ou RESPONDIDA --assumirMediacao (atendimento)--> EM_MEDIACAO
 *   EM_MEDIACAO --reembolsar (PROCEDENTE)--> REEMBOLSADA
 *   EM_MEDIACAO --encerrarSemReembolso (IMPROCEDENTE)--> RESOLVIDA
 *
 * Toda transicao fora desse desenho e recusada com RegraDeNegocioException,
 * citando o caso de uso. RESOLVIDA e REEMBOLSADA sao finais: e o que impede o
 * segundo reembolso da mesma reclamacao.
 *
 * O reembolso so e registrado aqui DEPOIS que o estorno foi efetivado pela
 * Transacao: o Diagrama de Atividades fecha a reclamacao como REEMBOLSADA
 * "quando o estorno foi efetivado, e nao quando foi pedido".
 *
 * Autor: Pedro Luciano de Azevedo (Entrega 4).
 */
public final class Reclamacao {

    /** O que o atendimento decide na mediacao (UC20). */
    public enum Decisao { PROCEDENTE, IMPROCEDENTE }

    private final String id;
    private final String pedidoId;
    private final String compradorId;
    private final String vendedorId;
    private final String anuncioId;
    private final String motivo;
    private final List<String> linhaDoTempo = new ArrayList<String>();

    private StatusReclamacao status;
    private String resposta;
    private String atendenteId;
    private Decisao decisao;
    private String parecer;
    private Dinheiro valorEstornado;
    private String descricaoDoEstorno;
    private String politicaDeEstorno;
    private int prazoDoEstornoEmDias;

    public Reclamacao(String id, String pedidoId, String compradorId, String vendedorId, String anuncioId,
            String motivo) {
        if (id == null || pedidoId == null || compradorId == null || vendedorId == null) {
            throw new IllegalArgumentException("Reclamacao incompleta");
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new RegraDeNegocioException("UC18", "Descreva o problema para abrir a reclamação");
        }
        this.id = id;
        this.pedidoId = pedidoId;
        this.compradorId = compradorId;
        this.vendedorId = vendedorId;
        this.anuncioId = anuncioId;
        this.motivo = motivo.trim();
        this.status = StatusReclamacao.ABERTA;
        registrar("Comprador abriu a reclamação: " + this.motivo);
    }

    // -------------------------------------------------------------- transicoes

    /** UC19: so o vendedor do envio responde, e so uma vez, enquanto a reclamacao esta ABERTA. */
    public synchronized void responder(String autorId, String texto) {
        if (!vendedorId.equals(autorId)) {
            throw new AcessoNegadoException("Só o vendedor do item responde a esta reclamação");
        }
        if (status != StatusReclamacao.ABERTA) {
            throw new RegraDeNegocioException("UC19", "Só uma reclamação aberta pode ser respondida; esta está "
                    + status.rotulo().toLowerCase());
        }
        if (texto == null || texto.trim().isEmpty()) {
            throw new RegraDeNegocioException("UC19", "Escreva a resposta ao comprador");
        }
        resposta = texto.trim();
        status = StatusReclamacao.RESPONDIDA;
        registrar("Vendedor respondeu: " + resposta);
    }

    /** UC20: o atendimento assume a disputa, com ou sem resposta do vendedor. */
    public synchronized void assumirMediacao(String atendente) {
        exigirNaoEncerrada();
        if (status == StatusReclamacao.EM_MEDIACAO) {
            throw new RegraDeNegocioException("UC20", "A reclamação " + id + " já está em mediação");
        }
        atendenteId = atendente;
        status = StatusReclamacao.EM_MEDIACAO;
        registrar("Atendimento assumiu a mediação");
    }

    /**
     * UC21: a mediacao deu razao ao comprador e o estorno ja foi efetivado
     * pela Transacao. Registra o que a politica de estorno fez.
     */
    public synchronized void reembolsar(String textoDoParecer, Dinheiro valor, String descricao, String politica,
            int prazoEmDias) {
        exigirEmMediacao();
        decisao = Decisao.PROCEDENTE;
        parecer = limpo(textoDoParecer);
        valorEstornado = valor;
        descricaoDoEstorno = descricao;
        politicaDeEstorno = politica;
        prazoDoEstornoEmDias = prazoEmDias;
        status = StatusReclamacao.REEMBOLSADA;
        registrar("Atendimento decidiu PROCEDENTE" + (parecer.isEmpty() ? "" : ": " + parecer));
        registrar("Estorno efetivado: " + descricao);
    }

    /** UC20: a mediacao deu razao ao vendedor; a venda e mantida e nada e estornado. */
    public synchronized void encerrarSemReembolso(String textoDoParecer) {
        exigirEmMediacao();
        decisao = Decisao.IMPROCEDENTE;
        parecer = limpo(textoDoParecer);
        status = StatusReclamacao.RESOLVIDA;
        registrar("Atendimento decidiu IMPROCEDENTE" + (parecer.isEmpty() ? "" : ": " + parecer)
                + ". A venda foi mantida");
    }

    /** Confere, antes de chamar o estorno, se a reclamacao pode receber uma decisao. */
    public synchronized void exigirEmMediacao() {
        exigirNaoEncerrada();
        if (status != StatusReclamacao.EM_MEDIACAO) {
            throw new RegraDeNegocioException("UC20", "A decisão só é tomada com a reclamação em mediação");
        }
    }

    private void exigirNaoEncerrada() {
        if (status.encerrada()) {
            throw new RegraDeNegocioException("UC21", "A reclamação " + id + " já foi encerrada como "
                    + status.name() + "; não há nova decisão nem segundo reembolso");
        }
    }

    private void registrar(String evento) {
        linhaDoTempo.add(evento);
    }

    private static String limpo(String texto) {
        return texto == null ? "" : texto.trim();
    }

    // ----------------------------------------------------------------- leitura

    public String id() {
        return id;
    }

    public String pedidoId() {
        return pedidoId;
    }

    public String compradorId() {
        return compradorId;
    }

    public String vendedorId() {
        return vendedorId;
    }

    public String anuncioId() {
        return anuncioId;
    }

    public String motivo() {
        return motivo;
    }

    public synchronized StatusReclamacao status() {
        return status;
    }

    public synchronized String resposta() {
        return resposta;
    }

    public synchronized String atendenteId() {
        return atendenteId;
    }

    public synchronized Decisao decisao() {
        return decisao;
    }

    public synchronized String parecer() {
        return parecer;
    }

    public synchronized Dinheiro valorEstornado() {
        return valorEstornado;
    }

    public synchronized String descricaoDoEstorno() {
        return descricaoDoEstorno;
    }

    public synchronized String politicaDeEstorno() {
        return politicaDeEstorno;
    }

    public synchronized int prazoDoEstornoEmDias() {
        return prazoDoEstornoEmDias;
    }

    /** Os passos da reclamacao, na ordem em que aconteceram. */
    public synchronized List<String> linhaDoTempo() {
        return Collections.unmodifiableList(new ArrayList<String>(linhaDoTempo));
    }
}
