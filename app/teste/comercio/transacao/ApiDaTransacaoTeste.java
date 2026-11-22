package comercio.transacao;

import java.util.List;
import java.util.Map;

import comercio.Aplicacao;
import comercio.externos.RelogioSimulado;
import comercio.infra.http.Requisicao;
import comercio.infra.http.Resposta;
import comercio.infra.http.Roteador;
import comercio.teste.Casos;
import comercio.teste.Suite;
import comercio.teste.Verifica;

/**
 * A compra pela API, do jeito que a tela a faz: as mesmas regras dos testes de
 * servico, agora atravessando Roteador e controladores, e com a rastreabilidade
 * declarada em cada resposta.
 */
public final class ApiDaTransacaoTeste implements Suite {

    private static Roteador roteador() {
        return Aplicacao.montar(new RelogioSimulado()).roteador();
    }

    private static Requisicao como(String usuario, String metodo, String caminho) {
        return Requisicao.deTeste(metodo, caminho).comUsuario(usuario);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapa(Object valor) {
        return (Map<String, Object>) valor;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> lista(Object valor) {
        return (List<Object>) valor;
    }

    private static String textoDe(Resposta resposta, String campo) {
        return String.valueOf(resposta.corpo().get(campo));
    }

    private static String mecanismo(Resposta resposta) {
        return String.valueOf(mapa(resposta.corpo().get("arquitetura")).get("mecanismo"));
    }

    private static String codigoDoErro(Resposta resposta) {
        return String.valueOf(mapa(resposta.corpo().get("erro")).get("codigo"));
    }

    private static String criarPedido(Roteador api) {
        api.despachar(como("com-01", "POST", "/api/carrinho/itens").comCampo("anuncioId", "anu-007")
                .comCampo("quantidade", 2));
        Resposta criado = api.despachar(como("com-01", "POST", "/api/pedidos").comCampo("enderecoId", "end-01"));
        Verifica.igual(201, criado.status());
        return textoDe(criado, "id");
    }

    @Override
    public void registrar(Casos c) {
        c.caso("UC08: sem sessao, o carrinho responde 401", () -> {
            Resposta resposta = roteador().despachar(Requisicao.deTeste("GET", "/api/carrinho"));
            Verifica.igual(401, resposta.status());
        });

        c.caso("UC08: adicionar pela API devolve o carrinho agrupado por vendedor e em centavos", () -> {
            Resposta resposta = roteador().despachar(como("com-01", "POST", "/api/carrinho/itens")
                    .comCampo("anuncioId", "anu-007").comCampo("quantidade", 2));
            Verifica.igual(200, resposta.status());
            Verifica.igual(2, resposta.corpo().get("unidades"));
            Map<String, Object> grupo = mapa(lista(resposta.corpo().get("grupos")).get(0));
            Map<String, Object> item = mapa(lista(grupo.get("itens")).get(0));
            Verifica.igual("anu-007", item.get("anuncioId"));
            Verifica.verdadeiro(mapa(item.get("subtotal")).get("centavos") instanceof Long,
                    "dinheiro sai em centavos inteiros");
            Verifica.contem(mecanismo(resposta), "RN-B07");
        });

        c.caso("RN-B07: quantidade acima do estoque responde 422 com o codigo da regra", () -> {
            Resposta resposta = roteador().despachar(como("com-01", "POST", "/api/carrinho/itens")
                    .comCampo("anuncioId", "anu-007").comCampo("quantidade", 9999));
            Verifica.igual(422, resposta.status());
            Verifica.igual("RN-B07", codigoDoErro(resposta));
        });

        c.caso("UC08: PATCH muda quantidade e selecao; DELETE retira o item", () -> {
            Roteador api = roteador();
            api.despachar(como("com-01", "POST", "/api/carrinho/itens").comCampo("anuncioId", "anu-007"));
            Resposta alterado = api.despachar(como("com-01", "PATCH", "/api/carrinho/itens/anu-007")
                    .comCampo("quantidade", 3).comCampo("selecionado", false));
            Map<String, Object> item = mapa(lista(mapa(lista(alterado.corpo().get("grupos")).get(0))
                    .get("itens")).get(0));
            Verifica.igual(3, item.get("quantidade"));
            Verifica.igual(false, item.get("selecionado"));
            Resposta vazio = api.despachar(como("com-01", "DELETE", "/api/carrinho/itens/anu-007"));
            Verifica.igual(0, vazio.corpo().get("unidades"));
        });

        c.caso("UC24: a API lista os quatro meios, cada um com o trio da sua familia", () -> {
            Resposta resposta = roteador().despachar(Requisicao.deTeste("GET", "/api/meios-de-pagamento"));
            List<Object> meios = lista(resposta.corpo().get("meios"));
            Verifica.igual(4, meios.size());
            Verifica.igual(3, lista(mapa(meios.get(0)).get("classesDoTrio")).size());
            Verifica.contem(mecanismo(resposta), "Abstract Factory");
        });

        c.caso("UC10 e UC12: pedido criado e pago pela API; a resposta conta os tres padroes", () -> {
            Roteador api = roteador();
            String pedido = criarPedido(api);
            Resposta pago = api.despachar(como("com-01", "POST", "/api/pedidos/" + pedido + "/pagamento")
                    .comCampo("meio", "PIX"));
            Verifica.igual(200, pago.status());
            Verifica.igual("PAGO", pago.corpo().get("status"));
            String texto = mecanismo(pago);
            Verifica.contem(texto, "Abstract Factory");
            Verifica.contem(texto, "Observer");
            Verifica.contem(texto, "State");
            Map<String, Object> arquitetura = mapa(pago.corpo().get("arquitetura"));
            Verifica.verdadeiro(lista(arquitetura.get("uc")).contains("UC12"), "a rota declara o UC12");
        });

        c.caso("ADR-05: pagar duas vezes responde 422 e nao cobra de novo", () -> {
            Roteador api = roteador();
            String pedido = criarPedido(api);
            api.despachar(como("com-01", "POST", "/api/pedidos/" + pedido + "/pagamento").comCampo("meio", "PIX"));
            Resposta segunda = api.despachar(como("com-01", "POST", "/api/pedidos/" + pedido + "/pagamento")
                    .comCampo("meio", "PIX"));
            Verifica.igual(422, segunda.status());
        });

        c.caso("UC13: outro comprador nao ve, nao paga, nao compensa e nao avanca o pedido alheio (403)", () -> {
            Roteador api = roteador();
            String pedido = criarPedido(api);
            Verifica.igual(403, api.despachar(como("com-02", "GET", "/api/pedidos/" + pedido)).status());
            Verifica.igual(403, api.despachar(como("com-02", "POST", "/api/pedidos/" + pedido + "/avancar"))
                    .status());
            Verifica.igual(403, api.despachar(como("com-02", "POST", "/api/pedidos/" + pedido + "/compensar"))
                    .status());
            Verifica.igual(403, api.despachar(como("com-02", "POST", "/api/pedidos/" + pedido + "/pagamento")
                    .comCampo("meio", "PIX")).status());
        });

        c.caso("UC26 e UC27: pago, o pedido avanca pela API ate ENTREGUE; depois disso, 422", () -> {
            Roteador api = roteador();
            String pedido = criarPedido(api);
            api.despachar(como("com-01", "POST", "/api/pedidos/" + pedido + "/pagamento").comCampo("meio", "PIX"));
            String caminho = "/api/pedidos/" + pedido + "/avancar";
            api.despachar(como("com-01", "POST", caminho));
            api.despachar(como("com-01", "POST", caminho));
            Resposta entregue = api.despachar(como("com-01", "POST", caminho));
            Verifica.igual("ENTREGUE", entregue.corpo().get("status"));
            Verifica.igual(422, api.despachar(como("com-01", "POST", caminho)).status());
        });

        c.caso("UC25: boleto fica aguardando ate a compensacao simulada", () -> {
            Roteador api = roteador();
            String pedido = criarPedido(api);
            Resposta boleto = api.despachar(como("com-01", "POST", "/api/pedidos/" + pedido + "/pagamento")
                    .comCampo("meio", "BOLETO"));
            Verifica.igual("AGUARDANDO_PAGAMENTO", boleto.corpo().get("status"));
            Resposta compensado = api.despachar(como("com-01", "POST", "/api/pedidos/" + pedido + "/compensar"));
            Verifica.igual("PAGO", compensado.corpo().get("status"));
        });

        c.caso("RN-B08: comprar agora pela API cria o pedido sem mexer no carrinho", () -> {
            Roteador api = roteador();
            Resposta criado = api.despachar(como("com-01", "POST", "/api/pedidos").comCampo("anuncioId", "anu-007")
                    .comCampo("quantidade", 1).comCampo("enderecoId", "end-01"));
            Verifica.igual(201, criado.status());
            Verifica.contem(mecanismo(criado), "RN-B08");
            Verifica.igual(0, api.despachar(como("com-01", "GET", "/api/carrinho")).corpo().get("unidades"));
        });

        c.caso("UC10: desistir de pedido nao pago o cancela; a lista do comprador o mostra", () -> {
            Roteador api = roteador();
            String pedido = criarPedido(api);
            Resposta cancelado = api.despachar(como("com-01", "POST", "/api/pedidos/" + pedido + "/desistencia"));
            Verifica.igual("CANCELADO", cancelado.corpo().get("status"));
            List<Object> pedidos = lista(api.despachar(como("com-01", "GET", "/api/pedidos")).corpo().get("pedidos"));
            Verifica.igual(1, pedidos.size());
        });

        c.caso("rastreabilidade: as rotas da Transacao declaram UC e TR", () -> {
            Resposta resposta = roteador().despachar(Requisicao.deTeste("GET", "/api/rastreabilidade"));
            int daTransacao = 0;
            for (Object linha : lista(resposta.corpo().get("rotas"))) {
                Map<String, Object> rota = mapa(linha);
                String caminho = String.valueOf(rota.get("caminho"));
                if (caminho.startsWith("/api/carrinho") || caminho.startsWith("/api/pedidos")
                        || caminho.startsWith("/api/meios")) {
                    daTransacao++;
                    Verifica.falso(lista(rota.get("uc")).isEmpty(), caminho + " sem UC");
                    Verifica.falso(lista(rota.get("tr")).isEmpty(), caminho + " sem TR");
                }
            }
            Verifica.igual(12, daTransacao);
        });
    }
}
