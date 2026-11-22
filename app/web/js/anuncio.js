// Ficha do anuncio (UC05) com a reputacao do vendedor (UC06), o frete por CEP
// (UC07, RN-A04) e as outras ofertas do mesmo produto (RN-B09). Os botoes
// levam ao carrinho (UC08) e ao "comprar agora" (UC11), rotas da Transacao.
//
// A pagina e enderecada por anuncio.html?id=<anuncio>[&cep=<cep>]: o CEP
// tambem fica na URL, entao a ficha com frete pode ser compartilhada.

(function () {
  'use strict';

  var el = casca.el;
  var aviso = document.getElementById('aviso');
  var avisoCompra = document.getElementById('aviso-compra');
  var campoCep = document.getElementById('cep');
  var id = casca.parametro('id');
  var ficha = null;

  // Os identificadores de categoria vem sem acento do servidor; a tela exibe o nome com acento.
  var CATEGORIAS = { Eletronicos: 'Eletrônicos', Informatica: 'Informática', Acessorios: 'Acessórios' };

  function tipoDoVendedor(tipo) {
    return tipo === 'LOJA_OFICIAL' ? 'Loja oficial' : 'Vendedor autônomo';
  }

  function texto(idDoElemento, valor) {
    document.getElementById(idDoElemento).textContent = valor;
  }

  async function carregar() {
    if (!id) {
      aviso.hidden = false;
      aviso.className = 'aviso erro';
      aviso.textContent = 'Nenhum anúncio informado. Volte para a busca e escolha um produto.';
      return;
    }
    var cep = casca.parametro('cep');
    campoCep.value = cep || '';
    var caminho = '/api/anuncios/' + encodeURIComponent(id);
    try {
      ficha = await api.get(caminho + (cep ? '?cep=' + encodeURIComponent(cep) : ''));
      aviso.hidden = true;
      desenhar(ficha);
    } catch (erro) {
      if (erro.codigo !== 'RN-B04') {
        casca.tratarErro(erro, aviso);
        return;
      }
      // CEP recusado pelo servidor (RN-B04): a ficha continua util, so sem frete.
      try {
        ficha = await api.get(caminho);
        aviso.hidden = true;
        desenhar(ficha);
        var resultado = document.getElementById('frete-resultado');
        resultado.className = 'pequeno indisponivel';
        resultado.textContent = erro.message + ' (RN-B04)';
      } catch (outro) {
        casca.tratarErro(outro, aviso);
      }
    }
  }

  function desenhar(f) {
    document.title = f.titulo + ' · G1_ProjetoComercioEletronico';
    texto('categoria', CATEGORIAS[f.categoria] || f.categoria);
    texto('titulo', f.titulo);
    texto('condicao', (f.condicao === 'USADO' ? 'Usado' : 'Novo') + ' · anúncio ' + f.id);
    texto('preco', f.preco);

    var p = f.parcelamento;
    var parcelas = document.getElementById('parcelamento');
    parcelas.textContent = '';
    parcelas.appendChild(document.createTextNode('em até '));
    parcelas.appendChild(el('strong', { texto: p.vezes + 'x de ' + p.maiorParcela }));
    parcelas.appendChild(document.createTextNode(' sem juros'));
    if (p.maiorParcela !== p.menorParcela) {
      parcelas.appendChild(el('span', { classe: 'pequeno suave', texto: ' (as últimas de ' + p.menorParcela + ')' }));
    }

    var imagens = document.getElementById('imagens');
    imagens.textContent = '';
    (f.imagens.length ? f.imagens : [null]).forEach(function (src, i) {
      imagens.appendChild(src
        ? el('img', { src: src, alt: i === 0 ? f.titulo : '', width: '240', height: '240' })
        : el('div', { classe: 'sem-imagem', texto: 'Sem imagem' }));
    });

    var disponivel = document.getElementById('disponibilidade');
    disponivel.className = 'pequeno ' + (f.disponivel ? 'disponivel' : 'indisponivel');
    disponivel.textContent = f.disponivel
      ? (f.estoque === 1 ? 'Última unidade disponível' : f.estoque + ' unidades disponíveis')
      : 'Este anúncio não está disponível para compra no momento.';
    document.getElementById('adicionar').disabled = !f.disponivel;
    var comprar = document.getElementById('comprar');
    comprar.href = 'checkout.html?anuncio=' + encodeURIComponent(f.id) + '&quantidade=1';
    comprar.setAttribute('aria-disabled', f.disponivel ? 'false' : 'true');
    comprar.classList.toggle('desativado', !f.disponivel);

    texto('vendedor-nome', f.vendedor.nome);
    texto('vendedor-tipo', tipoDoVendedor(f.vendedor.tipo));
    texto('vendedor-nota', '★ ' + f.vendedor.nota + ' de 5');
    texto('vendedor-avaliacoes', '(' + f.vendedor.avaliacoes.toLocaleString('pt-BR') + ' avaliações)');

    var resultado = document.getElementById('frete-resultado');
    if (f.frete) {
      resultado.className = 'frete-ok';
      resultado.textContent = '';
      resultado.appendChild(el('strong', { texto: 'Frete ' + f.frete }));
      resultado.appendChild(document.createTextNode(' · chega em até ' + f.prazoDias + ' dia(s) útil(eis)'));
    } else {
      resultado.className = 'pequeno suave';
      resultado.textContent = 'Informe o CEP para ver o frete antes de comprar (RN-A04).';
    }

    var ofertas = document.getElementById('ofertas');
    ofertas.textContent = '';
    if (!f.outrasOfertas.length) {
      ofertas.appendChild(el('p', { classe: 'suave pequeno', texto: 'Nenhum outro vendedor oferece este produto.' }));
    } else {
      var cep = casca.parametro('cep');
      ofertas.appendChild(el('ul', { classe: 'ofertas' }, f.outrasOfertas.map(function (o) {
        var link = 'anuncio.html?id=' + encodeURIComponent(o.id) + (cep ? '&cep=' + encodeURIComponent(cep) : '');
        return el('li', {}, [
          el('a', { href: link }, [
            el('span', { classe: 'oferta-vendedor', texto: o.vendedor }),
            el('span', { classe: 'pequeno suave', texto: o.condicao === 'USADO' ? 'Usado' : 'Novo' }),
            el('span', { classe: 'oferta-preco', texto: o.preco })
          ])
        ]);
      })));
    }

    document.getElementById('ficha').hidden = false;
  }

  // UC07: o CEP vai para a URL (substituindo a entrada do historico) e a ficha e pedida de novo.
  document.getElementById('form-frete').addEventListener('submit', function (evento) {
    evento.preventDefault();
    var params = new URLSearchParams(window.location.search);
    var cep = campoCep.value.trim();
    if (cep) {
      params.set('cep', cep);
    } else {
      params.delete('cep');
    }
    history.replaceState(null, '', 'anuncio.html?' + params.toString());
    carregar();
  });

  // UC08: o carrinho e do comprador logado; sem sessao, vai ao login e volta para esta ficha.
  document.getElementById('adicionar').addEventListener('click', async function () {
    if (!ficha || !casca.exigirSessao()) {
      return;
    }
    var botao = this;
    botao.disabled = true;
    try {
      await api.post('/api/carrinho/itens', { anuncioId: ficha.id, quantidade: 1 });
      window.location.assign('carrinho.html');
    } catch (erro) {
      botao.disabled = !ficha.disponivel;
      casca.tratarErro(erro, avisoCompra);
    }
  });

  document.getElementById('comprar').addEventListener('click', function (evento) {
    if (ficha && !ficha.disponivel) {
      evento.preventDefault();
    }
  });

  // Voltar: se veio da busca, volta para ela com os mesmos refinamentos (RN-A06).
  document.getElementById('voltar').addEventListener('click', function (evento) {
    if (document.referrer && document.referrer.indexOf('/busca.html') >= 0 && history.length > 1) {
      evento.preventDefault();
      history.back();
    }
  });

  carregar();
})();
