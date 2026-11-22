package comercio.transacao.aplicacao;

/**
 * Interface provida pelo Servico de Carrinho (Diagrama de Componentes da
 * Entrega 2). Caso de uso: UC08. Linha da matriz: TR-04.
 */
public interface ICarrinho {

    CarrinhoDTO ver(String compradorId);

    /** RN-B07: recusa quantidade acima do estoque do anuncio. */
    CarrinhoDTO adicionar(String compradorId, String anuncioId, int quantidade);

    CarrinhoDTO alterarQuantidade(String compradorId, String anuncioId, int quantidade);

    /** RF-B02: seleciona ou desseleciona um item sem remove-lo. */
    CarrinhoDTO selecionar(String compradorId, String anuncioId, boolean selecionado);

    CarrinhoDTO remover(String compradorId, String anuncioId);
}
