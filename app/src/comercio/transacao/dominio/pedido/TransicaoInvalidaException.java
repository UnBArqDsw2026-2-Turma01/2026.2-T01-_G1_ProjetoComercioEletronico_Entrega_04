package comercio.transacao.dominio.pedido;

import comercio.nucleo.RegraDeNegocioException;

/**
 * Tentativa de executar, sobre um pedido, uma operacao que o estado atual nao
 * admite: pagar o que ja foi pago, despachar o que nao foi pago, avancar o que
 * ja foi entregue.
 *
 * O codigo "ADR-05" aponta para a decisao do DAS que justifica a recusa.
 */
public class TransicaoInvalidaException extends RegraDeNegocioException {

    private static final long serialVersionUID = 1L;

    public TransicaoInvalidaException(String mensagem) {
        super("ADR-05", mensagem);
    }
}
