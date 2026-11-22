package comercio.arquitetura;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import comercio.teste.Casos;
import comercio.teste.Suite;
import comercio.teste.Verifica;

/**
 * Verifica, sobre o codigo-fonte real, que nenhuma classe importa o que a
 * Visao Logica do DAS proibe. Se alguem violar uma regra, este teste falha e
 * diz qual arquivo, qual importacao e qual secao do DAS.
 */
public final class ArquiteturaTeste implements Suite {

    private static final Pattern PACOTE = Pattern.compile("^\\s*package\\s+([\\w.]+)\\s*;");
    private static final Pattern IMPORTACAO = Pattern.compile("^\\s*import\\s+(?:static\\s+)?([\\w.]+?)(?:\\.\\*)?\\s*;");

    private static File raizDoCodigo() {
        return new File(System.getProperty("comercio.src", "src"));
    }

    private static void coletar(File pasta, List<File> arquivos) {
        File[] filhos = pasta.listFiles();
        if (filhos == null) {
            return;
        }
        for (File filho : filhos) {
            if (filho.isDirectory()) {
                coletar(filho, arquivos);
            } else if (filho.getName().endsWith(".java")) {
                arquivos.add(filho);
            }
        }
    }

    private static void esperaViolacao(String origem, String importado, String trecho) {
        String violacao = RegrasDeArquitetura.violacao(origem, importado);
        Verifica.verdadeiro(violacao != null, origem + " -> " + importado + " deveria ser proibido");
        Verifica.contem(violacao, trecho);
    }

    private static void esperaPermitido(String origem, String importado) {
        Verifica.igual(null, RegrasDeArquitetura.violacao(origem, importado));
    }

