# Procedure, Functions e Triggers na Barbearia — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Acrescentar ao projeto Java/PostgreSQL existente uma Procedure transacional, Functions escalar e tabular, Triggers de integridade e auditoria, corrigir os bugs bloqueantes e entregar uma colinha para o seminário.

**Architecture:** O banco passa a ser a autoridade das regras de negócio (transação na procedure, validação e auditoria em triggers); o Java mantém model/dao/service/view e ganha um DAO+service específicos para chamar os objetos via JDBC e exibi-los no menu.

**Tech Stack:** Java 21, PostgreSQL 16, JDBC, HikariCP 6.2.1, psql, PowerShell.

**Spec:** `SQL-Barbearia-main/docs/superpowers/specs/2026-11-25-procedure-function-trigger-barbearia-design.md`

**Execução escolhida:** nativa, nesta sessão, porque o usuário delegou as decisões ("o que for melhor"). Não há repositório Git, então não há passos de commit.

## Global Constraints

- Não adicionar dependências; usar `lib/*.jar` existentes.
- Todo objeto novo em português, `snake_case`; nomes exatos desta spec.
- `finalizar_atendimento` deve conter `COMMIT` e `ROLLBACK` explícitos e retornar `p_resultado`/`p_mensagem`.
- `fn_preco_servico` retorna escalar; `fn_faturamento_por_barbeiro` retorna tabela.
- Pelo menos um `TRIGGER` de integridade e um de auditoria.
- Preservar CRUD, relatórios e a estrutura `model`/`dao`/`service`/`view`.
- Script do banco executável via `psql -U postgres -f sql/barbearia.sql`.
- Console em UTF-8; sem lorem ipsum; colinha em português.

## Review Focus

1. `CALL` com `autoCommit=false` → PostgreSQL recusa transação interna; teste no harness Java deve chamar com autoCommit `true`.
2. Horário conflitante inserido direto no psql, burlando o Java → trigger deve rejeitar; teste SQL deve capturar `raise_exception`.
3. Procedure chamada duas vezes para o mesmo agendamento → segunda deve devolver `ERRO` sem criar segundo pagamento; teste SQL verifica contagem.
4. Parâmetro inválido (forma fora de `DINHEIRO/CARTAO_CREDITO/CARTAO_DEBITO/PIX`) → `ROLLBACK` e mensagem, sem alterar status; teste SQL verifica status continua `AGENDADO`.
5. Conexão do pool vazando em `Main` e caminho fixo do `executar.ps1` → teste de smoke executa o app a partir da pasta real do projeto.

---

### Task 1: Objetos SQL e testes de banco

**Files:**
- Modify: `sql/barbearia.sql`
- Create: `sql/testes.sql`

**Interfaces:**
- Produces: `finalizar_atendimento(INTEGER, VARCHAR, OUT VARCHAR, OUT VARCHAR)`, `fn_preco_servico(INTEGER)`, `fn_faturamento_por_barbeiro(INTEGER)`, tabela `AuditoriaPagamento`, gatilhos `trg_validar_agendamento` e `trg_auditoria_pagamento`.

- [ ] **Step 1: Escrever `sql/testes.sql` com os blocos `DO` que hoje falham**

Use `\set ON_ERROR_STOP on`, `\c barbearia`, `TRUNCATE Cliente, Barbeiro, Servico, Agendamento, Pagamento, Avaliacao, AuditoriaPagamento RESTART IDENTITY CASCADE`, reinsira 1 barbeiro, 1 serviço (`Corte Simples`, 30.00, 30 min), 1 cliente e 2 agendamentos (id 1 e 2) e afirme:
  - `fn_preco_servico(1) = 30.00`;
  - `fn_faturamento_por_barbeiro(NULL)` e `fn_faturamento_por_barbeiro(1)` retornam linhas;
  - `CALL finalizar_atendimento(1, 'PIX', v_res, v_msg)` devolve `OK`, status do agendamento 1 vira `REALIZADO` e existe um `Pagamento` `PAGO` com valor 30.00;
  - existe linha em `AuditoriaPagamento` para o pagamento do agendamento 1;
  - segunda chamada para o agendamento 1 devolve `ERRO` e não cria outro pagamento;
  - chamada com id 999999 devolve `ERRO` e não altera nada;
  - `INSERT` de agendamento conflitante para o mesmo barbeiro às 08:30 levanta exceção.

