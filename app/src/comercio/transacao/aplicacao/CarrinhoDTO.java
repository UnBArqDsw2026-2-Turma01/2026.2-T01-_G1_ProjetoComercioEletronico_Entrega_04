package comercio.transacao.aplicacao;

import java.util.Collections;
import java.util.List;

import comercio.nucleo.Dinheiro;

/**
 * O carrinho como a tela precisa ve-lo: itens agrupados por vendedor (RN-B01)
 * e o resumo dos selecionados (RF-B02). Estrutura de dados, sem comportamento.
 */
public final class CarrinhoDTO {

    public static final class Item {
        public final String anuncioId;
        public final String titulo;
        public final Dinheiro precoUnitario;
        public final int quantidade;
        public final int estoque;
        public final boolean selecionado;
        public final Dinheiro subtotal;

        public Item(String anuncioId, String titulo, Dinheiro precoUnitario, int quantidade, int estoque,
                boolean selecionado, Dinheiro subtotal) {
            this.anuncioId = anuncioId;
            this.titulo = titulo;
            this.precoUnitario = precoUnitario;
            this.quantidade = quantidade;
            this.estoque = estoque;
            this.selecionado = selecionado;
            this.subtotal = subtotal;
        }
    }

    public static final class Grupo {
        public final String vendedorId;
        public final String vendedorNome;
        public final List<Item> itens;

        public Grupo(String vendedorId, String vendedorNome, List<Item> itens) {
            this.vendedorId = vendedorId;
            this.vendedorNome = vendedorNome;
            this.itens = Collections.unmodifiableList(itens);
        }
    }

    public final List<Grupo> grupos;
    public final int unidades;
    public final Dinheiro subtotal;

    public CarrinhoDTO(List<Grupo> grupos, int unidades, Dinheiro subtotal) {
        this.grupos = Collections.unmodifiableList(grupos);
        this.unidades = unidades;
        this.subtotal = subtotal;
    }
}
