package comercio.catalogo;

import java.util.ArrayList;
import java.util.List;

import comercio.catalogo.aplicacao.PedidoDeBusca;
import comercio.catalogo.dominio.Anuncio;
import comercio.catalogo.dominio.ConsultaCatalogo;
import comercio.catalogo.dominio.Cotacao;
import comercio.catalogo.dominio.FonteDeResultados;
import comercio.catalogo.dominio.IFrete;
import comercio.catalogo.dominio.ItemListagem;
import comercio.catalogo.dominio.Listagem;
import comercio.catalogo.dominio.Ordenacao;
import comercio.catalogo.dominio.refinamento.ComFreteEPrazo;
import comercio.catalogo.persistencia.CatalogoEmMemoria;
import comercio.contas.dominio.CEP;
import comercio.externos.TransportadoraSimulada;
import comercio.nucleo.Dinheiro;
import comercio.nucleo.RegraDeNegocioException;
import comercio.teste.Casos;
import comercio.teste.Suite;
import comercio.teste.Verifica;
import comercio.transacao.aplicacao.ServicoDeEntrega;

public final class FreteTeste implements Suite {

    /** Porta falsa: registra o que foi pedido e responde sempre o mesmo. */
    private static final class FreteFixo implements IFrete {
        final List<String> pedidos = new ArrayList<String>();

        @Override
        public Cotacao cotar(CEP destino, Anuncio anuncio) {
            pedidos.add(destino.digitos() + ":" + anuncio.titulo());
            return new Cotacao(Dinheiro.emReais(1234), 3);
        }
    }

    @Override
    public void registrar(Casos c) {
        c.caso("RN-A04: o frete e calculado sem existir pedido nem envio", () -> {
            IFrete servico = new ServicoDeEntrega(new TransportadoraSimulada());
            Anuncio fone = new CatalogoEmMemoria().buscar(new ConsultaCatalogo("fone")).itens().get(0).anuncio();
            Cotacao cotacao = servico.cotar(CEP.de("72230-130"), fone);
            // 250 g cai na faixa leve (R$ 14,90); CEP iniciado em 7 acrescenta 7 x R$ 3,50.
            Verifica.igual(Dinheiro.emReais(3940), cotacao.valor());
            Verifica.igual(9, cotacao.prazoDias());
        });

        c.caso("RN-A04: faixas de peso da transportadora simulada", () -> {
            TransportadoraSimulada transportadora = new TransportadoraSimulada();
            CEP destino = CEP.de("01310-100");
            Verifica.igual(Dinheiro.emReais(1490), transportadora.cotar(destino, 500).valor());
            Verifica.igual(Dinheiro.emReais(2490), transportadora.cotar(destino, 501).valor());
            Verifica.igual(Dinheiro.emReais(3990), transportadora.cotar(destino, 2001).valor());
            Verifica.igual(2, transportadora.cotar(destino, 500).prazoDias());
        });

        c.caso("RN-A04: sem CEP o decorador nao acrescenta selo nem consulta a porta", () -> {
            FreteFixo porta = new FreteFixo();
            FonteDeResultados busca = new ComFreteEPrazo(new CatalogoEmMemoria(), porta);
            Listagem listagem = busca.buscar(new ConsultaCatalogo("fone de ouvido"));
            Verifica.verdadeiro(listagem.total() > 0, "a busca deve trazer resultados");
            for (ItemListagem item : listagem.itens()) {
                Verifica.falso(item.selos().containsKey(ComFreteEPrazo.SELO_FRETE), "sem CEP nao ha selo de frete");
            }
            Verifica.igual(0, porta.pedidos.size());
        });

        c.caso("RN-A04: com CEP cada item ganha frete e prazo vindos da porta IFrete", () -> {
            FreteFixo porta = new FreteFixo();
            FonteDeResultados busca = new ComFreteEPrazo(new CatalogoEmMemoria(), porta);
            Listagem listagem = busca.buscar(new ConsultaCatalogo("fone de ouvido").comCep("72230-130"));
            Verifica.igual(listagem.total(), porta.pedidos.size());
            for (ItemListagem item : listagem.itens()) {
                Verifica.igual("R$ 12,34", item.selos().get(ComFreteEPrazo.SELO_FRETE));
                Verifica.igual("3 dia(s) util(eis)", item.selos().get(ComFreteEPrazo.SELO_PRAZO));
            }
            Verifica.verdadeiro(porta.pedidos.get(0).startsWith("72230130:"), "a porta recebe o CEP normalizado");
        });

        c.caso("RN-B04: CEP invalido na busca e recusado em vez de gerar frete sem sentido", () -> {
            FonteDeResultados busca = new ComFreteEPrazo(new CatalogoEmMemoria(), new FreteFixo());
            RegraDeNegocioException recusa = Verifica.lanca(RegraDeNegocioException.class,
                    () -> busca.buscar(new ConsultaCatalogo("fone").comCep("123")));
            Verifica.igual("RN-B04", recusa.codigo());
        });

        c.caso("o decorador de frete aparece na descricao da cadeia", () -> {
            FonteDeResultados busca = new ComFreteEPrazo(new CatalogoEmMemoria(), new FreteFixo());
            Verifica.contem(busca.descricao(), "CatalogoEmMemoria");
            Verifica.contem(busca.descricao(), "+ ComFreteEPrazo");
        });

        c.caso("RN-A06: o pedido de busca e reconstruido a partir da URL da consulta", () -> {
            ConsultaCatalogo consulta = new ConsultaCatalogo("fone de ouvido").comFiltro("condicao", "NOVO")
                    .comOrdenacao(Ordenacao.MENOR_PRECO).comCep("72230-130").naPagina(2);
            PedidoDeBusca pedido = PedidoDeBusca.deURL(consulta.paraURL());
            Verifica.igual("fone de ouvido", pedido.termo());
            Verifica.igual("NOVO", pedido.filtros().get("condicao"));
            Verifica.igual("MENOR_PRECO", pedido.ordem());
            Verifica.igual("72230-130", pedido.cep());
            Verifica.igual(2, pedido.pagina());
        });

        c.caso("pedido de busca: valores ausentes assumem o padrao", () -> {
            PedidoDeBusca pedido = new PedidoDeBusca(null, null, " ", "", 0);
            Verifica.igual("", pedido.termo());
            Verifica.igual("RELEVANCIA", pedido.ordem());
            Verifica.igual(null, pedido.cep());
            Verifica.igual(1, pedido.pagina());
        });
    }
}
