package comercio.transacao;

import java.util.ArrayList;
import java.util.List;

import comercio.Aplicacao;
import comercio.externos.RelogioSimulado;
import comercio.nucleo.Dinheiro;
import comercio.nucleo.RegraDeNegocioException;
import comercio.teste.Casos;
import comercio.teste.Suite;
import comercio.teste.Verifica;
import comercio.transacao.aplicacao.EstornoDTO;
import comercio.transacao.aplicacao.IPagamento;
import comercio.transacao.aplicacao.MeioDTO;
import comercio.transacao.aplicacao.PedidoDTO;
import comercio.transacao.aplicacao.RegistroDeFabricas;
import comercio.transacao.aplicacao.ServicoDePagamentos;
import comercio.transacao.aplicacao.observadores.LiberacaoPedido;
import comercio.transacao.dominio.pagamento.Autorizacao;
import comercio.transacao.dominio.pagamento.Autorizador;
import comercio.transacao.dominio.pagamento.Comprovante;
import comercio.transacao.dominio.pagamento.FabricaMeioPagamento;
import comercio.transacao.dominio.pagamento.MeioPagamento;
import comercio.transacao.dominio.pagamento.ObservadorDePagamento;
import comercio.transacao.dominio.pagamento.Pagamento;
import comercio.transacao.dominio.pagamento.PoliticaEstorno;
import comercio.transacao.dominio.pagamento.StatusPagamento;
import comercio.transacao.dominio.pedido.Envio;
import comercio.transacao.dominio.pedido.ItemPedido;
import comercio.transacao.dominio.pedido.Pedido;
import comercio.transacao.persistencia.RepositorioDePedidos;

public final class PagamentoTeste implements Suite {

    private static final String JOANA = "com-01";
    private static final String CASA = "end-01";

    private RelogioSimulado relogio;

    private Aplicacao loja() {
        relogio = new RelogioSimulado();
        return Aplicacao.montar(relogio);
    }

    private static boolean contemEvento(PedidoDTO pedido, String trecho) {
        for (String evento : pedido.eventos) {
            if (evento.contains(trecho)) {
                return true;
            }
        }
        return false;
    }

    /** Familia de teste: prova que o servico so depende das interfaces do Abstract Factory. */
    private static final class FabricaDeTeste implements FabricaMeioPagamento {
        @Override
        public MeioPagamento meio() {
            return MeioPagamento.PIX;
        }

        @Override
        public Autorizador criarAutorizador() {
            return new Autorizador() {
                @Override
                public Autorizacao autorizar(String pedido, Dinheiro valor) {
                    return Autorizacao.aprovada("TESTE-" + pedido, "familia de teste");
                }

                @Override
                public int prazoLimiteEmMinutos() {
                    return 5;
                }

                @Override
                public boolean ehAssincrono() {
                    return false;
                }
            };
        }

        @Override
        public Comprovante criarComprovante() {
            return new Comprovante() {
                @Override
                public String gerar(String pedido, Dinheiro valor, Autorizacao autorizacao) {
                    return "comprovante de teste";
                }

                @Override
                public String formato() {
                    return "teste";
                }
            };
        }

        @Override
        public PoliticaEstorno criarPoliticaEstorno() {
            return new PoliticaEstorno() {
                @Override
                public boolean podeEstornar(Autorizacao autorizacao) {
                    return true;
                }

                @Override
                public String estornar(String pedido, Dinheiro valor) {
                    return "estorno de teste";
                }

                @Override
                public int prazoDeDevolucaoEmDias() {
                    return 0;
                }
            };
        }
    }

