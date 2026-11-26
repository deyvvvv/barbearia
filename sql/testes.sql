-- ============================================================
-- BARBEARIA DO BRAZ - Testes automatizados dos objetos SQL
-- Executar depois de sql/barbearia.sql:
--   psql -U postgres -f sql/testes.sql
-- ============================================================
\set ON_ERROR_STOP on
\c barbearia

-- ------------------------------------------------------------
-- Fixture determinística: IDs 1 e 2 para agendamentos
-- ------------------------------------------------------------
DO $$
BEGIN
    TRUNCATE TABLE AuditoriaPagamento, Avaliacao, Pagamento, Agendamento,
                   Servico, Barbeiro, Cliente
        RESTART IDENTITY CASCADE;

    INSERT INTO Barbeiro (nome, telefone, email, especialidade, data_contratacao)
    VALUES ('Mateus', '(86) 99999-1001', 'mateus@barbeariadobraz.com',
            'Corte Degradê e Barba', DATE '2024-01-15');

    INSERT INTO Servico (nome, descricao, preco, duracao_minutos)
    VALUES ('Corte Simples', 'Corte de cabelo tradicional com tesoura e máquina', 30.00, 30);

    INSERT INTO Cliente (nome, telefone, email, data_cadastro)
    VALUES ('João Silva', '(86) 99999-0001', 'joao@email.com', DATE '2025-01-10');

    INSERT INTO Agendamento (id_cliente, id_barbeiro, id_servico, data_hora, status)
    VALUES (1, 1, 1, TIMESTAMP '2026-11-26 08:00', 'AGENDADO'),
           (1, 1, 1, TIMESTAMP '2026-11-26 09:00', 'AGENDADO');
END $$;

-- ------------------------------------------------------------
-- Function escalar e Function tabela
-- ------------------------------------------------------------
DO $$
DECLARE
    v_total INTEGER;
BEGIN
    IF fn_preco_servico(1) IS DISTINCT FROM 30.00 THEN
        RAISE EXCEPTION 'FALHA: fn_preco_servico(1) deveria retornar 30.00';
    END IF;

    SELECT COUNT(*) INTO v_total FROM fn_faturamento_por_barbeiro(NULL);
    IF v_total = 0 THEN
        RAISE EXCEPTION 'FALHA: fn_faturamento_por_barbeiro(NULL) nao retornou linhas';
    END IF;

    SELECT COUNT(*) INTO v_total FROM fn_faturamento_por_barbeiro(1);
    IF v_total <> 1 THEN
        RAISE EXCEPTION 'FALHA: fn_faturamento_por_barbeiro(1) deveria retornar 1 linha';
    END IF;
END $$;

-- ------------------------------------------------------------
-- Trigger de integridade: sobreposicao de horario
-- ------------------------------------------------------------
DO $$
DECLARE
    v_barrou BOOLEAN := FALSE;
BEGIN
    BEGIN
        INSERT INTO Agendamento (id_cliente, id_barbeiro, id_servico, data_hora, status)
        VALUES (1, 1, 1, TIMESTAMP '2026-11-26 08:15', 'AGENDADO');
    EXCEPTION WHEN raise_exception THEN
        IF SQLERRM NOT LIKE 'FALHA:%' THEN
            v_barrou := TRUE;
        END IF;
    END;

    IF NOT v_barrou THEN
        RAISE EXCEPTION 'FALHA: trigger de conflito nao barrou 08:15';
    END IF;
END $$;

-- ------------------------------------------------------------
-- Trigger de integridade: fora do expediente
-- ------------------------------------------------------------
DO $$
DECLARE
    v_barrou BOOLEAN := FALSE;
BEGIN
    BEGIN
        INSERT INTO Agendamento (id_cliente, id_barbeiro, id_servico, data_hora, status)
        VALUES (1, 1, 1, TIMESTAMP '2026-11-26 17:50', 'AGENDADO');
    EXCEPTION WHEN raise_exception THEN
        IF SQLERRM NOT LIKE 'FALHA:%' THEN
            v_barrou := TRUE;
        END IF;
    END;

    IF NOT v_barrou THEN
        RAISE EXCEPTION 'FALHA: trigger nao barrou termino apos as 18:00';
    END IF;
END $$;

-- ------------------------------------------------------------
-- Procedure: caminho de COMMIT
-- ------------------------------------------------------------
CALL finalizar_atendimento(1, 'PIX', NULL, NULL);

DO $$
DECLARE
    v_status VARCHAR(20);
    v_valor NUMERIC(10,2);
    v_qtd INTEGER;
    v_auditorias INTEGER;
BEGIN
    SELECT status INTO v_status FROM Agendamento WHERE id_agendamento = 1;
    IF v_status IS DISTINCT FROM 'REALIZADO' THEN
        RAISE EXCEPTION 'FALHA: procedure nao marcou agendamento 1 como REALIZADO';
    END IF;

    SELECT COUNT(*), MAX(valor) INTO v_qtd, v_valor
    FROM Pagamento WHERE id_agendamento = 1 AND status_pagamento = 'PAGO';
    IF v_qtd <> 1 OR v_valor IS DISTINCT FROM 30.00 THEN
        RAISE EXCEPTION 'FALHA: procedure nao criou pagamento PAGO de 30.00';
    END IF;

    SELECT COUNT(*) INTO v_auditorias FROM AuditoriaPagamento;
    IF v_auditorias <> 1 THEN
        RAISE EXCEPTION 'FALHA: trigger de auditoria nao registrou o pagamento';
    END IF;
