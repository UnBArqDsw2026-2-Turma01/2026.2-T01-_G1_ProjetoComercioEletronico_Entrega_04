package comercio.contas.controle;

import comercio.contas.aplicacao.acesso.IAcesso;
import comercio.infra.http.Autenticador;

/**
 * Adaptador entre a porta da infraestrutura (Autenticador) e o servico de
 * acesso. Fica no Controle porque e a unica camada que conhece as duas.
 */
public final class AutenticadorDeSessao implements Autenticador {

    private final IAcesso acesso;

    public AutenticadorDeSessao(IAcesso acesso) {
        this.acesso = acesso;
    }

    @Override
    public String usuarioDoToken(String token) {
        return acesso.usuarioDoToken(token);
    }
}
