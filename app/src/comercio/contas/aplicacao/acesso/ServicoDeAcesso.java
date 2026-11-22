package comercio.contas.aplicacao.acesso;

import java.util.ArrayList;
import java.util.List;

import comercio.contas.dominio.Usuario;
import comercio.contas.persistencia.RepositorioDeContas;
import comercio.contas.persistencia.RepositorioDeCredenciais;

/**
 * Orquestra o login: passa a requisicao pela cadeia de validacao e, aprovada,
 * pede a sessao a fachada.
 *
 * A cadeia e montada aqui, em uma linha, e a ordem importa: o captcha vem
 * antes das credenciais para que um robo nao consiga testar senhas, e o risco
 * vem antes do segundo fator porque e ele quem decide se o codigo e cobrado.
 */
public final class ServicoDeAcesso implements IAcesso {

    private final RepositorioDeContas contas;
    private final DesafiosDeCaptcha desafios;
    private final CodigosDeVerificacao codigos;
    private final FachadaDeSessao sessao;
    private final ValidadorDeLogin cadeia;

    public ServicoDeAcesso(RepositorioDeContas contas, RepositorioDeCredenciais credenciais,
            DesafiosDeCaptcha desafios, CodigosDeVerificacao codigos, RegistroDeDispositivos dispositivos,
            FachadaDeSessao sessao) {
        this.contas = contas;
        this.desafios = desafios;
        this.codigos = codigos;
        this.sessao = sessao;
        this.cadeia = new ValidadorCaptcha(desafios);
        cadeia.encadear(new ValidadorCredenciais(contas, credenciais))
                .encadear(new ValidadorRisco(dispositivos))
                .encadear(new ValidadorDoisFatores(codigos));
    }

    @Override
    public String desafio() {
        return desafios.emitir();
    }

    @Override
    public ResultadoLoginDTO entrar(RequisicaoLogin requisicao) {
        ContextoDeLogin contexto = new ContextoDeLogin(requisicao);
        Veredito veredito = cadeia.validar(contexto);
        List<String> percurso = new ArrayList<String>(contexto.percurso());
        if (veredito == Veredito.RECUSADO) {
            return new ResultadoLoginDTO(veredito, null, null, null, null, null, null, percurso,
                    new ArrayList<String>());
        }
        Usuario usuario = contas.usuario(contexto.usuarioId());
        if (veredito == Veredito.EXIGE_DOIS_FATORES) {
            String codigo = codigos.ultimoEnviado(usuario.id(), requisicao.dispositivo());
            return new ResultadoLoginDTO(veredito, null, usuario.nome(), null, null, null, codigo, percurso,
                    new ArrayList<String>());
        }
        FachadaDeSessao.Sessao aberta = sessao.abrir(usuario.id(), requisicao);
        return new ResultadoLoginDTO(veredito, usuario.id(), usuario.nome(), usuario.papel(), aberta.token,
                aberta.protocolo, null, percurso, aberta.passos);
    }

    @Override
    public boolean sair(String token, String ipOrigem) {
        return sessao.encerrar(token, ipOrigem);
    }

    @Override
    public String usuarioDoToken(String token) {
        return sessao.usuarioDoToken(token);
    }
}
