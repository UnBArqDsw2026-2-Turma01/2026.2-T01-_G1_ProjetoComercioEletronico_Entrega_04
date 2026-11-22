package comercio.externos;

import comercio.catalogo.dominio.Cotacao;
import comercio.contas.dominio.CEP;
import comercio.nucleo.Dinheiro;
import comercio.transacao.dominio.ICotacaoEntrega;

/**
 * Simulador da Transportadora, sistema externo a plataforma.
 *
 * INFERIDO: a tabela de frete abaixo nao foi observada na Entrega 1. O Recorte A
 * registrou que o frete e calculado por CEP na ficha e na listagem, e nao os
 * valores nem a formula da transportadora. Os numeros servem a demonstracao.
 *
 * A tabela e a mesma que na Entrega 3 ficava dentro do decorador ComFreteEPrazo.
 * Ao integrar, ela saiu do Catalogo e veio para ca: preco de frete e assunto da
 * transportadora, e o Catalogo passou a obte-lo pela porta IFrete.
 *
 * Autor da tabela original: Patrick Anderson Carvalho dos Santos (Entrega 3).
 */
public final class TransportadoraSimulada implements ICotacaoEntrega {

    /** Faixa de peso ate a qual vale a primeira tarifa, em gramas. INFERIDO. */
    private static final int PESO_LEVE_EM_GRAMAS = 500;
    private static final int PESO_MEDIO_EM_GRAMAS = 2000;

    /** Tarifas de demonstracao, em centavos. INFERIDAS. */
    private static final long TARIFA_LEVE_EM_CENTAVOS = 1490L;
    private static final long TARIFA_MEDIA_EM_CENTAVOS = 2490L;
    private static final long TARIFA_PESADA_EM_CENTAVOS = 3990L;

    /** Acrescimo por faixa de distancia, derivado do primeiro digito do CEP. INFERIDO. */
    private static final long ACRESCIMO_POR_REGIAO_EM_CENTAVOS = 350L;

    @Override
    public Cotacao cotar(CEP destino, int pesoEmGramas) {
        int regiao = regiaoDoCep(destino);
        return new Cotacao(Dinheiro.emReais(freteEmCentavos(pesoEmGramas, regiao)), prazoEmDias(regiao));
    }

    /**
     * Primeiro digito do CEP como proxy de regiao. E a unica parte do CEP que a
     * Entrega 1 permite usar com alguma base: RN-B04 registrou que o CEP e
     * armazenado como oito digitos sem separador, e nada alem disso.
     */
    private static int regiaoDoCep(CEP cep) {
        return Character.getNumericValue(cep.digitos().charAt(0));
    }

    private static long freteEmCentavos(int pesoEmGramas, int regiao) {
        long tarifa;
        if (pesoEmGramas <= PESO_LEVE_EM_GRAMAS) {
            tarifa = TARIFA_LEVE_EM_CENTAVOS;
        } else if (pesoEmGramas <= PESO_MEDIO_EM_GRAMAS) {
            tarifa = TARIFA_MEDIA_EM_CENTAVOS;
        } else {
            tarifa = TARIFA_PESADA_EM_CENTAVOS;
        }
        return tarifa + (regiao * ACRESCIMO_POR_REGIAO_EM_CENTAVOS);
    }

    private static int prazoEmDias(int regiao) {
        return 2 + regiao;
    }
}
