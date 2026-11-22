# G1_ProjetoComercioEletronico - aplicacao da Entrega 4
#
# Requisito unico: JDK 11 ou superior no PATH. Nenhuma biblioteca externa.
#
#   make            compila
#   make test       compila e executa todos os testes
#   make arquitetura  executa so a verificacao das regras de dependencia
#   make verificar  o que a integracao continua executa (compila + testes)
#   make run        sobe a loja em http://localhost:8080  (PORTA=9090 para trocar)
#   make demo       roteiro no terminal
#   make clean      apaga a saida

JAVAC  ?= javac
JAVA   ?= java
FLAGS  := -encoding UTF-8 -Xlint:unchecked -Xlint:deprecation
SAIDA  := out
PORTA  ?= 8080

FONTES := $(shell find src -name '*.java')
TESTES := $(shell find teste -name '*.java')
CP     := $(SAIDA)/principal
CPT    := $(SAIDA)/principal:$(SAIDA)/teste

.PHONY: all test arquitetura verificar run demo clean

all: $(SAIDA)/.principal

$(SAIDA)/.principal: $(FONTES)
	@mkdir -p $(SAIDA)/principal
	@echo "compilando $(words $(FONTES)) arquivos de src/"
	@$(JAVAC) $(FLAGS) -d $(SAIDA)/principal $(FONTES)
	@touch $@

$(SAIDA)/.teste: $(SAIDA)/.principal $(TESTES)
	@mkdir -p $(SAIDA)/teste
	@echo "compilando $(words $(TESTES)) arquivos de teste/"
	@$(JAVAC) $(FLAGS) -cp $(CP) -d $(SAIDA)/teste $(TESTES)
	@touch $@

test: $(SAIDA)/.teste
	@$(JAVA) -cp $(CPT) -Dcomercio.src=src -Dcomercio.web=web comercio.teste.Executor $(SAIDA)/teste

arquitetura: $(SAIDA)/.teste
	@$(JAVA) -cp $(CPT) -Dcomercio.src=src comercio.teste.Executor $(SAIDA)/teste Arquitetura

verificar: test

run: all
	$(JAVA) -cp $(CP) -Dcomercio.porta=$(PORTA) -Dcomercio.web=web comercio.Aplicacao

demo: all
	$(JAVA) -cp $(CP) comercio.demonstracao.Roteiro

clean:
	rm -rf $(SAIDA)
