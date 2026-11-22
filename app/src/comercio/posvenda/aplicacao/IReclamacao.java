package comercio.posvenda.aplicacao;

/**
 * Interface provida pelo Servico de Pos-venda (Diagrama de Componentes da
 * Entrega 2, linha TR-09 da matriz do DAS). Requer IPagamento, para o
 * estorno, e IRastreio, para conhecer o pedido.
 *
 * Casos de uso: UC17 (avaliar), UC18 (abrir reclamacao), UC19 (responder),
 * UC20 (mediar) e UC21 (reembolsar). Quem age e sempre o usuario da sessao,
 * informado pela camada de Controle; nenhum metodo aceita o papel como
 * parametro: o servico o confere na conta.
 *
 * Autor: Pedro Luciano de Azevedo (Entrega 4).
 */
public interface IReclamacao {

    /** UC17: o comprador avalia um item de um pedido entregue; a reputacao do vendedor e recalculada (RN-A05). */
    AvaliacaoDTO avaliar(String compradorId, String pedidoId, String anuncioId, int nota, String comentario);

    /** UC18: o comprador abre uma reclamacao sobre um pedido enviado ou entregue. */
    ReclamacaoDTO abrir(String compradorId, String pedidoId, String anuncioId, String motivo);

    /** UC19: o vendedor do envio responde. */
    ReclamacaoDTO responder(String vendedorId, String reclamacaoId, String resposta);

    /**
     * UC20 e UC21: o atendimento assume a mediacao (decisao nula) ou decide.
     * PROCEDENTE estorna pelo IPagamento e encerra como REEMBOLSADA;
     * IMPROCEDENTE encerra como RESOLVIDA.
     */
    ReclamacaoDTO mediar(String atendenteId, String reclamacaoId, String decisao, String parecer);

    /** A reclamacao, para o comprador, o vendedor envolvido ou o atendimento. */
    ReclamacaoDTO reclamacao(String usuarioId, String reclamacaoId);

    /** O pos-venda do pedido, para o comprador, um vendedor do pedido ou o atendimento. */
    PosVendaDTO posVenda(String usuarioId, String pedidoId);
}
