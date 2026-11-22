package comercio.catalogo.aplicacao;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import comercio.nucleo.Dinheiro;

/** Um anuncio como aparece na listagem: os dados do cartao e os selos acrescentados pelos decoradores. */
public final class ItemDTO {

    private final String anuncioId;
    private final String titulo;
    private final Dinheiro preco;
    private final String condicao;
    private final String vendedorId;
    private final String vendedorNome;
    private final String tipoVendedor;
    private final Map<String, String> selos;

    public ItemDTO(String anuncioId, String titulo, Dinheiro preco, String condicao, String vendedorId,
            String vendedorNome, String tipoVendedor, Map<String, String> selos) {
        this.anuncioId = anuncioId;
        this.titulo = titulo;
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

    /** Selos na ordem em que os decoradores os acrescentaram: frete, prazo, reputacao. */
    public Map<String, String> selos() {
        return Collections.unmodifiableMap(selos);
    }
}
