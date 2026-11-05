# ACTA — Sistema de Gestão do PDCA

O ACTA é um projeto da organização ACTA que transforma a metodologia PDCA (*Plan, Do, Check, Act*) em um fluxo de trabalho para organizar ciclos de melhoria contínua. A aplicação registra problemas, metas e ações para apoiar o planejamento, a execução, o acompanhamento e o aprendizado das empresas.

## Estado atual do projeto

O repositório reúne uma página institucional e uma aplicação web administrativa. O backend já se conecta ao PostgreSQL e atende telas de gerenciamento em JSP. A página inicial também apresenta planos e ideias de recursos do produto; itens como IA, dashboards, relatórios, alertas e ferramentas de análise aparecem como conteúdo visual/institucional e não devem ser considerados funcionalidades integradas ao backend atual.

## O que há neste repositório

- `src/main/java/br/com/acta`: aplicação Java, separada em modelos, DAOs (acesso a dados), Servlets (requisições web), enums, validações, utilitários e exceções.
- `src/main/webapp/WEB-INF/views/index.jsp`: página institucional do ACTA, acessada pelo `InicioServlet` na raiz ou em `/inicio`.
- `src/main/webapp/WEB-INF/views`: JSPs de autenticação, CRUDs, perfis, empresa e erros, organizadas por assunto e acessadas pelos controladores. O navegador não abre esses arquivos diretamente.
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

O login do backend autentica administradores gerais e colaboradores ativos. O acesso aos CRUDs exige administrador geral. Após a autenticação administrativa, os Servlets e DAOs permitem consultar, cadastrar, editar e excluir registros no PostgreSQL pelas telas de CRUD. As entidades com telas de gerenciamento são empresas, endereços, colaboradores, ciclos PDCA, metas, problemas, planos de ação, tarefas e lições aprendidas. Os registros se relacionam entre si, por exemplo, ciclos com empresas e responsáveis, e tarefas com planos de ação e colaboradores.

Também estão implementados o acompanhamento da etapa e situação dos ciclos, validações de dados, mensagens de sucesso e erro, filtros e ordenação nas listagens que oferecem esses controles, além de views SQL que apresentam dados relacionados. Senhas são armazenadas com hash bcrypt e pepper configurado por variável de ambiente.

O `LoginFilter`, no pacote `br.com.acta.servlet`, protege os CRUDs, perfis, consulta da própria empresa e assinatura de upload, incluindo encaminhamentos para as views em `WEB-INF`. O acesso administrativo exige `usuarioLogado` na sessão do servidor, do tipo `AdministradorGeral`, com ID positivo. Colaboradores ativos podem acessar suas rotas pessoais, logout e assinatura de upload. Parâmetros de URL e formulário não concedem permissões. Sem sessão, navegações GET/HEAD são redirecionadas para `/login`; outras operações recebem HTTP 401. Perfis sem autorização recebem HTTP 403. O filtro também protege encaminhamentos e inclusões internos e envia `Cache-Control: no-store`.

Novos endpoints administrativos devem ser incluídos no mapeamento do filtro. A tela de login é acessada por `/login`, com autenticação em `/login-servlet`. Contas administrativas são pré-cadastradas; `/administrador-geral-servlet` permite alterar a própria conta autenticada, sem cadastro público de administradores.

O diretório `src/test` foi removido por decisão do responsável pelo projeto. O comando de compilação abaixo não comprova a execução de testes automatizados. Validações em banco de produção devem ser somente de leitura.

## Compilação

Com o Java 21 instalado, o wrapper do Maven pode gerar o WAR:

```powershell
.\mvnw.cmd clean package
```

Use `clean package` ao preparar a entrega: ele remove saídas de compilações anteriores para que páginas e recursos excluídos não permaneçam no WAR.

