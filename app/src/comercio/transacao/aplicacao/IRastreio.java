package comercio.transacao.aplicacao;

import java.util.List;

/**
 * Interface provida pelo Servico de Entrega (Diagrama de Componentes da Entrega
 * 2). Casos de uso: UC13, UC26 e UC27. Linha da matriz: TR-05.
 */
public interface IRastreio {

    /** O pedido, sem conferir quem pergunta. Usado por outros servicos (Pos-venda). */
    PedidoDTO pedido(String pedidoId);

    /** UC13: o pedido, apenas para o comprador dono dele. */
    PedidoDTO pedidoDoComprador(String compradorId, String pedidoId);

    List<PedidoDTO> pedidosDe(String compradorId);

    /** UC26 e UC27: o vendedor despacha, a transportadora entrega. Um passo por chamada. */
    PedidoDTO avancar(String pedidoId);
}
