package comercio.posvenda.persistencia;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import comercio.nucleo.NaoEncontradoException;
import comercio.posvenda.dominio.Avaliacao;
import comercio.posvenda.dominio.Reclamacao;

/**
 * Avaliacoes e reclamacoes, em memoria. Identificadores sequenciais
 * (ava-001, rec-001) para a demonstracao ser reproduzivel.
 *
 * Autor: Pedro Luciano de Azevedo (Entrega 4).
 */
public final class RepositorioDePosVenda {

    private final List<Avaliacao> avaliacoes = new ArrayList<Avaliacao>();
    private final Map<String, Reclamacao> reclamacoes = new LinkedHashMap<String, Reclamacao>();

    public synchronized String proximoIdDeAvaliacao() {
        return String.format("ava-%03d", avaliacoes.size() + 1);
    }

    public synchronized String proximoIdDeReclamacao() {
        return String.format("rec-%03d", reclamacoes.size() + 1);
    }

    public synchronized void salvar(Avaliacao avaliacao) {
        avaliacoes.add(avaliacao);
    }

    public synchronized void salvar(Reclamacao reclamacao) {
        reclamacoes.put(reclamacao.id(), reclamacao);
    }

    /** A avaliacao de um item do pedido, ou null se ainda nao foi avaliado. */
    public synchronized Avaliacao avaliacao(String pedidoId, String anuncioId) {
        for (Avaliacao avaliacao : avaliacoes) {
            if (avaliacao.pedidoId().equals(pedidoId) && avaliacao.anuncioId().equals(anuncioId)) {
                return avaliacao;
            }
        }
        return null;
    }

    public synchronized List<Avaliacao> avaliacoesDoPedido(String pedidoId) {
        List<Avaliacao> doPedido = new ArrayList<Avaliacao>();
        for (Avaliacao avaliacao : avaliacoes) {
            if (avaliacao.pedidoId().equals(pedidoId)) {
                doPedido.add(avaliacao);
            }
        }
        return doPedido;
    }

    public synchronized Reclamacao reclamacao(String id) {
        Reclamacao reclamacao = reclamacoes.get(id);
        if (reclamacao == null) {
            throw new NaoEncontradoException("A reclamação " + id + " não existe");
        }
        return reclamacao;
    }

    /** As reclamacoes de um pedido, da mais antiga para a mais recente. */
    public synchronized List<Reclamacao> reclamacoesDoPedido(String pedidoId) {
        List<Reclamacao> doPedido = new ArrayList<Reclamacao>();
        for (Reclamacao reclamacao : reclamacoes.values()) {
            if (reclamacao.pedidoId().equals(pedidoId)) {
                doPedido.add(reclamacao);
            }
        }
        return doPedido;
    }
}
