package comercio.contas.aplicacao.acesso;

/**
 * Elo 4: segundo fator. Sem exigencia, passa. Exigido e ainda nao informado,
 * envia o codigo e suspende o login. Informado, confere.
 */
public final class ValidadorDoisFatores extends ValidadorDeLogin {

    private final CodigosDeVerificacao codigos;

    public ValidadorDoisFatores(CodigosDeVerificacao codigos) {
        this.codigos = codigos;
    }

    @Override
    protected Veredito verificar(ContextoDeLogin contexto) {
        if (!contexto.exigeDoisFatores()) {
            return aprovar(contexto, "não exigido");
        }
        RequisicaoLogin requisicao = contexto.requisicao();
        if (requisicao.codigoDoisFatores() == null) {
            codigos.enviar(contexto.usuarioId(), requisicao.dispositivo());
            return suspender(contexto, "código enviado, aguardando confirmação");
        }
        if (!codigos.conferir(contexto.usuarioId(), requisicao.dispositivo(), requisicao.codigoDoisFatores())) {
            return recusar(contexto, "código incorreto ou já usado");
        }
        return aprovar(contexto, "código confirmado");
    }
}
