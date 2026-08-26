set timezone to 'America/Sao_Paulo';

-- ENUMs
CREATE TYPE status_enum AS ENUM ('NAO_INICIADO', 'INICIADO', 'FINALIZADO');
CREATE TYPE status_meta_enum AS ENUM ('ABAIXO_DO_ESPERADO', 'REGULAR', 'ACIMA_DO_ESPERADO');
CREATE TYPE status_problema_enum AS ENUM ('EM_ANALISE', 'EM_RESOLUCAO', 'RESOLVIDO');
CREATE TYPE prioridade_enum AS ENUM ('ALTO', 'MEDIO', 'BAIXO');
CREATE TYPE etapas_ciclo_enum AS ENUM ('PLAN', 'DO', 'CHECK', 'ACT');
-- Endereço
CREATE TABLE IF NOT EXISTS endereco (
                                        endereco_id SERIAL PRIMARY KEY,
                                        rua VARCHAR(30) NOT NULL,
    bairro VARCHAR(30) NOT NULL,
    cidade VARCHAR(30) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    cep CHAR(8) NOT NULL,
    numero VARCHAR(10) NOT NULL,
    complemento TEXT
    );

-- Empresa
CREATE TABLE IF NOT EXISTS empresa (
                                       empresa_id SERIAL PRIMARY KEY,
                                       nome VARCHAR(30) NOT NULL,
    setor VARCHAR(30) NOT NULL,
    unidade VARCHAR(30) NOT NULL,
    endereco_id INT REFERENCES endereco(endereco_id),
    cnpj CHAR(14) UNIQUE NOT NULL
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
    cargo VARCHAR(30) NOT NULL,
    email VARCHAR(80) UNIQUE NOT NULL,
    telefone CHAR(11) NOT NULL,
    cpf CHAR(11) UNIQUE NOT NULL,
    empresa_id INT NOT NULL REFERENCES empresa(empresa_id)
    );

-- Projeto
CREATE TABLE IF NOT EXISTS ciclo (
                                     ciclo_id SERIAL PRIMARY KEY,
                                     nome VARCHAR(30) NOT NULL,
    etapa_atual etapas_ciclo_enum NOT NULL DEFAULT 'PLAN',
    dt_inicio DATE NOT NULL,
    dt_fim DATE,
    status status_enum NOT NULL DEFAULT 'NAO_INICIADO',
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

-- Meta
CREATE TABLE IF NOT EXISTS meta (
                                    meta_id SERIAL PRIMARY KEY,
                                    meta VARCHAR(100) NOT NULL,
    descricao_meta TEXT,
    objetivo VARCHAR(200),
    prazo DATE NOT NULL,
    status status_meta_enum NOT NULL DEFAULT 'REGULAR',
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ciclo_id INT NOT NULL REFERENCES ciclo(ciclo_id)
    );

-- Plano de Ação
CREATE TABLE IF NOT EXISTS plano_acao (
                                          plano_acao_id SERIAL PRIMARY KEY,
                                          nome VARCHAR(20) NOT NULL,
    descricao TEXT,
    status status_enum NOT NULL DEFAULT 'NAO_INICIADO',
    prioridade prioridade_enum NOT NULL DEFAULT 'MEDIO',
    ciclo_id INT NOT NULL REFERENCES ciclo(ciclo_id)
    );

-- 5W2H
CREATE TABLE IF NOT EXISTS plano_acao5w2h (
                                              plano_acao_5w2h_id SERIAL PRIMARY KEY,
                                              what VARCHAR(50) NOT NULL,
    why VARCHAR(50),
    "where" VARCHAR(50),
    "when" DATE,
    who VARCHAR(50),
    how VARCHAR(50),
    how_much VARCHAR(50),
    plano_acao_id INT NOT NULL REFERENCES plano_acao(plano_acao_id)
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
    colaborador_id INT NOT NULL REFERENCES colaborador(colaborador_id)
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
    ciclo_id INT NOT NULL REFERENCES ciclo(ciclo_id)
    );

-- Problema
CREATE TABLE IF NOT EXISTS problema (
                                        problema_id SERIAL PRIMARY KEY,
                                        titulo VARCHAR(100) NOT NULL,
    descricao TEXT NOT NULL,
    solucao VARCHAR(200),
    status status_problema_enum NOT NULL DEFAULT 'EM_ANALISE',
    origem TEXT,
    encontrado_em DATE NOT NULL,
    ciclo_id INT NOT NULL REFERENCES ciclo(ciclo_id),
    plano_acao_id INT REFERENCES plano_acao(plano_acao_id)
    );

-- Relacionamentos N:N

CREATE TABLE IF NOT EXISTS ciclo_colaborador (
                                                 ciclo_id INT NOT NULL REFERENCES ciclo (ciclo_id),
    colaborador_id INT NOT NULL REFERENCES colaborador (colaborador_id),
    PRIMARY KEY (ciclo_id, colaborador_id)
    );

CREATE TABLE IF NOT EXISTS empresa_administrador_geral (
                                                           empresa_id INT NOT NULL REFERENCES empresa (empresa_id),
    adm_geral_id INT NOT NULL REFERENCES administrador_geral (adm_geral_id),
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