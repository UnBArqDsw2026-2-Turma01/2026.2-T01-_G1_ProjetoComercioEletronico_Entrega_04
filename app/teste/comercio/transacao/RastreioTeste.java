package comercio.transacao;

import comercio.Aplicacao;
import comercio.externos.RelogioSimulado;
import comercio.nucleo.RegraDeNegocioException;
import comercio.teste.Casos;
import comercio.teste.Suite;
import comercio.teste.Verifica;
import comercio.transacao.aplicacao.PedidoDTO;

/** O ciclo de vida do pedido, guiado pelo padrao State. */
public final class RastreioTeste implements Suite {

    private static final String JOANA = "com-01";
    private static final String CASA = "end-01";

    private static Aplicacao loja() {
        return Aplicacao.montar(new RelogioSimulado());
    }

    @Override
    public void registrar(Casos c) {
        c.caso("ADR-05: um pedido que aguarda pagamento nao pode ser despachado", () -> {
            Aplicacao loja = loja();
            PedidoDTO pedido = loja.checkout().comprarAgora(JOANA, "anu-004", 1, CASA);
            RegraDeNegocioException recusa = Verifica.lanca(RegraDeNegocioException.class,
                    () -> loja.rastreio().avancar(pedido.id));
            Verifica.igual("ADR-05", recusa.codigo());
            Verifica.igual("AGUARDANDO_PAGAMENTO", loja.rastreio().pedido(pedido.id).status);
        });

        c.caso("UC26 e UC27: pago, o pedido avanca um passo por vez ate a entrega, sem pular etapa", () -> {
            Aplicacao loja = loja();
            PedidoDTO pedido = loja.checkout().comprarAgora(JOANA, "anu-004", 1, CASA);
            loja.pagamentos().pagar(JOANA, pedido.id, "PIX");
            Verifica.igual("EM_SEPARACAO", loja.rastreio().avancar(pedido.id).status);
            Verifica.igual("ENVIADO", loja.rastreio().avancar(pedido.id).status);
            PedidoDTO entregue = loja.rastreio().avancar(pedido.id);
            Verifica.igual("ENTREGUE", entregue.status);
            Verifica.igual("[AGUARDANDO_PAGAMENTO, AGUARDANDO_PAGAMENTO -> PAGO, PAGO -> EM_SEPARACAO, "
                    + "EM_SEPARACAO -> ENVIADO, ENVIADO -> ENTREGUE]", String.valueOf(entregue.historico));
        });

        c.caso("ADR-05: entregue e o fim do ciclo; nao ha passo seguinte", () -> {
            Aplicacao loja = loja();
            PedidoDTO pedido = loja.checkout().comprarAgora(JOANA, "anu-004", 1, CASA);
            loja.pagamentos().pagar(JOANA, pedido.id, "PIX");
            loja.rastreio().avancar(pedido.id);
            loja.rastreio().avancar(pedido.id);
            loja.rastreio().avancar(pedido.id);
            Verifica.igual("ADR-05", Verifica.lanca(RegraDeNegocioException.class,
                    () -> loja.rastreio().avancar(pedido.id)).codigo());
        });

        c.caso("ADR-05: pedido cancelado nao avanca nem e pago", () -> {
            Aplicacao loja = loja();
            PedidoDTO pedido = loja.checkout().comprarAgora(JOANA, "anu-004", 1, CASA);
            loja.checkout().desistir(JOANA, pedido.id);
            Verifica.lanca(RegraDeNegocioException.class, () -> loja.rastreio().avancar(pedido.id));
            Verifica.lanca(RegraDeNegocioException.class, () -> loja.pagamentos().pagar(JOANA, pedido.id, "PIX"));
        });

        c.caso("UC13: o comprador ve os seus pedidos, do mais recente para o mais antigo", () -> {
            Aplicacao loja = loja();
            loja.checkout().comprarAgora(JOANA, "anu-004", 1, CASA);
            loja.checkout().comprarAgora(JOANA, "anu-007", 1, CASA);
            Verifica.igual(2, loja.rastreio().pedidosDe(JOANA).size());
            Verifica.igual("ped-0002", loja.rastreio().pedidosDe(JOANA).get(0).id);
            Verifica.igual(0, loja.rastreio().pedidosDe("com-02").size());
        });
    }
}
