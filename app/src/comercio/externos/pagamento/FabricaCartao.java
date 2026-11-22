package comercio.externos.pagamento;

import comercio.transacao.dominio.pagamento.FabricaMeioPagamento;
import comercio.transacao.dominio.pagamento.MeioPagamento;
import comercio.externos.pagamento.cartao.AutorizadorCartao;
import comercio.externos.pagamento.cartao.ComprovanteCartao;
import comercio.externos.pagamento.cartao.EstornoCartao;
import comercio.transacao.dominio.pagamento.Autorizador;
import comercio.transacao.dominio.pagamento.Comprovante;
import comercio.transacao.dominio.pagamento.PoliticaEstorno;

/**
 * Fabrica concreta (ConcreteFactory, GAMMA et al., 1995) da familia Cartao.
 *
 * Entrega os tres produtos que andam juntos quando o comprador paga com cartao:
 * autorizacao sincrona com tres desfechos, recibo com parcelamento e estorno
 * pela fatura. O ServicoDeCheckout recebe esta fabrica como
 * FabricaMeioPagamento e nunca ve as classes de familias.cartao.
 *
 * Os dois parametros opcionais existem para a demonstracao: o cliente escolhe a
 * *familia*, o que o padrao permite, e nunca os produtos. Configurar a fabrica e
 * o jeito de exercitar os tres desfechos do autorizador sem que o Main precise
 * instanciar AutorizadorCartao diretamente — o que quebraria o padrao.
 *
 * Autor: Patrick Anderson Carvalho dos Santos (familia Cartao de credito).
 */
public final class FabricaCartao implements FabricaMeioPagamento {

    private static final int PARCELAS_PADRAO = 3;

    private final int parcelas;
    private final int minutosDecorridosNaTentativa;

    public FabricaCartao() {
        this(PARCELAS_PADRAO, 0);
    }

    public FabricaCartao(int parcelas, int minutosDecorridosNaTentativa) {
        this.parcelas = parcelas;
        this.minutosDecorridosNaTentativa = minutosDecorridosNaTentativa;
    }

    /** Familia de cartao cuja tentativa acontece depois da janela de pagamento. */
    public static FabricaCartao aposMinutos(int minutos) {
        return new FabricaCartao(PARCELAS_PADRAO, minutos);
    }

    @Override
    public MeioPagamento meio() {
        return MeioPagamento.CARTAO;
    }

    @Override
    public Autorizador criarAutorizador() {
        return new AutorizadorCartao(minutosDecorridosNaTentativa);
    }

    @Override
    public Comprovante criarComprovante() {
        return new ComprovanteCartao(parcelas);
    }

    @Override
    public PoliticaEstorno criarPoliticaEstorno() {
        return new EstornoCartao();
    }
}
