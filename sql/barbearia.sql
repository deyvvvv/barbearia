-- ============================================================
-- BARBEARIA DO BRAZ - Script de criação do banco de dados
-- ATENÇÃO: não é idempotente. Antes de reexecutar, derrube o banco:
--   DROP DATABASE IF EXISTS barbearia;
-- Execute este arquivo conectado ao banco "postgres" via psql.
-- ============================================================

CREATE DATABASE barbearia ENCODING 'UTF8' TEMPLATE template0;

\c barbearia;

-- ============================================================
-- TABELA: CLIENTE
-- ============================================================
CREATE TABLE Cliente (
    id_cliente SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    data_cadastro DATE NOT NULL DEFAULT CURRENT_DATE
);

ALTER TABLE Cliente ADD CONSTRAINT uk_cliente_telefone UNIQUE (telefone);
ALTER TABLE Cliente ADD CONSTRAINT uk_cliente_email UNIQUE (email);

-- ============================================================
-- TABELA: BARBEIRO
-- ============================================================
CREATE TABLE Barbeiro (
    id_barbeiro SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    especialidade VARCHAR(100) NOT NULL,
    data_contratacao DATE NOT NULL DEFAULT CURRENT_DATE
);

ALTER TABLE Barbeiro ADD CONSTRAINT uk_barbeiro_telefone UNIQUE (telefone);
ALTER TABLE Barbeiro ADD CONSTRAINT uk_barbeiro_email UNIQUE (email);

-- ============================================================
-- TABELA: SERVICO
-- ============================================================
CREATE TABLE Servico (
    id_servico SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(255),
    preco DECIMAL(10, 2) NOT NULL,
    duracao_minutos INTEGER NOT NULL,
    CONSTRAINT ck_servico_preco CHECK (preco > 0),
    CONSTRAINT ck_servico_duracao CHECK (duracao_minutos > 0)
);

ALTER TABLE Servico ADD CONSTRAINT uk_servico_nome UNIQUE (nome);

-- ============================================================
-- TABELA: AGENDAMENTO
-- ============================================================
CREATE TABLE Agendamento (
    id_agendamento SERIAL PRIMARY KEY,
    id_cliente INTEGER NOT NULL,
    id_barbeiro INTEGER NOT NULL,
    id_servico INTEGER NOT NULL,
    data_hora TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AGENDADO',
    observacoes VARCHAR(255),
    CONSTRAINT fk_agendamento_cliente FOREIGN KEY (id_cliente)
        REFERENCES Cliente (id_cliente) ON DELETE CASCADE,
    CONSTRAINT fk_agendamento_barbeiro FOREIGN KEY (id_barbeiro)
        REFERENCES Barbeiro (id_barbeiro) ON DELETE CASCADE,
    CONSTRAINT fk_agendamento_servico FOREIGN KEY (id_servico)
        REFERENCES Servico (id_servico) ON DELETE CASCADE,
    CONSTRAINT ck_agendamento_status CHECK (
        status IN ('AGENDADO', 'CONFIRMADO', 'CANCELADO', 'REALIZADO')
    )
);

-- ============================================================
-- TABELA: PAGAMENTO
-- ============================================================
CREATE TABLE Pagamento (
    id_pagamento SERIAL PRIMARY KEY,
    id_agendamento INTEGER NOT NULL,
    valor DECIMAL(10, 2) NOT NULL,
    data_pagamento DATE NOT NULL DEFAULT CURRENT_DATE,
    forma_pagamento VARCHAR(30) NOT NULL,
    status_pagamento VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    CONSTRAINT fk_pagamento_agendamento FOREIGN KEY (id_agendamento)
        REFERENCES Agendamento (id_agendamento) ON DELETE CASCADE,
    CONSTRAINT ck_pagamento_valor CHECK (valor > 0),
    CONSTRAINT ck_pagamento_forma CHECK (
        forma_pagamento IN ('DINHEIRO', 'CARTAO_CREDITO', 'CARTAO_DEBITO', 'PIX')
    ),
    CONSTRAINT ck_pagamento_status CHECK (
        status_pagamento IN ('PENDENTE', 'PAGO', 'ESTORNADO')
    )
);

