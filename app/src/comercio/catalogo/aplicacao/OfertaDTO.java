package comercio.catalogo.aplicacao;

import comercio.nucleo.Dinheiro;

/** Outra oferta do mesmo produto, exibida em bloco separado na ficha (RN-B09). */
public final class OfertaDTO {

    private final String anuncioId;
    private final String vendedorNome;
    private final Dinheiro preco;
    private final String condicao;

    public OfertaDTO(String anuncioId, String vendedorNome, Dinheiro preco, String condicao) {
        this.anuncioId = anuncioId;
        this.vendedorNome = vendedorNome;
        this.preco = preco;
        this.condicao = condicao;
    }

    public String anuncioId() {
        return anuncioId;
    }

    public String vendedorNome() {
        return vendedorNome;
    }

    public Dinheiro preco() {
        return preco;
    }

    public String condicao() {
        return condicao;
    }
}
