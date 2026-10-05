-- VIEWS ACTA

DROP VIEW IF EXISTS
    vw_meta,
    vw_problema,
    vw_plano_acao,
    vw_ciclo,
    vw_tarefa,
    vw_licoes_aprendidas,
    vw_endereco,
    vw_empresa,
    vw_colaborador,
    vw_plano_acao_5w2h,
    vw_ciclo_colaborador,
    vw_empresa_administrador_geral;

-- View para meta
CREATE OR REPLACE VIEW vw_meta AS(
SELECT
    m.id_meta,
    m.meta,
    m.descricao_meta,
    m.objetivo,
    m.prioridade,
    m.prazo,
    m.status,
    m.criado_em,
    c.nome AS nome_ciclo,
    pa.nome AS nome_plano_acao
FROM meta m
LEFT JOIN ciclo c ON m.id_ciclo = c.id_ciclo
LEFT JOIN plano_acao pa ON m.id_plano_acao = pa.id_plano_acao
);

-- View para problema
CREATE OR REPLACE VIEW vw_problema AS(
SELECT
    p.id_problema,
    p.titulo,
    p.descricao,
    p.peso,
    p.solucao,
    p.status,
    p.origem,
    p.encontrado_em,
    c.nome AS nome_ciclo,
    pa.nome AS nome_plano_acao,
    col.nome || ' ' || col.sobrenome AS nome_colaborador
FROM problema p
LEFT JOIN ciclo c ON p.id_ciclo = c.id_ciclo
LEFT JOIN plano_acao pa ON p.id_plano_acao = pa.id_plano_acao
LEFT JOIN colaborador col ON p.id_colaborador = col.id_colaborador
);

-- View para plano de ação
CREATE OR REPLACE VIEW vw_plano_acao AS(
SELECT
    pa.id_plano_acao,
    pa.nome,
    pa.descricao,
    pa.status,
    pa.prioridade,
    c.nome AS nome_ciclo,
    col.nome || ' ' || col.sobrenome AS nome_colaborador
FROM plano_acao pa
JOIN ciclo c ON pa.id_ciclo = c.id_ciclo
JOIN colaborador col ON pa.id_criador = col.id_colaborador
);

-- View para ciclo
CREATE OR REPLACE VIEW vw_ciclo AS(
SELECT
    c.id_ciclo,
    c.nome,
    c.descricao,
    c.etapa_atual,
    c.dt_inicio,
    c.dt_fim,
    c.status,
    c.criado_em,
    emp.nome AS nome_empresa,
    col.nome || ' ' || col.sobrenome AS nome_colaborador
FROM ciclo c
JOIN empresa emp ON c.id_empresa = emp.id_empresa
JOIN colaborador col ON c.id_responsavel = col.id_colaborador
);

-- View para tarefa
CREATE OR REPLACE VIEW vw_tarefa AS(
SELECT
    t.id_tarefa,
    t.titulo,
    t.descricao,
    t.prioridade,
    t.dt_entrega,
    t.status,
    t.dt_inicio,
    col.nome || ' ' || col.sobrenome AS nome_colaborador,
    pa.nome AS nome_plano_acao
FROM tarefa t
JOIN colaborador col ON t.id_colaborador = col.id_colaborador
JOIN plano_acao pa ON t.id_plano_acao = pa.id_plano_acao
);

-- View para lições aprendidas
CREATE OR REPLACE VIEW vw_licoes_aprendidas AS(
SELECT
    l.id_licao,
    l.titulo,
    l.area,
    l.aprendizado,
    l.categoria,
    l.descricao,
    l.fase_origem,
    l.severidade,
    c.nome AS nome_ciclo
FROM licoes_aprendidas l
JOIN ciclo c ON l.id_ciclo = c.id_ciclo
);

-- View para endereço
CREATE OR REPLACE VIEW vw_endereco AS(
SELECT
    e.id_endereco,
    e.rua,
    e.bairro,
    e.cidade,
    e.estado,
    e.cep,
    e.numero,
    e.complemento,
    e.unidade,
    emp.nome AS nome_empresa
FROM endereco e
JOIN empresa emp ON e.id_empresa = emp.id_empresa
);

-- View para empresa
CREATE OR REPLACE VIEW vw_empresa AS(
SELECT
    e.id_empresa,
    e.nome,
    e.setor,
    e.cnpj,
    e.status,
    e.tamanho
FROM empresa e
);


-- View para colaborador
CREATE OR REPLACE VIEW vw_colaborador AS(
SELECT
    col.id_colaborador,
    col.nome,
    col.sobrenome,
    col.permissao_gestor,
    col.status,
    col.area,
    col.cargo,
    col.dt_contratacao,
    col.email,
    col.telefone,
    col.cpf,
    emp.nome AS nome_empresa
FROM colaborador col
JOIN empresa emp ON col.id_empresa = emp.id_empresa
);

-- View para plano de ação 5W2H
CREATE OR REPLACE VIEW vw_plano_acao_5w2h AS(
SELECT
    p5.id_plano_acao_5w2h,
    p5.what,
    p5.why,
    p5."where",
    p5."when",
    p5.who AS id_responsavel,
    col.nome || ' ' || col.sobrenome AS nome_responsavel,
    p5.how,
    p5.how_much,
    pa.nome AS nome_plano_acao
FROM plano_acao5w2h p5
JOIN plano_acao pa ON p5.id_plano_acao = pa.id_plano_acao
JOIN colaborador col on col.id_colaborador = p5.who
                                                );

-- View para ciclo_colaborador
CREATE OR REPLACE VIEW vw_ciclo_colaborador AS(
SELECT
    cc.papel_ciclo,
    c.nome AS nome_ciclo,
    col.nome || ' ' || col.sobrenome AS nome_colaborador
FROM ciclo_colaborador cc
JOIN ciclo c ON cc.id_ciclo = c.id_ciclo
JOIN colaborador col ON cc.id_colaborador = col.id_colaborador
);

-- View para empresa_administrador_geral
CREATE OR REPLACE VIEW vw_empresa_administrador_geral AS(
SELECT
    emp.nome AS nome_empresa,
    adm.nome AS nome_administrador
FROM empresa_administrador_geral eag
JOIN empresa emp ON eag.id_empresa = emp.id_empresa
JOIN administrador_geral adm ON eag.id_adm_geral = adm.id_adm_geral
);