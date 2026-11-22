package comercio.transacao;

import comercio.Aplicacao;
import comercio.catalogo.aplicacao.IAnuncio;
import comercio.catalogo.aplicacao.ServicoDeAnuncios;
import comercio.catalogo.dominio.Cotacao;
import comercio.catalogo.persistencia.RepositorioDeAnuncios;
import comercio.catalogo.persistencia.SementeDoCatalogo;
import comercio.contas.dominio.CEP;
import comercio.contas.persistencia.RepositorioDeContas;
import comercio.contas.persistencia.SementeDeContas;
import comercio.externos.RelogioSimulado;
import comercio.nucleo.AcessoNegadoException;
import comercio.nucleo.Dinheiro;
import comercio.nucleo.NaoEncontradoException;
import comercio.nucleo.RegraDeNegocioException;
import comercio.teste.Casos;
import comercio.teste.Suite;
import comercio.teste.Verifica;
import comercio.transacao.aplicacao.ICheckout;
import comercio.transacao.aplicacao.PedidoDTO;
import comercio.transacao.aplicacao.ServicoDeCarrinho;
import comercio.transacao.aplicacao.ServicoDeCheckout;
import comercio.transacao.dominio.ICotacaoEntrega;
import comercio.transacao.persistencia.RepositorioDeCarrinhos;
import comercio.transacao.persistencia.RepositorioDePedidos;

public final class CheckoutTeste implements Suite {

    private static final String JOANA = "com-01";
    private static final String CASA = "end-01";   // CEP 72230-130

    private static Aplicacao loja() {
        return Aplicacao.montar(new RelogioSimulado());
    }

