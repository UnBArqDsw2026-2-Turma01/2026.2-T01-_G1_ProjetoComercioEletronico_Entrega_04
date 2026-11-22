package comercio.contas;

import java.util.List;
import java.util.Map;

import comercio.Aplicacao;
import comercio.contas.aplicacao.acesso.RequisicaoLoginConcretoBuilder;
import comercio.contas.persistencia.SementeDeContas;
import comercio.externos.RelogioSimulado;
import comercio.infra.http.Requisicao;
import comercio.infra.http.Resposta;
import comercio.infra.http.Roteador;
import comercio.nucleo.RegraDeNegocioException;
import comercio.teste.Casos;
import comercio.teste.Suite;
import comercio.teste.Verifica;

/**
 * UC22: login com Builder, Chain of Responsibility, Facade e Controller GRASP,
 * exercitado pela API como a tela o usa.
 */
public final class AcessoTeste implements Suite {

    private static final String EMAIL = "joana@exemplo.com";
    private static final String SENHA = SementeDeContas.SENHA_DE_DEMONSTRACAO;

    private static Roteador api() {
        return Aplicacao.montar(new RelogioSimulado()).roteador();
    }

    private static String desafio(Roteador api) {
        return String.valueOf(api.despachar(Requisicao.deTeste("GET", "/api/sessao/desafio")).corpo().get("desafio"));
    }

    private static Resposta entrar(Roteador api, String desafio, String email, String senha, String dispositivo,
            String codigo, boolean lembrar) {
        Requisicao r = Requisicao.deTeste("POST", "/api/sessao").comCampo("email", email).comCampo("senha", senha)
                .comCampo("dispositivo", dispositivo).comCampo("lembrarDispositivo", lembrar);
        if (desafio != null) {
            r = r.comCampo("desafio", desafio);
        }
        if (codigo != null) {
            r = r.comCampo("codigo", codigo);
        }
        return api.despachar(r);
    }

    /** Login completo num dispositivo novo: senha, depois o codigo. Devolve a resposta final. */
    private static Resposta loginCompleto(Roteador api, String dispositivo, boolean lembrar) {
        Resposta primeira = entrar(api, desafio(api), EMAIL, SENHA, dispositivo, null, lembrar);
        Verifica.igual("EXIGE_DOIS_FATORES", primeira.corpo().get("situacao"));
        return entrar(api, String.valueOf(primeira.corpo().get("novoDesafio")), EMAIL, SENHA, dispositivo,
                String.valueOf(primeira.corpo().get("codigoSimulado")), lembrar);
    }

