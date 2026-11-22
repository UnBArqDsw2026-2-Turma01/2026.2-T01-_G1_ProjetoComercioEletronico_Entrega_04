package comercio.transacao.aplicacao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import comercio.catalogo.aplicacao.IAnuncio;
import comercio.catalogo.dominio.Anuncio;
import comercio.catalogo.dominio.Cotacao;
import comercio.contas.dominio.Endereco;
import comercio.contas.persistencia.RepositorioDeContas;
import comercio.nucleo.AcessoNegadoException;
import comercio.nucleo.RegraDeNegocioException;
import comercio.transacao.dominio.ICotacaoEntrega;
import comercio.transacao.dominio.Relogio;
import comercio.transacao.dominio.carrinho.Carrinho;
import comercio.transacao.dominio.carrinho.ItemCarrinho;
import comercio.transacao.dominio.pedido.Envio;
import comercio.transacao.dominio.pedido.ItemPedido;
import comercio.transacao.dominio.pedido.Pedido;
import comercio.transacao.dominio.pedido.StatusPedido;
import comercio.transacao.dominio.pedido.TransicaoInvalidaException;
import comercio.transacao.persistencia.RepositorioDeCarrinhos;
import comercio.transacao.persistencia.RepositorioDePedidos;

/**
 * Servico de Checkout e Pedidos: fornece ICheckout (UC10 e UC11).
 *
 * E o componente de maior acoplamento do sistema, como o DAS antecipa na secao
 * 4.6.2: precisa do carrinho, do endereco da conta, do estoque dos anuncios e
 * do frete. Cada um chega por uma dependencia entregue no construtor.
 *
 * Fechamento tudo ou nada. O checkout reserva o estoque de todos os itens
 * antes de criar o pedido. Se qualquer passo falhar (um item sem estoque, a
 * transportadora indisponivel), tudo o que ja foi reservado e devolvido e
 * nenhum pedido passa a existir. E o que o ADR-05 do DAS chama de fechamento
 * atomico; aqui a atomicidade e garantida em memoria, por compensacao, e nao
 * por transacao de banco de dados.
 */
public final class ServicoDeCheckout implements ICheckout {

    /** Um anuncio e a quantidade que vai virar item de pedido. */
    private static final class Linha {
        final Anuncio anuncio;
        final int quantidade;

        Linha(Anuncio anuncio, int quantidade) {
            this.anuncio = anuncio;
            this.quantidade = quantidade;
        }
    }

    private final RepositorioDeCarrinhos carrinhos;
    private final RepositorioDePedidos pedidos;
    private final RepositorioDeContas contas;
    private final IAnuncio anuncios;
    private final ICotacaoEntrega transportadora;
    private final Relogio relogio;

    public ServicoDeCheckout(RepositorioDeCarrinhos carrinhos, RepositorioDePedidos pedidos,
            RepositorioDeContas contas, IAnuncio anuncios, ICotacaoEntrega transportadora, Relogio relogio) {
        this.carrinhos = carrinhos;
        this.pedidos = pedidos;
        this.contas = contas;
        this.anuncios = anuncios;
        this.transportadora = transportadora;
        this.relogio = relogio;
    }

    @Override
    public synchronized PedidoDTO finalizar(String compradorId, String enderecoId) {
        Carrinho carrinho = carrinhos.de(compradorId);
        List<ItemCarrinho> selecionados = carrinho.selecionados();
        if (selecionados.isEmpty()) {
            throw new RegraDeNegocioException("RF-B02", "Selecione ao menos um item do carrinho para continuar");
        }
        // UC10 inclui UC09: sem endereco da conta nao ha compra (RN-B02).
        Endereco destino = contas.comprador(compradorId).endereco(enderecoId);

        List<Linha> linhas = new ArrayList<Linha>();
        for (ItemCarrinho item : selecionados) {
            linhas.add(new Linha(item.anuncio(), item.quantidade()));
        }
        Pedido pedido = criarPedido(compradorId, destino, linhas);
        carrinho.removerSelecionados();
        return MontadorDePedidoDTO.montar(pedido, pedidos);
    }

