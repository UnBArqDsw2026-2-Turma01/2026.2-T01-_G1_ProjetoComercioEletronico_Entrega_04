package comercio.posvenda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import comercio.contas.aplicacao.RegistroDeReputacao;
import comercio.contas.persistencia.RepositorioDeContas;
import comercio.contas.persistencia.SementeDeContas;
import comercio.nucleo.AcessoNegadoException;
import comercio.nucleo.Dinheiro;
import comercio.nucleo.NaoEncontradoException;
import comercio.nucleo.RegraDeNegocioException;
import comercio.posvenda.aplicacao.AvaliacaoDTO;
import comercio.posvenda.aplicacao.PosVendaDTO;
import comercio.posvenda.aplicacao.ReclamacaoDTO;
import comercio.posvenda.aplicacao.ServicoDePosVenda;
import comercio.posvenda.persistencia.RepositorioDePosVenda;
import comercio.teste.Casos;
import comercio.teste.Suite;
import comercio.teste.Verifica;
import comercio.transacao.aplicacao.EstornoDTO;
import comercio.transacao.aplicacao.IPagamento;
import comercio.transacao.aplicacao.IRastreio;
import comercio.transacao.aplicacao.MeioDTO;
import comercio.transacao.aplicacao.PedidoDTO;

/**
 * O Servico de Pos-venda com a Transacao substituida por implementacoes
 * falsas: um caso feliz e um de recusa para cada caso de uso, UC17 a UC21.
 */
public final class ServicoDePosVendaTeste implements Suite {

    static final String COMPRADORA = SementeDeContas.COMPRADORA;          // com-01
    static final String OUTRO_COMPRADOR = SementeDeContas.COMPRADOR_SEM_ENDERECO;
    static final String ATENDENTE = SementeDeContas.ATENDENTE;            // ate-01

    /** Rastreio falso: pedidos montados a mao, com a situacao que o caso precisa. */
    static final class RastreioFalso implements IRastreio {
        final Map<String, PedidoDTO> pedidos = new HashMap<String, PedidoDTO>();

        void cadastrar(String id, String status, String... itens) {
            Map<String, List<PedidoDTO.Item>> porVendedor = new HashMap<String, List<PedidoDTO.Item>>();
            List<String> ordem = new ArrayList<String>();
            for (String item : itens) {
                String[] partes = item.split(":");        // anuncio:vendedor
                if (!porVendedor.containsKey(partes[1])) {
                    porVendedor.put(partes[1], new ArrayList<PedidoDTO.Item>());
                    ordem.add(partes[1]);
                }
                porVendedor.get(partes[1]).add(new PedidoDTO.Item(partes[0], "Item " + partes[0], partes[1], 1,
                        Dinheiro.emReais(10000), Dinheiro.emReais(10000)));
            }
            List<PedidoDTO.Envio> envios = new ArrayList<PedidoDTO.Envio>();
            for (String vendedor : ordem) {
                envios.add(new PedidoDTO.Envio(vendedor, "Vendedor " + vendedor, "Casa", Dinheiro.emReais(1490), 3,
                        porVendedor.get(vendedor)));
            }
            pedidos.put(id, new PedidoDTO(id, COMPRADORA, status, status.toLowerCase(), envios,
                    Dinheiro.emReais(10000), Dinheiro.emReais(1490), Dinheiro.emReais(11490),
                    new ArrayList<String>(), new ArrayList<String>(), null, 1));
        }

        @Override
        public PedidoDTO pedido(String pedidoId) {
            PedidoDTO pedido = pedidos.get(pedidoId);
            if (pedido == null) {
                throw new NaoEncontradoException("O pedido " + pedidoId + " não existe");
            }
            return pedido;
        }

        @Override
        public PedidoDTO pedidoDoComprador(String compradorId, String pedidoId) {
            return pedido(pedidoId);
        }

        @Override
        public List<PedidoDTO> pedidosDe(String compradorId) {
            return new ArrayList<PedidoDTO>(pedidos.values());
        }

        @Override
        public PedidoDTO avancar(String pedidoId) {
            throw new UnsupportedOperationException();
        }
    }

    /** Pagamento falso: conta os estornos e recusa o segundo do mesmo pedido, como a Transacao real. */
    static final class PagamentoFalso implements IPagamento {
        final List<String> estornados = new ArrayList<String>();
        boolean recusar;

        @Override
        public EstornoDTO estornar(String pedidoId, String motivo) {
            if (recusar || estornados.contains(pedidoId)) {
                throw new RegraDeNegocioException("UC21", "Estorno recusado pela operadora simulada");
            }
            estornados.add(pedidoId);
            return new EstornoDTO(pedidoId, Dinheiro.emReais(11490), "CARTAO",
                    "Estorno de R$ 114,90 na fatura do cartão", 7, "EstornoCartao");
        }

