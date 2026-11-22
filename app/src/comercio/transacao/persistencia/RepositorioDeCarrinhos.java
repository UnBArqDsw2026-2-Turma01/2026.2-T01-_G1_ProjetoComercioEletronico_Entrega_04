package comercio.transacao.persistencia;

import java.util.HashMap;
import java.util.Map;

import comercio.transacao.dominio.carrinho.Carrinho;

/** Carrinhos em memoria, um por comprador. O carrinho nasce vazio no primeiro acesso. */
public final class RepositorioDeCarrinhos {

    private final Map<String, Carrinho> porComprador = new HashMap<String, Carrinho>();

    public synchronized Carrinho de(String compradorId) {
        Carrinho carrinho = porComprador.get(compradorId);
        if (carrinho == null) {
            carrinho = new Carrinho(compradorId);
            porComprador.put(compradorId, carrinho);
        }
        return carrinho;
    }
}
