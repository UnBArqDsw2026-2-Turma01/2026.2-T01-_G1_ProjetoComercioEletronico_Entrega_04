package comercio.posvenda.aplicacao;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import comercio.contas.aplicacao.RegistroDeReputacao;
import comercio.contas.dominio.Reputacao;
import comercio.contas.persistencia.RepositorioDeContas;
import comercio.nucleo.AcessoNegadoException;
import comercio.nucleo.RegraDeNegocioException;
import comercio.posvenda.dominio.Avaliacao;
import comercio.posvenda.dominio.Reclamacao;
import comercio.posvenda.dominio.StatusReclamacao;
import comercio.posvenda.persistencia.RepositorioDePosVenda;
import comercio.transacao.aplicacao.EstornoDTO;
import comercio.transacao.aplicacao.IPagamento;
import comercio.transacao.aplicacao.IRastreio;
import comercio.transacao.aplicacao.PedidoDTO;

/**
 * Servico de Pos-venda (Diagrama de Componentes da Entrega 2): fornece
 * IReclamacao e requer IRastreio e IPagamento da Transacao.
 *
 * Casos de uso UC17 a UC21; linhas TR-03 (avaliacao e reputacao) e TR-09
 * (reclamacao, mediacao e estorno) da matriz do DAS.
 *
 * Tres dependencias, todas no sentido que a figura 5.2.5 do DAS permite
 * (Pos-venda depende de Transacao e de Contas, nunca o contrario):
 *
 * - IRastreio informa o pedido, a situacao e de quem e cada item. O
 *   Pos-venda nao guarda pedido: so o consulta.
 * - IPagamento faz o estorno pela politica da familia do meio usado na
 *   compra (Abstract Factory, ADR-02). O Pos-venda nao sabe se foi cartao ou
 *   boleto: so le o que a politica fez.
 * - RegistroDeReputacao e o unico caminho pelo qual uma avaliacao muda a
 *   reputacao do vendedor. E assim que o DAS resolve, sem ciclo, a
 *   "dependencia de dado derivado" da limitacao 5.6.3.
 *
 * Quem age vem sempre da sessao. O papel (comprador, vendedor, atendimento)
 * e conferido aqui contra a conta e contra o pedido, e nunca aceito do
 * cliente.
 *
 * Os metodos que mudam estado sao synchronized: "uma avaliacao por item" e
 * "uma reclamacao em andamento por pedido" sao verificacoes seguidas de
 * gravacao, e duas requisicoes simultaneas nao podem passar as duas.
 *
 * Autor: Pedro Luciano de Azevedo (Entrega 4).
 */
public final class ServicoDePosVenda implements IReclamacao {

    private static final String ATENDENTE = "ATENDENTE";
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final RepositorioDePosVenda repositorio;
    private final IRastreio rastreio;
    private final IPagamento pagamento;
    private final RegistroDeReputacao reputacao;
    private final RepositorioDeContas contas;

    public ServicoDePosVenda(RepositorioDePosVenda repositorio, IRastreio rastreio, IPagamento pagamento,
            RegistroDeReputacao reputacao, RepositorioDeContas contas) {
        if (repositorio == null || rastreio == null || pagamento == null || reputacao == null || contas == null) {
            throw new IllegalArgumentException("O Servico de Pos-venda precisa de todas as suas dependencias");
        }
        this.repositorio = repositorio;
        this.rastreio = rastreio;
        this.pagamento = pagamento;
        this.reputacao = reputacao;
        this.contas = contas;
    }

    // ---------------------------------------------------------------- UC17

    @Override
    public synchronized AvaliacaoDTO avaliar(String compradorId, String pedidoId, String anuncioId, int nota,
            String comentario) {
        PedidoDTO pedido = rastreio.pedido(pedidoId);
        exigirComprador(pedido, compradorId, "Só o comprador do pedido pode avaliá-lo");
        if (!"ENTREGUE".equals(pedido.status)) {
            throw new RegraDeNegocioException("UC17", "Só um pedido entregue pode ser avaliado; este está "
                    + pedido.statusRotulo.toLowerCase());
        }
        PedidoDTO.Item item = item(pedido, anuncioId, "UC17");
        if (repositorio.avaliacao(pedidoId, item.anuncioId) != null) {
            throw new RegraDeNegocioException("UC17", "\"" + item.titulo + "\" já foi avaliado neste pedido");
        }

        // A nota e validada pelo dominio (RN-A05) antes de tocar na reputacao.
        Avaliacao avaliacao = new Avaliacao(repositorio.proximoIdDeAvaliacao(), pedidoId, item.anuncioId,
                item.vendedorId, compradorId, nota, comentario);
        String antes = reputacaoDe(item.vendedorId);
        reputacao.registrar(item.vendedorId, nota);
        repositorio.salvar(avaliacao);
        return avaliacao(avaliacao, item.titulo, antes, reputacaoDe(item.vendedorId));
    }

