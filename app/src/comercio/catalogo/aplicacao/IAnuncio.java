package comercio.catalogo.aplicacao;

import comercio.catalogo.dominio.Anuncio;

/**
 * Interface provida pelo Servico de Anuncios (Diagrama de Componentes da
 * Entrega 2). E por ela que o carrinho e o checkout, no pacote Transacao,
 * consultam uma oferta e movimentam o seu estoque.
 *
 * A dependencia aponta de Transacao para Catalogo, o sentido permitido pela
 * figura 5.2.5 do DAS.
 */
public interface IAnuncio {

    /** Lanca NaoEncontradoException quando o anuncio nao existe. */
    Anuncio obter(String anuncioId);

    /** Retira a quantidade do estoque disponivel. Recusa com RN-B07 se nao houver o bastante. */
    void reservar(String anuncioId, int quantidade);

    /** Devolve ao estoque uma quantidade antes reservada (pedido cancelado ou expirado). */
    void devolver(String anuncioId, int quantidade);
}
