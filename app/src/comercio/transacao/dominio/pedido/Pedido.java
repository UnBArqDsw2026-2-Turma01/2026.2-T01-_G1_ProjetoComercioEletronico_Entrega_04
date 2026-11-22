package comercio.transacao.dominio.pedido;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import comercio.nucleo.Dinheiro;

/**
 * Papel GoF: Context do padrao State.
 *
 * O pedido guarda o estado atual e delega a ele as tres operacoes do ciclo de
 * vida. Nao ha desvio condicional por situacao nesta classe: o comportamento
 * muda porque o objeto de estado muda.
 *
 * Origem: padrao State da Entrega 3 (Subequipe 03). La o pedido era so um
 * identificador com um estado, suficiente para demonstrar o padrao. Na Entrega
 * 4 ele passa a ser o pedido do dominio descrito no Diagrama de Classes da
 * Entrega 2: tem comprador, itens com preco congelado, um envio por vendedor
 * (RN-B01) e total. A maquina de estados e a mesma. As transicoes deixam de ser
 * impressas no console e passam a ser registradas no historico do pedido.
 */
public final class Pedido {

    private final String id;
    private final String compradorId;
    private final long criadoNoMinuto;
    private final List<Envio> envios;
    private final List<String> historico = new ArrayList<String>();
    private final List<String> eventos = new ArrayList<String>();
    private EstadoPedido estado;

    public Pedido(String id, String compradorId, List<Envio> envios, long criadoNoMinuto) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Pedido sem identificador");
        }
        if (compradorId == null || compradorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Pedido sem comprador");
        }
        if (envios == null || envios.isEmpty()) {
            throw new IllegalArgumentException("Pedido precisa de ao menos um envio (RN-B01)");
        }
        this.id = id;
        this.compradorId = compradorId;
        this.envios = new ArrayList<Envio>(envios);
        this.criadoNoMinuto = criadoNoMinuto;
        this.estado = new EstadoAguardandoPagamento();
        this.historico.add(estado.getStatus().name());
    }

    // ------------------------------------------------- ciclo de vida (State)

    public synchronized void confirmarPagamento() {
        estado.confirmarPagamento(this);
    }

    public synchronized void cancelar() {
        estado.cancelar(this);
    }

    public synchronized void avancar() {
        estado.avancar(this);
    }

    /** Chamado pelos estados concretos para efetivar uma transicao. */
    public synchronized void mudarEstado(EstadoPedido novoEstado) {
        historico.add(estado.getStatus().name() + " -> " + novoEstado.getStatus().name());
        this.estado = novoEstado;
    }

    public synchronized StatusPedido getStatus() {
        return estado.getStatus();
    }

    // ------------------------------------------------------------------ dados

    public String getId() {
        return id;
    }

    public String compradorId() {
        return compradorId;
    }

    /** Minuto, no relogio da aplicacao, em que o pedido foi criado. Base da janela de pagamento. */
    public long criadoNoMinuto() {
        return criadoNoMinuto;
    }

    public List<Envio> envios() {
        return Collections.unmodifiableList(envios);
    }

    public List<ItemPedido> itens() {
        List<ItemPedido> todos = new ArrayList<ItemPedido>();
        for (Envio envio : envios) {
            todos.addAll(envio.itens());
        }
        return todos;
    }

    public Dinheiro subtotal() {
        Dinheiro soma = Dinheiro.zero();
        for (ItemPedido item : itens()) {
            soma = soma.somar(item.subtotal());
        }
        return soma;
    }

    public Dinheiro frete() {
        Dinheiro soma = Dinheiro.zero();
        for (Envio envio : envios) {
            soma = soma.somar(envio.frete());
        }
        return soma;
    }

    public Dinheiro total() {
        return subtotal().somar(frete());
    }

    /** Transicoes de estado, na ordem em que ocorreram. */
    public synchronized List<String> historico() {
        return Collections.unmodifiableList(new ArrayList<String>(historico));
    }

    /** Registra um acontecimento do pedido (aviso ao comprador, nota fiscal, estoque devolvido). */
    public synchronized void registrarEvento(String descricao) {
        eventos.add(descricao);
    }

    public synchronized List<String> eventos() {
        return Collections.unmodifiableList(new ArrayList<String>(eventos));
    }
}
