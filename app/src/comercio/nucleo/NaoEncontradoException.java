package comercio.nucleo;

/** O recurso pedido nao existe. A camada de controle responde HTTP 404. */
public class NaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
