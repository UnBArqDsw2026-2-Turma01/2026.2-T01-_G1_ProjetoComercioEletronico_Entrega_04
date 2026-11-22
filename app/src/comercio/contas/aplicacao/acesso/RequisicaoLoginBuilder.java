package comercio.contas.aplicacao.acesso;

/**
 * Builder (GoF) da requisicao de login: monta o objeto em etapas, cada uma com
 * um nome que diz o que ela acrescenta, e valida o conjunto em build().
 */
public interface RequisicaoLoginBuilder {

    RequisicaoLoginBuilder comCredenciais(String email, String senha);

    RequisicaoLoginBuilder comTokenCaptcha(String token);

    RequisicaoLoginBuilder comTelemetria(String ipOrigem, String dispositivo);

    RequisicaoLoginBuilder comSegundoFator(String codigo);

    RequisicaoLoginBuilder lembrandoDispositivo(boolean lembrar);

    RequisicaoLogin build();
}
