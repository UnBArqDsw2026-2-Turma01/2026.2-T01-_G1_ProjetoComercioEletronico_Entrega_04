package comercio.teste;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Executor de testes sem dependencia externa.
 *
 * Uso: java comercio.teste.Executor <pasta das classes de teste> [filtro]
 *
 * Percorre a pasta, carrega toda classe terminada em "Teste" que implementa
 * Suite, executa os casos e termina com codigo 1 se algum falhar. O filtro
 * opcional restringe as suites pelo nome da classe.
 */
public final class Executor {

    private Executor() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("uso: Executor <pasta das classes de teste> [filtro]");
            System.exit(2);
        }
        File raiz = new File(args[0]);
        String filtro = args.length > 1 ? args[1] : "";

        List<String> classes = new ArrayList<String>();
        coletar(raiz, raiz, classes);
        Collections.sort(classes);

        int suites = 0;
        int aprovados = 0;
        List<String> falhas = new ArrayList<String>();

        for (String nome : classes) {
            if (!nome.contains(filtro)) {
                continue;
            }
            Class<?> tipo = Class.forName(nome);
            if (!Suite.class.isAssignableFrom(tipo) || tipo.isInterface()) {
                continue;
            }
            Suite suite = (Suite) tipo.getDeclaredConstructor().newInstance();
            Casos casos = new Casos();
            suite.registrar(casos);
            suites++;
            System.out.println(nome);
            for (Casos.Registro registro : casos.registros()) {
                try {
                    registro.caso.executar();
                    aprovados++;
                    System.out.println("  ok     " + registro.nome);
                } catch (Throwable falha) {
                    falhas.add(nome + " > " + registro.nome + "\n         " + descrever(falha));
                    System.out.println("  FALHA  " + registro.nome);
                }
            }
        }

        System.out.println();
        if (suites == 0) {
            System.out.println("Nenhuma suite encontrada em " + raiz + (filtro.isEmpty() ? "" : " com o filtro " + filtro));
            System.exit(1);
        }
        for (String falha : falhas) {
            System.out.println("FALHA: " + falha);
        }
        System.out.println(suites + " suite(s), " + (aprovados + falhas.size()) + " caso(s), "
                + falhas.size() + " falha(s)");
        System.exit(falhas.isEmpty() ? 0 : 1);
    }

    private static void coletar(File raiz, File pasta, List<String> classes) {
        File[] filhos = pasta.listFiles();
        if (filhos == null) {
            return;
        }
        for (File filho : filhos) {
            if (filho.isDirectory()) {
                coletar(raiz, filho, classes);
            } else if (filho.getName().endsWith("Teste.class")) {
                String relativo = raiz.toURI().relativize(filho.toURI()).getPath();
                classes.add(relativo.substring(0, relativo.length() - ".class".length()).replace('/', '.'));
            }
        }
    }

    private static String descrever(Throwable falha) {
        StringBuilder texto = new StringBuilder(falha.getClass().getSimpleName());
        if (falha.getMessage() != null) {
            texto.append(": ").append(falha.getMessage());
        }
        StackTraceElement[] pilha = falha.getStackTrace();
        for (StackTraceElement elemento : pilha) {
            if (elemento.getClassName().startsWith("comercio.") && !elemento.getClassName().startsWith("comercio.teste.")) {
                texto.append(" (").append(elemento.getFileName()).append(":").append(elemento.getLineNumber()).append(")");
                break;
            }
        }
        return texto.toString();
    }
}
