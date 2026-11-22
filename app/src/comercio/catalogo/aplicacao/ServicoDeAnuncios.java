package comercio.catalogo.aplicacao;

import comercio.catalogo.dominio.Anuncio;
import comercio.catalogo.persistencia.RepositorioDeAnuncios;

/**
 * Servico de Anuncios (Diagrama de Componentes da Entrega 2): fornece IAnuncio.
 *
 * E por aqui que o carrinho e o checkout consultam uma oferta e movimentam o
 * seu estoque. A regra de estoque (RN-B07) vive no proprio Anuncio; o servico
 * localiza a oferta e delega.
 */
public final class ServicoDeAnuncios implements IAnuncio {

    private final RepositorioDeAnuncios anuncios;

    public ServicoDeAnuncios(RepositorioDeAnuncios anuncios) {
        this.anuncios = anuncios;
    }

    @Override
    public Anuncio obter(String anuncioId) {
        return anuncios.obter(anuncioId);
    }

    @Override
    public void reservar(String anuncioId, int quantidade) {
        anuncios.obter(anuncioId).reservar(quantidade);
    }

    @Override
    public void devolver(String anuncioId, int quantidade) {
        anuncios.obter(anuncioId).devolver(quantidade);
    }
}
