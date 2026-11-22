package comercio.transacao.dominio.pedido;


/**
 * Papel GoF: ConcreteState.
 *
 * Origem: padrao State da Entrega 3 (Subequipe 03). Na Entrega 4 a transicao
 * invalida passa a ser sinalizada por TransicaoInvalidaException, que carrega
 * a referencia ao ADR-05 do DAS.
 */
public final class EstadoAguardandoPagamento implements EstadoPedido {

    @Override
    public void confirmarPagamento(Pedido pedido) {
        pedido.mudarEstado(new EstadoPago());
    }

    @Override
    public void cancelar(Pedido pedido) {
        pedido.mudarEstado(new EstadoCancelado());
    }

    @Override
    public void avancar(Pedido pedido) {
        throw new TransicaoInvalidaException(
            "Não é possível avançar um pedido que ainda aguarda pagamento."
        );
    }

    @Override
    public StatusPedido getStatus() {
        return StatusPedido.AGUARDANDO_PAGAMENTO;
    }
}