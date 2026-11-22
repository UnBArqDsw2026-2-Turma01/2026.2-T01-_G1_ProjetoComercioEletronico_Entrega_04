package comercio.contas.dominio;

/**
 * Reputacao de um vendedor: nota media e quantidade de avaliacoes.
 *
 * Origem: RN-A05 e RF-A05 — todo anuncio exibe a nota, o numero de avaliacoes e
 * a reputacao do vendedor ja na listagem.
 *
 * INFERIDO: a formula real nao e observavel (limite registrado no Diagrama de
 * Classes da Entrega 2). Adota-se a media aritmetica das notas, que e a forma
 * mais simples de tornar o atributo derivado e verificavel por teste.
 *
 * Imutavel: uma avaliacao nova produz outra Reputacao.
 */
public final class Reputacao {

    private final long somaDasNotas;
    private final int avaliacoes;

    private Reputacao(long somaDasNotas, int avaliacoes) {
        this.somaDasNotas = somaDasNotas;
        this.avaliacoes = avaliacoes;
    }

    public static Reputacao semAvaliacoes() {
        return new Reputacao(0, 0);
    }

    /** Ponto de partida para a semente: uma media ja acumulada. */
    public static Reputacao acumulada(double nota, int avaliacoes) {
        if (avaliacoes < 0 || nota < 0 || nota > 5) {
            throw new IllegalArgumentException("Reputacao fora do intervalo");
        }
        return new Reputacao(Math.round(nota * avaliacoes), avaliacoes);
    }

    public Reputacao comAvaliacao(int nota) {
        if (nota < 1 || nota > 5) {
            throw new IllegalArgumentException("A nota vai de 1 a 5");
        }
        return new Reputacao(somaDasNotas + nota, avaliacoes + 1);
    }

    /** Media das notas, de 0 a 5. Zero quando ainda nao ha avaliacao. */
    public double nota() {
        return avaliacoes == 0 ? 0.0 : (double) somaDasNotas / avaliacoes;
    }

    public int avaliacoes() {
        return avaliacoes;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Reputacao)) {
            return false;
        }
        Reputacao outra = (Reputacao) o;
        return somaDasNotas == outra.somaDasNotas && avaliacoes == outra.avaliacoes;
    }

    @Override
    public int hashCode() {
        return (int) (somaDasNotas * 31 + avaliacoes);
    }

    @Override
    public String toString() {
        return String.format("%.1f (%d)", nota(), avaliacoes);
    }
}
