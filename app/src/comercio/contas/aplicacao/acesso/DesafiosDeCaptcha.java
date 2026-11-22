package comercio.contas.aplicacao.acesso;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

/**
 * Simula o servico de captcha: a pagina de login pede um desafio ao abrir
 * (Achado-L01 da Engenharia Reversa: o token e carregado com a pagina) e o
 * devolve na tentativa. Cada desafio vale uma vez.
 */
public final class DesafiosDeCaptcha {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final Set<String> emitidos = new HashSet<String>();

    public synchronized String emitir() {
        String token = "cap-" + Long.toHexString(ALEATORIO.nextLong());
        emitidos.add(token);
        return token;
    }

    /** Verdadeiro se o desafio existia; ele e descartado de qualquer forma. */
    public synchronized boolean consumir(String token) {
        return token != null && emitidos.remove(token);
    }
}
