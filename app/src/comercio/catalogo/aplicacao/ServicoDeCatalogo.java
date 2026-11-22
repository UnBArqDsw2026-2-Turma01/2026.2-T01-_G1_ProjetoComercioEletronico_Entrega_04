package comercio.catalogo.aplicacao;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import comercio.catalogo.dominio.Anuncio;
import comercio.catalogo.dominio.ConsultaCatalogo;
import comercio.catalogo.dominio.Cotacao;
import comercio.catalogo.dominio.Faceta;
import comercio.catalogo.dominio.FonteDeResultados;
import comercio.catalogo.dominio.IFrete;
import comercio.catalogo.dominio.ItemListagem;
import comercio.catalogo.dominio.Listagem;
import comercio.catalogo.dominio.Ordenacao;
import comercio.catalogo.dominio.refinamento.ComFiltroDeFacetas;
import comercio.catalogo.dominio.refinamento.ComFreteEPrazo;
import comercio.catalogo.dominio.refinamento.ComOrdenacao;
import comercio.catalogo.dominio.refinamento.ComPaginacao;
import comercio.catalogo.dominio.refinamento.ComReputacaoDoVendedor;
import comercio.catalogo.dominio.refinamento.ComTermosAlternativos;
import comercio.catalogo.persistencia.CatalogoEmMemoria;
import comercio.catalogo.persistencia.RepositorioDeAnuncios;
import comercio.contas.dominio.CEP;
import comercio.nucleo.Dinheiro;
import comercio.nucleo.RegraDeNegocioException;

/**
 * Servico de Catalogo (Diagrama de Componentes da Entrega 2): fornece ICatalogo
 * e IFichaProduto e requer IFrete do Servico de Entrega.
 *
 * Casos de uso: UC01, UC02, UC03, UC04 e UC30 (busca) e UC05, UC06 e UC07
 * (ficha). Linhas da matriz do DAS: TR-01, TR-02 e TR-03.
 *
 * E a fronteira entre o texto que chega da camada de Controle e o dominio:
 * recebe PedidoDeBusca, valida, converte em ConsultaCatalogo (value object) e
 * devolve DTOs. Nenhum objeto de dominio sai daqui para o controlador.
 *
 * O ponto central e montarCadeia: a cada busca o servico empilha so os
 * decoradores que aquela consulta pede. Na Entrega 3 a cadeia era montada uma
 * vez, inteira, no programa de demonstracao; aqui ela e decidida por consulta.
 * Sem CEP, ComFreteEPrazo nem entra na pilha (RN-A04 e um «extend»); com
 * ordenacao por relevancia, ComOrdenacao tambem nao (RN-A02: a relevancia e a
 * ordem do proprio catalogo). A descricao da cadeia montada volta no DTO, no
 * campo mecanismo, e e o que a tela mostra para provar que o resultado saiu
 * de uma pilha de objetos, e nao de uma classe so.
 *
 * Regras validadas aqui: RF-A03 (ordenacao conhecida), RF-A02 (filtros
 * conhecidos) e RN-B04 (CEP valido, delegado ao value object CEP).
 *
 * Autor: Pedro Luciano de Azevedo (Servico de Catalogo, Entrega 4).
 */
public final class ServicoDeCatalogo implements ICatalogo, IFichaProduto {

    /** UC02: quantas sugestoes, no maximo. */
    public static final int MAXIMO_DE_SUGESTOES = 5;

    /** Parcelas sem juros exibidas na ficha. INFERIDO: o limite real varia por anuncio e nao foi observado. */
    public static final int PARCELAS_SEM_JUROS = 10;

    /** Dimensoes de filtro que a busca aceita (RF-A02). */
    private static final List<String> FILTROS = Arrays.asList(ComFiltroDeFacetas.CATEGORIA,
            ComFiltroDeFacetas.CONDICAO);

    private final RepositorioDeAnuncios anuncios;
    private final IFrete frete;

