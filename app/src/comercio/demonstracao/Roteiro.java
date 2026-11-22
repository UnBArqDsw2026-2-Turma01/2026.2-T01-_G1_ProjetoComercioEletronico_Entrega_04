package comercio.demonstracao;

import java.util.List;

import comercio.Aplicacao;
import comercio.catalogo.dominio.ConsultaCatalogo;
import comercio.catalogo.dominio.Faceta;
import comercio.catalogo.dominio.FonteDeResultados;
import comercio.catalogo.dominio.ItemListagem;
import comercio.catalogo.dominio.Listagem;
import comercio.catalogo.dominio.Ordenacao;
import comercio.catalogo.dominio.refinamento.ComFiltroDeFacetas;
import comercio.catalogo.dominio.refinamento.ComFreteEPrazo;
import comercio.catalogo.dominio.refinamento.ComOrdenacao;
import comercio.catalogo.dominio.refinamento.ComReputacaoDoVendedor;
import comercio.catalogo.persistencia.CatalogoEmMemoria;
import comercio.externos.RelogioSimulado;
import comercio.nucleo.RegraDeNegocioException;
import comercio.transacao.aplicacao.CarrinhoDTO;
import comercio.transacao.aplicacao.EstornoDTO;
import comercio.transacao.aplicacao.MeioDTO;
import comercio.transacao.aplicacao.PedidoDTO;

/**
 * Cenario arquitetural CA-01: uma compra de ponta a ponta, no terminal.
 *
 * Percorre, sem servidor HTTP, os casos de uso que o DAS (secao 4.6) declara
 * arquiteturalmente significativos, e mostra em cada passo qual padrao de
 * projeto da Entrega 3 esta trabalhando e onde ele mora na Visao Logica.
 *
 * O tempo e o de um relogio simulado, e os dados sao os da semente: a saida e
 * a mesma a cada execucao.
 *
 * Executar: make demo
 */
public final class Roteiro {

    private static final String JOANA = "com-01";
    private static final String CASA = "end-01";

    private Roteiro() {
    }

    public static void main(String[] args) {
        RelogioSimulado relogio = new RelogioSimulado();
        Aplicacao loja = Aplicacao.montar(relogio);

        cabecalho("G1_ProjetoComercioEletronico | CA-01: compra de ponta a ponta");
        System.out.println("Compradora: " + loja.contas().comprador(JOANA).nome()
                + " | entrega em " + loja.contas().comprador(JOANA).endereco(CASA).resumo());

        buscar(loja);
        PedidoDTO pedido = comprar(loja);
        pagar(loja, pedido);
        acompanhar(loja, pedido);
        outrasFamilias(loja, relogio);
        tudoOuNada(loja);

        cabecalho("Fim do CA-01");
        System.out.println("Decorator, Abstract Factory, Observer e State atuaram sobre os mesmos");
        System.out.println("anuncios e os mesmos pedidos, cada um dentro do pacote de dominio a que");
        System.out.println("serve. Nenhuma classe da Transacao citou uma classe concreta de pagamento.");
    }

    // ------------------------------------------------------------- 1. busca

    private static void buscar(Aplicacao loja) {
        titulo("1. Buscar e refinar", "UC01, UC03, UC04, UC06, UC07 | TR-01, TR-02 | Decorator em catalogo.dominio");

        FonteDeResultados catalogo = new CatalogoEmMemoria(loja.anuncios());
        FonteDeResultados busca = new ComReputacaoDoVendedor(
                new ComFreteEPrazo(
                        new ComOrdenacao(
                                new ComFiltroDeFacetas(catalogo)),
                        loja.frete()));

        ConsultaCatalogo consulta = new ConsultaCatalogo("fone de ouvido")
                .comFiltro(ComFiltroDeFacetas.CONDICAO, "NOVO")
                .comOrdenacao(Ordenacao.MENOR_PRECO)
                .comCep("72230-130");
        Listagem listagem = busca.buscar(consulta);

        System.out.println("  cadeia montada : " + busca.descricao());
        System.out.println("  consulta (URL) : " + consulta.paraURL());
        System.out.println("  resultados     : " + listagem.total());
        int mostrados = 0;
        for (ItemListagem item : listagem.itens()) {
            if (mostrados++ == 3) {
                break;
            }
            System.out.println("    - " + item.anuncio() + " " + item.selos());
        }
        StringBuilder facetas = new StringBuilder();
        for (Faceta faceta : listagem.facetas()) {
            facetas.append(faceta.nome()).append('=').append(faceta.opcao()).append('(')
                    .append(faceta.contagemPrevia()).append(") ");
        }
        System.out.println("  facetas com contagem previa (RN-A03): " + facetas.toString().trim());
        System.out.println("  O frete veio do Servico de Entrega pela porta IFrete: o Catalogo nao importa a Transacao.");
    }

