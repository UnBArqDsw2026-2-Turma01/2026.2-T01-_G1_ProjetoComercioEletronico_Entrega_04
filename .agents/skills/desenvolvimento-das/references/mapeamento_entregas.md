# Matriz de Rastreabilidade e Mapeamento de Entregas para o DAS

Este documento serve como mapa de navegação e fonte canônica para o preenchimento das 11 seções do Documento de Arquitetura de Software (DAS) da **Subequipe 03**, correlacionando os artefatos desenvolvidos nas Entregas 01, 02 e 03.

---

## 1. Visão Geral das Entregas Anteriores

| Entrega | Foco Temático | Repositório / Documentação |
| :---: | :--- | :--- |
| **Entrega 01** | Desenho de Software (Base) | [Entrega 01 GitPages](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_01/#/Base/Relat%C3%B3rios/1.1.3.SubEquipe_03) |
| **Entrega 02** | Modelagem UML Estática e Dinâmica | [Entrega 02 GitPages](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_02/#/Base/Relat%C3%B3rios/Subequipe03/Metodologia) |
| **Entrega 03** | Padrões de Projeto (GoFs e GRASP) | [Entrega 03 GitPages](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_03/#/) |

---

## 2. Mapeamento Seção a Seção do DAS

### Seção 1: Introdução (Finalidade, Escopo, Léxico e Referências)
- **Artefatos Fonte (Entrega 01):**
  - [Rich Picture & Artefato Generalista](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_01/#/Base/Relat%C3%B3rios/SubEquipe03/ArtefatoGeneralista): Visão macro dos atores (Comprador, Vendedor, Aplicação, Transportadora, Instituição Financeira).
  - [5W2H](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_01/#/Base/Relat%C3%B3rios/SubEquipe03/5W2H): Escopo do projeto, justificativa e delimitações.
  - [Léxico do Sistema](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_01/#/Base/Relat%C3%B3rios/SubEquipe03/Lexico): Vocabulário ubíquo (ex.: Carrinho, Pedido, Sessão, Checkout, Devolução).
  - [Design Sprint](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_01/#/Base/Relat%C3%B3rios/SubEquipe03/DesignSprint): Definição de prioridades de experiência de usuário e valor de negócio.

---

### Seção 2: Representação Arquitetural
- **Fundamentação:**
  - Modelo 4+1 visões de Philippe Kruchten (1995).
  - Como cada visão aborda os objetivos da Subequipe 03 (Casos de Uso, Lógica, Processos, Implantação e Implementação).

---

### Seção 3: Metas e Restrições da Arquitetura
- **Artefatos Fonte (Entrega 01 e 02):**
  - [NFR Framework da Subequipe 01](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_01/#/Base/Relat%C3%B3rios/SubEquipe01/NFR): Grafo SIG centrado em Usabilidade, Eficiência de Uso, Prevenção de Erros e Satisfação.
  - [NFR Framework da Subequipe 03](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_01/#/Base/Relat%C3%B3rios/SubEquipe03/NFR): Metas não-funcionais (Desempenho, Segurança, Confiabilidade, Escalabilidade).
  - [Engenharia Reversa da Subequipe 01](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_01/#/Base/Relat%C3%B3rios/SubEquipe01/EngenhariaReversa): Recortes A (Busca), B (Carrinho/Checkout) e C (Anúncios).
  - [Diagrama de Componentes da Subequipe 01](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_02/#/Base/Relat%C3%B3rios/Subequipe01/ModelagemEstatica/DiagramaDeComponentes): Interfaces providas/requeridas (`ICatalogo`, `IFrete`, `IPagamento`, `IRastreio`) e decomposição em serviços.
  - [Metodologia Scrum Adaptado](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_02/#/Base/Relat%C3%B3rios/Subequipe03/Metodologia): Restrições de processo, cronograma e divisão de responsabilidades.

---

### Seção 4: Visão de Casos de Uso
- **Artefatos Fonte (Entrega 02):**
  - [Diagrama de Casos de Uso](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_02/#/Base/Relat%C3%B3rios/Subequipe03/ModelagemEstatica/DiagramaDeCasosDeUso): Identificação dos casos centrais (Autenticação, Compra, Pagamento, Devolução).
  - [Diagrama de Atividades](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_02/#/Base/Relat%C3%B3rios/Subequipe03/ModelagemDinamica/DiagramaDeAtividades): Fluxo detalhado de ações dos atores nas realizações dos casos de uso.
  - [Modelagem BPMN (E1)](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_01/#/Base/Relat%C3%B3rios/SubEquipe03/BPMN): Processos de negócio mapeados.

---

### Seção 5: Visão Lógica (Obrigatória na Entrega 04)
- **Artefatos Fonte (Entrega 02 e 03):**
  - [Diagrama de Pacotes (E2)](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_02/#/Base/Relat%C3%B3rios/Subequipe03/ModelagemEstatica/DiagramaDePacotes): Estruturação em camadas (View, Controller, Service, Model, Repository) e separação de subsistemas de domínio.
  - [Diagrama de Classes (E2)](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_02/#/Base/Relat%C3%B3rios/Subequipe03/ModelagemEstatica/DiagramaDeClasses): Entidades e Value Objects do domínio de e-commerce.
  - **Padrões de Projeto Integrados (Entrega 03):**
    - [State - Fluxo de Compra](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_03/#/Base/Relat%C3%B3rios/Subequipe03/GofsComportamentais/FluxoDeCompra(State)): Estados do pedido (`Pendente`, `Pago`, `Enviado`, `Concluído`).
    - [Observer / Strategy - Fluxo de Pagamento](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_03/#/Base/Relat%C3%B3rios/Subequipe03/GofsComportamentais/FluxoDePagamento(StrategyObserver)): Notificação de status e estratégias de liquidação financeira.
    - [Chain of Responsibility - Fluxo de Login](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_03/#/Base/Relat%C3%B3rios/Subequipe03/GofsComportamentais/FluxoDeLogin(ChainOfResponsibility)): Validações encadeadas de segurança e credenciais.
    - [Builder - Fluxo de Login e Sessão](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_03/#/Base/Relat%C3%B3rios/Subequipe03/GofsCriacionais/FluxoLogin(Builder)): Construção progressiva da sessão do usuário.
    - [Facade - Emissão de Sessão](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_03/#/Base/Relat%C3%B3rios/Subequipe03/GofsEstruturais/EmissaoDeSessao(Facade)): Interface unificada para controle e validação de tokens e sessões.
    - [GRASP Controller](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_03/#/Base/Relat%C3%B3rios/Subequipe03/GRASP/Controller): Atribuição coesa de responsabilidades na camada de controle.

---

### Seção 6: Visão de Processos
- **Artefatos Fonte (Entrega 02):**
  - [Diagrama de Sequência](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_02/#/Base/Relat%C3%B3rios/Subequipe03/ModelagemDinamica/DiagramaDeSequencia): Troca de mensagens síncronas/assíncronas e concorrência nos cenários de compra e autenticação.
  - [Diagrama de Estados](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_02/#/Base/Relat%C3%B3rios/Subequipe03/ModelagemDinamica/DiagramaDeEstados): Ciclo de vida transacional do pedido.
  - [Diagrama de Comunicação](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_02/#/Base/Relat%C3%B3rios/Subequipe03/ModelagemDinamica/DiagramaDeComunicacao): Colaboração e vínculos em tempo de execução.

---

### Seção 7: Visão de Implantação
- **Artefatos Fonte (Entrega 02):**
  - [Diagrama de Implantação](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_02/#/Base/Relat%C3%B3rios/Subequipe03/ModelagemEstatica/DiagramaDeImplantacao): Topologia física, nós de cliente (Web/Mobile), rede de trânsito (CDN/Cloudflare, API Gateway), nós de aplicação (Nuvem/Contêineres) e nós de banco de dados/cache.

---

### Seção 8: Visão de Implementação
- **Artefatos Fonte (Entrega 02 e 03):**
  - [Diagrama de Componentes](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_02/#/Base/Relat%C3%B3rios/Subequipe03/ModelagemEstatica/DiagramaDeComponentes): Componentes de software, interfaces providas/requeridas e fronteiras entre camadas.
  - [Interface Gráfica do Observer (E3 - Iniciativa Extra)](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_03/#/Base/Relat%C3%B3rios/IniciativasExtras/InterfaceGraficaObserver): Implementação e protótipo interativo demonstrando a modularidade dos componentes de interface e atualização reativa.

---

### Seção 9: Visão de Dados (Opcional)
- **Artefatos Fonte (Entrega 02):**
  - Diagrama de Classes e Entidades persistentes com chaves, multiplicidades e relacionamentos de integridade.

---

### Seção 10: Tamanho e Desempenho
- **Artefatos Fonte (Entrega 01):**
  - [NFR Framework - Softgoal Desempenho](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_01/#/Base/Relat%C3%B3rios/SubEquipe03/NFR): Metas de tempo de resposta, latência e dimensionamento de infraestrutura.

---

### Seção 11: Qualidade
- **Artefatos Fonte (Entrega 01):**
  - [NFR Framework](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_01/#/Base/Relat%C3%B3rios/SubEquipe03/NFR): Avaliação de segurança, portabilidade, extensibilidade e confiabilidade com os respectivos impactos (operationalizations).
