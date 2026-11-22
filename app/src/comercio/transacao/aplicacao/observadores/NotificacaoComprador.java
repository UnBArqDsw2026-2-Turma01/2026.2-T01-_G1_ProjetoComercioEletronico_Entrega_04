package comercio.transacao.aplicacao.observadores;

import comercio.transacao.dominio.pagamento.ObservadorDePagamento;
import comercio.transacao.dominio.pagamento.Pagamento;
import comercio.transacao.persistencia.RepositorioDePedidos;

/**
 * Papel GoF: ConcreteObserver.
 *
 * Reage a qualquer mudanca de situacao, avisando o comprador. O aviso fica na
 * linha do tempo do pedido, que e o que a tela de acompanhamento exibe.
 *
 * Origem: padrao Observer da Entrega 3 (Subequipe 03).
 */
public final class NotificacaoComprador implements ObservadorDePagamento {

    private final RepositorioDePedidos pedidos;

    public NotificacaoComprador(RepositorioDePedidos pedidos) {
        this.pedidos = pedidos;
    }

    @Override
    public void atualizar(Pagamento pagamento) {
        StringBuilder aviso = new StringBuilder("Comprador avisado: pagamento por ")
                .append(pagamento.meio().rotulo()).append(" ")
                .append(pagamento.getStatus().getDescricao().toLowerCase());
        if (pagamento.autorizacao() != null && pagamento.autorizacao().detalhe() != null) {
            aviso.append(" (").append(pagamento.autorizacao().detalhe()).append(")");
        }
        pedidos.obter(pagamento.pedidoId()).registrarEvento(aviso.toString());
    }
}
