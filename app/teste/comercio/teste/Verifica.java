package comercio.teste;

/** Afirmacoes usadas dentro dos casos de teste. Falha lancando AssertionError. */
public final class Verifica {

    private Verifica() {
    }

    public static void igual(Object esperado, Object obtido) {
        boolean iguais = esperado == null ? obtido == null : esperado.equals(obtido);
        if (!iguais) {
            throw new AssertionError("esperado <" + esperado + "> mas veio <" + obtido + ">");
        }
    }

    public static void verdadeiro(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }

    public static void falso(boolean condicao, String mensagem) {
        if (condicao) {
            throw new AssertionError(mensagem);
        }
    }

    public static void contem(String texto, String trecho) {
        if (texto == null || !texto.contains(trecho)) {
            throw new AssertionError("esperava encontrar <" + trecho + "> em <" + texto + ">");
        }
    }

    /**
     * Exige que o caso lance uma excecao do tipo informado e a devolve, para
     * que o teste possa inspecionar a mensagem ou o codigo da regra.
     */
    public static <T extends Throwable> T lanca(Class<T> tipo, Caso caso) {
        try {
            caso.executar();
        } catch (Throwable lancada) {
            if (tipo.isInstance(lancada)) {
                return tipo.cast(lancada);
            }
            throw new AssertionError("esperava " + tipo.getSimpleName() + " mas veio "
                    + lancada.getClass().getSimpleName() + ": " + lancada.getMessage());
        }
        throw new AssertionError("esperava " + tipo.getSimpleName() + " e nada foi lancado");
    }
}
