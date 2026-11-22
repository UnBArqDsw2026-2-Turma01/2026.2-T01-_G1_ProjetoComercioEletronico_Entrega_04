package comercio.contas.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import comercio.nucleo.NaoEncontradoException;

/**
 * Comprador e os enderecos de entrega da sua conta.
 *
 * Origem: RN-B02 — o endereco pertence ao comprador; pedidos e envios apenas o
 * referenciam.
 */
public final class Comprador extends Usuario {

    private final List<Endereco> enderecos = new ArrayList<Endereco>();

    public Comprador(String id, String nome, String email) {
        super(id, nome, email);
    }

    @Override
    public String papel() {
        return "COMPRADOR";
    }

    public synchronized void adicionarEndereco(Endereco novo) {
        for (Endereco existente : enderecos) {
            if (existente.id().equals(novo.id())) {
                throw new IllegalArgumentException("Endereco ja cadastrado: " + novo.id());
            }
        }
        enderecos.add(novo);
    }

    public synchronized List<Endereco> enderecos() {
        return Collections.unmodifiableList(new ArrayList<Endereco>(enderecos));
    }

    public synchronized Endereco endereco(String enderecoId) {
        for (Endereco endereco : enderecos) {
            if (endereco.id().equals(enderecoId)) {
                return endereco;
            }
        }
        throw new NaoEncontradoException("O endereço " + enderecoId + " não pertence a esta conta");
    }
}
