package comercio.contas.dominio;

/**
 * Vendedor: quem publica anuncios.
 *
 * A reputacao e atributo derivado (a barra em "/reputacao" no Diagrama de
 * Classes da Entrega 2, decisao 9): ninguem a atribui. Ela so muda quando uma
 * avaliacao chega por receberAvaliacao, que e chamado pelo Pos-venda. O DAS
 * registra essa dependencia de dado derivado na figura 5.2.5 e na limitacao
 * 5.6.3.
 */
public final class Vendedor extends Usuario {

    private final TipoVendedor tipo;
    private Reputacao reputacao;

    public Vendedor(String id, String nome, String email, TipoVendedor tipo, Reputacao reputacaoInicial) {
        super(id, nome, email);
        if (tipo == null) {
            throw new IllegalArgumentException("Vendedor sem tipo");
        }
        this.tipo = tipo;
        this.reputacao = reputacaoInicial == null ? Reputacao.semAvaliacoes() : reputacaoInicial;
    }

    @Override
    public String papel() {
        return "VENDEDOR";
    }

    public TipoVendedor tipo() {
        return tipo;
    }

    public synchronized Reputacao reputacao() {
        return reputacao;
    }

    /** Unico caminho pelo qual a reputacao muda. */
    public synchronized void receberAvaliacao(int nota) {
        reputacao = reputacao.comAvaliacao(nota);
    }
}
