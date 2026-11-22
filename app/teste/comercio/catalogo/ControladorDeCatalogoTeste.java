package comercio.catalogo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import comercio.catalogo.aplicacao.ServicoDeCatalogo;
import comercio.catalogo.controle.ControladorDeCatalogo;
import comercio.catalogo.dominio.Anuncio;
import comercio.catalogo.dominio.Cotacao;
import comercio.catalogo.dominio.IFrete;
import comercio.catalogo.persistencia.RepositorioDeAnuncios;
import comercio.catalogo.persistencia.SementeDoCatalogo;
import comercio.contas.dominio.CEP;
import comercio.contas.persistencia.RepositorioDeContas;
import comercio.contas.persistencia.SementeDeContas;
import comercio.infra.http.Requisicao;
import comercio.infra.http.Resposta;
import comercio.infra.http.Roteador;
import comercio.nucleo.Dinheiro;
import comercio.teste.Casos;
import comercio.teste.Suite;
import comercio.teste.Verifica;

/**
 * As tres rotas do catalogo, chamadas sem rede. Passam pelo Roteador para que
 * os parametros do caminho ({id}) sejam preenchidos e as recusas virem o
 * codigo HTTP que a tela recebe.
 */
public final class ControladorDeCatalogoTeste implements Suite {

    private static Roteador roteador() {
        RepositorioDeContas contas = new RepositorioDeContas();
        SementeDeContas.popular(contas);
        RepositorioDeAnuncios anuncios = new RepositorioDeAnuncios();
        SementeDoCatalogo.popular(anuncios, contas.vendedores());
        IFrete frete = new IFrete() {
            @Override
            public Cotacao cotar(CEP destino, Anuncio anuncio) {
                return new Cotacao(Dinheiro.emReais(1490), 3);
            }
        };
        ServicoDeCatalogo servico = new ServicoDeCatalogo(anuncios, frete);
        return new Roteador().registrar(new ControladorDeCatalogo(servico, servico));
    }

