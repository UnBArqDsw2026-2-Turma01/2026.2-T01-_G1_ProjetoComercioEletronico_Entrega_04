package comercio.contas.aplicacao.acesso;

/** Elo 1: sem desafio valido, nem se consulta o cadastro. */
public final class ValidadorCaptcha extends ValidadorDeLogin {

    private final DesafiosDeCaptcha desafios;

    public ValidadorCaptcha(DesafiosDeCaptcha desafios) {
        this.desafios = desafios;
    }

    @Override
    protected Veredito verificar(ContextoDeLogin contexto) {
        if (!desafios.consumir(contexto.requisicao().tokenCaptcha())) {
            return recusar(contexto, "desafio ausente, vencido ou já usado");
        }
        return aprovar(contexto, "desafio válido");
    }
}
