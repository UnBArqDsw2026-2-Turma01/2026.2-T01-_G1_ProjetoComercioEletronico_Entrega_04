package comercio.transacao.dominio.pedido;


/**
 * Papel GoF: ConcreteState.
 *
 * Origem: padrao State da Entrega 3 (Subequipe 03). Na Entrega 4 a transicao
 * invalida passa a ser sinalizada por TransicaoInvalidaException, que carrega
 * a referencia ao ADR-05 do DAS.
 */
public final class EstadoEntregue implements EstadoPedido {

    @Override
    public void confirmarPagamento(Pedido pedido) {
        throw new TransicaoInvalidaException(
            "Pagamento já confirmado - pedido já foi entregue."
        );
    }

    @Override
    public void cancelar(Pedido pedido) {
        // Único cancelamento válido a partir daqui é o que vem do
        // fluxo de Devolução e Estorno (ver Diagrama de Atividades),
        // não um cancelamento espontâneo do cliente.
        pedido.mudarEstado(new EstadoCancelado());
    }

    @Override
    public void avancar(Pedido pedido) {
        throw new TransicaoInvalidaException(
            "Pedido entregue é o fim do ciclo normal de vida - nada para avançar."
        );
    }

    @Override
    public StatusPedido getStatus() {
        return StatusPedido.ENTREGUE;
    }
}