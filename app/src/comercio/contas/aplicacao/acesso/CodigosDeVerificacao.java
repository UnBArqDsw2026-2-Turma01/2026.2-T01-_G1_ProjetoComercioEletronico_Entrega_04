package comercio.contas.aplicacao.acesso;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * Codigos do segundo fator, um por usuario e dispositivo. Simula o envio por
 * SMS ou e-mail: o codigo gerado e devolvido para a tela de demonstracao
 * mostrar, ja que nao ha operadora nesta entrega.
 */
public final class CodigosDeVerificacao {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final Map<String, String> pendentes = new HashMap<String, String>();

    public synchronized String enviar(String usuarioId, String dispositivo) {
        String codigo = String.format("%06d", ALEATORIO.nextInt(1000000));
        pendentes.put(chave(usuarioId, dispositivo), codigo);
        return codigo;
    }

    /** Confere e descarta: um codigo errado tambem invalida o pendente. */
    public synchronized boolean conferir(String usuarioId, String dispositivo, String codigo) {
        String esperado = pendentes.remove(chave(usuarioId, dispositivo));
        return esperado != null && esperado.equals(codigo);
    }

    synchronized String ultimoEnviado(String usuarioId, String dispositivo) {
        return pendentes.get(chave(usuarioId, dispositivo));
    }

    private static String chave(String usuarioId, String dispositivo) {
        return usuarioId + "|" + dispositivo;
    }
}
