package comercio.catalogo.aplicacao;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import comercio.catalogo.dominio.ConsultaCatalogo;

/**
 * O que a camada de Controle entrega ao Servico de Catalogo: os parametros da
 * busca ainda como texto, do jeito que chegaram na requisicao.
 *
 * Existe para o controlador nao precisar conhecer o dominio. Quem valida e
 * converte em ConsultaCatalogo (value object) e o servico.
 */
public final class PedidoDeBusca {

    private final String termo;
    private final Map<String, String> filtros;
    private final String ordem;
    private final String cep;
    private final int pagina;

    /**
     * @param ordem  nome de uma Ordenacao; nulo vale RELEVANCIA
     * @param cep    com ou sem hifen; nulo quando o comprador nao informou
     * @param pagina a partir de 1; valores menores sao tratados como 1
     */
    public PedidoDeBusca(String termo, Map<String, String> filtros, String ordem, String cep, int pagina) {
        this.termo = termo == null ? "" : termo.trim();
        this.filtros = filtros == null ? new LinkedHashMap<String, String>()
                : new LinkedHashMap<String, String>(filtros);
        this.ordem = ordem == null || ordem.trim().isEmpty() ? "RELEVANCIA" : ordem.trim().toUpperCase();
        this.cep = cep == null || cep.trim().isEmpty() ? null : cep.trim();
        this.pagina = pagina < 1 ? 1 : pagina;
    }

    /** Reconstroi o pedido a partir da URL produzida por ConsultaCatalogo.paraURL (RN-A06). */
    public static PedidoDeBusca deURL(String url) {
        ConsultaCatalogo consulta = ConsultaCatalogo.deURL(url);
        return new PedidoDeBusca(consulta.termo(), consulta.filtros(), consulta.ordenacao().name(),
                consulta.cep(), consulta.pagina());
    }

    public String termo() {
        return termo;
    }

    public Map<String, String> filtros() {
        return Collections.unmodifiableMap(filtros);
    }

    public String ordem() {
        return ordem;
    }

    public String cep() {
        return cep;
    }

    public int pagina() {
        return pagina;
    }
}
