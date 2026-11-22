package comercio.contas.dominio;

/**
 * Atendente: quem media reclamacoes.
 *
 * E a classe, isto e, o registro que o sistema guarda de quem mediou. No
 * Diagrama de Casos de Uso a mesma pessoa aparece como o ator "Atendimento".
 * Classe de origem mais fraca do modelo: vem do Rich Picture, nao de observacao.
 */
public final class Atendente extends Usuario {

    public Atendente(String id, String nome, String email) {
        super(id, nome, email);
    }

    @Override
    public String papel() {
        return "ATENDENTE";
    }
}
