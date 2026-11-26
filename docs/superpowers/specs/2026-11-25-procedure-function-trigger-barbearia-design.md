# Procedures, Functions e Triggers na Barbearia do Braz — desenho

> Especificação aprovada por delegação: o usuário pediu "o que for melhor" e uma colinha para apresentação.

## Intenção

Adaptar o projeto Java/PostgreSQL existente para atender ao trabalho de BD2 de 25/11/2026: manter o CRUD já pronto e acrescentar Procedure com transação, Functions com retorno escalar e tabular, Triggers, e documentação que permita explicar tudo no seminário de 15 minutos.

## Fatos do projeto atual

- Java 21, console, JDBC + HikariCP 6.2.1, PostgreSQL, sem novas dependências.
- CRUD completo em `src/dao/*DAO.java` para Cliente, Barbeiro, Serviço, Agendamento, Pagamento e Avaliação; 5 relatórios em `RelatorioDAO`.
- `sql/barbearia.sql` hoje só cria tabelas, índices e dados iniciais. Não há PROCEDURE, FUNCTION ou TRIGGER.
- `executar.ps1` aponta para `Downloads\SQL-Barbearia-main`, caminho que não existe nesta pasta.
- `Main` obtém conexão do pool e nunca a devolve.
- Conflito de horário é verificado só em Java; duas sessões podem agendar o mesmo horário.
- `AgendamentoService.editar()` aceita qualquer status, deixando o `CHECK` do banco estourar como erro cru.
- DAOs engolem `SQLException`; o service pode imprimir sucesso após falha.

## Requisitos do PDF (verbatim do documento)

- Aplicação com conexão a PostgreSQL.
- No mínimo: operações CRUD, PostgreSQL Procedure, PostgreSQL Function, PostgreSQL Trigger.
- Procedure deve trabalhar com bloco de operações e gerenciar transação: modificações podem ser desfeitas (ROLLBACK) ou confirmadas (COMMIT) mediante condições específicas.
- Recomendado: Functions que retornem valores (escalar ou tabelas) e que auxiliem no processamento de Procedures ou Functions.
- Entregar aplicação + `script.sql` por repositório até 25/11/2026.
- Seminário avalia: domínio/clareza, introdução e contextualização, requisitos/casos de uso, diagrama DER ou relacional, execução real ou prints, e apresentação dos comandos SQL no código/script.

## Metas

1. `finalizar_atendimento`: bloco transacional que altera Agendamento e cria Pagamento, com `COMMIT` no caminho válido e `ROLLBACK` no inválido.
2. `fn_preco_servico` (escalar) usada pela procedure para calcular o valor.
3. `fn_faturamento_por_barbeiro` (tabela) consumida pela aplicação.
4. Trigger de integridade de agenda e trigger de auditoria de pagamento.
5. Correções dos bugs listados acima, sem reescrever a arquitetura.
6. `COLINHA.md` com roteiro de fala e respostas prováveis; diagramas UML/DER em `docs/DIAGRAMAS.md`.

## Não metas

- Não criar GUI, autenticação, nova biblioteca ou domínio novo.
- Não remov CRUD, relatórios ou padrão model/dao/service/view existente.
- Não inventar métricas, clientes ou requisitos ausentes do PDF.

## Restrições

- Java 21, PostgreSQL 16, HikariCP, JDBC, sem dependências novas.
- Saída UTF-8 no console; nomes de objetos em português, minúsculas com underscore, como no script atual.
- Script executável com `psql -U postgres -f sql/barbearia.sql`.
- Apresentação: até 15 minutos; material deve permitir explicar código e SQL.

## Decisões (escolhas do implementador, não do usuário)

### Procedure `finalizar_atendimento`

Assinatura:

```sql
CREATE OR REPLACE PROCEDURE finalizar_atendimento(
    p_id_agendamento INTEGER,
    p_forma_pagamento VARCHAR,
    OUT p_resultado VARCHAR,
    OUT p_mensagem VARCHAR)
LANGUAGE plpgsql
```

