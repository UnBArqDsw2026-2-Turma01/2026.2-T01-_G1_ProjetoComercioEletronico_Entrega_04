package comercio.catalogo.dominio.refinamento;

import java.util.ArrayList;
import java.util.List;

import comercio.catalogo.dominio.ConsultaCatalogo;
import comercio.catalogo.dominio.FonteDeResultados;
import comercio.catalogo.dominio.ItemListagem;
import comercio.catalogo.dominio.Listagem;

/**
 * Decorador concreto (ConcreteDecorator, GAMMA et al., 1995): corta a listagem
 * na pagina pedida.
 *
 * Origem: UC30 "Paginar resultados" da Visao de Casos de Uso do DAS (fonte:
 * Subequipe 02, "Paginar Resultados"), um «extend» de UC01: a busca e completa
 * sem paginacao, e a paginacao so existe porque a busca ja trouxe resultados.
 *
 * Este e o unico refinamento que precisa de lugar fixo na cadeia: o ULTIMO.
 * Os demais decoradores trabalham sobre o conjunto inteiro (o filtro conta as
 * facetas sobre todos os resultados, a ordenacao ordena todos eles), e so
 * depois disso faz sentido escolher quais seis aparecem. Se a pagina fosse
 * cortada antes do filtro, a pagina 1 de "condicao: USADO" seria "os usados
 * entre os seis primeiros", e nao "os seis primeiros usados". O teste
 * RefinamentosTeste registra esse caso.
 *
 * Pagina alem do fim devolve lista vazia, sem erro: e o que acontece quando o
 * comprador volta a uma URL antiga depois de o acervo mudar (RN-A06).
 *
 * Nao altera a listagem recebida: devolve uma copia via Listagem.paginada,
 * que guarda o total de antes do corte.
 *
 * Autor: Pedro Luciano de Azevedo (refinamento ComPaginacao, Entrega 4).
 */
public final class ComPaginacao extends RefinamentoDecorator {

    /** Itens por pagina. */
    public static final int TAMANHO_DA_PAGINA = 6;

    public ComPaginacao(FonteDeResultados origem) {
        super(origem);
    }

    @Override
    public Listagem buscar(ConsultaCatalogo consulta) {
        Listagem recebida = super.buscar(consulta);

        int pagina = consulta.pagina() < 1 ? 1 : consulta.pagina();
        List<ItemListagem> todos = recebida.itens();
        long inicio = (long) (pagina - 1) * TAMANHO_DA_PAGINA;

        List<ItemListagem> daPagina = new ArrayList<ItemListagem>(TAMANHO_DA_PAGINA);
        if (inicio < todos.size()) {
            int fim = (int) Math.min(inicio + TAMANHO_DA_PAGINA, todos.size());
            daPagina.addAll(todos.subList((int) inicio, fim));
        }
        return recebida.paginada(daPagina);
    }

    /** Quantas paginas cabem um total de resultados. Zero quando nao ha resultado. */
    public static int quantidadeDePaginas(int total) {
        return total <= 0 ? 0 : (total + TAMANHO_DA_PAGINA - 1) / TAMANHO_DA_PAGINA;
    }
}
