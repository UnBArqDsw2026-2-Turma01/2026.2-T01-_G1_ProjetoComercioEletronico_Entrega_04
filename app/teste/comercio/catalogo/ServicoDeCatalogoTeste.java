package comercio.catalogo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import comercio.catalogo.aplicacao.CepInformado;
import comercio.catalogo.aplicacao.FacetaDTO;
import comercio.catalogo.aplicacao.FichaDTO;
import comercio.catalogo.aplicacao.ItemDTO;
import comercio.catalogo.aplicacao.ListagemDTO;
import comercio.catalogo.aplicacao.OfertaDTO;
import comercio.catalogo.aplicacao.PedidoDeBusca;
import comercio.catalogo.aplicacao.ServicoDeCatalogo;
import comercio.catalogo.dominio.Anuncio;
import comercio.catalogo.dominio.Cotacao;
import comercio.catalogo.dominio.IFrete;
import comercio.catalogo.persistencia.RepositorioDeAnuncios;
import comercio.catalogo.persistencia.SementeDoCatalogo;
import comercio.contas.dominio.CEP;
import comercio.contas.persistencia.RepositorioDeContas;
import comercio.contas.persistencia.SementeDeContas;
import comercio.nucleo.Dinheiro;
import comercio.nucleo.NaoEncontradoException;
import comercio.nucleo.RegraDeNegocioException;
import comercio.teste.Casos;
import comercio.teste.Suite;
import comercio.teste.Verifica;

public final class ServicoDeCatalogoTeste implements Suite {

    /** Porta falsa: conta as cotacoes e responde sempre R$ 12,34 em 3 dias. */
    private static final class FreteFalso implements IFrete {
        int cotacoes;

        @Override
        public Cotacao cotar(CEP destino, Anuncio anuncio) {
            cotacoes++;
            return new Cotacao(Dinheiro.emReais(1234), 3);
        }
    }

    private RepositorioDeContas contas;
    private FreteFalso frete;

    private ServicoDeCatalogo servico() {
        contas = new RepositorioDeContas();
        SementeDeContas.popular(contas);
        RepositorioDeAnuncios anuncios = new RepositorioDeAnuncios();
        SementeDoCatalogo.popular(anuncios, contas.vendedores());
        frete = new FreteFalso();
        return new ServicoDeCatalogo(anuncios, frete);
    }

    private static PedidoDeBusca busca(String termo, String ordem, String cep, int pagina) {
        return new PedidoDeBusca(termo, null, ordem, cep, pagina);
    }

    private static PedidoDeBusca comFiltro(String termo, String nome, String opcao) {
        Map<String, String> filtros = new LinkedHashMap<String, String>();
        filtros.put(nome, opcao);
        return new PedidoDeBusca(termo, filtros, null, null, 1);
    }

    private static List<String> ids(ListagemDTO dto) {
        List<String> ids = new ArrayList<String>();
        for (ItemDTO item : dto.itens()) {
            ids.add(item.anuncioId());
        }
        return ids;
    }

