package comercio.transacao.controle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import comercio.nucleo.Dinheiro;
import comercio.transacao.aplicacao.CarrinhoDTO;
import comercio.transacao.aplicacao.MeioDTO;
import comercio.transacao.aplicacao.PedidoDTO;

/**
 * Converte os DTOs da camada de Aplicacao no corpo JSON das respostas.
 *
 * Fica na camada de Controle porque o formato da resposta HTTP e assunto dela:
 * a Aplicacao entrega objetos com campos publicos e imutaveis, e so aqui eles
 * viram mapas. Valores em dinheiro saem sempre como centavos e como texto ja
 * formatado, para que a tela nao precise fazer conta com ponto flutuante.
 */
final class Representacao {

    private Representacao() {
    }

    static Map<String, Object> dinheiro(Dinheiro valor) {
        Map<String, Object> mapa = new LinkedHashMap<String, Object>();
        mapa.put("centavos", valor.centavos());
        mapa.put("formatado", valor.formatado());
        return mapa;
    }

    static Map<String, Object> carrinho(CarrinhoDTO carrinho) {
        List<Object> grupos = new ArrayList<Object>();
        for (CarrinhoDTO.Grupo grupo : carrinho.grupos) {
            List<Object> itens = new ArrayList<Object>();
            for (CarrinhoDTO.Item item : grupo.itens) {
                Map<String, Object> i = new LinkedHashMap<String, Object>();
                i.put("anuncioId", item.anuncioId);
                i.put("titulo", item.titulo);
                i.put("precoUnitario", dinheiro(item.precoUnitario));
                i.put("quantidade", item.quantidade);
                i.put("estoque", item.estoque);
                i.put("selecionado", item.selecionado);
                i.put("subtotal", dinheiro(item.subtotal));
                itens.add(i);
            }
            Map<String, Object> g = new LinkedHashMap<String, Object>();
            g.put("vendedorId", grupo.vendedorId);
            g.put("vendedorNome", grupo.vendedorNome);
            g.put("itens", itens);
            grupos.add(g);
        }
        Map<String, Object> corpo = new LinkedHashMap<String, Object>();
        corpo.put("grupos", grupos);
        corpo.put("unidades", carrinho.unidades);
        corpo.put("subtotal", dinheiro(carrinho.subtotal));
        return corpo;
    }

    static Map<String, Object> pedido(PedidoDTO pedido) {
        List<Object> envios = new ArrayList<Object>();
        for (PedidoDTO.Envio envio : pedido.envios) {
            List<Object> itens = new ArrayList<Object>();
            for (PedidoDTO.Item item : envio.itens) {
                Map<String, Object> i = new LinkedHashMap<String, Object>();
                i.put("anuncioId", item.anuncioId);
                i.put("titulo", item.titulo);
                i.put("quantidade", item.quantidade);
                i.put("precoNoMomento", dinheiro(item.precoNoMomento));
                i.put("subtotal", dinheiro(item.subtotal));
                itens.add(i);
            }
            Map<String, Object> e = new LinkedHashMap<String, Object>();
            e.put("vendedorId", envio.vendedorId);
            e.put("vendedorNome", envio.vendedorNome);
            e.put("destino", envio.destino);
            e.put("frete", dinheiro(envio.frete));
            e.put("prazoDias", envio.prazoDias);
            e.put("itens", itens);
            envios.add(e);
        }
        Map<String, Object> corpo = new LinkedHashMap<String, Object>();
        corpo.put("id", pedido.id);
        corpo.put("status", pedido.status);
        corpo.put("statusRotulo", pedido.statusRotulo);
        corpo.put("envios", envios);
        corpo.put("subtotal", dinheiro(pedido.subtotal));
        corpo.put("frete", dinheiro(pedido.frete));
        corpo.put("total", dinheiro(pedido.total));
        corpo.put("historico", pedido.historico);
        corpo.put("eventos", pedido.eventos);
        corpo.put("tentativasDePagamento", pedido.tentativasDePagamento);
        corpo.put("cobranca", pedido.cobranca == null ? null : cobranca(pedido.cobranca));
        return corpo;
    }

    private static Map<String, Object> cobranca(PedidoDTO.Cobranca cobranca) {
        Map<String, Object> c = new LinkedHashMap<String, Object>();
        c.put("meio", cobranca.meio);
        c.put("meioRotulo", cobranca.meioRotulo);
        c.put("status", cobranca.status);
        c.put("statusRotulo", cobranca.statusRotulo);
        c.put("detalhe", cobranca.detalhe);
        c.put("identificador", cobranca.identificador);
        c.put("comprovante", cobranca.comprovante);
        c.put("estorno", cobranca.estorno);
        c.put("familia", cobranca.familia);
        c.put("classesDoTrio", cobranca.classesDoTrio);
        return c;
    }

    static Map<String, Object> meio(MeioDTO meio) {
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        m.put("meio", meio.meio);
        m.put("rotulo", meio.rotulo);
        m.put("familia", meio.familia);
        m.put("assincrono", meio.assincrono);
        m.put("janelaEmMinutos", meio.janelaEmMinutos);
        m.put("classesDoTrio", meio.classesDoTrio);
        return m;
    }

    static String juntar(List<String> partes) {
        StringBuilder texto = new StringBuilder();
        for (String parte : partes) {
            if (texto.length() > 0) {
                texto.append(", ");
            }
            texto.append(parte);
        }
        return texto.toString();
    }
}
