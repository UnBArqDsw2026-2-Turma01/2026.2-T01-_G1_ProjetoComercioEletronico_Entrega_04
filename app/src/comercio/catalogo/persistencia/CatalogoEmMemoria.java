package comercio.catalogo.persistencia;

import java.util.ArrayList;
import java.util.List;

import comercio.catalogo.dominio.Anuncio;
import comercio.catalogo.dominio.ConsultaCatalogo;
import comercio.catalogo.dominio.FonteDeResultados;
import comercio.catalogo.dominio.ItemListagem;
import comercio.catalogo.dominio.Listagem;

/**
 * Componente concreto (ConcreteComponent): o catalogo cru.
 *
 * Faz o minimo que a busca precisa fazer — casar o termo com o titulo do
 * anuncio — e nada mais: nao filtra por faceta, nao ordena, nao calcula frete e
 * nao exibe reputacao. Tudo isso e responsabilidade dos decoradores, e deixar o
 * componente concreto pobre de proposito e o que torna visivel o que cada
 * refinamento acrescenta.
 *
 * Na Entrega 3 o acervo era uma lista fixa dentro desta classe. Na Entrega 4
 * ele vem do RepositorioDeAnuncios, a mesma base que o carrinho e o checkout
 * consultam: o anuncio que a busca mostra e o anuncio que o pedido reserva.
 * So entram na busca os anuncios publicados.
 *
 * Autor: Pedro Luciano de Azevedo (contratos e componente base).
 * Adaptado na Entrega 4 por: Patrick Anderson Carvalho dos Santos (leitura do
 * repositorio no lugar do acervo fixo).
 */
public final class CatalogoEmMemoria implements FonteDeResultados {

    private final RepositorioDeAnuncios anuncios;

    public CatalogoEmMemoria(RepositorioDeAnuncios anuncios) {
        if (anuncios == null) {
            throw new IllegalArgumentException("O catalogo precisa de um repositorio de anuncios");
        }
        this.anuncios = anuncios;
    }

    @Override
    public Listagem buscar(ConsultaCatalogo consulta) {
        List<ItemListagem> encontrados = new ArrayList<ItemListagem>();
        String termo = consulta.termo().toLowerCase();
        for (Anuncio anuncio : anuncios.publicados()) {
            if (termo.isEmpty() || anuncio.titulo().toLowerCase().contains(termo)) {
                encontrados.add(new ItemListagem(anuncio));
            }
        }
        return new Listagem(encontrados);
    }

    @Override
    public String descricao() {
        return "CatalogoEmMemoria(" + anuncios.publicados().size() + " anuncios)";
    }
}
