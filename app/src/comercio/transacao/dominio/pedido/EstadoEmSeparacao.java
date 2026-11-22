package comercio.transacao.dominio.pedido;


/**
 * Papel GoF: ConcreteState.
 *
 * Origem: padrao State da Entrega 3 (Subequipe 03). Na Entrega 4 a transicao
 * invalida passa a ser sinalizada por TransicaoInvalidaException, que carrega
 * a referencia ao ADR-05 do DAS.
 */
public final class EstadoEmSeparacao implements EstadoPedido {

    @Override
    public void confirmarPagamento(Pedido pedido) {
        throw new TransicaoInvalidaException(
            "Pagamento já confirmado - pedido já está em separação."
        );
    }

    @Override
    public void cancelar(Pedido pedido) {
        pedido.mudarEstado(new EstadoCancelado());
    }

    @Override
    public void avancar(Pedido pedido) {
        pedido.mudarEstado(new EstadoEnviado());
    }

    @Override
    public StatusPedido getStatus() {
        return StatusPedido.EM_SEPARACAO;
    }
}