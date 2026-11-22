package comercio.infra;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import comercio.infra.http.Autenticador;
import comercio.infra.http.Requisicao;
import comercio.infra.http.RequisicaoInvalidaException;
import comercio.infra.http.Resposta;
import comercio.infra.http.Rota;
import comercio.infra.http.Roteador;
import comercio.infra.http.RotasDeInfraestrutura;
import comercio.infra.http.Tratador;
import comercio.nucleo.AcessoNegadoException;
import comercio.nucleo.NaoAutenticadoException;
import comercio.nucleo.NaoEncontradoException;
import comercio.nucleo.RegraDeNegocioException;
import comercio.teste.Casos;
import comercio.teste.Suite;
import comercio.teste.Verifica;

public final class RoteadorTeste implements Suite {

    private static Map<String, Object> mapa(String chave, Object valor) {
        Map<String, Object> mapa = new LinkedHashMap<String, Object>();
        mapa.put(chave, valor);
        return mapa;
    }

    private static Tratador lancando(final RuntimeException excecao) {
        return requisicao -> {
            throw excecao;
        };
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> erro(Resposta resposta) {
        return (Map<String, Object>) resposta.corpo().get("erro");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> arquitetura(Resposta resposta) {
        return (Map<String, Object>) resposta.corpo().get("arquitetura");
    }

    @Override
    public void registrar(Casos c) {
        c.caso("entrega ao tratador o trecho variavel do caminho", () -> {
            Roteador roteador = new Roteador().registrar(
                    Rota.get("/api/anuncios/{id}", r -> Resposta.ok(mapa("id", r.parametro("id")))));
            Resposta resposta = roteador.despachar(Requisicao.deTeste("GET", "/api/anuncios/anu-007"));
            Verifica.igual(200, resposta.status());
            Verifica.igual("anu-007", resposta.corpo().get("id"));
        });

        c.caso("toda resposta carrega a rastreabilidade declarada pela rota", () -> {
            Roteador roteador = new Roteador().registrar(
                    Rota.get("/api/busca", r -> Resposta.ok(mapa("total", 0)).mecanismo("cadeia de teste"))
                            .uc("UC01", "UC03").tr("TR-01").classes("ControladorDeCatalogo", "ServicoDeCatalogo"));
            Map<String, Object> arquitetura = arquitetura(
                    roteador.despachar(Requisicao.deTeste("GET", "/api/busca")));
            Verifica.igual("GET /api/busca", arquitetura.get("rota"));
            Verifica.igual("[UC01, UC03]", String.valueOf(arquitetura.get("uc")));
            Verifica.igual("[TR-01]", String.valueOf(arquitetura.get("tr")));
            Verifica.igual("[ControladorDeCatalogo, ServicoDeCatalogo]", String.valueOf(arquitetura.get("classes")));
            Verifica.igual("cadeia de teste", arquitetura.get("mecanismo"));
        });

        c.caso("regra de negocio violada vira 422 com o codigo da regra", () -> {
            Roteador roteador = new Roteador().registrar(Rota.post("/api/carrinho/itens",
                    lancando(new RegraDeNegocioException("RN-B07", "Quantidade maior que o estoque"))));
            Resposta resposta = roteador.despachar(Requisicao.deTeste("POST", "/api/carrinho/itens"));
            Verifica.igual(422, resposta.status());
            Verifica.igual("RN-B07", erro(resposta).get("codigo"));
            Verifica.contem(String.valueOf(arquitetura(resposta).get("mecanismo")), "RN-B07");
        });

        c.caso("cada excecao do nucleo tem o seu codigo HTTP", () -> {
            Roteador roteador = new Roteador()
                    .registrar(Rota.get("/a", lancando(new NaoEncontradoException("x"))))
                    .registrar(Rota.get("/b", lancando(new NaoAutenticadoException("x"))))
                    .registrar(Rota.get("/c", lancando(new AcessoNegadoException("x"))))
                    .registrar(Rota.get("/d", lancando(new RequisicaoInvalidaException("x"))));
            Verifica.igual(404, roteador.despachar(Requisicao.deTeste("GET", "/a")).status());
            Verifica.igual(401, roteador.despachar(Requisicao.deTeste("GET", "/b")).status());
            Verifica.igual(403, roteador.despachar(Requisicao.deTeste("GET", "/c")).status());
            Verifica.igual(400, roteador.despachar(Requisicao.deTeste("GET", "/d")).status());
        });

        c.caso("falha inesperada vira 500 sem expor a mensagem interna", () -> {
            Roteador roteador = new Roteador().registrar(
                    Rota.get("/api/x", lancando(new IllegalStateException("senha do banco: 1234"))));
            Resposta resposta = roteador.despachar(Requisicao.deTeste("GET", "/api/x"));
            Verifica.igual(500, resposta.status());
            Verifica.falso(String.valueOf(erro(resposta).get("mensagem")).contains("1234"),
                    "a mensagem interna nao pode chegar ao cliente");
        });

        c.caso("caminho desconhecido e 404; metodo errado em caminho existente e 405", () -> {
            Roteador roteador = new Roteador().registrar(Rota.get("/api/busca", r -> Resposta.ok(mapa("a", 1))));
            Verifica.igual(404, roteador.despachar(Requisicao.deTeste("GET", "/api/nada")).status());
            Verifica.igual(405, roteador.despachar(Requisicao.deTeste("POST", "/api/busca")).status());
        });

        c.caso("o token da requisicao e traduzido em usuario pelo Autenticador", () -> {
            Autenticador autenticador = token -> "tok-joana".equals(token) ? "com-01" : null;
            Roteador roteador = new Roteador().autenticarCom(autenticador).registrar(
                    Rota.get("/api/eu", r -> Resposta.ok(mapa("usuario", r.exigirUsuario()))));
            Resposta valida = roteador.despachar(
                    Requisicao.deTeste("GET", "/api/eu").comCabecalho("Authorization", "Bearer tok-joana"));
            Verifica.igual("com-01", valida.corpo().get("usuario"));
            Resposta invalida = roteador.despachar(
                    Requisicao.deTeste("GET", "/api/eu").comCabecalho("Authorization", "Bearer forjado"));
            Verifica.igual(401, invalida.status());
            Verifica.igual(401, roteador.despachar(Requisicao.deTeste("GET", "/api/eu")).status());
        });

        c.caso("a mesma rota nao pode ser registrada duas vezes", () -> {
            Roteador roteador = new Roteador().registrar(Rota.get("/api/busca", r -> Resposta.ok(mapa("a", 1))));
            Verifica.lanca(IllegalStateException.class,
                    () -> roteador.registrar(Rota.get("/api/busca", r -> Resposta.ok(mapa("a", 2)))));
        });

        c.caso("GET /api/rastreabilidade lista todas as rotas com UC e TR", () -> {
            Roteador roteador = new Roteador();
            roteador.registrar(Rota.get("/api/busca", r -> Resposta.ok(mapa("a", 1))).uc("UC01").tr("TR-01"));
            roteador.registrar(new RotasDeInfraestrutura(roteador));
            Resposta resposta = roteador.despachar(Requisicao.deTeste("GET", "/api/rastreabilidade"));
            List<?> rotas = (List<?>) resposta.corpo().get("rotas");
            Verifica.igual(3, rotas.size());
            Verifica.igual("[UC01]", String.valueOf(((Map<?, ?>) rotas.get(0)).get("uc")));
        });

        c.caso("leitura do corpo: campo obrigatorio ausente e numero invalido sao recusados", () -> {
            Requisicao requisicao = Requisicao.deTeste("POST", "/x").comCampo("quantidade", "dois");
            Verifica.lanca(RequisicaoInvalidaException.class, () -> requisicao.obrigatorio("anuncioId"));
            Verifica.lanca(RequisicaoInvalidaException.class, () -> requisicao.inteiro("quantidade", 1));
            Verifica.igual(7, requisicao.inteiro("ausente", 7));
        });
    }
}
