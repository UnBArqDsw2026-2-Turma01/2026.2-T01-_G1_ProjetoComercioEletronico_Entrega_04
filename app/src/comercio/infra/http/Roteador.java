package comercio.infra.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import comercio.nucleo.AcessoNegadoException;
import comercio.nucleo.NaoAutenticadoException;
import comercio.nucleo.NaoEncontradoException;
import comercio.nucleo.RegraDeNegocioException;

/**
 * Encaminha cada requisicao para a rota correspondente e padroniza a resposta.
 *
 * Tres responsabilidades, todas transversais aos controladores:
 *
 * 1. Sessao: se a requisicao traz token, pergunta ao Autenticador de quem ele e.
 * 2. Erros: converte as excecoes do nucleo no codigo HTTP correto, de modo que
 *    nenhum controlador precise montar resposta de erro.
 * 3. Rastreabilidade: acrescenta a toda resposta o campo "arquitetura", com os
 *    casos de uso, as linhas da matriz e as classes que a rota declarou.
 *
 * Nao depende do servidor HTTP: recebe Requisicao e devolve Resposta, o que o
 * torna testavel sem rede.
 */
public final class Roteador {

    private final List<Rota> rotas = new ArrayList<Rota>();
    private Autenticador autenticador;

    public Roteador registrar(ModuloDeRotas modulo) {
        for (Rota rota : modulo.rotas()) {
            registrar(rota);
        }
        return this;
    }

    public Roteador registrar(Rota nova) {
        for (Rota existente : rotas) {
            if (existente.metodo().equals(nova.metodo()) && existente.padrao().equals(nova.padrao())) {
                throw new IllegalStateException("Rota registrada duas vezes: " + nova);
            }
        }
        rotas.add(nova);
        return this;
    }

    public Roteador autenticarCom(Autenticador novo) {
        this.autenticador = novo;
        return this;
    }

    public List<Rota> rotas() {
        return Collections.unmodifiableList(rotas);
    }

    public Resposta despachar(Requisicao requisicao) {
        Rota escolhida = null;
        Map<String, String> parametros = null;
        boolean caminhoExiste = false;
        for (Rota rota : rotas) {
            Map<String, String> casados = rota.casar(requisicao.caminho());
            if (casados == null) {
                continue;
            }
            caminhoExiste = true;
            if (rota.metodo().equals(requisicao.metodo())) {
                escolhida = rota;
                parametros = casados;
                break;
            }
        }
        if (escolhida == null) {
            if (caminhoExiste) {
                return Resposta.erro(405, "METODO_NAO_PERMITIDO",
                        "O caminho existe, mas não aceita " + requisicao.metodo());
            }
            return Resposta.erro(404, "ROTA_INEXISTENTE", "Não há rota para " + requisicao.caminho());
        }

        Requisicao completa = requisicao.comParametros(parametros);
        if (autenticador != null && completa.usuarioId() == null && completa.token() != null) {
            completa = completa.comUsuario(autenticador.usuarioDoToken(completa.token()));
        }

        Resposta resposta;
        try {
            resposta = escolhida.tratador().tratar(completa);
            if (resposta == null) {
                throw new IllegalStateException("O tratador de " + escolhida + " devolveu null");
            }
        } catch (RegraDeNegocioException e) {
            resposta = Resposta.erro(422, e.codigo(), e.getMessage())
                    .mecanismo("Regra de negócio " + e.codigo() + " recusou a operação");
        } catch (NaoEncontradoException e) {
            resposta = Resposta.erro(404, "NAO_ENCONTRADO", e.getMessage());
        } catch (NaoAutenticadoException e) {
            resposta = Resposta.erro(401, "NAO_AUTENTICADO", e.getMessage());
        } catch (AcessoNegadoException e) {
            resposta = Resposta.erro(403, "ACESSO_NEGADO", e.getMessage());
        } catch (RequisicaoInvalidaException e) {
            resposta = Resposta.erro(400, "REQUISICAO_INVALIDA", e.getMessage());
        } catch (Exception e) {
            // Nunca devolver detalhes internos ao cliente: so o tipo, para diagnostico.
            resposta = Resposta.erro(500, "ERRO_INTERNO",
                    "Falha inesperada ao processar a requisição (" + e.getClass().getSimpleName() + ")");
        }
        return anexarArquitetura(resposta, escolhida);
    }

    private static Resposta anexarArquitetura(Resposta resposta, Rota rota) {
        Map<String, Object> arquitetura = new LinkedHashMap<String, Object>();
        arquitetura.put("rota", rota.toString());
        arquitetura.put("uc", rota.ucs());
        arquitetura.put("tr", rota.trs());
        arquitetura.put("classes", rota.classesEnvolvidas());
        arquitetura.put("mecanismo", resposta.mecanismo());

        Map<String, Object> corpo = new LinkedHashMap<String, Object>();
        if (resposta.corpo() != null) {
            corpo.putAll(resposta.corpo());
        }
        corpo.put("arquitetura", arquitetura);
        return resposta.comCorpo(corpo);
    }
}