- [ ] **Step 2: Rodar os testes e confirmar a falha por objeto inexistente**

Run: `& "$PG\bin\psql.exe" -U postgres -f sql\testes.sql`
Expected: FAIL com `function fn_preco_servico(integer) does not exist`.

- [ ] **Step 3: Implementar os objetos em `sql/barbearia.sql`**

Acrescentar `DROP`/`CREATE OR REPLACE` reaproveitando o estilo do arquivo; criar `AuditoriaPagamento`; criar as duas functions, a procedure e os dois triggers exatamente com os nomes da spec. Na procedure, usar `FOR UPDATE`, cálculo via `fn_preco_servico`, `UPDATE`, `INSERT`, `COMMIT` no sucesso e `EXCEPTION WHEN OTHERS THEN ROLLBACK` devolvendo `SQLERRM`. No trigger de agenda, usar `duracao_minutos` do serviço e intervalo 08:00–18:00, bloquear domingo e ignorar `CANCELADO`.

- [ ] **Step 4: Recriar o banco e rodar os testes**

Run (com a aplicação fechada):
```powershell
& "$PG\bin\psql.exe" -U postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'barbearia' AND pid <> pg_backend_pid();"
& "$PG\bin\psql.exe" -U postgres -c "DROP DATABASE IF EXISTS barbearia;"
& "$PG\bin\psql.exe" -U postgres -f sql\barbearia.sql
& "$PG\bin\psql.exe" -U postgres -f sql\testes.sql
```
Expected: `testes.sql` termina sem `ERRO:` e imprime `OK: testes SQL concluidos`.

---

### Task 2: Acesso JDBC aos objetos do banco

**Files:**
- Create: `src/model/ResultadoOperacao.java`
- Create: `src/dao/OperacaoBancoDAO.java`
- Create: `tests/TesteOperacaoBanco.java`

**Interfaces:**
- Consumes: `DatabaseConnection.getConnection()`, funções e procedure da Task 1.
- Produces: `record ResultadoOperacao(boolean sucesso, String mensagem)`; `OperacaoBancoDAO.finalizarAtendimento(int, String) -> ResultadoOperacao`, `precoServico(int) -> BigDecimal`, `faturamentoPorBarbeiro(Integer) -> List<String[]>`, `listarAuditoriaPagamentos() -> List<String[]>`.

- [ ] **Step 1: Escrever o teste Java que ainda não compila**

`tests/TesteOperacaoBanco.java`: `main` com `check(boolean, String)` que acumula falhas e termina com `System.exit(1)`. Afirme: `precoServico(1).compareTo(new BigDecimal("30.00")) == 0`; `faturamentoPorBarbeiro(null)` não vazio; `finalizarAtendimento(999999, "PIX").sucesso() == false`; `listarAuditoriaPagamentos()` não vazio.

- [ ] **Step 2: Compilar e verificar a falha**

Run: `& "$JDK\bin\javac.exe" -encoding UTF-8 -cp "build;lib\*" -d build tests\TesteOperacaoBanco.java`
Expected: FAIL com `cannot find symbol` para `ResultadoOperacao`/`OperacaoBancoDAO`.

- [ ] **Step 3: Implementar model e DAO**

`ResultadoOperacao`: record imutável com os dois campos. `OperacaoBancoDAO`: `finalizarAtendimento` usa `CallableStatement` com `CALL finalizar_atendimento(?, ?, ?, ?)`, registra os dois OUT como `Types.VARCHAR`, garante `setAutoCommit(true)` antes do `CALL` e monta o record; `precoServico` usa `SELECT fn_preco_servico(?)`; `faturamentoPorBarbeiro` usa `SELECT * FROM fn_faturamento_por_barbeiro(?)`, com `setNull` quando o parâmetro é `null`; `listarAuditoriaPagamentos` consulta a tabela de auditoria. Erros viram `ResultadoOperacao(false, mensagem)` ou lista vazia, com `System.err.println`.

- [ ] **Step 4: Compilar e rodar o teste**

Run:
```powershell
& "$JDK\bin\javac.exe" -encoding UTF-8 -cp "build;lib\*" -d build src\model\ResultadoOperacao.java src\dao\OperacaoBancoDAO.java tests\TesteOperacaoBanco.java
& "$JDK\bin\java.exe" -Dfile.encoding=UTF-8 -cp "build;lib\*" TesteOperacaoBanco
```
Expected: `PASS: 4 verificacoes de banco`.

