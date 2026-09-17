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
    situacao_enum,
    status_empresa_enum,
    status_meta_enum,
    status_problema_enum,
    prioridade_enum,
    etapas_ciclo_enum;

-- ENUMs
CREATE TYPE tamanho_empresa_enum AS ENUM('GRANDE','PEQUENA','MEDIA');
CREATE TYPE status_enum AS ENUM('ATIVA','INATIVA');
CREATE TYPE situacao_enum AS ENUM ('NAO_INICIADO', 'INICIADO', 'FINALIZADO');
CREATE TYPE status_meta_enum AS ENUM ('ABAIXO_DO_ESPERADO', 'REGULAR', 'ACIMA_DO_ESPERADO');
CREATE TYPE status_problema_enum AS ENUM ('EM_ANALISE', 'EM_RESOLUCAO', 'RESOLVIDO');
CREATE TYPE prioridade_enum AS ENUM ('ALTO', 'MEDIO', 'BAIXO');
CREATE TYPE etapas_ciclo_enum AS ENUM ('PLAN', 'DO', 'CHECK', 'ACT');

-- Empresa
CREATE TABLE IF NOT EXISTS empresa (
                                       id_empresa SERIAL PRIMARY KEY,
                                       nome VARCHAR(50) NOT NULL,
                                       setor VARCHAR(30) NOT NULL,
                                       cnpj CHAR(14) UNIQUE NOT NULL,
                                       status status_enum NOT NULL DEFAULT 'ATIVA',
                                       tamanho tamanho_empresa_enum DEFAULT 'MEDIA'
);

-- Endereço
CREATE TABLE IF NOT EXISTS endereco (
                                        id_endereco SERIAL PRIMARY KEY,
                                        rua VARCHAR(50) NOT NULL,
                                        bairro VARCHAR(30) NOT NULL,
                                        cidade VARCHAR(30) NOT NULL,
                                        estado CHAR(2) NOT NULL,
                                        cep CHAR(8) NOT NULL,
                                        numero VARCHAR(10) NOT NULL,
                                        complemento TEXT,
                                        unidade VARCHAR(30) NOT NULL,
                                        id_empresa INT NOT NULL REFERENCES empresa(id_empresa) ON DELETE CASCADE
);

-- Administrador Geral
CREATE TABLE IF NOT EXISTS administrador_geral (
                                                   id_adm_geral SERIAL PRIMARY KEY,
                                                   nome VARCHAR(50) NOT NULL,
                                                   senha VARCHAR(100) NOT NULL,
                                                   email VARCHAR(80) UNIQUE NOT NULL,
                                                   telefone CHAR(11) NOT NULL
);


-- Colaborador
CREATE TABLE IF NOT EXISTS colaborador (
                                           id_colaborador SERIAL PRIMARY KEY,
                                           nome VARCHAR(50) NOT NULL,
                                           sobrenome VARCHAR(50) NOT NULL,
                                           permissao_gestor BOOLEAN NOT NULL DEFAULT FALSE,
                                           status status_enum NOT NULL DEFAULT 'ATIVA',
                                           area VARCHAR(30) NOT NULL,
                                           cargo VARCHAR(100) NOT NULL,
                                           dt_contratacao DATE NOT NULL CHECK (dt_contratacao<=current_date),
                                           email VARCHAR(80) UNIQUE NOT NULL,
                                           senha VARCHAR(100) UNIQUE NOT NULL,
                                           telefone CHAR(11) NOT NULL,
                                           cpf CHAR(11) UNIQUE NOT NULL,
                                           id_empresa INT NOT NULL REFERENCES empresa(id_empresa) ON DELETE CASCADE
);

-- Projeto
CREATE TABLE IF NOT EXISTS ciclo (
                                     id_ciclo SERIAL PRIMARY KEY,
                                     nome VARCHAR(100) NOT NULL,
                                     descricao TEXT,
                                     etapa_atual etapas_ciclo_enum NOT NULL DEFAULT 'PLAN',
                                     dt_inicio DATE NOT NULL,
                                     dt_fim DATE,
                                     status situacao_enum NOT NULL DEFAULT 'NAO_INICIADO',
                                     criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                     id_empresa INT NOT NULL REFERENCES empresa(id_empresa) ON DELETE CASCADE,
                                     id_responsavel INT NOT NULL REFERENCES colaborador(id_colaborador)
);

