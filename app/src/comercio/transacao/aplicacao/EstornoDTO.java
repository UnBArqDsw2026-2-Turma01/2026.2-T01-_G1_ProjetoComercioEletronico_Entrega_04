package comercio.transacao.aplicacao;

import comercio.nucleo.Dinheiro;

/** Resultado de um estorno: o que a politica da familia de pagamento fez (UC21). */
public final class EstornoDTO {

    public final String pedidoId;
    public final Dinheiro valor;
    public final String meio;
    public final String descricao;
    public final int prazoEmDias;
    public final String politica;

    public EstornoDTO(String pedidoId, Dinheiro valor, String meio, String descricao, int prazoEmDias,
            String politica) {
        this.pedidoId = pedidoId;
        this.valor = valor;
        this.meio = meio;
        this.descricao = descricao;
        this.prazoEmDias = prazoEmDias;
        this.politica = politica;
    }
}
