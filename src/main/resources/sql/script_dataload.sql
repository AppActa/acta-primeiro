-- SCRIPT DATA LOAD ACTA

TRUNCATE TABLE
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
    empresa_administrador_geral
    RESTART IDENTITY CASCADE;

-- Empresa
INSERT INTO empresa (nome, setor, cnpj, tamanho)
VALUES ('Âmbar Energia', 'Elétrico', '67423601000188', 'MEDIA');

INSERT INTO empresa (nome, setor, cnpj, tamanho)
VALUES ('Friboi', 'Alimentício', '19583726000144', 'GRANDE');

INSERT INTO empresa (nome, setor, cnpj, tamanho)
VALUES ('Seara Alimentos', 'Alimentício', '48273910000156', 'GRANDE');

INSERT INTO empresa (nome, setor, cnpj, tamanho)
VALUES ('Swift', 'Alimentício', '82734510000199', 'GRANDE');

INSERT INTO empresa (nome, setor, cnpj, tamanho)
VALUES ('PicPay', 'Fintech', '35792468000110', 'GRANDE');

-- Endereço
INSERT INTO endereco (rua, bairro, cidade, estado, cep, numero, complemento, unidade, empresa_id)
VALUES ('Avenida Mutinga', 'Pirituba', 'São Paulo', 'SP', '09234000', '2300', 'Escritório 4', 'Mutinga', 1);

INSERT INTO endereco (rua, bairro, cidade, estado, cep, numero, complemento, unidade, empresa_id)
VALUES ('Rua das Palmeiras', 'Savassi', 'Belo Horizonte', 'MG', '30130170', '512', 'Sala 8', 'Savassi', 2);

INSERT INTO endereco (rua, bairro, cidade, estado, cep, numero, complemento, unidade, empresa_id)
VALUES ('Rua Machado', 'Industrial', 'Betim', 'MG', '32677000', '150', 'Galpão 2', 'Betim', 3);

INSERT INTO endereco (rua, bairro, cidade, estado, cep, numero, complemento, unidade, empresa_id)
VALUES ('Rua das Carnes', 'Veloso', 'Osasco', 'SP', '14783000', '780', 'Bloco C', 'Veloso', 4);

INSERT INTO endereco (rua, bairro, cidade, estado, cep, numero, complemento, unidade, empresa_id)
VALUES ('Rua Fidêncio Ramos', 'Vila Olímpia', 'São Paulo', 'SP', '04551010', '308', 'Andar 12', 'Vila Olímpia', 5);

-- Administrador Geral
INSERT INTO administrador_geral (nome, senha, email, telefone)
VALUES ('Roberson', 'souAdmdaAmbar1234', 'roberson.silva@ambarenergia.com.br', '11348394736');

INSERT INTO administrador_geral (nome, senha, email, telefone)
VALUES ('Fernanda', 'friboiAdm2026', 'fernanda.lopes@jbsfriboi.com.br', '31998475632');

INSERT INTO administrador_geral (nome, senha, email, telefone)
VALUES ('Patrícia', 'jbsAdmSeara25', 'patricia.gomes@jbsseara.com.br', '11976543210');

INSERT INTO administrador_geral (nome, senha, email, telefone)
VALUES ('Eduardo', 'swiftAdm2026', 'eduardo.martins@swiftbrasil.com.br', '17998765432');

INSERT INTO administrador_geral (nome, senha, email, telefone)
VALUES ('Juliana', 'picpayAdm2026', 'juliana.tavares@picpay.com.br', '11991234567');

-- Colaborador
INSERT INTO colaborador (nome, sobrenome, area, cargo, dt_contratacao, email, senha, telefone, cpf, empresa_id)
VALUES ('José', 'Amorim Martins', 'Manutenção Elétrica', 'Técnico Eletricista', '2022-03-15', 'amorimar.jose@ambarenergia.org.br', 'joseAmSenha2022', '11483058338', '98457908658', 1);

INSERT INTO colaborador (nome, sobrenome, area, cargo, dt_contratacao, email, senha, telefone, cpf, empresa_id)
VALUES ('Camila', 'Duarte Rocha', 'Segurança do Trabalho', 'Técnica de Segurança do Trabalho', '2021-07-01', 'camila.rocha@jbsfriboi.com.br', 'camilaSenha2021', '31976583421', '65498732014', 2);