    @Override
    public void registrar(Casos c) {
        c.caso("UC24: quatro meios, e o trio de cada um vem inteiro da mesma familia", () -> {
            List<MeioDTO> meios = loja().pagamentos().meios();
            Verifica.igual(4, meios.size());
            String[] sufixos = { "Cartao", "Pix", "Boleto", "Saldo" };
            for (int i = 0; i < meios.size(); i++) {
                MeioDTO meio = meios.get(i);
                Verifica.igual(3, meio.classesDoTrio.size());
                for (String classe : meio.classesDoTrio) {
                    Verifica.verdadeiro(classe.endsWith(sufixos[i]),
                            meio.rotulo + " recebeu " + classe + ", que e de outra familia");
                }
            }
            Verifica.verdadeiro(meios.get(2).assincrono, "boleto e a familia assincrona");
            Verifica.falso(meios.get(1).assincrono, "pix responde na hora");
        });

        c.caso("UC12: pagamento aprovado leva o pedido a PAGO e dispara os quatro observadores", () -> {
            Aplicacao loja = loja();
            PedidoDTO pedido = loja.checkout().comprarAgora(JOANA, "anu-004", 1, CASA);
            PedidoDTO pago = loja.pagamentos().pagar(JOANA, pedido.id, "CARTAO");

            Verifica.igual("PAGO", pago.status);
            Verifica.igual("APROVADO", pago.cobranca.status);
            Verifica.igual("[AutorizadorCartao, ComprovanteCartao, EstornoCartao]",
                    String.valueOf(pago.cobranca.classesDoTrio));
            Verifica.verdadeiro(pago.cobranca.comprovante != null, "pagamento aprovado tem comprovante");
            Verifica.verdadeiro(contemEvento(pago, "pedido liberado"), "LiberacaoPedido deve ter reagido");
            Verifica.verdadeiro(contemEvento(pago, "Comprador avisado"), "NotificacaoComprador deve ter reagido");
            Verifica.verdadeiro(contemEvento(pago, "Nota fiscal NF-" + pedido.id), "EmissaoNotaFiscal deve ter reagido");
            Verifica.falso(contemEvento(pago, "Estoque devolvido"), "LiberacaoEstoque nao reage a aprovacao");
            Verifica.igual("[AGUARDANDO_PAGAMENTO, AGUARDANDO_PAGAMENTO -> PAGO]", String.valueOf(pago.historico));
        });

        c.caso("ADR-05: um pedido pago nao e cobrado de novo", () -> {
            Aplicacao loja = loja();
            PedidoDTO pedido = loja.checkout().comprarAgora(JOANA, "anu-004", 1, CASA);
            loja.pagamentos().pagar(JOANA, pedido.id, "PIX");
            RegraDeNegocioException recusa = Verifica.lanca(RegraDeNegocioException.class,
                    () -> loja.pagamentos().pagar(JOANA, pedido.id, "CARTAO"));
            Verifica.igual("ADR-05", recusa.codigo());
            PedidoDTO depois = loja.rastreio().pedido(pedido.id);
            Verifica.igual(1, depois.tentativasDePagamento);
            Verifica.igual("PIX", depois.cobranca.meio);
        });

        c.caso("UC25: cartao recusado mantem o pedido aguardando; o comprador paga por outro meio", () -> {
            Aplicacao loja = loja();
            PedidoDTO pedido = loja.checkout().comprarAgora(JOANA, "anu-026", 1, CASA);   // acima do limite do cartao
            PedidoDTO recusado = loja.pagamentos().pagar(JOANA, pedido.id, "CARTAO");
            Verifica.igual("AGUARDANDO_PAGAMENTO", recusado.status);
            Verifica.igual("RECUSADO", recusado.cobranca.status);
            Verifica.igual(null, recusado.cobranca.comprovante);
            Verifica.igual(2, loja.anuncios().obter("anu-026").estoque());   // a reserva continua

            PedidoDTO pago = loja.pagamentos().pagar(JOANA, pedido.id, "PIX");
            Verifica.igual("PAGO", pago.status);
            Verifica.igual(2, pago.tentativasDePagamento);
            Verifica.igual("[AutorizadorPix, ComprovantePix, EstornoPix]", String.valueOf(pago.cobranca.classesDoTrio));
        });

        c.caso("UC25: saldo em conta insuficiente e recusado", () -> {
            Aplicacao loja = loja();
            PedidoDTO pedido = loja.checkout().comprarAgora(JOANA, "anu-008", 2, CASA);   // acima do saldo de R$ 500
            PedidoDTO recusado = loja.pagamentos().pagar(JOANA, pedido.id, "SALDO_EM_CONTA");
            Verifica.igual("RECUSADO", recusado.cobranca.status);
            Verifica.contem(recusado.cobranca.detalhe, "Saldo insuficiente");
        });

        c.caso("UC25: boleto e assincrono; o pedido so e pago quando a compensacao chega", () -> {
            Aplicacao loja = loja();
            PedidoDTO pedido = loja.checkout().comprarAgora(JOANA, "anu-004", 1, CASA);
            PedidoDTO emitido = loja.pagamentos().pagar(JOANA, pedido.id, "BOLETO");
            Verifica.igual("AGUARDANDO_PAGAMENTO", emitido.status);
            Verifica.igual("EM_ANALISE", emitido.cobranca.status);
            Verifica.verdadeiro(emitido.cobranca.comprovante != null, "o boleto emitido ja tem linha digitavel");
            Verifica.falso(contemEvento(emitido, "Nota fiscal"), "sem compensacao nao ha nota");

            RegraDeNegocioException emAnalise = Verifica.lanca(RegraDeNegocioException.class,
                    () -> loja.pagamentos().pagar(JOANA, pedido.id, "PIX"));
            Verifica.igual("UC25", emAnalise.codigo());

            PedidoDTO compensado = loja.pagamentos().confirmarCompensacao(pedido.id);
            Verifica.igual("PAGO", compensado.status);
            Verifica.verdadeiro(contemEvento(compensado, "Nota fiscal"), "a nota sai quando o boleto compensa");
            Verifica.lanca(RegraDeNegocioException.class, () -> loja.pagamentos().confirmarCompensacao(pedido.id));
        });

        c.caso("a janela de pagamento e da familia: 16 minutos expiram o cartao, mas nao o boleto", () -> {
            Aplicacao loja = loja();
            PedidoDTO noCartao = loja.checkout().comprarAgora(JOANA, "anu-001", 2, CASA);
            PedidoDTO noBoleto = loja.checkout().comprarAgora(JOANA, "anu-004", 1, CASA);
            Verifica.igual(10, loja.anuncios().obter("anu-001").estoque());
            relogio.avancar(16);

            PedidoDTO expirado = loja.pagamentos().pagar(JOANA, noCartao.id, "CARTAO");
            Verifica.igual("EXPIRADO", expirado.cobranca.status);
            Verifica.igual("CANCELADO", expirado.status);
            Verifica.igual(12, loja.anuncios().obter("anu-001").estoque());
            Verifica.verdadeiro(contemEvento(expirado, "Estoque devolvido: 2 unidade(s)"), "LiberacaoEstoque deve ter reagido");

            Verifica.igual("EM_ANALISE", loja.pagamentos().pagar(JOANA, noBoleto.id, "BOLETO").cobranca.status);
        });

        c.caso("UC21: o estorno usa a politica da mesma familia que autorizou a compra", () -> {
            Aplicacao loja = loja();
            PedidoDTO pedido = loja.checkout().comprarAgora(JOANA, "anu-001", 3, CASA);
            loja.pagamentos().pagar(JOANA, pedido.id, "PIX");
            EstornoDTO estorno = loja.pagamentos().estornar(pedido.id, "produto com defeito");

            Verifica.igual("EstornoPix", estorno.politica);
            Verifica.igual(pedido.total, estorno.valor);
            Verifica.contem(estorno.descricao, "produto com defeito");
            PedidoDTO depois = loja.rastreio().pedido(pedido.id);
            Verifica.igual("ESTORNADO", depois.cobranca.status);
            Verifica.igual("CANCELADO", depois.status);
            Verifica.igual(12, loja.anuncios().obter("anu-001").estoque());
        });

        c.caso("UC21: nao ha estorno em dobro nem estorno de pagamento que nao foi aprovado", () -> {
            Aplicacao loja = loja();
            PedidoDTO naoPago = loja.checkout().comprarAgora(JOANA, "anu-004", 1, CASA);
            Verifica.igual("UC21", Verifica.lanca(RegraDeNegocioException.class,
                    () -> loja.pagamentos().estornar(naoPago.id, null)).codigo());

            loja.pagamentos().pagar(JOANA, naoPago.id, "CARTAO");
            loja.pagamentos().estornar(naoPago.id, null);
            Verifica.igual("UC21", Verifica.lanca(RegraDeNegocioException.class,
                    () -> loja.pagamentos().estornar(naoPago.id, null)).codigo());
            Verifica.igual(40, loja.anuncios().obter("anu-004").estoque());   // devolvido uma vez so
        });

        c.caso("UC24: meio de pagamento desconhecido e recusado sem cobrar", () -> {
            Aplicacao loja = loja();
            PedidoDTO pedido = loja.checkout().comprarAgora(JOANA, "anu-004", 1, CASA);
            Verifica.igual("UC24", Verifica.lanca(RegraDeNegocioException.class,
                    () -> loja.pagamentos().pagar(JOANA, pedido.id, "CHEQUE")).codigo());
            Verifica.igual(0, loja.rastreio().pedido(pedido.id).tentativasDePagamento);
        });

        c.caso("ADR-02: o servico funciona com uma familia que ele nunca viu, sem mudar uma linha", () -> {
            RepositorioDePedidos pedidos = new RepositorioDePedidos();
            Pedido pedido = pedidoAvulso(pedidos);
            List<ObservadorDePagamento> observadores = new ArrayList<ObservadorDePagamento>();
            observadores.add(new LiberacaoPedido(pedidos));
            IPagamento servico = new ServicoDePagamentos(pedidos, new RegistroDeFabricas().registrar(new FabricaDeTeste()),
                    new RelogioSimulado(), observadores);

            PedidoDTO pago = servico.pagar(JOANA, pedido.getId(), "PIX");
            Verifica.igual("PAGO", pago.status);
            Verifica.igual("TESTE-" + pedido.getId(), pago.cobranca.identificador);
            Verifica.igual("comprovante de teste", pago.cobranca.comprovante);
            Verifica.igual(1, servico.meios().size());
        });

        c.caso("Observer: a falha de um observador nao impede os demais de reagir", () -> {
            RepositorioDePedidos pedidos = new RepositorioDePedidos();
            Pedido pedido = pedidoAvulso(pedidos);
            List<ObservadorDePagamento> observadores = new ArrayList<ObservadorDePagamento>();
            observadores.add(new ObservadorDePagamento() {
                @Override
                public void atualizar(Pagamento pagamento) {
                    throw new IllegalStateException("servico de e-mail fora do ar");
                }
            });
            observadores.add(new LiberacaoPedido(pedidos));
            IPagamento servico = new ServicoDePagamentos(pedidos, new RegistroDeFabricas().registrar(new FabricaDeTeste()),
                    new RelogioSimulado(), observadores);

            Verifica.igual("PAGO", servico.pagar(JOANA, pedido.getId(), "PIX").status);
            Pagamento pagamento = pedidos.pagamentoAtual(pedido.getId());
            Verifica.igual(1, pagamento.falhasDeNotificacao().size());
            Verifica.contem(pagamento.falhasDeNotificacao().get(0), "servico de e-mail fora do ar");
        });

        c.caso("Observer: atribuir a mesma situacao de novo nao gera aviso", () -> {
            final int[] avisos = { 0 };
            Pagamento pagamento = new Pagamento("ped-x", Dinheiro.emReais(100), MeioPagamento.PIX);
            pagamento.adicionarObservador(observado -> avisos[0]++);
            pagamento.setStatus(StatusPagamento.APROVADO);
            pagamento.setStatus(StatusPagamento.APROVADO);
            Verifica.igual(1, avisos[0]);
        });
    }

    private static Pedido pedidoAvulso(RepositorioDePedidos pedidos) {
        Aplicacao loja = Aplicacao.montar(new RelogioSimulado());
        List<ItemPedido> itens = new ArrayList<ItemPedido>();
        itens.add(new ItemPedido("anu-004", "Fone de ouvido com fio P2", "ven-01", 1, Dinheiro.emReais(4990)));
        List<Envio> envios = new ArrayList<Envio>();
        envios.add(new Envio("ven-01", "AudioStore", loja.contas().comprador(JOANA).endereco(CASA), itens,
                Dinheiro.emReais(1490), 3));
        Pedido pedido = new Pedido(pedidos.proximoId(), JOANA, envios, 0);
        pedidos.salvar(pedido);
        return pedido;
    }
}
