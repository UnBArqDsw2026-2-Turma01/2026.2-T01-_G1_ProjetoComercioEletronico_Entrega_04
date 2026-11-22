package comercio.transacao.dominio.pedido;


/**
 * Estado final: todas as operações são inválidas a partir daqui.
 */
/**
 * Papel GoF: ConcreteState.
 *
 * Origem: padrao State da Entrega 3 (Subequipe 03). Na Entrega 4 a transicao
 * invalida passa a ser sinalizada por TransicaoInvalidaException, que carrega
 * a referencia ao ADR-05 do DAS.
 */
public final class EstadoCancelado implements EstadoPedido {

    @Override
    public void confirmarPagamento(Pedido pedido) {
        throw new TransicaoInvalidaException("Pedido cancelado - operação inválida.");
    }

    @Override
    public void cancelar(Pedido pedido) {
        throw new TransicaoInvalidaException("Pedido já está cancelado.");
    }

    @Override
    public void avancar(Pedido pedido) {
        throw new TransicaoInvalidaException("Pedido cancelado - operação inválida.");
    }

    @Override
    public StatusPedido getStatus() {
        return StatusPedido.CANCELADO;
    }
}