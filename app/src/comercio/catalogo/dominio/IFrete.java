package comercio.catalogo.dominio;

import comercio.contas.dominio.CEP;

/**
 * Porta pela qual o Catalogo obtem frete e prazo de um anuncio para um CEP.
 *
 * E a interface IFrete do Diagrama de Componentes da Entrega 2: o Servico de
 * Catalogo a REQUER, e o Servico de Entrega a FORNECE. O frete aparece na
 * listagem e na ficha, antes de existir carrinho ou pedido (RN-A04).
 *
 * Por que a interface mora no Catalogo, e nao na Transacao, onde esta quem a
 * implementa: a figura 5.2.5 do DAS so permite dependencia de Transacao para
 * Catalogo. Se IFrete fosse declarada junto do Servico de Entrega, o Catalogo
 * teria de importar a Transacao e os dois pacotes formariam um ciclo. Com o
 * consumidor dono da porta, a seta de codigo continua em um sentido so. E a
 * mesma inversao de dependencia que o DAS aplica aos sistemas externos
 * (decisao 4 da secao 5.3), usada aqui entre dois pacotes internos.
 */
public interface IFrete {

    /** Assinatura da decisao 11 do Diagrama de Classes: calcularFrete(cep, anuncio). */
    Cotacao cotar(CEP destino, Anuncio anuncio);
}
