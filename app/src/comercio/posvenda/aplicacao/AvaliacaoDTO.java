package comercio.posvenda.aplicacao;

/**
 * Uma avaliacao como a tela a mostra. Quando vem da operacao de avaliar,
 * traz tambem a reputacao do vendedor antes e depois, para tornar visivel
 * que o atributo derivado mudou (RN-A05). Estrutura de dados, sem comportamento.
 */
public final class AvaliacaoDTO {

    public final String id;
    public final String pedidoId;
    public final String anuncioId;
    public final String titulo;
    public final String vendedorId;
    public final String vendedorNome;
    public final int nota;
    public final String comentario;
    /** Nulo fora da operacao de avaliar. */
    public final String reputacaoAntes;
    /** Nulo fora da operacao de avaliar. */
    public final String reputacaoDepois;

    public AvaliacaoDTO(String id, String pedidoId, String anuncioId, String titulo, String vendedorId,
            String vendedorNome, int nota, String comentario, String reputacaoAntes, String reputacaoDepois) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.anuncioId = anuncioId;
        this.titulo = titulo;
        this.vendedorId = vendedorId;
        this.vendedorNome = vendedorNome;
        this.nota = nota;
        this.comentario = comentario;
        this.reputacaoAntes = reputacaoAntes;
        this.reputacaoDepois = reputacaoDepois;
    }
}
