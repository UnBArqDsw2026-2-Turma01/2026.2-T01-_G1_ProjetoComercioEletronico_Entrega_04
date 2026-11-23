package comercio.contas.dominio;

/**
 * Loja oficial ou vendedor autonomo.
 *
 * E enumeracao, e nao subclasse de Vendedor: a diferenca observada entre os
 * dois e de visibilidade no ranking, nao de comportamento (decisao 8 do
 * Diagrama de Classes da Entrega 2).
 */
public enum TipoVendedor {
    LOJA_OFICIAL("Loja oficial"),
    AUTONOMO("Vendedor autônomo");

    private final String rotulo;

    TipoVendedor(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }
}
