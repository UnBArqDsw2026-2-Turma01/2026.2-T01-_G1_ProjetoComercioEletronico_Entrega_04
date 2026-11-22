package comercio.transacao.dominio;

import comercio.catalogo.dominio.Cotacao;
import comercio.contas.dominio.CEP;

/**
 * Porta para a Transportadora, sistema externo a plataforma.
 *
 * E a interface ICotacaoEntrega do Diagrama de Componentes da Entrega 2. O
 * dominio declara o que precisa; quem atende e um adaptador fora dele. Trocar
 * de transportadora e escrever outro adaptador, sem tocar em pedido, envio ou
 * catalogo (ADR-02 do DAS).
 */
public interface ICotacaoEntrega {
    Cotacao cotar(CEP destino, int pesoEmGramas);
}
