// Pos-venda (UC17 a UC21): avaliar um item entregue, abrir reclamacao,
// responder como vendedor, mediar e reembolsar como atendimento.
//
// posvenda.html?pedido=<id>. A pagina mostra o que a conta da sessao pode
// fazer agora; quem decide isso e o servidor (campos podeAvaliar e
// podeReclamar, e o papel conferido na conta), nao a tela.

(function () {
  'use strict';

  var el = casca.el;
  var aviso = document.getElementById('aviso');
  var pedidoId = casca.parametro('pedido');
  var SENHA_DE_DEMONSTRACAO = 'demo1234';
  var ultimo = null;

  // Contas da semente (comercio.contas.persistencia.SementeDeContas). So para
  // o seletor "agir como" da demonstracao; nenhuma regra depende disto.
  var EMAIL_DO_COMPRADOR = 'joana@exemplo.com';
  var EMAIL_DO_ATENDIMENTO = 'atendimento@exemplo.com';
  var EMAIL_DO_VENDEDOR = {
    'ven-01': 'contato@audiostore.exemplo',
    'ven-02': 'carlos@eletro.exemplo',
    'ven-03': 'marcia@usados.exemplo',
    'ven-04': 'vendas@gamehouse.exemplo',
    'ven-05': 'ola@fitsound.exemplo',
    'ven-06': 'sac@acessoriosbr.exemplo'
  };
  var NOMES_DOS_PAPEIS = { COMPRADOR: 'comprador', VENDEDOR: 'vendedor', ATENDENTE: 'atendimento' };

  function mostrarAviso(texto, tipo) {
    aviso.hidden = !texto;
    aviso.className = 'aviso ' + (tipo || '');
    aviso.textContent = texto || '';
  }

  function recusa(erro) {
    if (erro.status === 401) {
      api.guardarSessao(null);
      desenharSessao();
      mostrarAviso('A sessão terminou. Escolha com quem agir.', 'erro');
      return;
    }
    mostrarAviso(erro.message + (erro.codigo && erro.codigo !== 'ERRO' ? ' (' + erro.codigo + ')' : ''), 'erro');
  }

  // ------------------------------------------------------- agir como (demo)

  // Percorre o mesmo login das outras paginas: captcha, credenciais, risco e,
  // num dispositivo novo, o segundo fator com o codigo simulado.
  async function entrarComo(email) {
    var desafio = (await api.get('/api/sessao/desafio')).desafio;
    var corpo = { email: email, senha: SENHA_DE_DEMONSTRACAO, desafio: desafio,
      dispositivo: casca.dispositivo(), lembrarDispositivo: true };
    var resultado = await api.post('/api/sessao', corpo);
    if (resultado.situacao === 'EXIGE_DOIS_FATORES') {
      corpo.desafio = resultado.novoDesafio;
      corpo.codigo = resultado.codigoSimulado;
      resultado = await api.post('/api/sessao', corpo);
    }
    api.guardarSessao({ token: resultado.token, nome: resultado.nome, usuarioId: resultado.usuarioId,
      papel: resultado.papel });
  }

  function vendedorDaVez() {
    if (!ultimo) {
      return null;
    }
    var aberta = ultimo.reclamacoes.filter(function (r) { return !encerrada(r.status); })[0];
    if (aberta) {
      return aberta.vendedorId;
    }
    if (ultimo.reclamacoes.length) {
      return ultimo.reclamacoes[ultimo.reclamacoes.length - 1].vendedorId;
    }
    return ultimo.itens.length ? ultimo.itens[0].vendedorId : null;
  }

  document.querySelectorAll('[data-papel]').forEach(function (botao) {
    botao.addEventListener('click', async function () {
      var papel = botao.getAttribute('data-papel');
      var email = papel === 'COMPRADOR' ? EMAIL_DO_COMPRADOR
        : papel === 'ATENDENTE' ? EMAIL_DO_ATENDIMENTO : EMAIL_DO_VENDEDOR[vendedorDaVez()];
      if (!email) {
        mostrarAviso('Abra o pedido como comprador ou atendimento primeiro: é ele que diz quem é o vendedor.', 'erro');
        return;
      }
      try {
        await entrarComo(email);
        // Recarrega para o cabecalho da casca tambem mostrar quem esta agindo.
        window.location.reload();
      } catch (erro) {
        recusa(erro);
      }
    });
  });

  function desenharSessao() {
    var sessao = api.sessao();
    var alvo = document.getElementById('sessao-atual');
    alvo.textContent = '';
    if (!sessao || !sessao.token) {
      alvo.appendChild(el('span', { classe: 'suave', texto: 'Nenhuma sessão. Escolha com quem agir.' }));
      return;
    }
    alvo.appendChild(document.createTextNode('Agindo como '));
    alvo.appendChild(el('strong', { texto: sessao.nome }));
    alvo.appendChild(el('span', { classe: 'suave', texto: ' (' + (NOMES_DOS_PAPEIS[sessao.papel] || sessao.papel)
      + ', ' + sessao.usuarioId + ')' }));
    document.querySelector('[data-papel="VENDEDOR"]').disabled = !vendedorDaVez() && sessao.papel !== 'VENDEDOR';
  }

  // ------------------------------------------------------------- carregar

  // Depois de uma acao (avaliar, reclamar, responder, mediar), a pagina se
  // redesenha sem passar por api.get: assim a lateral continua mostrando o
  // mecanismo da acao (reputacao antes e depois, politica de estorno), e nao
  // o da consulta que veio em seguida.
  async function consultarSemLateral(caminho) {
    var sessao = api.sessao();
    var resposta = await fetch(caminho, { headers: { Authorization: 'Bearer ' + sessao.token } });
    var dados = await resposta.json();
    if (!resposta.ok) {
      var erro = new Error((dados.erro && dados.erro.mensagem) || ('Falha ' + resposta.status));
      erro.status = resposta.status;
      erro.codigo = (dados.erro && dados.erro.codigo) || 'ERRO';
      throw erro;
    }
    return dados;
  }

  async function carregar(depoisDeUmaAcao) {
    desenharSessao();
    var sessao = api.sessao();
    if (!sessao || !sessao.token) {
      document.getElementById('corpo').hidden = true;
      document.getElementById('lista-pedidos').hidden = true;
      return;
    }
    if (!pedidoId) {
      await listarPedidos(sessao);
      return;
    }
    var caminho = '/api/pedidos/' + encodeURIComponent(pedidoId) + '/posvenda';
    try {
      ultimo = depoisDeUmaAcao ? await consultarSemLateral(caminho) : await api.get(caminho);
      desenhar(ultimo, sessao);
      desenharSessao();
    } catch (erro) {
      document.getElementById('corpo').hidden = true;
      recusa(erro);
    }
  }

  async function listarPedidos(sessao) {
    var secao = document.getElementById('lista-pedidos');
    var lista = document.getElementById('pedidos');
    document.getElementById('corpo').hidden = true;
    secao.hidden = false;
    lista.textContent = '';
    if (sessao.papel !== 'COMPRADOR') {
      lista.appendChild(el('li', { classe: 'suave', texto: 'Abra esta página com ?pedido=<id>, ou aja como comprador para ver a lista.' }));
      return;
    }
    try {
      var dados = await api.get('/api/pedidos');
      if (!dados.pedidos.length) {
        lista.appendChild(el('li', { classe: 'suave', texto: 'Nenhum pedido ainda. Faça uma compra primeiro.' }));
      }
      dados.pedidos.forEach(function (p) {
        lista.appendChild(el('li', {}, [el('a', { href: 'posvenda.html?pedido=' + encodeURIComponent(p.id) }, [
          el('span', { classe: 'id', texto: p.id }),
          el('span', { classe: 'situacao ' + p.status, texto: p.statusRotulo })
        ])]));
      });
    } catch (erro) {
      recusa(erro);
    }
  }

  // ------------------------------------------------------------- desenhar

  function encerrada(status) {
    return status === 'RESOLVIDA' || status === 'REEMBOLSADA';
  }

  function desenhar(dados, sessao) {
    document.getElementById('titulo-pedido').textContent = dados.pedidoId;
    var situacao = document.getElementById('situacao');
    situacao.hidden = false;
    situacao.className = 'situacao ' + dados.status;
    situacao.textContent = dados.statusRotulo;
    document.getElementById('lista-pedidos').hidden = true;
    document.getElementById('corpo').hidden = false;

    desenharItens(dados);
    desenharReclamacoes(dados, sessao);
    desenharFormularioDeReclamacao(dados);
  }

  function desenharItens(dados) {
    var alvo = document.getElementById('itens');
    alvo.textContent = '';
    dados.itens.forEach(function (item) {
      var acoes = [];
      if (item.avaliacao) {
        acoes.push(el('p', { classe: 'pequeno', style: 'margin:0' }, [
          el('strong', { texto: '★ ' + item.avaliacao.nota + ' de 5' }),
          document.createTextNode(item.avaliacao.comentario ? ' · "' + item.avaliacao.comentario + '"' : '')
        ]));
      } else if (dados.podeAvaliar) {
        acoes.push(formularioDeAvaliacao(item));
      } else {
        acoes.push(el('p', { classe: 'pequeno suave', style: 'margin:0', texto: dados.papel === 'COMPRADOR'
          ? 'A avaliação abre quando o pedido for entregue (UC17).' : 'Ainda não avaliado.' }));
      }
      alvo.appendChild(el('div', { classe: 'item-linha', style: 'grid-template-columns:minmax(0,1fr)' }, [
        el('div', {}, [
          el('div', { classe: 'titulo', texto: item.quantidade + '× ' + item.titulo }),
          el('div', { classe: 'pequeno suave', texto: 'Vendido por ' + item.vendedorNome + ' · anúncio ' + item.anuncioId })
        ])
      ].concat(acoes)));
    });
  }

  function formularioDeAvaliacao(item) {
    var nota = el('select', { classe: 'campo', name: 'nota', 'aria-label': 'Nota para ' + item.titulo, style: 'width:auto' },
      [5, 4, 3, 2, 1].map(function (n) {
        return el('option', { value: String(n), texto: n + (n === 1 ? ' estrela' : ' estrelas') });
      }));
    var comentario = el('input', { classe: 'campo', name: 'comentario', placeholder: 'Comentário (opcional)', maxlength: '200' });
    var form = el('form', { classe: 'botoes-linha', style: 'margin-top:.25rem;align-items:center' }, [
      nota, comentario, el('button', { classe: 'botao', type: 'submit', texto: 'Avaliar' })
    ]);
    form.addEventListener('submit', async function (evento) {
      evento.preventDefault();
      try {
        var feita = await api.post('/api/pedidos/' + encodeURIComponent(pedidoId) + '/avaliacao', {
          anuncioId: item.anuncioId, nota: parseInt(nota.value, 10), comentario: comentario.value
        });
        mostrarAviso('Avaliação registrada. Reputação de ' + feita.vendedorNome + ': ' + feita.reputacaoAntes
          + ' → ' + feita.reputacaoDepois + ' (RN-A05).', 'ok');
        carregar(true);
      } catch (erro) {
        recusa(erro);
      }
    });
    return form;
  }

  function desenharReclamacoes(dados, sessao) {
    var alvo = document.getElementById('reclamacoes');
    alvo.textContent = '';
    if (!dados.reclamacoes.length) {
      alvo.appendChild(el('p', { classe: 'suave pequeno', texto: dados.podeReclamar
        ? 'Nenhuma reclamação. Se algo deu errado, descreva abaixo.'
        : 'Nenhuma reclamação. Ela pode ser aberta pelo comprador depois que o pedido for enviado (UC18).' }));
      return;
    }
    dados.reclamacoes.slice().reverse().forEach(function (r) {
      var partes = [
        el('div', { classe: 'cabeca-pagina', style: 'margin-bottom:.5rem' }, [
          el('h3', { style: 'margin:0', texto: r.id + ' · ' + r.vendedorNome }),
          el('span', { classe: 'situacao' + (r.status === 'REEMBOLSADA' ? ' ENTREGUE' : r.status === 'RESOLVIDA' ? ' PAGO' : ' AGUARDANDO_PAGAMENTO'),
            texto: r.statusRotulo })
        ]),
        el('p', {}, [el('strong', { texto: 'Motivo: ' }), document.createTextNode(r.motivo)])
      ];
      if (r.resposta) {
        partes.push(el('p', {}, [el('strong', { texto: 'Resposta do vendedor: ' }), document.createTextNode(r.resposta)]));
      }
      if (r.decisao) {
        partes.push(el('p', {}, [el('strong', { texto: 'Decisão do atendimento: ' + r.decisao }),
          document.createTextNode(r.parecer ? ' · ' + r.parecer : '')]));
      }
      if (r.estorno) {
        partes.push(el('p', { classe: 'aviso ok' }, [
          el('strong', { texto: 'Estorno de ' + r.estorno.valor + ' ' }),
          document.createTextNode('pela política ' + r.estorno.politica + ': ' + r.estorno.descricao
            + '. Devolução em até ' + r.estorno.prazoEmDias + ' dia(s).')
        ]));
      }
      partes.push(el('h4', { classe: 'pequeno', style: 'margin:.75rem 0 .25rem', texto: 'Linha do tempo' }));
      partes.push(el('ol', { classe: 'registro' }, r.linhaDoTempo.map(function (passo) {
        return el('li', { texto: passo });
      })));
      var acoes = acoesDaReclamacao(r, dados, sessao);
      if (acoes) {
        partes.push(acoes);
      }
      alvo.appendChild(el('div', { classe: 'grupo-vendedor', style: 'padding-top:.5rem' }, partes));
    });
  }

  async function enviar(caminho, corpo) {
    try {
      await api.post(caminho, corpo);
      mostrarAviso('', '');
      carregar(true);
    } catch (erro) {
      recusa(erro);
    }
  }

  function acoesDaReclamacao(r, dados, sessao) {
    var caminho = '/api/reclamacoes/' + encodeURIComponent(r.id);
    if (encerrada(r.status)) {
      return null;
    }
    // UC19: so o vendedor do envio, so com a reclamacao aberta.
    if (dados.papel === 'VENDEDOR' && sessao.usuarioId === r.vendedorId && r.status === 'ABERTA') {
      var texto = el('textarea', { classe: 'campo', rows: '2', placeholder: 'Resposta ao comprador', 'aria-label': 'Resposta ao comprador' });
      var formResposta = el('form', { classe: 'simulacao' }, [
        el('h3', { texto: 'Responder como vendedor (UC19)' }), texto,
        el('div', { classe: 'botoes' }, [el('button', { classe: 'botao', type: 'submit', texto: 'Enviar resposta' })])
      ]);
      formResposta.addEventListener('submit', function (evento) {
        evento.preventDefault();
        enviar(caminho + '/resposta', { resposta: texto.value });
      });
      return formResposta;
    }
    // UC20 e UC21: o atendimento assume e decide.
    if (dados.papel === 'ATENDENTE') {
      var parecer = el('textarea', { classe: 'campo', rows: '2', placeholder: 'Parecer da mediação', 'aria-label': 'Parecer da mediação' });
      var botoes = [];
      if (r.status !== 'EM_MEDIACAO') {
        var assumir = el('button', { classe: 'botao secundario', type: 'button', texto: 'Assumir mediação' });
        assumir.addEventListener('click', function () { enviar(caminho + '/mediacao', {}); });
        botoes.push(assumir);
      }
      var procedente = el('button', { classe: 'botao', type: 'button', texto: 'Procedente: reembolsar' });
      procedente.addEventListener('click', function () {
        enviar(caminho + '/mediacao', { decisao: 'PROCEDENTE', parecer: parecer.value });
      });
      var improcedente = el('button', { classe: 'botao secundario', type: 'button', texto: 'Improcedente: manter a venda' });
      improcedente.addEventListener('click', function () {
        enviar(caminho + '/mediacao', { decisao: 'IMPROCEDENTE', parecer: parecer.value });
      });
      botoes.push(procedente, improcedente);
      return el('div', { classe: 'simulacao' }, [
        el('h3', { texto: 'Mediar como atendimento (UC20 e UC21)' }),
        el('p', { classe: 'pequeno suave', texto: 'Procedente estorna pela política da família de pagamento usada na compra.' }),
        parecer, el('div', { classe: 'botoes' }, botoes)
      ]);
    }
    return null;
  }

  function desenharFormularioDeReclamacao(dados) {
    var form = document.getElementById('form-reclamacao');
    form.hidden = !dados.podeReclamar;
    if (!dados.podeReclamar) {
      return;
    }
    var seletor = document.getElementById('reclamacao-item');
    seletor.textContent = '';
    var vendedores = {};
    dados.itens.forEach(function (item) { vendedores[item.vendedorId] = true; });
    if (Object.keys(vendedores).length === 1) {
      seletor.appendChild(el('option', { value: '', texto: 'O envio inteiro' }));
    }
    dados.itens.forEach(function (item) {
      seletor.appendChild(el('option', { value: item.anuncioId, texto: item.titulo + ' (' + item.vendedorNome + ')' }));
    });
  }

  document.getElementById('form-reclamacao').addEventListener('submit', async function (evento) {
    evento.preventDefault();
    var f = evento.target.elements;
    try {
      await api.post('/api/reclamacoes', { pedidoId: pedidoId, anuncioId: f.anuncioId.value || null, motivo: f.motivo.value });
      evento.target.reset();
      mostrarAviso('Reclamação aberta. O vendedor responde por esta mesma página.', 'ok');
      carregar(true);
    } catch (erro) {
      recusa(erro);
    }
  });

  carregar();
})();