ALTER TABLE Pagamento ADD CONSTRAINT uk_pagamento_agendamento UNIQUE (id_agendamento);

-- ============================================================
-- TABELA: AVALIACAO
-- ============================================================
CREATE TABLE Avaliacao (
    id_avaliacao SERIAL PRIMARY KEY,
    id_agendamento INTEGER NOT NULL,
    id_cliente INTEGER NOT NULL,
    id_barbeiro INTEGER NOT NULL,
    nota INTEGER NOT NULL,
    comentario VARCHAR(500),
    data_avaliacao DATE NOT NULL DEFAULT CURRENT_DATE,
    CONSTRAINT fk_avaliacao_agendamento FOREIGN KEY (id_agendamento)
        REFERENCES Agendamento (id_agendamento) ON DELETE CASCADE,
    CONSTRAINT fk_avaliacao_cliente FOREIGN KEY (id_cliente)
        REFERENCES Cliente (id_cliente) ON DELETE CASCADE,
    CONSTRAINT fk_avaliacao_barbeiro FOREIGN KEY (id_barbeiro)
        REFERENCES Barbeiro (id_barbeiro) ON DELETE CASCADE,
    CONSTRAINT ck_avaliacao_nota CHECK (nota >= 1 AND nota <= 5)
);

ALTER TABLE Avaliacao ADD CONSTRAINT uk_avaliacao_agendamento UNIQUE (id_agendamento);

-- ============================================================
-- INDICES PARA PERFORMANCE
-- ============================================================
CREATE INDEX idx_agendamento_data_hora ON Agendamento (data_hora);
CREATE INDEX idx_agendamento_id_cliente ON Agendamento (id_cliente);
CREATE INDEX idx_agendamento_id_barbeiro ON Agendamento (id_barbeiro);
CREATE INDEX idx_agendamento_status ON Agendamento (status);
CREATE INDEX idx_pagamento_id_agendamento ON Pagamento (id_agendamento);
CREATE INDEX idx_avaliacao_id_barbeiro ON Avaliacao (id_barbeiro);

-- ============================================================
-- INSERTS - BARBEIROS
-- ============================================================
INSERT INTO Barbeiro (nome, telefone, email, especialidade, data_contratacao) VALUES
('Mateus', '(86) 99999-1001', 'mateus@barbeariadobraz.com', 'Corte Degradê e Barba', '2024-01-15'),
('Deyvid', '(86) 99999-1002', 'deyvid@barbeariadobraz.com', 'Corte Clássico e Social', '2024-03-20'),
('Weslley', '(86) 99999-1003', 'weslley@barbeariadobraz.com', 'Design Capilar e Hidratação', '2024-06-01');

-- ============================================================
-- INSERTS - SERVICOS
-- ============================================================
INSERT INTO Servico (nome, descricao, preco, duracao_minutos) VALUES
('Corte Simples', 'Corte de cabelo tradicional com tesoura e máquina', 30.00, 30),
('Corte Degradê', 'Corte com efeito degradê dos lados e parte de cima', 45.00, 45),
('Barba Completa', 'Aparação e modelagem completa da barba', 25.00, 20),
('Corte + Barba', 'Combo corte de cabelo com barba completa', 50.00, 50),
('Hidratação Capilar', 'Tratamento de hidratação para cabelos', 40.00, 40),
('Corte Infantil', 'Corte de cabelo para crianças até 12 anos', 25.00, 25),
('Corte Social', 'Corte social clássico para ocasiões formais', 35.00, 35),
('Design Capilar', 'Desenho e design personalizado no cabelo', 55.00, 60),
('Barba Desenhada', 'Barba com desenho e linhas definidas', 30.00, 25),
('Corte + Hidratação', 'Combo corte de cabelo com hidratação', 60.00, 60),
('Sobrancelha', 'Design de sobrancelha masculina', 15.00, 15),
('Combo Completo', 'Corte, barba e hidratação', 70.00, 75);

