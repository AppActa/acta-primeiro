-- Preparacao do banco existente para a V2 de autenticacao e perfil.
-- Executar antes de publicar a V2 no servidor Java.
-- Preserva os registros e pode ser executado novamente.
BEGIN;

ALTER TABLE colaborador ADD COLUMN IF NOT EXISTS imagem TEXT;

CREATE OR REPLACE VIEW vw_colaborador AS
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
    emp.nome AS nome_empresa,
    col.id_empresa,
    col.imagem
FROM colaborador col
JOIN empresa emp ON col.id_empresa = emp.id_empresa;

COMMIT;
