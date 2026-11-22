package comercio.transacao.dominio.carrinho;

import comercio.catalogo.dominio.Anuncio;
import comercio.nucleo.Dinheiro;
import comercio.nucleo.RegraDeNegocioException;

/**
 * Item do carrinho: um anuncio, a quantidade desejada e se esta selecionado
 * para a compra.
 *
 * E classe distinta de ItemPedido (decisao 6 do Diagrama de Classes da Entrega
 * 2). Duas regras so fazem sentido deste lado do checkout:
 *
 * - RF-B02: selecionar e desselecionar um item sem remove-lo;
 * - RN-B07: a quantidade e limitada pelo estoque do anuncio.
 *
 * Aponta para o anuncio vivo: o preco exibido e o preco de agora. O preco so e
 * congelado quando o item vira ItemPedido.
 */
public final class ItemCarrinho {

    private final Anuncio anuncio;
    private int quantidade;
    private boolean selecionado;

    ItemCarrinho(Anuncio anuncio, int quantidade) {
        this.anuncio = anuncio;
        this.selecionado = true;
        definirQuantidade(quantidade);
    }

    void definirQuantidade(int nova) {
        if (nova < 1) {
            throw new RegraDeNegocioException("RN-B07", "A quantidade mínima é de uma unidade");
        }
        if (nova > anuncio.estoque()) {
            throw new RegraDeNegocioException("RN-B07", "Há apenas " + anuncio.estoque()
                    + " unidade(s) de \"" + anuncio.titulo() + "\" em estoque");
        }
        this.quantidade = nova;
    }

    void selecionar(boolean valor) {
        this.selecionado = valor;
    }

    public Anuncio anuncio() {
        return anuncio;
    }

    public int quantidade() {
        return quantidade;
    }

    public boolean selecionado() {
        return selecionado;
    }

    public Dinheiro subtotal() {
        return anuncio.preco().multiplicar(quantidade);
    }
}
