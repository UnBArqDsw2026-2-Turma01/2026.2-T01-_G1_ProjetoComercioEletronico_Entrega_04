package comercio.contas.aplicacao.acesso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Subsistema da fachada: trilha de entradas e saidas, com protocolo sequencial. */
public final class AuditoriaDeAcesso {

    private final List<String> registros = new ArrayList<String>();

    public synchronized String registrar(String evento, String usuarioId, String ipOrigem) {
        String protocolo = String.format("AUD-%05d", registros.size() + 1);
        registros.add(protocolo + " " + evento + " " + usuarioId + " de " + ipOrigem);
        return protocolo;
    }

    public synchronized List<String> registros() {
        return Collections.unmodifiableList(new ArrayList<String>(registros));
    }
}
