-- Adiciona a URL da imagem do administrador sem apagar os dados existentes.
ALTER TABLE administrador_geral
    ADD COLUMN IF NOT EXISTS imagem TEXT;
