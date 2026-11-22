package comercio.contas.aplicacao;

/** Consulta da propria conta (UC09, UC23). */
public interface IConta {
    PerfilDTO perfil(String usuarioId);
}
