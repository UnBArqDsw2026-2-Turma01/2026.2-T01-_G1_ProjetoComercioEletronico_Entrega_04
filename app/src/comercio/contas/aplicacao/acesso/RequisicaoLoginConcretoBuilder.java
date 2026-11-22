package comercio.contas.aplicacao.acesso;

import comercio.nucleo.RegraDeNegocioException;

/** Builder concreto: guarda as partes e so entrega a requisicao se ela fizer sentido. */
public final class RequisicaoLoginConcretoBuilder implements RequisicaoLoginBuilder {

    private String email;
    private String senha;
    private String tokenCaptcha;
    private String ipOrigem = "desconhecido";
    private String dispositivo;
    private String codigoDoisFatores;
    private boolean lembrarDispositivo;

    @Override
    public RequisicaoLoginBuilder comCredenciais(String email, String senha) {
        this.email = email == null ? null : email.trim().toLowerCase();
        this.senha = senha;
        return this;
    }

    @Override
    public RequisicaoLoginBuilder comTokenCaptcha(String token) {
        this.tokenCaptcha = token;
        return this;
    }

    @Override
    public RequisicaoLoginBuilder comTelemetria(String ipOrigem, String dispositivo) {
        if (ipOrigem != null) {
            this.ipOrigem = ipOrigem;
        }
        this.dispositivo = dispositivo;
        return this;
    }

    @Override
    public RequisicaoLoginBuilder comSegundoFator(String codigo) {
        this.codigoDoisFatores = codigo == null || codigo.trim().isEmpty() ? null : codigo.trim();
        return this;
    }

    @Override
    public RequisicaoLoginBuilder lembrandoDispositivo(boolean lembrar) {
        this.lembrarDispositivo = lembrar;
        return this;
    }

    @Override
    public RequisicaoLogin build() {
        if (email == null || email.isEmpty() || senha == null || senha.isEmpty()) {
            throw new RegraDeNegocioException("LOGIN", "Informe e-mail e senha");
        }
        if (dispositivo == null || dispositivo.trim().isEmpty()) {
            throw new RegraDeNegocioException("LOGIN", "A tentativa de login precisa identificar o dispositivo");
        }
        if (codigoDoisFatores != null && !codigoDoisFatores.matches("\\d{6}")) {
            throw new RegraDeNegocioException("LOGIN", "O código de verificação tem 6 dígitos");
        }
        return new RequisicaoLogin(email, senha, tokenCaptcha, ipOrigem, dispositivo.trim(), codigoDoisFatores,
                lembrarDispositivo);
    }
}
