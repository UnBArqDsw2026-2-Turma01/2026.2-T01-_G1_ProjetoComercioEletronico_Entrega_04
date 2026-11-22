package comercio.catalogo.aplicacao;

import comercio.contas.dominio.CEP;

/**
 * Converte o CEP que chega como texto na requisicao no value object CEP.
 *
 * Existe por causa de uma tensao entre dois contratos da fundacao: a interface
 * congelada IFichaProduto recebe CEP, que e um tipo do dominio de Contas, e a
 * camada de Controle nao pode importar dominio (DAS 5.1). Com esta conversao
 * na camada de Aplicacao, o controlador entrega o texto e recebe de volta o
 * argumento pronto, sem nunca nomear a classe CEP. A validacao continua sendo
 * a do value object: CEP invalido e recusado com RN-B04.
 *
 * Se a interface puder mudar numa proxima versao, o melhor e IFichaProduto
 * receber o CEP como texto, como PedidoDeBusca ja faz, e esta classe some.
 *
 * Autor: Pedro Luciano de Azevedo (Entrega 4).
 */
public final class CepInformado {

    private CepInformado() {
    }

    /** null quando o comprador nao informou CEP; o value object validado quando informou (RN-B04). */
    public static CEP ouNulo(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        return CEP.de(texto.trim());
    }
}
