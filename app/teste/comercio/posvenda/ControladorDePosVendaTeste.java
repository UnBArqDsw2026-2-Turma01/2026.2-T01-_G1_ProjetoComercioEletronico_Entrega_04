package comercio.posvenda;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import comercio.infra.http.Requisicao;
import comercio.infra.http.Resposta;
import comercio.infra.http.Roteador;
import comercio.posvenda.controle.ControladorDePosVenda;
import comercio.teste.Casos;
import comercio.teste.Suite;
import comercio.teste.Verifica;

/**
 * As rotas do pos-venda, sem rede, pelo Roteador: sessao obrigatoria, codigo
 * HTTP de cada recusa e o campo arquitetura de cada resposta.
 */
public final class ControladorDePosVendaTeste implements Suite {

    private ServicoDePosVendaTeste apoio;

    private Roteador roteador() {
        apoio = new ServicoDePosVendaTeste();
        return new Roteador().registrar(new ControladorDePosVenda(apoio.servico()));
    }

    private static Requisicao post(String caminho, String usuario, Object... campos) {
        Map<String, Object> corpo = new LinkedHashMap<String, Object>();
        for (int i = 0; i + 1 < campos.length; i += 2) {
            corpo.put((String) campos[i], campos[i + 1]);
        }
        Requisicao requisicao = Requisicao.deTeste("POST", caminho).comCorpo(corpo);
        return usuario == null ? requisicao : requisicao.comUsuario(usuario);
    }

    private static Requisicao get(String caminho, String usuario) {
        return Requisicao.deTeste("GET", caminho).comUsuario(usuario);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapa(Object valor) {
        return (Map<String, Object>) valor;
    }

    private static String mecanismo(Resposta resposta) {
        return (String) mapa(resposta.corpo().get("arquitetura")).get("mecanismo");
    }

    private static String codigo(Resposta resposta) {
        return (String) mapa(resposta.corpo().get("erro")).get("codigo");
    }

    @Override
    public void registrar(Casos c) {
        c.caso("UC17: POST /api/pedidos/{id}/avaliacao responde 201 e mostra a reputacao antes e depois", () -> {
            Resposta resposta = roteador().despachar(post("/api/pedidos/ped-entregue/avaliacao",
                    ServicoDePosVendaTeste.COMPRADORA, "nota", 5, "comentario", "Otimo"));
            Verifica.igual(201, resposta.status());
            Verifica.igual(5, resposta.corpo().get("nota"));
            Verifica.contem(mecanismo(resposta), "RegistroDeReputacao");
            Verifica.contem(mecanismo(resposta), "RN-A05");
            List<?> tr = (List<?>) mapa(resposta.corpo().get("arquitetura")).get("tr");
            Verifica.verdadeiro(tr.contains("TR-03"), "rota sem TR-03");
        });

        c.caso("UC17: sem sessao responde 401; pedido nao entregue responde 422 com UC17", () -> {
            Roteador roteador = roteador();
            Verifica.igual(401, roteador.despachar(post("/api/pedidos/ped-entregue/avaliacao", null, "nota", 5)).status());
            Resposta recusa = roteador.despachar(post("/api/pedidos/ped-enviado/avaliacao",
                    ServicoDePosVendaTeste.COMPRADORA, "nota", 5));
            Verifica.igual(422, recusa.status());
            Verifica.igual("UC17", codigo(recusa));
        });

        c.caso("UC18 a UC21: abrir, responder, mediar e reembolsar pela API", () -> {
            Roteador roteador = roteador();
            Resposta aberta = roteador.despachar(post("/api/reclamacoes", ServicoDePosVendaTeste.COMPRADORA,
                    "pedidoId", "ped-enviado", "motivo", "Nao liga"));
            Verifica.igual(201, aberta.status());
            String id = (String) aberta.corpo().get("id");

            Resposta respondida = roteador.despachar(post("/api/reclamacoes/" + id + "/resposta", "ven-04",
                    "resposta", "Teste com outro cabo"));
            Verifica.igual("RESPONDIDA", respondida.corpo().get("status"));

            Resposta assumida = roteador.despachar(post("/api/reclamacoes/" + id + "/mediacao",
                    ServicoDePosVendaTeste.ATENDENTE));
            Verifica.igual("EM_MEDIACAO", assumida.corpo().get("status"));

            Resposta decidida = roteador.despachar(post("/api/reclamacoes/" + id + "/mediacao",
                    ServicoDePosVendaTeste.ATENDENTE, "decisao", "PROCEDENTE", "parecer", "Defeito confirmado"));
            Verifica.igual(200, decidida.status());
            Verifica.igual("REEMBOLSADA", decidida.corpo().get("status"));
            Verifica.igual("EstornoCartao", mapa(decidida.corpo().get("estorno")).get("politica"));
            // A politica de estorno aplicada aparece no mecanismo da resposta.
            Verifica.contem(mecanismo(decidida), "EstornoCartao");

            Resposta consulta = roteador.despachar(get("/api/reclamacoes/" + id, ServicoDePosVendaTeste.COMPRADORA));
            Verifica.igual(5, ((List<?>) consulta.corpo().get("linhaDoTempo")).size());
        });

        c.caso("UC19 e UC20: quem nao e o vendedor nem o atendimento recebe 403", () -> {
            Roteador roteador = roteador();
            String id = (String) roteador.despachar(post("/api/reclamacoes", ServicoDePosVendaTeste.COMPRADORA,
                    "pedidoId", "ped-enviado", "motivo", "Nao liga")).corpo().get("id");
            Verifica.igual(403, roteador.despachar(post("/api/reclamacoes/" + id + "/resposta", "ven-01",
                    "resposta", "x")).status());
            Verifica.igual(403, roteador.despachar(post("/api/reclamacoes/" + id + "/mediacao",
                    ServicoDePosVendaTeste.COMPRADORA, "decisao", "PROCEDENTE")).status());
        });

        c.caso("UC21: segundo reembolso da mesma reclamacao responde 422 com UC21", () -> {
            Roteador roteador = roteador();
            String id = (String) roteador.despachar(post("/api/reclamacoes", ServicoDePosVendaTeste.COMPRADORA,
                    "pedidoId", "ped-enviado", "motivo", "Nao liga")).corpo().get("id");
            roteador.despachar(post("/api/reclamacoes/" + id + "/mediacao", ServicoDePosVendaTeste.ATENDENTE,
                    "decisao", "PROCEDENTE"));
            Resposta segunda = roteador.despachar(post("/api/reclamacoes/" + id + "/mediacao",
                    ServicoDePosVendaTeste.ATENDENTE, "decisao", "PROCEDENTE"));
            Verifica.igual(422, segunda.status());
            Verifica.igual("UC21", codigo(segunda));
            Verifica.igual(1, apoio.pagamento.estornados.size());
        });

        c.caso("UC18: GET /api/pedidos/{id}/posvenda e GET de reclamacao inexistente", () -> {
            Roteador roteador = roteador();
            Resposta posVenda = roteador.despachar(get("/api/pedidos/ped-entregue/posvenda",
                    ServicoDePosVendaTeste.COMPRADORA));
            Verifica.igual(200, posVenda.status());
            Verifica.igual(Boolean.TRUE, posVenda.corpo().get("podeAvaliar"));
            Verifica.igual(404, roteador.despachar(get("/api/reclamacoes/rec-404",
                    ServicoDePosVendaTeste.ATENDENTE)).status());
        });
    }
}
