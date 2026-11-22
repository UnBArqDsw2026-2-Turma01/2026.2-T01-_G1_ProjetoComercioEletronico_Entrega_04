# <Nome do Projeto>
# Documento de Arquitetura de Software (DAS)

**Versão <1.0>**

---

## Histórico da Revisão

| Data | Versão | Descrição | Autor |
| :---: | :---: | :--- | :--- |
| <dd/mmm/aaaa> | <1.0> | <Criação inicial do documento com as visões arquiteturais> | <Nome dos Autores> |

---

## Índice Analítico

- [1. Introdução](#1-introdução)
  - [1.1 Finalidade](#11-finalidade)
  - [1.2 Escopo](#12-escopo)
  - [1.3 Definições, Acrônimos e Abreviações](#13-definições-acrônimos-e-abreviações)
  - [1.4 Referências](#14-referências)
  - [1.5 Visão Geral](#15-visão-geral)
- [2. Representação Arquitetural](#2-representação-arquitetural)
- [3. Metas e Restrições da Arquitetura](#3-metas-e-restrições-da-arquitetura)
- [4. Visão de Casos de Uso](#4-visão-de-casos-de-uso)
  - [4.1 Realizações de Casos de Uso](#41-realizações-de-casos-de-uso)
- [5. Visão Lógica](#5-visão-lógica)
  - [5.1 Visão Geral](#51-visão-geral)
  - [5.2 Pacotes de Design Significativos do Ponto de Vista da Arquitetura](#52-pacotes-de-design-significativos-do-ponto-de-vista-da-arquitetura)
- [6. Visão de Processos](#6-visão-de-processos)
- [7. Visão de Implantação](#7-visão-de-implantação)
- [8. Visão da Implementação](#8-visão-da-implementação)
  - [8.1 Visão Geral](#81-visão-geral)
  - [8.2 Camadas](#82-camadas)
- [9. Visão de Dados (opcional)](#9-visão-de-dados-opcional)
- [10. Tamanho e Desempenho](#10-tamanho-e-desempenho)
- [11. Qualidade](#11-qualidade)
- [FOCO_01: DAS & Demonstração](#foco_01-das--demonstração)
- [FOCO_02: IA Generativa & Lições Aprendidas](#foco_02-ia-generativa--lições-aprendidas)

---

# 1. Introdução

## 1.1 Finalidade
Este documento oferece uma visão geral arquitetural abrangente do sistema, usando diversas visões arquiteturais (Modelo 4+1 do RUP) para representar diferentes aspectos essenciais da solução. O objetivo principal é capturar, justificar e comunicar as decisões arquiteturais significativas tomadas para a plataforma, servindo como guia técnico para desenvolvedores, arquitetos e partes interessadas.

## 1.2 Escopo
Descreve o escopo coberto por este documento, detalhando a arquitetura do sistema `<Nome do Projeto>`, incluindo subsistemas de catálogo, autenticação, checkout/pedidos, pagamentos e rastreamento. Especifica os limites da solução e como os artefatos de engenharia de software das etapas anteriores se conectam a esta arquitetura.

## 1.3 Definições, Acrônimos e Abreviações
Definições dos termos essenciais utilizados ao longo do documento:
- **DAS**: Documento de Arquitetura de Software.
- **RUP**: Rational Unified Process.
- **UML**: Unified Modeling Language.
- **API**: Application Programming Interface.
- **REST**: Representational State Transfer.
- **GoF**: Gang of Four (Padrões de Projeto Criacionais, Estruturais e Comportamentais).
- **GRASP**: General Responsibility Assignment Software Patterns.
- *(Consulte também o Léxico do projeto para definições de negócio).*

## 1.4 Referências
Relação de referências bibliográficas e artefatos de projeto consultados:
1. KRUCHTEN, Philippe. *The 4+1 View Model of Architecture*. IEEE Software, 1995.
2. GAMMA, Erich et al. *Design Patterns: Elements of Reusable Object-Oriented Software*. Addison-Wesley, 1994.
3. LARMAN, Craig. *Utilizando UML e Padrões*. Bookman, 2007.
4. Artefatos de Entregas Anteriores do Projeto (ver documento de rastreabilidade).

## 1.5 Visão Geral
Apresentação da organização do documento, descrevendo sucintamente o conteúdo de cada uma das seções e como navegar entre as visões arquiteturais.

---

# 2. Representação Arquitetural

A arquitetura do software é representada com base no consagrado **Modelo 4+1 Visões** de Philippe Kruchten:
- **Visão de Casos de Uso (Cenários):** Captura os requisitos funcionais críticos e o comportamento esperado do sistema sob a ótica dos atores externos.
- **Visão Lógica:** Descreve a decomposição estrutural do sistema em pacotes, subsistemas, classes e padrões de projeto (GoF e GRASP).
- **Visão de Processos:** Foca na dinâmica de execução em tempo de execução, comunicação entre processos, concorrência, threads e mensageria assíncrona.
- **Visão de Implantação:** Mapeia a topologia física da infraestrutura de hardware e nós de nuvem (servidores, balanceadores, clusters, bancos de dados).
- **Visão de Implementação:** Detalha a organização dos módulos de código-fonte, componentes empacotados, camadas lógicas e dependências de bibliotecas.

---

# 3. Metas e Restrições da Arquitetura

Descreve os requisitos não-funcionais fundamentais e restrições que moldaram as decisões arquiteturais:
- **Segurança e Privacidade:** Criptografia de dados sensíveis (senhas, dados bancários), tokenização JWT, princípios de conformidade com LGPD.
- **Desempenho e Escalabilidade:** Tempo de resposta reduzido para buscas e checkout, arquitetura elástica para picos de acesso.
- **Disponibilidade e Confiabilidade:** Resiliência a falhas de componentes individuais e garantia de integridade nas transações financeiras (ACID).
- **Restrições Tecnológicas e de Design:** Linguagens, frameworks adotados, padrões de interface RESTful e padrões arquiteturais estabelecidos pela equipe.

---

# 4. Visão de Casos de Uso

Apresentação dos casos de uso arquiteturalmente significantes (aqueles que exercitam os principais fluxos de negócio e impõem maior criticidade à arquitetura).

| Identificador | Caso de Uso | Ator Principal | Relevância Arquitetural |
| :---: | :--- | :--- | :--- |
| **UC01** | Autenticar Usuário com Múltiplos Fatores | Usuário | Validação de credenciais, cadeia de segurança e geração de token. |
| **UC02** | Realizar Busca de Produtos com Filtros | Comprador | Desempenho de consulta, indexação e paginação eficiente. |
| **UC03** | Finalizar Pedido de Compra (Checkout) | Comprador | Transação de compra, orquestração de pagamento e máquina de estados. |
| **UC04** | Processar Pagamento | Sistema de Pagamentos | Integração com gateways externos e atualização assíncrona via Observer. |

## 4.1 Realizações de Casos de Uso
Detalhamento de como os subsistemas e classes colaboram para cumprir os cenários selecionados.
*(Inserir diagramas de sequência, atividades e explicações das mensagens trocadas entre os objetos).*

---

# 5. Visão Lógica

## 5.1 Visão Geral
Apresenta a decomposição do modelo de design em camadas lógicas e subsistemas. A arquitetura segue uma separação limpa de responsabilidades baseada no estilo em camadas (*Presentation, Controller, Application/Service, Domain/Model, Infrastructure/Repository*).

*(Inserir Diagrama de Pacotes da Visão Lógica).*

## 5.2 Pacotes de Design Significativos do Ponto de Vista da Arquitetura

### 5.2.1 Pacote `Auth & Session`
- **Responsabilidade:** Gerenciamento de credenciais, login e tokens de sessão.
- **Padrões de Projeto Aplicados:** *Chain of Responsibility* (validação de etapas de login), *Builder* (construção de objetos de sessão complexos), *Facade* (fachada simplificada para emissão e validação de tokens).
- **Classes Principais:** `AuthController`, `AuthService`, `SessionFacade`, `TokenBuilder`.

### 5.2.2 Pacote `Catalog & Search`
- **Responsabilidade:** Gestão e exibição de produtos, filtros e categorização.
- **Padrões de Projeto Aplicados:** *Decorator* (refinamento dinâmico de filtros de busca).
- **Classes Principais:** `ProductController`, `ProductService`, `ProductRepository`, `FilterDecorator`.

### 5.2.3 Pacote `Order & Checkout`
- **Responsabilidade:** Gerenciamento do ciclo de vida das compras e carrinho.
- **Padrões de Projeto Aplicados:** *State* (máquina de estados do pedido: Pendente, Pago, Enviado, Entregue, Cancelado), *GRASP Controller*.
- **Classes Principais:** `OrderController`, `OrderService`, `OrderState`, `OrderRepository`.

### 5.2.4 Pacote `Payment`
- **Responsabilidade:** Processamento financeiro e notificação de eventos de pagamento.
- **Padrões de Projeto Aplicados:** *Observer* (notificação reativa de status de pagamento), *Strategy/Abstract Factory* (diferentes métodos de pagamento: Pix, Cartão, Boleto).
- **Classes Principais:** `PaymentProcessor`, `PaymentObserver`, `PaymentMethodStrategy`.

---

# 6. Visão de Processos

Descreve a decomposição em processos, concorrência e modelo de comunicação do sistema:
- **Fluxos Síncronos:** Requisições HTTP/HTTPS via REST API orquestradas pelo API Gateway.
- **Fluxos Assíncronos & Mensageria:** Publicação e consumo de eventos (ex.: confirmação de pagamento, disparo de e-mails, atualização de estoque) através de filas/mensageria.
- **Tratamento de Concorrência & Transações:** Controle de concorrência em operações críticas de reserva de estoque e confirmação financeira.

*(Inserir Diagramas de Sequência e de Comunicação ilustrando a concorrência e os estados dos processos).*

---

# 7. Visão de Implantação

Representa a topologia física da infraestrutura de execução:
- **Camada de Cliente (Front-end):** Aplicações Web SPA e Mobile consumindo as APIs corporativas.
- **Camada de Borda (Edge / CDN):** Distribuição de arquivos estáticos e proteção DDoS.
- **Camada de Aplicação (Backend):** Contêineres de microsserviços / serviços modulares hospedados em cluster gerenciado.
- **Camada de Persistência:** Instâncias de Banco de Dados Relacional (PostgreSQL/MySQL) para dados transacionais e cache em memória (Redis).

*(Inserir Diagrama de Implantação com os nós físicos, protocolos de rede e mapeamento de componentes).*

---

# 8. Visão da Implementação

## 8.1 Visão Geral
Descreve a organização dos módulos do código-fonte, gerenciamento de dependências e estrutura de pastas do projeto.

*(Inserir Diagrama de Componentes da Visão de Implementação).*

## 8.2 Camadas
- **Camada de Apresentação (View / UI):** Componentes visuais desacoplados e responsivos.
- **Camada de Controle (Controllers / Handlers):** Recepção de requisições, validação de DTOs e aplicação de padrões GRASP.
- **Camada de Serviço (Application Services):** Orquestração das regras de negócio e casos de uso da aplicação.
- **Camada de Domínio (Entities & Value Objects):** Regras invariantes, modelos de negócio e entidades.
- **Camada de Infraestrutura / Persistência (Repositories & Adapters):** Acesso a banco de dados, drivers e clientes de serviços externos.

---

# 9. Visão de Dados (Opcional)

Apresentação da perspectiva de persistência de dados:
- Modelo relacional das principais tabelas (`Users`, `Products`, `Orders`, `OrderItems`, `Payments`).
- Estratégias de integridade referencial, índices para consultas críticas e versionamento de esquemas de banco de dados.

---

# 10. Tamanho e Desempenho

- **Volume de Dados Projetado:** Dimensionamento de requisições concorrentes, catálogo de produtos e transações diárias.
- **Métricas de Latência:** SLA esperado de resposta para consultas (< 200ms) e checkout (< 1s).
- **Políticas de Cache:** Uso de cache distribuído em memória para reduzir carga no banco de dados.

---

# 11. Qualidade

Mapeamento da conformidade arquitetural com os atributos de qualidade (baseado no NFR Framework):
- **Extensibilidade:** Baixo acoplamento entre pacotes e uso de padrões de projeto facilitam a adição de novos meios de pagamento ou transportadoras sem alterar o núcleo.
- **Confiabilidade:** Isolamento de falhas, retentativas automáticas e garantia transacional.
- **Manutenibilidade:** Código modularizado em camadas bem definidas e documentadas.
- **Segurança:** Autenticação robusta, criptografia ponta a ponta e sanitização de entradas.

---

# FOCO_01: DAS & Demonstração

### Participantes no Foco_01
| Nome do Membro | Papel / Contribuição Principal |
| :--- | :--- |
| <Nome 1> | <Elaboração da Visão Lógica e Diagrama de Pacotes> |
| <Nome 2> | <Elaboração da Visão de Implantação e Casos de Uso> |
| <Nome 3> | <Desenvolvimento e execução prática da feature demonstrada> |
| <Nome 4> | <Revisão arquitetural, rastreabilidade e integração de modelos> |

### Metodologia do Foco_01
<Breve relato detalhando como a equipe se organizou (reuniões, divisão de visões, validação de diagramas e integração do código executável).>

### Vídeo de Demonstração
<Inserir link do vídeo demonstrando as features arquiteturais do sistema rodando em ambiente prático.>

---

# FOCO_02: IA Generativa & Lições Aprendidas

### Participantes no Foco_02

| Nome do Membro | Lições Aprendidas | Uso da IA Generativa (Senso Crítico) |
| :--- | :--- | :--- |
| <Nome 1> | <Lição aprendida sobre abstração e visões do RUP> | <Análise crítica do apoio da IA na geração/refinamento dos modelos e onde houve intervenção manual> |
| <Nome 2> | <Lição aprendida sobre padrões GoF/GRASP e coesão> | <Reflexão sobre as sugestões da IA e correções conceituais aplicadas> |
| <Nome 3> | <Lição aprendida sobre arquitetura em camadas e integração> | <Crítica sobre o código e diagramas gerados por IA vs conformidade UML> |
| <Nome 4> | <Lição aprendida sobre rastreabilidade de requisitos até a arquitetura> | <Avaliação da precisão técnica das respostas e prompts utilizados> |

---

## Versionamentos

| Nome do Membro | Contribuição | Data |
| :--- | :--- | :---: |
| <Nome> | <Descrição detalhada da contribuição no DAS ou Focos> | <dd/mm/aaaa> |
