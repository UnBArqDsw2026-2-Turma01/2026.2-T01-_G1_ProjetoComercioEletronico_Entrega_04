package comercio.catalogo.persistencia;

import java.util.Arrays;
import java.util.List;

import comercio.catalogo.dominio.Anuncio;
import comercio.catalogo.dominio.Anuncio.Condicao;
import comercio.catalogo.dominio.Produto;
import comercio.contas.dominio.Vendedor;
import comercio.nucleo.Dinheiro;

/**
 * Acervo inicial da demonstracao. Deterministico: nenhum valor depende de
 * relogio ou de sorteio, o que torna os testes e o video reproduziveis.
 *
 * Os oito primeiros anuncios sao o acervo usado na demonstracao do Decorator
 * na Entrega 3, na mesma ordem. O restante foi acrescentado para a busca ter
 * mais de uma pagina, mais categorias e os casos que as regras de negocio
 * precisam exercitar. Produtos, lojas e precos sao ficticios.
 *
 * Casos cobertos de proposito:
 * - pro-001 tem tres ofertas, de vendedores e condicoes diferentes (RN-B09);
 * - anu-003 tem uma unica unidade em estoque (RN-B07);
 * - anu-025 esta pausado e nao aparece na busca;
 * - anu-026 custa mais que o limite do cartao da demonstracao.
 */
public final class SementeDoCatalogo {

    private SementeDoCatalogo() {
    }

    /**
     * O acervo completo. Recebe os vendedores ja cadastrados (ven-01 a ven-06):
     * a persistencia do Catalogo conhece o dominio de Contas, nao o repositorio
     * de Contas (DAS 5.1).
     */
    public static void popular(RepositorioDeAnuncios anuncios, List<Vendedor> contas) {
        popularAcervoDaEntrega3(anuncios, contas);

        Produto intra = produto("pro-016", "Fone de ouvido intra-auricular com microfone", "Eletronicos", 40);
        Produto mouse = produto("pro-007", "Mouse sem fio ergonomico", "Informatica", 110);
        Produto notebook = produto("pro-008", "Notebook 14 polegadas 8 GB", "Informatica", 1600);
        Produto monitor = produto("pro-009", "Monitor 24 polegadas Full HD", "Informatica", 3400);
        Produto caixa = produto("pro-010", "Caixa de som portatil", "Eletronicos", 540);
        Produto controle = produto("pro-011", "Controle sem fio para jogos", "Games", 280);
        Produto garrafa = produto("pro-012", "Garrafa termica 1 litro", "Esportes", 420);
        Produto tenis = produto("pro-013", "Tenis de corrida", "Esportes", 640);
        Produto cabo = produto("pro-014", "Cabo USB-C 2 metros", "Acessorios", 50);
        Produto suporte = produto("pro-015", "Suporte articulado para monitor", "Acessorios", 2300);
        Produto webcam = produto("pro-017", "Webcam Full HD", "Informatica", 160);
        Produto mochila = produto("pro-018", "Mochila para notebook", "Acessorios", 700);
        Produto teclado = produto("pro-006", "Teclado mecanico ABNT2", "Games", 900);
        Produto notebookTopo = produto("pro-019", "Notebook 16 polegadas 32 GB", "Informatica", 2100);

        anuncios.salvar(anuncio("anu-009", intra, "ven-01", 7990, Condicao.NOVO, 30, contas));
        anuncios.salvar(anuncio("anu-010", intra, "ven-06", 6490, Condicao.NOVO, 22, contas));
        anuncios.salvar(anuncio("anu-011", mouse, "ven-04", 12990, Condicao.NOVO, 18, contas));
        anuncios.salvar(anuncio("anu-012", mouse, "ven-02", 9990, Condicao.USADO, 3, contas));
        anuncios.salvar(anuncio("anu-013", notebook, "ven-01", 329900, Condicao.NOVO, 4, contas));
        anuncios.salvar(anuncio("anu-014", notebook, "ven-03", 219900, Condicao.USADO, 2, contas));
        anuncios.salvar(anuncio("anu-015", monitor, "ven-04", 89900, Condicao.NOVO, 7, contas));
        anuncios.salvar(anuncio("anu-016", caixa, "ven-01", 19990, Condicao.NOVO, 20, contas));
        anuncios.salvar(anuncio("anu-017", caixa, "ven-05", 17990, Condicao.NOVO, 9, contas));
        anuncios.salvar(anuncio("anu-018", controle, "ven-04", 27990, Condicao.NOVO, 11, contas));
        anuncios.salvar(anuncio("anu-019", garrafa, "ven-05", 8990, Condicao.NOVO, 25, contas));
        anuncios.salvar(anuncio("anu-020", tenis, "ven-05", 34990, Condicao.NOVO, 10, contas));
        anuncios.salvar(anuncio("anu-021", cabo, "ven-06", 1990, Condicao.NOVO, 100, contas));
        anuncios.salvar(anuncio("anu-022", suporte, "ven-06", 15990, Condicao.NOVO, 6, contas));
        anuncios.salvar(anuncio("anu-023", webcam, "ven-02", 14990, Condicao.NOVO, 5, contas));
        anuncios.salvar(anuncio("anu-024", mochila, "ven-06", 12990, Condicao.NOVO, 14, contas));

        Anuncio pausado = anuncio("anu-025", teclado, "ven-02", 39900, Condicao.USADO, 1, contas);
        pausado.pausar();
        anuncios.salvar(pausado);

        anuncios.salvar(anuncio("anu-026", notebookTopo, "ven-01", 649900, Condicao.NOVO, 3, contas));
    }

