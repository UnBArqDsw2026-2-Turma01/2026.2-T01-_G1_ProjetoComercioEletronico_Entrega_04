package comercio.catalogo.dominio;

import comercio.contas.dominio.TipoVendedor;
import comercio.contas.dominio.Vendedor;
import comercio.nucleo.Dinheiro;
import comercio.nucleo.RegraDeNegocioException;

/**
 * Oferta de um vendedor para um item de catalogo.
 *
 * Origem: decisao 1 do Diagrama de Classes da Entrega 2 — Produto e o item de
 * catalogo e Anuncio e a oferta, com preco, estoque e vendedor proprios
 * (RN-B09 e RN-C04).
 *
 * Na Entrega 3 o Produto estava achatado no titulo e o vendedor era um texto,
 * porque aquele artefato modelava apenas a listagem. Na Entrega 4 o anuncio
 * passa a apontar para um Produto e para a conta do Vendedor, e ganha
 * identidade e estoque: e o que permite ao carrinho limitar a quantidade
 * (RN-B07) e ao pedido agrupar por vendedor (RN-B01). Os metodos de leitura
 * que os decoradores usavam foram mantidos e agora delegam.
 *
 * Autor: Pedro Luciano de Azevedo (dominio compartilhado e contratos).
 * Adaptado na Entrega 4 por: Patrick Anderson Carvalho dos Santos (Produto e
 * Vendedor como objetos, identidade, estoque e status).
 */
public final class Anuncio {

    public enum Condicao { NOVO, USADO }

    private final String id;
    private final Produto produto;
    private final Vendedor vendedor;
    private final Dinheiro preco;
    private final Condicao condicao;
    private int estoque;
    private StatusAnuncio status;

    public Anuncio(String id, Produto produto, Vendedor vendedor, Dinheiro preco, Condicao condicao, int estoque) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Anuncio sem identificador");
        }
        if (produto == null || vendedor == null || preco == null || condicao == null) {
            throw new IllegalArgumentException("Anuncio incompleto: produto, vendedor, preco e condicao sao obrigatorios");
        }
        if (estoque < 0) {
            throw new IllegalArgumentException("Estoque negativo");
        }
        this.id = id;
        this.produto = produto;
        this.vendedor = vendedor;
        this.preco = preco;
        this.condicao = condicao;
        this.estoque = estoque;
        this.status = StatusAnuncio.PUBLICADO;
    }

    public String id() {
        return id;
    }

    public Produto produto() {
        return produto;
    }

    public Vendedor vendedor() {
        return vendedor;
    }

    public Dinheiro preco() {
        return preco;
    }

    public Condicao condicao() {
        return condicao;
    }

    public synchronized int estoque() {
        return estoque;
    }

    public synchronized StatusAnuncio status() {
        return status;
    }

    /** Publicado e com ao menos uma unidade: aparece na busca e pode ser comprado. */
    public synchronized boolean disponivel() {
        return status == StatusAnuncio.PUBLICADO && estoque > 0;
    }

    public synchronized void pausar() {
        status = StatusAnuncio.PAUSADO;
    }

    public synchronized void publicar() {
        status = StatusAnuncio.PUBLICADO;
    }

    /**
     * Retira unidades do estoque disponivel.
     *
     * RN-B07: a quantidade e limitada pelo estoque do anuncio. Se nao houver o
     * bastante, nada e retirado.
     */
    public synchronized void reservar(int quantidade) {
        if (quantidade <= 0) {
            throw new RegraDeNegocioException("RN-B07", "A quantidade deve ser de pelo menos uma unidade");
        }
        if (status != StatusAnuncio.PUBLICADO) {
            throw new RegraDeNegocioException("RN-B07", "O anúncio \"" + titulo() + "\" não está disponível para compra");
        }
        if (quantidade > estoque) {
            throw new RegraDeNegocioException("RN-B07", "Estoque insuficiente de \"" + titulo() + "\": há "
                    + estoque + " unidade(s) e foram pedidas " + quantidade);
        }
        estoque -= quantidade;
    }

    /** Devolve ao estoque unidades antes reservadas. */
    public synchronized void devolver(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade a devolver deve ser positiva");
        }
        estoque += quantidade;
    }

    // ---------------------------------------------------------------------
    // Leitura usada pelos decoradores da busca desde a Entrega 3. Delegam ao
    // Produto e ao Vendedor, que agora sao objetos proprios.

    public String titulo() {
        return produto.titulo();
    }

    public String categoria() {
        return produto.categoria();
    }

    public int pesoEmGramas() {
        return produto.pesoEmGramas();
    }

    public TipoVendedor tipoVendedor() {
        return vendedor.tipo();
    }

    /** Nota atual do vendedor: derivada das avaliacoes, lida no momento da consulta (RN-A05). */
    public double notaVendedor() {
        return vendedor.reputacao().nota();
    }

    public int quantidadeAvaliacoes() {
        return vendedor.reputacao().avaliacoes();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Anuncio && id.equals(((Anuncio) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return titulo() + " - " + preco + " (" + vendedor.nome() + ")";
    }
}
