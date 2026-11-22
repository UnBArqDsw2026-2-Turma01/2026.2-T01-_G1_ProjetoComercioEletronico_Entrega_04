package comercio.infra.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Escrita e leitura de JSON sem biblioteca externa.
 *
 * Cobre o subconjunto que a API usa: objetos, listas, textos, numeros,
 * booleanos e nulo. Na escrita, mapas viram objetos (na ordem de insercao),
 * iteraveis viram listas e enumeracoes viram o seu nome. Na leitura, objetos
 * viram LinkedHashMap, listas viram ArrayList, numeros inteiros viram Long e os
 * demais, Double.
 *
 * Existe para sustentar a decisao de nao depender de nada alem do JDK.
 */
public final class Json {

    private Json() {
    }

    // ------------------------------------------------------------------ escrita

    public static String escrever(Object valor) {
        StringBuilder saida = new StringBuilder();
        escrever(valor, saida);
        return saida.toString();
    }

    private static void escrever(Object valor, StringBuilder saida) {
        if (valor == null) {
            saida.append("null");
        } else if (valor instanceof String) {
            escreverTexto((String) valor, saida);
        } else if (valor instanceof Number || valor instanceof Boolean) {
            saida.append(valor.toString());
        } else if (valor instanceof Enum) {
            escreverTexto(((Enum<?>) valor).name(), saida);
        } else if (valor instanceof Map) {
            saida.append('{');
            boolean primeiro = true;
            for (Map.Entry<?, ?> entrada : ((Map<?, ?>) valor).entrySet()) {
                if (!primeiro) {
                    saida.append(',');
                }
                primeiro = false;
                escreverTexto(String.valueOf(entrada.getKey()), saida);
                saida.append(':');
                escrever(entrada.getValue(), saida);
            }
            saida.append('}');
        } else if (valor instanceof Iterable) {
            saida.append('[');
            boolean primeiro = true;
            for (Object item : (Iterable<?>) valor) {
                if (!primeiro) {
                    saida.append(',');
                }
                primeiro = false;
                escrever(item, saida);
            }
            saida.append(']');
        } else if (valor instanceof Object[]) {
            saida.append('[');
            Object[] itens = (Object[]) valor;
            for (int i = 0; i < itens.length; i++) {
                if (i > 0) {
                    saida.append(',');
                }
                escrever(itens[i], saida);
            }
            saida.append(']');
        } else {
            throw new IllegalArgumentException("Tipo sem representacao em JSON: " + valor.getClass().getName()
                    + ". O controlador deve converter o DTO em mapa antes de responder.");
        }
    }

