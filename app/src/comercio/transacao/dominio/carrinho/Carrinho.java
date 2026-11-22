package comercio.transacao.dominio.carrinho;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import comercio.catalogo.dominio.Anuncio;
import comercio.nucleo.Dinheiro;
import comercio.nucleo.NaoEncontradoException;
import comercio.nucleo.RegraDeNegocioException;

/**
 * Carrinho de compras de um comprador.
 *
 * Regras que vivem aqui, todas da Engenharia Reversa da Entrega 1:
 *
 * - RN-B01: os itens sao agrupados por vendedor (porVendedor), porque o pedido
 *   tera um envio por vendedor;
 * - RN-B07 e RF-B05: a quantidade de cada item nao passa do estoque do anuncio;
 * - RF-B02: um item pode ser desselecionado sem ser removido, e o resumo
 *   considera apenas os selecionados.
 *
 * O carrinho nao reserva estoque: reserva e ato do checkout. Por isso a
 * quantidade e conferida de novo, de forma atomica, ao finalizar a compra.
 */
public final class Carrinho {

    private final String compradorId;
    private final Map<String, ItemCarrinho> itens = new LinkedHashMap<String, ItemCarrinho>();

    public Carrinho(String compradorId) {
        this.compradorId = compradorId;
    }

    public String compradorId() {
        return compradorId;
    }

    /** Acrescenta unidades de um anuncio. Se ele ja esta no carrinho, soma a quantidade. */
    public synchronized void adicionar(Anuncio anuncio, int quantidade) {
        if (!anuncio.disponivel()) {
            throw new RegraDeNegocioException("RN-B07", "\"" + anuncio.titulo() + "\" não está disponível para compra");
        }
        ItemCarrinho existente = itens.get(anuncio.id());
        if (existente == null) {
            itens.put(anuncio.id(), new ItemCarrinho(anuncio, quantidade));
        } else {
            if (quantidade < 1) {
                throw new RegraDeNegocioException("RN-B07", "A quantidade mínima é de uma unidade");
            }
            existente.definirQuantidade(existente.quantidade() + quantidade);
        }
    }

    public synchronized void alterarQuantidade(String anuncioId, int quantidade) {
        item(anuncioId).definirQuantidade(quantidade);
    }

    public synchronized void selecionar(String anuncioId, boolean selecionado) {
        item(anuncioId).selecionar(selecionado);
    }

    public synchronized void remover(String anuncioId) {
        item(anuncioId);
        itens.remove(anuncioId);
    }

    /** Retira do carrinho os itens que acabaram de virar pedido. */
    public synchronized void removerSelecionados() {
        List<String> comprados = new ArrayList<String>();
        for (ItemCarrinho item : itens.values()) {
            if (item.selecionado()) {
                comprados.add(item.anuncio().id());
            }
        }
        for (String id : comprados) {
            itens.remove(id);
        }
    }

    public synchronized List<ItemCarrinho> itens() {
        return new ArrayList<ItemCarrinho>(itens.values());
    }

    public synchronized List<ItemCarrinho> selecionados() {
        List<ItemCarrinho> selecionados = new ArrayList<ItemCarrinho>();
        for (ItemCarrinho item : itens.values()) {
            if (item.selecionado()) {
                selecionados.add(item);
            }
        }
        return selecionados;
    }

    /** RN-B01: os itens agrupados por vendedor, na ordem em que cada vendedor apareceu. */
    public synchronized Map<String, List<ItemCarrinho>> porVendedor() {
        return agrupar(itens());
    }

    public static Map<String, List<ItemCarrinho>> agrupar(List<ItemCarrinho> lista) {
        Map<String, List<ItemCarrinho>> grupos = new LinkedHashMap<String, List<ItemCarrinho>>();
        for (ItemCarrinho item : lista) {
            String vendedorId = item.anuncio().vendedor().id();
            List<ItemCarrinho> grupo = grupos.get(vendedorId);
            if (grupo == null) {
                grupo = new ArrayList<ItemCarrinho>();
                grupos.put(vendedorId, grupo);
            }
            grupo.add(item);
        }
        return grupos;
    }

    /** RF-B02: soma apenas os itens selecionados. */
    public synchronized Dinheiro subtotal() {
        Dinheiro soma = Dinheiro.zero();
        for (ItemCarrinho item : itens.values()) {
            if (item.selecionado()) {
                soma = soma.somar(item.subtotal());
            }
        }
        return soma;
    }

    public synchronized boolean vazio() {
        return itens.isEmpty();
    }

    private ItemCarrinho item(String anuncioId) {
        ItemCarrinho item = itens.get(anuncioId);
        if (item == null) {
            throw new NaoEncontradoException("O anúncio " + anuncioId + " não está no carrinho");
        }
        return item;
    }
}
