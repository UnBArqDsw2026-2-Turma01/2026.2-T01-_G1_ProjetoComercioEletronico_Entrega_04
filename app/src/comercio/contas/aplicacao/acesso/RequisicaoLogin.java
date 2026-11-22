package comercio.contas.aplicacao.acesso;

/**
 * Produto do Builder: tudo o que uma tentativa de login carrega.
 *
 * Na Entrega 3 cada padrao tinha a sua RequisicaoLogin (quatro classes com o
 * mesmo nome e campos diferentes). Aqui ha uma so, imutavel, e o unico jeito de
 * cria-la e pelo RequisicaoLoginBuilder, que recusa combinacoes invalidas antes
 * de a cadeia de validacao comecar.
 */
public final class RequisicaoLogin {

    private final String email;
    private final String senha;
    private final String tokenCaptcha;
    private final String ipOrigem;
    private final String dispositivo;
    private final String codigoDoisFatores;
    private final boolean lembrarDispositivo;

    RequisicaoLogin(String email, String senha, String tokenCaptcha, String ipOrigem, String dispositivo,
            String codigoDoisFatores, boolean lembrarDispositivo) {
        this.email = email;
        this.senha = senha;
        this.tokenCaptcha = tokenCaptcha;
        this.ipOrigem = ipOrigem;
        this.dispositivo = dispositivo;
        this.codigoDoisFatores = codigoDoisFatores;
        this.lembrarDispositivo = lembrarDispositivo;
    }

    public String email() {
        return email;
    }

    public String senha() {
        return senha;
    }

    public String tokenCaptcha() {
        return tokenCaptcha;
    }

    public String ipOrigem() {
        return ipOrigem;
    }

    public String dispositivo() {
        return dispositivo;
    }

    /** Codigo do segundo fator, ou null quando ainda nao foi pedido. */
    public String codigoDoisFatores() {
        return codigoDoisFatores;
    }

    public boolean lembrarDispositivo() {
        return lembrarDispositivo;
    }
}