    @Override
    public void registrar(Casos c) {
        c.caso("UC10: finalizar cria um pedido aguardando pagamento e reserva o estoque", () -> {
            Aplicacao loja = loja();
            loja.carrinho().adicionar(JOANA, "anu-001", 2);
            PedidoDTO pedido = loja.checkout().finalizar(JOANA, CASA);
            Verifica.igual("ped-0001", pedido.id);
            Verifica.igual("AGUARDANDO_PAGAMENTO", pedido.status);
            Verifica.igual(10, loja.anuncios().obter("anu-001").estoque());
            Verifica.igual(0, loja.carrinho().ver(JOANA).grupos.size());
            Verifica.igual(null, pedido.cobranca);
        });

        c.caso("RN-B01 e RF-B01: um envio por vendedor, cada um com o seu frete", () -> {
            Aplicacao loja = loja();
            loja.carrinho().adicionar(JOANA, "anu-001", 2);   // ven-01, 2 x 250 g, 2 x R$ 249,90
            loja.carrinho().adicionar(JOANA, "anu-005", 1);   // ven-04, 1200 g, R$ 319,90
            PedidoDTO pedido = loja.checkout().finalizar(JOANA, CASA);

            Verifica.igual(2, pedido.envios.size());
            Verifica.igual("ven-01", pedido.envios.get(0).vendedorId);
            Verifica.igual(Dinheiro.emReais(3940), pedido.envios.get(0).frete);   // 500 g: faixa leve + regiao 7
            Verifica.igual("ven-04", pedido.envios.get(1).vendedorId);
            Verifica.igual(Dinheiro.emReais(4940), pedido.envios.get(1).frete);   // 1200 g: faixa media + regiao 7
            Verifica.igual(Dinheiro.emReais(81970), pedido.subtotal);
            Verifica.igual(Dinheiro.emReais(8880), pedido.frete);
            Verifica.igual(Dinheiro.emReais(90850), pedido.total);
            Verifica.contem(pedido.envios.get(0).destino, "72230-130");
        });

        c.caso("ADR-05: checkout e tudo ou nada; faltando estoque de um item, nada e reservado", () -> {
            Aplicacao loja = loja();
            loja.carrinho().adicionar(JOANA, "anu-001", 2);
            loja.carrinho().adicionar(JOANA, "anu-003", 1);            // ultima unidade
            loja.servicoDeAnuncios().reservar("anu-003", 1);           // outro comprador chega antes

            RegraDeNegocioException recusa = Verifica.lanca(RegraDeNegocioException.class,
                    () -> loja.checkout().finalizar(JOANA, CASA));
            Verifica.igual("RN-B07", recusa.codigo());
            Verifica.igual(12, loja.anuncios().obter("anu-001").estoque());   // a reserva do primeiro item voltou
            Verifica.igual(0, loja.rastreio().pedidosDe(JOANA).size());       // nenhum pedido passou a existir
            Verifica.igual(2, loja.carrinho().ver(JOANA).grupos.size());      // o carrinho continua como estava
        });

        c.caso("ADR-05: se a transportadora falha no meio do checkout, o estoque reservado e devolvido", () -> {
            RepositorioDeContas contas = new RepositorioDeContas();
            SementeDeContas.popular(contas);
            RepositorioDeAnuncios anuncios = new RepositorioDeAnuncios();
            SementeDoCatalogo.popular(anuncios, contas.vendedores());
            IAnuncio servicoDeAnuncios = new ServicoDeAnuncios(anuncios);
            RepositorioDeCarrinhos carrinhos = new RepositorioDeCarrinhos();
            RepositorioDePedidos pedidos = new RepositorioDePedidos();
            ICotacaoEntrega foraDoAr = new ICotacaoEntrega() {
                @Override
                public Cotacao cotar(CEP destino, int pesoEmGramas) {
                    throw new IllegalStateException("transportadora fora do ar");
                }
            };
            ICheckout checkout = new ServicoDeCheckout(carrinhos, pedidos, contas, servicoDeAnuncios, foraDoAr,
                    new RelogioSimulado());
            new ServicoDeCarrinho(carrinhos, servicoDeAnuncios).adicionar(JOANA, "anu-001", 3);

            Verifica.lanca(IllegalStateException.class, () -> checkout.finalizar(JOANA, CASA));
            Verifica.igual(12, anuncios.obter("anu-001").estoque());
            Verifica.igual(0, pedidos.total());
        });

        c.caso("RN-B02: o endereco precisa ser da conta do comprador; sem ele nada e reservado", () -> {
            Aplicacao loja = loja();
            loja.carrinho().adicionar(JOANA, "anu-001", 1);
            Verifica.lanca(NaoEncontradoException.class, () -> loja.checkout().finalizar(JOANA, "end-99"));
            Verifica.igual(12, loja.anuncios().obter("anu-001").estoque());
        });

        c.caso("RF-B02: so os itens selecionados viram pedido; os demais ficam no carrinho", () -> {
            Aplicacao loja = loja();
            loja.carrinho().adicionar(JOANA, "anu-001", 1);
            loja.carrinho().adicionar(JOANA, "anu-004", 1);
            loja.carrinho().selecionar(JOANA, "anu-004", false);
            PedidoDTO pedido = loja.checkout().finalizar(JOANA, CASA);
            Verifica.igual(1, pedido.envios.get(0).itens.size());
            Verifica.igual("anu-001", pedido.envios.get(0).itens.get(0).anuncioId);
            Verifica.igual("anu-004", loja.carrinho().ver(JOANA).grupos.get(0).itens.get(0).anuncioId);
            Verifica.igual(40, loja.anuncios().obter("anu-004").estoque());
        });

        c.caso("RF-B02: finalizar sem nenhum item selecionado e recusado", () -> {
            Aplicacao loja = loja();
            RegraDeNegocioException vazio = Verifica.lanca(RegraDeNegocioException.class,
                    () -> loja.checkout().finalizar(JOANA, CASA));
            Verifica.igual("RF-B02", vazio.codigo());
        });

        c.caso("RN-B08: comprar agora nao passa pelo carrinho e nao o altera", () -> {
            Aplicacao loja = loja();
            loja.carrinho().adicionar(JOANA, "anu-004", 1);
            PedidoDTO pedido = loja.checkout().comprarAgora(JOANA, "anu-005", 1, CASA);
            Verifica.igual("AGUARDANDO_PAGAMENTO", pedido.status);
            Verifica.igual("anu-005", pedido.envios.get(0).itens.get(0).anuncioId);
            Verifica.igual(7, loja.anuncios().obter("anu-005").estoque());
            Verifica.igual("anu-004", loja.carrinho().ver(JOANA).grupos.get(0).itens.get(0).anuncioId);
        });

        c.caso("o pedido guarda o preco do momento: mudar o preco do anuncio nao muda o pedido", () -> {
            Aplicacao loja = loja();
            PedidoDTO pedido = loja.checkout().comprarAgora(JOANA, "anu-004", 2, CASA);
            loja.anuncios().obter("anu-004").alterarPreco(Dinheiro.emReais(990));
            PedidoDTO depois = loja.rastreio().pedido(pedido.id);
            Verifica.igual(Dinheiro.emReais(4990), depois.envios.get(0).itens.get(0).precoNoMomento);
            Verifica.igual(pedido.total, depois.total);
        });

        c.caso("desistir de um pedido nao pago o cancela e devolve o estoque", () -> {
            Aplicacao loja = loja();
            PedidoDTO pedido = loja.checkout().comprarAgora(JOANA, "anu-001", 4, CASA);
            Verifica.igual(8, loja.anuncios().obter("anu-001").estoque());
            PedidoDTO cancelado = loja.checkout().desistir(JOANA, pedido.id);
            Verifica.igual("CANCELADO", cancelado.status);
            Verifica.igual(12, loja.anuncios().obter("anu-001").estoque());
        });

        c.caso("ADR-05: depois de pago, o pedido nao e desfeito por desistencia", () -> {
            Aplicacao loja = loja();
            PedidoDTO pedido = loja.checkout().comprarAgora(JOANA, "anu-004", 1, CASA);
            loja.pagamentos().pagar(JOANA, pedido.id, "PIX");
            RegraDeNegocioException recusa = Verifica.lanca(RegraDeNegocioException.class,
                    () -> loja.checkout().desistir(JOANA, pedido.id));
            Verifica.igual("ADR-05", recusa.codigo());
            Verifica.igual("PAGO", loja.rastreio().pedido(pedido.id).status);
        });

        c.caso("o pedido de um comprador nao e visto nem alterado por outro", () -> {
            Aplicacao loja = loja();
            PedidoDTO pedido = loja.checkout().comprarAgora(JOANA, "anu-004", 1, CASA);
            Verifica.lanca(AcessoNegadoException.class, () -> loja.checkout().desistir("com-02", pedido.id));
            Verifica.lanca(AcessoNegadoException.class,
                    () -> loja.rastreio().pedidoDoComprador("com-02", pedido.id));
            Verifica.lanca(AcessoNegadoException.class, () -> loja.pagamentos().pagar("com-02", pedido.id, "PIX"));
            Verifica.igual("AGUARDANDO_PAGAMENTO", loja.rastreio().pedido(pedido.id).status);
        });
    }
}
