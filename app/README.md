# Aplicação — G1_ProjetoComercioEletronico

Software que serve de **evidência de execução** ao Documento de Arquitetura de Software da Entrega 4. Ele reaproveita o código Java dos padrões de projeto da Entrega 3 e o reorganiza nas camadas e nos pacotes definidos pela Visão Lógica do DAS.

## Como executar

Requisito único: **JDK 11 ou superior**. Não há biblioteca externa nem etapa de instalação.

```bash
cd app
make run          # sobe a loja em http://localhost:8080
make test         # compila e executa todos os testes
make arquitetura  # executa só a verificação das regras de dependência
make verificar    # o que a integração contínua executa
make demo         # roteiro no terminal
make clean
```

Para outra porta: `make run PORTA=9090`.

## Estrutura

Cada pacote Java tem a forma `comercio.<domínio>.<camada>`. É a matriz "camadas × pacotes de domínio" da seção 5.1 do DAS escrita no nome do pacote.

```
app/
  src/comercio/
    Aplicacao.java        raiz de composição: monta o sistema e sobe o servidor
    nucleo/               Dinheiro e as exceções com o código da regra de negócio
    infra/http, infra/json  servidor, roteador e JSON sobre o JDK
    contas/               Contas e Acesso
    catalogo/             Catálogo e Descoberta
    transacao/            Transação
    externos/             simuladores dos sistemas externos (portas do domínio)
    demonstracao/         roteiros de terminal
    importado/            código da Entrega 3 que ainda aguarda integração
  teste/comercio/         testes, no mesmo arranjo de pacotes
  web/                    camada de Apresentação: páginas que só falam com a API
```

| Camada do DAS (5.1) | Onde fica |
| :-- | :-- |
| Apresentação | `web/` |
| Controle | `comercio.<domínio>.controle` |
| Aplicação | `comercio.<domínio>.aplicacao` |
| Domínio | `comercio.<domínio>.dominio` |
| Persistência | `comercio.<domínio>.persistencia` |
| Sistemas externos | `comercio.externos` |

## Regras de dependência verificadas por teste

As tabelas das seções 5.1 e 5.2.5 do DAS estão transcritas em `teste/comercio/arquitetura/RegrasDeArquitetura.java`. O teste lê as linhas `import` de todo o código e falha apontando o arquivo, a importação e a seção do DAS violada.

| Pacote | Pode depender de |
| :-- | :-- |
| `*.dominio` | `dominio` (do próprio pacote ou de pacote permitido), `nucleo` |
| `*.persistencia` | `dominio`, `nucleo` |
| `*.aplicacao` | `dominio`, `persistencia`, `aplicacao`, `nucleo` |
| `*.controle` | `aplicacao`, `infra`, `nucleo` |
| `externos` | `dominio`, `nucleo` |

| Pacote de domínio | Pode depender de |
| :-- | :-- |
| `contas` | nenhum |
| `catalogo` | `contas` |
| `transacao` | `catalogo`, `contas` |
| `posvenda` | `transacao`, `catalogo`, `contas` |

## Rastreabilidade no código

Cada rota declara os casos de uso do DAS que realiza, as linhas da matriz de rastreabilidade a que responde e as classes que participam:

```java
Rota.get("/api/busca", tratador)
    .uc("UC01", "UC03", "UC04", "UC30")
    .tr("TR-01")
    .classes("ControladorDeCatalogo", "ServicoDeCatalogo", "RefinamentoDecorator");
```

Essa declaração volta em toda resposta, no campo `arquitetura`, e é listada em `GET /api/rastreabilidade`. A lateral "Arquitetura em execução" das páginas é desenhada a partir dela.

## Origem do código e estado da integração

O código da Entrega 3 foi importado do commit [`2997515`](https://github.com/UnBArqDsw2026-2-Turma01/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_03/tree/2997515) sem alteração de lógica, e cada módulo sai de `importado/` ao ser distribuído nas camadas. Os cabeçalhos de autoria dos arquivos originais foram mantidos.

| Módulo da Entrega 3 | Autoria | Destino | Estado |
| :-- | :-- | :-- | :-- |
| Decorator, refinamento da busca | Subequipe 01 | `catalogo.dominio.refinamento` | integrado |
| Abstract Factory, meios de pagamento | Subequipe 01 | `transacao` | em `importado/` |
| State, fluxo do pedido | Subequipe 03 | `transacao` | em `importado/` |
| Observer, pagamento | Subequipe 03 | `transacao` | em `importado/` |
| Builder, Chain of Responsibility, Facade e GRASP Controller, login | Subequipe 03 | `contas` | em `importado/` |

## Como escrever um teste

Toda classe em `teste/` cujo nome termina em `Teste` e implementa `Suite` é descoberta sozinha. O nome do caso começa pelo código da regra de negócio ou do caso de uso que ele verifica.

```java
public final class CepTeste implements Suite {
    public void registrar(Casos c) {
        c.caso("RN-B04: o CEP é guardado como oito dígitos", () -> {
            Verifica.igual("72230130", CEP.de("72230-130").digitos());
        });
    }
}
```

## Limites

- A persistência é em memória: os dados voltam à semente a cada execução.
- Os sistemas externos (transportadora, operadora de pagamento) são simuladores. Valores marcados como `INFERIDO` no código não foram observados na Engenharia Reversa.
- A verificação de arquitetura lê as linhas `import`; uma referência escrita por extenso no meio do código não é vista.