    public ServicoDeCatalogo(RepositorioDeAnuncios anuncios, IFrete frete) {
        if (anuncios == null || frete == null) {
            throw new IllegalArgumentException("O Servico de Catalogo precisa do repositorio de anuncios e da porta IFrete");
        }
        this.anuncios = anuncios;
        this.frete = frete;
    }

    // ------------------------------------------------------------------ busca

    @Override
    public ListagemDTO buscar(PedidoDeBusca pedido) {
        ConsultaCatalogo consulta = consulta(pedido);
        FonteDeResultados cadeia = montarCadeia(consulta);
        Listagem listagem = cadeia.buscar(consulta);

        List<ItemDTO> itens = new ArrayList<ItemDTO>();
        for (ItemListagem item : listagem.itens()) {
            itens.add(item(item.anuncio(), item.selos()));
        }
        List<FacetaDTO> facetas = new ArrayList<FacetaDTO>();
        for (Faceta faceta : listagem.facetas()) {
            facetas.add(new FacetaDTO(faceta.nome(), faceta.opcao(), faceta.contagemPrevia()));
        }
        return new ListagemDTO(itens, facetas, listagem.termosAlternativos(), listagem.total(),
                consulta.pagina(), ComPaginacao.quantidadeDePaginas(listagem.total()), consulta.paraURL(),
                cadeia.descricao());
    }

    /**
     * Converte o pedido em texto no value object do dominio, recusando o que
     * nao tem sentido antes de qualquer decorador rodar.
     */
    private static ConsultaCatalogo consulta(PedidoDeBusca pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido de busca nulo");
        }
        ConsultaCatalogo consulta = new ConsultaCatalogo(pedido.termo())
                .comOrdenacao(ordenacao(pedido.ordem()))
                .naPagina(pedido.pagina());

        for (Map.Entry<String, String> filtro : pedido.filtros().entrySet()) {
            String nome = filtro.getKey();
            String opcao = filtro.getValue() == null ? "" : filtro.getValue().trim();
            if (!FILTROS.contains(nome)) {
                throw new RegraDeNegocioException("RF-A02", "Filtro desconhecido: \"" + nome
                        + "\". Os filtros aceitos são categoria e condicao");
            }
            if (opcao.isEmpty()) {
                continue;
            }
            if (ComFiltroDeFacetas.CONDICAO.equals(nome)) {
                opcao = condicao(opcao).name();
            }
            consulta = consulta.comFiltro(nome, opcao);
        }