Novas senhas usam o formato `{acta-v2}`: HMAC-SHA384 com o pepper, codificado em Base64 antes do BCrypt. Isso preserva toda a senha dentro do limite de 100 caracteres da aplicação. Hashes BCrypt antigos continuam aceitos quando `pepper + senha` ocupa menos de 72 bytes UTF-8. Entradas antigas no limite ou acima dele precisam de redefinição por um responsável: o hash truncado não permite recuperar ou confirmar o sufixo original. Não substitua o pepper existente durante a atualização nem regrave hashes antigos sem conhecer a senha.

O artefato é gerado em `target/ACTA-1.0-SNAPSHOT.war`. Para executar a aplicação, configure as variáveis de banco indicadas em `.env.example` e faça o deploy em um servidor compatível com Jakarta Servlet. O script `src/main/resources/sql/script_banco.sql` recria o esquema, mas primeiro apaga tabelas existentes; execute-o somente em um banco novo ou descartável.

## Publicação de primeira versão

A publicação usa dois serviços porque o GitHub Pages serve somente arquivos estáticos, enquanto login, JSP, Servlets e acesso ao PostgreSQL precisam de um servidor Java:

- **Render:** executa o WAR no Tomcat 11. O `Dockerfile` compila o projeto com Java 21 e o `render.yaml` configura o serviço para a branch `main`.
- **GitHub Pages:** publica somente `index.html` e `assets/`. O workflow `.github/workflows/pages.yml` troca, na cópia publicada, os links `login.jsp` para apontarem ao Render. A página do Pages não executa JSP.

### Configurar o Render

No Render, crie um Blueprint a partir deste repositório e da branch `main`, usando o arquivo `render.yaml`. Informe os valores de `DB_URL`, `DB_USER`, `DB_PASSWORD` e `PASSWORD_PEPPER` no painel do Render; nunca coloque valores reais em `render.yaml` ou no Git. A `DB_URL` precisa usar o formato JDBC PostgreSQL, por exemplo `jdbc:postgresql://servidor:5432/banco?sslmode=require`. Garanta também que o provedor do banco permita conexões vindas do Render. Se o banco usar lista de IPs permitidos, consulte a aba **Connect → Outbound** do serviço no Render e cadastre as faixas da região escolhida no provedor do banco. Se o perfil usar upload de foto, configure no painel `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY` e `CLOUDINARY_API_SECRET`.

O plano gratuito do Render pode suspender o serviço quando estiver ocioso, levando alguns instantes para responder ao primeiro acesso. Ele é adequado para demonstração, mas tem limitações para uso contínuo.

### Configurar o GitHub Pages

Depois que o Render concluir o primeiro deploy, copie a URL pública do serviço. No GitHub, abra **Settings → Secrets and variables → Actions → Variables**, crie a variável `ACTA_RENDER_URL` com essa URL e, em **Settings → Pages**, selecione **GitHub Actions** como fonte. O workflow publica a página quando houver alterações no `index.html` ou em `assets/` na branch `main` (ou quando executado manualmente pela aba **Actions**). Se o workflow rodar antes de `ACTA_RENDER_URL` ser criada, a publicação fica ignorada; depois de criar a variável, execute-o pela aba **Actions**.

Antes de usar em público, troque no provedor do banco qualquer senha que já tenha sido exposta em commits anteriores; o `.env` local não deve ser enviado ao GitHub.

### Segurança do Tomcat em produção

O WAR apresenta uma página genérica para exceções e erros HTTP 500, sem exibir mensagens ou pilhas Java. O Tomcat da hospedagem também deve estar atualizado (11.0.27 ou versão corrigida mais recente) e configurado para não mostrar relatórios técnicos padrão. Em uma instalação própria, configure o `ErrorReportValve` dentro de `<Host>` em `$CATALINA_BASE/conf/server.xml`:

```xml
<Valve className="org.apache.catalina.valves.ErrorReportValve"
       showReport="false"
       showServerInfo="false" />
```

Em hospedagem compartilhada, solicite ao provedor a atualização do Tomcat e a desativação dos relatórios detalhados.
