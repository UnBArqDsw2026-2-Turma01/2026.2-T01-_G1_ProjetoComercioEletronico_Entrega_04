// Casca comum a todas as paginas: cabecalho, grade e a lateral
// "Arquitetura em execucao".
//
// Uso: a pagina declara <main id="conteudo"> e inclui este arquivo. A casca
// envolve o <main> na grade e acrescenta o cabecalho e a lateral.
//
// A lateral mostra, a cada chamada a API, o que a rota declarou no codigo:
// casos de uso e linhas da matriz do DAS, as classes da camada mais externa
// para a mais interna e o padrao de projeto que atuou.

var casca = (function () {
  'use strict';

  var historico = [];

  // Cria um elemento. O texto entra sempre por textContent: nada do que vem
  // da API e interpretado como HTML.
  function el(nome, atributos, filhos) {
    var no = document.createElement(nome);
    Object.keys(atributos || {}).forEach(function (chave) {
      if (chave === 'texto') {
        no.textContent = atributos[chave];
      } else if (chave === 'classe') {
        no.className = atributos[chave];
      } else {
        no.setAttribute(chave, atributos[chave]);
      }
    });
    (filhos || []).forEach(function (filho) {
      if (filho) {
        no.appendChild(filho);
      }
    });
    return no;
  }

  function camadaDe(nomeDaClasse) {
    if (/^(Controlador|Rotas)/.test(nomeDaClasse) || nomeDaClasse === 'Roteador') {
      return 'controle';
    }
    if (/^(Servico|Registro|I[A-Z])/.test(nomeDaClasse) || /DTO$/.test(nomeDaClasse)) {
      return 'aplicacao';
    }
    return 'dominio';
  }

  function grupo(rotulo, conteudo) {
    return el('div', { classe: 'arq-grupo' }, [el('div', { classe: 'arq-rotulo', texto: rotulo }), conteudo]);
  }

  function etiquetas(itens) {
    return el('div', { classe: 'arq-etiquetas' }, itens.map(function (item) {
      return el('span', { classe: 'etiqueta', texto: item });
    }));
  }

  function mostrarArquitetura(arquitetura, chamada) {
    var painel = document.getElementById('arquitetura-corpo');
    if (!painel) {
      return;
    }
    historico.unshift(chamada.status + '  ' + chamada.metodo + ' ' + chamada.caminho);
    historico = historico.slice(0, 5);

    painel.textContent = '';
    var sucesso = chamada.status >= 200 && chamada.status < 300;
    painel.appendChild(el('div', { classe: 'arq-rota' }, [
      el('span', { classe: 'arq-status ' + (sucesso ? 'ok' : 'erro'), texto: chamada.status + ' ' }),
      el('span', { texto: arquitetura.rota || (chamada.metodo + ' ' + chamada.caminho) })
    ]));

    if (arquitetura.uc && arquitetura.uc.length) {
      painel.appendChild(grupo('Casos de uso (DAS, seção 4)', etiquetas(arquitetura.uc)));
    }
    if (arquitetura.tr && arquitetura.tr.length) {
      painel.appendChild(grupo('Matriz de rastreabilidade (DAS, seção 8.3)', etiquetas(arquitetura.tr)));
    }
    if (arquitetura.classes && arquitetura.classes.length) {
      painel.appendChild(grupo('Classes, da camada externa para a interna',
        el('ol', { classe: 'arq-classes' }, arquitetura.classes.map(function (nome) {
          return el('li', { classe: camadaDe(nome), texto: nome });
        }))));
    }
    if (arquitetura.mecanismo) {
      painel.appendChild(grupo('Mecanismo em execução', el('p', { classe: 'arq-mecanismo', texto: arquitetura.mecanismo })));
    }
    if (historico.length > 1) {
      painel.appendChild(el('ol', { classe: 'arq-historico' }, historico.slice(1).map(function (linha) {
        return el('li', { texto: linha });
      })));
    }
  }

  function montar() {
    var conteudo = document.getElementById('conteudo');
    if (!conteudo) {
      return;
    }
    var termoAtual = new URLSearchParams(window.location.search).get('termo') || '';

    var busca = el('form', { action: 'busca.html', method: 'get', role: 'search' }, [
      el('input', {
        classe: 'campo', type: 'search', name: 'termo', value: termoAtual,
        placeholder: 'Buscar produtos', 'aria-label': 'Buscar produtos', autocomplete: 'off'
      }),
      el('button', { classe: 'botao', type: 'submit', texto: 'Buscar' })
    ]);

    var sessao = window.api ? window.api.sessao() : null;
    var navegacao = el('nav', { 'aria-label': 'Principal' }, [
      el('a', { href: 'carrinho.html', texto: 'Carrinho' }),
      el('a', { href: 'pedidos.html', texto: 'Pedidos' }),
      el('a', { href: 'conta.html', texto: sessao && sessao.nome ? sessao.nome : 'Entrar' })
    ]);

    var topo = el('header', { classe: 'topo' }, [
      el('div', { classe: 'topo-interno' }, [
        el('a', { classe: 'marca', href: 'index.html' }, [
          el('span', { texto: 'G1' }), document.createTextNode(' Comércio Eletrônico')
        ]),
        busca,
        navegacao
      ])
    ]);

    var lateral = el('aside', { classe: 'arquitetura', 'aria-live': 'polite', 'aria-label': 'Arquitetura em execução' }, [
      el('h2', { texto: 'Arquitetura em execução' }),
      el('div', { id: 'arquitetura-corpo' }, [
        el('p', { classe: 'arq-vazio', texto: 'A cada ação, este painel mostra os casos de uso, as classes e o padrão de projeto que atenderam a chamada.' })
      ])
    ]);

    var pagina = el('div', { classe: 'pagina' });
    document.body.insertBefore(topo, document.body.firstChild);
    conteudo.parentNode.insertBefore(pagina, conteudo);
    pagina.appendChild(conteudo);
    pagina.appendChild(lateral);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', montar);
  } else {
    montar();
  }

  return { el: el, mostrarArquitetura: mostrarArquitetura };
})();
