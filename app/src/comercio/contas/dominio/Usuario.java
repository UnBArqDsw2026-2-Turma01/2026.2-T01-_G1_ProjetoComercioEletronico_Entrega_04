package comercio.contas.dominio;

/**
 * Quem interage com a plataforma. Especializa-se em Comprador, Vendedor e
 * Atendente (pacote Contas e Acesso, secao 5.2.1 do DAS).
 */
public abstract class Usuario {

    private final String id;
    private final String nome;
    private final String email;

    protected Usuario(String id, String nome, String email) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Usuario sem identificador");
        }
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Usuario sem nome");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("E-mail invalido");
        }
        this.id = id;
        this.nome = nome;
        this.email = email.toLowerCase();
    }

    public String id() {
        return id;
    }

    public String nome() {
        return nome;
    }

    public String email() {
        return email;
    }

    /** Papel do usuario, como aparece na sessao e nas telas. */
    public abstract String papel();

    @Override
    public String toString() {
        return papel() + " " + nome + " (" + id + ")";
    }
}
