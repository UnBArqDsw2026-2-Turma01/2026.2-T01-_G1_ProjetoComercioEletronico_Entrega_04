// Pedido (UC12, UC13, UC25, UC26, UC27): situacao no ciclo do State,
// pagamento por Abstract Factory, eventos do Observer e simuladores.

(function () {
  'use strict';

  if (!casca.exigirSessao()) {
    return;
  }
  var el = casca.el;
  var aviso = document.getElementById('aviso');
  var id = casca.parametro('id');
  var caminho = '/api/pedidos/' + encodeURIComponent(id || '');
  var ETAPAS = [
    ['AGUARDANDO_PAGAMENTO', 'Aguardando pagamento'],
    ['PAGO', 'Pago'],
    ['EM_SEPARACAO', 'Em separação'],
    ['ENVIADO', 'Enviado'],
    ['ENTREGUE', 'Entregue']
  ];
  var meiosCarregados = false;

  function preencher(alvo, itens) {
    var lista = document.getElementById(alvo);
    lista.textContent = '';
    if (!itens.length) {
      lista.appendChild(el('li', { texto: '—' }));
    }
    itens.forEach(function (texto) { lista.appendChild(el('li', { texto: texto })); });
  }

  function linhaDoTempo(status) {
    var lista = document.getElementById('linha');
    lista.textContent = '';
    var atual = ETAPAS.findIndex(function (e) { return e[0] === status; });
    ETAPAS.forEach(function (etapa, i) {
      var classe = atual >= 0 && i <= atual ? 'feito' : '';
      if (i === atual) {
        classe += ' atual';
      }
      var item = el('li', { classe: classe, texto: etapa[1] });
      if (i === atual) {
        item.setAttribute('aria-current', 'step');
      }
      lista.appendChild(item);
    });
  }

  function cobranca(c) {
    var alvo = document.getElementById('cobranca');
    alvo.textContent = '';
    if (!c) {
      alvo.appendChild(el('p', { classe: 'suave', texto: 'Nenhuma tentativa de pagamento ainda.' }));
      return;
    }
    var aprovado = c.status === 'APROVADO';
    alvo.appendChild(el('p', { classe: 'aviso ' + (aprovado ? 'ok' : (c.status === 'PENDENTE' || c.status === 'EM_ANALISE') ? '' : 'erro') }, [
      el('strong', { texto: c.meioRotulo + ': ' + c.statusRotulo }),
      document.createTextNode(c.detalhe ? ' · ' + c.detalhe : '')
    ]));
    var detalhes = [];
    if (c.identificador) { detalhes.push('Identificador: ' + c.identificador); }
    if (c.comprovante) { detalhes.push('Comprovante: ' + c.comprovante); }
    if (c.estorno) { detalhes.push('Estorno: ' + c.estorno); }
    detalhes.push('Família: ' + c.familia + ' (' + c.classesDoTrio.join(', ') + ')');
    alvo.appendChild(el('ul', { classe: 'registro', style: 'margin-top:.75rem' }, detalhes.map(function (d) {
      return el('li', { texto: d });
    })));
  }

  function envios(lista) {
    var alvo = document.getElementById('envios');
    alvo.textContent = '';
    lista.forEach(function (envio) {
      alvo.appendChild(el('div', { classe: 'grupo-vendedor' }, [
        el('h3', { classe: 'pequeno', style: 'margin:.75rem 0 .25rem', texto: envio.vendedorNome + ' → ' + envio.destino }),
        el('p', { classe: 'pequeno suave', texto: 'Frete ' + envio.frete.formatado + ' · até ' + envio.prazoDias + ' dia(s) úteis' })
      ].concat(envio.itens.map(function (item) {
        return el('div', { classe: 'item-linha', style: 'grid-template-columns:minmax(0,1fr) auto' }, [
          el('div', {}, [
            el('div', { classe: 'titulo', texto: item.titulo }),
            el('div', { classe: 'pequeno suave', texto: item.quantidade + ' × ' + item.precoNoMomento.formatado + ' (preço no momento da compra)' })
          ]),
          el('div', { classe: 'valor', texto: item.subtotal.formatado })
        ]);
      }))));
    });
  }

  async function carregarMeios() {
    if (meiosCarregados) {
      return;
    }
    meiosCarregados = true;
    var dados = await api.get('/api/meios-de-pagamento');
    var lista = document.getElementById('meios');
    dados.meios.forEach(function (m, i) {
      var entrada = el('input', { type: 'radio', name: 'meio', value: m.meio });
      entrada.checked = i === 0;
      lista.appendChild(el('label', { classe: 'opcao-cartao' }, [
        entrada, el('strong', { texto: m.rotulo }), el('span', { texto: m.familia })
      ]));
    });
  }

  function desenhar(p) {
    document.getElementById('corpo').hidden = false;
    document.getElementById('id').textContent = p.id;
    var chip = document.getElementById('situacao');
    chip.className = 'situacao ' + p.status;
    chip.textContent = p.statusRotulo;
    document.title = 'Pedido ' + p.id + ' · G1_ProjetoComercioEletronico';

    var cancelado = p.status === 'CANCELADO';
    document.getElementById('linha').hidden = cancelado;
    document.getElementById('cancelado').hidden = !cancelado;
    if (!cancelado) {
      linhaDoTempo(p.status);
    }

    cobranca(p.cobranca);
    var aguardando = p.status === 'AGUARDANDO_PAGAMENTO';
    var pendente = !!p.cobranca && (p.cobranca.status === 'PENDENTE' || p.cobranca.status === 'EM_ANALISE');
    document.getElementById('form-pagamento').hidden = !aguardando || pendente;
    if (aguardando && !pendente) {
      carregarMeios().catch(function (erro) { casca.tratarErro(erro, aviso); });
    }

    envios(p.envios);
    document.getElementById('subtotal').textContent = p.subtotal.formatado;
    document.getElementById('frete').textContent = p.frete.formatado;
    document.getElementById('total').textContent = p.total.formatado;
    preencher('historico', p.historico);
    preencher('eventos', p.eventos);

    var podeAvancar = ['PAGO', 'EM_SEPARACAO', 'ENVIADO'].indexOf(p.status) >= 0;
    document.getElementById('compensar').hidden = !(aguardando && pendente);
    document.getElementById('avancar').hidden = !podeAvancar;
    document.getElementById('simulacao').hidden = !(podeAvancar || (aguardando && pendente));
  }

  async function agir(sufixo, corpo) {
    try {
      desenhar(await api.post(caminho + sufixo, corpo));
      aviso.hidden = true;
    } catch (erro) {
      casca.tratarErro(erro, aviso);
    }
  }

  document.getElementById('form-pagamento').addEventListener('submit', function (evento) {
    evento.preventDefault();
    var marcado = document.querySelector('input[name="meio"]:checked');
    if (marcado) {
      agir('/pagamento', { meio: marcado.value });
    }
  });
  document.getElementById('desistir').addEventListener('click', function () {
    if (window.confirm('Desistir deste pedido? O estoque reservado volta para os anúncios.')) {
      agir('/desistencia');
    }
  });
  document.getElementById('compensar').addEventListener('click', function () { agir('/compensar'); });
  document.getElementById('avancar').addEventListener('click', function () { agir('/avancar'); });

  if (!id) {
    casca.tratarErro(new Error('Pedido não informado.'), aviso);
    return;
  }
  api.get(caminho).then(desenhar).catch(function (erro) { casca.tratarErro(erro, aviso); });
})();