        @Override
        public List<MeioDTO> meios() {
            return new ArrayList<MeioDTO>();
        }

        @Override
        public PedidoDTO pagar(String compradorId, String pedidoId, String meio) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PedidoDTO confirmarCompensacao(String pedidoId) {
            throw new UnsupportedOperationException();
        }
    }

    RepositorioDeContas contas;
    RastreioFalso rastreio;
    PagamentoFalso pagamento;

    ServicoDePosVenda servico() {
        contas = new RepositorioDeContas();
        SementeDeContas.popular(contas);
        rastreio = new RastreioFalso();
        rastreio.cadastrar("ped-entregue", "ENTREGUE", "anu-002:ven-02");
        rastreio.cadastrar("ped-enviado", "ENVIADO", "anu-005:ven-04");
        rastreio.cadastrar("ped-pago", "PAGO", "anu-001:ven-01");
        rastreio.cadastrar("ped-dois", "ENTREGUE", "anu-001:ven-01", "anu-012:ven-02");
        pagamento = new PagamentoFalso();
        return new ServicoDePosVenda(new RepositorioDePosVenda(), rastreio, pagamento,
                new RegistroDeReputacao(contas), contas);
    }

    @Override
    public void registrar(Casos c) {
        // ------------------------------------------------------------ UC17

        c.caso("UC17 e RN-A05: a reputacao do vendedor muda depois da avaliacao", () -> {
            ServicoDePosVenda servico = servico();
            int antes = contas.vendedor("ven-02").reputacao().avaliacoes();
            AvaliacaoDTO avaliacao = servico.avaliar(COMPRADORA, "ped-entregue", "anu-002", 5, "Chegou certinho");
            Verifica.igual(antes + 1, contas.vendedor("ven-02").reputacao().avaliacoes());
            Verifica.igual("ava-001", avaliacao.id);
            Verifica.igual("ven-02", avaliacao.vendedorId);
            Verifica.falso(avaliacao.reputacaoAntes.equals(avaliacao.reputacaoDepois),
                    "a reputacao exibida deveria mudar: " + avaliacao.reputacaoAntes);
        });

        c.caso("UC17: pedido com um item so dispensa informar o anuncio", () -> {
            AvaliacaoDTO avaliacao = servico().avaliar(COMPRADORA, "ped-entregue", null, 4, "");
            Verifica.igual("anu-002", avaliacao.anuncioId);
        });

        c.caso("UC17: pedido que ainda nao foi entregue nao pode ser avaliado", () -> {
            ServicoDePosVenda servico = servico();
            Verifica.igual("UC17", Verifica.lanca(RegraDeNegocioException.class,
                    () -> servico.avaliar(COMPRADORA, "ped-enviado", "anu-005", 5, "")).codigo());
        });

        c.caso("UC17: so o comprador do pedido avalia", () -> {
            ServicoDePosVenda servico = servico();
            Verifica.lanca(AcessoNegadoException.class,
                    () -> servico.avaliar(OUTRO_COMPRADOR, "ped-entregue", "anu-002", 5, ""));
        });

        c.caso("RN-A05: nota fora de 1 a 5 e recusada e a reputacao nao muda", () -> {
            ServicoDePosVenda servico = servico();
            int antes = contas.vendedor("ven-02").reputacao().avaliacoes();
            Verifica.igual("RN-A05", Verifica.lanca(RegraDeNegocioException.class,
                    () -> servico.avaliar(COMPRADORA, "ped-entregue", "anu-002", 6, "")).codigo());
            Verifica.igual(antes, contas.vendedor("ven-02").reputacao().avaliacoes());
        });

        c.caso("UC17: uma avaliacao por item do pedido", () -> {
            ServicoDePosVenda servico = servico();
            servico.avaliar(COMPRADORA, "ped-dois", "anu-001", 5, "");
            servico.avaliar(COMPRADORA, "ped-dois", "anu-012", 3, "");
            Verifica.igual("UC17", Verifica.lanca(RegraDeNegocioException.class,
                    () -> servico.avaliar(COMPRADORA, "ped-dois", "anu-001", 1, "")).codigo());
            Verifica.igual("UC17", Verifica.lanca(RegraDeNegocioException.class,
                    () -> servico.avaliar(COMPRADORA, "ped-dois", "anu-999", 1, "")).codigo());
        });

        // ------------------------------------------------------------ UC18

        c.caso("UC18: o comprador abre reclamacao sobre pedido enviado; ela nasce ABERTA para o vendedor do envio", () -> {
            ReclamacaoDTO reclamacao = servico().abrir(COMPRADORA, "ped-enviado", null, "Caixa chegou amassada");
            Verifica.igual("rec-001", reclamacao.id);
            Verifica.igual("ABERTA", reclamacao.status);
            Verifica.igual("ven-04", reclamacao.vendedorId);
            Verifica.igual(1, reclamacao.linhaDoTempo.size());
        });

        c.caso("UC18: pedido que nao foi enviado nao recebe reclamacao", () -> {
            ServicoDePosVenda servico = servico();
            Verifica.igual("UC18", Verifica.lanca(RegraDeNegocioException.class,
                    () -> servico.abrir(COMPRADORA, "ped-pago", null, "Demorou")).codigo());
        });

        c.caso("UC18: uma reclamacao em andamento por pedido; so o comprador abre; motivo obrigatorio", () -> {
            ServicoDePosVenda servico = servico();
            servico.abrir(COMPRADORA, "ped-enviado", null, "Caixa amassada");
            Verifica.igual("UC18", Verifica.lanca(RegraDeNegocioException.class,
                    () -> servico.abrir(COMPRADORA, "ped-enviado", null, "De novo")).codigo());
            Verifica.lanca(AcessoNegadoException.class,
                    () -> servico.abrir(OUTRO_COMPRADOR, "ped-entregue", null, "Nao e meu"));
            Verifica.igual("UC18", Verifica.lanca(RegraDeNegocioException.class,
                    () -> servico.abrir(COMPRADORA, "ped-entregue", null, "  ")).codigo());
        });

        c.caso("UC18: pedido com dois vendedores exige o item, e a reclamacao vai para o vendedor dele", () -> {
            ServicoDePosVenda servico = servico();
            Verifica.igual("UC18", Verifica.lanca(RegraDeNegocioException.class,
                    () -> servico.abrir(COMPRADORA, "ped-dois", null, "Veio errado")).codigo());
            Verifica.igual("ven-02", servico.abrir(COMPRADORA, "ped-dois", "anu-012", "Veio errado").vendedorId);
        });

        // ------------------------------------------------------------ UC19

        c.caso("UC19: o vendedor do envio responde e a reclamacao fica RESPONDIDA", () -> {
            ServicoDePosVenda servico = servico();
            String id = servico.abrir(COMPRADORA, "ped-enviado", null, "Caixa amassada").id;
            ReclamacaoDTO respondida = servico.responder("ven-04", id, "Enviamos outra caixa");
            Verifica.igual("RESPONDIDA", respondida.status);
            Verifica.igual("Enviamos outra caixa", respondida.resposta);
        });

        c.caso("UC19: outro vendedor nao responde, e a mesma reclamacao nao e respondida duas vezes", () -> {
            ServicoDePosVenda servico = servico();
            String id = servico.abrir(COMPRADORA, "ped-enviado", null, "Caixa amassada").id;
            Verifica.lanca(AcessoNegadoException.class, () -> servico.responder("ven-01", id, "Nao sou eu"));
            servico.responder("ven-04", id, "Enviamos outra caixa");
            Verifica.igual("UC19", Verifica.lanca(RegraDeNegocioException.class,
                    () -> servico.responder("ven-04", id, "De novo")).codigo());
        });

        // ------------------------------------------------------------ UC20

        c.caso("UC20: o atendimento assume a mediacao", () -> {
            ServicoDePosVenda servico = servico();
            String id = servico.abrir(COMPRADORA, "ped-enviado", null, "Caixa amassada").id;
            Verifica.igual("EM_MEDIACAO", servico.mediar(ATENDENTE, id, null, null).status);
        });

        c.caso("UC20: so o atendimento media, e a decisao precisa ser PROCEDENTE ou IMPROCEDENTE", () -> {
            ServicoDePosVenda servico = servico();
            String id = servico.abrir(COMPRADORA, "ped-enviado", null, "Caixa amassada").id;
            Verifica.lanca(AcessoNegadoException.class, () -> servico.mediar(COMPRADORA, id, "PROCEDENTE", ""));
            Verifica.lanca(AcessoNegadoException.class, () -> servico.mediar("ven-04", id, "IMPROCEDENTE", ""));
            Verifica.igual("UC20", Verifica.lanca(RegraDeNegocioException.class,
                    () -> servico.mediar(ATENDENTE, id, "TALVEZ", "")).codigo());
            Verifica.igual(0, pagamento.estornados.size());
        });

        // ------------------------------------------------------------ UC21

        c.caso("UC21: PROCEDENTE estorna pelo IPagamento e encerra como REEMBOLSADA", () -> {
            ServicoDePosVenda servico = servico();
            String id = servico.abrir(COMPRADORA, "ped-enviado", null, "Nao funciona").id;
            servico.responder("ven-04", id, "Funciona sim");
            ReclamacaoDTO decidida = servico.mediar(ATENDENTE, id, "PROCEDENTE", "Defeito comprovado por foto");
            Verifica.igual("REEMBOLSADA", decidida.status);
            Verifica.igual("PROCEDENTE", decidida.decisao);
            Verifica.igual(Arrays.asList("ped-enviado"), pagamento.estornados);
            Verifica.igual("EstornoCartao", decidida.estorno.politica);
            Verifica.igual(Dinheiro.emReais(11490), decidida.estorno.valor);
            Verifica.contem(decidida.linhaDoTempo.get(decidida.linhaDoTempo.size() - 1), "Estorno efetivado");
        });

        c.caso("UC20: IMPROCEDENTE encerra como RESOLVIDA, sem estorno", () -> {
            ServicoDePosVenda servico = servico();
            String id = servico.abrir(COMPRADORA, "ped-entregue", null, "Nao gostei da cor").id;
            ReclamacaoDTO decidida = servico.mediar(ATENDENTE, id, "IMPROCEDENTE", "A cor confere com o anuncio");
            Verifica.igual("RESOLVIDA", decidida.status);
            Verifica.igual(null, decidida.estorno);
            Verifica.igual(0, pagamento.estornados.size());
        });

        c.caso("UC21: reembolsar duas vezes a mesma reclamacao e recusado", () -> {
            ServicoDePosVenda servico = servico();
            String id = servico.abrir(COMPRADORA, "ped-enviado", null, "Nao funciona").id;
            servico.mediar(ATENDENTE, id, "PROCEDENTE", "");
            Verifica.igual("UC21", Verifica.lanca(RegraDeNegocioException.class,
                    () -> servico.mediar(ATENDENTE, id, "PROCEDENTE", "")).codigo());
            Verifica.igual(1, pagamento.estornados.size());
            Verifica.igual("UC18", Verifica.lanca(RegraDeNegocioException.class,
                    () -> servico.abrir(COMPRADORA, "ped-enviado", null, "Outra vez")).codigo());
        });

        c.caso("UC21: se o estorno e recusado, a reclamacao continua em mediacao", () -> {
            ServicoDePosVenda servico = servico();
            String id = servico.abrir(COMPRADORA, "ped-enviado", null, "Nao funciona").id;
            pagamento.recusar = true;
            Verifica.lanca(RegraDeNegocioException.class, () -> servico.mediar(ATENDENTE, id, "PROCEDENTE", ""));
            Verifica.igual("EM_MEDIACAO", servico.reclamacao(ATENDENTE, id).status);
        });

        // -------------------------------------------------------- consultas

        c.caso("UC18: a reclamacao e visivel ao comprador, ao vendedor e ao atendimento, e a mais ninguem", () -> {
            ServicoDePosVenda servico = servico();
            String id = servico.abrir(COMPRADORA, "ped-enviado", null, "Caixa amassada").id;
            Verifica.igual(id, servico.reclamacao(COMPRADORA, id).id);
            Verifica.igual(id, servico.reclamacao("ven-04", id).id);
            Verifica.igual(id, servico.reclamacao(ATENDENTE, id).id);
            Verifica.lanca(AcessoNegadoException.class, () -> servico.reclamacao("ven-01", id));
            Verifica.lanca(NaoEncontradoException.class, () -> servico.reclamacao(ATENDENTE, "rec-999"));
        });

        c.caso("UC17 e UC18: o pos-venda do pedido diz o que o comprador pode fazer agora", () -> {
            ServicoDePosVenda servico = servico();
            PosVendaDTO entregue = servico.posVenda(COMPRADORA, "ped-entregue");
            Verifica.verdadeiro(entregue.podeAvaliar, "pedido entregue pode ser avaliado");
            Verifica.verdadeiro(entregue.podeReclamar, "pedido entregue pode receber reclamacao");
            Verifica.igual("COMPRADOR", entregue.papel);
            servico.avaliar(COMPRADORA, "ped-entregue", null, 5, "");
            Verifica.igual(5, servico.posVenda(COMPRADORA, "ped-entregue").itens.get(0).avaliacao.nota);

            PosVendaDTO pago = servico.posVenda(COMPRADORA, "ped-pago");
            Verifica.falso(pago.podeAvaliar || pago.podeReclamar, "pedido so pago nao tem pos-venda");

            PosVendaDTO paraVendedor = servico.posVenda("ven-02", "ped-entregue");
            Verifica.igual("VENDEDOR", paraVendedor.papel);
            Verifica.falso(paraVendedor.podeAvaliar, "vendedor nao avalia");
            Verifica.lanca(AcessoNegadoException.class, () -> servico.posVenda("ven-05", "ped-entregue"));
        });
    }
}
