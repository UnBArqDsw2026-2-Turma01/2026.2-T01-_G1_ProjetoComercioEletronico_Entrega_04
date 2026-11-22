package comercio.transacao.dominio.pedido;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import comercio.contas.dominio.Endereco;
import comercio.nucleo.Dinheiro;

/**
 * Um envio: os itens de um mesmo vendedor, indo para um endereco.
 *
 * Origem: RN-B01 — o pedido e logicamente multiplo, com um envio por vendedor
 * — e RF-B01, que pede o frete de cada grupo calculado em separado. E a
 * decisao 7 do Diagrama de Classes da Entrega 2 (Pedido compoe Envio 1..*).
 *
 * O envio REFERENCIA o endereco da conta do comprador, e nao guarda uma copia
 * (RN-B02 e decisao 4 do Diagrama de Classes). O limite dessa escolha, tambem
 * registrado la: como Endereco e imutavel aqui, o historico nao muda; em um
 * sistema que permitisse editar o endereco, seria preciso um retrato.
 *
 * Guarda frete e prazo, mas nao os calcula: quem calcula e o Servico de
 * Entrega (decisao 11).
 */
public final class Envio {

    private final String vendedorId;
    private final String vendedorNome;
    private final Endereco destino;
    private final List<ItemPedido> itens;
    private final Dinheiro frete;
    private final int prazoDias;

    public Envio(String vendedorId, String vendedorNome, Endereco destino, List<ItemPedido> itens, Dinheiro frete,
            int prazoDias) {
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("Envio sem itens");
        }
        if (destino == null) {
            throw new IllegalArgumentException("Envio sem endereco de destino");
        }
        this.vendedorId = vendedorId;
        this.vendedorNome = vendedorNome;
        this.destino = destino;
        this.itens = new ArrayList<ItemPedido>(itens);
        this.frete = frete;
        this.prazoDias = prazoDias;
    }

    public String vendedorId() {
        return vendedorId;
    }

    public String vendedorNome() {
        return vendedorNome;
    }

    public Endereco destino() {
        return destino;
    }

    public List<ItemPedido> itens() {
        return Collections.unmodifiableList(itens);
    }

    public Dinheiro frete() {
        return frete;
    }

    public int prazoDias() {
        return prazoDias;
    }
}
