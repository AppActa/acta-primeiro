-- BANCO DE DADOS ACTA
set timezone to 'America/Sao_Paulo';

DROP TABLE IF EXISTS
    endereco,
    empresa,
    administrador_geral,
    colaborador,
    ciclo,
    meta,
    plano_acao,
    plano_acao5w2h,
    tarefa,
    licoes_aprendidas,
    problema,
    ciclo_colaborador,
    empresa_administrador_geral;

DROP TYPE IF EXISTS
    tamanho_empresa_enum,
    status_enum,
    status_empresa_enum,
    status_meta_enum,
    status_problema_enum,
    prioridade_enum,
    etapas_ciclo_enum;

-- ENUMs
CREATE TYPE tamanho_empresa_enum AS ENUM('GRANDE','PEQUENA','MEDIA');
CREATE TYPE status_empresa_enum AS ENUM('ATIVA','INATIVA');
CREATE TYPE status_enum AS ENUM ('NAO_INICIADO', 'INICIADO', 'FINALIZADO');
CREATE TYPE status_meta_enum AS ENUM ('ABAIXO_DO_ESPERADO', 'REGULAR', 'ACIMA_DO_ESPERADO');
CREATE TYPE status_problema_enum AS ENUM ('EM_ANALISE', 'EM_RESOLUCAO', 'RESOLVIDO');
CREATE TYPE prioridade_enum AS ENUM ('ALTO', 'MEDIO', 'BAIXO');
CREATE TYPE etapas_ciclo_enum AS ENUM ('PLAN', 'DO', 'CHECK', 'ACT');

-- Empresa
CREATE TABLE IF NOT EXISTS empresa (
                                       empresa_id SERIAL PRIMARY KEY,
                                       nome VARCHAR(30) NOT NULL,
                                       setor VARCHAR(30) NOT NULL,
                                       cnpj CHAR(14) UNIQUE NOT NULL,
                                       status status_empresa_enum NOT NULL DEFAULT 'ATIVA',
                                       tamanho tamanho_empresa_enum DEFAULT 'MEDIA'
);

-- Endereço
CREATE TABLE IF NOT EXISTS endereco (
                                        endereco_id SERIAL PRIMARY KEY,
                                        rua VARCHAR(30) NOT NULL,
                                        bairro VARCHAR(30) NOT NULL,
                                        cidade VARCHAR(30) NOT NULL,
                                        estado CHAR(2) NOT NULL,
                                        cep CHAR(8) NOT NULL,
                                        numero VARCHAR(10) NOT NULL,
                                        complemento TEXT,
                                        unidade VARCHAR(30) NOT NULL,
                                        empresa_id INT NOT NULL REFERENCES empresa(empresa_id) ON DELETE CASCADE
);

-- Administrador Geral
CREATE TABLE IF NOT EXISTS administrador_geral (
                                                   adm_geral_id SERIAL PRIMARY KEY,
                                                   nome VARCHAR(30) NOT NULL,
                                                   senha VARCHAR(100) NOT NULL,
                                                   email VARCHAR(80) UNIQUE NOT NULL,
                                                   telefone CHAR(11) NOT NULL
);


-- Colaborador
CREATE TABLE IF NOT EXISTS colaborador (
                                           colaborador_id SERIAL PRIMARY KEY,
                                           nome VARCHAR(30) NOT NULL,
                                           sobrenome VARCHAR(50) NOT NULL,
                                           permissao_gestor BOOLEAN NOT NULL DEFAULT FALSE,
                                           area VARCHAR(30) NOT NULL,
                                           cargo VARCHAR(50) NOT NULL,
                                           dt_contratacao DATE NOT NULL CHECK (dt_contratacao<=current_date),
                                           email VARCHAR(80) UNIQUE NOT NULL,
                                           senha VARCHAR(100) UNIQUE NOT NULL,
                                           telefone CHAR(11) NOT NULL,
                                           cpf CHAR(11) UNIQUE NOT NULL,
                                           empresa_id INT NOT NULL REFERENCES empresa(empresa_id) ON DELETE CASCADE
);

-- Projeto
CREATE TABLE IF NOT EXISTS ciclo (
                                     ciclo_id SERIAL PRIMARY KEY,
                                     nome VARCHAR(30) NOT NULL,
                                     descricao TEXT,
                                     etapa_atual etapas_ciclo_enum NOT NULL DEFAULT 'PLAN',
                                     dt_inicio DATE NOT NULL,
                                     dt_fim DATE,
                                     status status_enum NOT NULL DEFAULT 'NAO_INICIADO',
                                     criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                     empresa_id INT NOT NULL REFERENCES empresa(empresa_id) ON DELETE CASCADE,
                                     responsavel_id INT NOT NULL REFERENCES colaborador(colaborador_id)
);
-- Plano de Ação
CREATE TABLE IF NOT EXISTS plano_acao (
                                          plano_acao_id SERIAL PRIMARY KEY,
                                          nome VARCHAR(20) NOT NULL,
                                          descricao TEXT,
                                          status status_enum NOT NULL DEFAULT 'NAO_INICIADO',
                                          prioridade prioridade_enum NOT NULL DEFAULT 'MEDIO',
                                          ciclo_id INT NOT NULL REFERENCES ciclo(ciclo_id),
                                          criador_id INT NOT NULL REFERENCES colaborador(colaborador_id) ON DELETE RESTRICT
);

