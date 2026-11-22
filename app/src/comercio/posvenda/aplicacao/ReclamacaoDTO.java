package comercio.posvenda.aplicacao;

import java.util.Collections;
import java.util.List;

import comercio.nucleo.Dinheiro;

/** Uma reclamacao como a tela a mostra, com a linha do tempo. Estrutura de dados, sem comportamento. */
public final class ReclamacaoDTO {

    /** O que a politica de estorno da familia de pagamento fez (UC21). */
    public static final class Estorno {
        public final Dinheiro valor;
        public final String descricao;
        public final String politica;
        public final int prazoEmDias;

        public Estorno(Dinheiro valor, String descricao, String politica, int prazoEmDias) {
            this.valor = valor;
            this.descricao = descricao;
            this.politica = politica;
            this.prazoEmDias = prazoEmDias;
        }
    }

    public final String id;
    public final String pedidoId;
    public final String compradorId;
    public final String vendedorId;
    public final String vendedorNome;
    /** Nulo quando a reclamacao e sobre o envio inteiro. */
    public final String anuncioId;
    public final String motivo;
    public final String status;
    public final String statusRotulo;
    public final String resposta;
    public final String decisao;
    public final String parecer;
    /** Nulo enquanto nao houve reembolso. */
    public final Estorno estorno;
    public final List<String> linhaDoTempo;

    public ReclamacaoDTO(String id, String pedidoId, String compradorId, String vendedorId, String vendedorNome,
            String anuncioId, String motivo, String status, String statusRotulo, String resposta, String decisao,
            String parecer, Estorno estorno, List<String> linhaDoTempo) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.compradorId = compradorId;
        this.vendedorId = vendedorId;
        this.vendedorNome = vendedorNome;
        this.anuncioId = anuncioId;
        this.motivo = motivo;
        this.status = status;
        this.statusRotulo = statusRotulo;
        this.resposta = resposta;
        this.decisao = decisao;
        this.parecer = parecer;
        this.estorno = estorno;
        this.linhaDoTempo = Collections.unmodifiableList(linhaDoTempo);
    }
}
