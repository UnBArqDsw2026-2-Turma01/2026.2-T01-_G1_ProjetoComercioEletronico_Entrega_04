// Pagina inicial: situacao do servidor e a rastreabilidade lida da propria API.

(function () {
  'use strict';

  var el = casca.el;

  function linha(rota) {
    return el('tr', {}, [
      el('td', {}, [el('code', { texto: rota.metodo + ' ' + rota.caminho })]),
      el('td', { texto: rota.uc.join(', ') || '—' }),
      el('td', { texto: rota.tr.join(', ') || '—' }),
      el('td', { classe: 'mono', texto: rota.classes.join(' → ') || '—' })
    ]);
  }

  async function carregar() {
    var situacao = document.getElementById('situacao');
    var corpo = document.getElementById('rotas');
    try {
      var saude = await api.get('/api/saude');
      situacao.className = 'aviso ok';
      situacao.textContent = 'Servidor ' + saude.situacao + ', com ' + saude.rotas + ' rota(s) publicada(s).';

      var dados = await api.get('/api/rastreabilidade');
      corpo.textContent = '';
      dados.rotas.forEach(function (rota) {
        corpo.appendChild(linha(rota));
      });
    } catch (erro) {
      situacao.className = 'aviso erro';
      situacao.textContent = erro.message;
      corpo.textContent = '';
      corpo.appendChild(el('tr', {}, [el('td', { colspan: '4', classe: 'suave', texto: 'Sem dados.' })]));
    }
  }

  carregar();
})();
