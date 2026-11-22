package comercio.transacao.aplicacao;

import java.util.Collections;
import java.util.List;

/** Um meio de pagamento oferecido e o que a familia dele entrega (UC24). */
public final class MeioDTO {

    public final String meio;
    public final String rotulo;
    public final String familia;
    public final boolean assincrono;
    public final int janelaEmMinutos;
    public final List<String> classesDoTrio;

    public MeioDTO(String meio, String rotulo, String familia, boolean assincrono, int janelaEmMinutos,
            List<String> classesDoTrio) {
        this.meio = meio;
        this.rotulo = rotulo;
        this.familia = familia;
        this.assincrono = assincrono;
        this.janelaEmMinutos = janelaEmMinutos;
        this.classesDoTrio = Collections.unmodifiableList(classesDoTrio);
    }
}
