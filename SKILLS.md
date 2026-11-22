# Guia de Skills: Desenvolvimento do Documento de Arquitetura de Software (DAS)

Este arquivo documenta a **Skill de Desenvolvimento do Documento de Arquitetura de Software (DAS)** para o projeto **G1_ProjetoComercioEletronico** (Subequipe 03), configurada no padrão de Customizações do Antigravity.

A Skill completa e executável está instalada no diretório:
👉 [`.agents/skills/desenvolvimento-das/SKILL.md`](file://.agents/skills/desenvolvimento-das/SKILL.md)

---

## 📁 Estrutura de Arquivos da Skill

| Arquivo | Descrição |
| :--- | :--- |
| [`.agents/skills/desenvolvimento-das/SKILL.md`](file://.agents/skills/desenvolvimento-das/SKILL.md) | **Instrução mestre da Skill**: Procedimento detalhado passo a passo para elaboração e refinamento do DAS. |
| [`.agents/skills/desenvolvimento-das/resources/template_das.md`](file://.agents/skills/desenvolvimento-das/resources/template_das.md) | **Template RUP 4+1**: Estrutura completa das 11 seções em Markdown limpo, formatado para Docsify. |
| [`.agents/skills/desenvolvimento-das/references/mapeamento_entregas.md`](file://.agents/skills/desenvolvimento-das/references/mapeamento_entregas.md) | **Matriz de Rastreabilidade**: Mapeamento direto de cada seção do DAS com os artefatos das Entregas 01, 02 e 03. |
| [`.agents/skills/desenvolvimento-das/references/checklist_avaliacao.md`](file://.agents/skills/desenvolvimento-das/references/checklist_avaliacao.md) | **Checklist de Conformidade**: Critérios de avaliação da Entrega 04 (Foco 01, Foco 02, Docsify, software rodando). |

---

## 🎯 Visão Rápida do Template RUP 4+1

O documento de arquitetura segue o modelo 4+1 de Philippe Kruchten e o template padrão da disciplina:

```
<Nome do Projeto> - Documento de Arquitetura de Software (DAS)
│
├── 1. Introdução
│   ├── 1.1 Finalidade
│   ├── 1.2 Escopo
│   ├── 1.3 Definições, Acrônimos e Abreviações
│   ├── 1.4 Referências
│   └── 1.5 Visão Geral
├── 2. Representação Arquitetural (Modelo 4+1 Visões)
├── 3. Metas e Restrições da Arquitetura (NFR Framework / Requisitos)
├── 4. Visão de Casos de Uso
│   └── 4.1 Realizações de Casos de Uso (Diagramas de Sequência e Atividades)
├── 5. Visão Lógica (OBRIGATÓRIA)
│   ├── 5.1 Visão Geral (Diagrama de Pacotes em Camadas)
│   └── 5.2 Pacotes Significativos (Diagrama de Classes + Padrões GoF/GRASP da E3)
├── 6. Visão de Processos (Concorrência, Síncrono/Assíncrono, Estados)
├── 7. Visão de Implantação (Topologia de Hardware/Nuvem, CDN, Gateway, BDs)
├── 8. Visão da Implementação
│   ├── 8.1 Visão Geral (Diagrama de Componentes)
│   └── 8.2 Camadas
├── 9. Visão de Dados (Opcional - Esquema de Persistência)
├── 10. Tamanho e Desempenho (Métricas e Volume de Requisições)
├── 11. Qualidade (Avaliação NFR: Confiabilidade, Segurança, Manutenibilidade)
│
├── FOCO_01: DAS & Demonstração (Participantes, Metodologia e Vídeo da Feature Rodando)
├── FOCO_02: IA Generativa & Lições Aprendidas (Todos os Membros com Senso Crítico)
└── Versionamentos (Histórico de Revisões e Commits)
```

---

## 🔗 Links e Rastreabilidade com Entregas Anteriores

1. **Entrega 01 (Base):**
   - [Relatório da Subequipe 03](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_01/#/Base/Relat%C3%B3rios/1.1.3.SubEquipe_03)
   - Artefatos: [Design Sprint](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_01/#/Base/Relat%C3%B3rios/SubEquipe03/DesignSprint), [5W2H](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_01/#/Base/Relat%C3%B3rios/SubEquipe03/5W2H), [Rich Picture](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_01/#/Base/Relat%C3%B3rios/SubEquipe03/ArtefatoGeneralista), [Léxico](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_01/#/Base/Relat%C3%B3rios/SubEquipe03/Lexico), [NFR Framework](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_01/#/Base/Relat%C3%B3rios/SubEquipe03/NFR), [BPMN](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_01/#/Base/Relat%C3%B3rios/SubEquipe03/BPMN).

2. **Entrega 02 (Modelagem UML):**
   - [Relatório da Subequipe 03](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_02/#/Base/Relat%C3%B3rios/Subequipe03/Metodologia)
   - Artefatos Estáticos: [Classes](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_02/#/Base/Relat%C3%B3rios/Subequipe03/ModelagemEstatica/DiagramaDeClasses), [Pacotes](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_02/#/Base/Relat%C3%B3rios/Subequipe03/ModelagemEstatica/DiagramaDePacotes), [Casos de Uso](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_02/#/Base/Relat%C3%B3rios/Subequipe03/ModelagemEstatica/DiagramaDeCasosDeUso), [Componentes](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_02/#/Base/Relat%C3%B3rios/Subequipe03/ModelagemEstatica/DiagramaDeComponentes), [Implantação](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_02/#/Base/Relat%C3%B3rios/Subequipe03/ModelagemEstatica/DiagramaDeImplantacao).
   - Artefatos Dinâmicos: [Sequência](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_02/#/Base/Relat%C3%B3rios/Subequipe03/ModelagemDinamica/DiagramaDeSequencia), [Atividades](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_02/#/Base/Relat%C3%B3rios/Subequipe03/ModelagemDinamica/DiagramaDeAtividades), [Estados](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_02/#/Base/Relat%C3%B3rios/Subequipe03/ModelagemDinamica/DiagramaDeEstados), [Comunicação](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_02/#/Base/Relat%C3%B3rios/Subequipe03/ModelagemDinamica/DiagramaDeComunicacao).

3. **Entrega 03 (Padrões de Projeto):**
   - [Relatório da Subequipe 03](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_03/#/)
   - Padrões GoF: [State (Fluxo de Compra)](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_03/#/Base/Relat%C3%B3rios/Subequipe03/GofsComportamentais/FluxoDeCompra(State)), [Observer/Strategy (Fluxo de Pagamento)](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_03/#/Base/Relat%C3%B3rios/Subequipe03/GofsComportamentais/FluxoDePagamento(StrategyObserver)), [Chain of Responsibility (Fluxo de Login)](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_03/#/Base/Relat%C3%B3rios/Subequipe03/GofsComportamentais/FluxoDeLogin(ChainOfResponsibility)), [Builder (Sessão)](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_03/#/Base/Relat%C3%B3rios/Subequipe03/GofsCriacionais/FluxoLogin(Builder)), [Facade (Emissão de Sessão)](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_03/#/Base/Relat%C3%B3rios/Subequipe03/GofsEstruturais/EmissaoDeSessao(Facade)).
   - Padrões GRASP: [Controller](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_03/#/Base/Relat%C3%B3rios/Subequipe03/GRASP/Controller).
   - Iniciativa Extra: [Interface Gráfica do Observer](https://unbarqdsw2026-2-turma01.github.io/2026.2-T01-_G1_ProjetoComercioEletronico_Entrega_03/#/Base/Relat%C3%B3rios/IniciativasExtras/InterfaceGraficaObserver).

---

## 🚀 Como Executar

Para acionar a Skill diretamente no Antigravity, utilize o comando:
```bash
# O Antigravity carrega a skill sob demanda a partir da descrição ou você pode solicitar:
"Elabore o Documento de Arquitetura de Software utilizando a skill desenvolvimento-das"
```
O agente seguirá automaticamente o roteiro de 7 passos definido em [`.agents/skills/desenvolvimento-das/SKILL.md`](file://.agents/skills/desenvolvimento-das/SKILL.md).
