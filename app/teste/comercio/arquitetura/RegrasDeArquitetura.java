package comercio.arquitetura;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * As regras de dependencia da Visao Logica do DAS, escritas como codigo.
 *
 * Duas tabelas do DAS sao a fonte:
 *
 * - secao 5.1, "Regras de dependencia entre camadas": quem pode depender de
 *   quem entre Apresentacao, Controle, Aplicacao, Dominio e Persistencia;
 * - secao 5.2.5, "Dependencias entre pacotes de dominio": Contas e Acesso nao
 *   depende de ninguem; Catalogo depende de Contas; Transacao depende de
 *   Catalogo e de Contas; Pos-venda depende dos tres.
 *
 * Cada pacote Java tem a forma comercio.<dominio>.<camada>, de modo que as
 * duas dimensoes da matriz do DAS podem ser lidas do proprio nome do pacote.
 *
 * Limite conhecido: a verificacao le as linhas import. Uma referencia escrita
 * por extenso no meio do codigo (comercio.x.y.Classe) nao e vista.
 */
public final class RegrasDeArquitetura {

    private static final Set<String> DOMINIOS = conjunto("contas", "catalogo", "transacao", "posvenda");
    private static final Set<String> CAMADAS = conjunto("dominio", "aplicacao", "controle", "persistencia");

    /** Secao 5.2.5 do DAS: de quais pacotes de dominio cada um pode depender. */
    private static final Map<String, Set<String>> DOMINIO_PODE = new HashMap<String, Set<String>>();

    /** Secao 5.1 do DAS: de quais camadas cada camada pode depender. */
    private static final Map<String, Set<String>> CAMADA_PODE = new HashMap<String, Set<String>>();

    static {
        DOMINIO_PODE.put("contas", conjunto());
        DOMINIO_PODE.put("catalogo", conjunto("contas"));
        DOMINIO_PODE.put("transacao", conjunto("catalogo", "contas"));
        DOMINIO_PODE.put("posvenda", conjunto("transacao", "catalogo", "contas"));

        CAMADA_PODE.put("dominio", conjunto("dominio"));
        CAMADA_PODE.put("persistencia", conjunto("dominio"));
        CAMADA_PODE.put("aplicacao", conjunto("dominio", "persistencia", "aplicacao"));
        CAMADA_PODE.put("controle", conjunto("aplicacao"));
    }

    private RegrasDeArquitetura() {
    }

    private static Set<String> conjunto(String... itens) {
        return new HashSet<String>(Arrays.asList(itens));
    }

    /**
     * Avalia uma importacao. Devolve null quando ela e permitida e, quando nao
     * e, uma frase dizendo qual regra do DAS foi violada.
     */
    public static String violacao(String pacoteDeOrigem, String importado) {
        String[] origem = pacoteDeOrigem.split("\\.");
        String grupo = origem.length > 1 ? origem[1] : "";

        // Raiz de composicao: monta o sistema, entao enxerga tudo.
        if (pacoteDeOrigem.equals("comercio") || grupo.equals("demonstracao")) {
            return null;
        }

        if (!importado.startsWith("comercio.")) {
            return violacaoExterna(grupo, origem, importado);
        }

        String[] alvo = importado.split("\\.");
        String grupoAlvo = alvo.length > 1 ? alvo[1] : "";

        if (grupoAlvo.equals("demonstracao") || alvo.length == 2) {
            return "ninguem depende da raiz de composicao";
        }

        String estrutura = violacaoDeEstrutura(pacoteDeOrigem);
        if (estrutura != null) {
            return estrutura;
        }
        if (grupo.equals("nucleo")) {
            return "o nucleo nao depende de nenhum outro pacote da aplicacao";
        }
        if (grupoAlvo.equals("nucleo")) {
            return null;
        }
        if (grupo.equals("infra")) {
            return grupoAlvo.equals("infra") ? null : "a infraestrutura so conhece o nucleo";
        }
        if (grupo.equals("externos")) {
            if (grupoAlvo.equals("externos")) {
                return null;
            }
            boolean porta = DOMINIOS.contains(grupoAlvo) && alvo.length > 2 && alvo[2].equals("dominio");
            return porta ? null : "um adaptador externo so implementa portas do dominio (DAS 5.3, decisao 4)";
        }

        String camada = origem[2];

        if (grupoAlvo.equals("infra")) {
            return camada.equals("controle") ? null
                    : "so a camada de Controle fala com a infraestrutura HTTP (DAS 5.1)";
        }
        if (grupoAlvo.equals("externos")) {
            return "o dominio nao conhece adaptadores externos: usa portas (DAS 5.3, decisao 4)";
        }
        if (!DOMINIOS.contains(grupoAlvo)) {
            return "dependencia de pacote desconhecido";
        }

        String camadaAlvo = alvo.length > 2 ? alvo[2] : "";
        if (!grupoAlvo.equals(grupo) && !DOMINIO_PODE.get(grupo).contains(grupoAlvo)) {
            return "o pacote " + grupo + " nao pode depender de " + grupoAlvo + " (DAS 5.2.5)";
        }
        if (!CAMADA_PODE.get(camada).contains(camadaAlvo)) {
            return "a camada " + camada + " nao pode depender de " + camadaAlvo + " (DAS 5.1)";
        }
        return null;
    }

    /**
     * Confere se o pacote ocupa um lugar valido na estrutura: nucleo, infra,
     * externos, a raiz de composicao ou uma celula da matriz dominio x camada.
     * Devolve null quando sim. Vale tambem para arquivos sem nenhuma importacao.
     */
    public static String violacaoDeEstrutura(String pacote) {
        if (pacote.equals("comercio")) {
            return null;
        }
        String[] partes = pacote.split("\\.");
        String grupo = partes.length > 1 ? partes[1] : "";
        if (grupo.equals("nucleo") || grupo.equals("infra") || grupo.equals("externos")
                || grupo.equals("demonstracao")) {
            return null;
        }
        if (!DOMINIOS.contains(grupo)) {
            return "pacote fora da estrutura comercio.<dominio>.<camada>";
        }
        String camada = partes.length > 2 ? partes[2] : "";
        if (!CAMADAS.contains(camada)) {
            return "camada desconhecida '" + camada
                    + "' (DAS 5.1 define dominio, aplicacao, controle e persistencia)";
        }
        return null;
    }

    private static String violacaoExterna(String grupo, String[] origem, String importado) {
        if (importado.startsWith("com.sun.net.httpserver.")) {
            return grupo.equals("infra") ? null : "so a infraestrutura conhece o servidor HTTP";
        }
        if (importado.startsWith("java.awt.") || importado.startsWith("javax.swing.")) {
            return "interface grafica nao pertence a nenhuma camada do servidor";
        }
        if (importado.startsWith("java.")) {
            return null;
        }
        return "dependencia externa ao JDK: a aplicacao nao usa bibliotecas de terceiros";
    }
}