    @Override
    public void registrar(Casos c) {
        c.caso("RN-A04: sem CEP a cadeia montada nao tem o decorador de frete", () -> {
            ListagemDTO dto = servico().buscar(busca("fone de ouvido", null, null, 1));
            Verifica.falso(dto.mecanismo().toLowerCase().contains("frete"), "mecanismo: " + dto.mecanismo());
            Verifica.igual(0, frete.cotacoes);
            for (ItemDTO item : dto.itens()) {
                Verifica.falso(item.selos().containsKey("frete"), "sem CEP nao ha selo de frete");
            }
        });

        c.caso("RN-A04: com CEP a cadeia ganha ComFreteEPrazo e cada item da pagina traz frete e prazo", () -> {
            ListagemDTO dto = servico().buscar(busca("fone de ouvido", null, "72230-130", 1));
            Verifica.contem(dto.mecanismo(), "ComFreteEPrazo");
            for (ItemDTO item : dto.itens()) {
                Verifica.igual("R$ 12,34", item.selos().get("frete"));
                Verifica.igual("3 dia(s) útil(eis)", item.selos().get("prazo"));
            }
        });

        c.caso("RN-A02: ordenacao RELEVANCIA nao inclui ComOrdenacao na cadeia", () -> {
            ListagemDTO dto = servico().buscar(busca("fone", "RELEVANCIA", null, 1));
            Verifica.falso(dto.mecanismo().contains("ComOrdenacao"), "mecanismo: " + dto.mecanismo());
        });

        c.caso("UC04: MENOR_PRECO inclui ComOrdenacao e devolve os precos em ordem crescente", () -> {
            ListagemDTO dto = servico().buscar(busca("", "MENOR_PRECO", null, 1));
            Verifica.contem(dto.mecanismo(), "ComOrdenacao");
            long anterior = -1;
            for (ItemDTO item : dto.itens()) {
                Verifica.verdadeiro(item.preco().centavos() >= anterior, "fora de ordem: " + item.titulo());
                anterior = item.preco().centavos();
            }
        });

        c.caso("a cadeia montada comeca no catalogo cru e termina na paginacao", () -> {
            String mecanismo = servico().buscar(busca("fone", null, null, 1)).mecanismo();
            Verifica.verdadeiro(mecanismo.startsWith("CatalogoEmMemoria("), mecanismo);
            Verifica.verdadeiro(mecanismo.endsWith("+ ComTermosAlternativos + ComPaginacao"), mecanismo);
            Verifica.contem(mecanismo, "+ ComFiltroDeFacetas + ComReputacaoDoVendedor");
        });

        c.caso("UC30: 'fone de ouvido' tem 9 resultados em 2 paginas; a segunda traz 3", () -> {
            ServicoDeCatalogo servico = servico();
            ListagemDTO primeira = servico.buscar(busca("fone de ouvido", null, null, 1));
            ListagemDTO segunda = servico.buscar(busca("fone de ouvido", null, null, 2));
            Verifica.igual(9, primeira.total());
            Verifica.igual(2, primeira.paginas());
            Verifica.igual(6, primeira.itens().size());
            Verifica.igual(3, segunda.itens().size());
            Verifica.igual(2, segunda.pagina());
        });

        c.caso("RN-A03: facetas com contagem aparecem mesmo sem filtro", () -> {
            ListagemDTO dto = servico().buscar(busca("fone de ouvido", null, null, 1));
            Verifica.verdadeiro(!dto.facetas().isEmpty(), "sem facetas");
            int somaCondicao = 0;
            for (FacetaDTO faceta : dto.facetas()) {
                if (faceta.nome().equals("condicao")) {
                    somaCondicao += faceta.contagemPrevia();
                }
            }
            Verifica.igual(dto.total(), somaCondicao);
        });

        c.caso("UC03: filtrar por condicao USADO devolve so usados, aceitando minusculas", () -> {
            ListagemDTO dto = servico().buscar(comFiltro("", "condicao", "usado"));
            Verifica.verdadeiro(dto.total() > 0, "deveria haver usados");
            for (ItemDTO item : dto.itens()) {
                Verifica.igual("USADO", item.condicao());
            }
        });

        c.caso("RN-A05: todo item da listagem traz o selo de reputacao do vendedor", () -> {
            for (ItemDTO item : servico().buscar(busca("", null, null, 1)).itens()) {
                Verifica.verdadeiro(item.selos().containsKey("reputação"), item.titulo() + " sem reputacao");
            }
        });

        c.caso("RN-A01: busca sem resultado traz termos alternativos e zero paginas", () -> {
            ListagemDTO dto = servico().buscar(busca("fones sem marca", null, null, 1));
            Verifica.igual(0, dto.total());
            Verifica.igual(0, dto.paginas());
            Verifica.verdadeiro(!dto.termosAlternativos().isEmpty(), "deveria sugerir termos");
        });

        c.caso("UC02: sugerir('fon') devolve termos que comecam com 'fon', no maximo 5, sem repeticao", () -> {
            List<String> sugestoes = servico().sugerir("fon");
            Verifica.verdadeiro(!sugestoes.isEmpty(), "sem sugestoes para fon");
            Verifica.verdadeiro(sugestoes.size() <= 5, "mais de 5 sugestoes: " + sugestoes);
            Verifica.igual(sugestoes.size(), new HashSet<String>(sugestoes).size());
            for (String sugestao : sugestoes) {
                Verifica.verdadeiro(sugestao.toLowerCase().startsWith("fon"), sugestao);
            }
        });

        c.caso("UC02: sugestao ignora maiusculas e acentos; prefixo vazio nao sugere nada", () -> {
            ServicoDeCatalogo servico = servico();
            Verifica.igual(servico.sugerir("fon"), servico.sugerir(" FÔN"));
            Verifica.igual(0, servico.sugerir("").size());
            Verifica.igual(0, servico.sugerir(null).size());
            Verifica.igual(0, servico.sugerir("geladeira").size());
        });

        c.caso("RN-B09: a ficha de um produto com varias ofertas lista as outras em outrasOfertas", () -> {
            FichaDTO ficha = servico().ficha("anu-001", null);
            Set<String> outras = new HashSet<String>();
            for (OfertaDTO oferta : ficha.outrasOfertas()) {
                outras.add(oferta.anuncioId());
            }
            Verifica.verdadeiro(outras.contains("anu-002"), "faltou anu-002");
            Verifica.verdadeiro(outras.contains("anu-003"), "faltou anu-003");
            Verifica.falso(outras.contains("anu-001"), "a propria oferta nao e 'outra oferta'");
            Verifica.igual("pro-001", ficha.produtoId());
        });

        c.caso("RN-A04 e UC07: a ficha traz frete e prazo sem existir nenhum pedido", () -> {
            ServicoDeCatalogo servico = servico();
            FichaDTO comCep = servico.ficha("anu-001", CEP.de("72230-130"));
            Verifica.igual(Dinheiro.emReais(1234), comCep.frete());
            Verifica.igual(Integer.valueOf(3), comCep.prazoDias());
            FichaDTO semCep = servico.ficha("anu-001", null);
            Verifica.igual(null, semCep.frete());
            Verifica.igual(null, semCep.prazoDias());
        });

        c.caso("UC06 e RN-A05: a ficha mostra a reputacao atual do vendedor", () -> {
            ServicoDeCatalogo servico = servico();
            int antes = servico.ficha("anu-003", null).avaliacoesVendedor();
            contas.vendedor("ven-03").receberAvaliacao(5);
            Verifica.igual(antes + 1, servico.ficha("anu-003", null).avaliacoesVendedor());
        });

        c.caso("UC05: parcelamento em 10 vezes calculado no servidor, sem perder centavos", () -> {
            FichaDTO ficha = servico().ficha("anu-001", null);
            Verifica.igual(10, ficha.parcelas().size());
            Dinheiro soma = Dinheiro.zero();
            for (Dinheiro parcela : ficha.parcelas()) {
                soma = soma.somar(parcela);
            }
            Verifica.igual(ficha.item().preco(), soma);
        });

        c.caso("UC05: anuncio pausado tem ficha, mas nao esta disponivel; inexistente nao existe", () -> {
            ServicoDeCatalogo servico = servico();
            Verifica.falso(servico.ficha("anu-025", null).disponivel(), "anu-025 esta pausado");
            Verifica.verdadeiro(servico.ficha("anu-001", null).disponivel(), "anu-001 esta publicado");
            Verifica.lanca(NaoEncontradoException.class, () -> servico.ficha("anu-999", null));
        });

        c.caso("RN-A06: buscar(PedidoDeBusca.deURL(dto.url)) devolve os mesmos itens", () -> {
            ServicoDeCatalogo servico = servico();
            Map<String, String> filtros = new LinkedHashMap<String, String>();
            filtros.put("condicao", "NOVO");
            ListagemDTO original = servico.buscar(new PedidoDeBusca("fone de ouvido", filtros, "MAIOR_PRECO",
                    "72230130", 2));
            ListagemDTO reproduzida = servico.buscar(PedidoDeBusca.deURL(original.url()));
            Verifica.igual(ids(original), ids(reproduzida));
            Verifica.igual(original.total(), reproduzida.total());
            Verifica.igual(original.url(), reproduzida.url());
        });

        c.caso("RF-A03: ordenacao desconhecida e recusada", () -> {
            RegraDeNegocioException recusa = Verifica.lanca(RegraDeNegocioException.class,
                    () -> servico().buscar(busca("fone", "PRECO_ALEATORIO", null, 1)));
            Verifica.igual("RF-A03", recusa.codigo());
        });

        c.caso("RF-A02: filtro ou condicao desconhecidos sao recusados", () -> {
            ServicoDeCatalogo servico = servico();
            Verifica.igual("RF-A02", Verifica.lanca(RegraDeNegocioException.class,
                    () -> servico.buscar(comFiltro("fone", "cor", "azul"))).codigo());
            Verifica.igual("RF-A02", Verifica.lanca(RegraDeNegocioException.class,
                    () -> servico.buscar(comFiltro("fone", "condicao", "SEMINOVO"))).codigo());
        });

        c.caso("RN-B04: CEP invalido na busca e na ficha e recusado", () -> {
            ServicoDeCatalogo servico = servico();
            Verifica.igual("RN-B04", Verifica.lanca(RegraDeNegocioException.class,
                    () -> servico.buscar(busca("fone", null, "123", 1))).codigo());
            Verifica.igual("RN-B04", Verifica.lanca(RegraDeNegocioException.class,
                    () -> servico.ficha("anu-001", CepInformado.ouNulo("abc"))).codigo());
            Verifica.igual(null, CepInformado.ouNulo("  "));
        });
    }
}
