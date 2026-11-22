package comercio.externos.pagamento;

import comercio.transacao.dominio.pagamento.FabricaMeioPagamento;
import comercio.nucleo.Dinheiro;
import comercio.transacao.dominio.pagamento.MeioPagamento;
import comercio.externos.pagamento.saldo.AutorizadorSaldo;
import comercio.externos.pagamento.saldo.ComprovanteSaldo;
import comercio.externos.pagamento.saldo.EstornoSaldo;
import comercio.transacao.dominio.pagamento.Autorizador;
import comercio.transacao.dominio.pagamento.Comprovante;
import comercio.transacao.dominio.pagamento.PoliticaEstorno;

/**
 * Fabrica concreta (ConcreteFactory, GAMMA et al., 1995) da familia Saldo em
 * conta.
 *
 * E a familia em que a coerencia entre os produtos fica mais visivel: os tres
 * operam sobre o mesmo saldo. O autorizador debita, o comprovante informa o
 * saldo restante e o estorno credita de volta. Para isso, a fabrica cria um
 * unico AutorizadorSaldo e o entrega ao comprovante e ao estorno. O
 * ServicoDeCheckout recebe esta fabrica como FabricaMeioPagamento e nunca ve
 * essa ligacao.
 *
 * Autor: Guilherme Costa Zanella (familia Saldo em conta).
 */
public final class FabricaSaldoEmConta implements FabricaMeioPagamento {

    /** Saldo de demonstracao (R$ 500,00): o valor real nao foi observado. */
    private static final long SALDO_DE_DEMONSTRACAO_EM_CENTAVOS = 50000;

    private final AutorizadorSaldo autorizador;

    public FabricaSaldoEmConta() {
        this(Dinheiro.emReais(SALDO_DE_DEMONSTRACAO_EM_CENTAVOS));
    }

    public FabricaSaldoEmConta(Dinheiro saldoDisponivel) {
        this.autorizador = new AutorizadorSaldo(saldoDisponivel);
    }

    @Override
    public MeioPagamento meio() {
        return MeioPagamento.SALDO_EM_CONTA;
    }

    @Override
    public Autorizador criarAutorizador() {
        return autorizador;
    }

    @Override
    public Comprovante criarComprovante() {
        return new ComprovanteSaldo(autorizador);
    }

    @Override
    public PoliticaEstorno criarPoliticaEstorno() {
        return new EstornoSaldo(autorizador);
    }
}
