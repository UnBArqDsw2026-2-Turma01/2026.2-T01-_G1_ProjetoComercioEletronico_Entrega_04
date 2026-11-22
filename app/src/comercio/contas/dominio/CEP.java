package comercio.contas.dominio;

import comercio.nucleo.RegraDeNegocioException;

/**
 * Value object do CEP: oito digitos, sem separador.
 *
 * Origem: RN-B04 da Engenharia Reversa (Entrega 1), achado de inspecao da
 * pagina de endereco: o CEP e armazenado como oito digitos e formatar e
 * responsabilidade da exibicao. RF-B04 completa: a entrada e normalizada com
 * ou sem hifen. E a decisao 5 do Diagrama de Classes da Entrega 2.
 *
 * A invariante fica no unico ponto de construcao (de), de modo que nao existe
 * CEP invalido circulando pelo sistema. E o que a matriz de rastreabilidade do
 * DAS chama de "validacao sintatica do CEP no construtor" (linha TR-04).
 */
public final class CEP {

    private final String digitos;

    private CEP(String digitos) {
        this.digitos = digitos;
    }

    /** Aceita "72230-130", "72230130" e variacoes com espaco ou ponto. */
    public static CEP de(String entrada) {
        if (entrada == null) {
            throw new RegraDeNegocioException("RN-B04", "Informe o CEP");
        }
        String limpo = entrada.replaceAll("[\\s.\\-]", "");
        if (!limpo.matches("[0-9]{8}")) {
            throw new RegraDeNegocioException("RN-B04", "CEP inválido: são esperados oito dígitos");
        }
        return new CEP(limpo);
    }

    /** Os oito digitos, como sao armazenados. */
    public String digitos() {
        return digitos;
    }

    /** Apresentacao com hifen: 72230-130. */
    public String formatado() {
        return digitos.substring(0, 5) + "-" + digitos.substring(5);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CEP && digitos.equals(((CEP) o).digitos);
    }

    @Override
    public int hashCode() {
        return digitos.hashCode();
    }

    @Override
    public String toString() {
        return formatado();
    }
}
