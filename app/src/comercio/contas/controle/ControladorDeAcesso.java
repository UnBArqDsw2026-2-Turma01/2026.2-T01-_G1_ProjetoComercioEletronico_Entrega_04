package comercio.contas.controle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import comercio.contas.aplicacao.IConta;
import comercio.contas.aplicacao.PerfilDTO;
import comercio.contas.aplicacao.acesso.IAcesso;
import comercio.contas.aplicacao.acesso.RequisicaoLogin;
import comercio.contas.aplicacao.acesso.RequisicaoLoginConcretoBuilder;
import comercio.contas.aplicacao.acesso.ResultadoLoginDTO;
import comercio.contas.aplicacao.acesso.Veredito;
import comercio.infra.http.ModuloDeRotas;
import comercio.infra.http.Requisicao;
import comercio.infra.http.Resposta;
import comercio.infra.http.Rota;

/**
 * Controller (GRASP): o primeiro objeto, depois da tela, a receber os eventos
 * de sistema do login, da saida e da consulta da conta.
 *
 * Ele nao valida nada sozinho. Monta a RequisicaoLogin pelo Builder (fazendo o
 * papel de diretor), entrega ao ServicoDeAcesso, que roda a Chain of
 * Responsibility e abre a sessao pela Facade, e traduz o resultado em HTTP.
 * Na Entrega 3 esse papel era do ControladorDeAutenticacao, que criava as
 * proprias dependencias; aqui elas chegam pelo construtor.
 */
public final class ControladorDeAcesso implements ModuloDeRotas {

    private static final String IP_LOCAL = "127.0.0.1";

    private final IAcesso acesso;
    private final IConta conta;

    public ControladorDeAcesso(IAcesso acesso, IConta conta) {
        this.acesso = acesso;
        this.conta = conta;
    }

    @Override
    public List<Rota> rotas() {
        List<Rota> rotas = new ArrayList<Rota>();
        rotas.add(Rota.get("/api/sessao/desafio", r -> {
            Map<String, Object> corpo = new LinkedHashMap<String, Object>();
            corpo.put("desafio", acesso.desafio());
            return Resposta.ok(corpo).mecanismo("Desafio de captcha emitido ao abrir a página (Achado-L01); "
                    + "vale para uma tentativa");
        }).uc("UC22").tr("TR-08").classes("ControladorDeAcesso", "ServicoDeAcesso", "DesafiosDeCaptcha"));

        rotas.add(Rota.post("/api/sessao", this::entrar).uc("UC22").tr("TR-08").classes("ControladorDeAcesso",
                "RequisicaoLoginConcretoBuilder", "ServicoDeAcesso", "ValidadorCaptcha", "ValidadorCredenciais",
                "ValidadorRisco", "ValidadorDoisFatores", "FachadaDeSessao"));

        rotas.add(Rota.delete("/api/sessao", r -> {
            r.exigirUsuario();
            acesso.sair(r.token(), IP_LOCAL);
            Map<String, Object> corpo = new LinkedHashMap<String, Object>();
            corpo.put("situacao", "sessão encerrada");
            return Resposta.ok(corpo).mecanismo("FachadaDeSessao revogou o token e registrou a saída na auditoria");
        }).uc("UC22").tr("TR-08").classes("ControladorDeAcesso", "ServicoDeAcesso", "FachadaDeSessao"));

        rotas.add(Rota.get("/api/conta", r -> {
            PerfilDTO perfil = conta.perfil(r.exigirUsuario());
            List<Object> enderecos = new ArrayList<Object>();
            for (PerfilDTO.EnderecoDTO e : perfil.enderecos) {
                Map<String, Object> endereco = new LinkedHashMap<String, Object>();
                endereco.put("id", e.id);
                endereco.put("apelido", e.apelido);
                endereco.put("resumo", e.resumo);
                endereco.put("cep", e.cep);
                enderecos.add(endereco);
            }
            Map<String, Object> corpo = new LinkedHashMap<String, Object>();
            corpo.put("id", perfil.id);
            corpo.put("nome", perfil.nome);
            corpo.put("email", perfil.email);
            corpo.put("papel", perfil.papel);
            corpo.put("enderecos", enderecos);
            return Resposta.ok(corpo).mecanismo("O usuário vem do token da sessão; nenhum identificador é aceito "
                    + "do corpo ou da URL");
        }).uc("UC09", "UC23").tr("TR-04", "TR-08").classes("ControladorDeAcesso", "ServicoDeConta",
                "RepositorioDeContas"));
        return rotas;
    }

    private Resposta entrar(Requisicao r) {
        // Diretor do Builder: a ordem das etapas e a mesma para todo login vindo da web.
        RequisicaoLogin requisicao = new RequisicaoLoginConcretoBuilder()
                .comCredenciais(r.texto("email"), r.texto("senha"))
                .comTokenCaptcha(r.texto("desafio"))
                .comTelemetria(IP_LOCAL, r.texto("dispositivo"))
                .comSegundoFator(r.texto("codigo"))
                .lembrandoDispositivo(r.logico("lembrarDispositivo", false))
                .build();
        ResultadoLoginDTO resultado = acesso.entrar(requisicao);
        String cadeia = "Chain of Responsibility: " + juntar(resultado.percurso, " → ");

        if (resultado.veredito == Veredito.RECUSADO) {
            Resposta recusa = Resposta.erro(401, "LOGIN", "Não foi possível entrar. Confira e-mail e senha e "
                    + "tente de novo.");
            recusa.corpo().put("novoDesafio", acesso.desafio());
            return recusa.mecanismo(cadeia);
        }
        Map<String, Object> corpo = new LinkedHashMap<String, Object>();
        corpo.put("situacao", resultado.veredito.name());
        corpo.put("percurso", resultado.percurso);
        if (resultado.veredito == Veredito.EXIGE_DOIS_FATORES) {
            corpo.put("nome", resultado.nome);
            corpo.put("canal", "SMS simulado");
            corpo.put("codigoSimulado", resultado.codigoSimulado);
            corpo.put("novoDesafio", acesso.desafio());
            return Resposta.ok(corpo).mecanismo(cadeia);
        }
        corpo.put("token", resultado.token);
        corpo.put("usuarioId", resultado.usuarioId);
        corpo.put("nome", resultado.nome);
        corpo.put("papel", resultado.papel);
        corpo.put("protocolo", resultado.protocolo);
        corpo.put("passosDaSessao", resultado.passosDaSessao);
        return Resposta.ok(corpo).mecanismo("Builder montou a requisição. " + cadeia + ". Facade: "
                + juntar(resultado.passosDaSessao, "; ") + ".");
    }

    private static String juntar(List<String> partes, String separador) {
        StringBuilder texto = new StringBuilder();
        for (String parte : partes) {
            if (texto.length() > 0) {
                texto.append(separador);
            }
            texto.append(parte);
        }
        return texto.toString();
    }
}
