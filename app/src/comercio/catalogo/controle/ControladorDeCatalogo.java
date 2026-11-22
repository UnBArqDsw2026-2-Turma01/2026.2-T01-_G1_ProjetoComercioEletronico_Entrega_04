package comercio.catalogo.controle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import comercio.catalogo.aplicacao.CepInformado;
import comercio.catalogo.aplicacao.FacetaDTO;
import comercio.catalogo.aplicacao.FichaDTO;
import comercio.catalogo.aplicacao.ICatalogo;
import comercio.catalogo.aplicacao.IFichaProduto;
import comercio.catalogo.aplicacao.ItemDTO;
import comercio.catalogo.aplicacao.ListagemDTO;
import comercio.catalogo.aplicacao.OfertaDTO;
import comercio.catalogo.aplicacao.PedidoDeBusca;
import comercio.infra.http.ModuloDeRotas;
import comercio.infra.http.Requisicao;
import comercio.infra.http.Resposta;
import comercio.infra.http.Rota;
import comercio.nucleo.Dinheiro;

/**
 * Rotas do Catalogo e Descoberta: busca, sugestoes e ficha do anuncio.
 *
 * O controlador faz tres coisas e nada mais: le os parametros da requisicao,
 * monta o pedido para a camada de Aplicacao e converte o DTO de resposta em
 * mapa. Depende das interfaces ICatalogo e IFichaProduto, e nao da classe
 * ServicoDeCatalogo, e nao conhece nenhuma classe de dominio. Toda validacao
 * (ordenacao, filtros, CEP) fica no servico; as recusas sobem como excecao e o
 * Roteador as converte em 4xx.
 *
 * Todas as rotas sao publicas: buscar, ver a ficha e cotar o frete nao exigem
 * login (RN-A04: o frete aparece antes do carrinho).
 *
 * Autor: Pedro Luciano de Azevedo (Entrega 4).
 */
public final class ControladorDeCatalogo implements ModuloDeRotas {

    /** Filtros que a busca le da query string (RF-A02). */
    private static final String[] FILTROS = { "categoria", "condicao" };

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final ICatalogo catalogo;
    private final IFichaProduto fichas;

    public ControladorDeCatalogo(ICatalogo catalogo, IFichaProduto fichas) {
        if (catalogo == null || fichas == null) {
            throw new IllegalArgumentException("O controlador do catalogo precisa de ICatalogo e IFichaProduto");
        }
        this.catalogo = catalogo;
        this.fichas = fichas;
    }

    @Override
    public List<Rota> rotas() {
        List<Rota> rotas = new ArrayList<Rota>();

        rotas.add(Rota.get("/api/busca", r -> {
            ListagemDTO dto = catalogo.buscar(pedido(r));
            return Resposta.ok(listagem(dto)).mecanismo(dto.mecanismo());
        }).uc("UC01", "UC03", "UC04", "UC30").tr("TR-01")
                .classes("ControladorDeCatalogo", "ServicoDeCatalogo", "RefinamentoDecorator", "CatalogoEmMemoria"));

        rotas.add(Rota.get("/api/sugestoes", r -> {
            String prefixo = primeiro(r.consulta("q"), r.consulta("termo"));
            List<String> sugestoes = catalogo.sugerir(prefixo);
            Map<String, Object> corpo = new LinkedHashMap<String, Object>();
            corpo.put("sugestoes", sugestoes);
            return Resposta.ok(corpo).mecanismo("ServicoDeCatalogo.sugerir: " + sugestoes.size()
                    + " título(s) publicado(s) começando com \"" + (prefixo == null ? "" : prefixo)
                    + "\", no máximo cinco e sem repetição (UC02)");
        }).uc("UC02").tr("TR-01").classes("ControladorDeCatalogo", "ServicoDeCatalogo", "RepositorioDeAnuncios"));

        rotas.add(Rota.get("/api/anuncios/{id}", r -> {
            FichaDTO dto = fichas.ficha(r.parametro("id"), CepInformado.ouNulo(r.consulta("cep")));
            return Resposta.ok(ficha(dto)).mecanismo(mecanismoDaFicha(dto));
        }).uc("UC05", "UC06", "UC07").tr("TR-02", "TR-03")
                .classes("ControladorDeCatalogo", "ServicoDeCatalogo", "Anuncio", "Produto", "Vendedor", "IFrete"));

        return rotas;
    }

    // ----------------------------------------------------------------- entrada

    /** Le a query string como texto. pagina ausente vale 1; ordem ausente, RELEVANCIA (no PedidoDeBusca). */
    private static PedidoDeBusca pedido(Requisicao r) {
        Map<String, String> filtros = new LinkedHashMap<String, String>();
        for (String nome : FILTROS) {
            String valor = r.consulta(nome);
            if (valor != null) {
                filtros.put(nome, valor);
            }
        }
        return new PedidoDeBusca(primeiro(r.consulta("termo"), r.consulta("q")), filtros, r.consulta("ordem"),
                r.consulta("cep"), r.consultaInteira("pagina", 1));
    }

