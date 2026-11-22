package comercio.catalogo;

import java.util.ArrayList;
import java.util.List;

import comercio.catalogo.dominio.Anuncio;
import comercio.catalogo.dominio.ConsultaCatalogo;
import comercio.catalogo.dominio.Faceta;
import comercio.catalogo.dominio.FonteDeResultados;
import comercio.catalogo.dominio.ItemListagem;
import comercio.catalogo.dominio.Listagem;
import comercio.catalogo.dominio.refinamento.ComFiltroDeFacetas;
import comercio.catalogo.dominio.refinamento.ComPaginacao;
import comercio.catalogo.dominio.refinamento.ComTermosAlternativos;
import comercio.catalogo.persistencia.CatalogoEmMemoria;
import comercio.catalogo.persistencia.RepositorioDeAnuncios;
import comercio.catalogo.persistencia.SementeDoCatalogo;
import comercio.contas.persistencia.RepositorioDeContas;
import comercio.contas.persistencia.SementeDeContas;
import comercio.teste.Casos;
import comercio.teste.Suite;
import comercio.teste.Verifica;

/** Os refinamentos da Entrega 4 (paginacao e termos alternativos) e a ordem da cadeia. */
public final class RefinamentosTeste implements Suite {

    private static RepositorioDeAnuncios acervo() {
        RepositorioDeContas contas = new RepositorioDeContas();
        SementeDeContas.popular(contas);
        RepositorioDeAnuncios anuncios = new RepositorioDeAnuncios();
        SementeDoCatalogo.popular(anuncios, contas.vendedores());
        return anuncios;
    }

    private static List<String> titulos(RepositorioDeAnuncios anuncios) {
        List<String> titulos = new ArrayList<String>();
        for (Anuncio anuncio : anuncios.publicados()) {
            titulos.add(anuncio.titulo());
        }
        return titulos;
    }

    /** Fonte fixa com exatamente oito resultados, para a conta da paginacao ser exata. */
    private static FonteDeResultados oitoResultados() {
        final List<ItemListagem> oito = new ArrayList<ItemListagem>();
        for (Anuncio anuncio : acervo().publicados().subList(0, 8)) {
            oito.add(new ItemListagem(anuncio));
        }
        return new FonteDeResultados() {
            @Override
            public Listagem buscar(ConsultaCatalogo consulta) {
                return new Listagem(oito);
            }

            @Override
            public String descricao() {
                return "OitoResultados";
            }
        };
    }

    private static List<String> ids(Listagem listagem) {
        List<String> ids = new ArrayList<String>();
        for (ItemListagem item : listagem.itens()) {
            ids.add(item.anuncio().id());
        }
        return ids;
    }

