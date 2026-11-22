package comercio.nucleo;

/**
 * Violacao de uma regra de negocio.
 *
 * Carrega o codigo da regra na origem ("RN-B07", "RF-A03"), o mesmo usado na
 * Engenharia Reversa da Entrega 1 e na matriz de rastreabilidade do DAS. E esse
 * codigo que permite a quem le uma recusa na tela chegar ao requisito que a
 * motivou. A camada de controle a converte em resposta HTTP 422.
 */
public class RegraDeNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String codigo;

    public RegraDeNegocioException(String codigo, String mensagem) {
        super(mensagem);
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("Toda regra de negocio violada precisa informar o seu codigo");
        }
        this.codigo = codigo;
    }

    public String codigo() {
        return codigo;
    }
}
