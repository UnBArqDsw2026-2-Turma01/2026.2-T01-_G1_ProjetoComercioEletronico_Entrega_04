package comercio.contas.aplicacao.acesso;

/** Como a cadeia terminou. */
public enum Veredito {
    /** Todos os elos aprovaram. */
    APROVADO,
    /** O dispositivo e novo: um codigo foi enviado e o login espera por ele. */
    EXIGE_DOIS_FATORES,
    /** Um elo recusou e interrompeu a cadeia. */
    RECUSADO
}