INSERT INTO colaborador (nome, sobrenome, area, cargo, dt_contratacao, email, senha, telefone, cpf, empresa_id)
VALUES ('Rafael', 'Costa Almeida', 'Produção', 'Supervisor de Produção', '2020-11-20', 'rafael.almeida@jbsseara.com.br', 'rafaelSenha2020', '11987654321', '52698471033', 3);

INSERT INTO colaborador (nome, sobrenome, area, cargo, dt_contratacao, email, senha, telefone, cpf, empresa_id)
VALUES ('Larissa', 'Ferreira Souza', 'Comércio Exterior', 'Analista de Exportação', '2023-02-10', 'larissa.souza@swiftbrasil.com.br', 'larissaSenha2023', '17987651234', '74185296330', 4);

INSERT INTO colaborador (nome, sobrenome, area, cargo, dt_contratacao, email, senha, telefone, cpf, empresa_id)
VALUES ('Thiago', 'Ramos Vieira', 'Risco e Compliance', 'Analista de Prevenção a Fraudes', '2022-09-05', 'thiago.vieira@picpay.com.br', 'thiagoSenha2022', '11987651230', '15926374805', 5);

-- Ciclo
INSERT INTO ciclo (nome, dt_inicio, empresa_id, responsavel_id)
VALUES ('PDCA para algum problema', current_date, 1, 1);

INSERT INTO ciclo (nome, dt_inicio, empresa_id, responsavel_id)
VALUES ('PDCA redução de acidentes de trabalho na planta industrial', '2026-09-19', 2, 2);

INSERT INTO ciclo (nome, dt_inicio, empresa_id, responsavel_id)
VALUES ('PDCA redução de desperdício na linha de produção', '2026-09-21', 3, 3);

INSERT INTO ciclo (nome, dt_inicio, empresa_id, responsavel_id)
VALUES ('PDCA melhoria no processo de exportação de carne bovina', '2026-09-23', 4, 4);

INSERT INTO ciclo (nome, dt_inicio, empresa_id, responsavel_id)
VALUES ('PDCA redução de fraudes em transações', '2026-09-18', 5, 5);

-- Plano de ação
INSERT INTO plano_acao (nome, descricao, ciclo_id, criador_id)
VALUES ('Revisão de tensão', 'Identificar causas da oscilação de tensão na rede', 1, 1);

INSERT INTO plano_acao (nome, descricao, ciclo_id, criador_id)
VALUES ('Reforço de EPI', 'Reforçar uso obrigatório de equipamentos de proteção', 2, 2);

INSERT INTO plano_acao (nome, descricao, ciclo_id, criador_id)
VALUES ('Otimização de linha', 'Reduzir desperdício de matéria-prima na produção', 3, 3);

INSERT INTO plano_acao (nome, descricao, ciclo_id, criador_id)
VALUES ('Otimização documental', 'Agilizar emissão de documentos de exportação', 4, 4);

INSERT INTO plano_acao (nome, descricao, ciclo_id, criador_id)
VALUES ('Reforço antifraude', 'Implementar novas regras de detecção de fraude em tempo real', 5, 5);

-- 5W2H
INSERT INTO plano_acao5w2h (what, why, "where", "when", who, how, how_much, plano_acao_id)
VALUES ('Inspecionar transformadores', 'Reduzir oscilação de tensão', 'Subestação Mutinga', '2026-08-20', 1, 'Inspeção visual e testes de carga', 3200.00, 1);

INSERT INTO plano_acao5w2h (what, why, "where", "when", who, how, how_much, plano_acao_id)
VALUES ('Auditar uso de EPI', 'Reduzir número de acidentes na planta', 'Linha de abate', '2026-08-25', 2, 'Vistoria diária com checklist', 1500.00, 2);

INSERT INTO plano_acao5w2h (what, why, "where", "when", who, how, how_much, plano_acao_id)
VALUES ('Revisar processo de corte', 'Reduzir desperdício de matéria-prima', 'Linha de produção 2', '2026-08-24', 3, 'Treinamento e ajuste de máquinas', 2800.00, 3);