    // ------------------------------------------------- 2. carrinho e checkout

    private static PedidoDTO comprar(Aplicacao loja) {
        titulo("2. Carrinho", "UC08 | TR-04 | RN-B01, RN-B07, RF-B02");
        loja.carrinho().adicionar(JOANA, "anu-001", 2);
        loja.carrinho().adicionar(JOANA, "anu-004", 1);
        CarrinhoDTO carrinho = loja.carrinho().adicionar(JOANA, "anu-005", 1);
        for (CarrinhoDTO.Grupo grupo : carrinho.grupos) {
            System.out.println("  vendedor " + grupo.vendedorNome + ":");
            for (CarrinhoDTO.Item item : grupo.itens) {
                System.out.println("    " + item.quantidade + " x " + item.titulo + " = " + item.subtotal
                        + "  (estoque " + item.estoque + ")");
            }
        }
        System.out.println("  subtotal dos selecionados: " + carrinho.subtotal);
        try {
            loja.carrinho().adicionar(JOANA, "anu-003", 2);
        } catch (RegraDeNegocioException recusa) {
            System.out.println("  recusa " + recusa.codigo() + ": " + recusa.getMessage());
        }

        titulo("3. Finalizar a compra", "UC10 (inclui UC09) | TR-05 | State nasce em AGUARDANDO_PAGAMENTO");
        System.out.println("  estoque de anu-001 antes : " + loja.anuncios().obter("anu-001").estoque());
        PedidoDTO pedido = loja.checkout().finalizar(JOANA, CASA);
        System.out.println("  pedido " + pedido.id + " | situacao " + pedido.status);
        for (PedidoDTO.Envio envio : pedido.envios) {
            System.out.println("  envio de " + envio.vendedorNome + ": " + envio.itens.size() + " item(ns), frete "
                    + envio.frete + ", " + envio.prazoDias + " dia(s)");
        }
        System.out.println("  subtotal " + pedido.subtotal + " + frete " + pedido.frete + " = total " + pedido.total);
        System.out.println("  estoque de anu-001 depois: " + loja.anuncios().obter("anu-001").estoque()
                + "  (reservado pelo checkout)");
        return pedido;
    }

    // ----------------------------------------------------------- 4. pagamento

    private static void pagar(Aplicacao loja, PedidoDTO pedido) {
        titulo("4. Escolher o meio e pagar",
                "UC12, UC24, UC25 | TR-05 | Abstract Factory + Observer em transacao.dominio.pagamento");
        for (MeioDTO meio : loja.pagamentos().meios()) {
            System.out.println("  " + meio.rotulo + " -> " + meio.classesDoTrio);
        }
        System.out.println();

        PedidoDTO recusado = loja.pagamentos().pagar(JOANA, pedido.id, "SALDO_EM_CONTA");
        System.out.println("  tentativa 1, saldo em conta: " + recusado.cobranca.status + " ("
                + recusado.cobranca.detalhe + ")");
        System.out.println("  pedido continua " + recusado.status + "; o estoque segue reservado");

        PedidoDTO pago = loja.pagamentos().pagar(JOANA, pedido.id, "CARTAO");
        System.out.println("  tentativa 2, cartao        : " + pago.cobranca.status + " | trio "
                + pago.cobranca.classesDoTrio);
        System.out.println("  pedido agora esta " + pago.status);
        System.out.println("  observadores avisados pela mudanca de situacao do pagamento:");
        for (String evento : pago.eventos) {
            System.out.println("    - " + evento);
        }
        try {
            loja.pagamentos().pagar(JOANA, pedido.id, "PIX");
        } catch (RegraDeNegocioException recusa) {
            System.out.println("  cobrar de novo -> recusa " + recusa.codigo() + ": " + recusa.getMessage());
        }
    }

