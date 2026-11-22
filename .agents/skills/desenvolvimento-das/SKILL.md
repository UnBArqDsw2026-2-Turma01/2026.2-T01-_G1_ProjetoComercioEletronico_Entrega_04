---
name: desenvolvimento-das
description: >-
  Guia procedural e automação para elaboração do Documento de Arquitetura de Software (DAS) baseado no modelo 4+1 visões do RUP, integrando rastreabilidade completa com os artefatos das Entregas 01, 02 e 03 de Arquitetura e Desenho de Software.
---

# Skill: Desenvolvimento do Documento de Arquitetura de Software (DAS)

Esta Skill orienta e executa o processo completo de elaboração, estruturação e revisão técnica do **Documento de Arquitetura de Software (DAS)** segundo o modelo **4+1 Visões do Rational Unified Process (RUP)** de Philippe Kruchten.

A Skill foi desenhada especificamente para os requisitos acadêmicos da disciplina de **Arquitetura e Desenho de Software (UnB FGA)**, integrando e rastreando organicamente os modelos e decisões construídos pela **Subequipe 03** ao longo das entregas anteriores:
- **Entrega 01 (Base):** 5W2H, Rich Picture, Léxico, NFR Framework, BPMN e Design Sprint.
- **Entrega 02 (Modelagem UML):** Diagramas de Casos de Uso, Classes, Pacotes, Componentes, Implantação, Sequência, Atividades, Estados e Comunicação.
- **Entrega 03 (Padrões de Projeto):** GoF Criacionais (Builder), Estruturais (Facade), Comportamentais (State, Strategy/Observer, Chain of Responsibility), GRASP (Controller) e Interface Gráfica Interativa.
- **Entrega 04 (DAS & Reutilização):** Foco 01 (DAS com Visão Lógica + Visão adicional + Demonstração em vídeo) e Foco 02 (Uso de IA Generativa com senso crítico por membro).

---

## Estrutura de Arquivos da Skill

- [Template RUP do DAS](./resources/template_das.md): Estrutura mestre das 11 seções em Markdown compatível com Docsify.
- [Matriz de Rastreabilidade](./references/mapeamento_entregas.md): Tabela de correlação direta entre artefatos prévios e seções do DAS.
- [Checklist de Avaliação](./references/checklist_avaliacao.md): Critérios da Entrega 04 para obtenção de nota máxima.

---

## Procedimento Passo a Passo para Gerar/Atualizar o DAS

