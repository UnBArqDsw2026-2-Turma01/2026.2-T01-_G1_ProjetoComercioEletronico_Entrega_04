package comercio.composicao;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import comercio.Aplicacao;
import comercio.infra.http.Requisicao;
import comercio.infra.http.Resposta;
import comercio.infra.http.Rota;
import comercio.teste.Casos;
import comercio.teste.Suite;
import comercio.teste.Verifica;

/** A aplicacao montada pela raiz de composicao, exercitada sem rede. */
public final class AplicacaoTeste implements Suite {

    private static final Pattern ROTA = Pattern.compile("Rota\\.(get|post|patch|delete)\\(\"([^\"]+)\"");

    private static void controladores(File pasta, List<File> encontrados) {
        File[] filhos = pasta.listFiles();
        if (filhos == null) {
            return;
        }
        for (File filho : filhos) {
            if (filho.isDirectory()) {
                controladores(filho, encontrados);
            } else if (filho.getName().startsWith("Controlador") && filho.getName().endsWith(".java")
                    && filho.getParentFile().getName().equals("controle")) {
                encontrados.add(filho);
            }
        }
    }

    @Override
    public void registrar(Casos c) {
        c.caso("a raiz de composicao monta a aplicacao e ela responde", () -> {
            Resposta resposta = Aplicacao.montar().roteador().despachar(Requisicao.deTeste("GET", "/api/saude"));
            Verifica.igual(200, resposta.status());
            Verifica.igual("G1_ProjetoComercioEletronico", resposta.corpo().get("projeto"));
        });

        c.caso("cada montagem tem os seus proprios dados: uma execucao nao contamina a outra", () -> {
            Aplicacao primeira = Aplicacao.montar();
            Aplicacao segunda = Aplicacao.montar();
            primeira.contas().vendedor("ven-03").receberAvaliacao(5);
            Verifica.igual(41, segunda.contas().vendedor("ven-03").reputacao().avaliacoes());
            Verifica.igual(42, primeira.contas().vendedor("ven-03").reputacao().avaliacoes());
        });

        c.caso("todo controlador da camada de Controle esta registrado na raiz de composicao", () -> {
            Set<String> publicadas = new HashSet<String>();
            for (Rota rota : Aplicacao.montar().roteador().rotas()) {
                publicadas.add(rota.metodo() + " " + rota.padrao());
            }
            List<File> arquivos = new ArrayList<File>();
            controladores(new File(System.getProperty("comercio.src", "src")), arquivos);
            Verifica.verdadeiro(arquivos.size() >= 5, "esperava ao menos cinco controladores, achei " + arquivos.size());
            List<String> faltando = new ArrayList<String>();
            for (File arquivo : arquivos) {
                String codigo = new String(Files.readAllBytes(arquivo.toPath()), StandardCharsets.UTF_8);
                Matcher m = ROTA.matcher(codigo);
                while (m.find()) {
                    String rota = m.group(1).toUpperCase() + " " + m.group(2);
                    if (!publicadas.contains(rota)) {
                        faltando.add(arquivo.getName() + ": " + rota);
                    }
                }
            }
            Verifica.verdadeiro(faltando.isEmpty(), "rotas declaradas e nao publicadas: " + faltando);
        });

        c.caso("busca, ficha e pos-venda respondem pela aplicacao montada", () -> {
            Aplicacao aplicacao = Aplicacao.montar();
            Verifica.igual(200, aplicacao.roteador().despachar(Requisicao.deTeste("GET", "/api/busca")
                    .comConsulta("termo", "fone")).status());
            Verifica.igual(200, aplicacao.roteador().despachar(Requisicao.deTeste("GET", "/api/anuncios/anu-007"))
                    .status());
            Verifica.igual(401, aplicacao.roteador().despachar(Requisicao.deTeste("GET", "/api/posvenda/ped-0001"))
                    .status());
        });

        c.caso("toda rota publicada aparece em /api/rastreabilidade", () -> {
            Aplicacao aplicacao = Aplicacao.montar();
            Resposta resposta = aplicacao.roteador().despachar(Requisicao.deTeste("GET", "/api/rastreabilidade"));
            List<?> rotas = (List<?>) resposta.corpo().get("rotas");
            Verifica.igual(aplicacao.roteador().rotas().size(), rotas.size());
        });
    }
}
