# Mind Connect — Back-end (API RESTful)

**Global Solution 2026/2 · CitizenTech · Domain Driven Design using Java · FIAP**

API em Java com Spring Boot da plataforma **Mind Connect**: acesso a informação e a serviços públicos de
acolhimento em saúde mental, com biblioteca de conteúdos educativos e diário pessoal de bem-estar.

## Integrantes

| Nome | RM |
|---|---|
| Guilherme Joel de Camargo | RM570403 |
| Pedro Felipe de Castro Rosa | RM569049 |
| Pedro Henrique Cardoso Cavalcante De Souza | RM570464 |
| Rafaela Donadio Figueiredo Tavares | RM572797 |
| Victor Ferreira Gomes | RM569273 |

## Links

- **API publicada no Render:** https://mindconnect-backend-ashz.onrender.com
- **Vídeo de demonstração (YouTube não listado):** COLE AQUI O LINK DO VÍDEO

## ATENÇÃO
O projeto conta com 2 arquivos SQL, pois foi necessário dividir a parte dos comandos **INSERT** para armazenar os dados
na tabela correta, devido a grande quantidade de dados públicos que foram utilizados e adicionados a API.

## Tecnologias

Java 21 · Spring Boot · **JDBC puro** (DAO com `DataSource` e `PreparedStatement`, sem JPA/Hibernate/Spring Data) ·
Oracle (FIAP) · Bean Validation · ModelMapper · Docker (multi-stage) · Render.

## Domínio

8 entidades (9 tabelas): `Usuario`, `CategoriaServico`, `ServicoAcolhimento`, `CategoriaConteudo`, `Conteudo`
(com a tabela de associação `GS_CONTEUDO_CATEGORIA`), `RegistroBemEstar`, `Administrador` e `Favorito`
(conteúdos favoritados pelos usuários, chave composta).

Relacionamentos 1:N: **CategoriaServico → ServicoAcolhimento** e **Usuario → RegistroBemEstar**.

## Como rodar

1. No Oracle da FIAP, execute o script **`GSsqlDataBase.sql`** (apaga as tabelas antigas com `DROP TABLE`, recria tudo e carrega os dados de teste).
2. Defina as variáveis de ambiente (os valores **não** ficam no código):

   | Variável | Obrigatória | Descrição |
   |---|---|---|
   | `DB_URL` | sim | URL JDBC do Oracle, ex.: `jdbc:oracle:thin:@HOST:1521:SID` |
   | `DB_USERNAME` | sim | usuário do banco |
   | `DB_PASSWORD` | sim | senha do banco |
   | `CORS_ORIGINS` | não | endereços do front separados por vírgula (sem ela, qualquer origem é aceita) |
   | `AUTH_SECRET` | recomendada | segredo (mín. 16 caracteres) que assina os tokens de login; sem ela os logins caem a cada reinício |
   | `ADMIN_EMAIL` / `ADMIN_SENHA` | recomendadas | administrador inicial (veja *Segurança*); senha com no mínimo 6 caracteres |
   | `PORT` | automática | porta definida pelo Render (padrão local: 8080) |

   Exemplo no PowerShell:

   ```powershell
   $env:DB_URL = "jdbc:oracle:thin:@HOST:1521:SID"
   $env:DB_USERNAME = "seu_usuario"
   $env:DB_PASSWORD = "sua_senha"
   $env:AUTH_SECRET = "um-segredo-longo-qualquer"
   $env:ADMIN_EMAIL = "ana.souza@example.com"
   $env:ADMIN_SENHA = "senha-do-admin"
   ```

   Exemplo no Linux/macOS: `export DB_URL="jdbc:oracle:thin:@HOST:1521:SID"` (e assim por diante).

3. Rode com Java 21 e Maven:

   ```bash
   mvn spring-boot:run
   ```

   A API sobe em `http://localhost:8080`. Teste: `GET http://localhost:8080/api/categorias-servico`.

### Com Docker

```bash
docker build -t mindconnect .
docker run -p 8080:8080 -e DB_URL=... -e DB_USERNAME=... -e DB_PASSWORD=... mindconnect
```

## Deploy no Render

Web Service com ambiente **Docker** apontando para este repositório; cadastrar `DB_URL`, `DB_USERNAME`,
`DB_PASSWORD` (e, se quiser, `CORS_ORIGINS`) em *Environment*. Detalhes na documentação técnica em PDF (`docs/`).

## Endpoints (todos com prefixo `/api`)

Cada recurso segue o padrão: `POST` (201 + `Location`), `GET` lista (200), `GET /{id}` (200/404),
`PUT /{id}` (200/400/404/409) e `DELETE /{id}` (204/404/409).

