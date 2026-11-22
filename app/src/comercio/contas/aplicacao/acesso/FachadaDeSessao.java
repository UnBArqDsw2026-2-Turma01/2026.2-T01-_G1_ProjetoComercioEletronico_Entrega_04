package comercio.contas.aplicacao.acesso;

import java.util.ArrayList;
import java.util.List;

/**
 * Facade (GoF): abrir uma sessao toca tres subsistemas, e quem pede a sessao
 * nao precisa conhecer nenhum deles nem a ordem em que trabalham.
 *
 * Na Entrega 3 havia um quarto subsistema, o replicador de SSO entre os
 * produtos do ecossistema (Achado-L05). Ele saiu: esta aplicacao e um produto
 * so, e replicar sessao para lugar nenhum seria simular por simular.
 */
public final class FachadaDeSessao {

    private final EmissorDeTokens tokens;
    private final RegistroDeDispositivos dispositivos;
    private final AuditoriaDeAcesso auditoria;

    public FachadaDeSessao(EmissorDeTokens tokens, RegistroDeDispositivos dispositivos, AuditoriaDeAcesso auditoria) {
        this.tokens = tokens;
        this.dispositivos = dispositivos;
        this.auditoria = auditoria;
    }

    /** Abre a sessao e devolve o token e o que cada subsistema fez. */
    public Sessao abrir(String usuarioId, RequisicaoLogin requisicao) {
        List<String> passos = new ArrayList<String>();
        String token = tokens.emitir(usuarioId);
        passos.add("EmissorDeTokens: token emitido");
        if (requisicao.lembrarDispositivo()) {
            dispositivos.lembrar(usuarioId, requisicao.dispositivo());
            passos.add("RegistroDeDispositivos: dispositivo marcado como confiável");
        } else {
            passos.add("RegistroDeDispositivos: dispositivo não guardado");
        }
        String protocolo = auditoria.registrar("ENTRADA", usuarioId, requisicao.ipOrigem());
        passos.add("AuditoriaDeAcesso: protocolo " + protocolo);
        return new Sessao(token, protocolo, passos);
    }

    public boolean encerrar(String token, String ipOrigem) {
        String usuario = tokens.usuarioDe(token);
        if (usuario == null) {
            return false;
        }
        tokens.revogar(token);
        auditoria.registrar("SAIDA", usuario, ipOrigem);
        return true;
    }

    public String usuarioDoToken(String token) {
        return tokens.usuarioDe(token);
    }

    /** O que a fachada devolve ao abrir a sessao. */
    public static final class Sessao {
        public final String token;
        public final String protocolo;
        public final List<String> passos;

        Sessao(String token, String protocolo, List<String> passos) {
            this.token = token;
            this.protocolo = protocolo;
            this.passos = passos;
        }
    }
}
