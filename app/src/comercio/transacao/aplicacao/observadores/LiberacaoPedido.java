package comercio.transacao.aplicacao.observadores;

import comercio.transacao.dominio.pagamento.ObservadorDePagamento;
import comercio.transacao.dominio.pagamento.Pagamento;
import comercio.transacao.dominio.pedido.Pedido;
import comercio.transacao.dominio.pedido.StatusPedido;
import comercio.transacao.persistencia.RepositorioDePedidos;

/**
 * Papel GoF: ConcreteObserver.
 *
 * Leva a situacao do pagamento para o ciclo de vida do pedido. E o ponto em que
 * o Observer encontra o State: este observador pede a transicao e o estado do
 * pedido decide.
 *
 * - APROVADO: confirma o pagamento; o pedido passa a PAGO.
 * - EXPIRADO: a janela fechou; o pedido e cancelado.
 * - ESTORNADO: o valor voltou ao comprador; o pedido e cancelado.
 *
 * Origem: padrao Observer da Entrega 3 (Subequipe 03), onde a reacao era uma
 * mensagem no console. Na Entrega 4 a reacao e a transicao de estado.
 */
public final class LiberacaoPedido implements ObservadorDePagamento {

    private final RepositorioDePedidos pedidos;

    public LiberacaoPedido(RepositorioDePedidos pedidos) {
        this.pedidos = pedidos;
    }

    @Override
    public void atualizar(Pagamento pagamento) {
        Pedido pedido = pedidos.obter(pagamento.pedidoId());
        switch (pagamento.getStatus()) {
            case APROVADO:
                pedido.confirmarPagamento();
                pedido.registrarEvento("Pagamento aprovado: pedido liberado para o vendedor preparar");
                break;
            case EXPIRADO:
                pedido.cancelar();
                pedido.registrarEvento("Janela de pagamento encerrada: pedido cancelado");
                break;
            case ESTORNADO:
                if (pedido.getStatus() != StatusPedido.CANCELADO) {
                    pedido.cancelar();
                }
                pedido.registrarEvento("Pagamento estornado: pedido cancelado");
                break;
            default:
                break;
        }
    }
}
