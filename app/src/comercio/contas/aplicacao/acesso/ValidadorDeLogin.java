package comercio.contas.aplicacao.acesso;

/**
 * Elo da Chain of Responsibility do login.
 *
 * Cada elo decide uma coisa so. Se aprova, passa adiante; se recusa, a cadeia
 * para ali e nenhum elo seguinte roda. A ordem e montada pelo ServicoDeAcesso
 * e pode mudar sem que nenhum elo saiba quem vem depois dele.
 */
public abstract class ValidadorDeLogin {

    private ValidadorDeLogin proximo;

    /** Liga o proximo elo e devolve esse proximo, para encadear em uma linha. */
    public ValidadorDeLogin encadear(ValidadorDeLogin seguinte) {
        this.proximo = seguinte;
        return seguinte;
    }

    public final Veredito validar(ContextoDeLogin contexto) {
        return verificar(contexto);
    }

    protected abstract Veredito verificar(ContextoDeLogin contexto);

    protected final String nome() {
        return getClass().getSimpleName();
    }

    protected final Veredito aprovar(ContextoDeLogin contexto, String detalhe) {
        contexto.registrar(nome(), detalhe);
        return proximo == null ? Veredito.APROVADO : proximo.validar(contexto);
    }

    protected final Veredito recusar(ContextoDeLogin contexto, String detalhe) {
        contexto.registrar(nome(), detalhe);
        return Veredito.RECUSADO;
    }

    protected final Veredito suspender(ContextoDeLogin contexto, String detalhe) {
        contexto.registrar(nome(), detalhe);
        return Veredito.EXIGE_DOIS_FATORES;
    }
}
