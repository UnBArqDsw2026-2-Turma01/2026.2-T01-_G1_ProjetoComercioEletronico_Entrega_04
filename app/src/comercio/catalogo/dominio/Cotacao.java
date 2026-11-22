package comercio.catalogo.dominio;

import comercio.nucleo.Dinheiro;

/** Resultado de uma cotacao de entrega: quanto custa e em quantos dias uteis chega. */
public final class Cotacao {

    private final Dinheiro valor;
    private final int prazoDias;

    public Cotacao(Dinheiro valor, int prazoDias) {
        if (valor == null) {
            throw new IllegalArgumentException("Cotacao sem valor");
        }
        if (prazoDias < 0) {
            throw new IllegalArgumentException("Prazo negativo");
        }
        this.valor = valor;
        this.prazoDias = prazoDias;
    }

    public Dinheiro valor() {
        return valor;
    }

    public int prazoDias() {
        return prazoDias;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Cotacao)) {
            return false;
        }
        Cotacao outra = (Cotacao) o;
        return valor.equals(outra.valor) && prazoDias == outra.prazoDias;
    }

    @Override
    public int hashCode() {
        return valor.hashCode() * 31 + prazoDias;
    }

    @Override
    public String toString() {
        return valor + " em " + prazoDias + " dia(s)";
    }
}