### Passo 1: Leitura de Contexto e Artefatos Prévios
Antes de redigir qualquer seção, o agente ou autor deve consultar a [Matriz de Rastreabilidade](./references/mapeamento_entregas.md) para resgatar os insumos consolidados:
1. **Entrega 01:**
   - [Rich Picture & Artefato Generalista](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_01/#/Base/Relat%C3%B3rios/SubEquipe03/ArtefatoGeneralista)
   - [NFR Framework](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_01/#/Base/Relat%C3%B3rios/SubEquipe03/NFR)
   - [5W2H](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_01/#/Base/Relat%C3%B3rios/SubEquipe03/5W2H)
   - [Léxico](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_01/#/Base/Relat%C3%B3rios/SubEquipe03/Lexico)
2. **Entrega 02:**
   - [Diagrama de Pacotes](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_02/#/Base/Relat%C3%B3rios/Subequipe03/ModelagemEstatica/DiagramaDePacotes)
   - [Diagrama de Classes](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_02/#/Base/Relat%C3%B3rios/Subequipe03/ModelagemEstatica/DiagramaDeClasses)
   - [Diagrama de Casos de Uso](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_02/#/Base/Relat%C3%B3rios/Subequipe03/ModelagemEstatica/DiagramaDeCasosDeUso)
   - [Diagrama de Componentes](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_02/#/Base/Relat%C3%B3rios/Subequipe03/ModelagemEstatica/DiagramaDeComponentes)
   - [Diagrama de Implantação](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_02/#/Base/Relat%C3%B3rios/Subequipe03/ModelagemEstatica/DiagramaDeImplantacao)
   - Diagramas Dinâmicos: Sequência, Atividades, Estados e Comunicação.
3. **Entrega 03:**
   - [Padrões GoF e GRASP](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_03/#/) (State, Observer/Strategy, Chain of Responsibility, Builder, Facade, GRASP Controller).

---

### Passo 2: Inicializar o Documento Base
Utilizar como esqueleto o arquivo [template_das.md](./resources/template_das.md), salvando o conteúdo no destino apropriado do repositório (`docs/Base/Relatórios/1.1.1.DAS.md`).
Preencher os metadados iniciais:
- **Título:** `G1_ProjetoComercioEletronico — Documento de Arquitetura de Software (DAS)`.
- **Versão:** `1.0`.
- **Histórico de Revisão:** Tabela com data, versão, escopo da alteração e membros responsáveis.

---

### Passo 3: Redigir as Seções Conceituais (1 a 3)

1. **Seção 1 (Introdução):**
   - *1.1 Finalidade:* Explicar a comunicação técnica das decisões arquiteturais para partes interessadas e equipe de desenvolvimento.
   - *1.2 Escopo:* Definir o domínio de e-commerce inspirado em marketplace de grande porte, compreendendo catálogo, carrinho, checkout, pagamentos e rastreamento.
   - *1.3 Definições & Acrônimos:* Consolidar termos arquiteturais e referenciar o Léxico da E1.
   - *1.4 Referências:* Citar Kruchten (1995), GoF (Gamma et al., 1994), Larman (2007) e os links das Entregas 01, 02 e 03.
   - *1.5 Visão Geral:* Resumo sucinto do conteúdo das seções subsequentes.
2. **Seção 2 (Representação Arquitetural):**
   - Apresentar o diagrama conceitual do Modelo 4+1 Visões.
   - Justificar a importância de cada visão para isolar diferentes preocupações (*separation of concerns*).
3. **Seção 3 (Metas e Restrições da Arquitetura):**
   - Transpor os softgoals do NFR Framework (E1): Desempenho, Segurança, Integridade Transacional (ACID) e Escalabilidade.
   - Listar restrições tecnológicas (arquitetura em camadas, comunicação REST, microsserviços desacoplados e padrões GoF/GRASP).

---

### Passo 4: Redigir as Visões Arquiteturais (4 a 8)

1. **Seção 4 (Visão de Casos de Uso):**
   - Selecionar de 3 a 5 casos de uso arquiteturalmente críticos (ex.: Autenticação em Cadeia, Checkout Transacional, Processamento Reativo de Pagamento).
   - Inserir a subseção 4.1 (Realizações de Casos de Uso) integrando diagramas de sequência e atividades da E2.
2. **Seção 5 (Visão Lógica — OBRIGATÓRIA):**
   - *5.1 Visão Geral:* Decomposição em camadas: Apresentação (View), Controle (Controller), Aplicação (Service), Domínio (Model) e Persistência (Repository).
   - *5.2 Pacotes Significativos:* Detalhar os subsistemas de Domínio (Autenticação/Sessão, Catálogo/Busca, Pedidos/Checkout, Pagamentos).
   - **Vincular explicitamente os Padrões da Entrega 03:**
     - `Chain of Responsibility` e `Builder` no pacote de Sessão e Login.
     - `Facade` na interface unificada de emissão de token/sessão.
     - `State` na máquina de estados de transição do Pedido (`Order`).
     - `Observer` e `Strategy` na liquidação e notificação de pagamentos.
     - `Controller` do GRASP na recepção desacoplada de comandos da UI.
3. **Seção 6 (Visão de Processos):**
   - Mapear processos leves (threads de requisições HTTP, jobs de notificação) e processos pesados (instâncias de serviços, workers de fila).
   - Explicar modelo síncrono (REST) e assíncrono (mensageria/eventos).
4. **Seção 7 (Visão de Implantação):**
   - Apresentar o Diagrama de Implantação da E2.
   - Descrever os nós físicos/lógicos: Dispositivos de Cliente (Browser/Mobile), Camada de Borda/CDN (Cloudflare), API Gateway, Clusters de Execução (Kubernetes/Cloud Containers) e Nós de Banco de Dados/Cache (PostgreSQL, Redis).
5. **Seção 8 (Visão da Implementação):**
   - Descrever o Diagrama de Componentes da E2.
   - Definir as fronteiras entre módulos, interfaces providas/requeridas e estratégias de empacotamento.

---

### Passo 5: Redigir Seções de Suporte (9 a 11)

1. **Seção 9 (Visão de Dados):**
   - Descrever o modelo relacional de persistência das entidades centrais (`Usuario`, `Produto`, `Pedido`, `ItemPedido`, `Pagamento`).
2. **Seção 10 (Tamanho e Desempenho):**
   - Dimensionamento de carga concorrente, volumetria de banco e políticas de cache em memória.
3. **Seção 11 (Qualidade):**
   - Cruzar os atributos de qualidade da arquitetura com as operationalizations definidas no NFR Framework (extensibilidade via padrões de projeto, segurança via cadeia de autenticação, confiabilidade via transações atômicas).

---

### Passo 6: Preencher Focos de Avaliação da Disciplina (Foco 01 e Foco 02)

1. **FOCO_01 (DAS & Demonstração Prática):**
   - Tabela com os participantes do Foco 01 e suas atribuições técnicas.
   - Metodologia de trabalho adotada pela equipe (Scrum adaptado, divisão em sprints, reuniões síncronas no Discord/Teams).
   - Inserir link para o **Vídeo de Demonstração** de features rodando em ambiente prático (ex.: fluxo de pagamento reativo com interface gráfica desenvolvida na E3).
2. **FOCO_02 (IA Generativa & Lições Aprendidas):**
   - **Obrigatório para TODOS os membros da Subequipe 03.**
   - Coluna 1: Nome do membro.
   - Coluna 2: Lições aprendidas sobre engenharia de software e arquitetura.
   - Coluna 3: Senso crítico sobre a IA Generativa (onde a IA gerou alucinações, correções exigidas na sintaxe UML, refinamentos manuais necessários).
3. **Versionamentos:**
   - Tabela cronológica de commits e contribuições de cada membro.

---

### Passo 7: Validação e Checklist
Antes da entrega final, submeter o documento gerado à verificação do [Checklist de Avaliação](./references/checklist_avaliacao.md) e confirmar a correta renderização na sidebar do Docsify (`docs/_sidebar.md`).
