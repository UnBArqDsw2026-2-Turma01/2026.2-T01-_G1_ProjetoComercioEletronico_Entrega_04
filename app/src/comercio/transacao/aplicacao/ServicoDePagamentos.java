package comercio.transacao.aplicacao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import comercio.nucleo.AcessoNegadoException;
import comercio.nucleo.RegraDeNegocioException;
import comercio.transacao.dominio.Relogio;
import comercio.transacao.dominio.pagamento.Autorizacao;
import comercio.transacao.dominio.pagamento.FabricaMeioPagamento;
import comercio.transacao.dominio.pagamento.MeioPagamento;
import comercio.transacao.dominio.pagamento.ObservadorDePagamento;
import comercio.transacao.dominio.pagamento.Pagamento;
import comercio.transacao.dominio.pagamento.StatusPagamento;
import comercio.transacao.dominio.pagamento.TrioDePagamento;
import comercio.transacao.dominio.pedido.Pedido;
import comercio.transacao.dominio.pedido.StatusPedido;
import comercio.transacao.dominio.pedido.TransicaoInvalidaException;
import comercio.transacao.persistencia.RepositorioDePedidos;

/**
 * Servico de Pagamentos: fornece IPagamento (UC12, UC24, UC25 e UC21).
 *
 * E onde os tres padroes da Transacao trabalham sobre o mesmo pedido:
 *
 * - Abstract Factory: o meio escolhido vira uma fabrica, e a fabrica entrega o
 *   trio (autorizador, comprovante, politica de estorno) daquela familia. Este
 *   servico nao escreve o nome de nenhuma classe concreta de pagamento. Trocar
 *   Pix por boleto nao muda uma linha daqui (ADR-02 do DAS);
 * - Observer: o resultado da cobranca e uma mudanca de situacao do Pagamento.
 *   Quem reage a ela (liberar o pedido, devolver estoque, avisar o comprador,
 *   emitir a nota) sao observadores que este servico nao chama pelo nome;
 * - State: um dos observadores confirma o pagamento no Pedido, e e o estado
 *   do pedido que decide se a transicao e valida (ADR-05 do DAS).
 *
 * Cobranca nao se repete. So um pedido que aguarda pagamento pode ser cobrado;
 * a segunda tentativa sobre um pedido ja pago e recusada antes de chegar a
 * operadora.
 */
public final class ServicoDePagamentos implements IPagamento {

    private final RepositorioDePedidos pedidos;
    private final RegistroDeFabricas fabricas;
    private final Relogio relogio;
    private final List<ObservadorDePagamento> observadores;

    /** @param observadores quem deve ser avisado a cada mudanca de situacao, na ordem de aviso */
    public ServicoDePagamentos(RepositorioDePedidos pedidos, RegistroDeFabricas fabricas, Relogio relogio,
            List<ObservadorDePagamento> observadores) {
        this.pedidos = pedidos;
        this.fabricas = fabricas;
        this.relogio = relogio;
        this.observadores = new ArrayList<ObservadorDePagamento>(observadores);
    }

    @Override
    public List<MeioDTO> meios() {
        List<MeioDTO> meios = new ArrayList<MeioDTO>();
        for (FabricaMeioPagamento fabrica : fabricas.todas()) {
            TrioDePagamento trio = new TrioDePagamento(fabrica);
            meios.add(new MeioDTO(trio.meio().name(), trio.meio().rotulo(), trio.resumoDaFamilia(),
                    trio.ehAssincrono(), trio.prazoLimiteEmMinutos(), Arrays.asList(trio.classesDoTrio())));
        }
        return meios;
    }

    @Override
    public synchronized PedidoDTO pagar(String compradorId, String pedidoId, String meioEscolhido) {
        Pedido pedido = pedidos.obter(pedidoId);
        if (!pedido.compradorId().equals(compradorId)) {
            throw new AcessoNegadoException("O pedido " + pedidoId + " é de outro comprador");
        }
        if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
            throw new TransicaoInvalidaException("O pedido " + pedidoId + " não aguarda pagamento: está "
                    + pedido.getStatus().rotulo().toLowerCase());
        }
        Pagamento anterior = pedidos.pagamentoAtual(pedidoId);
        if (anterior != null && anterior.getStatus() == StatusPagamento.EM_ANALISE) {
            throw new RegraDeNegocioException("UC25", "Já existe uma cobrança em análise para o pedido " + pedidoId
                    + " (" + anterior.meio().rotulo() + ")");
        }

