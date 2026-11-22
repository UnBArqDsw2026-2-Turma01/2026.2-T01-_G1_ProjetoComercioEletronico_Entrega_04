package comercio.transacao.aplicacao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import comercio.catalogo.aplicacao.IAnuncio;
import comercio.transacao.dominio.carrinho.Carrinho;
import comercio.transacao.dominio.carrinho.ItemCarrinho;
import comercio.transacao.persistencia.RepositorioDeCarrinhos;

/**
 * Servico de Carrinho: fornece ICarrinho (UC08).
 *
 * Obtem o anuncio pelo contrato IAnuncio do Catalogo, que e a unica coisa que
 * a Transacao conhece do Catalogo alem do seu dominio. As regras (RN-B01,
 * RN-B07, RF-B02) estao no Carrinho; o servico localiza, delega e monta o DTO.
 */
public final class ServicoDeCarrinho implements ICarrinho {

    private final RepositorioDeCarrinhos carrinhos;
    private final IAnuncio anuncios;

    public ServicoDeCarrinho(RepositorioDeCarrinhos carrinhos, IAnuncio anuncios) {
        this.carrinhos = carrinhos;
        this.anuncios = anuncios;
    }

    @Override
    public CarrinhoDTO ver(String compradorId) {
        return montar(carrinhos.de(compradorId));
    }

    @Override
    public CarrinhoDTO adicionar(String compradorId, String anuncioId, int quantidade) {
        Carrinho carrinho = carrinhos.de(compradorId);
        carrinho.adicionar(anuncios.obter(anuncioId), quantidade);
        return montar(carrinho);
    }

    @Override
    public CarrinhoDTO alterarQuantidade(String compradorId, String anuncioId, int quantidade) {
        Carrinho carrinho = carrinhos.de(compradorId);
        carrinho.alterarQuantidade(anuncioId, quantidade);
        return montar(carrinho);
    }

    @Override
    public CarrinhoDTO selecionar(String compradorId, String anuncioId, boolean selecionado) {
        Carrinho carrinho = carrinhos.de(compradorId);
        carrinho.selecionar(anuncioId, selecionado);
        return montar(carrinho);
    }

    @Override
    public CarrinhoDTO remover(String compradorId, String anuncioId) {
        Carrinho carrinho = carrinhos.de(compradorId);
        carrinho.remover(anuncioId);
        return montar(carrinho);
    }

    private static CarrinhoDTO montar(Carrinho carrinho) {
        List<CarrinhoDTO.Grupo> grupos = new ArrayList<CarrinhoDTO.Grupo>();
        int unidades = 0;
        for (Map.Entry<String, List<ItemCarrinho>> grupo : carrinho.porVendedor().entrySet()) {
            List<CarrinhoDTO.Item> itens = new ArrayList<CarrinhoDTO.Item>();
            String vendedorNome = "";
            for (ItemCarrinho item : grupo.getValue()) {
                vendedorNome = item.anuncio().vendedor().nome();
                itens.add(new CarrinhoDTO.Item(item.anuncio().id(), item.anuncio().titulo(), item.anuncio().preco(),
                        item.quantidade(), item.anuncio().estoque(), item.selecionado(), item.subtotal()));
                if (item.selecionado()) {
                    unidades += item.quantidade();
                }
            }
            grupos.add(new CarrinhoDTO.Grupo(grupo.getKey(), vendedorNome, itens));
        }
        return new CarrinhoDTO(grupos, unidades, carrinho.subtotal());
    }
}