    // ---------------------------------------------------------------- UC18

    @Override
    public synchronized ReclamacaoDTO abrir(String compradorId, String pedidoId, String anuncioId, String motivo) {
        PedidoDTO pedido = rastreio.pedido(pedidoId);
        exigirComprador(pedido, compradorId, "Só o comprador do pedido pode reclamar dele");
        if (!"ENVIADO".equals(pedido.status) && !"ENTREGUE".equals(pedido.status)) {
            throw new RegraDeNegocioException("UC18", "Só um pedido enviado ou entregue pode receber reclamação; este está "
                    + pedido.statusRotulo.toLowerCase());
        }
        for (Reclamacao existente : repositorio.reclamacoesDoPedido(pedidoId)) {
            if (!existente.status().encerrada()) {
                throw new RegraDeNegocioException("UC18", "O pedido já tem a reclamação " + existente.id()
                        + " em andamento");
            }
            if (existente.decisao() == Reclamacao.Decisao.PROCEDENTE) {
                throw new RegraDeNegocioException("UC18", "O pedido já foi reembolsado pela reclamação "
                        + existente.id());
            }
        }

        String vendedorId;
        String sobreAnuncio = null;
        if (anuncioId != null && !anuncioId.trim().isEmpty()) {
            PedidoDTO.Item item = item(pedido, anuncioId, "UC18");
            vendedorId = item.vendedorId;
            sobreAnuncio = item.anuncioId;
        } else if (pedido.envios.size() == 1) {
            vendedorId = pedido.envios.get(0).vendedorId;
        } else {
            throw new RegraDeNegocioException("UC18", "O pedido tem mais de um vendedor: informe sobre qual item é a reclamação");
        }

        Reclamacao reclamacao = new Reclamacao(repositorio.proximoIdDeReclamacao(), pedidoId, compradorId,
                vendedorId, sobreAnuncio, motivo);
        repositorio.salvar(reclamacao);
        return reclamacao(reclamacao);
    }

    // ---------------------------------------------------------------- UC19

    @Override
    public synchronized ReclamacaoDTO responder(String vendedorId, String reclamacaoId, String resposta) {
        Reclamacao reclamacao = repositorio.reclamacao(reclamacaoId);
        reclamacao.responder(vendedorId, resposta);
        return reclamacao(reclamacao);
    }

    // --------------------------------------------------------- UC20 e UC21

    @Override
    public synchronized ReclamacaoDTO mediar(String atendenteId, String reclamacaoId, String decisao,
            String parecer) {
        exigirAtendimento(atendenteId);
        Reclamacao reclamacao = repositorio.reclamacao(reclamacaoId);

        if (decisao == null || decisao.trim().isEmpty()) {
            reclamacao.assumirMediacao(atendenteId);
            return reclamacao(reclamacao);
        }
        Reclamacao.Decisao escolhida = decisao(decisao);
        if (reclamacao.status() != StatusReclamacao.EM_MEDIACAO) {
            reclamacao.assumirMediacao(atendenteId);
        }
        reclamacao.exigirEmMediacao();

        if (escolhida == Reclamacao.Decisao.IMPROCEDENTE) {
            reclamacao.encerrarSemReembolso(parecer);
            return reclamacao(reclamacao);
        }

        // UC21: primeiro o estorno; so se ele for efetivado a reclamacao vira
        // REEMBOLSADA. Se a Transacao recusar, a reclamacao continua em mediacao.
        EstornoDTO estorno = pagamento.estornar(reclamacao.pedidoId(),
                "reclamação " + reclamacao.id() + ": " + reclamacao.motivo());
        reclamacao.reembolsar(parecer, estorno.valor, estorno.descricao, estorno.politica, estorno.prazoEmDias);
        return reclamacao(reclamacao);
    }

