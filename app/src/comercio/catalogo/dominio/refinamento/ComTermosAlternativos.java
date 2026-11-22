package comercio.catalogo.dominio.refinamento;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import comercio.catalogo.dominio.ConsultaCatalogo;
import comercio.catalogo.dominio.FonteDeResultados;
import comercio.catalogo.dominio.Listagem;

/**
 * Decorador concreto (ConcreteDecorator, GAMMA et al., 1995): termos
 * alternativos para a busca que nao encontrou nada.
 *
 * Origem: RN-A01 e RF-A06 da Entrega 1 — a busca sem resultado nao termina
 * numa tela vazia; a plataforma oferece outros termos. No Diagrama de Classes
 * da Entrega 2 isso virou o atributo Listagem.termosAlternativos.
 *
 * Quando a listagem recebida tem resultado, este decorador nao faz nada: a
 * regra so vale para a busca vazia. Quando esta vazia, procura no acervo ate
 * TRES titulos que compartilham alguma palavra com o termo buscado,
 * ignorando maiusculas, acentos e palavras de ligacao. Plural e singular
 * contam como a mesma palavra ("fones" casa com "fone").
 *
 * INFERIDO: o criterio real de escolha dos termos alternativos nao e
 * observavel de fora. Adota-se o mais simples que torna a regra verificavel:
 * mais palavras em comum primeiro e, no empate, a ordem do acervo.
 *
 * O acervo de titulos chega pelo construtor, e nao por consulta ao
 * repositorio, porque o dominio nao conhece a persistencia (DAS 5.1). Quem
 * monta a cadeia, o Servico de Catalogo, entrega os titulos publicados.
 *
 * Nao altera a listagem recebida: devolve uma copia via comTermosAlternativos.
 *
 * Autor: Pedro Luciano de Azevedo (refinamento ComTermosAlternativos, Entrega 4).
 */
public final class ComTermosAlternativos extends RefinamentoDecorator {

    /** RN-A01: quantos termos alternativos, no maximo. */
    public static final int MAXIMO = 3;

    private static final Set<String> PALAVRAS_DE_LIGACAO = new HashSet<String>(Arrays.asList(
            "com", "sem", "para", "por", "das", "dos", "uma", "uns", "umas", "que"));

    private final List<String> acervo;

    /**
     * @param acervo titulos dos anuncios publicados, na ordem do catalogo; repeticoes sao ignoradas
     */
    public ComTermosAlternativos(FonteDeResultados origem, List<String> acervo) {
        super(origem);
        if (acervo == null) {
            throw new IllegalArgumentException("ComTermosAlternativos precisa do acervo de titulos");
        }
        this.acervo = new ArrayList<String>(new LinkedHashSet<String>(acervo));
    }

    @Override
    public Listagem buscar(ConsultaCatalogo consulta) {
        Listagem recebida = super.buscar(consulta);
        if (!recebida.vazia()) {
            return recebida;
        }
        return recebida.comTermosAlternativos(alternativas(consulta.termo()));
    }

    private List<String> alternativas(String termo) {
        Set<String> buscadas = palavras(termo);
        List<String> escolhidas = new ArrayList<String>();
        if (buscadas.isEmpty()) {
            return escolhidas;
        }

        // Pontua cada titulo pelo numero de palavras em comum. A ordenacao
        // abaixo e estavel, entao o empate preserva a ordem do acervo.
        final List<String> candidatos = new ArrayList<String>();
        final List<Integer> pontos = new ArrayList<Integer>();
        for (String titulo : acervo) {
            int emComum = emComum(buscadas, palavras(titulo));
            if (emComum > 0) {
                candidatos.add(titulo);
                pontos.add(emComum);
            }
        }
        for (int maximo = buscadas.size(); maximo > 0 && escolhidas.size() < MAXIMO; maximo--) {
            for (int i = 0; i < candidatos.size() && escolhidas.size() < MAXIMO; i++) {
                if (pontos.get(i) == maximo) {
                    escolhidas.add(candidatos.get(i));
                }
            }
        }
        return escolhidas;
    }

    private static int emComum(Set<String> buscadas, Set<String> doTitulo) {
        int total = 0;
        for (String buscada : buscadas) {
            for (String palavra : doTitulo) {
                if (mesmaPalavra(buscada, palavra)) {
                    total++;
                    break;
                }
            }
        }
        return total;
    }

    /** Igual, ou uma comeca com a outra e a menor tem ao menos 4 letras ("fone" e "fones"). */
    private static boolean mesmaPalavra(String a, String b) {
        if (a.equals(b)) {
            return true;
        }
        if (Math.min(a.length(), b.length()) < 4) {
            return false;
        }
        return a.startsWith(b) || b.startsWith(a);
    }

    /** Palavras significativas do texto: minusculas, sem acento, com 3 letras ou mais. */
    static Set<String> palavras(String texto) {
        Set<String> palavras = new LinkedHashSet<String>();
        if (texto == null) {
            return palavras;
        }
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        for (String palavra : semAcento.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
            if (palavra.length() >= 3 && !PALAVRAS_DE_LIGACAO.contains(palavra)) {
                palavras.add(palavra);
            }
        }
        return palavras;
    }
}
