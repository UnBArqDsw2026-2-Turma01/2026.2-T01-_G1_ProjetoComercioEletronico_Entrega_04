package comercio.catalogo.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * O que a busca devolve: os itens encontrados, as facetas com contagem previa e,
 * quando nao ha resultado, os termos alternativos.
 *
 * Origem: decisoes 2 e 3 do Diagrama de Classes da Entrega 2.
 * Listagem.termosAlternativos vem de RN-A01 e RF-A06 — a busca sem resultado
 * oferece termos alternativos em vez de uma tela vazia.
 *
 * Imutavel: cada decorador recebe uma listagem e devolve outra, com os itens
 * filtrados, ordenados ou enriquecidos. E esse contrato que mantem a cadeia
 * previsivel, independentemente da ordem em que ela for montada.
 *
 * Na Entrega 4 a listagem passou a saber se foi paginada (UC30). Depois da
 * paginacao, itens() traz so a pagina pedida, mas total() continua
 * respondendo quantos resultados a busca encontrou: e esse numero que a tela
 * usa para dizer "8 resultados" e calcular quantas paginas existem.
 *
 * Autor: Pedro Luciano de Azevedo (dominio compartilhado e contratos).
 */
public final class Listagem {

    /** Valor de totalAntesDaPaginacao quando a listagem ainda nao foi paginada. */
    private static final int NAO_PAGINADA = -1;

    private final List<ItemListagem> itens;
    private final List<Faceta> facetas;
    private final List<String> termosAlternativos;
    private final int totalAntesDaPaginacao;

    public Listagem(List<ItemListagem> itens) {
        this(itens, new ArrayList<Faceta>(), new ArrayList<String>(), NAO_PAGINADA);
    }

    private Listagem(List<ItemListagem> itens, List<Faceta> facetas, List<String> termosAlternativos,
                     int totalAntesDaPaginacao) {
        this.itens = itens;
        this.facetas = facetas;
        this.termosAlternativos = termosAlternativos;
        this.totalAntesDaPaginacao = totalAntesDaPaginacao;
    }

    /**
     * Troca os itens. Se a listagem ja foi paginada, o total encontrado e
     * mantido: um refinamento de enriquecimento aplicado depois da paginacao
     * continua vendo o total certo. Quem remove itens precisa vir antes de
     * ComPaginacao (regra de montagem da cadeia no Servico de Catalogo).
     */
    public Listagem comItens(List<ItemListagem> novosItens) {
        return new Listagem(novosItens, facetas, termosAlternativos, totalAntesDaPaginacao);
    }

    public Listagem comFacetas(List<Faceta> novasFacetas) {
        return new Listagem(itens, novasFacetas, termosAlternativos, totalAntesDaPaginacao);
    }

    public Listagem comTermosAlternativos(List<String> novosTermos) {
        return new Listagem(itens, facetas, novosTermos, totalAntesDaPaginacao);
    }

    /**
     * UC30: devolve uma listagem so com os itens da pagina, guardando quantos
     * resultados havia antes de cortar.
     */
    public Listagem paginada(List<ItemListagem> itensDaPagina) {
        return new Listagem(itensDaPagina, facetas, termosAlternativos, total());
    }

    public List<ItemListagem> itens() {
        return Collections.unmodifiableList(itens);
    }

    public List<Faceta> facetas() {
        return Collections.unmodifiableList(facetas);
    }

    public List<String> termosAlternativos() {
        return Collections.unmodifiableList(termosAlternativos);
    }

    /** Quantidade de resultados da busca. Depois da paginacao, continua sendo o total, e nao o da pagina. */
    public int total() {
        return totalAntesDaPaginacao == NAO_PAGINADA ? itens.size() : totalAntesDaPaginacao;
    }

    public boolean paginada() {
        return totalAntesDaPaginacao != NAO_PAGINADA;
    }

    /** true quando a busca nao encontrou nada (e nao apenas quando a pagina pedida esta vazia). */
    public boolean vazia() {
        return total() == 0;
    }
}
