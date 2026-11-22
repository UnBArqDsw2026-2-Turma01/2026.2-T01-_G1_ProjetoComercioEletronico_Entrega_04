package comercio.contas;

import comercio.contas.aplicacao.RegistroDeReputacao;
import comercio.contas.dominio.CEP;
import comercio.contas.dominio.Comprador;
import comercio.contas.dominio.Reputacao;
import comercio.contas.dominio.TipoVendedor;
import comercio.contas.dominio.Vendedor;
import comercio.contas.persistencia.RepositorioDeContas;
import comercio.contas.persistencia.SementeDeContas;
import comercio.nucleo.NaoEncontradoException;
import comercio.nucleo.RegraDeNegocioException;
import comercio.teste.Casos;
import comercio.teste.Suite;
import comercio.teste.Verifica;

public final class ContasTeste implements Suite {

    private static RepositorioDeContas contasDaSemente() {
        RepositorioDeContas contas = new RepositorioDeContas();
        SementeDeContas.popular(contas);
        return contas;
    }

    @Override
    public void registrar(Casos c) {
        c.caso("RN-B04: o CEP e guardado como oito digitos, com ou sem hifen na entrada", () -> {
            Verifica.igual("72230130", CEP.de("72230-130").digitos());
            Verifica.igual("72230130", CEP.de(" 72.230-130 ").digitos());
            Verifica.igual(CEP.de("72230130"), CEP.de("72230-130"));
            Verifica.igual("72230-130", CEP.de("72230130").formatado());
        });

        c.caso("RN-B04: nao existe CEP invalido; a recusa cita a regra", () -> {
            String[] invalidos = { "7223013", "722301300", "7223A-130", "", null };
            for (final String invalido : invalidos) {
                RegraDeNegocioException recusa = Verifica.lanca(RegraDeNegocioException.class, () -> CEP.de(invalido));
                Verifica.igual("RN-B04", recusa.codigo());
            }
        });

        c.caso("RN-B02: o endereco pertence a conta do comprador", () -> {
            Comprador joana = contasDaSemente().comprador(SementeDeContas.COMPRADORA);
            Verifica.igual(2, joana.enderecos().size());
            Verifica.igual(CEP.de("72230-130"), joana.endereco("end-01").cep());
            Verifica.lanca(NaoEncontradoException.class, () -> joana.endereco("end-99"));
        });

        c.caso("RN-A05: a reputacao e derivada e so muda quando chega uma avaliacao", () -> {
            RepositorioDeContas contas = contasDaSemente();
            Vendedor marcia = contas.vendedor("ven-03");
            Reputacao antes = marcia.reputacao();
            new RegistroDeReputacao(contas).registrar("ven-03", 5);
            Verifica.igual(antes.avaliacoes() + 1, marcia.reputacao().avaliacoes());
            Verifica.verdadeiro(marcia.reputacao().nota() > antes.nota(), "uma nota 5 deve elevar a media de 3,6");
        });

        c.caso("RN-A05: a media e a soma das notas dividida pela quantidade", () -> {
            Reputacao reputacao = Reputacao.semAvaliacoes().comAvaliacao(5).comAvaliacao(4).comAvaliacao(3);
            Verifica.igual(3, reputacao.avaliacoes());
            Verifica.igual(4.0, reputacao.nota());
            Verifica.igual(0.0, Reputacao.semAvaliacoes().nota());
        });

        c.caso("RN-A05: nota fora de 1 a 5 e recusada e nao altera a reputacao", () -> {
            RepositorioDeContas contas = contasDaSemente();
            Reputacao antes = contas.vendedor("ven-01").reputacao();
            RegraDeNegocioException recusa = Verifica.lanca(RegraDeNegocioException.class,
                    () -> new RegistroDeReputacao(contas).registrar("ven-01", 6));
            Verifica.igual("RN-A05", recusa.codigo());
            Verifica.igual(antes, contas.vendedor("ven-01").reputacao());
        });

        c.caso("o tipo do vendedor e enumeracao: loja oficial e autonomo sao a mesma classe", () -> {
            RepositorioDeContas contas = contasDaSemente();
            Verifica.igual(TipoVendedor.LOJA_OFICIAL, contas.vendedor("ven-01").tipo());
            Verifica.igual(TipoVendedor.AUTONOMO, contas.vendedor("ven-02").tipo());
            Verifica.igual(contas.vendedor("ven-01").getClass(), contas.vendedor("ven-02").getClass());
            Verifica.igual(6, contas.vendedores().size());
        });

        c.caso("uma conta de comprador nao responde como vendedor", () -> {
            RepositorioDeContas contas = contasDaSemente();
            Verifica.lanca(NaoEncontradoException.class, () -> contas.vendedor(SementeDeContas.COMPRADORA));
            Verifica.igual("com-01", contas.porEmail("JOANA@exemplo.com").id());
            Verifica.igual(null, contas.porEmail("ninguem@exemplo.com"));
        });
    }
}
