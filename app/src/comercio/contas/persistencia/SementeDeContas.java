package comercio.contas.persistencia;

import comercio.contas.dominio.Atendente;
import comercio.contas.dominio.CEP;
import comercio.contas.dominio.Comprador;
import comercio.contas.dominio.Endereco;
import comercio.contas.dominio.Reputacao;
import comercio.contas.dominio.TipoVendedor;
import comercio.contas.dominio.Vendedor;

/**
 * Contas iniciais da demonstracao. Deterministica: a mesma execucao produz
 * sempre os mesmos dados, o que torna os testes e o video reproduziveis.
 *
 * Os seis vendedores e as suas reputacoes sao os mesmos do acervo usado na
 * demonstracao do Decorator na Entrega 3, agora como contas de verdade em vez
 * de texto solto dentro de cada anuncio. Todos os nomes sao ficticios.
 */
public final class SementeDeContas {

    public static final String COMPRADORA = "com-01";
    public static final String COMPRADOR_SEM_ENDERECO = "com-02";
    public static final String ATENDENTE = "ate-01";

    private SementeDeContas() {
    }

    public static void popular(RepositorioDeContas contas) {
        contas.salvar(new Vendedor("ven-01", "AudioStore", "contato@audiostore.exemplo",
                TipoVendedor.LOJA_OFICIAL, Reputacao.acumulada(4.8, 1532)));
        contas.salvar(new Vendedor("ven-02", "Carlos Eletro", "carlos@eletro.exemplo",
                TipoVendedor.AUTONOMO, Reputacao.acumulada(4.1, 87)));
        contas.salvar(new Vendedor("ven-03", "Marcia Usados", "marcia@usados.exemplo",
                TipoVendedor.AUTONOMO, Reputacao.acumulada(3.6, 41)));
        contas.salvar(new Vendedor("ven-04", "GameHouse", "vendas@gamehouse.exemplo",
                TipoVendedor.LOJA_OFICIAL, Reputacao.acumulada(4.6, 903)));
        contas.salvar(new Vendedor("ven-05", "FitSound", "ola@fitsound.exemplo",
                TipoVendedor.AUTONOMO, Reputacao.acumulada(4.3, 210)));
        contas.salvar(new Vendedor("ven-06", "AcessoriosBR", "sac@acessoriosbr.exemplo",
                TipoVendedor.AUTONOMO, Reputacao.acumulada(3.9, 64)));

        Comprador compradora = new Comprador(COMPRADORA, "Joana Prado", "joana@exemplo.com");
        compradora.adicionarEndereco(new Endereco("end-01", "Casa", "Quadra 4 Conjunto B", "12",
                "Brasilia", "DF", CEP.de("72230-130")));
        compradora.adicionarEndereco(new Endereco("end-02", "Trabalho", "Avenida Central", "1500",
                "Sao Paulo", "SP", CEP.de("01310-100")));
        contas.salvar(compradora);

        contas.salvar(new Comprador(COMPRADOR_SEM_ENDERECO, "Rui Moraes", "rui@exemplo.com"));
        contas.salvar(new Atendente(ATENDENTE, "Central de Atendimento", "atendimento@exemplo.com"));
    }
}
