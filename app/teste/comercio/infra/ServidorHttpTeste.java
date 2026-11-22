package comercio.infra;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import comercio.infra.http.Resposta;
import comercio.infra.http.Rota;
import comercio.infra.http.Roteador;
import comercio.infra.http.ServidorHttp;
import comercio.infra.json.Json;
import comercio.teste.Casos;
import comercio.teste.Suite;
import comercio.teste.Verifica;

/** Sobe o servidor em uma porta livre e conversa com ele por HTTP de verdade. */
public final class ServidorHttpTeste implements Suite {

    private static final class Retorno {
        int status;
        String tipo;
        String corpo;
    }

    private static Retorno chamar(int porta, String metodo, String caminho, String corpo) throws Exception {
        HttpURLConnection conexao = (HttpURLConnection) new URI("http://127.0.0.1:" + porta + caminho).toURL().openConnection();
        conexao.setRequestMethod(metodo);
        conexao.setConnectTimeout(3000);
        conexao.setReadTimeout(3000);
        if (corpo != null) {
            conexao.setDoOutput(true);
            conexao.setRequestProperty("Content-Type", "application/json");
            OutputStream saida = conexao.getOutputStream();
            saida.write(corpo.getBytes(StandardCharsets.UTF_8));
            saida.close();
        }
        Retorno retorno = new Retorno();
        retorno.status = conexao.getResponseCode();
        retorno.tipo = conexao.getContentType();
        InputStream entrada = retorno.status >= 400 ? conexao.getErrorStream() : conexao.getInputStream();
        ByteArrayOutputStream acumulado = new ByteArrayOutputStream();
        if (entrada != null) {
            byte[] bloco = new byte[2048];
            int lidos;
            while ((lidos = entrada.read(bloco)) != -1) {
                acumulado.write(bloco, 0, lidos);
            }
            entrada.close();
        }
        retorno.corpo = new String(acumulado.toByteArray(), StandardCharsets.UTF_8);
        return retorno;
    }

    private static ServidorHttp servidorDeTeste() throws Exception {
        Roteador roteador = new Roteador();
        roteador.registrar(Rota.post("/api/eco/{id}", r -> {
            Map<String, Object> corpo = new LinkedHashMap<String, Object>();
            corpo.put("id", r.parametro("id"));
            corpo.put("q", r.consulta("q"));
            corpo.put("nome", r.texto("nome"));
            return Resposta.criado(corpo);
        }).uc("UC00"));
        ServidorHttp servidor = new ServidorHttp(roteador, new File(System.getProperty("comercio.web", "web")), 0);
        servidor.iniciar();
        return servidor;
    }

    @Override
    public void registrar(Casos c) {
        c.caso("requisicao real: caminho, query com acento e corpo JSON chegam ao tratador", () -> {
            ServidorHttp servidor = servidorDeTeste();
            try {
                Retorno retorno = chamar(servidor.porta(), "POST", "/api/eco/anu-001?q=fone+de+ouvido%20%C3%A9",
                        "{\"nome\":\"Joana\"}");
                Verifica.igual(201, retorno.status);
                Verifica.contem(retorno.tipo, "application/json");
                Map<String, Object> corpo = Json.lerObjeto(retorno.corpo);
                Verifica.igual("anu-001", corpo.get("id"));
                Verifica.igual("fone de ouvido é", corpo.get("q"));
                Verifica.igual("Joana", corpo.get("nome"));
                Verifica.verdadeiro(corpo.containsKey("arquitetura"), "a resposta HTTP deve trazer a rastreabilidade");
            } finally {
                servidor.parar();
            }
        });

        c.caso("corpo que nao e JSON e recusado com 400", () -> {
            ServidorHttp servidor = servidorDeTeste();
            try {
                Retorno retorno = chamar(servidor.porta(), "POST", "/api/eco/x", "{nome: Joana");
                Verifica.igual(400, retorno.status);
                Verifica.contem(retorno.corpo, "JSON_INVALIDO");
            } finally {
                servidor.parar();
            }
        });

        c.caso("a pagina inicial e servida como arquivo estatico", () -> {
            ServidorHttp servidor = servidorDeTeste();
            try {
                Retorno retorno = chamar(servidor.porta(), "GET", "/", null);
                Verifica.igual(200, retorno.status);
                Verifica.contem(retorno.tipo, "text/html");
                Verifica.contem(retorno.corpo, "G1_ProjetoComercioEletronico");
            } finally {
                servidor.parar();
            }
        });

        c.caso("caminho que tenta sair da pasta web nao e servido", () -> {
            ServidorHttp servidor = servidorDeTeste();
            try {
                Verifica.igual(404, chamar(servidor.porta(), "GET", "/../makefile", null).status);
                Verifica.igual(404, chamar(servidor.porta(), "GET", "/%2e%2e/makefile", null).status);
                Verifica.igual(404, chamar(servidor.porta(), "GET", "/css/../../makefile", null).status);
            } finally {
                servidor.parar();
            }
        });
    }
}