    /** Os oito anuncios da demonstracao do Decorator na Entrega 3, na ordem original. */
    public static void popularAcervoDaEntrega3(RepositorioDeAnuncios anuncios, List<Vendedor> contas) {
        Produto bluetooth = produto("pro-001", "Fone de ouvido bluetooth XZ-200", "Eletronicos", 250);
        Produto comFio = produto("pro-002", "Fone de ouvido com fio P2", "Eletronicos", 120);
        Produto gamer = produto("pro-003", "Fone de ouvido gamer RGB", "Games", 1200);
        Produto esportivo = produto("pro-004", "Fone de ouvido esportivo à prova d'água", "Esportes", 180);
        Produto capa = produto("pro-005", "Capa para fone de ouvido", "Acessorios", 60);
        Produto teclado = produto("pro-006", "Teclado mecanico ABNT2", "Games", 900);

        anuncios.salvar(anuncio("anu-001", bluetooth, "ven-01", 24990, Condicao.NOVO, 12, contas));
        anuncios.salvar(anuncio("anu-002", bluetooth, "ven-02", 21990, Condicao.NOVO, 5, contas));
        anuncios.salvar(anuncio("anu-003", bluetooth, "ven-03", 13500, Condicao.USADO, 1, contas));
        anuncios.salvar(anuncio("anu-004", comFio, "ven-01", 4990, Condicao.NOVO, 40, contas));
        anuncios.salvar(anuncio("anu-005", gamer, "ven-04", 31990, Condicao.NOVO, 8, contas));
        anuncios.salvar(anuncio("anu-006", esportivo, "ven-05", 17900, Condicao.NOVO, 15, contas));
        anuncios.salvar(anuncio("anu-007", capa, "ven-06", 2990, Condicao.NOVO, 60, contas));
        anuncios.salvar(anuncio("anu-008", teclado, "ven-04", 45900, Condicao.NOVO, 6, contas));
    }

    private static Produto produto(String id, String titulo, String categoria, int pesoEmGramas) {
        String imagem = "img/" + categoria.toLowerCase() + ".svg";
        return new Produto(id, titulo, categoria, pesoEmGramas, Arrays.asList(imagem));
    }

    private static Anuncio anuncio(String id, Produto produto, String vendedorId, long centavos, Condicao condicao,
            int estoque, List<Vendedor> contas) {
        return new Anuncio(id, produto, vendedor(vendedorId, contas), Dinheiro.emReais(centavos), condicao, estoque);
    }

    private static Vendedor vendedor(String id, List<Vendedor> contas) {
        for (Vendedor vendedor : contas) {
            if (vendedor.id().equals(id)) {
                return vendedor;
            }
        }
        throw new IllegalArgumentException("A semente do catalogo precisa do vendedor " + id);
    }
}