INSERT INTO plano_acao5w2h (what, why, "where", "when", who, how, how_much, plano_acao_id)
VALUES ('Digitalizar documentação de exportação', 'Reduzir tempo de liberação alfandegária', 'Setor de Exportação', '2026-08-22', 4, 'Implantação de sistema digital de documentos', 4500.00, 4);

INSERT INTO plano_acao5w2h (what, why, "where", "when", who, how, how_much, plano_acao_id)
VALUES ('Implementar modelo de scoring antifraude', 'Reduzir transações fraudulentas', 'Time de Risco e Compliance', '2026-08-21', 5, 'Machine learning aplicado a padrões de transação', 12000.00, 5);

-- Meta
INSERT INTO meta (meta, descricao_meta, objetivo, prazo, ciclo_id)
VALUES ('tensão da energia', 'blablablabla', 'Controlar a oscilação de tensão', '2026-11-30', 1);

INSERT INTO meta (meta, descricao_meta, objetivo, prazo, ciclo_id)
VALUES ('acidentes de trabalho', 'redução de incidentes registrados na operação', 'Zerar acidentes graves até o fim do ano', '2026-12-20', 2);

INSERT INTO meta (meta, descricao_meta, objetivo, prazo, ciclo_id)
VALUES ('desperdício de matéria-prima', 'redução de perdas na linha de produção', 'Reduzir desperdício em 20%', '2026-12-10', 3);

INSERT INTO meta (meta, descricao_meta, objetivo, prazo, ciclo_id)
VALUES ('tempo de processamento de exportação', 'redução do tempo entre abate e liberação para exportação', 'Reduzir tempo de processamento em 25%', '2026-12-15', 4);

INSERT INTO meta (meta, descricao_meta, objetivo, prazo, ciclo_id)
VALUES ('índice de fraudes em transações', 'redução de transações fraudulentas identificadas na plataforma', 'Reduzir fraudes em 30%', '2026-12-30', 5);

-- Tarefa
INSERT INTO tarefa (titulo, descricao, dt_entrega, colaborador_id, plano_acao_id)
VALUES ('Inspecionar transformador', 'Verificar estado dos transformadores da subestação', '2026-09-22', 1, 1);

INSERT INTO tarefa (titulo, descricao, dt_entrega, colaborador_id, plano_acao_id)
VALUES ('Vistoriar EPI', 'Checar uso correto de EPI pelos operadores da linha de abate', '2026-09-19', 2, 2);

INSERT INTO tarefa (titulo, descricao, dt_entrega, colaborador_id, plano_acao_id)
VALUES ('Revisar linha 2', 'Ajustar máquinas de corte para reduzir desperdício', '2026-09-20', 3, 3);

INSERT INTO tarefa (titulo, descricao, dt_entrega, colaborador_id, plano_acao_id)
VALUES ('Digitalizar documentos', 'Implementar sistema de digitalização de documentos de exportação', '2026-09-25', 4, 4);

INSERT INTO tarefa (titulo, descricao, dt_entrega, colaborador_id, plano_acao_id)
VALUES ('Implementar scoring antifraude', 'Desenvolver e testar modelo de machine learning para detecção de fraudes', '2026-10-05', 5, 5);

-- Lições Aprendidas
INSERT INTO licoes_aprendidas (titulo, area, aprendizado, categoria, descricao, fase_origem, severidade, ciclo_id)
VALUES ('Falha de monitoramento', 'Elétrica', 'Necessário monitoramento contínuo de tensão', 'Processo', 'Oscilações não foram detectadas a tempo', 'DO', 'Alta', 1);

INSERT INTO licoes_aprendidas (titulo, area, aprendizado, categoria, descricao, fase_origem, severidade, ciclo_id)
VALUES ('Uso irregular de EPI', 'Segurança', 'Fiscalização precisa ser diária e não semanal', 'Segurança do Trabalho', 'Vistorias semanais não identificaram uso incorreto a tempo', 'PLAN', 'Alta', 2);

