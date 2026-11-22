package comercio.contas.aplicacao.acesso;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * Subsistema da fachada: emite o token de sessao e o resolve de volta para o
 * usuario. O token e opaco (128 bits aleatorios); nao carrega dado nenhum.
 */
public final class EmissorDeTokens {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final Map<String, String> sessoes = new HashMap<String, String>();

    public synchronized String emitir(String usuarioId) {
        byte[] bytes = new byte[16];
        ALEATORIO.nextBytes(bytes);
        StringBuilder token = new StringBuilder();
        for (byte b : bytes) {
            token.append(String.format("%02x", b));
        }
        sessoes.put(token.toString(), usuarioId);
        return token.toString();
    }

    public synchronized String usuarioDe(String token) {
        return token == null ? null : sessoes.get(token);
    }

    public synchronized boolean revogar(String token) {
        return token != null && sessoes.remove(token) != null;
    }
}
