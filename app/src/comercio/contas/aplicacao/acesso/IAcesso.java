package comercio.contas.aplicacao.acesso;

/** Entrada e saida da conta (UC22). */
public interface IAcesso {

    /** Desafio de captcha que a pagina de login pede ao abrir. */
    String desafio();

    ResultadoLoginDTO entrar(RequisicaoLogin requisicao);

    boolean sair(String token, String ipOrigem);

    /** Usuario dono do token, ou null. */
    String usuarioDoToken(String token);
}
