package comercio.transacao.aplicacao;

import java.util.ArrayList;
import java.util.List;

import comercio.transacao.dominio.pagamento.Pagamento;
import comercio.transacao.dominio.pedido.Envio;
import comercio.transacao.dominio.pedido.ItemPedido;
import comercio.transacao.dominio.pedido.Pedido;
import comercio.transacao.persistencia.RepositorioDePedidos;

/** Converte o Pedido do dominio no DTO que os tres servicos de Transacao devolvem. */
final class MontadorDePedidoDTO {

    private MontadorDePedidoDTO() {
    }

    static PedidoDTO montar(Pedido pedido, RepositorioDePedidos pedidos) {
        List<PedidoDTO.Envio> envios = new ArrayList<PedidoDTO.Envio>();
        for (Envio envio : pedido.envios()) {
            List<PedidoDTO.Item> itens = new ArrayList<PedidoDTO.Item>();
            for (ItemPedido item : envio.itens()) {
                itens.add(new PedidoDTO.Item(item.anuncioId(), item.titulo(), item.vendedorId(), item.quantidade(),
                        item.precoNoMomento(), item.subtotal()));
            }
            envios.add(new PedidoDTO.Envio(envio.vendedorId(), envio.vendedorNome(), envio.destino().resumo(),
                    envio.frete(), envio.prazoDias(), itens));
        }

        Pagamento pagamento = pedidos.pagamentoAtual(pedido.getId());
        PedidoDTO.Cobranca cobranca = null;
        if (pagamento != null) {
            cobranca = new PedidoDTO.Cobranca(pagamento.meio().name(), pagamento.meio().rotulo(),
                    pagamento.getStatus().name(), pagamento.getStatus().getDescricao(),
                    pagamento.autorizacao() == null ? null : pagamento.autorizacao().detalhe(),
                    pagamento.autorizacao() == null ? null : pagamento.autorizacao().identificador(),
                    pagamento.comprovante(), pagamento.estorno(), pagamento.familia(), pagamento.classesDoTrio());
        }

        return new PedidoDTO(pedido.getId(), pedido.compradorId(), pedido.getStatus().name(),
                pedido.getStatus().rotulo(), envios, pedido.subtotal(), pedido.frete(), pedido.total(),
                pedido.historico(), pedido.eventos(), cobranca, pedidos.tentativasDe(pedido.getId()).size());
    }
}