    @Override
    public void registrar(Casos c) {
        c.caso("DAS 5.1 e 5.2.5: nenhuma importacao do codigo real viola as regras de dependencia", () -> {
            List<File> arquivos = new ArrayList<File>();
            coletar(raizDoCodigo(), arquivos);
            Verifica.verdadeiro(arquivos.size() > 20, "nao encontrei o codigo-fonte em " + raizDoCodigo());

            List<String> violacoes = new ArrayList<String>();
            int verificados = 0;
            int emEspera = 0;
            for (File arquivo : arquivos) {
                String pacote = null;
                for (String linha : Files.readAllLines(arquivo.toPath(), StandardCharsets.UTF_8)) {
                    Matcher p = PACOTE.matcher(linha);
                    if (p.find()) {
                        pacote = p.group(1);
                        continue;
                    }
                    Matcher i = IMPORTACAO.matcher(linha);
                    if (i.find() && pacote != null) {
                        String violacao = RegrasDeArquitetura.violacao(pacote, i.group(1));
                        if (violacao != null) {
                            violacoes.add(arquivo.getPath() + " importa " + i.group(1) + ": " + violacao);
                        }
                    }
                }
                Verifica.verdadeiro(pacote != null, arquivo.getPath() + " nao declara pacote");
                String estrutura = RegrasDeArquitetura.violacaoDeEstrutura(pacote);
                if (estrutura != null) {
                    violacoes.add(arquivo.getPath() + " esta em " + pacote + ": " + estrutura);
                }
                if (RegrasDeArquitetura.aguardandoIntegracao(pacote)) {
                    emEspera++;
                } else {
                    verificados++;
                }
            }
            System.out.println("         " + verificados + " arquivos verificados; " + emEspera
                    + " da Entrega 3 ainda na area de espera");
            Verifica.verdadeiro(violacoes.isEmpty(), violacoes.size() + " violacao(oes):\n         "
                    + String.join("\n         ", violacoes));
        });

        c.caso("DAS 5.2.5: os pacotes de dominio so dependem no sentido da figura", () -> {
            esperaPermitido("comercio.catalogo.dominio", "comercio.contas.dominio.CEP");
            esperaPermitido("comercio.transacao.aplicacao", "comercio.catalogo.dominio.Anuncio");
            esperaPermitido("comercio.posvenda.aplicacao", "comercio.transacao.aplicacao.IPagamento");
            esperaViolacao("comercio.catalogo.dominio", "comercio.transacao.dominio.Pedido", "DAS 5.2.5");
            esperaViolacao("comercio.contas.dominio", "comercio.catalogo.dominio.Anuncio", "DAS 5.2.5");
            esperaViolacao("comercio.transacao.aplicacao", "comercio.posvenda.aplicacao.IReclamacao", "DAS 5.2.5");
        });

        c.caso("DAS 5.1: o Dominio nao depende de nenhuma outra camada", () -> {
            esperaViolacao("comercio.catalogo.dominio", "comercio.catalogo.aplicacao.ICatalogo", "DAS 5.1");
            esperaViolacao("comercio.catalogo.dominio", "comercio.catalogo.persistencia.CatalogoEmMemoria", "DAS 5.1");
            esperaViolacao("comercio.catalogo.dominio.refinamento", "comercio.infra.http.Requisicao", "DAS 5.1");
        });

        c.caso("DAS 5.1: o Controle so conhece a Aplicacao", () -> {
            esperaPermitido("comercio.catalogo.controle", "comercio.catalogo.aplicacao.ICatalogo");
            esperaPermitido("comercio.catalogo.controle", "comercio.infra.http.Rota");
            esperaPermitido("comercio.catalogo.controle", "comercio.nucleo.Dinheiro");
            esperaViolacao("comercio.catalogo.controle", "comercio.catalogo.dominio.Anuncio", "DAS 5.1");
            esperaViolacao("comercio.catalogo.controle", "comercio.catalogo.persistencia.CatalogoEmMemoria", "DAS 5.1");
        });

        c.caso("DAS 5.1: Aplicacao nao depende do Controle; Persistencia so do Dominio", () -> {
            esperaPermitido("comercio.catalogo.aplicacao", "comercio.catalogo.persistencia.CatalogoEmMemoria");
            esperaViolacao("comercio.catalogo.aplicacao", "comercio.catalogo.controle.ControladorDeCatalogo", "DAS 5.1");
            esperaViolacao("comercio.catalogo.aplicacao", "comercio.infra.http.Requisicao", "DAS 5.1");
            esperaViolacao("comercio.catalogo.persistencia", "comercio.catalogo.aplicacao.ICatalogo", "DAS 5.1");
        });

        c.caso("DAS 5.3: sistemas externos so entram por porta do dominio", () -> {
            esperaPermitido("comercio.externos", "comercio.transacao.dominio.ICotacaoEntrega");
            esperaPermitido("comercio.externos.pagamento", "comercio.externos.pagamento.pix.AutorizadorPix");
            esperaViolacao("comercio.transacao.aplicacao", "comercio.externos.pagamento.FabricaPix", "portas");
            esperaViolacao("comercio.externos", "comercio.transacao.aplicacao.ServicoDeEntrega", "portas do dominio");
            esperaViolacao("comercio.transacao.dominio", "comercio.externos.TransportadoraSimulada", "portas");
        });

        c.caso("nucleo e infraestrutura nao conhecem o negocio; ninguem depende da area de espera", () -> {
            esperaViolacao("comercio.nucleo", "comercio.contas.dominio.CEP", "nucleo");
            esperaViolacao("comercio.infra.http", "comercio.contas.dominio.Usuario", "infraestrutura");
            esperaPermitido("comercio.infra.http", "comercio.nucleo.RegraDeNegocioException");
            esperaViolacao("comercio.catalogo.aplicacao", "comercio.importado.state.context.Pedido", "area de espera");
            esperaViolacao("comercio.catalogo.dominio", "comercio.Aplicacao", "raiz de composicao");
        });

        c.caso("sem biblioteca de terceiros e sem interface grafica dentro das camadas", () -> {
            esperaPermitido("comercio.catalogo.dominio", "java.util.List");
            esperaViolacao("comercio.catalogo.dominio", "org.apache.commons.lang3.StringUtils", "terceiros");
            esperaViolacao("comercio.catalogo.aplicacao", "javax.swing.JFrame", "interface grafica");
            esperaViolacao("comercio.catalogo.controle", "com.sun.net.httpserver.HttpExchange", "servidor HTTP");
        });

        c.caso("pacote fora da matriz dominio x camada e apontado", () -> {
            esperaViolacao("comercio.catalogo.util", "comercio.nucleo.Dinheiro", "camada desconhecida");
            esperaViolacao("comercio.utilitarios", "comercio.catalogo.dominio.Anuncio", "fora da estrutura");
            Verifica.contem(RegrasDeArquitetura.violacaoDeEstrutura("comercio.catalogo.padroes"), "camada desconhecida");
            Verifica.igual(null, RegrasDeArquitetura.violacaoDeEstrutura("comercio.catalogo.dominio.refinamento"));
        });
    }
}
