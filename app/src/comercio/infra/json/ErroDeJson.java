package comercio.infra.json;

/** Texto recebido nao e JSON valido. */
public class ErroDeJson extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public ErroDeJson(String mensagem) {
        super(mensagem);
    }
}
