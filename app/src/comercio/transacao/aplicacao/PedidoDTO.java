package comercio.transacao.aplicacao;

import java.util.Collections;
import java.util.List;

import comercio.nucleo.Dinheiro;

/**
 * Um pedido como as telas e o Pos-venda precisam ve-lo: situacao, itens com o
 * preco congelado, um envio por vendedor, totais, a linha do tempo e a cobranca
 * mais recente. Estrutura de dados, sem comportamento.
 */
public final class PedidoDTO {

    public static final class Item {
        public final String anuncioId;
        public final String titulo;
        public final String vendedorId;
        public final int quantidade;
        public final Dinheiro precoNoMomento;
        public final Dinheiro subtotal;

        public Item(String anuncioId, String titulo, String vendedorId, int quantidade, Dinheiro precoNoMomento,
                Dinheiro subtotal) {
            this.anuncioId = anuncioId;
            this.titulo = titulo;
            this.vendedorId = vendedorId;
            this.quantidade = quantidade;
            this.precoNoMomento = precoNoMomento;
            this.subtotal = subtotal;
        }
    }

    public static final class Envio {
        public final String vendedorId;
        public final String vendedorNome;
        public final String destino;
        public final Dinheiro frete;
        public final int prazoDias;
        public final List<Item> itens;

        public Envio(String vendedorId, String vendedorNome, String destino, Dinheiro frete, int prazoDias,
                List<Item> itens) {
            this.vendedorId = vendedorId;
            this.vendedorNome = vendedorNome;
            this.destino = destino;
            this.frete = frete;
            this.prazoDias = prazoDias;
            this.itens = Collections.unmodifiableList(itens);
        }
    }

    /** A cobranca mais recente do pedido. */
    public static final class Cobranca {
        public final String meio;
        public final String meioRotulo;
        public final String status;
        public final String statusRotulo;
        public final String detalhe;
        public final String identificador;
        public final String comprovante;
        public final String estorno;
        public final String familia;
        public final List<String> classesDoTrio;

        public Cobranca(String meio, String meioRotulo, String status, String statusRotulo, String detalhe,
                String identificador, String comprovante, String estorno, String familia,
                List<String> classesDoTrio) {
            this.meio = meio;
            this.meioRotulo = meioRotulo;
            this.status = status;
            this.statusRotulo = statusRotulo;
            this.detalhe = detalhe;
            this.identificador = identificador;
            this.comprovante = comprovante;
            this.estorno = estorno;
            this.familia = familia;
            this.classesDoTrio = Collections.unmodifiableList(classesDoTrio);
        }
    }

    public final String id;
    public final String compradorId;
    public final String status;
    public final String statusRotulo;
    public final List<Envio> envios;
    public final Dinheiro subtotal;
    public final Dinheiro frete;
    public final Dinheiro total;
    public final List<String> historico;
    public final List<String> eventos;
    /** Nulo enquanto o pedido nao foi cobrado nenhuma vez. */
    public final Cobranca cobranca;
    public final int tentativasDePagamento;

    public PedidoDTO(String id, String compradorId, String status, String statusRotulo, List<Envio> envios,
            Dinheiro subtotal, Dinheiro frete, Dinheiro total, List<String> historico, List<String> eventos,
            Cobranca cobranca, int tentativasDePagamento) {
        this.id = id;
        this.compradorId = compradorId;
        this.status = status;
        this.statusRotulo = statusRotulo;
        this.envios = Collections.unmodifiableList(envios);
        this.subtotal = subtotal;
        this.frete = frete;
        this.total = total;
        this.historico = Collections.unmodifiableList(historico);
        this.eventos = Collections.unmodifiableList(eventos);
        this.cobranca = cobranca;
        this.tentativasDePagamento = tentativasDePagamento;
    }
}
