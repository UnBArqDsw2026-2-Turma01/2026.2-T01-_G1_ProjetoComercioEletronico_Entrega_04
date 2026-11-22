// Carrinho (UC08): itens agrupados por vendedor, quantidade limitada ao
// estoque (RN-B07) e selecao do que vai para o checkout.

(function () {
  'use strict';

  if (!casca.exigirSessao()) {
    return;
  }
  var el = casca.el;
  var aviso = document.getElementById('aviso');

  function caminhoDoItem(id) {
    return '/api/carrinho/itens/' + encodeURIComponent(id);
  }

  async function alterar(id, corpo) {
    try {
      desenhar(await api.patch(caminhoDoItem(id), corpo));
      aviso.hidden = true;
    } catch (erro) {
      casca.tratarErro(erro, aviso);
    }
  }

  async function remover(id) {
    try {
      desenhar(await api.delete(caminhoDoItem(id)));
      aviso.hidden = true;
    } catch (erro) {
      casca.tratarErro(erro, aviso);
    }
  }

  function contador(item) {
    var menos = el('button', { type: 'button', 'aria-label': 'Diminuir quantidade de ' + item.titulo, texto: '−' });
    var mais = el('button', { type: 'button', 'aria-label': 'Aumentar quantidade de ' + item.titulo, texto: '+' });
    menos.disabled = item.quantidade <= 1;
    mais.disabled = item.quantidade >= item.estoque;
    menos.addEventListener('click', function () { alterar(item.anuncioId, { quantidade: item.quantidade - 1 }); });
    mais.addEventListener('click', function () { alterar(item.anuncioId, { quantidade: item.quantidade + 1 }); });
    return el('span', { classe: 'contador' }, [menos, el('output', { texto: String(item.quantidade) }), mais]);
  }

  function linha(item) {
    var marcar = el('input', { type: 'checkbox', 'aria-label': 'Levar ' + item.titulo + ' no checkout' });
    marcar.checked = item.selecionado;
    marcar.addEventListener('change', function () { alterar(item.anuncioId, { selecionado: marcar.checked }); });
    var retirar = el('button', { type: 'button', classe: 'link-botao perigo', texto: 'Remover' });
    retirar.addEventListener('click', function () { remover(item.anuncioId); });

    return el('div', { classe: 'item-linha' + (item.selecionado ? '' : ' desmarcado') }, [
      marcar,
      el('div', {}, [
        el('div', { classe: 'titulo', texto: item.titulo }),
        el('div', { classe: 'pequeno suave', texto: item.precoUnitario.formatado + ' cada · ' + item.estoque + ' em estoque' })
      ]),
      el('div', { classe: 'valor', texto: item.subtotal.formatado }),
      el('div', { classe: 'acoes' }, [contador(item), retirar])
    ]);
  }

  function desenhar(carrinho) {
    var grupos = document.getElementById('grupos');
    var resumo = document.getElementById('resumo');
    grupos.textContent = '';
    document.getElementById('unidades').textContent = carrinho.unidades
      ? carrinho.unidades + (carrinho.unidades === 1 ? ' unidade' : ' unidades') : '';

    if (!carrinho.grupos.length) {
      resumo.hidden = true;
      grupos.appendChild(el('section', { classe: 'cartao vazio' }, [
        el('h2', { texto: 'Seu carrinho está vazio' }),
        el('p', { texto: 'Busque um produto e adicione pela ficha do anúncio.' }),
        el('a', { classe: 'botao', href: 'busca.html', texto: 'Buscar produtos', style: 'display:inline-block;text-decoration:none;padding-top:.5rem' })
      ]));
      return;
    }

    var selecionados = 0;
    carrinho.grupos.forEach(function (grupo) {
      grupo.itens.forEach(function (item) { if (item.selecionado) { selecionados += item.quantidade; } });
      grupos.appendChild(el('section', { classe: 'cartao grupo-vendedor' }, [
        el('h2', { texto: 'Vendido por ' + grupo.vendedorNome }),
        el('p', { classe: 'pequeno suave', texto: 'Um envio próprio, com frete próprio (RN-B01).' })
      ].concat(grupo.itens.map(linha))));
    });

    resumo.hidden = false;
    document.getElementById('selecionados').textContent = String(selecionados);
    document.getElementById('subtotal').textContent = carrinho.subtotal.formatado;
    var continuar = document.getElementById('continuar');
    continuar.setAttribute('aria-disabled', selecionados ? 'false' : 'true');
    continuar.style.pointerEvents = selecionados ? '' : 'none';
    continuar.style.opacity = selecionados ? '' : '.55';
  }

  api.get('/api/carrinho').then(desenhar).catch(function (erro) { casca.tratarErro(erro, aviso); });
})();