        MeioPagamento meio = fabricas.meioDe(meioEscolhido);
        // A unica linha que importa: a fabrica do meio escolhido entrega o trio inteiro.
        TrioDePagamento trio = new TrioDePagamento(fabricas.para(meio));

        long minutos = relogio.minutoAtual() - pedido.criadoNoMinuto();
        Autorizacao resposta;
        if (minutos > trio.prazoLimiteEmMinutos()) {
            resposta = Autorizacao.expirada("Janela de " + trio.prazoLimiteEmMinutos() + " min encerrada após "
                    + minutos + " min");
        } else {
            resposta = trio.pagar(pedido.getId(), pedido.total());
        }
        String comprovante = resposta.aprovada()
                ? trio.comprovanteDe(pedido.getId(), pedido.total(), resposta) : null;

        Pagamento pagamento = new Pagamento(pedido.getId(), pedido.total(), meio);
        for (ObservadorDePagamento observador : observadores) {
            pagamento.adicionarObservador(observador);
        }
        pagamento.registrarTrio(trio, resposta, comprovante);
        pedidos.registrar(pagamento);

        // A mudanca de situacao e o evento: os observadores reagem a partir daqui.
        pagamento.setStatus(situacaoPara(resposta, trio));
        return MontadorDePedidoDTO.montar(pedido, pedidos);
    }

    private static StatusPagamento situacaoPara(Autorizacao resposta, TrioDePagamento trio) {
        switch (resposta.status()) {
            case APROVADO:
                // Familia assincrona: a emissao foi aceita, o dinheiro ainda nao chegou.
                return trio.ehAssincrono() ? StatusPagamento.EM_ANALISE : StatusPagamento.APROVADO;
            case EXPIRADO:
                return StatusPagamento.EXPIRADO;
            default:
                return StatusPagamento.RECUSADO;
        }
    }

    @Override
    public synchronized PedidoDTO confirmarCompensacao(String pedidoId) {
        Pedido pedido = pedidos.obter(pedidoId);
        Pagamento pagamento = pedidos.pagamentoAtual(pedidoId);
        if (pagamento == null || pagamento.getStatus() != StatusPagamento.EM_ANALISE) {
            throw new RegraDeNegocioException("UC25", "O pedido " + pedidoId + " não tem cobrança aguardando compensação");
        }
        pagamento.setStatus(StatusPagamento.APROVADO);
        return MontadorDePedidoDTO.montar(pedido, pedidos);
    }

    @Override
    public synchronized EstornoDTO estornar(String pedidoId, String motivo) {
        pedidos.obter(pedidoId);
        Pagamento pagamento = pedidos.pagamentoAtual(pedidoId);
        if (pagamento == null) {
            throw new RegraDeNegocioException("UC21", "O pedido " + pedidoId + " nunca foi cobrado");
        }
        if (pagamento.getStatus() == StatusPagamento.ESTORNADO) {
            throw new RegraDeNegocioException("UC21", "O pagamento do pedido " + pedidoId + " já foi estornado");
        }
        if (pagamento.getStatus() != StatusPagamento.APROVADO) {
            throw new RegraDeNegocioException("UC21", "Só um pagamento aprovado pode ser estornado; este está "
                    + pagamento.getStatus().getDescricao().toLowerCase());
        }

        // A politica de estorno e a da MESMA familia que autorizou a compra.
        TrioDePagamento trio = new TrioDePagamento(fabricas.para(pagamento.meio()));
        if (!trio.podeEstornar(pagamento.autorizacao())) {
            throw new RegraDeNegocioException("UC21", "A política de estorno de " + pagamento.meio().rotulo()
                    + " não aceita esta autorização");
        }
        String descricao = trio.reembolsar(pedidoId, pagamento.valor(), pagamento.autorizacao());
        String registro = motivo == null || motivo.trim().isEmpty() ? descricao
                : descricao + ". Motivo: " + motivo.trim();
        pagamento.registrarEstorno(registro);
        pagamento.setStatus(StatusPagamento.ESTORNADO);

        return new EstornoDTO(pedidoId, pagamento.valor(), pagamento.meio().name(), registro,
                trio.prazoDeDevolucaoEmDias(), trio.classesDoTrio()[2]);
    }
}
