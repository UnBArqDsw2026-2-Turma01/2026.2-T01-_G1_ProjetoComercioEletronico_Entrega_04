// Lista de pedidos do comprador da sessao (UC13), do mais recente para o mais antigo.

(function () {
  'use strict';

  if (!casca.exigirSessao()) {
    return;
  }
  var el = casca.el;

  function descricao(pedido) {
    var itens = 0;
    pedido.envios.forEach(function (e) { e.itens.forEach(function (i) { itens += i.quantidade; }); });
    return itens + (itens === 1 ? ' item' : ' itens') + ' · ' + pedido.envios.length
      + (pedido.envios.length === 1 ? ' envio' : ' envios');
  }

  api.get('/api/pedidos').then(function (dados) {
    var lista = document.getElementById('lista');
    lista.textContent = '';
    if (!dados.pedidos.length) {
      lista.appendChild(el('li', { classe: 'vazio' }, [
        el('p', { texto: 'Você ainda não fez nenhum pedido.' }),
        el('a', { href: 'carrinho.html', texto: 'Ir para o carrinho' })
      ]));
      return;
    }
    dados.pedidos.forEach(function (p) {
      lista.appendChild(el('li', {}, [el('a', { href: 'pedido.html?id=' + encodeURIComponent(p.id) }, [
        el('span', { classe: 'id', texto: p.id }),
        el('span', { classe: 'valor', texto: p.total.formatado }),
        el('span', { classe: 'pequeno suave', texto: descricao(p) }),
        el('span', { style: 'text-align:right' }, [el('span', { classe: 'situacao ' + p.status, texto: p.statusRotulo })])
      ])]));
    });
  }).catch(function (erro) { casca.tratarErro(erro, document.getElementById('aviso')); });
})();
