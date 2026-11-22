package comercio.transacao.dominio;

/**
 * Porta para o tempo.
 *
 * Cada familia de pagamento tem uma janela (quinze minutos para o cartao, tres
 * dias para o boleto). Para que a expiracao seja testavel e a demonstracao
 * seja reproduzivel, o dominio nao le o relogio do sistema: pergunta a esta
 * porta. Em producao ela responde o horario real; nos testes e no roteiro, um
 * relogio que avanca sob comando.
 */
public interface Relogio {

    /** Minutos decorridos desde um instante fixo. So a diferenca entre duas leituras importa. */
    long minutoAtual();
}
