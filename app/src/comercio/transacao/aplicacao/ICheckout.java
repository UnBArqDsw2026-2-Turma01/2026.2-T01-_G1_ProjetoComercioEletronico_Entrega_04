package comercio.transacao.aplicacao;

/**
 * Interface provida pelo Servico de Checkout e Pedidos (Diagrama de Componentes
 * da Entrega 2). Casos de uso: UC10 e UC11. Linha da matriz: TR-05.
 *
 * Os dois caminhos de compra (RN-B08) terminam no mesmo lugar: um pedido
 * aguardando pagamento, com o estoque reservado.
 */
public interface ICheckout {

    /** UC10: transforma os itens selecionados do carrinho em pedido. Inclui UC09: exige o endereco. */
    PedidoDTO finalizar(String compradorId, String enderecoId);

    /** UC11: compra direta a partir da ficha, sem passar pelo carrinho. */
    PedidoDTO comprarAgora(String compradorId, String anuncioId, int quantidade, String enderecoId);

    /** O comprador desiste de um pedido ainda nao pago; o estoque reservado volta. */
    PedidoDTO desistir(String compradorId, String pedidoId);
}
