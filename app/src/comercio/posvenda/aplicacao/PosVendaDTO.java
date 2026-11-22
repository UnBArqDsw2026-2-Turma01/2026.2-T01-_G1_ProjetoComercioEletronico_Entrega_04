package comercio.posvenda.aplicacao;

import java.util.Collections;
import java.util.List;

/**
 * O pos-venda de um pedido, visto por quem pergunta: os itens com a
 * avaliacao de cada um, as reclamacoes e o que o usuario pode fazer agora.
 * Estrutura de dados, sem comportamento.
 */
public final class PosVendaDTO {

    public static final class Item {
        public final String anuncioId;
        public final String titulo;
        public final String vendedorId;
        public final String vendedorNome;
        public final int quantidade;
        /** Nula enquanto o item nao foi avaliado. */
        public final AvaliacaoDTO avaliacao;

        public Item(String anuncioId, String titulo, String vendedorId, String vendedorNome, int quantidade,
                AvaliacaoDTO avaliacao) {
            this.anuncioId = anuncioId;
            this.titulo = titulo;
            this.vendedorId = vendedorId;
            this.vendedorNome = vendedorNome;
            this.quantidade = quantidade;
            this.avaliacao = avaliacao;
        }
    }

    public final String pedidoId;
    public final String compradorId;
    public final String status;
    public final String statusRotulo;
    /** Papel de quem consulta: COMPRADOR, VENDEDOR ou ATENDENTE. */
    public final String papel;
    public final List<Item> itens;
    public final List<ReclamacaoDTO> reclamacoes;
    /** UC17: o pedido foi entregue e quem consulta e o comprador. */
    public final boolean podeAvaliar;
    /** UC18: o pedido foi enviado ou entregue, nao ha reclamacao em andamento e quem consulta e o comprador. */
    public final boolean podeReclamar;

    public PosVendaDTO(String pedidoId, String compradorId, String status, String statusRotulo, String papel,
            List<Item> itens, List<ReclamacaoDTO> reclamacoes, boolean podeAvaliar, boolean podeReclamar) {
        this.pedidoId = pedidoId;
        this.compradorId = compradorId;
        this.status = status;
        this.statusRotulo = statusRotulo;
        this.papel = papel;
        this.itens = Collections.unmodifiableList(itens);
        this.reclamacoes = Collections.unmodifiableList(reclamacoes);
        this.podeAvaliar = podeAvaliar;
        this.podeReclamar = podeReclamar;
    }
}
