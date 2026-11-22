package comercio.catalogo.dominio;

/**
 * Ciclo de vida de um anuncio, conforme a Maquina de Estados do Anuncio da
 * Entrega 2. So anuncios PUBLICADO aparecem na busca e podem ser comprados.
 */
public enum StatusAnuncio {
    RASCUNHO,
    PUBLICADO,
    PAUSADO,
    ENCERRADO
}
