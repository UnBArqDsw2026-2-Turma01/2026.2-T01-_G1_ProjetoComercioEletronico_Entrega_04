package comercio.demonstracao;

/**
 * Roteiro de demonstracao no terminal (make demo).
 *
 * Executa, sem servidor HTTP, o que ja esta integrado nas camadas da Visao
 * Logica. Cresce a cada marco ate cobrir o cenario de compra de ponta a ponta.
 */
public final class Roteiro {

    private Roteiro() {
    }

    public static void main(String[] args) {
        DemonstracaoDecorator.main(args);
    }
}
