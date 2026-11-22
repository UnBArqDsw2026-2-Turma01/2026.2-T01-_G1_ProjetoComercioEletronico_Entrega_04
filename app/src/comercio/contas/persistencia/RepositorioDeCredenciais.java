package comercio.contas.persistencia;

import java.util.HashMap;
import java.util.Map;

import comercio.contas.dominio.acesso.Senha;

/** Senhas por usuario, separadas do cadastro: quem le o perfil nunca toca na credencial. */
public final class RepositorioDeCredenciais {

    private final Map<String, Senha> porUsuario = new HashMap<String, Senha>();

    public synchronized void definir(String usuarioId, Senha senha) {
        porUsuario.put(usuarioId, senha);
    }

    /** A senha do usuario, ou null quando ele nao tem credencial cadastrada. */
    public synchronized Senha de(String usuarioId) {
        return porUsuario.get(usuarioId);
    }
}