    private static Reclamacao.Decisao decisao(String texto) {
        try {
            return Reclamacao.Decisao.valueOf(texto.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new RegraDeNegocioException("UC20", "Decisão desconhecida: \"" + texto
                    + "\". Use PROCEDENTE ou IMPROCEDENTE");
        }
    }

    // ------------------------------------------------------------- consultas

    @Override
    public synchronized ReclamacaoDTO reclamacao(String usuarioId, String reclamacaoId) {
        Reclamacao reclamacao = repositorio.reclamacao(reclamacaoId);
        boolean envolvido = reclamacao.compradorId().equals(usuarioId) || reclamacao.vendedorId().equals(usuarioId);
        if (!envolvido && !atendimento(usuarioId)) {
            throw new AcessoNegadoException("Esta reclamação é de outro comprador e de outro vendedor");
        }
        return reclamacao(reclamacao);
    }

    @Override
    public synchronized PosVendaDTO posVenda(String usuarioId, String pedidoId) {
        PedidoDTO pedido = rastreio.pedido(pedidoId);
        Set<String> vendedores = new LinkedHashSet<String>();
        for (PedidoDTO.Envio envio : pedido.envios) {
            vendedores.add(envio.vendedorId);
        }
        boolean comprador = pedido.compradorId.equals(usuarioId);
        if (!comprador && !vendedores.contains(usuarioId) && !atendimento(usuarioId)) {
            throw new AcessoNegadoException("O pedido " + pedidoId + " não envolve esta conta");
        }

        List<PosVendaDTO.Item> itens = new ArrayList<PosVendaDTO.Item>();
        for (PedidoDTO.Envio envio : pedido.envios) {
            for (PedidoDTO.Item item : envio.itens) {
                Avaliacao avaliacao = repositorio.avaliacao(pedidoId, item.anuncioId);
                itens.add(new PosVendaDTO.Item(item.anuncioId, item.titulo, item.vendedorId, envio.vendedorNome,
                        item.quantidade, avaliacao == null ? null : avaliacao(avaliacao, item.titulo, null, null)));
            }
        }

        List<ReclamacaoDTO> reclamacoes = new ArrayList<ReclamacaoDTO>();
        boolean emAndamento = false;
        boolean reembolsado = false;
        for (Reclamacao reclamacao : repositorio.reclamacoesDoPedido(pedidoId)) {
            reclamacoes.add(reclamacao(reclamacao));
            emAndamento = emAndamento || !reclamacao.status().encerrada();
            reembolsado = reembolsado || reclamacao.decisao() == Reclamacao.Decisao.PROCEDENTE;
        }

        boolean enviado = "ENVIADO".equals(pedido.status) || "ENTREGUE".equals(pedido.status);
        return new PosVendaDTO(pedido.id, pedido.compradorId, pedido.status, pedido.statusRotulo,
                contas.usuario(usuarioId).papel(), itens, reclamacoes,
                comprador && "ENTREGUE".equals(pedido.status),
                comprador && enviado && !emAndamento && !reembolsado);
    }

    // --------------------------------------------------------------- apoio

    private static void exigirComprador(PedidoDTO pedido, String usuarioId, String mensagem) {
        if (!pedido.compradorId.equals(usuarioId)) {
            throw new AcessoNegadoException(mensagem);
        }
    }

    private boolean atendimento(String usuarioId) {
        return usuarioId != null && ATENDENTE.equals(contas.usuario(usuarioId).papel());
    }

    private void exigirAtendimento(String usuarioId) {
        if (!atendimento(usuarioId)) {
            throw new AcessoNegadoException("Só o atendimento media reclamações (UC20)");
        }
    }

    /** O item do pedido com o anuncio informado; sem anuncio, o unico item do pedido. */
    private static PedidoDTO.Item item(PedidoDTO pedido, String anuncioId, String casoDeUso) {
        List<PedidoDTO.Item> todos = new ArrayList<PedidoDTO.Item>();
        for (PedidoDTO.Envio envio : pedido.envios) {
            todos.addAll(envio.itens);
        }
        if (anuncioId == null || anuncioId.trim().isEmpty()) {
            if (todos.size() == 1) {
                return todos.get(0);
            }
            throw new RegraDeNegocioException(casoDeUso, "O pedido tem mais de um item: informe qual");
        }
        for (PedidoDTO.Item item : todos) {
            if (item.anuncioId.equals(anuncioId.trim())) {
                return item;
            }
        }
        throw new RegraDeNegocioException(casoDeUso, "O anúncio " + anuncioId + " não faz parte do pedido "
                + pedido.id);
    }

    private String reputacaoDe(String vendedorId) {
        Reputacao atual = contas.vendedor(vendedorId).reputacao();
        return String.format(PT_BR, "%.2f (%d avaliações)", atual.nota(), atual.avaliacoes());
    }

    private AvaliacaoDTO avaliacao(Avaliacao a, String titulo, String antes, String depois) {
        return new AvaliacaoDTO(a.id(), a.pedidoId(), a.anuncioId(), titulo, a.vendedorId(),
                contas.vendedor(a.vendedorId()).nome(), a.nota(), a.comentario(), antes, depois);
    }

    private ReclamacaoDTO reclamacao(Reclamacao r) {
        ReclamacaoDTO.Estorno estorno = r.valorEstornado() == null ? null
                : new ReclamacaoDTO.Estorno(r.valorEstornado(), r.descricaoDoEstorno(), r.politicaDeEstorno(),
                        r.prazoDoEstornoEmDias());
        return new ReclamacaoDTO(r.id(), r.pedidoId(), r.compradorId(), r.vendedorId(),
                contas.vendedor(r.vendedorId()).nome(), r.anuncioId(), r.motivo(), r.status().name(),
                r.status().rotulo(), r.resposta(), r.decisao() == null ? null : r.decisao().name(), r.parecer(),
                estorno, r.linhaDoTempo());
    }
}