    // -------------------------------------------------------------- 5. envio

    private static void acompanhar(Aplicacao loja, PedidoDTO pedido) {
        titulo("5. Acompanhar o envio", "UC13, UC26, UC27 | TR-05 | State em transacao.dominio.pedido");
        loja.rastreio().avancar(pedido.id);
        loja.rastreio().avancar(pedido.id);
        PedidoDTO entregue = loja.rastreio().avancar(pedido.id);
        for (String transicao : entregue.historico) {
            System.out.println("  " + transicao);
        }
        System.out.println("  situacao final: " + entregue.status);
        try {
            loja.rastreio().avancar(pedido.id);
        } catch (RegraDeNegocioException recusa) {
            System.out.println("  avancar de novo -> recusa " + recusa.codigo() + ": " + recusa.getMessage());
        }
    }

    // ------------------------------------------------- 6. as outras familias

    private static void outrasFamilias(Aplicacao loja, RelogioSimulado relogio) {
        titulo("6. O mesmo servico, outras familias",
                "ADR-02 | trocar o meio nao muda uma linha de ServicoDePagamentos");

        PedidoDTO noBoleto = loja.checkout().comprarAgora(JOANA, "anu-007", 1, CASA);
        PedidoDTO emitido = loja.pagamentos().pagar(JOANA, noBoleto.id, "BOLETO");
        System.out.println("  boleto: cobranca " + emitido.cobranca.status + ", pedido " + emitido.status
                + " (familia assincrona)");
        PedidoDTO compensado = loja.pagamentos().confirmarCompensacao(noBoleto.id);
        System.out.println("  compensacao chegou: pedido " + compensado.status);

        PedidoDTO noPix = loja.checkout().comprarAgora(JOANA, "anu-006", 1, CASA);
        loja.pagamentos().pagar(JOANA, noPix.id, "PIX");
        EstornoDTO estorno = loja.pagamentos().estornar(noPix.id, "arrependimento");
        System.out.println("  pix pago e estornado pela politica " + estorno.politica + ": " + estorno.descricao);
        List<String> eventos = loja.rastreio().pedido(noPix.id).eventos;
        System.out.println("  " + eventos.get(eventos.size() - 1) + " | pedido "
                + loja.rastreio().pedido(noPix.id).status);

        PedidoDTO atrasado = loja.checkout().comprarAgora(JOANA, "anu-021", 1, CASA);
        relogio.avancar(16);
        PedidoDTO expirado = loja.pagamentos().pagar(JOANA, atrasado.id, "CARTAO");
        System.out.println("  16 min depois, no cartao (janela de 15): cobranca " + expirado.cobranca.status
                + ", pedido " + expirado.status);
    }

    // ------------------------------------------------------- 7. tudo ou nada

    private static void tudoOuNada(Aplicacao loja) {
        titulo("7. Fechamento tudo ou nada", "ADR-05 | RN-B07");
        loja.carrinho().adicionar(JOANA, "anu-002", 2);
        loja.carrinho().adicionar(JOANA, "anu-003", 1);
        loja.servicoDeAnuncios().reservar("anu-003", 1);
        System.out.println("  carrinho com anu-002 (2 un.) e anu-003 (ultima unidade); outro comprador leva anu-003");
        int antes = loja.anuncios().obter("anu-002").estoque();
        try {
            loja.checkout().finalizar(JOANA, CASA);
        } catch (RegraDeNegocioException recusa) {
            System.out.println("  recusa " + recusa.codigo() + ": " + recusa.getMessage());
        }
        System.out.println("  estoque de anu-002: " + antes + " antes, " + loja.anuncios().obter("anu-002").estoque()
                + " depois | nenhum pedido novo foi criado");
    }

    // ------------------------------------------------------------ impressao

    private static void cabecalho(String texto) {
        System.out.println();
        System.out.println("=========================================================================");
        System.out.println(" " + texto);
        System.out.println("=========================================================================");
    }

    private static void titulo(String texto, String rastro) {
        System.out.println();
        System.out.println(texto);
        System.out.println("[" + rastro + "]");
    }
}
