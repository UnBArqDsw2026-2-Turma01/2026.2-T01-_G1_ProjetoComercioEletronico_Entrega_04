package comercio.infra.http;

/** A requisicao esta malformada (campo obrigatorio ausente, numero invalido). HTTP 400. */
public class RequisicaoInvalidaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RequisicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
