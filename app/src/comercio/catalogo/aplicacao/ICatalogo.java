package comercio.catalogo.aplicacao;

import java.util.List;

/**
 * Interface provida pelo Servico de Catalogo (Diagrama de Componentes da
 * Entrega 2): busca com refinamentos e sugestoes ao digitar.
 *
 * Casos de uso: UC01, UC02, UC03, UC04, UC30. Linha da matriz: TR-01.
 *
 * Contrato congelado na fundacao: a camada de Controle e a Apresentacao
 * dependem dele. Mudanca de assinatura e combinada antes.
 */
public interface ICatalogo {

    /** Executa a busca. Ordenacao desconhecida e recusada com RF-A03; CEP invalido, com RN-B04. */
    ListagemDTO buscar(PedidoDeBusca pedido);

    /** Termos do acervo que comecam com o prefixo digitado. No maximo cinco, sem repeticao (UC02). */
    List<String> sugerir(String prefixo);
}