-- 5W2H
CREATE TABLE IF NOT EXISTS plano_acao5w2h (
                                              plano_acao_5w2h_id SERIAL PRIMARY KEY,
                                              what VARCHAR(50) NOT NULL,
                                              why VARCHAR(50),
                                              "where" VARCHAR(50),
                                              "when" DATE,
                                              who INT NOT NULL REFERENCES colaborador(colaborador_id) ON DELETE RESTRICT,
                                              how VARCHAR(50),
                                              how_much NUMERIC,
                                              plano_acao_id INT REFERENCES plano_acao(plano_acao_id) ON DELETE CASCADE
);
-- Meta
CREATE TABLE IF NOT EXISTS meta (
                                    meta_id SERIAL PRIMARY KEY,
                                    meta VARCHAR(100) NOT NULL,
                                    descricao_meta TEXT,
                                    objetivo VARCHAR(200),
                                    prioridade VARCHAR(15),
                                    prazo DATE NOT NULL,
                                    status status_meta_enum NOT NULL DEFAULT 'REGULAR',
                                    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                    ciclo_id INT NOT NULL REFERENCES ciclo(ciclo_id) ON DELETE CASCADE,
                                    plano_acao_id INT REFERENCES plano_acao(plano_acao_id) ON DELETE SET NULL
);

-- Tarefa
CREATE TABLE IF NOT EXISTS tarefa (
                                      tarefa_id SERIAL PRIMARY KEY,
                                      titulo VARCHAR(20) NOT NULL,
                                      descricao TEXT,
                                      prioridade prioridade_enum NOT NULL DEFAULT 'MEDIO',
                                      dt_entrega DATE,
                                      status status_enum NOT NULL DEFAULT 'NAO_INICIADO',
                                      dt_inicio DATE NOT NULL DEFAULT now(),
                                      colaborador_id INT NOT NULL REFERENCES colaborador(colaborador_id) ON DELETE RESTRICT
);

-- Lições Aprendidas
CREATE TABLE IF NOT EXISTS licoes_aprendidas (
                                                 licao_id SERIAL PRIMARY KEY,
                                                 titulo VARCHAR(50) NOT NULL,
                                                 area VARCHAR(30),
                                                 aprendizado TEXT NOT NULL,
                                                 categoria VARCHAR(30),
                                                 descricao TEXT,
                                                 fase_origem VARCHAR(8),
                                                 severidade VARCHAR(50),
                                                 ciclo_id INT REFERENCES ciclo(ciclo_id) ON DELETE CASCADE
);

-- Problema
CREATE TABLE IF NOT EXISTS problema (
                                        problema_id SERIAL PRIMARY KEY,
                                        titulo VARCHAR(100) NOT NULL,
                                        descricao TEXT NOT NULL,
                                        peso VARCHAR(30),
                                        solucao VARCHAR(200),
                                        status status_problema_enum NOT NULL DEFAULT 'EM_ANALISE',
                                        origem TEXT,
                                        encontrado_em DATE NOT NULL,
                                        ciclo_id INT NOT NULL REFERENCES ciclo(ciclo_id) ON DELETE CASCADE,
                                        plano_acao_id INT REFERENCES plano_acao(plano_acao_id) ON DELETE SET NULL,
                                        colaborador_id INT REFERENCES colaborador(colaborador_id) ON DELETE SET NULL
);

-- Relacionamentos N:N

CREATE TABLE IF NOT EXISTS ciclo_colaborador (
                                                 ciclo_id INT NOT NULL REFERENCES ciclo (ciclo_id) ON DELETE CASCADE,
                                                 colaborador_id INT NOT NULL REFERENCES colaborador (colaborador_id) ON DELETE CASCADE,
                                                 PRIMARY KEY (ciclo_id, colaborador_id),
                                                 papel_ciclo VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS empresa_administrador_geral (
                                                           empresa_id INT NOT NULL REFERENCES empresa (empresa_id) ON DELETE CASCADE,
                                                           adm_geral_id INT NOT NULL REFERENCES administrador_geral (adm_geral_id) ON DELETE CASCADE,
                                                           PRIMARY KEY (empresa_id, adm_geral_id)
);

ALTER TABLE endereco
    ADD CONSTRAINT chk_cep_tamanho CHECK (length(cep)=8);

ALTER TABLE administrador_geral
    ADD CONSTRAINT chk_tamanho_telefone_adm CHECK (length(telefone)=11);

ALTER TABLE empresa
    ADD CONSTRAINT chk_cnpj_empresa CHECK (length(cnpj)=14);

ALTER TABLE colaborador
    ADD CONSTRAINT chk_cpf_colab CHECK ( length(cpf)=11);

ALTER TABLE colaborador
    ADD CONSTRAINT chk_tamanho_telefone_colab CHECK (length(telefone)=11);

ALTER TABLE ciclo
    ADD CONSTRAINT chk_dt_inicio CHECK ( dt_inicio>=current_date);

ALTER TABLE ciclo
    ADD CONSTRAINT chk_dt_fim CHECK (dt_fim>=dt_inicio);

ALTER TABLE meta
    ADD CONSTRAINT chk_prazo CHECK ( prazo>=current_date);

ALTER TABLE plano_acao5w2h
    ADD CONSTRAINT chk_when CHECK ( "when" <= current_date);

ALTER TABLE tarefa
    ADD CONSTRAINT chk_dt_entrega CHECK (dt_entrega>=current_date);

ALTER TABLE tarefa
    ADD CONSTRAINT chk_dt_inicio CHECK (dt_inicio>=current_date);

ALTER TABLE colaborador
    ALTER COLUMN cargo TYPE VARCHAR(100);

ALTER TABLE ciclo
    ALTER COLUMN nome TYPE VARCHAR(60);

ALTER TABLE tarefa
    ALTER COLUMN titulo TYPE VARCHAR(50);

ALTER TABLE plano_acao
    ALTER COLUMN nome TYPE VARCHAR(50);