// Busca (UC01) com sugestoes ao digitar (UC02), facetas com contagem (UC03,
// RN-A03), ordenacao (UC04), frete por CEP (RN-A04), paginacao (UC30) e
// termos alternativos quando nada e encontrado (RN-A01).
//
// RN-A06: o estado da busca vive na URL da pagina. Toda mudanca grava a URL
// (history.pushState) e a listagem e sempre desenhada a partir dela; por isso
// recarregar, colar o endereco em outra aba ou usar o botao voltar reproduz
// a mesma listagem.

(function () {
  'use strict';

  var el = casca.el;
  var PARAMETROS = ['termo', 'categoria', 'condicao', 'ordem', 'cep', 'pagina'];
  var ROTULOS_DE_FACETA = { categoria: 'Categoria', condicao: 'Condição' };
  // Os identificadores vem sem acento do servidor; a tela exibe o nome com acento.
  var ROTULOS_DE_OPCAO = {
    NOVO: 'Novo', USADO: 'Usado',
    Eletronicos: 'Eletrônicos', Informatica: 'Informática', Acessorios: 'Acessórios'
  };
  var ESPERA_DAS_SUGESTOES = 250;

  var formulario = document.getElementById('refino');
  var campoTermo = document.getElementById('termo');
  var campoCep = document.getElementById('cep');
  var campoOrdem = document.getElementById('ordem');
  var listaSugestoes = document.getElementById('sugestoes');
  var aviso = document.getElementById('aviso');

  var buscaAtual = 0;
  var sugestaoAtual = 0;
  var esperaSugestoes = null;
  var destacada = -1;

  // ------------------------------------------------------------- estado na URL

  function estadoDaUrl() {
    var params = new URLSearchParams(window.location.search);
    var estado = {};
    PARAMETROS.forEach(function (nome) {
      var valor = params.get(nome);
      if (valor !== null && valor.trim() !== '') {
        estado[nome] = valor.trim();
      }
    });
    return estado;
  }

  function consultaDe(estado) {
    var params = new URLSearchParams();
    PARAMETROS.forEach(function (nome) {
      if (estado[nome] !== undefined && estado[nome] !== '') {
        params.set(nome, estado[nome]);
      }
    });
    return params.toString();
  }

  // Grava o novo estado na URL e desenha a busca a partir dela.
  function navegar(estado) {
    var consulta = consultaDe(estado);
    var novaUrl = 'busca.html' + (consulta ? '?' + consulta : '');
    if (novaUrl !== 'busca.html' + window.location.search) {
      history.pushState(null, '', novaUrl);
    }
    carregar();
  }

  // Muda uma parte do estado. Qualquer refinamento volta para a pagina 1.
  function refinar(mudancas) {
    var estado = estadoDaUrl();
    Object.keys(mudancas).forEach(function (nome) {
      if (mudancas[nome] === null || mudancas[nome] === '') {
        delete estado[nome];
      } else {
        estado[nome] = String(mudancas[nome]);
      }
    });
    if (!('pagina' in mudancas)) {
      delete estado.pagina;
    }
    navegar(estado);
  }

  function preencherFormulario(estado) {
    campoTermo.value = estado.termo || '';
    campoCep.value = estado.cep || '';
    campoOrdem.value = estado.ordem || 'RELEVANCIA';
  }

  // ----------------------------------------------------------------- busca

  async function carregar() {
    var estado = estadoDaUrl();
    preencherFormulario(estado);
    fecharSugestoes();
    var minha = ++buscaAtual;

    try {
      var dados = await api.get('/api/busca?' + consultaDe(estado));
      if (minha !== buscaAtual) {
        return; // uma busca mais nova ja foi disparada
      }
      aviso.hidden = true;
      desenhar(dados, estado);
    } catch (erro) {
      if (minha !== buscaAtual) {
        return;
      }
      casca.tratarErro(erro, aviso);
    }
  }

  function desenhar(dados, estado) {
    var termo = estado.termo || '';
    document.getElementById('titulo').textContent = termo ? 'Resultados para "' + termo + '"' : 'Todos os produtos';
    document.title = (termo ? termo + ' · ' : '') + 'Busca · G1_ProjetoComercioEletronico';
    document.getElementById('resumo').textContent = dados.total === 1 ? '1 resultado'
      : dados.total + ' resultados' + (dados.paginas > 1 ? ' · página ' + dados.pagina + ' de ' + dados.paginas : '');

    desenharFacetas(dados.facetas, estado);
    desenharResultados(dados, estado);
    desenharPaginacao(dados);
    desenharCadeia(dados.cadeia);
  }

  function desenharFacetas(facetas, estado) {
    var alvo = document.getElementById('facetas');
    alvo.textContent = '';
    var grupos = {};
    facetas.forEach(function (faceta) {
      (grupos[faceta.nome] = grupos[faceta.nome] || []).push(faceta);
    });
    var nomes = Object.keys(grupos);
    if (!nomes.length) {
      alvo.appendChild(el('p', { classe: 'suave pequeno', texto: 'Sem filtros para esta busca.' }));
      return;
    }
    nomes.forEach(function (nome) {
      var escolhida = estado[nome];
      var opcoes = grupos[nome].map(function (faceta) {
        var ativa = escolhida && escolhida.toUpperCase() === faceta.opcao.toUpperCase();
        var botao = el('button', {
          type: 'button', classe: 'faceta' + (ativa ? ' ativa' : ''), 'aria-pressed': ativa ? 'true' : 'false'
        }, [
          el('span', { texto: ROTULOS_DE_OPCAO[faceta.opcao] || faceta.opcao }),
          // RN-A03: quantos resultados restarao se o comprador clicar aqui.
          el('span', { classe: 'contagem', texto: String(faceta.contagemPrevia) })
        ]);
        botao.addEventListener('click', function () {
          var mudanca = {};
          mudanca[nome] = ativa ? null : faceta.opcao;
          refinar(mudanca);
        });
        return el('li', {}, [botao]);
      });
      var cabeca = [el('h3', { texto: ROTULOS_DE_FACETA[nome] || nome })];
      if (escolhida) {
        var limpar = el('button', { type: 'button', classe: 'link-botao pequeno', texto: 'limpar' });
        limpar.addEventListener('click', function () {
          var mudanca = {};
          mudanca[nome] = null;
          refinar(mudanca);
        });
        cabeca.push(limpar);
      }
      alvo.appendChild(el('div', { classe: 'faceta-grupo' }, [
        el('div', { classe: 'faceta-cabeca' }, cabeca),
        el('ul', {}, opcoes)
      ]));
    });
  }

  function linkDoAnuncio(id, estado) {
    return 'anuncio.html?id=' + encodeURIComponent(id) + (estado.cep ? '&cep=' + encodeURIComponent(estado.cep) : '');
  }

  function desenharResultados(dados, estado) {
    var alvo = document.getElementById('resultados');
    alvo.textContent = '';

    if (dados.total === 0) {
      var vazio = [
        el('h2', { texto: 'Nenhum resultado' + (estado.termo ? ' para "' + estado.termo + '"' : '') }),
        el('p', { texto: 'Confira a grafia ou tente um termo mais geral.' })
      ];
      // RN-A01: em vez de uma tela vazia, termos alternativos do acervo.
      if (dados.termosAlternativos.length) {
        vazio.push(el('p', { classe: 'pequeno', texto: 'Talvez você procure:' }));
        vazio.push(el('ul', { classe: 'alternativos' }, dados.termosAlternativos.map(function (termo) {
          return el('li', {}, [el('a', { href: 'busca.html?' + consultaDe({ termo: termo, cep: estado.cep }), texto: termo })]);
        })));
      }
      alvo.appendChild(el('div', { classe: 'cartao vazio' }, vazio));
      return;
    }

    if (!dados.itens.length) {
      var primeira = el('a', { href: 'busca.html', texto: 'Voltar para a primeira página' });
      primeira.addEventListener('click', function (evento) {
        evento.preventDefault();
        refinar({ pagina: null });
      });
      alvo.appendChild(el('div', { classe: 'cartao vazio' }, [
        el('p', { texto: 'Esta página não existe para esta busca.' }), primeira
      ]));
      return;
    }

    alvo.appendChild(el('ul', { classe: 'grade-anuncios' }, dados.itens.map(function (item) {
      return el('li', {}, [cartao(item, estado)]);
    })));
  }

  function cartao(item, estado) {
    var selos = item.selos || {};
    var detalhes = [];
    if (selos['reputação']) {
      detalhes.push(el('span', { classe: 'selo', title: 'Reputação do vendedor (RN-A05)', texto: '★ ' + selos['reputação'] }));
    }
    if (selos.frete) {
      detalhes.push(el('span', { classe: 'selo frete', title: 'Frete para o CEP informado (RN-A04)', texto: 'Frete ' + selos.frete }));
    }
    if (selos.prazo) {
      detalhes.push(el('span', { classe: 'selo', title: 'Prazo de entrega', texto: selos.prazo.replace('util(eis)', 'útil(eis)') }));
    }
    var link = linkDoAnuncio(item.id, estado);
    return el('article', { classe: 'cartao-anuncio' }, [
      el('a', { classe: 'cartao-imagem', href: link, tabindex: '-1', 'aria-hidden': 'true' }, [
        item.imagem ? el('img', { src: item.imagem, alt: '', loading: 'lazy', width: '120', height: '120' }) : null
      ]),
      el('div', { classe: 'cartao-corpo' }, [
        el('h3', {}, [el('a', { href: link, texto: item.titulo })]),
        el('p', { classe: 'preco', texto: item.preco }),
        el('p', { classe: 'pequeno suave', texto: (item.condicao === 'USADO' ? 'Usado' : 'Novo') + ' · ' + item.vendedor
          + (item.tipoVendedor === 'LOJA_OFICIAL' ? ' (loja oficial)' : '') }),
        el('div', { classe: 'selos' }, detalhes)
      ])
    ]);
  }

  function desenharPaginacao(dados) {
    var alvo = document.getElementById('paginacao');
    alvo.textContent = '';
    if (dados.paginas <= 1) {
      return;
    }
    function botao(texto, pagina, atual, rotulo) {
      var b = el('button', { type: 'button', classe: 'botao secundario' + (atual ? ' atual' : ''), texto: texto });
      if (rotulo) {
        b.setAttribute('aria-label', rotulo);
      }
      if (atual) {
        b.setAttribute('aria-current', 'page');
      }
      b.disabled = pagina < 1 || pagina > dados.paginas || atual;
      b.addEventListener('click', function () {
        refinar({ pagina: pagina === 1 ? null : pagina });
        window.scrollTo(0, 0);
      });
      return b;
    }
    alvo.appendChild(botao('‹ Anterior', dados.pagina - 1, false, 'Página anterior'));
    for (var p = 1; p <= dados.paginas; p++) {
      alvo.appendChild(botao(String(p), p, p === dados.pagina, 'Página ' + p));
    }
    alvo.appendChild(botao('Próxima ›', dados.pagina + 1, false, 'Próxima página'));
  }

  function desenharCadeia(cadeia) {
    var caixa = document.getElementById('cadeia');
    var lista = document.getElementById('cadeia-lista');
    lista.textContent = '';
    (cadeia || []).forEach(function (elo) {
      lista.appendChild(el('li', { classe: 'mono', texto: elo }));
    });
    caixa.hidden = !(cadeia && cadeia.length);
  }

  // ------------------------------------------------- sugestoes ao digitar (UC02)

  function fecharSugestoes() {
    listaSugestoes.hidden = true;
    listaSugestoes.textContent = '';
    campoTermo.setAttribute('aria-expanded', 'false');
    campoTermo.removeAttribute('aria-activedescendant');
    destacada = -1;
  }

  function escolherSugestao(texto) {
    campoTermo.value = texto;
    fecharSugestoes();
    refinar({ termo: texto, categoria: null, condicao: null });
  }

  function mostrarSugestoes(sugestoes) {
    listaSugestoes.textContent = '';
    destacada = -1;
    if (!sugestoes.length) {
      fecharSugestoes();
      return;
    }
    sugestoes.forEach(function (texto, i) {
      var item = el('li', { id: 'sugestao-' + i, role: 'option', 'aria-selected': 'false', texto: texto });
      // mousedown, e nao click: dispara antes de o campo perder o foco.
      item.addEventListener('mousedown', function (evento) {
        evento.preventDefault();
        escolherSugestao(texto);
      });
      listaSugestoes.appendChild(item);
    });
    listaSugestoes.hidden = false;
    campoTermo.setAttribute('aria-expanded', 'true');
  }

  function destacar(indice) {
    var itens = listaSugestoes.querySelectorAll('li');
    if (!itens.length) {
      return;
    }
    destacada = (indice + itens.length) % itens.length;
    itens.forEach(function (item, i) {
      item.setAttribute('aria-selected', i === destacada ? 'true' : 'false');
    });
    campoTermo.setAttribute('aria-activedescendant', itens[destacada].id);
  }

  campoTermo.addEventListener('input', function () {
    clearTimeout(esperaSugestoes);
    var prefixo = campoTermo.value.trim();
    if (!prefixo) {
      fecharSugestoes();
      return;
    }
    // Espera 250 ms sem tecla nova antes de chamar o servidor.
    esperaSugestoes = setTimeout(async function () {
      var minha = ++sugestaoAtual;
      try {
        var dados = await api.get('/api/sugestoes?q=' + encodeURIComponent(prefixo));
        if (minha === sugestaoAtual && campoTermo.value.trim() === prefixo) {
          mostrarSugestoes(dados.sugestoes);
        }
      } catch (erro) {
        fecharSugestoes();
      }
    }, ESPERA_DAS_SUGESTOES);
  });

  campoTermo.addEventListener('keydown', function (evento) {
    if (listaSugestoes.hidden) {
      return;
    }
    if (evento.key === 'ArrowDown' || evento.key === 'ArrowUp') {
      evento.preventDefault();
      destacar(destacada + (evento.key === 'ArrowDown' ? 1 : -1));
    } else if (evento.key === 'Enter' && destacada >= 0) {
      evento.preventDefault();
      escolherSugestao(listaSugestoes.querySelectorAll('li')[destacada].textContent);
    } else if (evento.key === 'Escape') {
      fecharSugestoes();
    }
  });

  campoTermo.addEventListener('blur', function () {
    setTimeout(fecharSugestoes, 100);
  });

  // ------------------------------------------------------------- formulario

  formulario.addEventListener('submit', function (evento) {
    evento.preventDefault();
    clearTimeout(esperaSugestoes);
    var estado = estadoDaUrl();
    var termoNovo = campoTermo.value.trim();
    var mudancas = { cep: campoCep.value.trim(), ordem: campoOrdem.value === 'RELEVANCIA' ? null : campoOrdem.value, termo: termoNovo };
    if (termoNovo !== (estado.termo || '')) {
      // Termo novo e uma busca nova: os filtros da busca anterior nao valem mais.
      mudancas.categoria = null;
      mudancas.condicao = null;
    }
    refinar(mudancas);
  });

  // A ordenacao muda a listagem na hora, sem precisar do botao.
  campoOrdem.addEventListener('change', function () {
    refinar({ ordem: campoOrdem.value === 'RELEVANCIA' ? null : campoOrdem.value });
  });

  // Voltar e avancar do navegador: a URL ja mudou, basta desenhar a partir dela.
  window.addEventListener('popstate', carregar);

  carregar();
})();
