package comercio.transacao.controle;

import java.util.ArrayList;
import java.util.List;

import comercio.infra.http.ModuloDeRotas;
import comercio.infra.http.Requisicao;
import comercio.infra.http.Resposta;
import comercio.infra.http.Rota;
import comercio.transacao.aplicacao.CarrinhoDTO;
import comercio.transacao.aplicacao.ICarrinho;

/**
 * Rotas do carrinho (UC08). Toda rota exige sessao: o carrinho e do comprador
 * que esta logado, e o identificador dele vem do token, nunca do corpo.
 */
public final class ControladorDeCarrinho implements ModuloDeRotas {

    private static final String[] CLASSES = { "ControladorDeCarrinho", "ServicoDeCarrinho", "Carrinho",
            "ServicoDeAnuncios" };

    private final ICarrinho carrinho;

    public ControladorDeCarrinho(ICarrinho carrinho) {
        this.carrinho = carrinho;
    }

    @Override
    public List<Rota> rotas() {
        List<Rota> rotas = new ArrayList<Rota>();
        rotas.add(Rota.get("/api/carrinho", r -> responder(carrinho.ver(r.exigirUsuario()),
                "O carrinho agrupa os itens por vendedor (RN-B01); cada grupo vira um envio no checkout"))
                .uc("UC08").tr("TR-04").classes(CLASSES));

        rotas.add(Rota.post("/api/carrinho/itens", r -> responder(
                carrinho.adicionar(r.exigirUsuario(), r.obrigatorio("anuncioId"), r.inteiro("quantidade", 1)),
                "ServicoDeCarrinho consultou o estoque pela porta IAnuncio antes de aceitar a quantidade (RN-B07)"))
                .uc("UC08").tr("TR-04").classes(CLASSES));

        rotas.add(Rota.patch("/api/carrinho/itens/{anuncioId}", r -> responder(alterar(r),
                "A quantidade e a selecao sao validadas contra o estoque atual do anuncio (RN-B07)"))
                .uc("UC08").tr("TR-04").classes(CLASSES));

        rotas.add(Rota.delete("/api/carrinho/itens/{anuncioId}", r -> responder(
                carrinho.remover(r.exigirUsuario(), r.parametro("anuncioId")),
                "Item retirado do carrinho; nenhum estoque estava reservado, a reserva so acontece no checkout"))
                .uc("UC08").tr("TR-04").classes(CLASSES));
        return rotas;
    }

    private CarrinhoDTO alterar(Requisicao r) {
        String comprador = r.exigirUsuario();
        String anuncio = r.parametro("anuncioId");
        CarrinhoDTO resultado = null;
        if (r.corpo().containsKey("quantidade")) {
            resultado = carrinho.alterarQuantidade(comprador, anuncio, r.inteiro("quantidade", 1));
        }
        if (r.corpo().containsKey("selecionado")) {
            resultado = carrinho.selecionar(comprador, anuncio, r.logico("selecionado", true));
        }
        return resultado == null ? carrinho.ver(comprador) : resultado;
    }

    private static Resposta responder(CarrinhoDTO dto, String mecanismo) {
        return Resposta.ok(Representacao.carrinho(dto)).mecanismo(mecanismo);
    }
}