Sequência:

1. `SELECT ... FOR UPDATE` no Agendamento (trava concorrência).
2. Valida existência e status em `AGENDADO`/`CONFIRMADO`.
3. Calcula valor com `fn_preco_servico(id_servico)`.
4. Valida forma de pagamento contra o `CHECK` da tabela.
5. `UPDATE Agendamento SET status = 'REALIZADO'`.
6. `INSERT INTO Pagamento (..., status_pagamento) VALUES (..., 'PAGO')`.
7. Trigger de auditoria registra a mudança.
8. `COMMIT` com `p_resultado = 'OK'`; qualquer exceção cai no handler, executa `ROLLBACK` e devolve `p_resultado = 'ERRO'` com `SQLERRM`.

Chamada via JDBC exige `Connection.setAutoCommit(true)`, porque procedimentos com controle de transação não podem ser chamados dentro de transação explícita do cliente.

### Functions

- `fn_preco_servico(p_id_servico INTEGER) RETURNS NUMERIC(10,2)`: retorna `preco` ou `NULL`; marcada `STABLE`. Usada pela procedure e por consulta na aplicação.
- `fn_faturamento_por_barbeiro(p_id_barbeiro INTEGER DEFAULT NULL) RETURNS TABLE (id_barbeiro INTEGER, nome VARCHAR(100), atendimentos BIGINT, faturamento NUMERIC)`: soma pagamentos `PAGO` de agendamentos `REALIZADO`; `NULL` no parâmetro retorna todos.

### Triggers

- `trg_validar_agendamento`: `BEFORE INSERT OR UPDATE OF id_barbeiro, id_servico, data_hora, status ON Agendamento`. Barra domingo, horário fora de 08:00–18:00, término após 18:00 e sobreposição com outro atendimento ativo do mesmo barbeiro, usando `duracao_minutos` de Serviço. `CANCELADO` fica fora da regra de sobreposição.
- `trg_auditoria_pagamento`: `AFTER INSERT OR UPDATE OF status_pagamento ON Pagamento`. Grava em `AuditoriaPagamento (id_auditoria, id_pagamento, status_anterior, status_novo, registrado_em)`.

### Integração na aplicação

- Novo `model/ResultadoOperacao` (record com `sucesso` e `mensagem`).
- Novo `dao/OperacaoBancoDAO`: `finalizarAtendimento(int, String)`, `precoServico(int)`, `faturamentoPorBarbeiro(Integer)`, `listarAuditoriaPagamentos()`.
- Novo `service/OperacaoBancoService`: menu com finalizar atendimento, faturamento, preço e auditoria; imprime o SQL chamado para fins didáticos.
- `MenuView`: nova opção 9, "Procedure / Functions / Triggers".
- Correções: fechar conexão em `Main`; caminho do `executar.ps1` relativo ao script; validação de status no editar de agendamento; erro de banco propagado em `AgendamentoDAO.inserir`.

### Documentação

- `COLINHA.md` na raiz do projeto: pitch de 60 segundos, mapa de arquivos, passo a passo da procedure, funções, triggers, roteiro de demonstração no menu e perguntas prováveis com respostas curtas.
- `docs/DIAGRAMAS.md`: diagrama de casos de uso e DER em Mermaid, mais dicas de explicação.

## Como verificar

- `sql/testes.sql` com blocos `DO` que lançam exceção se algum objeto ou comportamento estiver errado.
- `javac` com o classpath de `lib/*` para compilar tudo.
- Execução interativa: finalizar um agendamento válido (OK), tentar novamente (ERRO), inserir agendamento conflitante (ERRO), consultar faturamento e auditoria.

## Riscos conhecidos

- `CREATE DATABASE` + `\c` exigem `psql`; o script não é idempotente se houver conexões abertas. O comando de reset deve encerrar a aplicação antes de rodar.
- Se o procedimento for chamado com `autoCommit=false`, PostgreSQL recusa `COMMIT`/`ROLLBACK` internos. O DAO controla isso explicitamente.
