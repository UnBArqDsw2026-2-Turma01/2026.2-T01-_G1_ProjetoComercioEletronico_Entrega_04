package comercio.catalogo.aplicacao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import comercio.nucleo.Dinheiro;

/**
 * A ficha de um anuncio (UC05), com a reputacao do vendedor (UC06), o frete
 * por CEP quando informado (UC07) e as outras ofertas do mesmo produto.
 *
 * Reaproveita ItemDTO para os dados que a ficha tem em comum com o cartao da
 * listagem.
 */
public final class FichaDTO {

    private final ItemDTO item;
    private final String produtoId;
    private final String categoria;
    private final List<String> imagens;
    private final int estoque;
    private final boolean disponivel;
    private final List<Dinheiro> parcelas;
    private final double notaVendedor;
    private final int avaliacoesVendedor;
    private final Dinheiro frete;
    private final Integer prazoDias;
    private final List<OfertaDTO> outrasOfertas;

    /**
     * @param disponivel publicado e com estoque: pode ir para o carrinho
     * @param parcelas   o preco dividido sem perda de centavos (Dinheiro.parcelar)
     * @param frete      nulo quando o comprador nao informou CEP
     * @param prazoDias  nulo quando o comprador nao informou CEP
     */
    public FichaDTO(ItemDTO item, String produtoId, String categoria, List<String> imagens, int estoque,
            boolean disponivel, List<Dinheiro> parcelas, double notaVendedor, int avaliacoesVendedor,
            Dinheiro frete, Integer prazoDias, List<OfertaDTO> outrasOfertas) {
        this.item = item;
        this.produtoId = produtoId;
        this.categoria = categoria;
        this.imagens = new ArrayList<String>(imagens);
        this.estoque = estoque;
        this.disponivel = disponivel;
        this.parcelas = new ArrayList<Dinheiro>(parcelas);
        this.notaVendedor = notaVendedor;
        this.avaliacoesVendedor = avaliacoesVendedor;
        this.frete = frete;
        this.prazoDias = prazoDias;
        this.outrasOfertas = new ArrayList<OfertaDTO>(outrasOfertas);
    }

    public ItemDTO item() {
        return item;
    }

    public String produtoId() {
        return produtoId;
    }

    public String categoria() {
        return categoria;
    }

    public List<String> imagens() {
        return Collections.unmodifiableList(imagens);
    }

    public int estoque() {
        return estoque;
    }

    public boolean disponivel() {
        return disponivel;
    }

    public List<Dinheiro> parcelas() {
        return Collections.unmodifiableList(parcelas);
    }

    public double notaVendedor() {
        return notaVendedor;
    }

    public int avaliacoesVendedor() {
        return avaliacoesVendedor;
    }

    public Dinheiro frete() {
        return frete;
    }

    public Integer prazoDias() {
        return prazoDias;
    }

    public List<OfertaDTO> outrasOfertas() {
        return Collections.unmodifiableList(outrasOfertas);
    }
}
