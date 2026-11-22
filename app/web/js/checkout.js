// Checkout (UC09, UC10, UC11, UC12, UC24): escolhe o endereco, cria o pedido
// e paga. Com ?anuncio=<id>&quantidade=<n> faz o "comprar agora" (RN-B08),
// sem passar pelo carrinho; e o que o botao da ficha do anuncio chama.

(function () {
  'use strict';

  if (!casca.exigirSessao()) {
    return;
  }
  var el = casca.el;
  var aviso = document.getElementById('aviso');
  var anuncio = casca.parametro('anuncio');
  var quantidade = parseInt(casca.parametro('quantidade') || '1', 10) || 1;
  var pedido = null;

  function opcao(nome, valor, titulo, detalhe, marcada) {
    var entrada = el('input', { type: 'radio', name: nome, value: valor });
    entrada.checked = !!marcada;
    return el('label', { classe: 'opcao-cartao' }, [entrada, el('strong', { texto: titulo }), el('span', { texto: detalhe })]);
  }

  function escolhido(nome) {
    var marcado = document.querySelector('input[name="' + nome + '"]:checked');
    return marcado ? marcado.value : null;
  }

  async function carregarEnderecos() {
    var conta = await api.get('/api/conta');
    var lista = document.getElementById('enderecos');
    lista.querySelectorAll('p, label').forEach(function (n) { n.remove(); });
    if (!conta.enderecos.length) {
      lista.appendChild(el('p', { classe: 'aviso', texto: 'Esta conta não tem endereço de entrega cadastrado (RN-B02). Entre com a conta joana@exemplo.com para testar a compra.' }));
      document.getElementById('criar').disabled = true;
      return;
    }
    conta.enderecos.forEach(function (e, i) {
      lista.appendChild(opcao('endereco', e.id, e.apelido, e.resumo + ' · CEP ' + e.cep, i === 0));
    });
    document.getElementById('origem').textContent = anuncio
      ? 'Comprar agora: ' + quantidade + ' unidade(s) do anúncio ' + anuncio + ', sem passar pelo carrinho.'
      : 'Vão para o pedido os itens selecionados no carrinho.';
  }

  async function carregarMeios() {
    var dados = await api.get('/api/meios-de-pagamento');
    var lista = document.getElementById('meios');
    dados.meios.forEach(function (m, i) {
      var prazo = m.assincrono ? 'confirmação posterior' : 'aprovação na hora';
      lista.appendChild(opcao('meio', m.meio, m.rotulo, m.familia + ' · ' + prazo + ' · ' + m.classesDoTrio.join(', '), i === 0));
    });
  }

  function resumoDoPedido(p) {
    var linhas = p.envios.map(function (envio) {
      return el('li', { texto: envio.vendedorNome + ': ' + envio.itens.map(function (i) {
        return i.quantidade + '× ' + i.titulo;
      }).join(', ') + ' · frete ' + envio.frete.formatado + ' · ' + envio.prazoDias + ' dia(s)' });
    });
    return el('div', {}, [
      el('ul', { classe: 'registro', style: 'color:var(--texto)' }, linhas),
      el('p', { classe: 'preco', style: 'margin-top:.75rem', texto: 'Total ' + p.total.formatado }),
      el('p', { classe: 'pequeno suave', texto: 'Estoque reservado. O pedido está em "' + p.statusRotulo + '".' })
    ]);
  }

  document.getElementById('form-endereco').addEventListener('submit', async function (evento) {
    evento.preventDefault();
    var endereco = escolhido('endereco');
    if (!endereco) {
      return;
    }
    var corpo = { enderecoId: endereco };
    if (anuncio) {
      corpo.anuncioId = anuncio;
      corpo.quantidade = quantidade;
    }
    try {
      pedido = await api.post('/api/pedidos', corpo);
      aviso.hidden = true;
      document.getElementById('etapa-endereco').hidden = true;
      document.getElementById('etapa-pagamento').hidden = false;
      document.getElementById('passo-endereco').removeAttribute('aria-current');
      document.getElementById('passo-pagamento').setAttribute('aria-current', 'step');
      document.getElementById('pedido-id').textContent = pedido.id;
      document.getElementById('depois').href = 'pedido.html?id=' + encodeURIComponent(pedido.id);
      var resumo = document.getElementById('pedido-resumo');
      resumo.textContent = '';
      resumo.appendChild(resumoDoPedido(pedido));
      await carregarMeios();
    } catch (erro) {
      casca.tratarErro(erro, aviso);
    }
  });

  document.getElementById('form-pagamento').addEventListener('submit', async function (evento) {
    evento.preventDefault();
    try {
      await api.post('/api/pedidos/' + encodeURIComponent(pedido.id) + '/pagamento', { meio: escolhido('meio') });
      window.location.assign('pedido.html?id=' + encodeURIComponent(pedido.id));
    } catch (erro) {
      casca.tratarErro(erro, aviso);
    }
  });

  carregarEnderecos().catch(function (erro) { casca.tratarErro(erro, aviso); });
})();
