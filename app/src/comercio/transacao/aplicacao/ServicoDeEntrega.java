package comercio.transacao.aplicacao;

import java.util.ArrayList;
import java.util.List;

import comercio.catalogo.dominio.Anuncio;
import comercio.catalogo.dominio.Cotacao;
import comercio.catalogo.dominio.IFrete;
import comercio.contas.dominio.CEP;
import comercio.nucleo.AcessoNegadoException;
import comercio.transacao.dominio.ICotacaoEntrega;
import comercio.transacao.dominio.pedido.Pedido;
import comercio.transacao.dominio.pedido.StatusPedido;
import comercio.transacao.persistencia.RepositorioDePedidos;

/**
 * Servico de Entrega: fornece IFrete ao Catalogo e IRastreio aos demais.
 *
 * Frete. Origem: decisao 11 do Diagrama de Classes da Entrega 2. O calculo nao
 * pode pertencer a Envio, porque e pedido na ficha do produto, quando ainda nao
 * existe pedido nem envio (RN-A04). Consulta a Transportadora pela porta
 * ICotacaoEntrega.
 *
 * Rastreio (UC13, UC26, UC27). Cada chamada a avancar leva o pedido um passo
 * adiante: o vendedor separa, despacha, a transportadora entrega. Quem decide
 * se o passo e possivel e o estado do pedido (padrao State): um pedido que
 * ainda aguarda pagamento recusa ser despachado.
 */
public final class ServicoDeEntrega implements IFrete, IRastreio {

    private final ICotacaoEntrega transportadora;
    private final RepositorioDePedidos pedidos;

    public ServicoDeEntrega(ICotacaoEntrega transportadora, RepositorioDePedidos pedidos) {
        if (transportadora == null) {
            throw new IllegalArgumentException("O Servico de Entrega precisa de uma transportadora");
        }
        this.transportadora = transportadora;
        this.pedidos = pedidos;
    }

    /** So frete, sem rastreio: suficiente para a busca e para a ficha do produto. */
    public ServicoDeEntrega(ICotacaoEntrega transportadora) {
        this(transportadora, new RepositorioDePedidos());
    }

    @Override
    public Cotacao cotar(CEP destino, Anuncio anuncio) {
        return transportadora.cotar(destino, anuncio.pesoEmGramas());
    }

    @Override
    public PedidoDTO pedido(String pedidoId) {
        return MontadorDePedidoDTO.montar(pedidos.obter(pedidoId), pedidos);
    }

    @Override
    public PedidoDTO pedidoDoComprador(String compradorId, String pedidoId) {
        Pedido pedido = pedidos.obter(pedidoId);
        if (!pedido.compradorId().equals(compradorId)) {
            throw new AcessoNegadoException("O pedido " + pedidoId + " é de outro comprador");
        }
        return MontadorDePedidoDTO.montar(pedido, pedidos);
    }

    @Override
    public List<PedidoDTO> pedidosDe(String compradorId) {
        List<PedidoDTO> lista = new ArrayList<PedidoDTO>();
        for (Pedido pedido : pedidos.doComprador(compradorId)) {
            lista.add(MontadorDePedidoDTO.montar(pedido, pedidos));
        }
        return lista;
    }

    @Override
    public synchronized PedidoDTO avancar(String pedidoId) {
        Pedido pedido = pedidos.obter(pedidoId);
        pedido.avancar();
        pedido.registrarEvento(descricaoDoPasso(pedido.getStatus()));
        return MontadorDePedidoDTO.montar(pedido, pedidos);
    }

    private static String descricaoDoPasso(StatusPedido alcancado) {
        switch (alcancado) {
            case EM_SEPARACAO:
                return "Vendedor iniciou a separação dos itens";
            case ENVIADO:
                return "Vendedor despachou o pedido; transportadora coletou";
            case ENTREGUE:
                return "Transportadora confirmou a entrega";
            default:
                return "Pedido agora está " + alcancado.rotulo().toLowerCase();
        }
    }
}
