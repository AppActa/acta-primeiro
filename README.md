# ACTA — Sistema de Gestão do PDCA

O ACTA é um projeto da organização ACTA que transforma a metodologia PDCA (*Plan, Do, Check, Act*) em um fluxo de trabalho para organizar ciclos de melhoria contínua. A aplicação registra problemas, metas e ações para apoiar o planejamento, a execução, o acompanhamento e o aprendizado das empresas.

## Estado atual do projeto

O repositório reúne uma página institucional e uma aplicação web administrativa. O backend já se conecta ao PostgreSQL e atende telas de gerenciamento em JSP. A página inicial também apresenta planos e ideias de recursos do produto; itens como IA, dashboards, relatórios, alertas e ferramentas de análise aparecem como conteúdo visual/institucional e não devem ser considerados funcionalidades integradas ao backend atual.

## O que há neste repositório

- `src/main/java/br/com/acta`: aplicação Java, separada em modelos, DAOs (acesso a dados), Servlets (requisições web), enums, validações, utilitários e exceções.
- `src/main/webapp/index.html`: página institucional do ACTA, com apresentação do PDCA, recursos, planos, perguntas frequentes e informações sobre o projeto e a equipe.
- `src/main/webapp/login.jsp` e `src/main/webapp/crud`: login e telas JSP de gerenciamento de empresas, endereços, colaboradores, ciclos, metas, problemas, planos de ação, tarefas e lições aprendidas.
- `src/main/webapp/assets`: arquivos CSS, JavaScript, imagens, ícones e fontes usados nas páginas.
- `src/main/resources/sql`: `script_banco.sql` define o esquema, tipos e relacionamentos do banco; `script_views.sql` cria views para consultas.
- `pom.xml`, `mvnw` e `mvnw.cmd`: configuração e wrapper do Maven. O projeto é empacotado como uma aplicação web WAR.
- `.env.example`: nomes das variáveis de ambiente usadas para a conexão com o PostgreSQL e configuração do pepper de senhas.

## Tecnologias

| Tecnologia | Uso |
| --- | --- |
| Java 21 | Regras da aplicação, modelos, acesso ao banco e processamento de requisições. |
| Jakarta Servlets e JSP | Endpoints do backend e geração das telas administrativas com dados do banco. |
| HTML, CSS e JavaScript | Página institucional, estrutura e estilos das telas e interações do navegador, como filtros, ordenação e validações de formulário. |
| SQL e PostgreSQL | Persistência, relacionamentos, tipos de dados e views de consulta. |
| XML e Maven | Configuração do projeto, dependências e empacotamento WAR. |

## Área administrativa e funcionalidades implementadas

O login do backend autentica administradores gerais. Após a autenticação, os Servlets e DAOs permitem consultar, cadastrar, editar e excluir registros no PostgreSQL pelas telas de CRUD. As entidades com telas de gerenciamento são empresas, endereços, colaboradores, ciclos PDCA, metas, problemas, planos de ação, tarefas e lições aprendidas. Os registros se relacionam entre si, por exemplo, ciclos com empresas e responsáveis, e tarefas com planos de ação e colaboradores.

Também estão implementados o acompanhamento da etapa e situação dos ciclos, validações de dados, mensagens de sucesso e erro, filtros e ordenação nas listagens que oferecem esses controles, além de views SQL que apresentam dados relacionados. Senhas são armazenadas com hash bcrypt e pepper configurado por variável de ambiente.

## Limitações conhecidas

- A página de configurações do perfil e alguns formulários de recuperação de senha ainda são telas demonstrativas e usam `action="#"`; não estão conectados a operações do backend.
- Recursos citados na página institucional — como IA, dashboards, relatórios, alertas e ferramentas de análise — não têm implementação integrada identificada no código atual.
- O login conectado ao backend autentica apenas o administrador geral. Embora exista um endpoint de logout, o atalho “Sair” das telas JSP ainda aponta para `login.html`; também não foi identificada uma checagem de sessão nos endpoints CRUD.

## Compilação

Com o Java 21 instalado, o wrapper do Maven pode gerar o WAR:

```powershell
.\mvnw.cmd package
```

O artefato é gerado em `target/ACTA-1.0-SNAPSHOT.war`. Para executar a aplicação, configure as variáveis de banco indicadas em `.env.example` e faça o deploy em um servidor compatível com Jakarta Servlet. O script `src/main/resources/sql/script_banco.sql` recria o esquema, mas primeiro apaga tabelas existentes; execute-o somente em um banco novo ou descartável.
