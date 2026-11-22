package comercio.infra.http;

/** O que uma rota executa: recebe a requisicao e devolve a resposta. */
public interface Tratador {
    Resposta tratar(Requisicao requisicao) throws Exception;
}