    private static Resposta get(String caminho, String... consulta) {
        Map<String, String> parametros = new LinkedHashMap<String, String>();
        for (int i = 0; i + 1 < consulta.length; i += 2) {
            parametros.put(consulta[i], consulta[i + 1]);
        }
        return roteador().despachar(Requisicao.deTeste("GET", caminho, parametros));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapa(Object valor) {
        return (Map<String, Object>) valor;
    }

    private static List<?> lista(Object valor) {
        return (List<?>) valor;
    }

    private static String codigoDoErro(Resposta resposta) {
        return (String) mapa(resposta.corpo().get("erro")).get("codigo");
    }

    @Override
    public void registrar(Casos c) {
        c.caso("UC01 e UC30: GET /api/busca devolve a primeira pagina, o total e a cadeia montada", () -> {
            Resposta resposta = get("/api/busca", "termo", "fone de ouvido");
            Verifica.igual(200, resposta.status());
            Map<String, Object> corpo = resposta.corpo();
            Verifica.igual(9, corpo.get("total"));
            Verifica.igual(1, corpo.get("pagina"));
            Verifica.igual(2, corpo.get("paginas"));
            Verifica.igual(6, lista(corpo.get("itens")).size());
            Verifica.verdadeiro(!lista(corpo.get("facetas")).isEmpty(), "sem facetas");
            Verifica.contem((String) corpo.get("url"), "termo=fone+de+ouvido");

            Map<String, Object> primeiro = mapa(lista(corpo.get("itens")).get(0));
            Verifica.igual("anu-001", primeiro.get("id"));
            Verifica.igual("R$ 249,90", primeiro.get("preco"));
            Verifica.igual(24990L, primeiro.get("precoCentavos"));
            Verifica.verdadeiro(mapa(primeiro.get("selos")).containsKey("reputação"), "sem selo de reputacao");

            Map<String, Object> arquitetura = mapa(corpo.get("arquitetura"));
            Verifica.verdadeiro(lista(arquitetura.get("uc")).contains("UC01"), "rota sem UC01");
            Verifica.verdadeiro(lista(arquitetura.get("tr")).contains("TR-01"), "rota sem TR-01");
            Verifica.contem((String) arquitetura.get("mecanismo"), "+ ComPaginacao");
            Verifica.igual("ComPaginacao", lista(corpo.get("cadeia")).get(lista(corpo.get("cadeia")).size() - 1));
        });

        c.caso("UC03 e UC04: filtros, ordem, CEP e pagina chegam ao servico; 'q' vale como termo", () -> {
            Resposta resposta = get("/api/busca", "q", "fone", "condicao", "NOVO", "ordem", "MENOR_PRECO",
                    "cep", "72230-130", "pagina", "2");
            Verifica.igual(200, resposta.status());
            Map<String, Object> corpo = resposta.corpo();
            Verifica.igual(2, corpo.get("pagina"));
            String mecanismo = (String) mapa(corpo.get("arquitetura")).get("mecanismo");
            Verifica.contem(mecanismo, "ComFreteEPrazo");
            Verifica.contem(mecanismo, "ComOrdenacao");
            for (Object item : lista(corpo.get("itens"))) {
                Verifica.igual("NOVO", mapa(item).get("condicao"));
                Verifica.igual("R$ 14,90", mapa(mapa(item).get("selos")).get("frete"));
            }
        });

        c.caso("RF-A03: ordenacao desconhecida na busca responde 422 com o codigo da regra", () -> {
            Resposta resposta = get("/api/busca", "termo", "fone", "ordem", "ALEATORIA");
            Verifica.igual(422, resposta.status());
            Verifica.igual("RF-A03", codigoDoErro(resposta));
        });

        c.caso("RN-B04: CEP invalido na busca responde 422; pagina que nao e numero responde 400", () -> {
            Resposta cep = get("/api/busca", "termo", "fone", "cep", "123");
            Verifica.igual(422, cep.status());
            Verifica.igual("RN-B04", codigoDoErro(cep));
            Verifica.igual(400, get("/api/busca", "termo", "fone", "pagina", "dois").status());
        });

        c.caso("UC02: GET /api/sugestoes devolve ate cinco titulos que comecam com o prefixo", () -> {
            Resposta resposta = get("/api/sugestoes", "q", "fon");
            Verifica.igual(200, resposta.status());
            List<?> sugestoes = lista(resposta.corpo().get("sugestoes"));
            Verifica.verdadeiro(!sugestoes.isEmpty() && sugestoes.size() <= 5, "sugestoes: " + sugestoes);
            Verifica.igual(0, lista(get("/api/sugestoes").corpo().get("sugestoes")).size());
        });

        c.caso("UC02: sugestoes so aceitam GET (405 para outro metodo)", () -> {
            Resposta resposta = roteador().despachar(Requisicao.deTeste("POST", "/api/sugestoes"));
            Verifica.igual(405, resposta.status());
        });

        c.caso("UC05, UC06 e UC07: GET /api/anuncios/{id}?cep traz ficha, reputacao, frete e outras ofertas", () -> {
            Resposta resposta = get("/api/anuncios/anu-001", "cep", "72230130");
            Verifica.igual(200, resposta.status());
            Map<String, Object> corpo = resposta.corpo();
            Verifica.igual("Fone de ouvido bluetooth XZ-200", corpo.get("titulo"));
            Verifica.igual("R$ 14,90", corpo.get("frete"));
            Verifica.igual(3, corpo.get("prazoDias"));
            Verifica.igual(10, mapa(corpo.get("parcelamento")).get("vezes"));
            Verifica.igual("R$ 24,99", mapa(corpo.get("parcelamento")).get("maiorParcela"));
            Verifica.igual("4,8", mapa(corpo.get("vendedor")).get("nota"));
            Verifica.igual(2, lista(corpo.get("outrasOfertas")).size());
            Map<String, Object> arquitetura = mapa(corpo.get("arquitetura"));
            Verifica.verdadeiro(lista(arquitetura.get("tr")).contains("TR-02"), "rota sem TR-02");
            Verifica.contem((String) arquitetura.get("mecanismo"), "IFrete");
        });

        c.caso("UC07: sem CEP a ficha vem sem frete", () -> {
            Map<String, Object> corpo = get("/api/anuncios/anu-001").corpo();
            Verifica.igual(null, corpo.get("frete"));
            Verifica.igual(null, corpo.get("prazoDias"));
        });

        c.caso("UC05: anuncio inexistente responde 404; CEP invalido na ficha responde 422", () -> {
            Verifica.igual(404, get("/api/anuncios/anu-999").status());
            Resposta cep = get("/api/anuncios/anu-001", "cep", "7223");
            Verifica.igual(422, cep.status());
            Verifica.igual("RN-B04", codigoDoErro(cep));
        });
    }
}
