package comercio.catalogo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import comercio.catalogo.aplicacao.IAnuncio;
import comercio.catalogo.aplicacao.ServicoDeAnuncios;
import comercio.catalogo.dominio.Anuncio;
import comercio.catalogo.dominio.ConsultaCatalogo;
import comercio.catalogo.dominio.StatusAnuncio;
import comercio.catalogo.persistencia.CatalogoEmMemoria;
import comercio.catalogo.persistencia.RepositorioDeAnuncios;
import comercio.catalogo.persistencia.SementeDoCatalogo;
import comercio.contas.persistencia.RepositorioDeContas;
import comercio.contas.persistencia.SementeDeContas;
import comercio.nucleo.NaoEncontradoException;
import comercio.nucleo.RegraDeNegocioException;
import comercio.teste.Casos;
import comercio.teste.Suite;
import comercio.teste.Verifica;

public final class AcervoTeste implements Suite {

    private RepositorioDeContas contas;

    private RepositorioDeAnuncios acervo() {
        contas = new RepositorioDeContas();
        SementeDeContas.popular(contas);
        RepositorioDeAnuncios anuncios = new RepositorioDeAnuncios();
        SementeDoCatalogo.popular(anuncios, contas.vendedores());
        return anuncios;
    }

    @Override
    public void registrar(Casos c) {
        c.caso("RN-C04 e RN-B09: o mesmo produto tem ofertas de vendedores, precos e condicoes diferentes", () -> {
            List<Anuncio> ofertas = acervo().doProduto("pro-001");
            Verifica.igual(3, ofertas.size());
            Set<String> vendedores = new HashSet<String>();
            Set<Long> precos = new HashSet<Long>();
            for (Anuncio oferta : ofertas) {
                Verifica.igual("Fone de ouvido bluetooth XZ-200", oferta.titulo());
                vendedores.add(oferta.vendedor().id());
                precos.add(oferta.preco().centavos());
            }
            Verifica.igual(3, vendedores.size());
            Verifica.igual(3, precos.size());
        });

        c.caso("RN-B07: reservar mais que o estoque e recusado e o estoque nao muda", () -> {
            IAnuncio servico = new ServicoDeAnuncios(acervo());
            Verifica.igual(1, servico.obter("anu-003").estoque());
            RegraDeNegocioException recusa = Verifica.lanca(RegraDeNegocioException.class,
                    () -> servico.reservar("anu-003", 2));
            Verifica.igual("RN-B07", recusa.codigo());
            Verifica.igual(1, servico.obter("anu-003").estoque());
        });

        c.caso("RN-B07: reservar retira do estoque e devolver recoloca", () -> {
            IAnuncio servico = new ServicoDeAnuncios(acervo());
            servico.reservar("anu-001", 5);
            Verifica.igual(7, servico.obter("anu-001").estoque());
            servico.devolver("anu-001", 5);
            Verifica.igual(12, servico.obter("anu-001").estoque());
            Verifica.lanca(RegraDeNegocioException.class, () -> servico.reservar("anu-001", 0));
        });

        c.caso("RN-A05: a nota exibida no anuncio e a reputacao atual do vendedor", () -> {
            RepositorioDeAnuncios anuncios = acervo();
            Anuncio oferta = anuncios.obter("anu-003");
            int antes = oferta.quantidadeAvaliacoes();
            contas.vendedor("ven-03").receberAvaliacao(5);
            Verifica.igual(antes + 1, oferta.quantidadeAvaliacoes());
        });

        c.caso("anuncio pausado nao aparece na busca nem pode ser reservado", () -> {
            RepositorioDeAnuncios anuncios = acervo();
            Verifica.igual(StatusAnuncio.PAUSADO, anuncios.obter("anu-025").status());
            Verifica.igual(26, anuncios.total());
            Verifica.igual(25, anuncios.publicados().size());
            for (Anuncio publicado : anuncios.publicados()) {
                Verifica.falso(publicado.id().equals("anu-025"), "anuncio pausado nao pode estar entre os publicados");
            }
            Verifica.lanca(RegraDeNegocioException.class, () -> anuncios.obter("anu-025").reservar(1));
        });

        c.caso("a semente cobre o que a busca precisa: nove fones, cinco categorias, seis vendedores", () -> {
            RepositorioDeAnuncios anuncios = acervo();
            Verifica.igual(9, new CatalogoEmMemoria(anuncios).buscar(new ConsultaCatalogo("fone de ouvido")).total());
            Set<String> categorias = new HashSet<String>();
            Set<String> vendedores = new HashSet<String>();
            for (Anuncio anuncio : anuncios.publicados()) {
                categorias.add(anuncio.categoria());
                vendedores.add(anuncio.vendedor().id());
            }
            Verifica.igual(5, categorias.size());
            Verifica.igual(6, vendedores.size());
        });

        c.caso("anuncio inexistente e sinalizado como nao encontrado", () -> {
            RepositorioDeAnuncios anuncios = acervo();
            Verifica.lanca(NaoEncontradoException.class, () -> anuncios.obter("anu-999"));
        });
    }
}
