# ACTA — Sistema de Gestão do PDCA

O ACTA é um projeto da organização ACTA que sistematiza a metodologia PDCA (*Plan, Do, Check, Act*) em uma aplicação web. A proposta é apoiar a organização de ciclos de melhoria contínua, reunindo o planejamento, a execução e o acompanhamento de ações e resultados.

## O que há neste repositório

- `src/main/java/br/com/acta`: código Java do servidor, organizado em modelos de dados, DAOs de acesso ao banco, Servlets, utilitários, validações, enums e exceções.
- `src/main/webapp`: páginas HTML e JSP, estilos CSS, imagens, ícones e fontes da aplicação. A pasta `crud` reúne telas de gerenciamento de empresas, endereços, colaboradores, ciclos, metas, problemas, planos de ação, tarefas e lições aprendidas.
- `src/main/resources/sql`: scripts SQL para criação do esquema e das tabelas, carga de dados e criação de views para consultas.
- `pom.xml` e `mvnw`/`mvnw.cmd`: configuração e wrapper do Maven para compilar e empacotar a aplicação como WAR.

## Linguagens e tecnologias

| Tecnologia | Uso no projeto |
| --- | --- |
| Java 21 | Regras e modelos da aplicação, acesso ao PostgreSQL por DAOs e processamento de requisições com Jakarta Servlets. |
| JSP | Geração de páginas dinâmicas no servidor. |
| HTML | Estrutura das páginas de login, cadastro e gerenciamento. |
| CSS | Estilização das telas e componentes da interface. |
| SQL (PostgreSQL) | Definição das tabelas, tipos, relacionamentos, dados de carga e views. |
| XML | Configuração do projeto Maven em `pom.xml` e da aplicação web em `web.xml`. |

O Maven gerencia as dependências, incluindo a API Jakarta Servlet e o driver PostgreSQL.

## Área restrita e operações CRUD

A área restrita reúne telas de gerenciamento com operações de cadastro, consulta, edição e exclusão (CRUD). No backend, os Servlets recebem as ações e os DAOs executam as operações no banco PostgreSQL. Esses códigos permitem que o administrador mantenha os registros usados pela aplicação, como empresas, endereços, administradores, colaboradores, ciclos PDCA, metas, problemas, planos de ação, tarefas e lições aprendidas.

As telas HTML de CRUD estão em `src/main/webapp/crud`; os endpoints e as operações correspondentes ficam em `src/main/java/br/com/acta/servlet` e `src/main/java/br/com/acta/dao`. As telas e a lógica de servidor estão presentes no repositório, embora alguns formulários HTML ainda usem ações de exemplo (`action="#"`) e possam precisar de integração com os endpoints.

## Funcionalidades implementadas

- Autenticação no backend para administrador geral e colaborador, com criação de sessão.
- Operações CRUD nos DAOs e Servlets para as principais entidades administrativas e do ciclo de melhoria.
- Registro e acompanhamento de ciclos PDCA, com etapa atual (`PLAN`, `DO`, `CHECK` ou `ACT`), datas, situação, empresa e responsável.
- Organização de metas, problemas, planos de ação e tarefas relacionados aos ciclos.
- Registro de lições aprendidas associadas aos ciclos.
- Views SQL que consolidam dados relacionados para consulta, além de restrições relacionais no esquema do banco.
- Validação de dados e tratamento de erros de duplicidade, integridade referencial e registros não encontrados.

As senhas são armazenadas com hash bcrypt e pepper configurado no ambiente. As variáveis necessárias para a conexão com o banco e para o pepper estão exemplificadas em `.env.example`.
