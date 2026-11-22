package comercio.infra.http;

import java.util.List;

/**
 * Conjunto de rotas que um controlador publica.
 *
 * Cada controlador da camada de Controle implementa esta interface; a raiz de
 * composicao (comercio.Aplicacao) entrega os modulos ao Roteador.
 */
public interface ModuloDeRotas {
    List<Rota> rotas();
}
