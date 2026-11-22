package comercio.transacao.persistencia;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import comercio.nucleo.NaoEncontradoException;
import comercio.transacao.dominio.pagamento.Pagamento;
import comercio.transacao.dominio.pedido.Pedido;

/**
 * Pedidos e as suas tentativas de pagamento, em memoria.
 *
 * Tambem emite os identificadores de pedido, em sequencia (ped-0001, ped-0002),
 * para que execucoes iguais produzam os mesmos numeros.
 */
public final class RepositorioDePedidos {

    private final Map<String, Pedido> pedidos = new LinkedHashMap<String, Pedido>();
    private final Map<String, List<Pagamento>> pagamentos = new LinkedHashMap<String, List<Pagamento>>();
    private int sequencia;

    public synchronized String proximoId() {
        sequencia++;
        return String.format("ped-%04d", sequencia);
    }

    public synchronized void salvar(Pedido pedido) {
        pedidos.put(pedido.getId(), pedido);
    }

    public synchronized Pedido obter(String pedidoId) {
        Pedido pedido = pedidos.get(pedidoId);
        if (pedido == null) {
            throw new NaoEncontradoException("O pedido " + pedidoId + " não existe");
        }
        return pedido;
    }

    /** Pedidos do comprador, do mais recente para o mais antigo. */
    public synchronized List<Pedido> doComprador(String compradorId) {
        List<Pedido> lista = new ArrayList<Pedido>();
        for (Pedido pedido : pedidos.values()) {
            if (pedido.compradorId().equals(compradorId)) {
                lista.add(0, pedido);
            }
        }
        return lista;
    }

    public synchronized void registrar(Pagamento pagamento) {
        List<Pagamento> tentativas = pagamentos.get(pagamento.pedidoId());
        if (tentativas == null) {
            tentativas = new ArrayList<Pagamento>();
            pagamentos.put(pagamento.pedidoId(), tentativas);
        }
        tentativas.add(pagamento);
    }

    /** Todas as tentativas de pagamento do pedido, na ordem em que ocorreram. */
    public synchronized List<Pagamento> tentativasDe(String pedidoId) {
        List<Pagamento> tentativas = pagamentos.get(pedidoId);
        return tentativas == null ? new ArrayList<Pagamento>() : new ArrayList<Pagamento>(tentativas);
    }

    /** A tentativa mais recente, ou null se o pedido nunca foi cobrado. */
    public synchronized Pagamento pagamentoAtual(String pedidoId) {
        List<Pagamento> tentativas = pagamentos.get(pedidoId);
        return tentativas == null || tentativas.isEmpty() ? null : tentativas.get(tentativas.size() - 1);
    }

    public synchronized int total() {
        return pedidos.size();
    }
}
