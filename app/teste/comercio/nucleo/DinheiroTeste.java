package comercio.nucleo;

import java.util.List;

import comercio.teste.Casos;
import comercio.teste.Suite;
import comercio.teste.Verifica;

public final class DinheiroTeste implements Suite {

    @Override
    public void registrar(Casos c) {
        c.caso("parcelar nao perde centavos: tres parcelas de R$ 100,00 somam R$ 100,00", () -> {
            List<Dinheiro> parcelas = Dinheiro.emReais(10000).parcelar(3);
            Dinheiro soma = Dinheiro.zero();
            for (Dinheiro parcela : parcelas) {
                soma = soma.somar(parcela);
            }
            Verifica.igual(Dinheiro.emReais(10000), soma);
            Verifica.igual(Dinheiro.emReais(3334), parcelas.get(0));
            Verifica.igual(Dinheiro.emReais(3333), parcelas.get(2));
        });

        c.caso("o valor e inteiro em centavos e a apresentacao usa virgula", () -> {
            Verifica.igual("R$ 199,90", Dinheiro.emReais(19990).formatado());
            Verifica.igual("R$ 0,05", Dinheiro.emReais(5).formatado());
            Verifica.igual(Dinheiro.emReais(39980), Dinheiro.emReais(19990).multiplicar(2));
        });

        c.caso("valor negativo e soma entre moedas diferentes sao recusados", () -> {
            Verifica.lanca(IllegalArgumentException.class, () -> Dinheiro.emReais(-1));
            Verifica.lanca(IllegalArgumentException.class,
                    () -> Dinheiro.emReais(100).somar(new Dinheiro(100, "USD")));
        });

        c.caso("comparacao por valor", () -> {
            Verifica.verdadeiro(Dinheiro.emReais(200).maiorQue(Dinheiro.emReais(199)), "200 > 199");
            Verifica.falso(Dinheiro.emReais(199).maiorQue(Dinheiro.emReais(199)), "199 nao e maior que 199");
        });
    }
}