    @Override
    public synchronized PedidoDTO comprarAgora(String compradorId, String anuncioId, int quantidade,
            String enderecoId) {
        // RN-B08: o caminho direto nao passa pelo carrinho, e nao o altera.
        Endereco destino = contas.comprador(compradorId).endereco(enderecoId);
        List<Linha> linhas = new ArrayList<Linha>();
        linhas.add(new Linha(anuncios.obter(anuncioId), quantidade));
        return MontadorDePedidoDTO.montar(criarPedido(compradorId, destino, linhas), pedidos);
    }

    @Override
    public synchronized PedidoDTO desistir(String compradorId, String pedidoId) {
        Pedido pedido = pedidos.obter(pedidoId);
        if (!pedido.compradorId().equals(compradorId)) {
            throw new AcessoNegadoException("O pedido " + pedidoId + " é de outro comprador");
        }
        if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
            throw new TransicaoInvalidaException("O pedido " + pedidoId + " está "
                    + pedido.getStatus().rotulo().toLowerCase() + ": depois de pago, só é desfeito por reembolso");
        }
        pedido.cancelar();
        for (ItemPedido item : pedido.itens()) {
            anuncios.devolver(item.anuncioId(), item.quantidade());
        }
        pedido.registrarEvento("Comprador desistiu do pedido; estoque devolvido");
        return MontadorDePedidoDTO.montar(pedido, pedidos);
    }

    private Pedido criarPedido(String compradorId, Endereco destino, List<Linha> linhas) {
        List<Linha> reservadas = new ArrayList<Linha>();
        try {
            for (Linha linha : linhas) {
                anuncios.reservar(linha.anuncio.id(), linha.quantidade);
                reservadas.add(linha);
            }
            List<Envio> envios = montarEnvios(destino, linhas);
            Pedido pedido = new Pedido(pedidos.proximoId(), compradorId, envios, relogio.minutoAtual());
            pedidos.salvar(pedido);
            return pedido;
        } catch (RuntimeException falha) {
            // Compensacao: nada do que foi reservado fica preso a um pedido que nao existe.
            for (Linha reservada : reservadas) {
                anuncios.devolver(reservada.anuncio.id(), reservada.quantidade);
            }
            throw falha;
        }
    }

    /** RN-B01 e RF-B01: um envio por vendedor, cada um com o seu frete. */
    private List<Envio> montarEnvios(Endereco destino, List<Linha> linhas) {
        Map<String, List<Linha>> porVendedor = new LinkedHashMap<String, List<Linha>>();
        for (Linha linha : linhas) {
            String vendedorId = linha.anuncio.vendedor().id();
            List<Linha> grupo = porVendedor.get(vendedorId);
            if (grupo == null) {
                grupo = new ArrayList<Linha>();
                porVendedor.put(vendedorId, grupo);
            }
            grupo.add(linha);
        }

        List<Envio> envios = new ArrayList<Envio>();
        for (Map.Entry<String, List<Linha>> grupo : porVendedor.entrySet()) {
            List<ItemPedido> itens = new ArrayList<ItemPedido>();
            int pesoEmGramas = 0;
            String vendedorNome = "";
            for (Linha linha : grupo.getValue()) {
                vendedorNome = linha.anuncio.vendedor().nome();
                pesoEmGramas += linha.anuncio.pesoEmGramas() * linha.quantidade;
                // O preco e copiado agora: o pedido nao acompanha mudancas posteriores do anuncio.
                itens.add(new ItemPedido(linha.anuncio.id(), linha.anuncio.titulo(), grupo.getKey(),
                        linha.quantidade, linha.anuncio.preco()));
            }
            Cotacao cotacao = transportadora.cotar(destino.cep(), pesoEmGramas);
            envios.add(new Envio(grupo.getKey(), vendedorNome, destino, itens, cotacao.valor(), cotacao.prazoDias()));
        }
        return envios;
    }
}
