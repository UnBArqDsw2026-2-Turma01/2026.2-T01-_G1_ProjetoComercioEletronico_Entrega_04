package comercio.catalogo.aplicacao;

/** Uma opcao de filtro e quantos resultados restarao se ela for aplicada (RN-A03). */
public final class FacetaDTO {

    private final String nome;
    private final String opcao;
    private final int contagemPrevia;

    public FacetaDTO(String nome, String opcao, int contagemPrevia) {
        this.nome = nome;
        this.opcao = opcao;
        this.contagemPrevia = contagemPrevia;
    }

    public String nome() {
        return nome;
    }

    public String opcao() {
        return opcao;
    }

    public int contagemPrevia() {
        return contagemPrevia;
    }
}
