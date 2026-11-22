package comercio.transacao.aplicacao.observadores;

import comercio.catalogo.aplicacao.IAnuncio;
import comercio.transacao.dominio.pagamento.ObservadorDePagamento;
import comercio.transacao.dominio.pagamento.Pagamento;
import comercio.transacao.dominio.pagamento.StatusPagamento;
import comercio.transacao.dominio.pedido.ItemPedido;
import comercio.transacao.dominio.pedido.Pedido;
import comercio.transacao.persistencia.RepositorioDePedidos;

/**
 * Papel GoF: ConcreteObserver.
 *
 * Devolve ao estoque dos anuncios as unidades de um pedido que nao sera
 * entregue: quando a janela de pagamento expira ou quando o pagamento e
 * estornado.
 *
 * Origem: padrao Observer da Entrega 3 (Subequipe 03). Duas mudancas na
 * Entrega 4. A devolucao passa a acontecer de fato, pelo contrato IAnuncio do
 * Catalogo. E a recusa do pagamento deixa de devolver o estoque: o pedido
 * continua aguardando e o comprador pode tentar outro meio, de modo que as
 * unidades seguem reservadas ate o pedido expirar ou ser desfeito.
 */
public final class LiberacaoEstoque implements ObservadorDePagamento {

    private final RepositorioDePedidos pedidos;
    private final IAnuncio anuncios;

    public LiberacaoEstoque(RepositorioDePedidos pedidos, IAnuncio anuncios) {
        this.pedidos = pedidos;
        this.anuncios = anuncios;
    }

    @Override
    public void atualizar(Pagamento pagamento) {
        StatusPagamento status = pagamento.getStatus();
        if (status != StatusPagamento.EXPIRADO && status != StatusPagamento.ESTORNADO) {
            return;
        }
        Pedido pedido = pedidos.obter(pagamento.pedidoId());
        int unidades = 0;
        for (ItemPedido item : pedido.itens()) {
            anuncios.devolver(item.anuncioId(), item.quantidade());
            unidades += item.quantidade();
        }
        pedido.registrarEvento("Estoque devolvido: " + unidades + " unidade(s)");
    }
}
