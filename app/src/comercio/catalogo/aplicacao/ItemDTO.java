package comercio.catalogo.aplicacao;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import comercio.nucleo.Dinheiro;

/** Um anuncio como aparece na listagem: os dados do cartao e os selos acrescentados pelos decoradores. */
public final class ItemDTO {

    private final String anuncioId;
    private final String titulo;
    private final String categoria;
    private final String imagem;
    private final Dinheiro preco;
    private final String condicao;
    private final String vendedorId;
    private final String vendedorNome;
    private final String tipoVendedor;
    private final Map<String, String> selos;

    /**
     * @param imagem caminho da primeira imagem do produto, ou null quando ele nao tem imagem
     */
    public ItemDTO(String anuncioId, String titulo, String categoria, String imagem, Dinheiro preco,
            String condicao, String vendedorId, String vendedorNome, String tipoVendedor, Map<String, String> selos) {
        this.anuncioId = anuncioId;
        this.titulo = titulo;
        this.categoria = categoria;
        this.imagem = imagem;
        this.preco = preco;
        this.condicao = condicao;
        this.vendedorId = vendedorId;
        this.vendedorNome = vendedorNome;
        this.tipoVendedor = tipoVendedor;
        this.selos = selos == null ? new LinkedHashMap<String, String>() : new LinkedHashMap<String, String>(selos);
    }

    public String anuncioId() {
        return anuncioId;
    }

    public String titulo() {
        return titulo;
    }

    public String categoria() {
        return categoria;
    }

    public String imagem() {
        return imagem;
    }

    public Dinheiro preco() {
        return preco;
    }

    public String condicao() {
        return condicao;
    }

    public String vendedorId() {
        return vendedorId;
    }

    public String vendedorNome() {
        return vendedorNome;
    }

    public String tipoVendedor() {
        return tipoVendedor;
    }

    /** Selos na ordem em que os decoradores os acrescentaram (reputacao, depois frete e prazo). */
    public Map<String, String> selos() {
        return Collections.unmodifiableMap(selos);
    }
}