    /** As paginas usam "termo" (o nome que ConsultaCatalogo.paraURL escreve); "q" e aceito como sinonimo. */
    private static String primeiro(String preferido, String alternativo) {
        return preferido != null ? preferido : alternativo;
    }

    // ------------------------------------------------------------------- saida

    private static Map<String, Object> listagem(ListagemDTO dto) {
        List<Object> itens = new ArrayList<Object>();
        for (ItemDTO item : dto.itens()) {
            itens.add(item(item));
        }
        List<Object> facetas = new ArrayList<Object>();
        for (FacetaDTO faceta : dto.facetas()) {
            Map<String, Object> f = new LinkedHashMap<String, Object>();
            f.put("nome", faceta.nome());
            f.put("opcao", faceta.opcao());
            f.put("contagemPrevia", faceta.contagemPrevia());
            facetas.add(f);
        }
        Map<String, Object> corpo = new LinkedHashMap<String, Object>();
        corpo.put("total", dto.total());
        corpo.put("pagina", dto.pagina());
        corpo.put("paginas", dto.paginas());
        corpo.put("url", dto.url());
        corpo.put("itens", itens);
        corpo.put("facetas", facetas);
        corpo.put("termosAlternativos", dto.termosAlternativos());
        corpo.put("cadeia", Arrays.asList(dto.mecanismo().split(" \\+ ")));
        return corpo;
    }

    private static Map<String, Object> item(ItemDTO item) {
        Map<String, Object> i = new LinkedHashMap<String, Object>();
        i.put("id", item.anuncioId());
        i.put("titulo", item.titulo());
        i.put("categoria", item.categoria());
        i.put("imagem", item.imagem());
        i.put("preco", item.preco().formatado());
        i.put("precoCentavos", item.preco().centavos());
        i.put("condicao", item.condicao());
        i.put("vendedorId", item.vendedorId());
        i.put("vendedor", item.vendedorNome());
        i.put("tipoVendedor", item.tipoVendedor());
        i.put("selos", item.selos());
        return i;
    }

    private static Map<String, Object> ficha(FichaDTO dto) {
        ItemDTO item = dto.item();
        Map<String, Object> corpo = new LinkedHashMap<String, Object>();
        corpo.put("id", item.anuncioId());
        corpo.put("titulo", item.titulo());
        corpo.put("produtoId", dto.produtoId());
        corpo.put("categoria", dto.categoria());
        corpo.put("imagens", dto.imagens());
        corpo.put("preco", item.preco().formatado());
        corpo.put("precoCentavos", item.preco().centavos());
        corpo.put("condicao", item.condicao());
        corpo.put("estoque", dto.estoque());
        corpo.put("disponivel", dto.disponivel());

        // As parcelas podem diferir em um centavo (Dinheiro.parcelar distribui o resto nas primeiras).
        List<Dinheiro> parcelas = dto.parcelas();
        Map<String, Object> parcelamento = new LinkedHashMap<String, Object>();
        parcelamento.put("vezes", parcelas.size());
        parcelamento.put("maiorParcela", parcelas.isEmpty() ? null : parcelas.get(0).formatado());
        parcelamento.put("menorParcela", parcelas.isEmpty() ? null : parcelas.get(parcelas.size() - 1).formatado());
        parcelamento.put("semJuros", true);
        corpo.put("parcelamento", parcelamento);

        Map<String, Object> vendedor = new LinkedHashMap<String, Object>();
        vendedor.put("id", item.vendedorId());
        vendedor.put("nome", item.vendedorNome());
        vendedor.put("tipo", item.tipoVendedor());
        vendedor.put("nota", String.format(PT_BR, "%.1f", dto.notaVendedor()));
        vendedor.put("avaliacoes", dto.avaliacoesVendedor());
        corpo.put("vendedor", vendedor);

        corpo.put("frete", dto.frete() == null ? null : dto.frete().formatado());
        corpo.put("freteCentavos", dto.frete() == null ? null : dto.frete().centavos());
        corpo.put("prazoDias", dto.prazoDias());

        List<Object> outras = new ArrayList<Object>();
        for (OfertaDTO oferta : dto.outrasOfertas()) {
            Map<String, Object> o = new LinkedHashMap<String, Object>();
            o.put("id", oferta.anuncioId());
            o.put("vendedor", oferta.vendedorNome());
            o.put("preco", oferta.preco().formatado());
            o.put("precoCentavos", oferta.preco().centavos());
            o.put("condicao", oferta.condicao());
            outras.add(o);
        }
        corpo.put("outrasOfertas", outras);
        return corpo;
    }

    private static String mecanismoDaFicha(FichaDTO dto) {
        StringBuilder texto = new StringBuilder("Reputação lida do Vendedor no momento da consulta (RN-A05). ");
        if (dto.frete() == null) {
            texto.append("Sem CEP: o frete não foi cotado (UC07 é extensão). ");
        } else {
            texto.append("Frete cotado pela porta IFrete, que o Serviço de Entrega implementa, sem carrinho nem pedido (RN-A04). ");
        }
        texto.append(dto.outrasOfertas().size()).append(" outra(s) oferta(s) do mesmo Produto (RN-B09).");
        return texto.toString();
    }
}
