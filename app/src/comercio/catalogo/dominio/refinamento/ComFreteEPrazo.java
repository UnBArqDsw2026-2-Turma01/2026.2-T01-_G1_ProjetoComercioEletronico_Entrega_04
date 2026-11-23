package comercio.catalogo.dominio.refinamento;

import java.util.ArrayList;
import java.util.List;

import comercio.catalogo.dominio.ConsultaCatalogo;
import comercio.catalogo.dominio.Cotacao;
import comercio.catalogo.dominio.FonteDeResultados;
import comercio.catalogo.dominio.IFrete;
import comercio.catalogo.dominio.ItemListagem;
import comercio.catalogo.dominio.Listagem;
import comercio.contas.dominio.CEP;

/**
 * Decorador concreto (ConcreteDecorator, GAMMA et al., 1995): acrescenta a cada
 * item os selos de frete e de prazo de entrega.
 *
 * Origem: RN-A04 da Entrega 1 — frete e prazo dependem do CEP e sao calculados
 * ainda na listagem, antes do carrinho. Foi esse achado que, no Diagrama de
 * Componentes da Entrega 2, obrigou o Servico de Catalogo a requerer IFrete do
 * Servico de Entrega: mostrar o frete cedo reduz o abandono no fim do funil,
 * mas empurra uma dependencia para o componente mais consultado do sistema.
 *
 * No Diagrama de Sequencia da Entrega 2, o calculo esta no fragmento opt das
 * mensagens 30 a 35, condicionado a "informa CEP". Aqui a condicao e a mesma:
 * sem CEP na consulta, nenhum selo e acrescentado, e a listagem atravessa este
 * decorador sem alteracao. E a traducao direta do «extend» para codigo.
 *
 * Este e um decorador de *enriquecimento*: nao remove nem reordena item nenhum,
 * so acrescenta informacao a cada um. Por isso a sua posicao na cadeia e
 * indiferente para o conjunto de resultados, mas nao para o custo: colocado
 * depois de ComFiltroDeFacetas, calcula frete apenas dos itens que sobraram.
 *
 * Na Entrega 3 a tabela de frete ficava dentro desta classe. Na Entrega 4 o
 * decorador passou a perguntar o frete a porta IFrete, que e o que o Diagrama
 * de Componentes sempre disse que deveria acontecer: a dependencia que a
 * Entrega 2 desenhou agora existe no codigo. A tabela foi para o simulador da
 * transportadora. Outra consequencia: o CEP deixa de ser texto livre e passa a
 * ser o value object CEP, de modo que um CEP invalido e recusado (RN-B04) em
 * vez de gerar um frete sem sentido.
 *
 * Nao altera a listagem recebida: devolve uma copia nova via comItens.
 *
 * Autor: Patrick Anderson Carvalho dos Santos (refinamento ComFreteEPrazo).
 */
public final class ComFreteEPrazo extends RefinamentoDecorator {

    public static final String SELO_FRETE = "frete";
    public static final String SELO_PRAZO = "prazo";

    private final IFrete frete;

    public ComFreteEPrazo(FonteDeResultados origem, IFrete frete) {
        super(origem);
        if (frete == null) {
            throw new IllegalArgumentException("ComFreteEPrazo precisa da porta IFrete");
        }
        this.frete = frete;
    }

    @Override
    public Listagem buscar(ConsultaCatalogo consulta) {
        Listagem recebida = super.buscar(consulta);

        // RN-A04 so se aplica quando ha CEP. Sem ele, nada a acrescentar: o
        // fragmento opt do Diagrama de Sequencia simplesmente nao executa.
        String informado = consulta.cep();
        if (informado == null || informado.trim().isEmpty()) {
            return recebida;
        }

        CEP destino = CEP.de(informado);
        List<ItemListagem> enriquecidos = new ArrayList<ItemListagem>(recebida.total());
        for (ItemListagem item : recebida.itens()) {
            Cotacao cotacao = frete.cotar(destino, item.anuncio());
            enriquecidos.add(item
                    .comSelo(SELO_FRETE, cotacao.valor().formatado())
                    .comSelo(SELO_PRAZO, cotacao.prazoDias() + " dia(s) útil(eis)"));
        }
        return recebida.comItens(enriquecidos);
    }
}
