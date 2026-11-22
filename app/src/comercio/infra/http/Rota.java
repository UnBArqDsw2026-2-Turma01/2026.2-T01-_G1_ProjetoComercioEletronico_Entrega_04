package comercio.infra.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Uma rota da API e, junto dela, a sua rastreabilidade.
 *
 * Alem do metodo, do caminho e do tratador, a rota declara quais casos de uso
 * do DAS ela realiza (uc), a quais linhas da matriz de rastreabilidade ela
 * responde (tr) e quais classes participam, da camada mais externa para a mais
 * interna (classes). Com isso a rastreabilidade deixa de ser uma tabela
 * mantida a mao e passa a ser um dado do proprio codigo: GET /api/rastreabilidade
 * a lista, e a tabela de evidencias do DAS e gerada a partir dela.
 */
public final class Rota {

    private final String metodo;
    private final String padrao;
    private final String[] segmentos;
    private final Tratador tratador;
    private final List<String> ucs = new ArrayList<String>();
    private final List<String> trs = new ArrayList<String>();
    private final List<String> classes = new ArrayList<String>();

    private Rota(String metodo, String padrao, Tratador tratador) {
        if (!padrao.startsWith("/")) {
            throw new IllegalArgumentException("O caminho da rota comeca com /: " + padrao);
        }
        if (tratador == null) {
            throw new IllegalArgumentException("Rota sem tratador: " + padrao);
        }
        this.metodo = metodo;
        this.padrao = padrao;
        this.segmentos = dividir(padrao);
        this.tratador = tratador;
    }

    public static Rota get(String padrao, Tratador tratador) {
        return new Rota("GET", padrao, tratador);
    }

    public static Rota post(String padrao, Tratador tratador) {
        return new Rota("POST", padrao, tratador);
    }

    public static Rota patch(String padrao, Tratador tratador) {
        return new Rota("PATCH", padrao, tratador);
    }

    public static Rota delete(String padrao, Tratador tratador) {
        return new Rota("DELETE", padrao, tratador);
    }

    /** Casos de uso do DAS (secao 4) que esta rota realiza. */
    public Rota uc(String... ids) {
        ucs.addAll(Arrays.asList(ids));
        return this;
    }

    /** Linhas da matriz de rastreabilidade do DAS (secao 8.3) a que esta rota responde. */
    public Rota tr(String... ids) {
        trs.addAll(Arrays.asList(ids));
        return this;
    }

    /** Classes que participam da chamada, da camada mais externa para a mais interna. */
    public Rota classes(String... nomes) {
        classes.addAll(Arrays.asList(nomes));
        return this;
    }

    public String metodo() {
        return metodo;
    }

    public String padrao() {
        return padrao;
    }

    public Tratador tratador() {
        return tratador;
    }

    public List<String> ucs() {
        return Collections.unmodifiableList(ucs);
    }

    public List<String> trs() {
        return Collections.unmodifiableList(trs);
    }

    public List<String> classesEnvolvidas() {
        return Collections.unmodifiableList(classes);
    }

    /**
     * Compara o caminho com o padrao da rota. Devolve os trechos variaveis
     * ({id}) quando casa e null quando nao casa. Nao considera o metodo.
     */
    public Map<String, String> casar(String caminho) {
        String[] partes = dividir(caminho);
        if (partes.length != segmentos.length) {
            return null;
        }
        Map<String, String> parametros = new LinkedHashMap<String, String>();
        for (int i = 0; i < segmentos.length; i++) {
            String segmento = segmentos[i];
            if (segmento.startsWith("{") && segmento.endsWith("}")) {
                if (partes[i].isEmpty()) {
                    return null;
                }
                parametros.put(segmento.substring(1, segmento.length() - 1), partes[i]);
            } else if (!segmento.equals(partes[i])) {
                return null;
            }
        }
        return parametros;
    }

    private static String[] dividir(String caminho) {
        String limpo = caminho;
        while (limpo.length() > 1 && limpo.endsWith("/")) {
            limpo = limpo.substring(0, limpo.length() - 1);
        }
        return limpo.substring(1).split("/", -1);
    }

    @Override
    public String toString() {
        return metodo + " " + padrao;
    }
}
