package comercio.transacao.controle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import comercio.infra.http.ModuloDeRotas;
import comercio.infra.http.Resposta;
import comercio.infra.http.Rota;
import comercio.transacao.aplicacao.ICheckout;
import comercio.transacao.aplicacao.IPagamento;
import comercio.transacao.aplicacao.IRastreio;
import comercio.transacao.aplicacao.MeioDTO;
import comercio.transacao.aplicacao.PedidoDTO;

/**
 * Rotas do pedido: criar (UC10, UC11), pagar (UC12, UC24, UC25), desistir e
 * acompanhar (UC13, UC26, UC27).
 *
 * O controlador so traduz HTTP. Quem decide e a Aplicacao: o checkout reserva
 * estoque tudo ou nada, o pagamento escolhe a familia pela Abstract Factory e
 * o pedido muda de estado pelo State. A resposta conta, no campo
 * "arquitetura", qual desses mecanismos trabalhou e com quais classes.
 *
 * As rotas /avancar e /compensar fazem o papel de sistemas que nao existem
 * nesta entrega (a transportadora e o banco que compensa boleto e Pix). Elas
 * existem para a demonstracao e estao marcadas como simulacao.
 */
public final class ControladorDePedidos implements ModuloDeRotas {

    private final ICheckout checkout;
    private final IPagamento pagamentos;
    private final IRastreio rastreio;

    public ControladorDePedidos(ICheckout checkout, IPagamento pagamentos, IRastreio rastreio) {
        this.checkout = checkout;
        this.pagamentos = pagamentos;
        this.rastreio = rastreio;
    }

