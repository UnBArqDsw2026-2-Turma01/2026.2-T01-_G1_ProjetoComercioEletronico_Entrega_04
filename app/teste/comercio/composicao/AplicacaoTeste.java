package comercio.composicao;

import java.util.List;

import comercio.Aplicacao;
import comercio.infra.http.Requisicao;
import comercio.infra.http.Resposta;
import comercio.teste.Casos;
import comercio.teste.Suite;
import comercio.teste.Verifica;

/** A aplicacao montada pela raiz de composicao, exercitada sem rede. */
public final class AplicacaoTeste implements Suite {

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

        c.caso("toda rota publicada aparece em /api/rastreabilidade", () -> {
            Aplicacao aplicacao = Aplicacao.montar();
            Resposta resposta = aplicacao.roteador().despachar(Requisicao.deTeste("GET", "/api/rastreabilidade"));
            List<?> rotas = (List<?>) resposta.corpo().get("rotas");
            Verifica.igual(aplicacao.roteador().rotas().size(), rotas.size());
        });
    }
}