    @Override
    public void registrar(Casos c) {
        c.caso("UC30: 8 resultados viram pagina 1 com 6 e pagina 2 com 2, e o total e 8 nas duas", () -> {
            FonteDeResultados busca = new ComPaginacao(oitoResultados());
            Listagem primeira = busca.buscar(new ConsultaCatalogo("x").naPagina(1));
            Listagem segunda = busca.buscar(new ConsultaCatalogo("x").naPagina(2));
            Verifica.igual(6, primeira.itens().size());
            Verifica.igual(2, segunda.itens().size());
            Verifica.igual(8, primeira.total());
            Verifica.igual(8, segunda.total());
            Verifica.igual(2, ComPaginacao.quantidadeDePaginas(primeira.total()));
            for (String id : ids(segunda)) {
                Verifica.falso(ids(primeira).contains(id), id + " apareceu nas duas paginas");
            }
        });

        c.caso("UC30: pagina alem do fim devolve lista vazia, sem erro, e mantem o total", () -> {
            Listagem terceira = new ComPaginacao(oitoResultados()).buscar(new ConsultaCatalogo("x").naPagina(3));
            Verifica.igual(0, terceira.itens().size());
            Verifica.igual(8, terceira.total());
            Verifica.falso(terceira.vazia(), "a busca encontrou 8; so a pagina 3 e que nao tem itens");
        });

        c.caso("UC30: pagina menor que 1 e tratada como a primeira", () -> {
            Listagem listagem = new ComPaginacao(oitoResultados()).buscar(new ConsultaCatalogo("x").naPagina(0));
            Verifica.igual(6, listagem.itens().size());
        });

        c.caso("UC30: quantidade de paginas", () -> {
            Verifica.igual(0, ComPaginacao.quantidadeDePaginas(0));
            Verifica.igual(1, ComPaginacao.quantidadeDePaginas(1));
            Verifica.igual(1, ComPaginacao.quantidadeDePaginas(6));
            Verifica.igual(2, ComPaginacao.quantidadeDePaginas(7));
        });

        c.caso("RN-A01: busca sem resultado traz ate tres termos alternativos que compartilham palavra", () -> {
            RepositorioDeAnuncios anuncios = acervo();
            FonteDeResultados busca = new ComTermosAlternativos(new CatalogoEmMemoria(anuncios), titulos(anuncios));
            Listagem listagem = busca.buscar(new ConsultaCatalogo("fones sem marca conhecida"));
            Verifica.verdadeiro(listagem.vazia(), "o termo nao deveria casar com nenhum titulo");
            List<String> termos = listagem.termosAlternativos();
            Verifica.verdadeiro(!termos.isEmpty(), "a busca vazia deveria sugerir termos");
            Verifica.verdadeiro(termos.size() <= ComTermosAlternativos.MAXIMO, "mais de tres termos: " + termos);
            for (String termo : termos) {
                Verifica.contem(termo.toLowerCase(), "fone");
            }
        });

        c.caso("RN-A01: cada termo alternativo, buscado, encontra resultado", () -> {
            RepositorioDeAnuncios anuncios = acervo();
            FonteDeResultados busca = new ComTermosAlternativos(new CatalogoEmMemoria(anuncios), titulos(anuncios));
            for (String termo : busca.buscar(new ConsultaCatalogo("tênis azul")).termosAlternativos()) {
                Verifica.falso(busca.buscar(new ConsultaCatalogo(termo)).vazia(), termo + " nao encontra nada");
            }
            Verifica.igual(1, busca.buscar(new ConsultaCatalogo("tênis azul")).termosAlternativos().size());
        });

        c.caso("RN-A01: busca com resultado nao traz termos alternativos", () -> {
            RepositorioDeAnuncios anuncios = acervo();
            FonteDeResultados busca = new ComTermosAlternativos(new CatalogoEmMemoria(anuncios), titulos(anuncios));
            Listagem listagem = busca.buscar(new ConsultaCatalogo("fone"));
            Verifica.falso(listagem.vazia(), "fone deveria ter resultado");
            Verifica.igual(0, listagem.termosAlternativos().size());
        });

        c.caso("RN-A01: termo sem nenhuma palavra em comum nao inventa alternativa", () -> {
            RepositorioDeAnuncios anuncios = acervo();
            FonteDeResultados busca = new ComTermosAlternativos(new CatalogoEmMemoria(anuncios), titulos(anuncios));
            Verifica.igual(0, busca.buscar(new ConsultaCatalogo("geladeira")).termosAlternativos().size());
        });

        c.caso("RN-A03: a contagem previa de cada faceta e o total obtido ao aplicar aquele filtro", () -> {
            FonteDeResultados busca = new ComFiltroDeFacetas(new CatalogoEmMemoria(acervo()));
            ConsultaCatalogo[] consultas = {
                new ConsultaCatalogo("fone de ouvido"),
                new ConsultaCatalogo(""),
                new ConsultaCatalogo("").comFiltro(ComFiltroDeFacetas.CONDICAO, "NOVO")
            };
            for (ConsultaCatalogo consulta : consultas) {
                Listagem listagem = busca.buscar(consulta);
                Verifica.verdadeiro(!listagem.facetas().isEmpty(), "sem facetas para " + consulta);
                for (Faceta faceta : listagem.facetas()) {
                    int obtido = busca.buscar(consulta.comFiltro(faceta.nome(), faceta.opcao())).total();
                    Verifica.igual(faceta.contagemPrevia(), obtido);
                }
            }
        });

        c.caso("UC30 e RN-A03: filtrar antes de paginar; cortar a pagina antes do filtro perde resultados", () -> {
            RepositorioDeAnuncios anuncios = acervo();
            ConsultaCatalogo usados = new ConsultaCatalogo("").comFiltro(ComFiltroDeFacetas.CONDICAO, "USADO");

            Listagem certa = new ComPaginacao(new ComFiltroDeFacetas(new CatalogoEmMemoria(anuncios))).buscar(usados);
            Listagem errada = new ComFiltroDeFacetas(new ComPaginacao(new CatalogoEmMemoria(anuncios))).buscar(usados);

            int usadosNoAcervo = 0;
            for (Anuncio anuncio : anuncios.publicados()) {
                usadosNoAcervo += anuncio.condicao() == Anuncio.Condicao.USADO ? 1 : 0;
            }
            Verifica.igual(usadosNoAcervo, certa.total());
            Verifica.igual(Math.min(usadosNoAcervo, ComPaginacao.TAMANHO_DA_PAGINA), certa.itens().size());
            for (ItemListagem item : certa.itens()) {
                Verifica.igual(Anuncio.Condicao.USADO, item.anuncio().condicao());
            }
            Verifica.verdadeiro(errada.itens().size() < certa.itens().size(),
                    "com a pagina cortada antes, o filtro so ve os seis primeiros do acervo");
        });

        c.caso("os refinamentos novos aparecem na descricao da cadeia", () -> {
            FonteDeResultados busca = new ComPaginacao(new ComTermosAlternativos(oitoResultados(),
                    new ArrayList<String>()));
            Verifica.igual("OitoResultados + ComTermosAlternativos + ComPaginacao", busca.descricao());
        });
    }
}
