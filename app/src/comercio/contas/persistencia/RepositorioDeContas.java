package comercio.contas.persistencia;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import comercio.contas.dominio.Atendente;
import comercio.contas.dominio.Comprador;
import comercio.contas.dominio.Usuario;
import comercio.contas.dominio.Vendedor;
import comercio.nucleo.NaoEncontradoException;

/**
 * Repositorio de contas, em memoria.
 *
 * A persistencia desta aplicacao nao e duravel: os dados vivem enquanto o
 * processo vive e voltam a semente a cada execucao. E uma escolha de escopo,
 * registrada como limite na Visao de Implementacao.
 */
public final class RepositorioDeContas {

    private final Map<String, Usuario> porId = new LinkedHashMap<String, Usuario>();

    public synchronized void salvar(Usuario usuario) {
        for (Usuario existente : porId.values()) {
            if (existente.email().equals(usuario.email()) && !existente.id().equals(usuario.id())) {
                throw new IllegalArgumentException("E-mail ja cadastrado: " + usuario.email());
            }
        }
        porId.put(usuario.id(), usuario);
    }

    public synchronized Usuario usuario(String id) {
        Usuario usuario = porId.get(id);
        if (usuario == null) {
            throw new NaoEncontradoException("A conta " + id + " não existe");
        }
        return usuario;
    }

    /** Devolve null quando nao ha conta com o e-mail. */
    public synchronized Usuario porEmail(String email) {
        if (email == null) {
            return null;
        }
        String procurado = email.trim().toLowerCase();
        for (Usuario usuario : porId.values()) {
            if (usuario.email().equals(procurado)) {
                return usuario;
            }
        }
        return null;
    }

    public Comprador comprador(String id) {
        Usuario usuario = usuario(id);
        if (!(usuario instanceof Comprador)) {
            throw new NaoEncontradoException("A conta " + id + " não é de comprador");
        }
        return (Comprador) usuario;
    }

    public Vendedor vendedor(String id) {
        Usuario usuario = usuario(id);
        if (!(usuario instanceof Vendedor)) {
            throw new NaoEncontradoException("A conta " + id + " não é de vendedor");
        }
        return (Vendedor) usuario;
    }

    public Atendente atendente(String id) {
        Usuario usuario = usuario(id);
        if (!(usuario instanceof Atendente)) {
            throw new NaoEncontradoException("A conta " + id + " não é de atendimento");
        }
        return (Atendente) usuario;
    }

    public synchronized List<Vendedor> vendedores() {
        List<Vendedor> vendedores = new ArrayList<Vendedor>();
        for (Usuario usuario : porId.values()) {
            if (usuario instanceof Vendedor) {
                vendedores.add((Vendedor) usuario);
            }
        }
        return vendedores;
    }
}
