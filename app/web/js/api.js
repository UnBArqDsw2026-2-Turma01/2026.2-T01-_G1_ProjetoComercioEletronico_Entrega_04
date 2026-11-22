// Chamadas a API. Toda resposta traz o campo "arquitetura"; ele e entregue a
// casca para alimentar a lateral "Arquitetura em execucao".
//
//   const dados = await api.get('/api/busca?termo=fone');
//   await api.post('/api/carrinho/itens', { anuncioId: 'anu-001', quantidade: 1 });
//
// Em caso de recusa, a promessa rejeita com um Error que carrega status e
// codigo (por exemplo, erro.codigo === 'RN-B07').

var api = (function () {
  'use strict';

  var CHAVE_DA_SESSAO = 'g1.sessao';

  function sessao() {
    try {
      return JSON.parse(sessionStorage.getItem(CHAVE_DA_SESSAO)) || null;
    } catch (e) {
      return null;
    }
  }

  function guardarSessao(dados) {
    try {
      if (dados) {
        sessionStorage.setItem(CHAVE_DA_SESSAO, JSON.stringify(dados));
      } else {
        sessionStorage.removeItem(CHAVE_DA_SESSAO);
      }
    } catch (e) {
      // Sem armazenamento de sessao a pagina continua funcionando, como anonima.
    }
  }

  async function chamar(metodo, caminho, corpo) {
    var opcoes = { method: metodo, headers: {} };
    var atual = sessao();
    if (atual && atual.token) {
      opcoes.headers['Authorization'] = 'Bearer ' + atual.token;
    }
    if (corpo !== undefined) {
      opcoes.headers['Content-Type'] = 'application/json';
      opcoes.body = JSON.stringify(corpo);
    }

    var resposta;
    try {
      resposta = await fetch(caminho, opcoes);
    } catch (falha) {
      var semRede = new Error('Nao foi possivel falar com o servidor. Ele esta no ar?');
      semRede.status = 0;
      semRede.codigo = 'SEM_CONEXAO';
      throw semRede;
    }

    var dados = {};
    try {
      dados = await resposta.json();
    } catch (e) {
      dados = {};
    }

    if (window.casca && dados.arquitetura) {
      window.casca.mostrarArquitetura(dados.arquitetura, {
        metodo: metodo, caminho: caminho, status: resposta.status
      });
    }

    if (!resposta.ok) {
      var detalhe = dados.erro || {};
      var erro = new Error(detalhe.mensagem || ('Falha ' + resposta.status));
      erro.status = resposta.status;
      erro.codigo = detalhe.codigo || 'ERRO';
      throw erro;
    }
    return dados;
  }

  return {
    get: function (caminho) { return chamar('GET', caminho); },
    post: function (caminho, corpo) { return chamar('POST', caminho, corpo || {}); },
    patch: function (caminho, corpo) { return chamar('PATCH', caminho, corpo || {}); },
    delete: function (caminho) { return chamar('DELETE', caminho); },
    sessao: sessao,
    guardarSessao: guardarSessao
  };
})();
