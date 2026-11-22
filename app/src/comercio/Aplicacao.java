package comercio;

import java.io.File;

import comercio.contas.persistencia.RepositorioDeContas;
import comercio.contas.persistencia.SementeDeContas;
import comercio.infra.http.RotasDeInfraestrutura;
import comercio.infra.http.Roteador;
import comercio.infra.http.ServidorHttp;

/**
 * Raiz de composicao: o unico lugar que conhece todas as camadas e todos os
 * pacotes, porque e aqui que o sistema e montado.
 *
 * Cria os repositorios, popula a semente, instancia os servicos entregando a
 * cada um as portas de que precisa e registra os controladores no Roteador.
 * Nenhuma outra classe escolhe implementacao: todas recebem as suas
 * dependencias pelo construtor.
 */
public final class Aplicacao {

    private final RepositorioDeContas contas = new RepositorioDeContas();
    private final Roteador roteador = new Roteador();

    private Aplicacao() {
        SementeDeContas.popular(contas);
        roteador.registrar(new RotasDeInfraestrutura(roteador));
    }

    /** Monta a aplicacao inteira em memoria, sem abrir porta de rede. Usado tambem nos testes. */
    public static Aplicacao montar() {
        return new Aplicacao();
    }

    public Roteador roteador() {
        return roteador;
    }

    public RepositorioDeContas contas() {
        return contas;
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
