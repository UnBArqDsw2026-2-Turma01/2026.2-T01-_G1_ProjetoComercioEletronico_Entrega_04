package comercio.infra.http;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import comercio.nucleo.NaoAutenticadoException;

/**
 * Requisicao HTTP ja interpretada, independente do servidor que a recebeu.
 *
 * E o que os controladores enxergam. Por nao carregar nenhum tipo do servidor
 * embutido do JDK, um controlador pode ser testado chamando o tratador direto,
 * sem abrir porta de rede (ver deTeste).
 */
public final class Requisicao {

    private final String metodo;
    private final String caminho;
    private final Map<String, String> parametros;
    private final Map<String, String> consulta;
    private final Map<String, String> cabecalhos;
    private final Map<String, Object> corpo;
    private final String usuarioId;

    public Requisicao(String metodo, String caminho, Map<String, String> parametros, Map<String, String> consulta,
            Map<String, String> cabecalhos, Map<String, Object> corpo, String usuarioId) {
        this.metodo = metodo.toUpperCase();
        this.caminho = caminho;
        this.parametros = copia(parametros);
        this.consulta = copia(consulta);
        this.cabecalhos = copia(cabecalhos);
        this.corpo = corpo == null ? new LinkedHashMap<String, Object>() : new LinkedHashMap<String, Object>(corpo);
        this.usuarioId = usuarioId;
    }

    private static Map<String, String> copia(Map<String, String> origem) {
        return origem == null ? new LinkedHashMap<String, String>() : new LinkedHashMap<String, String>(origem);
    }

    // ---------------------------------------------------------------- para testes

    public static Requisicao deTeste(String metodo, String caminho) {
        return new Requisicao(metodo, caminho, null, null, null, null, null);
    }

    public static Requisicao deTeste(String metodo, String caminho, Map<String, String> consulta) {
        return new Requisicao(metodo, caminho, null, consulta, null, null, null);
    }

    public Requisicao comConsulta(String nome, String valor) {
        Map<String, String> nova = copia(consulta);
        nova.put(nome, valor);
        return new Requisicao(metodo, caminho, parametros, nova, cabecalhos, corpo, usuarioId);
    }

    public Requisicao comCorpo(Map<String, Object> novoCorpo) {
        return new Requisicao(metodo, caminho, parametros, consulta, cabecalhos, novoCorpo, usuarioId);
    }

    public Requisicao comCampo(String nome, Object valor) {
        Map<String, Object> novo = new LinkedHashMap<String, Object>(corpo);
        novo.put(nome, valor);
        return new Requisicao(metodo, caminho, parametros, consulta, cabecalhos, novo, usuarioId);
    }

    public Requisicao comCabecalho(String nome, String valor) {
        Map<String, String> novos = copia(cabecalhos);
        novos.put(nome.toLowerCase(), valor);
        return new Requisicao(metodo, caminho, parametros, consulta, novos, corpo, usuarioId);
    }

    public Requisicao comUsuario(String id) {
        return new Requisicao(metodo, caminho, parametros, consulta, cabecalhos, corpo, id);
    }

    Requisicao comParametros(Map<String, String> novos) {
        return new Requisicao(metodo, caminho, novos, consulta, cabecalhos, corpo, usuarioId);
    }

    // ------------------------------------------------------------------- leitura

    public String metodo() {
        return metodo;
    }

    public String caminho() {
        return caminho;
    }

    /** Trecho variavel do caminho: em /api/anuncios/{id}, parametro("id"). */
    public String parametro(String nome) {
        String valor = parametros.get(nome);
        if (valor == null) {
            throw new IllegalStateException("A rota nao declara o parametro {" + nome + "}");
        }
        return valor;
    }

    /** Parametro da query string, ou null se ausente ou vazio. */
    public String consulta(String nome) {
        String valor = consulta.get(nome);
        return valor == null || valor.trim().isEmpty() ? null : valor.trim();
    }

    public int consultaInteira(String nome, int padrao) {
        String valor = consulta(nome);
        if (valor == null) {
            return padrao;
        }
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new RequisicaoInvalidaException("O parâmetro '" + nome + "' precisa ser um número inteiro");
        }
    }

    public Map<String, String> consultas() {
        return Collections.unmodifiableMap(consulta);
    }

    /** Corpo da requisicao, lido do JSON. Vazio quando nao ha corpo. */
    public Map<String, Object> corpo() {
        return Collections.unmodifiableMap(corpo);
    }

    /** Campo de texto do corpo, ou null se ausente. */
    public String texto(String campo) {
        Object valor = corpo.get(campo);
        if (valor == null) {
            return null;
        }
        String texto = String.valueOf(valor).trim();
        return texto.isEmpty() ? null : texto;
    }

    public String obrigatorio(String campo) {
        String valor = texto(campo);
        if (valor == null) {
            throw new RequisicaoInvalidaException("O campo '" + campo + "' é obrigatório");
        }
        return valor;
    }

    public int inteiro(String campo, int padrao) {
        Object valor = corpo.get(campo);
        if (valor == null) {
            return padrao;
        }
        if (valor instanceof Number) {
            return ((Number) valor).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(valor).trim());
        } catch (NumberFormatException e) {
            throw new RequisicaoInvalidaException("O campo '" + campo + "' precisa ser um número inteiro");
        }
    }

    public boolean logico(String campo, boolean padrao) {
        Object valor = corpo.get(campo);
        if (valor == null) {
            return padrao;
        }
        return Boolean.parseBoolean(String.valueOf(valor));
    }

    public String cabecalho(String nome) {
        return cabecalhos.get(nome.toLowerCase());
    }

    /** Token de sessao enviado em "Authorization: Bearer <token>", ou null. */
    public String token() {
        String autorizacao = cabecalho("authorization");
        if (autorizacao == null || !autorizacao.toLowerCase().startsWith("bearer ")) {
            return null;
        }
        String token = autorizacao.substring("bearer ".length()).trim();
        return token.isEmpty() ? null : token;
    }

    /** Identificador do usuario da sessao, ou null quando a requisicao e anonima. */
    public String usuarioId() {
        return usuarioId;
    }

    /** Identificador do usuario da sessao; recusa a requisicao anonima. */
    public String exigirUsuario() {
        if (usuarioId == null) {
            throw new NaoAutenticadoException("Entre na sua conta para continuar");
        }
        return usuarioId;
    }
}
