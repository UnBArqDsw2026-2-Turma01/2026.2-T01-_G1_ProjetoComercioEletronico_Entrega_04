package comercio.infra.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import comercio.infra.json.ErroDeJson;
import comercio.infra.json.Json;

/**
 * Servidor HTTP da aplicacao, sobre o servidor embutido no JDK.
 *
 * Um unico processo atende as duas metades da camada de borda:
 *
 * - tudo que comeca com /api/ vai para o Roteador (camada de Controle);
 * - o restante e arquivo estatico da pasta web/ (camada de Apresentacao).
 *
 * As paginas nao tem acesso a nada alem da API, que e a separacao descrita no
 * ADR-01 do DAS. O servidor escuta apenas na interface local.
 */
public final class ServidorHttp {

    private static final int LIMITE_DO_CORPO_EM_BYTES = 64 * 1024;

    private final Roteador roteador;
    private final File pastaWeb;
    private final int portaPedida;
    private HttpServer servidor;

    /** Porta 0 pede ao sistema uma porta livre (usado nos testes). */
    public ServidorHttp(Roteador roteador, File pastaWeb, int porta) {
        this.roteador = roteador;
        this.pastaWeb = pastaWeb;
        this.portaPedida = porta;
    }

    public void iniciar() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), portaPedida), 0);
        servidor.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange troca) throws IOException {
                try {
                    if (troca.getRequestURI().getPath().startsWith("/api/")) {
                        atenderApi(troca);
                    } else {
                        atenderArquivo(troca);
                    }
                } finally {
                    troca.close();
                }
            }
        });
        servidor.start();
    }

    public void parar() {
        if (servidor != null) {
            servidor.stop(0);
            servidor = null;
        }
    }

    public int porta() {
        if (servidor == null) {
            throw new IllegalStateException("Servidor nao iniciado");
        }
        return servidor.getAddress().getPort();
    }

    // -------------------------------------------------------------------- API

    private void atenderApi(HttpExchange troca) throws IOException {
        Resposta resposta;
        try {
            resposta = roteador.despachar(lerRequisicao(troca));
        } catch (ErroDeJson e) {
            resposta = Resposta.erro(400, "JSON_INVALIDO", e.getMessage());
        } catch (RequisicaoInvalidaException e) {
            resposta = Resposta.erro(400, "REQUISICAO_INVALIDA", e.getMessage());
        }
        byte[] corpo = Json.escrever(resposta.corpo()).getBytes(StandardCharsets.UTF_8);
        troca.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        troca.getResponseHeaders().set("Cache-Control", "no-store");
        troca.sendResponseHeaders(resposta.status(), corpo.length);
        OutputStream saida = troca.getResponseBody();
        saida.write(corpo);
        saida.close();
    }

    private Requisicao lerRequisicao(HttpExchange troca) throws IOException {
        Map<String, String> cabecalhos = new LinkedHashMap<String, String>();
        for (Map.Entry<String, List<String>> entrada : troca.getRequestHeaders().entrySet()) {
            if (!entrada.getValue().isEmpty()) {
                cabecalhos.put(entrada.getKey().toLowerCase(), entrada.getValue().get(0));
            }
        }
        byte[] bruto = lerLimitado(troca.getRequestBody());
        Map<String, Object> corpo = Json.lerObjeto(new String(bruto, StandardCharsets.UTF_8));
        return new Requisicao(troca.getRequestMethod(), troca.getRequestURI().getPath(), null,
                lerConsulta(troca.getRequestURI().getRawQuery()), cabecalhos, corpo, null);
    }

    private static byte[] lerLimitado(InputStream entrada) throws IOException {
        ByteArrayOutputStream acumulado = new ByteArrayOutputStream();
        byte[] bloco = new byte[4096];
        int lidos;
        while ((lidos = entrada.read(bloco)) != -1) {
            acumulado.write(bloco, 0, lidos);
            if (acumulado.size() > LIMITE_DO_CORPO_EM_BYTES) {
                throw new RequisicaoInvalidaException("Corpo da requisição maior que o permitido");
            }
        }
        return acumulado.toByteArray();
    }

    static Map<String, String> lerConsulta(String bruta) {
        Map<String, String> consulta = new LinkedHashMap<String, String>();
        if (bruta == null || bruta.isEmpty()) {
            return consulta;
        }
        for (String par : bruta.split("&")) {
            if (par.isEmpty()) {
                continue;
            }
            int igual = par.indexOf('=');
            String nome = igual < 0 ? par : par.substring(0, igual);
            String valor = igual < 0 ? "" : par.substring(igual + 1);
            consulta.put(decodificar(nome), decodificar(valor));
        }
        return consulta;
    }

    private static String decodificar(String texto) {
        try {
            return URLDecoder.decode(texto, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 indisponivel", e);
        } catch (IllegalArgumentException e) {
            throw new RequisicaoInvalidaException("Parâmetro com codificação inválida");
        }
    }

    // --------------------------------------------------------------- arquivos

    private void atenderArquivo(HttpExchange troca) throws IOException {
        if (!"GET".equals(troca.getRequestMethod()) && !"HEAD".equals(troca.getRequestMethod())) {
            enviarTexto(troca, 405, "Método não permitido");
            return;
        }
        String caminho = troca.getRequestURI().getPath();
        if (caminho.equals("/")) {
            caminho = "/index.html";
        }
        File arquivo = resolver(caminho);
        if (arquivo == null || !arquivo.isFile()) {
            enviarTexto(troca, 404, "Página não encontrada");
            return;
        }
        byte[] conteudo = Files.readAllBytes(arquivo.toPath());
        troca.getResponseHeaders().set("Content-Type", tipoDe(arquivo.getName()));
        troca.getResponseHeaders().set("Cache-Control", "no-cache");
        if ("HEAD".equals(troca.getRequestMethod())) {
            troca.sendResponseHeaders(200, -1);
            return;
        }
        troca.sendResponseHeaders(200, conteudo.length);
        OutputStream saida = troca.getResponseBody();
        saida.write(conteudo);
        saida.close();
    }

    /**
     * Resolve o caminho pedido dentro da pasta web e devolve null se ele
     * apontar para fora dela. Impede que "/../" alcance arquivos do sistema.
     */
    File resolver(String caminho) throws IOException {
        File raiz = pastaWeb.getCanonicalFile();
        File alvo = new File(raiz, caminho).getCanonicalFile();
        String prefixo = raiz.getPath() + File.separator;
        if (!alvo.getPath().startsWith(prefixo)) {
            return null;
        }
        return alvo;
    }

    private static String tipoDe(String nome) {
        String minusculo = nome.toLowerCase();
        if (minusculo.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        if (minusculo.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (minusculo.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        }
        if (minusculo.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (minusculo.endsWith(".png")) {
            return "image/png";
        }
        if (minusculo.endsWith(".jpg") || minusculo.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (minusculo.endsWith(".ico")) {
            return "image/x-icon";
        }
        return "application/octet-stream";
    }

    private static void enviarTexto(HttpExchange troca, int status, String texto) throws IOException {
        byte[] corpo = texto.getBytes(StandardCharsets.UTF_8);
        troca.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        troca.sendResponseHeaders(status, corpo.length);
        OutputStream saida = troca.getResponseBody();
        saida.write(corpo);
        saida.close();
    }
}
