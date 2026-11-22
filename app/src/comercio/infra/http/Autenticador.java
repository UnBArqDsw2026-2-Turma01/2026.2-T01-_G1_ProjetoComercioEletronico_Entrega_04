package comercio.infra.http;

/**
 * Traduz o token de sessao recebido no cabecalho Authorization no identificador
 * do usuario. Devolve null quando o token nao corresponde a nenhuma sessao.
 *
 * A infraestrutura declara a porta; quem a implementa e o pacote Contas e
 * Acesso. Assim o Roteador nao conhece nada sobre contas.
 */
public interface Autenticador {
    String usuarioDoToken(String token);
}
