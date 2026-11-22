package comercio.contas.aplicacao.acesso;

import comercio.contas.dominio.Usuario;
import comercio.contas.dominio.acesso.Senha;
import comercio.contas.persistencia.RepositorioDeContas;
import comercio.contas.persistencia.RepositorioDeCredenciais;

/**
 * Elo 2: e-mail e senha. A recusa nao diz qual dos dois errou (Achado-L02:
 * a mensagem e a mesma para e-mail inexistente e senha errada).
 */
public final class ValidadorCredenciais extends ValidadorDeLogin {

    private final RepositorioDeContas contas;
    private final RepositorioDeCredenciais credenciais;

    public ValidadorCredenciais(RepositorioDeContas contas, RepositorioDeCredenciais credenciais) {
        this.contas = contas;
        this.credenciais = credenciais;
    }

    @Override
    protected Veredito verificar(ContextoDeLogin contexto) {
        Usuario usuario = contas.porEmail(contexto.requisicao().email());
        Senha senha = usuario == null ? null : credenciais.de(usuario.id());
        if (senha == null || !senha.confere(contexto.requisicao().senha())) {
            return recusar(contexto, "credenciais não conferem");
        }
        contexto.identificar(usuario.id());
        return aprovar(contexto, "credenciais conferem");
    }
}
