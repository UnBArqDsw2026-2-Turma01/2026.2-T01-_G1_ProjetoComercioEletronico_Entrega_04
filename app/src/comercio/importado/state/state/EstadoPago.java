package comercio.importado.state.state;

import comercio.importado.state.context.Pedido;
import comercio.importado.state.context.StatusPedido;

public class EstadoPago implements EstadoPedido {

    @Override
    public void confirmarPagamento(Pedido pedido) {
        throw new IllegalStateException(
            "Pagamento já confirmado para este pedido."
        );
    }

    @Override
    public void cancelar(Pedido pedido) {
        pedido.mudarEstado(new EstadoCancelado());
    }

    @Override
    public void avancar(Pedido pedido) {
        pedido.mudarEstado(new EstadoEmSeparacao());
    }

    @Override
    public StatusPedido getStatus() {
        return StatusPedido.PAGO;
    }
}