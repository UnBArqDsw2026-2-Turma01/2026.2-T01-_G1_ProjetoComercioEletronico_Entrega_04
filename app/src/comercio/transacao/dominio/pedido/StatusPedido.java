package comercio.transacao.dominio.pedido;

/**
 * Situacoes de um pedido. Cada valor corresponde a um estado concreto do
 * padrao State.
 *
 * Origem: padrao State da Entrega 3 (Subequipe 03). A Maquina de Estados do
 * Pedido da Entrega 2 (Subequipe 01) tem doze estados; o codigo executavel da
 * Entrega 3 implementou estes seis, e a Entrega 4 os preserva. A diferenca
 * esta registrada como limite na Visao de Implementacao.
 */
public enum StatusPedido {
    AGUARDANDO_PAGAMENTO("Aguardando pagamento"),
    PAGO("Pago"),
    EM_SEPARACAO("Em separação"),
    ENVIADO("Enviado"),
    ENTREGUE("Entregue"),
    CANCELADO("Cancelado");

    private final String rotulo;

    StatusPedido(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }
}
