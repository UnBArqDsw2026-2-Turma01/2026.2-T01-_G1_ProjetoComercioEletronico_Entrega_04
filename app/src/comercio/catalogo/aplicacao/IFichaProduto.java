package comercio.catalogo.aplicacao;

import comercio.contas.dominio.CEP;

/**
 * Interface provida pelo Servico de Catalogo (Diagrama de Componentes da
 * Entrega 2): a ficha de um anuncio.
 *
 * Casos de uso: UC05, UC06, UC07. Linhas da matriz: TR-02, TR-03.
 */
public interface IFichaProduto {

    /**
     * Ficha do anuncio, com a reputacao do vendedor (RN-A05) e as demais
     * ofertas do mesmo produto (RN-B09). Quando ha CEP, traz frete e prazo
     * (RN-A04); quando nao ha, esses dois campos vem nulos.
     */
    FichaDTO ficha(String anuncioId, CEP cepOuNulo);
}
