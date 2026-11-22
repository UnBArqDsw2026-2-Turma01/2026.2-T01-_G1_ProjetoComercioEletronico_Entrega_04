package comercio.transacao.aplicacao;

import comercio.catalogo.dominio.Anuncio;
import comercio.catalogo.dominio.Cotacao;
import comercio.catalogo.dominio.IFrete;
import comercio.contas.dominio.CEP;
import comercio.transacao.dominio.ICotacaoEntrega;

/**
 * Servico de Entrega: calcula frete e prazo de um anuncio para um CEP.
 *
 * Origem: decisao 11 do Diagrama de Classes da Entrega 2. O calculo nao pode
 * pertencer a Envio, porque e pedido na ficha do produto, quando ainda nao
 * existe pedido nem envio (RN-A04). E um servico de dominio proprio.
 *
 * Fornece IFrete ao Catalogo e consulta a Transportadora pela porta
 * ICotacaoEntrega. Fica entre as duas inversoes de dependencia do desenho.
 */
public final class ServicoDeEntrega implements IFrete {

    private final ICotacaoEntrega transportadora;

    public ServicoDeEntrega(ICotacaoEntrega transportadora) {
        if (transportadora == null) {
            throw new IllegalArgumentException("O Servico de Entrega precisa de uma transportadora");
        }
        this.transportadora = transportadora;
    }

    @Override
    public Cotacao cotar(CEP destino, Anuncio anuncio) {
        return transportadora.cotar(destino, anuncio.pesoEmGramas());
    }
}
