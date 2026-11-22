package comercio.contas.aplicacao.acesso;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Subsistema da fachada: dispositivos que cada usuario marcou como confiaveis. */
public final class RegistroDeDispositivos {

    private final Map<String, Set<String>> porUsuario = new HashMap<String, Set<String>>();

    public synchronized boolean confiavel(String usuarioId, String dispositivo) {
        Set<String> conhecidos = porUsuario.get(usuarioId);
        return conhecidos != null && conhecidos.contains(dispositivo);
    }

    public synchronized void lembrar(String usuarioId, String dispositivo) {
        Set<String> conhecidos = porUsuario.get(usuarioId);
        if (conhecidos == null) {
            conhecidos = new HashSet<String>();
            porUsuario.put(usuarioId, conhecidos);
        }
        conhecidos.add(dispositivo);
    }
}
