package comercio.contas.aplicacao;

import comercio.contas.persistencia.RepositorioDeContas;
import comercio.nucleo.RegraDeNegocioException;

/**
 * Ponto pelo qual uma avaliacao altera a reputacao de um vendedor.
 *
 * Quem chama e o Pos-venda, ao registrar uma avaliacao (UC17). A dependencia
 * aponta de Pos-venda para Contas e Acesso, no sentido permitido pela figura
 * 5.2.5 do DAS, e resolve sem ciclo a "dependencia de dado derivado" que o DAS
 * registra como limitacao 5.6.3.
 */
public final class RegistroDeReputacao {

    private final RepositorioDeContas contas;

    public RegistroDeReputacao(RepositorioDeContas contas) {
        this.contas = contas;
    }

    public void registrar(String vendedorId, int nota) {
        if (nota < 1 || nota > 5) {
            throw new RegraDeNegocioException("RN-A05", "A nota da avaliação vai de 1 a 5");
        }
        contas.vendedor(vendedorId).receberAvaliacao(nota);
    }
}
