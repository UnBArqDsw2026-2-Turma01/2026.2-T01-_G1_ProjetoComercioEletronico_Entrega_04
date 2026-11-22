package comercio.transacao.aplicacao.observadores;

import comercio.transacao.dominio.pagamento.ObservadorDePagamento;
import comercio.transacao.dominio.pagamento.Pagamento;
import comercio.transacao.dominio.pagamento.StatusPagamento;
import comercio.transacao.persistencia.RepositorioDePedidos;

/**
 * Papel GoF: ConcreteObserver.
 *
 * So reage quando o pagamento e aprovado: emite a nota fiscal no valor pago.
 *
 * Origem: padrao Observer da Entrega 3 (Subequipe 03). O valor passa a ser
 * Dinheiro, de modo que a nota traz exatamente os centavos cobrados.
 */
public final class EmissaoNotaFiscal implements ObservadorDePagamento {

    private final RepositorioDePedidos pedidos;

    public EmissaoNotaFiscal(RepositorioDePedidos pedidos) {
        this.pedidos = pedidos;
    }

    @Override
    public void atualizar(Pagamento pagamento) {
        if (pagamento.getStatus() == StatusPagamento.APROVADO) {
            pedidos.obter(pagamento.pedidoId()).registrarEvento("Nota fiscal NF-" + pagamento.pedidoId()
                    + " emitida no valor de " + pagamento.valor().formatado());
        }
    }
}
