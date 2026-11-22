package comercio.contas.dominio.acesso;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Senha guardada como resumo SHA-256 com sal, nunca em texto.
 *
 * A comparacao e feita em tempo constante (MessageDigest.isEqual), para que o
 * tempo de resposta nao revele quantos caracteres batem.
 */
public final class Senha {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final byte[] sal;
    private final byte[] resumo;

    private Senha(byte[] sal, byte[] resumo) {
        this.sal = sal;
        this.resumo = resumo;
    }

    public static Senha criar(String texto) {
        if (texto == null || texto.length() < 8) {
            throw new IllegalArgumentException("A senha tem pelo menos 8 caracteres");
        }
        byte[] sal = new byte[16];
        ALEATORIO.nextBytes(sal);
        return new Senha(sal, resumir(sal, texto));
    }

    public boolean confere(String tentativa) {
        if (tentativa == null) {
            return false;
        }
        return MessageDigest.isEqual(resumo, resumir(sal, tentativa));
    }

    private static byte[] resumir(byte[] sal, String texto) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            sha.update(sal);
            return sha.digest(texto.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 ausente no JDK", e);
        }
    }
}