-- ============================================================
-- INSERTS - CLIENTES (para teste)
-- ============================================================
INSERT INTO Cliente (nome, telefone, email, data_cadastro) VALUES
('João Silva', '(86) 99999-0001', 'joao@email.com', '2025-01-10'),
('Maria Oliveira', '(86) 99999-0002', 'maria@email.com', '2025-01-15'),
('Carlos Santos', '(86) 99999-0003', 'carlos@email.com', '2025-02-01'),
('Ana Costa', '(86) 99999-0004', 'ana@email.com', '2025-02-10'),
('Pedro Almeida', '(86) 99999-0005', 'pedro@email.com', '2025-03-05'),
('Lucas Pereira', '(86) 99999-0006', 'lucas@email.com', '2025-03-10');

-- ============================================================
-- AUDITORIA DE PAGAMENTOS (usada pelo trigger)
-- ============================================================
CREATE TABLE AuditoriaPagamento (
    id_auditoria SERIAL PRIMARY KEY,
    id_pagamento INTEGER NOT NULL,
    status_anterior VARCHAR(20),
    status_novo VARCHAR(20) NOT NULL,
    registrado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- FUNCTION: retorna o preco de um servico (valor escalar)
-- ============================================================
CREATE OR REPLACE FUNCTION fn_preco_servico(p_id_servico INTEGER)
RETURNS NUMERIC(10, 2)
LANGUAGE sql
STABLE
AS $$
    SELECT preco FROM Servico WHERE id_servico = p_id_servico;
$$;

-- ============================================================
-- FUNCTION: faturamento por barbeiro (valor tabular)
-- NULL no parametro retorna todos os barbeiros.
-- ============================================================
CREATE OR REPLACE FUNCTION fn_faturamento_por_barbeiro(p_id_barbeiro INTEGER DEFAULT NULL)
RETURNS TABLE (
    id_barbeiro INTEGER,
    nome VARCHAR(100),
    atendimentos BIGINT,
    faturamento NUMERIC
)
LANGUAGE sql
STABLE
AS $$
    SELECT b.id_barbeiro,
           b.nome,
           COUNT(a.id_agendamento) AS atendimentos,
           COALESCE(SUM(p.valor), 0) AS faturamento
    FROM Barbeiro b
    LEFT JOIN Agendamento a
           ON a.id_barbeiro = b.id_barbeiro
          AND a.status = 'REALIZADO'
    LEFT JOIN Pagamento p
           ON p.id_agendamento = a.id_agendamento
          AND p.status_pagamento = 'PAGO'
    WHERE p_id_barbeiro IS NULL OR b.id_barbeiro = p_id_barbeiro
    GROUP BY b.id_barbeiro, b.nome
    ORDER BY faturamento DESC;
$$;

-- ============================================================
-- TRIGGER: integridade dos agendamentos
-- Regra de negocio no banco: nao deixa o Java (ou outro cliente)
-- inserir horarios sobrepostos, aos domingos ou fora do expediente.
-- ============================================================
CREATE OR REPLACE FUNCTION validar_agendamento()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_duracao INTEGER;
    v_fim TIMESTAMP;
    v_conflito BOOLEAN;
BEGIN
    IF NEW.status = 'CANCELADO' THEN
        RETURN NEW;
    END IF;

    IF NEW.data_hora IS NULL THEN
        RAISE EXCEPTION 'Data e hora do agendamento sao obrigatorias';
    END IF;

    SELECT duracao_minutos INTO v_duracao
    FROM Servico WHERE id_servico = NEW.id_servico;

    IF v_duracao IS NULL THEN
        RAISE EXCEPTION 'Servico % nao encontrado', NEW.id_servico;
    END IF;

    IF EXTRACT(ISODOW FROM NEW.data_hora) = 7 THEN
        RAISE EXCEPTION 'A barbearia nao abre aos domingos';
    END IF;

    v_fim := NEW.data_hora + (v_duracao || ' minutes')::interval;

    IF NEW.data_hora::TIME < TIME '08:00' OR v_fim::TIME > TIME '18:00' THEN
        RAISE EXCEPTION 'Horario fora do expediente (08:00 as 18:00)';
    END IF;

    -- Um atendimento so pode virar REALIZADO depois do pagamento PAGO.
    IF NEW.status = 'REALIZADO' AND NOT EXISTS (
        SELECT 1 FROM Pagamento p
        WHERE p.id_agendamento = NEW.id_agendamento
          AND p.status_pagamento = 'PAGO'
    ) THEN
        RAISE EXCEPTION 'Agendamento so pode ser REALIZADO apos pagamento PAGO';
    END IF;

    -- Serializa alteracoes na agenda do mesmo barbeiro no mesmo dia.
    -- Sem essa trava, duas sessoes poderiam passar no SELECT EXISTS abaixo.
    PERFORM pg_advisory_xact_lock(
        hashtextextended(NEW.id_barbeiro::text || ':' || NEW.data_hora::date::text, 0::bigint)
    );

    SELECT EXISTS (
        SELECT 1
        FROM Agendamento a
        INNER JOIN Servico s ON s.id_servico = a.id_servico
        WHERE a.id_barbeiro = NEW.id_barbeiro
          AND a.id_agendamento IS DISTINCT FROM NEW.id_agendamento
          AND a.status <> 'CANCELADO'
          AND NEW.data_hora < a.data_hora + (s.duracao_minutos || ' minutes')::interval
          AND a.data_hora < NEW.data_hora + (v_duracao || ' minutes')::interval
    ) INTO v_conflito;

    IF v_conflito THEN
        RAISE EXCEPTION 'Conflito de horario: o barbeiro % ja tem atendimento nesse periodo',
            NEW.id_barbeiro;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_validar_agendamento ON Agendamento;
CREATE TRIGGER trg_validar_agendamento
    BEFORE INSERT OR UPDATE OF id_barbeiro, id_servico, data_hora, status
    ON Agendamento
    FOR EACH ROW
    EXECUTE FUNCTION validar_agendamento();

-- ============================================================
-- TRIGGER: auditoria automatica de mudancas no pagamento
-- ============================================================
CREATE OR REPLACE FUNCTION auditar_pagamento()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO AuditoriaPagamento (id_pagamento, status_anterior, status_novo)
    VALUES (
        NEW.id_pagamento,
        CASE WHEN TG_OP = 'INSERT' THEN NULL ELSE OLD.status_pagamento END,
        NEW.status_pagamento
    );
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_auditoria_pagamento ON Pagamento;
CREATE TRIGGER trg_auditoria_pagamento
    AFTER INSERT OR UPDATE OF status_pagamento
    ON Pagamento
    FOR EACH ROW
    EXECUTE FUNCTION auditar_pagamento();

-- ============================================================
-- TRIGGER: protege o pagamento de um atendimento ja realizado
-- ============================================================
CREATE OR REPLACE FUNCTION proteger_pagamento_realizado()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_id_agendamento INTEGER;
    v_status VARCHAR(20);
BEGIN
    IF TG_OP = 'DELETE' THEN
        v_id_agendamento := OLD.id_agendamento;
    ELSE
        v_id_agendamento := NEW.id_agendamento;
    END IF;

    SELECT status INTO v_status
    FROM Agendamento WHERE id_agendamento = v_id_agendamento;

    IF v_status = 'REALIZADO'
       AND (TG_OP = 'DELETE' OR NEW.status_pagamento IS DISTINCT FROM 'PAGO') THEN
        RAISE EXCEPTION 'Pagamento de atendimento REALIZADO nao pode ser excluido ou estornado';
    END IF;

    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_proteger_pagamento_realizado ON Pagamento;
CREATE TRIGGER trg_proteger_pagamento_realizado
    BEFORE DELETE OR UPDATE OF status_pagamento
    ON Pagamento
    FOR EACH ROW
    EXECUTE FUNCTION proteger_pagamento_realizado();

-- ============================================================
-- PROCEDURE: finalizar atendimento com transacao
-- Caminho valido: INSERT pagamento + UPDATE status + COMMIT.
-- Condicoes invalidas: ROLLBACK desfazendo INSERT e UPDATE ja feitos.
-- Out parameters devolvem o resultado para a aplicacao Java.
-- ============================================================
CREATE OR REPLACE PROCEDURE finalizar_atendimento(
    p_id_agendamento INTEGER,
    p_forma_pagamento VARCHAR,
    OUT p_resultado VARCHAR,
    OUT p_mensagem VARCHAR
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_status VARCHAR(20);
    v_status_pagamento VARCHAR(20);
    v_id_servico INTEGER;
    v_valor NUMERIC(10, 2);
    v_valor_pagamento NUMERIC(10, 2);
    v_erro BOOLEAN := FALSE;
BEGIN
    p_resultado := 'ERRO';
    p_mensagem := '';

    -- Bloco com handler: qualquer falha e capturada aqui. As decisoes de
    -- COMMIT/ROLLBACK ficam FORA deste bloco (PostgreSQL nao permite transacao
    -- dentro de bloco que trata excecoes).
    BEGIN
        -- Travamento da linha: duas sessoes nao finalizam o mesmo agendamento.
        SELECT status, id_servico INTO v_status, v_id_servico
        FROM Agendamento
        WHERE id_agendamento = p_id_agendamento
        FOR UPDATE;

        IF NOT FOUND THEN
            RAISE EXCEPTION 'Agendamento % nao encontrado', p_id_agendamento;
        END IF;

        IF v_status NOT IN ('AGENDADO', 'CONFIRMADO') THEN
            RAISE EXCEPTION 'Agendamento % ja esta %', p_id_agendamento, v_status;
        END IF;

        IF p_forma_pagamento IS NULL
           OR UPPER(p_forma_pagamento) NOT IN
              ('DINHEIRO', 'CARTAO_CREDITO', 'CARTAO_DEBITO', 'PIX') THEN
            RAISE EXCEPTION 'Forma de pagamento invalida: %',
                COALESCE(p_forma_pagamento, 'NULL');
        END IF;

        -- Function escalar auxilia a procedure no calculo do valor.
        v_valor := fn_preco_servico(v_id_servico);

        IF v_valor IS NULL OR v_valor <= 0 THEN
            RAISE EXCEPTION 'Servico do agendamento nao possui preco valido';
        END IF;

        IF EXISTS (SELECT 1 FROM Pagamento WHERE id_agendamento = p_id_agendamento) THEN
            SELECT status_pagamento, valor INTO v_status_pagamento, v_valor_pagamento
            FROM Pagamento
            WHERE id_agendamento = p_id_agendamento
            LIMIT 1;

            IF v_status_pagamento IS DISTINCT FROM 'PAGO'
               OR v_valor_pagamento IS DISTINCT FROM v_valor THEN
                RAISE EXCEPTION 'Pagamento existente e incompativel com o servico';
            END IF;
        ELSE
            INSERT INTO Pagamento (id_agendamento, valor, data_pagamento,
                                   forma_pagamento, status_pagamento)
            VALUES (p_id_agendamento, v_valor, CURRENT_DATE,
                    UPPER(p_forma_pagamento), 'PAGO');
        END IF;

        -- O pagamento entra antes: o trigger da agenda exige pagamento PAGO
        -- para aceitar o status REALIZADO.
        UPDATE Agendamento
        SET status = 'REALIZADO'
        WHERE id_agendamento = p_id_agendamento;

    EXCEPTION WHEN OTHERS THEN
        p_mensagem := SQLERRM;
        v_erro := TRUE;
    END;

    IF v_erro THEN
        -- Desfaz UPDATE e INSERT feitos dentro do bloco.
        ROLLBACK;
        RETURN;
    END IF;

    COMMIT;
    p_resultado := 'OK';
    p_mensagem := 'Atendimento finalizado e pagamento de R$ ' || v_valor || ' registrado';
END;
$$;
