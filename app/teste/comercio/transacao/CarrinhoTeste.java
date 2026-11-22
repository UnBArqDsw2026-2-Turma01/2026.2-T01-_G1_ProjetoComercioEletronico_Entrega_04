package comercio.transacao;

import comercio.Aplicacao;
import comercio.externos.RelogioSimulado;
import comercio.nucleo.Dinheiro;
import comercio.nucleo.NaoEncontradoException;
import comercio.nucleo.RegraDeNegocioException;
import comercio.teste.Casos;
import comercio.teste.Suite;
import comercio.teste.Verifica;
import comercio.transacao.aplicacao.CarrinhoDTO;

public final class CarrinhoTeste implements Suite {

    private static final String JOANA = "com-01";

    private static Aplicacao loja() {
        return Aplicacao.montar(new RelogioSimulado());
    }

    @Override
    public void registrar(Casos c) {
        c.caso("RN-B01: os itens do carrinho sao agrupados por vendedor", () -> {
            Aplicacao loja = loja();
            loja.carrinho().adicionar(JOANA, "anu-001", 1);   // ven-01
            loja.carrinho().adicionar(JOANA, "anu-005", 1);   // ven-04
            CarrinhoDTO carrinho = loja.carrinho().adicionar(JOANA, "anu-004", 2);   // ven-01 de novo
            Verifica.igual(2, carrinho.grupos.size());
            Verifica.igual("ven-01", carrinho.grupos.get(0).vendedorId);
            Verifica.igual(2, carrinho.grupos.get(0).itens.size());
            Verifica.igual("ven-04", carrinho.grupos.get(1).vendedorId);
            Verifica.igual("GameHouse", carrinho.grupos.get(1).vendedorNome);
        });

        c.caso("RN-B07: a quantidade nao passa do estoque do anuncio", () -> {
            Aplicacao loja = loja();
            RegraDeNegocioException recusa = Verifica.lanca(RegraDeNegocioException.class,
                    () -> loja.carrinho().adicionar(JOANA, "anu-003", 2));   // so ha uma unidade
            Verifica.igual("RN-B07", recusa.codigo());
            Verifica.igual(0, loja.carrinho().ver(JOANA).grupos.size());

            loja.carrinho().adicionar(JOANA, "anu-003", 1);
            Verifica.lanca(RegraDeNegocioException.class, () -> loja.carrinho().adicionar(JOANA, "anu-003", 1));
            Verifica.lanca(RegraDeNegocioException.class,
                    () -> loja.carrinho().alterarQuantidade(JOANA, "anu-003", 5));
            Verifica.igual(1, loja.carrinho().ver(JOANA).grupos.get(0).itens.get(0).quantidade);
        });

        c.caso("RN-B07: quantidade menor que um e recusada", () -> {
            Aplicacao loja = loja();
            Verifica.lanca(RegraDeNegocioException.class, () -> loja.carrinho().adicionar(JOANA, "anu-001", 0));
            loja.carrinho().adicionar(JOANA, "anu-001", 1);
            Verifica.lanca(RegraDeNegocioException.class,
                    () -> loja.carrinho().alterarQuantidade(JOANA, "anu-001", 0));
        });

        c.caso("RF-B02: desselecionar tira o item do resumo sem remove-lo do carrinho", () -> {
            Aplicacao loja = loja();
            loja.carrinho().adicionar(JOANA, "anu-001", 2);   // 2 x R$ 249,90
            loja.carrinho().adicionar(JOANA, "anu-004", 1);   // R$ 49,90
            Verifica.igual(Dinheiro.emReais(54970), loja.carrinho().ver(JOANA).subtotal);

            CarrinhoDTO depois = loja.carrinho().selecionar(JOANA, "anu-001", false);
            Verifica.igual(Dinheiro.emReais(4990), depois.subtotal);
            Verifica.igual(1, depois.unidades);
            Verifica.igual(2, depois.grupos.get(0).itens.size());
            Verifica.falso(depois.grupos.get(0).itens.get(0).selecionado, "o item continua no carrinho, desmarcado");
        });

        c.caso("colocar no carrinho nao reserva estoque: a reserva e do checkout", () -> {
            Aplicacao loja = loja();
            loja.carrinho().adicionar(JOANA, "anu-001", 5);
            Verifica.igual(12, loja.anuncios().obter("anu-001").estoque());
        });

        c.caso("anuncio pausado nao entra no carrinho", () -> {
            Aplicacao loja = loja();
            RegraDeNegocioException recusa = Verifica.lanca(RegraDeNegocioException.class,
                    () -> loja.carrinho().adicionar(JOANA, "anu-025", 1));
            Verifica.igual("RN-B07", recusa.codigo());
        });

        c.caso("cada comprador tem o seu carrinho; remover o que nao esta la e sinalizado", () -> {
            Aplicacao loja = loja();
            loja.carrinho().adicionar(JOANA, "anu-001", 1);
            Verifica.igual(0, loja.carrinho().ver("com-02").grupos.size());
            Verifica.lanca(NaoEncontradoException.class, () -> loja.carrinho().remover(JOANA, "anu-999"));
            Verifica.igual(0, loja.carrinho().remover(JOANA, "anu-001").grupos.size());
        });

        c.caso("o carrinho mostra o preco de agora: se o vendedor muda o preco, o resumo acompanha", () -> {
            Aplicacao loja = loja();
            loja.carrinho().adicionar(JOANA, "anu-004", 2);
            loja.anuncios().obter("anu-004").alterarPreco(Dinheiro.emReais(3990));
            Verifica.igual(Dinheiro.emReais(7980), loja.carrinho().ver(JOANA).subtotal);
        });
    }
}
