// Login (UC22): pede o desafio de captcha ao abrir, envia as credenciais e,
// se o elo de risco exigir, conduz o segundo fator.

(function () {
  'use strict';

  var el = casca.el;
  var desafio = null;
  var credenciais = document.getElementById('credenciais');
  var segundoFator = document.getElementById('segundo-fator');
  var aviso = document.getElementById('aviso');
  var guardadas = null;

  // So aceita voltar para uma pagina desta mesma aplicacao.
  function destino() {
    var volta = casca.parametro('volta') || '';
    return /^[a-z]+\.html(\?[\w=&%.-]*)?$/.test(volta) ? volta : 'pedidos.html';
  }

  function mostrarAviso(texto, tipo) {
    aviso.hidden = !texto;
    aviso.className = 'aviso ' + (tipo || '');
    aviso.textContent = texto || '';
  }

  function mostrarPercurso(percurso) {
    var lista = document.getElementById('percurso');
    lista.textContent = '';
    (percurso || []).forEach(function (passo) {
      lista.appendChild(el('li', { texto: passo }));
    });
    document.getElementById('cadeia').hidden = !(percurso && percurso.length);
  }

  async function novoDesafio() {
    var dados = await api.get('/api/sessao/desafio');
    desafio = dados.desafio;
  }

  async function tentar(codigo) {
    var corpo = {
      email: guardadas.email,
      senha: guardadas.senha,
      desafio: desafio,
      dispositivo: casca.dispositivo(),
      lembrarDispositivo: guardadas.lembrar
    };
    if (codigo) {
      corpo.codigo = codigo;
    }
    try {
      var dados = await api.post('/api/sessao', corpo);
      mostrarPercurso(dados.percurso);
      if (dados.situacao === 'EXIGE_DOIS_FATORES') {
        desafio = dados.novoDesafio;
        credenciais.hidden = true;
        segundoFator.hidden = false;
        document.getElementById('codigo-simulado').textContent = dados.codigoSimulado;
        mostrarAviso('Olá, ' + dados.nome + '. Falta confirmar o código.', 'ok');
        segundoFator.elements.codigo.focus();
        return;
      }
      api.guardarSessao({ token: dados.token, nome: dados.nome, usuarioId: dados.usuarioId, papel: dados.papel });
      window.location.assign(destino());
    } catch (erro) {
      mostrarAviso(erro.message, 'erro');
      credenciais.hidden = false;
      segundoFator.hidden = true;
      segundoFator.reset();
      await novoDesafio();
    }
  }

  credenciais.addEventListener('submit', function (evento) {
    evento.preventDefault();
    var f = credenciais.elements;
    if (!f.email.value || !f.senha.value) {
      mostrarAviso('Informe e-mail e senha.', 'erro');
      return;
    }
    guardadas = { email: f.email.value, senha: f.senha.value, lembrar: f.lembrar.checked };
    tentar(null);
  });

  segundoFator.addEventListener('submit', function (evento) {
    evento.preventDefault();
    var codigo = segundoFator.elements.codigo.value.trim();
    if (!/^\d{6}$/.test(codigo)) {
      mostrarAviso('O código tem 6 dígitos.', 'erro');
      return;
    }
    tentar(codigo);
  });

  if (api.sessao() && api.sessao().token) {
    mostrarAviso('Você já entrou como ' + api.sessao().nome + '. Entrar de novo troca a sessão.', '');
  }
  novoDesafio().catch(function (erro) { mostrarAviso(erro.message, 'erro'); });
})();