        if (pedido.cep() != null) {
            // RN-B04: um CEP invalido e recusado aqui, com o codigo da regra.
            consulta = consulta.comCep(CEP.de(pedido.cep()).formatado());
        }
        return consulta;
    }

    private static Ordenacao ordenacao(String nome) {
        try {
            return Ordenacao.valueOf(nome);
        } catch (IllegalArgumentException e) {
            throw new RegraDeNegocioException("RF-A03", "Ordenação desconhecida: \"" + nome
                    + "\". Use RELEVANCIA, MENOR_PRECO ou MAIOR_PRECO");
        }
    }

    private static Anuncio.Condicao condicao(String nome) {
        try {
            return Anuncio.Condicao.valueOf(nome.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new RegraDeNegocioException("RF-A02", "Condição desconhecida: \"" + nome + "\". Use NOVO ou USADO");
        }
    }

    /**
     * Monta a pilha de decoradores para esta consulta. A ordem importa:
     *
     *   1. ComFiltroDeFacetas      sempre: conta as facetas e filtra (RN-A03)
     *   2. ComReputacaoDoVendedor  sempre: selo de reputacao (RN-A05)
     *   3. ComFreteEPrazo          so com CEP (RN-A04)
     *   4. ComOrdenacao            so se a ordem nao for RELEVANCIA (RN-A02)
     *   5. ComTermosAlternativos   sempre: age so quando a busca vem vazia (RN-A01)
     *   6. ComPaginacao            sempre, por ultimo (UC30)
     *
     * Filtrar vem antes de enriquecer, para o frete ser cotado so para os itens
     * que sobraram, e tudo vem antes de paginar, para a pagina ser cortada do
     * conjunto final (ver RefinamentosTeste).
     */
    FonteDeResultados montarCadeia(ConsultaCatalogo consulta) {
        FonteDeResultados cadeia = new CatalogoEmMemoria(anuncios);
        cadeia = new ComFiltroDeFacetas(cadeia);
        cadeia = new ComReputacaoDoVendedor(cadeia);
        if (consulta.cep() != null) {
            cadeia = new ComFreteEPrazo(cadeia, frete);
        }
        if (consulta.ordenacao() != Ordenacao.RELEVANCIA) {
            cadeia = new ComOrdenacao(cadeia);
        }
        cadeia = new ComTermosAlternativos(cadeia, titulosPublicados());
        cadeia = new ComPaginacao(cadeia);
        return cadeia;
    }

    private List<String> titulosPublicados() {
        List<String> titulos = new ArrayList<String>();
        for (Anuncio anuncio : anuncios.publicados()) {
            titulos.add(anuncio.titulo());
        }
        return titulos;
    }

    // -------------------------------------------------------------- sugestoes

    /**
     * UC02: titulos do acervo publicado que comecam com o prefixo digitado,
     * sem diferenciar maiusculas nem acentos. No maximo cinco, sem repeticao
     * (tres ofertas do mesmo produto viram uma sugestao so).
     */
    @Override
    public List<String> sugerir(String prefixo) {
        List<String> sugestoes = new ArrayList<String>();
        String procurado = normalizar(prefixo);
        if (procurado.isEmpty()) {
            return sugestoes;
        }
        Set<String> vistos = new LinkedHashSet<String>();
        for (Anuncio anuncio : anuncios.publicados()) {
            String titulo = anuncio.titulo();
            if (normalizar(titulo).startsWith(procurado) && vistos.add(titulo)) {
                sugestoes.add(titulo);
                if (sugestoes.size() == MAXIMO_DE_SUGESTOES) {
                    break;
                }
            }
        }
        return sugestoes;
    }

    private static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }

    // ------------------------------------------------------------------ ficha

    /**
     * UC05, com UC06 incluido e UC07 como extensao: a ficha mostra sempre a
     * reputacao do vendedor (RN-A05) e as outras ofertas do mesmo produto
     * (RN-B09), e so cota o frete quando ha CEP (RN-A04). A cotacao nao exige
     * carrinho nem pedido.
     */
    @Override
    public FichaDTO ficha(String anuncioId, CEP cepOuNulo) {
        Anuncio anuncio = anuncios.obter(anuncioId);

        Dinheiro valorDoFrete = null;
        Integer prazoDias = null;
        if (cepOuNulo != null) {
            Cotacao cotacao = frete.cotar(cepOuNulo, anuncio);
            valorDoFrete = cotacao.valor();
            prazoDias = cotacao.prazoDias();
        }

        List<OfertaDTO> outras = new ArrayList<OfertaDTO>();
        for (Anuncio oferta : anuncios.doProduto(anuncio.produto().id())) {
            if (!oferta.id().equals(anuncio.id())) {
                outras.add(new OfertaDTO(oferta.id(), oferta.vendedor().nome(), oferta.preco(),
                        oferta.condicao().name()));
            }
        }

        return new FichaDTO(item(anuncio, new LinkedHashMap<String, String>()), anuncio.produto().id(),
                anuncio.categoria(), anuncio.produto().imagens(), anuncio.estoque(), anuncio.disponivel(),
                anuncio.preco().parcelar(PARCELAS_SEM_JUROS), anuncio.notaVendedor(),
                anuncio.quantidadeAvaliacoes(), valorDoFrete, prazoDias, outras);
    }

    // --------------------------------------------------------------- conversao

    private static ItemDTO item(Anuncio anuncio, Map<String, String> selos) {
        List<String> imagens = anuncio.produto().imagens();
        return new ItemDTO(anuncio.id(), anuncio.titulo(), anuncio.categoria(),
                imagens.isEmpty() ? null : imagens.get(0), anuncio.preco(), anuncio.condicao().name(),
                anuncio.vendedor().id(), anuncio.vendedor().nome(), anuncio.tipoVendedor().name(), selos);
    }
}