    private static void escreverTexto(String texto, StringBuilder saida) {
        saida.append('"');
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            switch (c) {
                case '"':
                    saida.append("\\\"");
                    break;
                case '\\':
                    saida.append("\\\\");
                    break;
                case '\n':
                    saida.append("\\n");
                    break;
                case '\r':
                    saida.append("\\r");
                    break;
                case '\t':
                    saida.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        saida.append(String.format("\\u%04x", (int) c));
                    } else {
                        saida.append(c);
                    }
            }
        }
        saida.append('"');
    }

    // ------------------------------------------------------------------ leitura

    public static Object ler(String texto) {
        if (texto == null) {
            throw new ErroDeJson("JSON ausente");
        }
        Leitor leitor = new Leitor(texto);
        leitor.pularEspacos();
        Object valor = leitor.valor();
        leitor.pularEspacos();
        if (!leitor.fim()) {
            throw leitor.erro("conteudo inesperado depois do valor");
        }
        return valor;
    }

    /** Le um texto que precisa ser um objeto JSON. Texto vazio vira objeto vazio. */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> lerObjeto(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return new LinkedHashMap<String, Object>();
        }
        Object valor = ler(texto);
        if (!(valor instanceof Map)) {
            throw new ErroDeJson("esperava um objeto JSON");
        }
        return (Map<String, Object>) valor;
    }

    private static final class Leitor {

        private final String texto;
        private int posicao;

        Leitor(String texto) {
            this.texto = texto;
        }

        boolean fim() {
            return posicao >= texto.length();
        }

        ErroDeJson erro(String mensagem) {
            return new ErroDeJson("JSON inválido na posição " + posicao + ": " + mensagem);
        }

        void pularEspacos() {
            while (!fim() && Character.isWhitespace(texto.charAt(posicao))) {
                posicao++;
            }
        }

        Object valor() {
            if (fim()) {
                throw erro("fim inesperado");
            }
            char c = texto.charAt(posicao);
            if (c == '{') {
                return objeto();
            }
            if (c == '[') {
                return lista();
            }
            if (c == '"') {
                return cadeia();
            }
            if (texto.startsWith("true", posicao)) {
                posicao += 4;
                return Boolean.TRUE;
            }
            if (texto.startsWith("false", posicao)) {
                posicao += 5;
                return Boolean.FALSE;
            }
            if (texto.startsWith("null", posicao)) {
                posicao += 4;
                return null;
            }
            if (c == '-' || Character.isDigit(c)) {
                return numero();
            }
            throw erro("caractere inesperado '" + c + "'");
        }

        private Map<String, Object> objeto() {
            Map<String, Object> mapa = new LinkedHashMap<String, Object>();
            posicao++;
            pularEspacos();
            if (!fim() && texto.charAt(posicao) == '}') {
                posicao++;
                return mapa;
            }
            while (true) {
                pularEspacos();
                if (fim() || texto.charAt(posicao) != '"') {
                    throw erro("esperava o nome de um campo");
                }
                String chave = cadeia();
                pularEspacos();
                if (fim() || texto.charAt(posicao) != ':') {
                    throw erro("esperava ':' depois do campo " + chave);
                }
                posicao++;
                pularEspacos();
                mapa.put(chave, valor());
                pularEspacos();
                if (fim()) {
                    throw erro("objeto nao foi fechado");
                }
                char c = texto.charAt(posicao++);
                if (c == '}') {
                    return mapa;
                }
                if (c != ',') {
                    throw erro("esperava ',' ou '}'");
                }
            }
        }

        private List<Object> lista() {
            List<Object> itens = new ArrayList<Object>();
            posicao++;
            pularEspacos();
            if (!fim() && texto.charAt(posicao) == ']') {
                posicao++;
                return itens;
            }
            while (true) {
                pularEspacos();
                itens.add(valor());
                pularEspacos();
                if (fim()) {
                    throw erro("lista nao foi fechada");
                }
                char c = texto.charAt(posicao++);
                if (c == ']') {
                    return itens;
                }
                if (c != ',') {
                    throw erro("esperava ',' ou ']'");
                }
            }
        }

        private String cadeia() {
            StringBuilder saida = new StringBuilder();
            posicao++;
            while (true) {
                if (fim()) {
                    throw erro("texto nao foi fechado");
                }
                char c = texto.charAt(posicao++);
                if (c == '"') {
                    return saida.toString();
                }
                if (c != '\\') {
                    saida.append(c);
                    continue;
                }
                if (fim()) {
                    throw erro("escape incompleto");
                }
                char escape = texto.charAt(posicao++);
                switch (escape) {
                    case '"':
                    case '\\':
                    case '/':
                        saida.append(escape);
                        break;
                    case 'n':
                        saida.append('\n');
                        break;
                    case 'r':
                        saida.append('\r');
                        break;
                    case 't':
                        saida.append('\t');
                        break;
                    case 'b':
                        saida.append('\b');
                        break;
                    case 'f':
                        saida.append('\f');
                        break;
                    case 'u':
                        if (posicao + 4 > texto.length()) {
                            throw erro("escape unicode incompleto");
                        }
                        try {
                            saida.append((char) Integer.parseInt(texto.substring(posicao, posicao + 4), 16));
                        } catch (NumberFormatException e) {
                            throw erro("escape unicode invalido");
                        }
                        posicao += 4;
                        break;
                    default:
                        throw erro("escape desconhecido '\\" + escape + "'");
                }
            }
        }

        private Object numero() {
            int inicio = posicao;
            boolean decimal = false;
            if (texto.charAt(posicao) == '-') {
                posicao++;
            }
            while (!fim()) {
                char c = texto.charAt(posicao);
                if (Character.isDigit(c)) {
                    posicao++;
                } else if (c == '.' || c == 'e' || c == 'E' || c == '+' || c == '-') {
                    decimal = true;
                    posicao++;
                } else {
                    break;
                }
            }
            String trecho = texto.substring(inicio, posicao);
            try {
                if (decimal) {
                    return Double.valueOf(trecho);
                }
                return Long.valueOf(trecho);
            } catch (NumberFormatException e) {
                throw erro("numero invalido '" + trecho + "'");
            }
        }
    }
}
