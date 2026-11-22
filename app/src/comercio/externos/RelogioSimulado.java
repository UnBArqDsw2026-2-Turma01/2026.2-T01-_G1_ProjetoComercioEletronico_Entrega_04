package comercio.externos;

import comercio.transacao.dominio.Relogio;

/**
 * Relogio que so anda quando mandam. Torna a expiracao da janela de pagamento
 * testavel e o roteiro de demonstracao reproduzivel.
 */
public final class RelogioSimulado implements Relogio {

    private long minuto;

    @Override
    public synchronized long minutoAtual() {
        return minuto;
    }

    public synchronized void avancar(long minutos) {
        if (minutos < 0) {
            throw new IllegalArgumentException("O tempo nao volta");
        }
        minuto += minutos;
    }
}