END $$;

-- ------------------------------------------------------------
-- Procedure: agendamento ja finalizado (ROLLBACK/ERRO)
-- ------------------------------------------------------------
CALL finalizar_atendimento(1, 'PIX', NULL, NULL);

DO $$
DECLARE
    v_qtd INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_qtd FROM Pagamento WHERE id_agendamento = 1;
    IF v_qtd <> 1 THEN
        RAISE EXCEPTION 'FALHA: segunda chamada criou pagamento duplicado';
    END IF;
END $$;

-- ------------------------------------------------------------
-- Procedure: id inexistente (ROLLBACK/ERRO)
-- ------------------------------------------------------------
CALL finalizar_atendimento(999999, 'PIX', NULL, NULL);

DO $$
DECLARE
    v_qtd INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_qtd FROM Pagamento;
    IF v_qtd <> 1 THEN
        RAISE EXCEPTION 'FALHA: chamada com id inexistente alterou pagamentos';
    END IF;
END $$;

-- ------------------------------------------------------------
-- Procedure: forma de pagamento invalida (ROLLBACK/ERRO)
-- ------------------------------------------------------------
CALL finalizar_atendimento(2, 'CHEQUE', NULL, NULL);

DO $$
DECLARE
    v_status VARCHAR(20);
    v_qtd INTEGER;
BEGIN
    SELECT status INTO v_status FROM Agendamento WHERE id_agendamento = 2;
    IF v_status IS DISTINCT FROM 'AGENDADO' THEN
        RAISE EXCEPTION 'FALHA: forma invalida alterou status do agendamento 2';
    END IF;

    SELECT COUNT(*) INTO v_qtd FROM Pagamento WHERE id_agendamento = 2;
    IF v_qtd <> 0 THEN
        RAISE EXCEPTION 'FALHA: forma invalida criou pagamento';
    END IF;
END $$;

-- ------------------------------------------------------------
-- Invariante: REALIZADO exige pagamento PAGO (bypass do CRUD)
-- ------------------------------------------------------------
DO $$
DECLARE
    v_barrou BOOLEAN := FALSE;
BEGIN
    BEGIN
        UPDATE Agendamento SET status = 'REALIZADO' WHERE id_agendamento = 2;
    EXCEPTION WHEN raise_exception THEN
        v_barrou := TRUE;
    END;

    IF NOT v_barrou THEN
        RAISE EXCEPTION 'FALHA: banco aceitou REALIZADO sem pagamento';
    END IF;
END $$;

-- ------------------------------------------------------------
-- Invariante: nao apagar/estornar pagamento de atendimento realizado
-- ------------------------------------------------------------
DO $$
DECLARE
    v_barrou BOOLEAN := FALSE;
BEGIN
    BEGIN
        DELETE FROM Pagamento WHERE id_agendamento = 1;
    EXCEPTION WHEN raise_exception THEN
        v_barrou := TRUE;
    END;

    IF NOT v_barrou THEN
        RAISE EXCEPTION 'FALHA: banco permitiu excluir pagamento de atendimento realizado';
    END IF;
END $$;

-- ------------------------------------------------------------
-- Handler de excecao da procedure: erro inesperado no INSERT
-- ------------------------------------------------------------
CREATE OR REPLACE FUNCTION falhar_insercao_pagamento()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE EXCEPTION 'FALHA FORCADA no pagamento';
END;
$$;

DROP TRIGGER IF EXISTS trg_teste_falha_pagamento ON Pagamento;
CREATE TRIGGER trg_teste_falha_pagamento
    BEFORE INSERT ON Pagamento
    FOR EACH ROW
    EXECUTE FUNCTION falhar_insercao_pagamento();

CALL finalizar_atendimento(2, 'PIX', NULL, NULL);

DROP TRIGGER trg_teste_falha_pagamento ON Pagamento;

DO $$
DECLARE
    v_status VARCHAR(20);
    v_qtd INTEGER;
BEGIN
    SELECT status INTO v_status FROM Agendamento WHERE id_agendamento = 2;
    IF v_status IS DISTINCT FROM 'AGENDADO' THEN
        RAISE EXCEPTION 'FALHA: excecao no INSERT nao desfez o UPDATE do agendamento 2';
    END IF;

    SELECT COUNT(*) INTO v_qtd FROM Pagamento WHERE id_agendamento = 2;
    IF v_qtd <> 0 THEN
        RAISE EXCEPTION 'FALHA: excecao no INSERT criou pagamento';
    END IF;
END $$;

DO $$ BEGIN RAISE NOTICE 'OK: testes SQL concluidos'; END $$;
