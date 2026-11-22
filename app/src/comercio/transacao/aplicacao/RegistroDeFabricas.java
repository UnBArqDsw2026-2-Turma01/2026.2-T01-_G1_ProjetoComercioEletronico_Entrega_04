package comercio.transacao.aplicacao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import comercio.nucleo.RegraDeNegocioException;
import comercio.transacao.dominio.pagamento.FabricaMeioPagamento;
import comercio.transacao.dominio.pagamento.MeioPagamento;

/**
 * As familias de pagamento que a aplicacao oferece, indexadas pelo meio.
 *
 * E o unico ponto em que a escolha do comprador (um valor de MeioPagamento)
 * vira uma fabrica. A partir dai o codigo de pagamento trabalha so com as
 * interfaces do Abstract Factory.
 *
 * Na Entrega 3 esta classe se chamava CatalogoDeFabricas e localizava as
 * fabricas pelo nome da classe, via reflexao, para permitir que cada
 * integrante entregasse a sua familia em momento diferente. Com as quatro
 * familias entregues, a reflexao deixou de ter funcao e escondia do
 * compilador uma dependencia real. Agora quem conhece as classes concretas e a
 * raiz de composicao, que as registra aqui: acrescentar um meio de pagamento e
 * escrever uma familia nova e registra-la, sem tocar em ServicoDePagamentos
 * (ADR-02 do DAS).
 *
 * Autor: Patrick Anderson Carvalho dos Santos (cliente do criacional).
 */
public final class RegistroDeFabricas {

    private final Map<MeioPagamento, FabricaMeioPagamento> fabricas =
            new LinkedHashMap<MeioPagamento, FabricaMeioPagamento>();

    public RegistroDeFabricas registrar(FabricaMeioPagamento fabrica) {
        if (fabrica == null) {
            throw new IllegalArgumentException("Fabrica ausente");
        }
        if (fabricas.containsKey(fabrica.meio())) {
            throw new IllegalStateException("Ja existe uma familia registrada para " + fabrica.meio());
        }
        fabricas.put(fabrica.meio(), fabrica);
        return this;
    }

    public boolean disponivel(MeioPagamento meio) {
        return fabricas.containsKey(meio);
    }

    public FabricaMeioPagamento para(MeioPagamento meio) {
        FabricaMeioPagamento fabrica = fabricas.get(meio);
        if (fabrica == null) {
            throw new RegraDeNegocioException("UC24", "O meio de pagamento " + meio.rotulo() + " não está disponível");
        }
        return fabrica;
    }

    /** Converte o texto recebido da tela no meio de pagamento correspondente. */
    public MeioPagamento meioDe(String nome) {
        if (nome != null) {
            for (MeioPagamento meio : MeioPagamento.values()) {
                if (meio.name().equalsIgnoreCase(nome.trim())) {
                    return meio;
                }
            }
        }
        throw new RegraDeNegocioException("UC24", "Meio de pagamento desconhecido: " + nome);
    }

    /** As fabricas registradas, na ordem de registro. */
    public List<FabricaMeioPagamento> todas() {
        return new ArrayList<FabricaMeioPagamento>(fabricas.values());
    }
}