INSERT INTO licoes_aprendidas (titulo, area, aprendizado, categoria, descricao, fase_origem, severidade, ciclo_id)
VALUES ('Desperdício por má calibração', 'Produção', 'Necessário calibrar máquinas semanalmente', 'Processo', 'Máquinas descalibradas geravam perdas de matéria-prima', 'CHECK', 'Média', 3);

INSERT INTO licoes_aprendidas (titulo, area, aprendizado, categoria, descricao, fase_origem, severidade, ciclo_id)
VALUES ('Atraso em documentação', 'Exportação', 'Processos manuais geravam atrasos na liberação', 'Processo', 'Documentos físicos causavam gargalos na exportação', 'PLAN', 'Média', 4);

INSERT INTO licoes_aprendidas (titulo, area, aprendizado, categoria, descricao, fase_origem, severidade, ciclo_id)
VALUES ('Falha na detecção precoce', 'Risco e Compliance', 'Modelo antigo não identificava novos padrões de fraude', 'Tecnologia', 'Regras estáticas não acompanhavam evolução das fraudes', 'CHECK', 'Alta', 5);

-- Problema
INSERT INTO problema (titulo, descricao, solucao, status, origem, encontrado_em, ciclo_id, plano_acao_id, colaborador_id)
VALUES ('Oscilação de tensão na rede', 'Consumidores reportaram variações de tensão frequentes', 'Substituição de transformador defeituoso', 'EM_ANALISE', 'Reclamação de clientes', '2026-08-25', 1, 1, 1);

INSERT INTO problema (titulo, descricao, solucao, status, origem, encontrado_em, ciclo_id, plano_acao_id, colaborador_id)
VALUES ('Aumento de quase-acidentes na planta industrial', 'Registro elevado de quase-acidentes na linha de abate', 'Reforço de treinamento e fiscalização de EPI', 'EM_RESOLUCAO', 'Relatório interno de segurança', '2026-08-28', 2, 2, 2);

INSERT INTO problema (titulo, descricao, solucao, status, origem, encontrado_em, ciclo_id, plano_acao_id, colaborador_id)
VALUES ('Alto desperdício de matéria-prima', 'Linha de produção 2 apresenta perdas acima do esperado', 'Calibração e treinamento da equipe', 'EM_RESOLUCAO', 'Auditoria interna de produção', '2026-08-20', 3, 3, 3);

INSERT INTO problema (titulo, descricao, solucao, status, origem, encontrado_em, ciclo_id, plano_acao_id, colaborador_id)
VALUES ('Atrasos na liberação de exportação', 'Processo documental manual gera atrasos frequentes', 'Implantação de sistema digital', 'EM_RESOLUCAO', 'Auditoria de processos', '2026-08-18', 4, 4, 4);

INSERT INTO problema (titulo, descricao, solucao, status, origem, encontrado_em, ciclo_id, plano_acao_id, colaborador_id)
VALUES ('Aumento de transações fraudulentas', 'Crescimento no número de fraudes não identificadas pelo sistema atual', 'Implementação de modelo de machine learning antifraude', 'EM_ANALISE', 'Relatório de risco e compliance', '2026-08-15', 5, 5, 5);

-- Ciclo_Colaborador
INSERT INTO ciclo_colaborador (ciclo_id, colaborador_id)
VALUES (1, 1);

INSERT INTO ciclo_colaborador (ciclo_id, colaborador_id)
VALUES (2, 2);

INSERT INTO ciclo_colaborador (ciclo_id, colaborador_id)
VALUES (3, 3);

INSERT INTO ciclo_colaborador (ciclo_id, colaborador_id)
VALUES (4, 4);

INSERT INTO ciclo_colaborador (ciclo_id, colaborador_id)
VALUES (5, 5);

-- Empresa_Administrador_Geral
INSERT INTO empresa_administrador_geral (empresa_id, adm_geral_id)
VALUES (1, 1);

INSERT INTO empresa_administrador_geral (empresa_id, adm_geral_id)
VALUES (2, 2);

INSERT INTO empresa_administrador_geral (empresa_id, adm_geral_id)
VALUES (3, 3);

INSERT INTO empresa_administrador_geral (empresa_id, adm_geral_id)
VALUES (4, 4);

INSERT INTO empresa_administrador_geral (empresa_id, adm_geral_id)
VALUES (5, 5);