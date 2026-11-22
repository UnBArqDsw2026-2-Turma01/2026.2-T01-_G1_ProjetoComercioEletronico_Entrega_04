package comercio.transacao.dominio.pagamento;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import comercio.nucleo.Dinheiro;

/**
 * Papel GoF: ConcreteSubject do padrao Observer.
 *
 * Guarda a situacao de uma cobranca e avisa os observadores sempre que ela
 * muda. Quem reage (liberar o pedido, devolver o estoque, avisar o comprador,
 * emitir a nota) nao e conhecido por esta classe: ela so conhece a interface
 * ObservadorDePagamento.
 *
 * Origem: padrao Observer da Entrega 3 (Subequipe 03). Mudancas na Entrega 4:
 *
 * - o valor passa a ser Dinheiro, em centavos, e nao double (decisao 10 do
 *   Diagrama de Classes da Entrega 2);
 * - o pagamento passa a saber de qual pedido e, por qual meio foi feito e qual
 *   autorizacao recebeu, que e o que os observadores precisam consultar;
 * - a falha de um observador continua nao impedindo os demais, mas deixa de
 *   ser impressa no console e passa a ficar registrada (falhasDeNotificacao).
 */
public final class Pagamento implements PagamentoObservavel {

    private final String pedidoId;
    private final Dinheiro valor;
    private final MeioPagamento meio;
    private StatusPagamento status;
    private Autorizacao autorizacao;
    private String comprovante;
    private String estorno;
    private String familia;
    private List<String> classesDoTrio = new ArrayList<String>();
    private final List<ObservadorDePagamento> observadores = new ArrayList<ObservadorDePagamento>();
    private final List<String> falhasDeNotificacao = new ArrayList<String>();

    public Pagamento(String pedidoId, Dinheiro valor, MeioPagamento meio) {
        if (pedidoId == null || valor == null || meio == null) {
            throw new IllegalArgumentException("Pagamento incompleto: pedido, valor e meio sao obrigatorios");
        }
        this.pedidoId = pedidoId;
        this.valor = valor;
        this.meio = meio;
        this.status = StatusPagamento.PENDENTE;
    }

    public String pedidoId() {
        return pedidoId;
    }

    public Dinheiro valor() {
        return valor;
    }

    public MeioPagamento meio() {
        return meio;
    }

    public synchronized StatusPagamento getStatus() {
        return status;
    }

    public synchronized Autorizacao autorizacao() {
        return autorizacao;
    }

    public synchronized String comprovante() {
        return comprovante;
    }

    public synchronized String estorno() {
        return estorno;
    }

    /** Resumo da familia de pagamento usada, para exibicao. */
    public synchronized String familia() {
        return familia;
    }

    /** Nomes das tres classes que a fabrica entregou para esta cobranca. */
    public synchronized List<String> classesDoTrio() {
        return Collections.unmodifiableList(new ArrayList<String>(classesDoTrio));
    }

    /** Registra o que o trio da familia produziu. Nao notifica: nao e mudanca de situacao. */
    public synchronized void registrarTrio(TrioDePagamento trio, Autorizacao resposta, String comprovanteEmitido) {
        this.familia = trio.resumoDaFamilia();
        this.classesDoTrio = new ArrayList<String>();
        for (String classe : trio.classesDoTrio()) {
            this.classesDoTrio.add(classe);
        }
        this.autorizacao = resposta;
        this.comprovante = comprovanteEmitido;
    }

    public synchronized void registrarEstorno(String descricao) {
        this.estorno = descricao;
    }

    /**
     * Muda a situacao e avisa os observadores. Atribuir a mesma situacao de
     * novo nao gera aviso.
     */
    public void setStatus(StatusPagamento novo) {
        synchronized (this) {
            if (novo == this.status) {
                return;
            }
            this.status = novo;
        }
        notificarObservadores();
    }

    @Override
    public synchronized void adicionarObservador(ObservadorDePagamento observador) {
        if (!observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    @Override
    public synchronized void removerObservador(ObservadorDePagamento observador) {
        observadores.remove(observador);
    }

    @Override
    public void notificarObservadores() {
        List<ObservadorDePagamento> copia;
        synchronized (this) {
            copia = new ArrayList<ObservadorDePagamento>(observadores);
        }
        for (ObservadorDePagamento observador : copia) {
            try {
                observador.atualizar(this);
            } catch (RuntimeException e) {
                synchronized (this) {
                    falhasDeNotificacao.add(observador.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
        }
    }

    /** Observadores que falharam ao ser avisados. Vazio no caminho normal. */
    public synchronized List<String> falhasDeNotificacao() {
        return Collections.unmodifiableList(new ArrayList<String>(falhasDeNotificacao));
    }
}
