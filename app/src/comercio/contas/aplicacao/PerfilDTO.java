package comercio.contas.aplicacao;

import java.util.Collections;
import java.util.List;

/** Dados da conta que a tela mostra (UC23), sem nenhuma credencial. */
public final class PerfilDTO {

    public static final class EnderecoDTO {
        public final String id;
        public final String apelido;
        public final String resumo;
        public final String cep;

        public EnderecoDTO(String id, String apelido, String resumo, String cep) {
            this.id = id;
            this.apelido = apelido;
            this.resumo = resumo;
            this.cep = cep;
        }
    }

    public final String id;
    public final String nome;
    public final String email;
    public final String papel;
    public final List<EnderecoDTO> enderecos;

    public PerfilDTO(String id, String nome, String email, String papel, List<EnderecoDTO> enderecos) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.papel = papel;
        this.enderecos = Collections.unmodifiableList(enderecos);
    }
}
