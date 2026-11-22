package comercio.contas.aplicacao;

import java.util.ArrayList;
import java.util.List;

import comercio.contas.dominio.Comprador;
import comercio.contas.dominio.Endereco;
import comercio.contas.dominio.Usuario;
import comercio.contas.persistencia.RepositorioDeContas;

/** Monta o perfil do usuario da sessao; enderecos so existem para comprador. */
public final class ServicoDeConta implements IConta {

    private final RepositorioDeContas contas;

    public ServicoDeConta(RepositorioDeContas contas) {
        this.contas = contas;
    }

    @Override
    public PerfilDTO perfil(String usuarioId) {
        Usuario usuario = contas.usuario(usuarioId);
        List<PerfilDTO.EnderecoDTO> enderecos = new ArrayList<PerfilDTO.EnderecoDTO>();
        if (usuario instanceof Comprador) {
            for (Endereco e : ((Comprador) usuario).enderecos()) {
                enderecos.add(new PerfilDTO.EnderecoDTO(e.id(), e.apelido(), e.resumo(), e.cep().formatado()));
            }
        }
        return new PerfilDTO(usuario.id(), usuario.nome(), usuario.email(), usuario.papel(), enderecos);
    }
}
