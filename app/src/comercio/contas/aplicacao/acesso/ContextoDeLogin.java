package comercio.contas.aplicacao.acesso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * O que a cadeia descobre enquanto percorre os elos: quem e o usuario, se o
 * segundo fator e exigido e o percurso feito. A requisicao continua imutavel;
 * o que muda durante a validacao fica aqui.
 */
public final class ContextoDeLogin {

    private final RequisicaoLogin requisicao;
    private final List<String> percurso = new ArrayList<String>();
    private String usuarioId;
    private boolean exigeDoisFatores;

    public ContextoDeLogin(RequisicaoLogin requisicao) {
        this.requisicao = requisicao;
    }

    public RequisicaoLogin requisicao() {
        return requisicao;
    }

    void registrar(String elo, String resultado) {
        percurso.add(elo + ": " + resultado);
    }

    public List<String> percurso() {
        return Collections.unmodifiableList(percurso);
    }

    public String usuarioId() {
        return usuarioId;
    }

    void identificar(String id) {
        this.usuarioId = id;
    }

    public boolean exigeDoisFatores() {
        return exigeDoisFatores;
    }

    void exigirDoisFatores() {
        this.exigeDoisFatores = true;
    }
}
