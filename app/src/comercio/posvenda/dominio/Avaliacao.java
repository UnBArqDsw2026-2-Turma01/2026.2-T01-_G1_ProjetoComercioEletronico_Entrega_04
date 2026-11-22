package comercio.posvenda.dominio;

import comercio.nucleo.RegraDeNegocioException;

/**
 * Avaliacao de um item comprado (UC17).
 *
 * Origem: classe Avaliacao do Diagrama de Classes da Entrega 2, que aponta
 * para Anuncio, e nao para Produto (decisao 1): o comprador avalia a oferta
 * de um vendedor. E dela que a reputacao do vendedor e derivada (RN-A05,
 * decisao 9, o atributo /reputacao).
 *
 * Imutavel: uma avaliacao registrada nao muda. A regra "uma por item do
 * pedido" e garantida pelo Servico de Pos-venda, que conhece as demais.
 *
 * Autor: Pedro Luciano de Azevedo (Entrega 4).
 */
public final class Avaliacao {

    private final String id;
    private final String pedidoId;
    private final String anuncioId;
    private final String vendedorId;
    private final String compradorId;
    private final int nota;
    private final String comentario;

    public Avaliacao(String id, String pedidoId, String anuncioId, String vendedorId, String compradorId, int nota,
            String comentario) {
        if (id == null || pedidoId == null || anuncioId == null || vendedorId == null || compradorId == null) {
            throw new IllegalArgumentException("Avaliacao incompleta");
        }
        if (nota < 1 || nota > 5) {
            throw new RegraDeNegocioException("RN-A05", "A nota da avaliação vai de 1 a 5");
        }
        this.id = id;
        this.pedidoId = pedidoId;
        this.anuncioId = anuncioId;
        this.vendedorId = vendedorId;
        this.compradorId = compradorId;
        this.nota = nota;
        this.comentario = comentario == null ? "" : comentario.trim();
    }

    public String id() {
        return id;
    }

    public String pedidoId() {
        return pedidoId;
    }

    public String anuncioId() {
        return anuncioId;
    }

    public String vendedorId() {
        return vendedorId;
    }

    public String compradorId() {
        return compradorId;
    }

    public int nota() {
        return nota;
    }

    public String comentario() {
        return comentario;
    }
}
