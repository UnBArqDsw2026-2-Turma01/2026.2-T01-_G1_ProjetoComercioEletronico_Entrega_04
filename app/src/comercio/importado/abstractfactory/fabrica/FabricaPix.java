package comercio.importado.abstractfactory.fabrica;

import comercio.importado.abstractfactory.dominio.MeioPagamento;
import comercio.importado.abstractfactory.familias.pix.AutorizadorPix;
import comercio.importado.abstractfactory.familias.pix.ComprovantePix;
import comercio.importado.abstractfactory.familias.pix.EstornoPix;
import comercio.importado.abstractfactory.produto.Autorizador;
import comercio.importado.abstractfactory.produto.Comprovante;
import comercio.importado.abstractfactory.produto.PoliticaEstorno;

/**
 * Fabrica concreta (ConcreteFactory, GAMMA et al., 1995) da familia Pix.
 *
 * Entrega os tres produtos que precisam andar juntos quando o comprador escolhe
 * Pix: autorizacao imediata, comprovante com QR Code e devolucao para a conta de
 * origem. O ServicoDeCheckout recebe esta fabrica como FabricaMeioPagamento e
 * nunca ve as classes de familias.pix.
 *
 * Autor: Maria Clara Sena de Lima (familia Pix).
 */
public final class FabricaPix implements FabricaMeioPagamento {

    @Override
    public MeioPagamento meio() {
        return MeioPagamento.PIX;
    }

    @Override
    public Autorizador criarAutorizador() {
        return new AutorizadorPix();
    }

    @Override
    public Comprovante criarComprovante() {
        return new ComprovantePix();
    }

    @Override
    public PoliticaEstorno criarPoliticaEstorno() {
        return new EstornoPix();
    }
}
