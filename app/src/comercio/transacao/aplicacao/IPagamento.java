package comercio.transacao.aplicacao;

import java.util.List;

/**
 * Interface provida pelo Servico de Pagamentos (Diagrama de Componentes da
 * Entrega 2). Casos de uso: UC12, UC24, UC25 e UC21. Linhas: TR-05 e TR-09.
 */
public interface IPagamento {

    /** UC24: os meios oferecidos e o que a familia de cada um entrega. */
    List<MeioDTO> meios();

    /** UC12 e UC25: cobra o pedido pelo meio escolhido. */
    PedidoDTO pagar(String compradorId, String pedidoId, String meio);

    /** UC25, meios assincronos: a operadora informa que o boleto foi compensado. */
    PedidoDTO confirmarCompensacao(String pedidoId);

    /** UC21: devolve o valor pela politica de estorno da familia usada na compra. */
    EstornoDTO estornar(String pedidoId, String motivo);
}
