package comercio.teste;

/**
 * Conjunto de casos de teste sobre um mesmo assunto.
 *
 * Toda classe em teste/ cujo nome termina em "Teste" e implementa esta
 * interface e descoberta pelo Executor. Nao ha arquivo de registro.
 */
public interface Suite {
    void registrar(Casos c);
}