---

### Task 3: Integração no menu e correções do projeto antigo

**Files:**
- Create: `src/service/OperacaoBancoService.java`
- Modify: `src/view/MenuView.java`
- Modify: `src/service/AgendamentoService.java`
- Modify: `src/dao/AgendamentoDAO.java`
- Modify: `src/Main.java`
- Modify: `executar.ps1`
- Create: `scripts/smoke.ps1`

**Interfaces:**
- Consumes: `OperacaoBancoDAO` da Task 2, DAOs existentes.
- Produces: opção `9` do menu principal com submenu 1–4; `scripts/smoke.ps1` executável sem digitação manual.

- [ ] **Step 1: Escrever o smoke test antes do service**

`scripts/smoke.ps1`: com `$JDK`, `$PG`, `$project` derivados da própria pasta do script, envia a sequência `9,1,2,PIX,0,9,2,1,0,9,3,1,0,9,4,0,0` para `java Main` e falha se a saída não contiver `ATENDIMENTO FINALIZADO`, `FATURAMENTO POR BARBEIRO`, `PRECO DO SERVICO` e `AUDITORIA DE PAGAMENTOS`.

- [ ] **Step 2: Rodar o smoke e verificar a falha**

Run: `powershell -File scripts\smoke.ps1`
Expected: FAIL porque a opção 9 não existe.

- [ ] **Step 3: Implementar service, menu e correções**

`OperacaoBancoService`: quatro métodos (`finalizar`, `faturamento`, `preco`, `auditoria`) que imprimem o comando SQL chamado antes de executar. `MenuView`: acrescentar opção 9 e submenu 1–4. `AgendamentoService.editar`: validar status contra os quatro valores do `CHECK` antes de `atualizar`. `AgendamentoDAO.inserir`: relançar `SQLException` como `IllegalStateException` com mensagem legível (trigger de conflito passa a aparecer ao usuário). `Main`: obter conexão, fechar com `close()` em `try/finally` antes de encerrar o pool. `executar.ps1`: usar `Split-Path -Parent $MyInvocation.MyCommand.Path` como raiz e procurar JDK/PostgreSQL em variáveis de ambiente ou em `AppData\Local\Temp\opencode\tools`.

- [ ] **Step 4: Compilar tudo e rodar o smoke**

Run:
```powershell
& "$JDK\bin\javac.exe" -encoding UTF-8 -cp "lib\*" -d build (Get-ChildItem src -Recurse -Filter *.java).FullName
powershell -File scripts\smoke.ps1
```
Expected: compilação sem erros e smoke imprime `PASS: menu e objetos SQL funcionando`.

---

### Task 4: Colinha e diagramas para o seminário

**Files:**
- Create: `COLINHA.md`
- Create: `docs/DIAGRAMAS.md`
- Modify: `README.md` (criar se não existir)

**Interfaces:**
- Consumes: nomes e comportamentos das Tasks 1–3.
- Produces: material de 15 minutos com roteiro, comandos e respostas para perguntas do professor.

- [ ] **Step 1: Escrever os documentos**

`COLINHA.md`: pitch de 60 s, mapa de arquivos, explicação linha a linha da procedure (trava, validação, function, update, insert, commit/rollback), functions, triggers, roteiro de demonstração com as opções do menu, e 8 perguntas prováveis com respostas curtas. `docs/DIAGRAMAS.md`: casos de uso e DER em Mermaid com as 7 tabelas e os relacionamentos do script. `README.md`: como recriar o banco, compilar, executar, rodar testes e onde está a colinha.

- [ ] **Step 2: Verificar o material**

Run: `Get-Content COLINHA.md, docs\DIAGRAMAS.md, README.md | Select-String -Pattern "finalizar_atendimento","fn_preco_servico","fn_faturamento_por_barbeiro","trg_validar_agendamento","trg_auditoria_pagamento"`
Expected: todos os cinco nomes aparecem.

- [ ] **Step 3: Revisão final de requisitos**

Comparar o PDF com a entrega: CRUD (já existente, preservado), Procedure com transação, Function escalar, Function tabela, Trigger, `sql/barbearia.sql`, colinha, diagramas, comandos de execução. Se algum item faltar, corrigir antes de encerrar.
