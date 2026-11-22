package comercio.posvenda.dominio;

/**
 * Situacao de uma reclamacao (UC18 a UC21).
 *
 * Origem: enumeracao StatusReclamacao do Diagrama de Classes da Entrega 2 e as
 * anotacoes de estado do Diagrama de Atividades do fluxo de reclamacao
 * (Subequipe 01). Classe de origem fraca, declarada assim naquele diagrama:
 * vem do Rich Picture, e nao de observacao, porque o fluxo de reclamacao nao e
 * acessivel sem uma compra real.
 *
 * O Diagrama de Atividades chama de AGUARDANDO_VENDEDOR o intervalo entre a
 * abertura e a resposta; aqui esse intervalo e ABERTA, e a resposta do
 * vendedor ganha estado proprio, RESPONDIDA, para o atendimento saber se ha
 * versao do vendedor antes de decidir.
 *
 * Autor: Pedro Luciano de Azevedo (Entrega 4).
 */
public enum StatusReclamacao {

    ABERTA("Aberta, aguardando o vendedor"),
    RESPONDIDA("Respondida pelo vendedor"),
    EM_MEDIACAO("Em mediação"),
    RESOLVIDA("Resolvida"),
    REEMBOLSADA("Reembolsada");

    private final String rotulo;

    StatusReclamacao(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }

    /** RESOLVIDA e REEMBOLSADA sao finais: nada mais acontece com a reclamacao. */
    public boolean encerrada() {
        return this == RESOLVIDA || this == REEMBOLSADA;
    }
}
