package comercio.catalogo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import comercio.catalogo.dominio.Anuncio;
import comercio.catalogo.dominio.ConsultaCatalogo;
import comercio.catalogo.dominio.ItemListagem;
import comercio.catalogo.dominio.StatusAnuncio;
import comercio.catalogo.persistencia.CatalogoEmMemoria;
import comercio.catalogo.persistencia.RepositorioDeAnuncios;
import comercio.catalogo.persistencia.SementeDoCatalogo;
import comercio.contas.persistencia.RepositorioDeContas;
import comercio.contas.persistencia.SementeDeContas;
import comercio.teste.Casos;
import comercio.teste.Suite;
import comercio.teste.Verifica;

/**
 * O que a semente do catalogo precisa garantir para a busca, a ficha e o
 * carrinho terem o que demonstrar. Cada item da lista da semente e um caso.
 */
public final class SementeDoCatalogoTeste implements Suite {

    private static RepositorioDeAnuncios semente() {
        RepositorioDeContas contas = new RepositorioDeContas();
        SementeDeContas.popular(contas);
        RepositorioDeAnuncios anuncios = new RepositorioDeAnuncios();
        SementeDoCatalogo.popular(anuncios, contas.vendedores());
        return anuncios;
    }

    /** Uma linha por anuncio com tudo o que a semente define, para comparar duas execucoes. */
    private static List<String> retrato(RepositorioDeAnuncios anuncios) {
        List<String> linhas = new ArrayList<String>();
        for (int i = 1; i <= anuncios.total(); i++) {
            Anuncio a = anuncios.obter(String.format("anu-%03d", i));
            linhas.add(a.id() + "|" + a.produto().id() + "|" + a.titulo() + "|" + a.vendedor().id() + "|"
                    + a.preco().centavos() + "|" + a.condicao() + "|" + a.estoque() + "|" + a.status());
        }
        return linhas;
    }

    @Override
    public void registrar(Casos c) {
        c.caso("semente: no minimo 24 anuncios, com ids sequenciais anu-001 em diante", () -> {
            RepositorioDeAnuncios anuncios = semente();
            Verifica.verdadeiro(anuncios.total() >= 24, "a semente tem so " + anuncios.total() + " anuncios");
            for (int i = 1; i <= anuncios.total(); i++) {
                String id = String.format("anu-%03d", i);
                Verifica.igual(id, anuncios.obter(id).id());
                Verifica.verdadeiro(anuncios.obter(id).produto().id().startsWith("pro-"),
                        id + " deveria apontar para um produto pro-...");
            }
        });

        c.caso("semente: deterministica, duas execucoes produzem o mesmo acervo", () -> {
            Verifica.igual(retrato(semente()), retrato(semente()));
        });

        c.caso("semente: cinco categorias e os seis vendedores ven-01 a ven-06", () -> {
            Set<String> categorias = new HashSet<String>();
            Set<String> vendedores = new HashSet<String>();
            for (Anuncio anuncio : semente().publicados()) {
                categorias.add(anuncio.categoria());
                vendedores.add(anuncio.vendedor().id());
            }
            Verifica.igual(5, categorias.size());
            for (int i = 1; i <= 6; i++) {
                String id = "ven-0" + i;
                Verifica.verdadeiro(vendedores.contains(id), "nenhum anuncio publicado de " + id);
            }
        });

        c.caso("UC01: 'fone de ouvido' traz ao menos 8 anuncios com condicao, peso, preco e vendedor variados", () -> {
            List<ItemListagem> itens = new CatalogoEmMemoria(semente())
                    .buscar(new ConsultaCatalogo("fone de ouvido")).itens();
            Verifica.verdadeiro(itens.size() >= 8, "a busca trouxe so " + itens.size() + " anuncios");
            Set<String> condicoes = new HashSet<String>();
            Set<Integer> pesos = new HashSet<Integer>();
            Set<Long> precos = new HashSet<Long>();
            Set<String> vendedores = new HashSet<String>();
            for (ItemListagem item : itens) {
                condicoes.add(item.anuncio().condicao().name());
                pesos.add(item.anuncio().pesoEmGramas());
                precos.add(item.anuncio().preco().centavos());
                vendedores.add(item.anuncio().vendedor().id());
            }
            Verifica.igual(2, condicoes.size());
            Verifica.verdadeiro(pesos.size() >= 4, "pesos pouco variados: " + pesos);
            Verifica.verdadeiro(precos.size() >= 6, "precos pouco variados: " + precos);
            Verifica.verdadeiro(vendedores.size() >= 4, "vendedores pouco variados: " + vendedores);
        });

        c.caso("RN-C04: ao menos dois produtos sao anunciados por dois vendedores diferentes", () -> {
            Map<String, Set<String>> vendedoresPorProduto = new HashMap<String, Set<String>>();
            for (Anuncio anuncio : semente().publicados()) {
                Set<String> vendedores = vendedoresPorProduto.get(anuncio.produto().id());
                if (vendedores == null) {
                    vendedores = new HashSet<String>();
                    vendedoresPorProduto.put(anuncio.produto().id(), vendedores);
                }
                vendedores.add(anuncio.vendedor().id());
            }
            int compartilhados = 0;
            for (Set<String> vendedores : vendedoresPorProduto.values()) {
                if (vendedores.size() >= 2) {
                    compartilhados++;
                }
            }
            Verifica.verdadeiro(compartilhados >= 2, "so " + compartilhados + " produto(s) com mais de um vendedor");
        });

        c.caso("RN-B07: ao menos um anuncio publicado tem uma unica unidade em estoque", () -> {
            boolean achou = false;
            for (Anuncio anuncio : semente().publicados()) {
                achou = achou || anuncio.estoque() == 1;
            }
            Verifica.verdadeiro(achou, "nenhum anuncio publicado com estoque 1");
        });

        c.caso("semente: ao menos um anuncio PAUSADO, e ele nao aparece na busca", () -> {
            RepositorioDeAnuncios anuncios = semente();
            List<String> pausados = new ArrayList<String>();
            for (int i = 1; i <= anuncios.total(); i++) {
                Anuncio anuncio = anuncios.obter(String.format("anu-%03d", i));
                if (anuncio.status() == StatusAnuncio.PAUSADO) {
                    pausados.add(anuncio.id());
                }
            }
            Verifica.verdadeiro(!pausados.isEmpty(), "nenhum anuncio pausado na semente");
            for (ItemListagem item : new CatalogoEmMemoria(anuncios).buscar(new ConsultaCatalogo("")).itens()) {
                Verifica.falso(pausados.contains(item.anuncio().id()), item.anuncio().id() + " pausado apareceu na busca");
            }
        });
    }
}
