package comercio.teste;

import java.util.ArrayList;
import java.util.List;

/** Coletor dos casos que uma suite registra. */
public final class Casos {

    static final class Registro {
        final String nome;
        final Caso caso;

        Registro(String nome, Caso caso) {
            this.nome = nome;
            this.caso = caso;
        }
    }

    private final List<Registro> registros = new ArrayList<Registro>();

    /**
     * Registra um caso. O nome deve comecar pelo codigo da regra de negocio ou
     * do caso de uso que ele verifica ("RN-B04: ...", "UC30: ...").
     */
    public void caso(String nome, Caso caso) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Todo caso de teste precisa de nome");
        }
        registros.add(new Registro(nome, caso));
    }

    List<Registro> registros() {
        return registros;
    }
}
