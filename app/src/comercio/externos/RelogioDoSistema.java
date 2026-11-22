package comercio.externos;

import comercio.transacao.dominio.Relogio;

/** O relogio real: minutos desde a epoca. Usado quando a aplicacao esta no ar. */
public final class RelogioDoSistema implements Relogio {

    @Override
    public long minutoAtual() {
        return System.currentTimeMillis() / 60000L;
    }
}
