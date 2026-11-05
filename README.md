# 🌐 ACTA Web

Plataforma web do ACTA para organizar ciclos PDCA, gerenciar planos de ação e acompanhar processos de melhoria contínua.

**[Acessar o site](https://acta-web.onrender.com/)** · **[Acesso pelo GitHub Pages](https://appacta.github.io/acta-primeiro/)**

## 📌 Visão geral

O ACTA Web reúne o cadastro de empresas e colaboradores, o gerenciamento de ciclos de melhoria e a organização de problemas, metas, planos de ação, tarefas e lições aprendidas.

A metodologia **PDCA — Plan, Do, Check, Act** orienta a estrutura do projeto. A aplicação utiliza Java, Servlets, JSP e PostgreSQL, com interfaces para administradores gerais e colaboradores.

Este repositório contém a aplicação web do primeiro ano e sua página institucional. Os demais componentes da comunidade ACTA estão relacionados ao final deste documento.

## ✨ Funcionalidades

| Área | Capacidades |
| --- | --- |
| Cadastro e acesso | Cadastro público de colaboradores e empresas; autenticação de administradores gerais e colaboradores ativos. |
| Perfil | Consulta e edição de dados pessoais, alteração de senha e envio de foto de perfil pelo Cloudinary. |
| Empresas | Cadastro, consulta, edição e exclusão de empresas, endereços e colaboradores na área administrativa. |
| Ciclos PDCA | Organização de ciclos, associação de responsáveis e acompanhamento de etapas e situações. |
| Planejamento e execução | Gerenciamento de problemas, metas, planos de ação e tarefas. |
| Aprendizado | Registro e consulta de lições aprendidas associadas aos ciclos. |
| Listagens | Pesquisa, filtros e ordenação nas telas que disponibilizam esses controles. |
| Navegação | Página inicial adaptável a diferentes telas, acesso ao perfil e confirmação antes de sair da conta. |

### Perfis de acesso

| Perfil | Acesso no ACTA Web |
| --- | --- |
| Visitante | Página institucional, login e cadastro público. |
| Colaborador | Próprio perfil, dados da empresa vinculada, alteração dos dados pessoais, senha e foto. |
| Administrador geral | Próprio perfil e área administrativa para gerenciamento dos registros. |

O cadastro público cria uma conta de **colaborador**. O acesso administrativo utiliza uma conta de administrador geral disponibilizada pela equipe responsável pelo ambiente.

## 🧭 Como usar o site

### 1. Acessar o ACTA

Abra **[acta-web.onrender.com](https://acta-web.onrender.com/)**. A página inicial apresenta o projeto, a metodologia PDCA, os planos e a equipe.

O endereço do GitHub Pages redireciona para esse mesmo site. Se o serviço estiver iniciando, aguarde o carregamento da página.

### 2. Criar uma conta de colaborador

1. Na página inicial, clique em **Cadastrar-se**. Essa opção também está disponível na tela de login.
2. Preencha as etapas do formulário com seus dados pessoais e profissionais.
3. Informe uma senha e selecione a empresa à qual você está vinculado.
4. Se precisar cadastrar uma empresa, use a opção oferecida pelo formulário e preencha os dados da empresa e do endereço. Depois, conclua o cadastro do colaborador.
5. Revise os campos e confirme o cadastro. Quando a autenticação automática for concluída, você voltará à página inicial com a conta conectada; se for direcionado ao login, entre com as credenciais cadastradas.

### 3. Entrar em uma conta existente

1. Clique em **Entrar**, no topo da página inicial.
2. Informe seu e-mail e sua senha.
3. Confirme o login. O sistema identifica o perfil da conta e apresenta os acessos correspondentes.

Depois de entrar, clique no seu nome ou na sua foto no topo da página para abrir **Meu perfil**.

### 4. Editar dados e senha

Em **Meu perfil**, clique em **Editar dados**, atualize os campos disponíveis e salve as alterações.

Para trocar a senha, clique em **Alterar senha**, informe a senha atual, digite a nova senha e confirme-a antes de salvar.

### 5. Trocar a foto

1. Em **Meu perfil**, clique em **Alterar foto**.
2. Selecione uma imagem **PNG, JPEG ou WebP de até 5 MB**.
3. Aguarde a mensagem de envio concluído.
4. Clique em **Salvar foto** para gravar a imagem no seu perfil.

Se a sessão tiver expirado, atualize a página e entre novamente antes de repetir o envio. O ambiente de hospedagem precisa estar configurado com as credenciais do Cloudinary para disponibilizar esse recurso.

### 6. Consultar a empresa ou abrir a área administrativa

**Colaborador:** no perfil, clique na empresa vinculada para consultar suas informações e endereços.

**Administrador geral:** clique em **Área administrativa** no perfil ou no atalho administrativo da página inicial. Utilize o menu para acessar empresas, endereços, colaboradores, ciclos, problemas, metas, planos de ação, tarefas e lições aprendidas.

Nas telas de gerenciamento, use os controles de cadastro, consulta, edição e exclusão. As listagens disponibilizam filtros e ordenação conforme a entidade. Ao preencher registros relacionados, selecione os dados já cadastrados, como empresa, responsável, ciclo ou plano de ação.

### 7. Sair da conta

No perfil, clique em **Sair**. Na janela de confirmação, escolha **Confirmar saída** para encerrar a sessão ou **Cancelar** para continuar conectado.

## 🛠️ Tecnologias

| Tecnologia | Uso |
| --- | --- |
| Java 21 | Modelos, regras da aplicação e processamento das requisições. |
| Jakarta Servlet 6.1 e JSP | Controle de requisições e geração das páginas com dados do servidor. |
| HTML, CSS e JavaScript | Interface, estilos, formulários e interações no navegador. |
| PostgreSQL e JDBC | Persistência, relacionamentos e acesso aos dados. |
| Maven | Dependências, testes e geração do pacote WAR. |
| Apache Tomcat 11 | Execução da aplicação web. |
| Cloudinary | Upload e disponibilização das fotos de perfil. |
| Docker e Render | Empacotamento e hospedagem da aplicação. |
| GitHub Actions e Pages | Publicação do endereço de acesso pelo GitHub Pages. |

As dependências e versões estão declaradas em [`pom.xml`](pom.xml).

## Segurança e senhas

O `LoginFilter`, no pacote `br.com.acta.servlet`, protege os CRUDs, perfis, consulta da própria empresa e assinatura de upload, incluindo encaminhamentos para as views em `WEB-INF`. O acesso administrativo exige `usuarioLogado` na sessão do servidor, do tipo `AdministradorGeral`, com ID positivo. Colaboradores ativos podem acessar suas rotas pessoais, logout e assinatura de upload. Parâmetros de URL e formulário não concedem permissões. Sem sessão, navegações GET/HEAD são redirecionadas para `/login`; outras operações recebem HTTP 401. Perfis sem autorização recebem HTTP 403. O filtro também protege encaminhamentos e inclusões internos e envia `Cache-Control: no-store`.

Novos endpoints administrativos devem ser incluídos no mapeamento do filtro. A tela de login é acessada por `/login`, com autenticação em `/login-servlet`. Contas administrativas são pré-cadastradas; `/administrador-geral-servlet` permite alterar a própria conta autenticada, sem cadastro público de administradores.

Novas senhas usam o formato `{acta-v2}`: HMAC-SHA384 com o pepper, codificado em Base64 antes do BCrypt. Isso preserva toda a senha dentro do limite de 100 caracteres da aplicação. Hashes BCrypt antigos continuam sendo verificados pelo algoritmo original, sem bloquear contas pelo tamanho de `pepper + senha`. Esses hashes mantêm a limitação original de 72 bytes UTF-8 do BCrypt; para eliminá-la em uma conta existente, altere a senha pelo fluxo autenticado, que grava o formato v2. Não substitua o pepper existente durante a atualização nem regrave hashes antigos sem conhecer a senha.

## ✅ Pré-requisitos e configuração

Para executar uma instalação própria, prepare JDK 21, PostgreSQL e um servidor compatível com Jakarta Servlet 6.1, como o Tomcat 11. O projeto também inclui um [`Dockerfile`](Dockerfile) para publicação em contêiner.

Use [`.env.example`](.env.example) como referência para configurar as variáveis do ambiente:

| Variável | Finalidade |
| --- | --- |
| `DB_URL` | URL JDBC do PostgreSQL. |
| `DB_USER` | Usuário do banco. |
| `DB_PASSWORD` | Senha do usuário do banco. |
| `PASSWORD_PEPPER` | Segredo utilizado na geração e verificação dos hashes de senha. Deve permanecer consistente para as contas existentes. |
| `CLOUDINARY_CLOUD_NAME` | Identificação do ambiente de imagens. |
| `CLOUDINARY_API_KEY` | Chave de acesso ao serviço de imagens. |
| `CLOUDINARY_API_SECRET` | Segredo utilizado pelo servidor para assinar os uploads. |

A aplicação lê as variáveis do ambiente e, em desenvolvimento, pode utilizar um `.env` local. Credenciais reais, arquivos de senhas e cargas com dados pessoais devem permanecer fora do GitHub.

### Preparar o banco

Os scripts estão em [`src/main/resources/sql`](src/main/resources/sql):

- [`script_banco.sql`](src/main/resources/sql/script_banco.sql): estrutura das tabelas, tipos e restrições.
- [`script_views.sql`](src/main/resources/sql/script_views.sql): consultas que relacionam os dados utilizados nas telas.

**O `script_banco.sql` remove tabelas existentes antes de recriá-las. Execute-o apenas em um banco novo ou com backup adequado.** Em instalações existentes, confira a compatibilidade do esquema e das views antes da atualização; a migração antiga de perfil foi removida deste repositório.

## 🚀 Compilação e execução

Clone o repositório:

```bash
git clone https://github.com/AppActa/acta-primeiro.git
cd acta-primeiro
```

Com o JDK 21 instalado, gere o pacote pelo Maven Wrapper.

**Windows:**

```powershell
.\mvnw.cmd clean package
```

**Linux ou macOS:**

```bash
./mvnw clean package
```

Use `clean package` para remover saídas anteriores e impedir que páginas e recursos excluídos permaneçam no WAR. A carga local `sql/dataload.sql` e o `.env` são excluídos do empacotamento.

O arquivo será gerado em `target/ACTA-1.0-SNAPSHOT.war`. Publique-o no Tomcat com as variáveis do ambiente configuradas. O caminho de acesso dependerá do contexto atribuído ao WAR.

Use HTTPS ao testar os fluxos autenticados: a configuração da aplicação utiliza cookies de sessão com o atributo `Secure`.

## 🧪 Validação

O diretório `src/test` foi removido por decisão do responsável pelo projeto. O comando de compilação acima não comprova a execução de testes automatizados. Validações em banco de produção devem ser somente de leitura.

## 🏗️ Arquitetura e estrutura

As requisições são processadas pelos Servlets, os DAOs executam as operações no PostgreSQL e os modelos representam as entidades do sistema. As páginas JSP apresentam os dados e os formulários no navegador. As views ficam em `WEB-INF/views` e não são abertas diretamente pelo navegador; o `InicioServlet` atende a raiz e `/inicio`.

```text
src/main/
├── java/br/com/acta/
│   ├── dao/          # Acesso ao banco de dados
│   ├── enums/        # Categorias e valores do domínio
│   ├── excecoes/     # Exceções da aplicação
│   ├── model/        # Entidades e relacionamentos
│   ├── servlet/      # Requisições, autenticação e perfis
│   └── utils/        # Configuração, validações e segurança
├── resources/sql/    # Estrutura e views do banco
└── webapp/
    ├── assets/       # CSS e JavaScript organizados por funcionalidade
    └── WEB-INF/
        ├── web.xml   # Configuração e páginas de erro HTTP
        ├── includes/ # Trechos compartilhados
        └── views/    # JSPs acessadas pelos controladores
            ├── autenticacao/
            ├── crud/
            ├── empresa/
            ├── erros/
            ├── perfis/
            └── index.jsp
```

## ☁️ Publicação

| Serviço | Responsabilidade |
| --- | --- |
| Render | Executa a aplicação Java no Tomcat, incluindo a página inicial, login, perfis e área administrativa. |
| PostgreSQL | Armazena os dados da aplicação. |
| Cloudinary | Armazena as imagens de perfil enviadas pelos usuários. |
| GitHub Pages | Disponibiliza um endereço que redireciona para o site no Render. |

O [`render.yaml`](render.yaml) configura o serviço a partir da branch `main`. Cadastre as variáveis necessárias no ambiente do Render, incluindo as três variáveis do Cloudinary para permitir a troca de fotos. A infraestrutura deve permitir a conexão entre o servidor e o PostgreSQL e manter o Tomcat atualizado.

O workflow [Publicar página institucional](.github/workflows/pages.yml) usa a variável de repositório `ACTA_RENDER_URL` como destino do redirecionamento. Configure essa variável com a URL pública do Render e selecione **GitHub Actions** como fonte em **Settings → Pages**. O fluxo roda após mudanças na página inicial ou no próprio workflow na `main` e também pode ser executado manualmente.

O GitHub Pages não executa JSP: a aplicação é atendida pelo servidor Java no Render.

## 🤝 Comunidade ACTA

O ACTA é um projeto acadêmico desenvolvido em equipe. Suas frentes de aplicação, serviços, dados e documentação estão reunidas na organização [AppActa](https://github.com/AppActa).

### 📦 Repositórios do projeto

- [acta-primeiro](https://github.com/AppActa/acta-primeiro)
- [acta-mobile](https://github.com/AppActa/acta-mobile)
- [acta-ts-web](https://github.com/AppActa/acta-ts-web)
- [acta-platform](https://github.com/AppActa/acta-platform)
- [acta-pg-api](https://github.com/AppActa/acta-pg-api)
- [acta-mongo-api](https://github.com/AppActa/acta-mongo-api)
- [acta-auth-api](https://github.com/AppActa/acta-auth-api)
- [acta-fcm-api](https://github.com/AppActa/acta-fcm-api)
- [acta-import-api](https://github.com/AppActa/acta-import-api)
- [acta-database](https://github.com/AppActa/acta-database)
- [acta-neo4j](https://github.com/AppActa/acta-neo4j)
- [acta-bi](https://github.com/AppActa/acta-bi)
- [acta-ai](https://github.com/AppActa/acta-ai)
- [acta-mcp](https://github.com/AppActa/acta-mcp)
- [acta-engenharia](https://github.com/AppActa/acta-engenharia)
- [acta-template-repository](https://github.com/AppActa/acta-template-repository)
- [.github](https://github.com/AppActa/.github)

## 📄 Licença

Este repositório utiliza a licença MIT. Consulte o arquivo [LICENSE](LICENSE) para os termos de uso e distribuição.
