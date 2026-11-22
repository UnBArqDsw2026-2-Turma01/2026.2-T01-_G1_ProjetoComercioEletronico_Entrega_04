// Conta (UC09, UC23) e saida (UC22).

(function () {
  'use strict';

  if (!casca.exigirSessao()) {
    return;
  }
  var el = casca.el;
  var aviso = document.getElementById('aviso');

  api.get('/api/conta').then(function (conta) {
    document.getElementById('perfil').hidden = false;
    document.getElementById('nome').textContent = conta.nome;
    document.getElementById('email').textContent = conta.email;
    var lista = document.getElementById('enderecos');
    if (!conta.enderecos.length) {
      lista.appendChild(el('li', { texto: 'Nenhum endereço cadastrado.' }));
    }
    conta.enderecos.forEach(function (e) {
      lista.appendChild(el('li', { texto: e.apelido + ': ' + e.resumo + ' · CEP ' + e.cep }));
    });
  }).catch(function (erro) { casca.tratarErro(erro, aviso); });

  document.getElementById('sair').addEventListener('click', async function () {
    try {
      await api.delete('/api/sessao');
    } catch (erro) {
      // A sessao pode ja ter vencido; sai do mesmo jeito.
    }
    api.guardarSessao(null);
    window.location.assign('entrar.html');
  });
})();
