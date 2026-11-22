package comercio.infra.http;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Rotas que nao pertencem a nenhum pacote de dominio: a verificacao de que o
 * servidor esta no ar e a listagem da rastreabilidade declarada pelas rotas.
 */
public final class RotasDeInfraestrutura implements ModuloDeRotas {

    private final Roteador roteador;

    public RotasDeInfraestrutura(Roteador roteador) {
        this.roteador = roteador;
    }

    @Override
    public List<Rota> rotas() {
        List<Rota> rotas = new ArrayList<Rota>();
        rotas.add(Rota.get("/api/saude", new Tratador() {
            @Override
            public Resposta tratar(Requisicao requisicao) {
                Map<String, Object> corpo = new LinkedHashMap<String, Object>();
                corpo.put("situacao", "no ar");
                corpo.put("projeto", "G1_ProjetoComercioEletronico");
                corpo.put("rotas", roteador.rotas().size());
                return Resposta.ok(corpo);
            }
        }).classes("RotasDeInfraestrutura"));

        rotas.add(Rota.get("/api/rastreabilidade", new Tratador() {
            @Override
            public Resposta tratar(Requisicao requisicao) {
                List<Object> linhas = new ArrayList<Object>();
                for (Rota rota : roteador.rotas()) {
                    Map<String, Object> linha = new LinkedHashMap<String, Object>();
                    linha.put("metodo", rota.metodo());
                    linha.put("caminho", rota.padrao());
                    linha.put("uc", rota.ucs());
                    linha.put("tr", rota.trs());
                    linha.put("classes", rota.classesEnvolvidas());
                    linhas.add(linha);
                }
                Map<String, Object> corpo = new LinkedHashMap<String, Object>();
                corpo.put("rotas", linhas);
                return Resposta.ok(corpo)
                        .mecanismo("Cada rota declara no código os casos de uso e as linhas da matriz que realiza");
            }
        }).classes("RotasDeInfraestrutura", "Roteador", "Rota"));
        return rotas;
    }
}
