package comercio.infra.http;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Resposta de uma rota: codigo HTTP, corpo e, opcionalmente, a descricao do
 * mecanismo arquitetural que atuou na chamada.
 *
 * O controlador entrega o corpo como mapa; a infraestrutura o converte em
 * JSON. O "mecanismo" e um texto curto que diz qual padrao de projeto
 * trabalhou e como (por exemplo, a cadeia de decoradores montada para aquela
 * busca). Ele viaja no campo "arquitetura" da resposta e alimenta a lateral
 * "Arquitetura em execucao" da tela.
 */
public final class Resposta {

    private final int status;
    private final Map<String, Object> corpo;
    private final String mecanismo;

    private Resposta(int status, Map<String, Object> corpo, String mecanismo) {
        this.status = status;
        this.corpo = corpo;
        this.mecanismo = mecanismo;
    }

    public static Resposta ok(Map<String, Object> corpo) {
        return new Resposta(200, corpo, null);
    }

    public static Resposta criado(Map<String, Object> corpo) {
        return new Resposta(201, corpo, null);
    }

    public static Resposta erro(int status, String codigo, String mensagem) {
        Map<String, Object> erro = new LinkedHashMap<String, Object>();
        erro.put("codigo", codigo);
        erro.put("mensagem", mensagem);
        Map<String, Object> corpo = new LinkedHashMap<String, Object>();
        corpo.put("erro", erro);
        return new Resposta(status, corpo, null);
    }

    /** Devolve uma copia desta resposta com o mecanismo arquitetural informado. */
    public Resposta mecanismo(String descricao) {
        return new Resposta(status, corpo, descricao);
    }

    Resposta comCorpo(Map<String, Object> novoCorpo) {
        return new Resposta(status, novoCorpo, mecanismo);
    }

    public int status() {
        return status;
    }

    public boolean sucesso() {
        return status >= 200 && status < 300;
    }

    public Map<String, Object> corpo() {
        return corpo;
    }

    public String mecanismo() {
        return mecanismo;
    }
}
