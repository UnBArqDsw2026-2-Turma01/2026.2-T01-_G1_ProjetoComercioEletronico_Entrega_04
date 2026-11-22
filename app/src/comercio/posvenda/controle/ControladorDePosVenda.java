package comercio.posvenda.controle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import comercio.infra.http.ModuloDeRotas;
import comercio.infra.http.Resposta;
import comercio.infra.http.Rota;
import comercio.posvenda.aplicacao.AvaliacaoDTO;
import comercio.posvenda.aplicacao.IReclamacao;
import comercio.posvenda.aplicacao.PosVendaDTO;
import comercio.posvenda.aplicacao.ReclamacaoDTO;

/**
 * Rotas do Pos-venda: avaliar (UC17), abrir reclamacao (UC18), responder
 * (UC19), mediar e reembolsar (UC20 e UC21).
 *
 * Toda rota exige sessao. Quem age vem do token, nunca do corpo: o comprador,
 * o vendedor e o atendimento sao conferidos pelo servico contra a conta e
 * contra o pedido. O controlador le a requisicao, chama IReclamacao e
 * converte o DTO em mapa; nao conhece nenhuma classe de dominio.
 *
 * Nenhuma rota fica sob /api/pedidos: esse prefixo e da Transacao, que
 * verifica a rastreabilidade das proprias rotas por ele. Por isso a
 * avaliacao e POST /api/avaliacoes, com o pedido no corpo, e nao
 * /api/pedidos/{id}/avaliacao, como previa o plano.
 *
 * Autor: Pedro Luciano de Azevedo (Entrega 4).
 */
public final class ControladorDePosVenda implements ModuloDeRotas {

    private final IReclamacao posVenda;

    public ControladorDePosVenda(IReclamacao posVenda) {
        if (posVenda == null) {
            throw new IllegalArgumentException("O controlador do pos-venda precisa de IReclamacao");
        }
        this.posVenda = posVenda;
    }

    @Override
    public List<Rota> rotas() {
        List<Rota> rotas = new ArrayList<Rota>();

        rotas.add(Rota.get("/api/posvenda/{pedidoId}", r -> {
            PosVendaDTO dto = posVenda.posVenda(r.exigirUsuario(), r.parametro("pedidoId"));
            return Resposta.ok(posVenda(dto)).mecanismo("Pós-venda do pedido visto como " + dto.papel
                    + ": o Serviço de Pós-venda consulta o pedido pela porta IRastreio, sem guardá-lo");
        }).uc("UC17", "UC18").tr("TR-03", "TR-09").classes("ControladorDePosVenda", "ServicoDePosVenda", "IRastreio"));

        rotas.add(Rota.post("/api/avaliacoes", r -> {
            AvaliacaoDTO dto = posVenda.avaliar(r.exigirUsuario(), r.obrigatorio("pedidoId"), r.texto("anuncioId"),
                    r.inteiro("nota", 0), r.texto("comentario"));
            return Resposta.criado(avaliacao(dto)).mecanismo("RegistroDeReputacao recalculou a reputação de "
                    + dto.vendedorNome + ": " + dto.reputacaoAntes + " → " + dto.reputacaoDepois
                    + ". A reputação é atributo derivado das avaliações; ninguém a digita (RN-A05)");
        }).uc("UC17").tr("TR-03").classes("ControladorDePosVenda", "ServicoDePosVenda", "Avaliacao",
                "RegistroDeReputacao", "Vendedor"));

        rotas.add(Rota.post("/api/reclamacoes", r -> {
            ReclamacaoDTO dto = posVenda.abrir(r.exigirUsuario(), r.obrigatorio("pedidoId"), r.texto("anuncioId"),
                    r.texto("motivo"));
            return Resposta.criado(reclamacao(dto)).mecanismo("Reclamação " + dto.id + " ABERTA e encaminhada a "
                    + dto.vendedorNome + ". Comprador e vendedor não se falam direto: tudo passa pela plataforma");
        }).uc("UC18").tr("TR-09").classes("ControladorDePosVenda", "ServicoDePosVenda", "Reclamacao", "IRastreio"));

        rotas.add(Rota.post("/api/reclamacoes/{id}/resposta", r -> {
            ReclamacaoDTO dto = posVenda.responder(r.exigirUsuario(), r.parametro("id"), r.texto("resposta"));
            return Resposta.ok(reclamacao(dto)).mecanismo("Reclamação " + dto.id + ": ABERTA → RESPONDIDA. "
                    + "Só o vendedor do envio pode fazer esta transição");
        }).uc("UC19").tr("TR-09").classes("ControladorDePosVenda", "ServicoDePosVenda", "Reclamacao"));

        rotas.add(Rota.post("/api/reclamacoes/{id}/mediacao", r -> {
            ReclamacaoDTO dto = posVenda.mediar(r.exigirUsuario(), r.parametro("id"), r.texto("decisao"),
                    r.texto("parecer"));
            return Resposta.ok(reclamacao(dto)).mecanismo(mecanismoDaMediacao(dto));
        }).uc("UC20", "UC21").tr("TR-09").classes("ControladorDePosVenda", "ServicoDePosVenda", "Reclamacao",
                "IPagamento", "ServicoDePagamentos", "TrioDePagamento"));

        rotas.add(Rota.get("/api/reclamacoes/{id}", r -> {
            ReclamacaoDTO dto = posVenda.reclamacao(r.exigirUsuario(), r.parametro("id"));
            return Resposta.ok(reclamacao(dto)).mecanismo("Reclamação " + dto.id + " " + dto.status + ", com "
                    + dto.linhaDoTempo.size() + " passo(s) na linha do tempo");
        }).uc("UC18").tr("TR-09").classes("ControladorDePosVenda", "ServicoDePosVenda", "Reclamacao"));

        return rotas;
    }

