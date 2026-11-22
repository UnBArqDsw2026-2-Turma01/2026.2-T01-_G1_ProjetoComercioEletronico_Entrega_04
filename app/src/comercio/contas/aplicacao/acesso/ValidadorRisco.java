package comercio.contas.aplicacao.acesso;

/**
 * Elo 3: avalia o risco. Dispositivo que o usuario ja marcou como confiavel
 * passa sem segundo fator (Achado-L03, o fluxo silencioso); dispositivo novo
 * exige codigo. Este elo nao recusa: ele decide o que o proximo cobra.
 */
public final class ValidadorRisco extends ValidadorDeLogin {

    private final RegistroDeDispositivos dispositivos;

    public ValidadorRisco(RegistroDeDispositivos dispositivos) {
        this.dispositivos = dispositivos;
    }

    @Override
    protected Veredito verificar(ContextoDeLogin contexto) {
        if (dispositivos.confiavel(contexto.usuarioId(), contexto.requisicao().dispositivo())) {
            return aprovar(contexto, "dispositivo conhecido, risco baixo");
        }
        contexto.exigirDoisFatores();
        return aprovar(contexto, "dispositivo novo, risco alto: exige segundo fator");
    }
}