| Recurso | Rota base | Rotas extras |
|---|---|---|
| Usuários | `/api/usuarios` | `GET/POST /api/usuarios/{id}/bem-estar` · `GET/POST /api/usuarios/{id}/favoritos` · `GET/DELETE /api/usuarios/{id}/favoritos/{idConteudo}` |
| Login | — | `POST /api/auth/login` |
| Categorias de serviço | `/api/categorias-servico` | `GET/POST /api/categorias-servico/{id}/servicos` |
| Serviços de acolhimento | `/api/servicos` | `?nome=&regiao=&categoria=`, `/regiao/{regiao}`, `/categoria/{categoria}` |
| Categorias de conteúdo | `/api/categorias-conteudo` | — |
| Conteúdos | `/api/conteudos` | `?busca=&status=&categoria=`, `/categoria/{categoria}` |
| Diário de bem-estar | `/api/bem-estar` | `?idUsuario=`, `/historico?idUsuario=` |
| Administradores | `/api/administradores` | — |

Erros sempre em JSON padronizado (sem stack trace nem SQL): `400` validação, `401` login incorreto,
`404` não encontrado, `405` método não suportado, `409` regra de negócio / remoção de registro com filhos,
`500` erro inesperado (mensagem genérica).

### Exemplo rápido

```http
POST /api/usuarios
Content-Type: application/json

{ "nome": "Maria Silva", "email": "maria@example.com", "senha": "senha123" }
```

```http
POST /api/usuarios/1/favoritos
Content-Type: application/json

{ "idConteudo": 5 }
```

```http
POST /api/usuarios/6/bem-estar
Content-Type: application/json

{ "humor": "BEM", "nivelHumor": 4, "sentimentos": "Tranquilidade", "observacoes": "Dia produtivo" }
```

## Segurança e permissões

O login (`POST /api/auth/login`) devolve um **token assinado** (válido por 8 horas). As rotas protegidas exigem o header:

```
Authorization: Bearer <token>
```

O usuário logado é identificado pelo token (assinado com HMAC-SHA256, não dá para trocar o id dentro dele) e **nunca**
por um id enviado pelo cliente. A permissão de administrador é conferida no banco a cada requisição.

| Quem | O que pode |
|---|---|
| **Público** (sem login) | Cadastrar-se (`POST /api/usuarios`), fazer login e consultar serviços, categorias de serviço, conteúdos e categorias de conteúdo (`GET`) |
| **Administrador** | Tudo do público + cadastrar, alterar e excluir serviços, conteúdos, categorias e administradores; listar contas (`GET /api/usuarios`) |
| **Usuário comum (logado)** | Seus próprios dados: conta, diário de bem-estar e favoritos |

Proteção dos dados individuais (diário e favoritos): cada usuário só acessa os **próprios** registros. Nem o administrador
lê o diário ou os favoritos de outra pessoa.

- `/api/usuarios/{id}/bem-estar` e `/api/usuarios/{id}/favoritos`: o `{id}` precisa ser o do usuário logado.
- `/api/bem-estar`: lista só os registros do logado; `?idUsuario=` de outra pessoa e `idUsuario` de outra pessoa no corpo do `POST` retornam 403;
  consultar, alterar ou excluir registro de outra pessoa retorna 403.
- `GET`/`PUT`/`DELETE /api/usuarios/{id}`: o dono da conta (o `GET` e o `DELETE` também aceitam administrador).

Respostas: **401** sem token válido (ou expirado) e **403** sem permissão.

**Primeiro administrador.** Os administradores da carga do SQL têm senha de demonstração e não conseguem logar. Defina
`ADMIN_EMAIL` e `ADMIN_SENHA`: ao iniciar, a API cria essa conta (ou ativa uma existente, como `ana.souza@example.com`, que já é administrador)
com a senha informada. Depois, o administrador pode cadastrar outros administradores pela API.

## Regras de negócio

- E-mail de usuário único (409).
- Apenas **um registro de bem-estar por usuário por dia** (409).
- Não pode haver dois serviços com o mesmo nome e endereço (409).
- Não remove registro com filhos: categoria com serviços/conteúdos, usuário com registros de bem-estar, favoritos ou acesso de administrador (409).
- Nomes de categoria únicos (409).
- Um usuário não favorita o mesmo conteúdo duas vezes (409); ao excluir um conteúdo, os favoritos dele saem junto.

## Observações

- Os usuários da carga inicial (`HASH_DEMO_...`) têm senha de demonstração e **não** fazem login. Para testar o login,
  cadastre um usuário novo em `POST /api/usuarios` (a senha é salva com hash PBKDF2) ou use `ADMIN_EMAIL`/`ADMIN_SENHA` para o administrador.
- Os serviços e conteúdos da carga inicial são fictícios para teste acadêmico.
- A documentação técnica completa (classes, DER, endpoints, regras, deploy) está em `docs/Documentacao_Tecnica_MindConnect.pdf`.
