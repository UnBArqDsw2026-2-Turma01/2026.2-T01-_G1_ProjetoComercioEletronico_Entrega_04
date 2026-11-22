package comercio.contas.dominio;

/**
 * Endereco de entrega cadastrado na conta do comprador.
 *
 * Origem: RN-B02 — o endereco e dado da conta, nao do pedido. E escolhido em
 * tela propria e reutilizado entre compras. Por isso vive em Contas e Acesso e
 * o envio apenas o referencia (decisao 4 do Diagrama de Classes da Entrega 2).
 */
public final class Endereco {

    private final String id;
    private final String apelido;
    private final String logradouro;
    private final String numero;
    private final String cidade;
    private final String uf;
    private final CEP cep;

    public Endereco(String id, String apelido, String logradouro, String numero, String cidade, String uf, CEP cep) {
        this.id = exigir(id, "identificador");
        this.apelido = exigir(apelido, "apelido");
        this.logradouro = exigir(logradouro, "logradouro");
        this.numero = exigir(numero, "numero");
        this.cidade = exigir(cidade, "cidade");
        this.uf = exigir(uf, "UF");
        if (cep == null) {
            throw new IllegalArgumentException("Endereco sem CEP");
        }
        this.cep = cep;
    }

    private static String exigir(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("Endereco sem " + campo);
        }
        return valor.trim();
    }

    public String id() {
        return id;
    }

    public String apelido() {
        return apelido;
    }

    public String logradouro() {
        return logradouro;
    }

    public String numero() {
        return numero;
    }

    public String cidade() {
        return cidade;
    }

    public String uf() {
        return uf;
    }

    public CEP cep() {
        return cep;
    }

    public String resumo() {
        return logradouro + ", " + numero + " - " + cidade + "/" + uf + " - CEP " + cep.formatado();
    }

    @Override
    public String toString() {
        return apelido + ": " + resumo();
    }
}