    // -------------------------------------------------------------- mecanismo

    /** A politica de estorno vem do EstornoDTO da Transacao: e a da familia de pagamento usada na compra. */
    private static String mecanismoDaMediacao(ReclamacaoDTO dto) {
        if (dto.estorno != null) {
            return "Atendimento decidiu PROCEDENTE. IPagamento.estornar aplicou a política " + dto.estorno.politica
                    + ", da mesma família de pagamento da compra (Abstract Factory): " + dto.estorno.descricao
                    + ". Devolução em até " + dto.estorno.prazoEmDias + " dia(s). Reclamação REEMBOLSADA";
        }
        if ("RESOLVIDA".equals(dto.status)) {
            return "Atendimento decidiu IMPROCEDENTE: reclamação RESOLVIDA, venda mantida e nenhum estorno";
        }
        return "Atendimento assumiu a disputa: reclamação " + dto.status;
    }

    // ------------------------------------------------------------------ saida

    private static Map<String, Object> posVenda(PosVendaDTO dto) {
        List<Object> itens = new ArrayList<Object>();
        for (PosVendaDTO.Item item : dto.itens) {
            Map<String, Object> i = new LinkedHashMap<String, Object>();
            i.put("anuncioId", item.anuncioId);
            i.put("titulo", item.titulo);
            i.put("vendedorId", item.vendedorId);
            i.put("vendedorNome", item.vendedorNome);
            i.put("quantidade", item.quantidade);
            i.put("avaliacao", item.avaliacao == null ? null : avaliacao(item.avaliacao));
            itens.add(i);
        }
        List<Object> reclamacoes = new ArrayList<Object>();
        for (ReclamacaoDTO reclamacao : dto.reclamacoes) {
            reclamacoes.add(reclamacao(reclamacao));
        }
        Map<String, Object> corpo = new LinkedHashMap<String, Object>();
        corpo.put("pedidoId", dto.pedidoId);
        corpo.put("compradorId", dto.compradorId);
        corpo.put("status", dto.status);
        corpo.put("statusRotulo", dto.statusRotulo);
        corpo.put("papel", dto.papel);
        corpo.put("podeAvaliar", dto.podeAvaliar);
        corpo.put("podeReclamar", dto.podeReclamar);
        corpo.put("itens", itens);
        corpo.put("reclamacoes", reclamacoes);
        return corpo;
    }

    private static Map<String, Object> avaliacao(AvaliacaoDTO dto) {
        Map<String, Object> a = new LinkedHashMap<String, Object>();
        a.put("id", dto.id);
        a.put("pedidoId", dto.pedidoId);
        a.put("anuncioId", dto.anuncioId);
        a.put("titulo", dto.titulo);
        a.put("vendedorId", dto.vendedorId);
        a.put("vendedorNome", dto.vendedorNome);
        a.put("nota", dto.nota);
        a.put("comentario", dto.comentario);
        if (dto.reputacaoDepois != null) {
            a.put("reputacaoAntes", dto.reputacaoAntes);
            a.put("reputacaoDepois", dto.reputacaoDepois);
        }
        return a;
    }

    private static Map<String, Object> reclamacao(ReclamacaoDTO dto) {
        Map<String, Object> r = new LinkedHashMap<String, Object>();
        r.put("id", dto.id);
        r.put("pedidoId", dto.pedidoId);
        r.put("compradorId", dto.compradorId);
        r.put("vendedorId", dto.vendedorId);
        r.put("vendedorNome", dto.vendedorNome);
        r.put("anuncioId", dto.anuncioId);
        r.put("motivo", dto.motivo);
        r.put("status", dto.status);
        r.put("statusRotulo", dto.statusRotulo);
        r.put("resposta", dto.resposta);
        r.put("decisao", dto.decisao);
        r.put("parecer", dto.parecer);
        if (dto.estorno == null) {
            r.put("estorno", null);
        } else {
            Map<String, Object> e = new LinkedHashMap<String, Object>();
            e.put("valor", dto.estorno.valor.formatado());
            e.put("valorCentavos", dto.estorno.valor.centavos());
            e.put("descricao", dto.estorno.descricao);
            e.put("politica", dto.estorno.politica);
            e.put("prazoEmDias", dto.estorno.prazoEmDias);
            r.put("estorno", e);
        }
        r.put("linhaDoTempo", dto.linhaDoTempo);
        return r;
    }
}
