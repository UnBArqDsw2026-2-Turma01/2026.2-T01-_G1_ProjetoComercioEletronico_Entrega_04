package comercio.contas.aplicacao.acesso;

import java.util.Collections;
import java.util.List;

/** Resultado de uma tentativa de login, com o percurso da cadeia e os passos da fachada. */
public final class ResultadoLoginDTO {

    public final Veredito veredito;
    public final String usuarioId;
    public final String nome;
    public final String papel;
    public final String token;
    public final String protocolo;
    /** So em EXIGE_DOIS_FATORES: o codigo que o SMS simulado teria levado. */
    public final String codigoSimulado;
    public final List<String> percurso;
    public final List<String> passosDaSessao;

    ResultadoLoginDTO(Veredito veredito, String usuarioId, String nome, String papel, String token, String protocolo,
            String codigoSimulado, List<String> percurso, List<String> passosDaSessao) {
        this.veredito = veredito;
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.papel = papel;
        this.token = token;
        this.protocolo = protocolo;
        this.codigoSimulado = codigoSimulado;
        this.percurso = Collections.unmodifiableList(percurso);
        this.passosDaSessao = Collections.unmodifiableList(passosDaSessao);
    }
}
