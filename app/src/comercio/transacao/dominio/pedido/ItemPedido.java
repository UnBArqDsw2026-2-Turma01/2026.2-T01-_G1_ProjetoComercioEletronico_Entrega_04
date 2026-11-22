package comercio.transacao.dominio.pedido;

import comercio.nucleo.Dinheiro;

/**
 * Item de um pedido confirmado.
 *
 * E classe distinta de ItemCarrinho (decisao 6 do Diagrama de Classes da
 * Entrega 2). O item do carrinho aponta para o anuncio vivo; o item do pedido
 * guarda o preco no momento da compra. Se ele lesse o preco corrente do
 * anuncio, uma promocao posterior mudaria o valor de um pedido ja pago.
 *
 * Imutavel: o que foi acordado em uma transacao nao muda por efeito colateral.
 */
public final class ItemPedido {

    private final String anuncioId;
    private final String titulo;
    private final String vendedorId;
    private final int quantidade;
    private final Dinheiro precoNoMomento;

    public ItemPedido(String anuncioId, String titulo, String vendedorId, int quantidade, Dinheiro precoNoMomento) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Item de pedido com quantidade invalida");
        }
        this.anuncioId = anuncioId;
        this.titulo = titulo;
        this.vendedorId = vendedorId;
        this.quantidade = quantidade;
        this.precoNoMomento = precoNoMomento;
    }

    public String anuncioId() {
        return anuncioId;
    }

    public String titulo() {
        return titulo;
    }

    public String vendedorId() {
        return vendedorId;
    }

    public int quantidade() {
        return quantidade;
    }

    public Dinheiro precoNoMomento() {
        return precoNoMomento;
    }

    public Dinheiro subtotal() {
        return precoNoMomento.multiplicar(quantidade);
    }
}
