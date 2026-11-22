package comercio.composicao;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import comercio.Aplicacao;
import comercio.catalogo.dominio.ConsultaCatalogo;
import comercio.catalogo.dominio.FonteDeResultados;
import comercio.catalogo.dominio.ItemListagem;
import comercio.catalogo.dominio.Listagem;
import comercio.catalogo.dominio.Ordenacao;
import comercio.catalogo.dominio.refinamento.ComFiltroDeFacetas;
import comercio.catalogo.dominio.refinamento.ComFreteEPrazo;
import comercio.catalogo.dominio.refinamento.ComOrdenacao;
import comercio.catalogo.dominio.refinamento.ComReputacaoDoVendedor;
import comercio.catalogo.persistencia.CatalogoEmMemoria;
import comercio.demonstracao.Roteiro;
import comercio.externos.RelogioSimulado;
import comercio.nucleo.Dinheiro;
import comercio.teste.Casos;
import comercio.teste.Suite;
import comercio.teste.Verifica;
import comercio.transacao.aplicacao.PedidoDTO;

/**
 * Cenario arquitetural CA-01: a compra de ponta a ponta atravessando Catalogo,
 * Contas e Transacao, com os quatro padroes da Entrega 3 no mesmo fluxo.
 */
public final class CenarioCA01Teste implements Suite {

    @Override
    public void registrar(Casos c) {
        c.caso("CA-01: o anuncio achado na busca e o mesmo que o pedido reserva, paga e entrega", () -> {
            Aplicacao loja = Aplicacao.montar(new RelogioSimulado());

            // Decorator: busca refinada, com frete vindo da Transacao pela porta IFrete.
            FonteDeResultados busca = new ComReputacaoDoVendedor(new ComFreteEPrazo(
                    new ComOrdenacao(new ComFiltroDeFacetas(new CatalogoEmMemoria(loja.anuncios()))), loja.frete()));
            Listagem listagem = busca.buscar(new ConsultaCatalogo("fone de ouvido")
                    .comFiltro(ComFiltroDeFacetas.CONDICAO, "NOVO").comOrdenacao(Ordenacao.MENOR_PRECO)
                    .comCep("72230-130"));
            ItemListagem maisBarato = listagem.itens().get(0);
            String escolhido = maisBarato.anuncio().id();
            Verifica.igual("anu-007", escolhido);
            Verifica.igual("R$ 39,40", maisBarato.selos().get(ComFreteEPrazo.SELO_FRETE));
            int estoqueInicial = maisBarato.anuncio().estoque();

            // Carrinho e checkout: o frete do envio e o mesmo que a busca mostrou.
            loja.carrinho().adicionar("com-01", escolhido, 2);
            PedidoDTO pedido = loja.checkout().finalizar("com-01", "end-01");
            Verifica.igual(Dinheiro.emReais(3940), pedido.frete);
            Verifica.igual(estoqueInicial - 2, loja.anuncios().obter(escolhido).estoque());

            // Abstract Factory + Observer + State.
            PedidoDTO pago = loja.pagamentos().pagar("com-01", pedido.id, "PIX");
            Verifica.igual("PAGO", pago.status);
            loja.rastreio().avancar(pedido.id);
            loja.rastreio().avancar(pedido.id);
            Verifica.igual("ENTREGUE", loja.rastreio().avancar(pedido.id).status);
            Verifica.igual(estoqueInicial - 2, loja.anuncios().obter(escolhido).estoque());
        });

        c.caso("CA-01: o roteiro de terminal executa do inicio ao fim (make demo)", () -> {
            PrintStream original = System.out;
            ByteArrayOutputStream capturado = new ByteArrayOutputStream();
            System.setOut(new PrintStream(capturado, true, "UTF-8"));
            try {
                Roteiro.main(new String[0]);
            } finally {
                System.setOut(original);
            }
            String saida = new String(capturado.toByteArray(), "UTF-8");
            Verifica.contem(saida, "situacao final: ENTREGUE");
            Verifica.contem(saida, "[AutorizadorCartao, ComprovanteCartao, EstornoCartao]");
            Verifica.contem(saida, "recusa ADR-05");
            Verifica.contem(saida, "Fim do CA-01");
        });
    }
}
