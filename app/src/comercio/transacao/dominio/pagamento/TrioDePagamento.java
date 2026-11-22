package comercio.transacao.dominio.pagamento;

import comercio.nucleo.Dinheiro;

/**
 * Os tres produtos de uma mesma familia de pagamento, criados de uma vez.
 *
 * E o ponto em que o Abstract Factory (GAMMA et al., 1995) entrega a sua
 * garantia. O construtor recebe uma fabrica e pede a ela o autorizador, o
 * comprovante e a politica de estorno. Depois disso nao ha como obter um
 * comprovante de uma familia e um estorno de outra, porque nao existe no
 * codigo nenhum caminho que crie um produto isolado. A combinacao invalida nao
 * e proibida por validacao: ela e inalcancavel por construcao.
 *
 * Tudo o que esta classe conhece sao as quatro interfaces do contrato:
 * FabricaMeioPagamento, Autorizador, Comprovante e PoliticaEstorno. Nenhum
 * nome de classe concreta aparece aqui.
 *
 * Na Entrega 3 esta classe se chamava ServicoDeCheckout e era o cliente do
 * padrao na demonstracao. Na Entrega 4 o servico de aplicacao que atende
 * IPagamento passou a ser ServicoDePagamentos, e esta classe ficou com o que
 * sempre foi a sua essencia: manter o trio unido. O cliente do padrao e o
 * ServicoDePagamentos, que cria um trio para cada cobranca.
 *
 * Rastreabilidade: tarefa "Autorizar pagamento" do BPMN 1 (Entrega 1); estados
 * Autorizando e Pago da Maquina de Estados do Pedido (Entrega 2); ADR-02 do DAS.
 *
 * Autor: Patrick Anderson Carvalho dos Santos (cliente do criacional).
 */
public final class TrioDePagamento {

    private final MeioPagamento meio;
    private final Autorizador autorizador;
    private final Comprovante comprovante;
    private final PoliticaEstorno politicaEstorno;

    /**
     * Recebe a familia escolhida. A selecao da fabrica a partir do
     * MeioPagamento acontece fora daqui, em CatalogoDeFabricas: a interface da
     * fabrica nao deve saber escolher entre as suas implementacoes.
     */
    public TrioDePagamento(FabricaMeioPagamento fabrica) {
        if (fabrica == null) {
            throw new IllegalArgumentException("O trio precisa de uma familia de pagamento");
        }
        this.meio = fabrica.meio();
        this.autorizador = fabrica.criarAutorizador();
        this.comprovante = fabrica.criarComprovante();
        this.politicaEstorno = fabrica.criarPoliticaEstorno();
    }

    public MeioPagamento meio() {
        return meio;
    }

    /** Janela, em minutos, dentro da qual a familia aceita a cobranca. */
    public int prazoLimiteEmMinutos() {
        return autorizador.prazoLimiteEmMinutos();
    }

    /** Familias assincronas (boleto) aprovam a emissao e confirmam o pagamento depois. */
    public boolean ehAssincrono() {
        return autorizador.ehAssincrono();
    }

    /** O estorno desta familia aceita a autorizacao informada? */
    public boolean podeEstornar(Autorizacao autorizacao) {
        return politicaEstorno.podeEstornar(autorizacao);
    }

    public int prazoDeDevolucaoEmDias() {
        return politicaEstorno.prazoDeDevolucaoEmDias();
    }

    /** Estado Autorizando da Maquina de Estados do Pedido. */
    public Autorizacao pagar(String pedido, Dinheiro valor) {
        return autorizador.autorizar(pedido, valor);
    }

    /** O que o comprador recebe depois de pagar. */
    public String comprovanteDe(String pedido, Dinheiro valor, Autorizacao autorizacao) {
        return comprovante.gerar(pedido, valor, autorizacao);
    }

    /** Transicao "Em disputa -> Reembolsado" da Maquina de Estados do Pedido. */
    public String reembolsar(String pedido, Dinheiro valor, Autorizacao autorizacao) {
        if (!politicaEstorno.podeEstornar(autorizacao)) {
            return "Nada a estornar no pedido " + pedido
                    + ": a cobranca nao chegou a ser capturada (" + autorizacao.status() + ")";
        }
        return politicaEstorno.estornar(pedido, valor)
                + " (prazo de " + politicaEstorno.prazoDeDevolucaoEmDias() + " dia(s))";
    }

    /** Resumo da familia montada, para a demonstracao. */
    /*
     * Os quatro metodos abaixo nao fazem parte do padrao: existem para a
     * demonstracao grafica mostrar QUAL trio a fabrica entregou e com que
     * caracteristicas, sem que a janela precise conhecer uma classe concreta.
     * Ela pergunta ao cliente o que ele recebeu; nao escolhe nada. E o mesmo
     * acrescimo registrado no Decorator desta subequipe com `descricao()`.
     */

    public String[] classesDoTrio() {
        return new String[] {
            autorizador.getClass().getSimpleName(),
            comprovante.getClass().getSimpleName(),
            politicaEstorno.getClass().getSimpleName()
        };
    }

    public String descricaoDoAutorizador() {
        return "janela de " + autorizador.prazoLimiteEmMinutos() + " min, "
                + (autorizador.ehAssincrono() ? "compensação assíncrona" : "resposta imediata");
    }

    public String descricaoDoComprovante() {
        return "formato: " + comprovante.formato();
    }

    public String descricaoDoEstorno() {
        return "devolução em " + politicaEstorno.prazoDeDevolucaoEmDias() + " dia(s)";
    }

    public String resumoDaFamilia() {
        return meio.rotulo()
                + " | comprovante em " + comprovante.formato()
                + " | janela de " + autorizador.prazoLimiteEmMinutos() + " min"
                + " | " + (autorizador.ehAssincrono() ? "compensação assíncrona" : "resposta imediata")
                + " | devolução em " + politicaEstorno.prazoDeDevolucaoEmDias() + " dia(s)";
    }
}
