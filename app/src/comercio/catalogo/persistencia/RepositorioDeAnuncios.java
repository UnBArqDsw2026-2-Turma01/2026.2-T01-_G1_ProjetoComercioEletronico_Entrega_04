package comercio.catalogo.persistencia;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import comercio.catalogo.dominio.Anuncio;
import comercio.catalogo.dominio.StatusAnuncio;
import comercio.nucleo.NaoEncontradoException;

/**
 * Repositorio de anuncios, em memoria. E a base "Catalogo e Ofertas" que o
 * Diagrama de Componentes da Entrega 2 mostra compartilhada pelo Servico de
 * Catalogo e pelo Servico de Anuncios.
 */
public final class RepositorioDeAnuncios {

    private final Map<String, Anuncio> porId = new LinkedHashMap<String, Anuncio>();

    public synchronized void salvar(Anuncio anuncio) {
        porId.put(anuncio.id(), anuncio);
    }

    public synchronized Anuncio obter(String id) {
        Anuncio anuncio = porId.get(id);
        if (anuncio == null) {
            throw new NaoEncontradoException("O anúncio " + id + " não existe");
        }
        return anuncio;
    }

    /** Anuncios que aparecem na busca, na ordem em que foram cadastrados. */
    public synchronized List<Anuncio> publicados() {
        List<Anuncio> publicados = new ArrayList<Anuncio>();
        for (Anuncio anuncio : porId.values()) {
            if (anuncio.status() == StatusAnuncio.PUBLICADO) {
                publicados.add(anuncio);
            }
        }
        return publicados;
    }

    /** As ofertas publicadas de um mesmo produto (RN-B09). */
    public synchronized List<Anuncio> doProduto(String produtoId) {
        List<Anuncio> ofertas = new ArrayList<Anuncio>();
        for (Anuncio anuncio : porId.values()) {
            if (anuncio.produto().id().equals(produtoId) && anuncio.status() == StatusAnuncio.PUBLICADO) {
                ofertas.add(anuncio);
            }
        }
        return ofertas;
    }

    public synchronized int total() {
        return porId.size();
    }
}
