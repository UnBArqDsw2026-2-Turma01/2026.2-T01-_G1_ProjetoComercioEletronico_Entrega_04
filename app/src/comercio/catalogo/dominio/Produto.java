package comercio.catalogo.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Item de catalogo: o produto que existe uma vez, independentemente de quem o vende.
 *
 * Origem: decisao 1 do Diagrama de Classes da Entrega 2. RN-C04 registra que a
 * plataforma tenta vincular toda oferta nova a um item ja existente no
 * catalogo; RN-B09, que a ficha de um produto exibe uma oferta principal e as
 * demais em bloco separado. Uma classe so, com preco e vendedor, nao
 * representaria nem uma coisa nem outra. O que e transacional (carrinho,
 * pedido, avaliacao) aponta para Anuncio, e nao para Produto.
 */
public final class Produto {

    private final String id;
    private final String titulo;
    private final String categoria;
    private final int pesoEmGramas;
    private final List<String> imagens;

    public Produto(String id, String titulo, String categoria, int pesoEmGramas, List<String> imagens) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Produto sem identificador");
        }
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("Produto sem titulo");
        }
        if (categoria == null || categoria.trim().isEmpty()) {
            throw new IllegalArgumentException("Produto sem categoria");
        }
        if (pesoEmGramas <= 0) {
            throw new IllegalArgumentException("Peso do produto deve ser positivo");
        }
        this.id = id;
        this.titulo = titulo;
        this.categoria = categoria;
        this.pesoEmGramas = pesoEmGramas;
        this.imagens = imagens == null ? new ArrayList<String>() : new ArrayList<String>(imagens);
    }

    public String id() {
        return id;
    }

    public String titulo() {
        return titulo;
    }

    public String categoria() {
        return categoria;
    }

    public int pesoEmGramas() {
        return pesoEmGramas;
    }

    public List<String> imagens() {
        return Collections.unmodifiableList(imagens);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Produto && id.equals(((Produto) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return titulo + " (" + id + ")";
    }
}
