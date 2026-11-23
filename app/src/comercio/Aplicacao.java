package comercio;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import comercio.catalogo.aplicacao.IAnuncio;
import comercio.catalogo.aplicacao.ServicoDeAnuncios;
import comercio.catalogo.aplicacao.ServicoDeCatalogo;
import comercio.catalogo.controle.ControladorDeCatalogo;
import comercio.catalogo.dominio.IFrete;
import comercio.catalogo.persistencia.RepositorioDeAnuncios;
import comercio.catalogo.persistencia.SementeDoCatalogo;
import comercio.contas.aplicacao.IConta;
import comercio.contas.aplicacao.RegistroDeReputacao;
import comercio.contas.aplicacao.ServicoDeConta;
import comercio.contas.aplicacao.acesso.AuditoriaDeAcesso;
import comercio.contas.aplicacao.acesso.CodigosDeVerificacao;
import comercio.contas.aplicacao.acesso.DesafiosDeCaptcha;
import comercio.contas.aplicacao.acesso.EmissorDeTokens;
import comercio.contas.aplicacao.acesso.FachadaDeSessao;
import comercio.contas.aplicacao.acesso.IAcesso;
import comercio.contas.aplicacao.acesso.RegistroDeDispositivos;
import comercio.contas.aplicacao.acesso.ServicoDeAcesso;
import comercio.contas.controle.AutenticadorDeSessao;
import comercio.contas.controle.ControladorDeAcesso;
import comercio.contas.persistencia.RepositorioDeContas;
import comercio.contas.persistencia.RepositorioDeCredenciais;
import comercio.contas.persistencia.SementeDeContas;
import comercio.externos.RelogioDoSistema;
import comercio.externos.TransportadoraSimulada;
import comercio.externos.pagamento.FabricaBoleto;
import comercio.externos.pagamento.FabricaCartao;
import comercio.externos.pagamento.FabricaPix;
import comercio.externos.pagamento.FabricaSaldoEmConta;
import comercio.infra.http.RotasDeInfraestrutura;
import comercio.posvenda.aplicacao.IReclamacao;
import comercio.posvenda.aplicacao.ServicoDePosVenda;
import comercio.posvenda.controle.ControladorDePosVenda;
import comercio.posvenda.persistencia.RepositorioDePosVenda;
import comercio.infra.http.Roteador;
import comercio.infra.http.ServidorHttp;
import comercio.transacao.aplicacao.ICarrinho;
import comercio.transacao.aplicacao.ICheckout;
import comercio.transacao.aplicacao.IPagamento;
import comercio.transacao.aplicacao.IRastreio;
import comercio.transacao.aplicacao.RegistroDeFabricas;
import comercio.transacao.aplicacao.ServicoDeCarrinho;
import comercio.transacao.aplicacao.ServicoDeCheckout;
import comercio.transacao.aplicacao.ServicoDeEntrega;
import comercio.transacao.aplicacao.ServicoDePagamentos;
import comercio.transacao.aplicacao.observadores.EmissaoNotaFiscal;
import comercio.transacao.controle.ControladorDeCarrinho;
import comercio.transacao.controle.ControladorDePedidos;
import comercio.transacao.aplicacao.observadores.LiberacaoEstoque;
import comercio.transacao.aplicacao.observadores.LiberacaoPedido;
import comercio.transacao.aplicacao.observadores.NotificacaoComprador;
import comercio.transacao.dominio.ICotacaoEntrega;
import comercio.transacao.dominio.Relogio;
import comercio.transacao.dominio.pagamento.ObservadorDePagamento;
import comercio.transacao.persistencia.RepositorioDeCarrinhos;
import comercio.transacao.persistencia.RepositorioDePedidos;

/**
 * Raiz de composicao: o unico lugar que conhece todas as camadas e todos os
 * pacotes, porque e aqui que o sistema e montado.
 *
 * Cria os repositorios, popula a semente, instancia os servicos entregando a
 * cada um as portas de que precisa e registra os controladores no Roteador.
 * Nenhuma outra classe escolhe implementacao: todas recebem as suas
 * dependencias pelo construtor.
 *
 * E aqui, e so aqui, que aparecem os nomes das classes concretas de pagamento
 * e dos simuladores externos. Acrescentar um meio de pagamento e uma linha em
 * registrarFamilias.
 */
public final class Aplicacao {

    private final RepositorioDeContas contas = new RepositorioDeContas();
    private final RepositorioDeCredenciais credenciais = new RepositorioDeCredenciais();
    private final RepositorioDeAnuncios anuncios = new RepositorioDeAnuncios();
    private final RepositorioDeCarrinhos carrinhos = new RepositorioDeCarrinhos();
    private final RepositorioDePedidos pedidos = new RepositorioDePedidos();
    private final Roteador roteador = new Roteador();

    private final IAcesso acesso;
    private final IConta conta;
    private final IAnuncio servicoDeAnuncios;
    private final ServicoDeEntrega entrega;
    private final ICarrinho carrinho;
    private final ICheckout checkout;
    private final IPagamento pagamentos;
    private final ServicoDeCatalogo catalogo;
    private final IReclamacao posVenda;

