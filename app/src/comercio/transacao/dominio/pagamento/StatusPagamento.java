package comercio.transacao.dominio.pagamento;

/**
 * Situacoes de um pagamento. Cada mudanca de situacao e o evento que dispara a
 * notificacao dos observadores.
 *
 * Origem: padrao Observer da Entrega 3 (Subequipe 03). EXPIRADO foi
 * acrescentado na Entrega 4 para representar a janela de pagamento encerrada,
 * que na Entrega 3 existia apenas no Abstract Factory (Autorizacao.Status).
 */
public enum StatusPagamento {
    PENDENTE("Pendente"),
    EM_ANALISE("Em análise"),
    APROVADO("Aprovado"),
    RECUSADO("Recusado"),
    EXPIRADO("Expirado"),
    ESTORNADO("Estornado");

    private final String descricao;

    StatusPagamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
