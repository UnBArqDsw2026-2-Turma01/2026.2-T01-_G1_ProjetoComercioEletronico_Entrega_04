package comercio.nucleo;

/**
 * Ha sessao, mas o usuario nao pode executar a operacao (por exemplo, responder
 * a reclamacao de um item que nao e dele). A camada de controle responde HTTP 403.
 */
public class AcessoNegadoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
