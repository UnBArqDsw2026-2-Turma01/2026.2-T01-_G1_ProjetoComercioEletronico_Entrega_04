package comercio.nucleo;

/** A operacao exige sessao e nao ha nenhuma valida. A camada de controle responde HTTP 401. */
public class NaoAutenticadoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NaoAutenticadoException(String mensagem) {
        super(mensagem);
    }
}
