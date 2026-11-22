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

O código da Entrega 3 foi importado do commit [`2997515`](https://github.com/UnBArqDsw2026-2-Turma01/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_03/tree/2997515) sem alteração de lógica e depois distribuído nas camadas, módulo a módulo, em commits separados. A área de espera `importado/` foi esvaziada quando o último módulo, o login, foi integrado; desde então o teste de arquitetura verifica todos os arquivos, sem isenção. Os cabeçalhos de autoria dos arquivos originais foram mantidos.

| Módulo da Entrega 3 | Autoria | Destino | Estado |
| :-- | :-- | :-- | :-- |
| Decorator, refinamento da busca | Subequipe 01 | `catalogo.dominio.refinamento` | integrado |
| Abstract Factory, meios de pagamento | Subequipe 01 | contrato em `transacao.dominio.pagamento`; famílias em `externos.pagamento` | integrado |
| State, fluxo do pedido | Subequipe 03 | `transacao.dominio.pedido` | integrado |
| Observer, pagamento | Subequipe 03 | `transacao.dominio.pagamento` e `transacao.aplicacao.observadores` | integrado |
| Builder, Chain of Responsibility, Facade e GRASP Controller, login | Subequipe 03 | `contas.aplicacao.acesso` e `contas.controle` | integrado |

As telas Swing e os programas de console de cada padrão não vieram para a aplicação: quem demonstra os padrões aqui são o roteiro de terminal e as páginas. Os originais continuam no repositório da Entrega 3.

## API

Todas as rotas respondem JSON. As que dependem de sessão leem o usuário do cabeçalho `Authorization: Bearer <token>`; nenhuma aceita o identificador do comprador no corpo ou na URL. A lista completa, com casos de uso e linhas da matriz, está em `GET /api/rastreabilidade`.

| Rota | Casos de uso | Padrão que atua |
| :-- | :-- | :-- |
| `GET /api/sessao/desafio` | UC22 | |
| `POST /api/sessao` | UC22 | Builder, Chain of Responsibility, Facade, Controller GRASP |
| `DELETE /api/sessao` | UC22 | Facade |
| `GET /api/conta` | UC09, UC23 | |
| `GET`, `POST /api/carrinho`, `/api/carrinho/itens` | UC08 | |
| `PATCH`, `DELETE /api/carrinho/itens/{anuncioId}` | UC08 | |
| `GET /api/meios-de-pagamento` | UC24 | Abstract Factory |
| `POST /api/pedidos` (do carrinho ou, com `anuncioId`, comprar agora) | UC10, UC11 | State |
| `GET /api/pedidos`, `GET /api/pedidos/{id}` | UC13 | State |
| `POST /api/pedidos/{id}/pagamento` | UC12, UC24, UC25 | Abstract Factory, Observer, State |
| `POST /api/pedidos/{id}/desistencia` | UC10 | State |
| `POST /api/pedidos/{id}/compensar` (simula o banco) | UC25 | Observer |
| `POST /api/pedidos/{id}/avancar` (simula a transportadora) | UC26, UC27 | State |

### Login de demonstração

As contas da semente usam a senha de demonstração `demo1234`, definida em `SementeDeContas.SENHA_DE_DEMONSTRACAO`. A compradora com endereços é `joana@exemplo.com`.

O login percorre quatro elos, nesta ordem: captcha, credenciais, risco e segundo fator. Num dispositivo que a conta ainda não marcou como confiável, o terceiro elo exige o segundo fator. O quarto elo então gera o código, e a resposta o devolve no campo `codigoSimulado`, no lugar do SMS. Se a tentativa marcar `lembrarDispositivo`, o próximo login nesse dispositivo passa sem código.

## Cenário CA-01: compra de ponta a ponta

`make demo` executa, no terminal e sem servidor, uma compra completa. Cada passo informa os casos de uso e a linha da matriz do DAS que realiza e qual padrão está trabalhando.

| Passo | Casos de uso | Padrão | Onde mora |
| :-- | :-- | :-- | :-- |
| Buscar e refinar, com frete por CEP | UC01, UC03, UC04, UC06, UC07 | Decorator | `catalogo.dominio.refinamento` |
| Carrinho agrupado por vendedor | UC08 | | `transacao.dominio.carrinho` |
| Finalizar a compra | UC10, UC09 | State (o pedido nasce) | `transacao.dominio.pedido` |
| Escolher o meio e pagar | UC12, UC24, UC25 | Abstract Factory e Observer | `transacao.dominio.pagamento` |
| Acompanhar o envio | UC13, UC26, UC27 | State | `transacao.dominio.pedido` |
| Estorno | UC21 | Abstract Factory (política da família) | `transacao.dominio.pagamento` |

O que o cenário demonstra e os testes verificam:

- **O frete da busca vem da Transação sem que o Catálogo a importe.** A porta `IFrete` é declarada em `catalogo.dominio` e implementada por `transacao.aplicacao.ServicoDeEntrega`.
- **Trocar o meio de pagamento não muda `ServicoDePagamentos`.** O serviço só conhece as interfaces do Abstract Factory; as classes concretas aparecem apenas em `comercio.Aplicacao`.
- **O checkout é tudo ou nada.** Se falta estoque de um item, ou a transportadora falha, o que já foi reservado é devolvido e nenhum pedido é criado.
- **Um pedido pago não é cobrado de novo**, e um pedido não pago não é despachado: o estado do pedido recusa a operação.
- **A janela de pagamento é da família**: dezesseis minutos expiram o cartão e não o boleto.

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

- A persistência é em memória: os dados voltam à semente a cada execução, e as sessões abertas se perdem quando o servidor reinicia.
- O fechamento atômico do checkout é garantido por compensação em memória, e não por transação de banco de dados.
- O pedido tem os seis estados implementados na Entrega 3; a Máquina de Estados da Entrega 2 prevê doze.
- Os sistemas externos (transportadora, operadora de pagamento, captcha e envio do código de verificação) são simuladores. Valores marcados como `INFERIDO` no código não foram observados na Engenharia Reversa.
- A verificação de arquitetura lê as linhas `import`; uma referência escrita por extenso no meio do código não é vista.