    @Override
    public List<Rota> rotas() {
        List<Rota> rotas = new ArrayList<Rota>();

        rotas.add(Rota.get("/api/meios-de-pagamento", r -> {
            List<Object> meios = new ArrayList<Object>();
            List<String> familias = new ArrayList<String>();
            for (MeioDTO meio : pagamentos.meios()) {
                meios.add(Representacao.meio(meio));
                familias.add(meio.familia);
            }
            Map<String, Object> corpo = new LinkedHashMap<String, Object>();
            corpo.put("meios", meios);
            return Resposta.ok(corpo).mecanismo("Abstract Factory: cada meio é uma família registrada na raiz de "
                    + "composição (" + Representacao.juntar(familias) + ")");
        }).uc("UC24").tr("TR-05").classes("ControladorDePedidos", "ServicoDePagamentos", "RegistroDeFabricas",
                "FabricaMeioPagamento"));

        rotas.add(Rota.post("/api/pedidos", r -> {
            String comprador = r.exigirUsuario();
            String endereco = r.obrigatorio("enderecoId");
            String anuncio = r.texto("anuncioId");
            PedidoDTO pedido;
            String mecanismo;
            if (anuncio == null) {
                pedido = checkout.finalizar(comprador, endereco);
                mecanismo = "Checkout tudo ou nada: o estoque dos itens selecionados foi reservado e o pedido "
                        + "nasceu em " + pedido.statusRotulo + " (State), com um envio por vendedor";
            } else {
                pedido = checkout.comprarAgora(comprador, anuncio, r.inteiro("quantidade", 1), endereco);
                mecanismo = "Comprar agora (RN-B08): o pedido foi criado direto do anúncio, sem passar pelo "
                        + "carrinho, e nasceu em " + pedido.statusRotulo + " (State)";
            }
            return Resposta.criado(Representacao.pedido(pedido)).mecanismo(mecanismo);
        }).uc("UC10", "UC11").tr("TR-05").classes("ControladorDePedidos", "ServicoDeCheckout", "Pedido",
                "EstadoAguardandoPagamento", "ServicoDeAnuncios", "IFrete"));

        rotas.add(Rota.get("/api/pedidos", r -> {
            List<Object> lista = new ArrayList<Object>();
            for (PedidoDTO pedido : rastreio.pedidosDe(r.exigirUsuario())) {
                lista.add(Representacao.pedido(pedido));
            }
            Map<String, Object> corpo = new LinkedHashMap<String, Object>();
            corpo.put("pedidos", lista);
            return Resposta.ok(corpo).mecanismo("Pedidos do comprador da sessão, do mais recente para o mais antigo");
        }).uc("UC13").tr("TR-05").classes("ControladorDePedidos", "ServicoDeEntrega", "RepositorioDePedidos"));

        rotas.add(Rota.get("/api/pedidos/{id}", r -> {
            PedidoDTO pedido = rastreio.pedidoDoComprador(r.exigirUsuario(), r.parametro("id"));
            return Resposta.ok(Representacao.pedido(pedido))
                    .mecanismo("State: o pedido está em " + pedido.statusRotulo + "; o histórico lista cada "
                            + "transição feita pelo objeto de estado");
        }).uc("UC13").tr("TR-05").classes("ControladorDePedidos", "ServicoDeEntrega", "Pedido", "EstadoPedido"));

        rotas.add(Rota.post("/api/pedidos/{id}/pagamento", r -> {
            PedidoDTO pedido = pagamentos.pagar(r.exigirUsuario(), r.parametro("id"), r.obrigatorio("meio"));
            return Resposta.ok(Representacao.pedido(pedido)).mecanismo(mecanismoDoPagamento(pedido));
        }).uc("UC12", "UC24", "UC25").tr("TR-05").classes("ControladorDePedidos", "ServicoDePagamentos",
                "RegistroDeFabricas", "TrioDePagamento", "Pagamento", "ObservadorDePagamento", "Pedido"));

        rotas.add(Rota.post("/api/pedidos/{id}/desistencia", r -> {
            PedidoDTO pedido = checkout.desistir(r.exigirUsuario(), r.parametro("id"));
            return Resposta.ok(Representacao.pedido(pedido))
                    .mecanismo("State: só um pedido aguardando pagamento aceita desistência; o estoque reservado "
                            + "voltou para os anúncios");
        }).uc("UC10").tr("TR-05").classes("ControladorDePedidos", "ServicoDeCheckout", "Pedido",
                "EstadoCancelado", "ServicoDeAnuncios"));

        rotas.add(Rota.post("/api/pedidos/{id}/compensar", r -> {
            String id = r.parametro("id");
            rastreio.pedidoDoComprador(r.exigirUsuario(), id);
            PedidoDTO pedido = pagamentos.confirmarCompensacao(id);
            return Resposta.ok(Representacao.pedido(pedido))
                    .mecanismo("Simulação do banco: a compensação chegou e o Pagamento avisou os observadores "
                            + "(Observer); o pedido foi para " + pedido.statusRotulo);
        }).uc("UC25").tr("TR-05").classes("ControladorDePedidos", "ServicoDePagamentos", "Pagamento",
                "ObservadorDePagamento"));

        rotas.add(Rota.post("/api/pedidos/{id}/avancar", r -> {
            String id = r.parametro("id");
            rastreio.pedidoDoComprador(r.exigirUsuario(), id);
            PedidoDTO pedido = rastreio.avancar(id);
            return Resposta.ok(Representacao.pedido(pedido))
                    .mecanismo("Simulação da transportadora: o estado atual decidiu o próximo passo (State) e o "
                            + "pedido foi para " + pedido.statusRotulo);
        }).uc("UC26", "UC27").tr("TR-05").classes("ControladorDePedidos", "ServicoDeEntrega", "Pedido",
                "EstadoPedido"));
        return rotas;
    }

    private static String mecanismoDoPagamento(PedidoDTO pedido) {
        PedidoDTO.Cobranca cobranca = pedido.cobranca;
        StringBuilder texto = new StringBuilder();
        texto.append("Abstract Factory: ").append(cobranca.familia).append(" entregou o trio ")
                .append(Representacao.juntar(cobranca.classesDoTrio)).append(". ");
        texto.append("Pagamento ").append(cobranca.statusRotulo.toLowerCase());
        if (!pedido.eventos.isEmpty()) {
            texto.append("; Observer avisou: ").append(Representacao.juntar(pedido.eventos));
        }
        texto.append(". Pedido em ").append(pedido.statusRotulo).append(" (State).");
        return texto.toString();
    }
}
