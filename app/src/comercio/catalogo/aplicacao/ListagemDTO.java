package comercio.catalogo.aplicacao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Resultado de uma busca, pronto para a camada de Controle.
 *
 * Alem dos itens, carrega a URL que reproduz a mesma consulta (RN-A06) e o
 * "mecanismo": a descricao da cadeia de decoradores que o servico montou para
 * aquela busca. E o que a tela exibe para tornar visivel que o resultado saiu
 * de uma pilha de refinamentos, e nao de uma classe so.
 */
public final class ListagemDTO {

    private final List<ItemDTO> itens;
    private final List<FacetaDTO> facetas;
    private final List<String> termosAlternativos;
    private final int total;
    private final int pagina;
    private final int paginas;
    private final String url;
    private final String mecanismo;

    /**
     * @param total   quantidade de resultados antes da paginacao
     * @param paginas quantidade de paginas; zero quando nao ha resultado
     */
    public ListagemDTO(List<ItemDTO> itens, List<FacetaDTO> facetas, List<String> termosAlternativos, int total,
            int pagina, int paginas, String url, String mecanismo) {
        this.itens = new ArrayList<ItemDTO>(itens);
        this.facetas = new ArrayList<FacetaDTO>(facetas);
        this.termosAlternativos = new ArrayList<String>(termosAlternativos);
        this.total = total;
        this.pagina = pagina;
        this.paginas = paginas;
        this.url = url;
        this.mecanismo = mecanismo;
    }

    public List<ItemDTO> itens() {
        return Collections.unmodifiableList(itens);
    }

    public List<FacetaDTO> facetas() {
        return Collections.unmodifiableList(facetas);
    }

    public List<String> termosAlternativos() {
        return Collections.unmodifiableList(termosAlternativos);
    }

    public int total() {
        return total;
    }

    public int pagina() {
        return pagina;
    }

    public int paginas() {
        return paginas;
    }

    public String url() {
        return url;
    }

    public String mecanismo() {
        return mecanismo;
    }
}
