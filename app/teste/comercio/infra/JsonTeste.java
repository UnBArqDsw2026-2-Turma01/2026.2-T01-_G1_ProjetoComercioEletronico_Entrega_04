package comercio.infra;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import comercio.infra.json.ErroDeJson;
import comercio.infra.json.Json;
import comercio.teste.Casos;
import comercio.teste.Suite;
import comercio.teste.Verifica;

public final class JsonTeste implements Suite {

    private enum Cor { AZUL }

    @Override
    public void registrar(Casos c) {
        c.caso("escreve objeto na ordem de insercao, com lista, numero, booleano e nulo", () -> {
            Map<String, Object> mapa = new LinkedHashMap<String, Object>();
            mapa.put("titulo", "Fone");
            mapa.put("centavos", 19990L);
            mapa.put("novo", true);
            mapa.put("frete", null);
            List<Object> selos = new ArrayList<Object>();
            selos.add("a");
            selos.add(2);
            mapa.put("selos", selos);
            mapa.put("cor", Cor.AZUL);
            Verifica.igual("{\"titulo\":\"Fone\",\"centavos\":19990,\"novo\":true,\"frete\":null,"
                    + "\"selos\":[\"a\",2],\"cor\":\"AZUL\"}", Json.escrever(mapa));
        });

        c.caso("escapa aspas, barra invertida, quebra de linha e caractere de controle", () -> {
            Verifica.igual("\"a\\\"b\\\\c\\nd\\u0001\"", Json.escrever("a\"b\\c\nd\u0001"));
        });

        c.caso("texto com acento atravessa escrita e leitura sem alteracao", () -> {
            String original = "Reputação: ótima — 4,8";
            Verifica.igual(original, Json.ler(Json.escrever(original)));
        });

        c.caso("le objeto aninhado, inteiro como Long e decimal como Double", () -> {
            Map<String, Object> lido = Json.lerObjeto(
                    " { \"anuncioId\" : \"anu-001\", \"quantidade\": 2, \"nota\": 4.5, \"itens\": [1, {\"x\": null}] } ");
            Verifica.igual("anu-001", lido.get("anuncioId"));
            Verifica.igual(Long.valueOf(2), lido.get("quantidade"));
            Verifica.igual(Double.valueOf(4.5), lido.get("nota"));
            List<?> itens = (List<?>) lido.get("itens");
            Verifica.igual(2, itens.size());
            Verifica.verdadeiro(((Map<?, ?>) itens.get(1)).containsKey("x"), "o campo nulo deve existir no mapa");
        });

        c.caso("le escape unicode", () -> {
            Verifica.igual("é", Json.ler("\"\\u00e9\""));
        });

        c.caso("corpo vazio vira objeto vazio", () -> {
            Verifica.igual(0, Json.lerObjeto("  ").size());
        });

        c.caso("recusa JSON malformado em vez de devolver dado parcial", () -> {
            Verifica.lanca(ErroDeJson.class, () -> Json.ler("{\"a\": 1,}"));
            Verifica.lanca(ErroDeJson.class, () -> Json.ler("{\"a\" 1}"));
            Verifica.lanca(ErroDeJson.class, () -> Json.ler("[1, 2"));
            Verifica.lanca(ErroDeJson.class, () -> Json.ler("\"aberto"));
            Verifica.lanca(ErroDeJson.class, () -> Json.ler("{} sobra"));
            Verifica.lanca(ErroDeJson.class, () -> Json.lerObjeto("[1]"));
        });

        c.caso("recusa escrever tipo sem representacao, para o controlador converter o DTO", () -> {
            Verifica.lanca(IllegalArgumentException.class, () -> Json.escrever(new Object()));
        });
    }
}