-- Plano de Ação
CREATE TABLE IF NOT EXISTS plano_acao (
                                          id_plano_acao SERIAL PRIMARY KEY,
                                          nome VARCHAR(100) NOT NULL,
                                          descricao TEXT,
                                          status situacao_enum NOT NULL DEFAULT 'NAO_INICIADO',
                                          prioridade prioridade_enum NOT NULL DEFAULT 'MEDIO',
                                          id_ciclo INT NOT NULL REFERENCES ciclo(id_ciclo),
                                          id_criador INT NOT NULL REFERENCES colaborador(id_colaborador) ON DELETE RESTRICT
);

-- 5W2H
CREATE TABLE IF NOT EXISTS plano_acao5w2h (
                                              id_plano_acao_5w2h SERIAL PRIMARY KEY,
                                              what VARCHAR(50) NOT NULL,
                                              why VARCHAR(50),
                                              "where" VARCHAR(50),
                                              "when" DATE,
                                              who INT NOT NULL REFERENCES colaborador(id_colaborador) ON DELETE RESTRICT,
                                              how VARCHAR(50),
                                              how_much NUMERIC,
                                              id_plano_acao INT REFERENCES plano_acao(id_plano_acao) ON DELETE CASCADE
);
-- Meta
CREATE TABLE IF NOT EXISTS meta (
                                    id_meta SERIAL PRIMARY KEY,
                                    meta VARCHAR(100) NOT NULL,
                                    descricao_meta TEXT,
                                    objetivo VARCHAR(200),
                                    prioridade VARCHAR(15),
                                    prazo DATE NOT NULL,
                                    status status_meta_enum NOT NULL DEFAULT 'REGULAR',
                                    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                    id_ciclo INT NOT NULL REFERENCES ciclo(id_ciclo) ON DELETE CASCADE,
                                    id_plano_acao INT REFERENCES plano_acao(id_plano_acao) ON DELETE SET NULL
);

-- Tarefa
CREATE TABLE IF NOT EXISTS tarefa (
                                      id_tarefa SERIAL PRIMARY KEY,
                                      titulo VARCHAR(100) NOT NULL,
                                      descricao TEXT,
                                      prioridade prioridade_enum NOT NULL DEFAULT 'MEDIO',
                                      dt_entrega DATE,
                                      status situacao_enum NOT NULL DEFAULT 'NAO_INICIADO',
                                      dt_inicio DATE NOT NULL DEFAULT now(),
                                      id_colaborador INT NOT NULL REFERENCES colaborador(id_colaborador) ON DELETE RESTRICT,
                                      id_plano_acao  INT NOT NULL REFERENCES plano_acao(id_plano_acao) ON DELETE SET NULL
);

-- Lições Aprendidas
CREATE TABLE IF NOT EXISTS licoes_aprendidas (
                                                 id_licao SERIAL PRIMARY KEY,
                                                 titulo VARCHAR(100) NOT NULL,
                                                 area VARCHAR(30),
                                                 aprendizado TEXT NOT NULL,
                                                 categoria VARCHAR(30),
                                                 descricao TEXT,
                                                 fase_origem VARCHAR(8),
                                                 severidade VARCHAR(50),
                                                 id_ciclo INT REFERENCES ciclo(id_ciclo) ON DELETE CASCADE
);

-- Problema
CREATE TABLE IF NOT EXISTS problema (
                                        id_problema SERIAL PRIMARY KEY,
                                        titulo VARCHAR(100) NOT NULL,
                                        descricao TEXT NOT NULL,
                                        peso VARCHAR(30),
                                        solucao VARCHAR(200),
                                        status status_problema_enum NOT NULL DEFAULT 'EM_ANALISE',
                                        origem TEXT,
                                        encontrado_em DATE NOT NULL,
                                        id_ciclo INT NOT NULL REFERENCES ciclo(id_ciclo) ON DELETE CASCADE,
                                        id_plano_acao INT REFERENCES plano_acao(id_plano_acao) ON DELETE SET NULL,
                                        id_colaborador INT REFERENCES colaborador(id_colaborador) ON DELETE SET NULL
);

-- Relacionamentos N:N

CREATE TABLE IF NOT EXISTS ciclo_colaborador (
                                                 id_ciclo INT NOT NULL REFERENCES ciclo (id_ciclo) ON DELETE CASCADE,
                                                 id_colaborador INT NOT NULL REFERENCES colaborador (id_colaborador) ON DELETE CASCADE,
                                                 PRIMARY KEY (id_ciclo, id_colaborador),
                                                 papel_ciclo VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS empresa_administrador_geral (
                                                           id_empresa INT NOT NULL REFERENCES empresa (id_empresa) ON DELETE CASCADE,
                                                           id_adm_geral INT NOT NULL REFERENCES administrador_geral (id_adm_geral) ON DELETE CASCADE,
                                                           PRIMARY KEY (id_empresa, id_adm_geral)
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