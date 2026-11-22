package comercio.transacao.dominio.pedido;

/**
 * Papel GoF: State.
 *
 * Cada estado concreto decide o que as tres operacoes do ciclo de vida fazem
 * quando o pedido esta naquela situacao: executar a transicao ou recusa-la.
 * O Pedido nao tem nenhum "if (status == ...)": ele delega ao estado atual.
 *
 * E o mecanismo do ADR-05 do DAS: um pedido nao pula etapas, nao e despachado
 * sem pagamento e nao e pago duas vezes, porque a operacao invalida
 * simplesmente nao existe no estado em que ele esta.
 *
 * Origem: padrao State da Entrega 3 (Subequipe 03, FluxoDeCompra(State)).
 */
public interface EstadoPedido {

    void confirmarPagamento(Pedido pedido);

    void cancelar(Pedido pedido);

    void avancar(Pedido pedido);

    StatusPedido getStatus();
}