    private static String mecanismo(Resposta resposta) {
        return String.valueOf(mapa(resposta.corpo().get("arquitetura")).get("mecanismo"));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapa(Object valor) {
        return (Map<String, Object>) valor;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> lista(Object valor) {
        return (List<Object>) valor;
    }

    private static Requisicao comToken(String metodo, String caminho, String token) {
        return Requisicao.deTeste(metodo, caminho).comCabecalho("Authorization", "Bearer " + token);
    }

    @Override
    public void registrar(Casos c) {
        c.caso("Builder: sem e-mail, sem dispositivo ou com codigo fora do formato, nada e montado", () -> {
            Verifica.lanca(RegraDeNegocioException.class, () -> new RequisicaoLoginConcretoBuilder()
                    .comCredenciais("", SENHA).comTelemetria(null, "d1").build());
            Verifica.lanca(RegraDeNegocioException.class, () -> new RequisicaoLoginConcretoBuilder()
                    .comCredenciais(EMAIL, SENHA).build());
            Verifica.lanca(RegraDeNegocioException.class, () -> new RequisicaoLoginConcretoBuilder()
                    .comCredenciais(EMAIL, SENHA).comTelemetria(null, "d1").comSegundoFator("12a").build());
        });

        c.caso("Chain: sem desafio de captcha a cadeia para no primeiro elo e o cadastro nem e consultado", () -> {
            Resposta resposta = entrar(api(), null, EMAIL, SENHA, "d1", null, false);
            Verifica.igual(401, resposta.status());
            String texto = mecanismo(resposta);
            Verifica.contem(texto, "ValidadorCaptcha");
            Verifica.falso(texto.contains("ValidadorCredenciais"), "o elo seguinte nao rodou");
        });

        c.caso("Chain: o desafio vale uma vez so", () -> {
            Roteador api = api();
            String usado = desafio(api);
            entrar(api, usado, EMAIL, SENHA, "d1", null, false);
            Verifica.igual(401, entrar(api, usado, EMAIL, SENHA, "d1", null, false).status());
        });

        c.caso("Achado-L02: senha errada e e-mail inexistente recebem a mesma resposta", () -> {
            Roteador api = api();
            Resposta senhaErrada = entrar(api, desafio(api), EMAIL, "outra-senha", "d1", null, false);
            Resposta semConta = entrar(api, desafio(api), "ninguem@exemplo.com", SENHA, "d1", null, false);
            Verifica.igual(401, senhaErrada.status());
            Verifica.igual(String.valueOf(senhaErrada.corpo().get("erro")), String.valueOf(semConta.corpo().get("erro")));
            Verifica.verdadeiro(senhaErrada.corpo().get("novoDesafio") != null, "a recusa ja traz outro desafio");
        });

        c.caso("ValidadorRisco: dispositivo novo exige segundo fator; com o codigo, a sessao abre", () -> {
            Roteador api = api();
            Resposta final_ = loginCompleto(api, "notebook", false);
            Verifica.igual(200, final_.status());
            Verifica.igual("APROVADO", final_.corpo().get("situacao"));
            Verifica.igual("com-01", final_.corpo().get("usuarioId"));
            Verifica.igual(4, lista(final_.corpo().get("percurso")).size());
            Verifica.contem(mecanismo(final_), "Facade");
        });

        c.caso("ValidadorDoisFatores: codigo errado recusa e invalida o codigo pendente", () -> {
            Roteador api = api();
            Resposta primeira = entrar(api, desafio(api), EMAIL, SENHA, "celular", null, false);
            String certo = String.valueOf(primeira.corpo().get("codigoSimulado"));
            String errado = certo.equals("000000") ? "111111" : "000000";
            Verifica.igual(401, entrar(api, desafio(api), EMAIL, SENHA, "celular", errado, false).status());
            Verifica.igual(401, entrar(api, desafio(api), EMAIL, SENHA, "celular", certo, false).status());
        });

        c.caso("Achado-L03: dispositivo lembrado entra sem segundo fator na proxima vez", () -> {
            Roteador api = api();
            loginCompleto(api, "notebook", true);
            Resposta segunda = entrar(api, desafio(api), EMAIL, SENHA, "notebook", null, false);
            Verifica.igual("APROVADO", segunda.corpo().get("situacao"));
            Verifica.contem(mecanismo(segunda), "não exigido");
        });

        c.caso("UC23 e UC09: o token da sessao abre a conta, com os enderecos do comprador", () -> {
            Roteador api = api();
            String token = String.valueOf(loginCompleto(api, "notebook", false).corpo().get("token"));
            Resposta conta = api.despachar(comToken("GET", "/api/conta", token));
            Verifica.igual(200, conta.status());
            Verifica.igual("Joana Prado", conta.corpo().get("nome"));
            Verifica.igual(2, lista(conta.corpo().get("enderecos")).size());
        });

        c.caso("UC22: sair revoga o token; a mesma chave nao abre mais nada", () -> {
            Roteador api = api();
            String token = String.valueOf(loginCompleto(api, "notebook", false).corpo().get("token"));
            Verifica.igual(200, api.despachar(comToken("DELETE", "/api/sessao", token)).status());
            Verifica.igual(401, api.despachar(comToken("GET", "/api/conta", token)).status());
        });

        c.caso("CA-01 pela API: login, carrinho e pedido usando so o token", () -> {
            Roteador api = api();
            String token = String.valueOf(loginCompleto(api, "notebook", false).corpo().get("token"));
            api.despachar(comToken("POST", "/api/carrinho/itens", token).comCampo("anuncioId", "anu-007"));
            Resposta pedido = api.despachar(comToken("POST", "/api/pedidos", token).comCampo("enderecoId", "end-01"));
            Verifica.igual(201, pedido.status());
            Verifica.igual("AGUARDANDO_PAGAMENTO", pedido.corpo().get("status"));
        });
    }
}