    private Aplicacao(Relogio relogio) {
        SementeDeContas.popular(contas);
        SementeDeContas.popularCredenciais(contas, credenciais);
        SementeDoCatalogo.popular(anuncios, contas.vendedores());

        // Sistemas externos, atras das portas do dominio.
        ICotacaoEntrega transportadora = new TransportadoraSimulada();

        // Contas e Acesso: a cadeia de login e a fachada de sessao.
        RegistroDeDispositivos dispositivos = new RegistroDeDispositivos();
        FachadaDeSessao fachada = new FachadaDeSessao(new EmissorDeTokens(), dispositivos, new AuditoriaDeAcesso());
        acesso = new ServicoDeAcesso(contas, credenciais, new DesafiosDeCaptcha(), new CodigosDeVerificacao(),
                dispositivos, fachada);
        conta = new ServicoDeConta(contas);

        // Catalogo e Descoberta.
        servicoDeAnuncios = new ServicoDeAnuncios(anuncios);

        // Transacao.
        entrega = new ServicoDeEntrega(transportadora, pedidos);
        carrinho = new ServicoDeCarrinho(carrinhos, servicoDeAnuncios);
        checkout = new ServicoDeCheckout(carrinhos, pedidos, contas, servicoDeAnuncios, transportadora, relogio);
        pagamentos = new ServicoDePagamentos(pedidos, registrarFamilias(), relogio, observadoresDePagamento());

        // Catalogo e Descoberta: a busca monta a cadeia de decoradores por consulta e
        // recebe o frete da Transacao pela porta IFrete.
        catalogo = new ServicoDeCatalogo(anuncios, entrega);

        // Pos-venda: consome Transacao (rastreio e estorno) e Contas (reputacao).
        posVenda = new ServicoDePosVenda(new RepositorioDePosVenda(), entrega, pagamentos,
                new RegistroDeReputacao(contas), contas);

        // Controle: cada controlador recebe so as interfaces da Aplicacao.
        roteador.autenticarCom(new AutenticadorDeSessao(acesso))
                .registrar(new RotasDeInfraestrutura(roteador))
                .registrar(new ControladorDeAcesso(acesso, conta))
                .registrar(new ControladorDeCatalogo(catalogo, catalogo))
                .registrar(new ControladorDeCarrinho(carrinho))
                .registrar(new ControladorDePedidos(checkout, pagamentos, entrega))
                .registrar(new ControladorDePosVenda(posVenda));
    }

    /** As familias de pagamento oferecidas (Abstract Factory). */
    private static RegistroDeFabricas registrarFamilias() {
        return new RegistroDeFabricas()
                .registrar(new FabricaCartao())
                .registrar(new FabricaPix())
                .registrar(new FabricaBoleto())
                .registrar(new FabricaSaldoEmConta());
    }

    /**
     * Quem reage a cada mudanca de situacao de um pagamento (Observer), na
     * ordem de aviso. O pedido muda de estado antes de o comprador ser avisado.
     */
    private List<ObservadorDePagamento> observadoresDePagamento() {
        List<ObservadorDePagamento> observadores = new ArrayList<ObservadorDePagamento>();
        observadores.add(new LiberacaoPedido(pedidos));
        observadores.add(new LiberacaoEstoque(pedidos, servicoDeAnuncios));
        observadores.add(new NotificacaoComprador(pedidos));
        observadores.add(new EmissaoNotaFiscal(pedidos));
        return observadores;
    }

    /** Monta a aplicacao inteira em memoria, sem abrir porta de rede. */
    public static Aplicacao montar() {
        return new Aplicacao(new RelogioDoSistema());
    }

    /** Monta com o relogio informado. Usado nos testes e no roteiro, onde o tempo anda sob comando. */
    public static Aplicacao montar(Relogio relogio) {
        return new Aplicacao(relogio);
    }

    public Roteador roteador() {
        return roteador;
    }

    public RepositorioDeContas contas() {
        return contas;
    }

    public RepositorioDeAnuncios anuncios() {
        return anuncios;
    }

    public IAcesso acesso() {
        return acesso;
    }

    public IAnuncio servicoDeAnuncios() {
        return servicoDeAnuncios;
    }

    public IFrete frete() {
        return entrega;
    }

    public ICarrinho carrinho() {
        return carrinho;
    }

    public ICheckout checkout() {
        return checkout;
    }

    public IPagamento pagamentos() {
        return pagamentos;
    }

    public IRastreio rastreio() {
        return entrega;
    }

    public static void main(String[] args) throws Exception {
        int porta = Integer.parseInt(System.getProperty("comercio.porta", "8080"));
        File pastaWeb = new File(System.getProperty("comercio.web", "web"));
        if (!pastaWeb.isDirectory()) {
            System.err.println("Pasta de paginas nao encontrada: " + pastaWeb.getAbsolutePath());
            System.err.println("Execute a partir de app/ (make run).");
            System.exit(2);
        }

        Aplicacao aplicacao = montar();
        final ServidorHttp servidor = new ServidorHttp(aplicacao.roteador(), pastaWeb, porta);
        servidor.iniciar();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                servidor.parar();
            }
        }));

        System.out.println("G1_ProjetoComercioEletronico no ar");
        System.out.println("  loja ............: http://localhost:" + servidor.porta() + "/");
        System.out.println("  rastreabilidade .: http://localhost:" + servidor.porta() + "/api/rastreabilidade");
        System.out.println("  rotas ...........: " + aplicacao.roteador().rotas().size());
        System.out.println("Ctrl+C para encerrar.");
    }
}
