# Barbearia do Braz — BD2 2026.2

Aplicação de console em Java 21 conectada ao PostgreSQL. Mantém o CRUD original do trabalho anterior e acrescenta os objetos pedidos no trabalho de **Procedures, Functions e Triggers**.

## Requisitos

- Java 21 (JDK)
- PostgreSQL 16 com `psql` no PATH, ou a instalação portátil usada pelos scripts
- Driver PostgreSQL, HikariCP e SLF4J em `lib/` (já incluídos)

## Executar

Na raiz do projeto (`SQL-Barbearia-main`):

```powershell
.\run             # comando único: abre o sistema
.\run -Demo       # demonstração automática: recria o banco, testa e executa os exemplos
```

O script procura Java/PostgreSQL nesta ordem: variáveis de ambiente (`JAVA_HOME`, `PGBIN`, `PGDATA`), PATH e instalação portátil em `AppData\Local\Temp\opencode\tools`. Se o PostgreSQL estiver parado e não houver cluster configurado, ele avisa em vez de tentar iniciar um caminho inexistente. No menu, a opção **9** mostra Procedure, Functions e Triggers; a opção **10** imprime a folha de comandos para deixar visível durante a apresentação.

## Recriar o banco

Com a aplicação fechada:

```powershell
& "<pg>\bin\psql.exe" -U postgres -d postgres -c "DROP DATABASE IF EXISTS barbearia;"
& "<pg>\bin\psql.exe" -U postgres -d postgres -f sql\barbearia.sql
```

> `sql/barbearia.sql` contém `CREATE DATABASE` e `\c barbearia`, por isso precisa ser executado com `psql`, conectado inicialmente ao banco `postgres`.

## Testes

```powershell
# Recria o banco, executa testes SQL, compila e testa o menu
.\scripts\smoke.ps1

# Somente os testes dos objetos SQL
& "<pg>\bin\psql.exe" -U postgres -d postgres -f sql\testes.sql
```

O teste SQL verifica: functions, conflito de horário, auditoria, `COMMIT`, `ROLLBACK` e pagamento duplicado. O smoke test verifica as quatro telas novas do menu.

## Estrutura

```text
sql/barbearia.sql                  tabelas + functions + triggers + procedure
sql/testes.sql                     testes automatizados do banco
src/Main.java                      entrada da aplicação
src/database/DatabaseConnection    pool HikariCP
src/dao/                           CRUD + RelatorioDAO + OperacaoBancoDAO
src/service/                       regras de tela e menu
src/model/                         entidades + ResultadoOperacao
src/view/MenuView.java             menu principal
scripts/ambiente.ps1               localização de Java/PostgreSQL e startup
scripts/smoke.ps1                  teste ponta a ponta
COLINHA.md                         roteiro para explicar no seminário
docs/DIAGRAMAS.md                  casos de uso e DER
docs/superpowers/                  especificação e plano de implementação
```

## Objetos do banco

| Objeto | Tipo | Função |
|---|---|---|
| `finalizar_atendimento` | Procedure | Fecha atendimento, grava pagamento e controla `COMMIT`/`ROLLBACK` |
| `fn_preco_servico` | Function escalar | Retorna o preço de um serviço |
| `fn_faturamento_por_barbeiro` | Function tabela | Retorna atendimentos e faturamento por barbeiro |
| `trg_validar_agendamento` | Trigger | Barra domingo, fora do expediente, sobreposição e `REALIZADO` sem pagamento; trava por barbeiro/dia |
| `trg_auditoria_pagamento` | Trigger | Registra alterações de status de pagamento |
| `trg_proteger_pagamento_realizado` | Trigger | Impede excluir ou estornar pagamento de atendimento realizado |
| `AuditoriaPagamento` | Tabela | Histórico gravado pelo trigger |

## Apresentação

- Leia `COLINHA.md` para o roteiro de fala e as respostas às perguntas prováveis.
- Use `docs/DIAGRAMAS.md` para casos de uso e DER.
- O tempo sugerido é de 10 a 12 minutos de apresentação, deixando 3 para perguntas.

## Entrega

O PDF pede que aplicação e `script.sql` sejam **compartilhados via repositório** no e-mail do professor (joao.nascimento@ifpi.edu.br) até 25/11/2026. A pasta entregue não é um repositório Git, então o envio exige publicá-la:

```powershell
cd SQL-Barbearia-main
git init
git add .
git commit -m "Trabalho BD2: CRUD, Procedure, Functions e Triggers"
git remote add origin <URL_DO_SEU_REPOSITORIO>
git push -u origin main
```

Depois envie o link do repositório por e-mail. Se o professor aceitar o projeto compactado, o ZIP precisa conter `sql/barbearia.sql`, `sql/testes.sql`, `COLINHA.md`, `docs/DIAGRAMAS.md` e a pasta `src`. Confirme a forma aceita antes de assumir que o ZIP substitui o repositório.
