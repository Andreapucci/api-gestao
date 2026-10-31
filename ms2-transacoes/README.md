# MS2 — Serviço de Transações

Microsserviço responsável pelo registro e consulta das transações financeiras (depósito, saque, transferência e compra), análise de despesas, câmbio e relatórios, sempre validando saldo e limites antes de aprovar.

## O que o serviço faz

- Cria transações dos quatro tipos exigidos: `DEPOSITO`, `SAQUE`, `TRANSFERENCIA`, `COMPRA`.
- Valida saldo, limite diário e limite por transação consultando a API Mock (saldo e limites), antes de aprovar saques, transferências e compras.
- Grava toda transação como `APROVADA` ou `REJEITADA`, com o motivo quando rejeitada. Transações nunca são apagadas.
- Consulta a cada transação o câmbio do dia na BrasilAPI e mostra o valor convertido em USD e EUR — se a BrasilAPI estiver fora do ar, a consulta continua respondendo, sem as conversões.
- Gera análise de despesas por categoria, por dia e por mês (apenas saídas aprovadas contam como despesa).
- Gera relatório de transações em CSV para download, compatível com Excel em português.
- Protege todas as rotas com autenticação básica (Basic Auth), validada contra o MS1, e impede que um usuário acesse dados de outro.

## Arquitetura (hexagonal)

```
src/main/java/com/apigestao/transacoes/
├── domain/
│   ├── model/              -> Transacao, TipoTransacao, StatusTransacao (sem dependência de framework)
│   └── ports/output/       -> TransacaoRepositoryPort, ContaClientPort, UsuarioClientPort, CambioClientPort
├── application/
│   └── usecases/           -> CreateTransacaoUseCase, FindTransacaoUseCase, ListTransacoesUseCase,
│                               AnalisarDespesasUseCase, GerarRelatorioCsvUseCase, ConverterValorTransacaoUseCase
└── infrastructure/
    ├── adapters/
    │   ├── input/web/      -> Controller, DTOs e mapper (porta de entrada HTTP)
    │   └── output/
    │       ├── persistence/ -> Adapter JPA, entidade, mapper e repositório Spring Data
    │       ├── ms1/         -> Cliente REST para o MS1 (existência de usuário)
    │       ├── mockapi/     -> Cliente REST para a API Mock (saldo e limites)
    │       └── brasilapi/   -> Cliente REST para a BrasilAPI (câmbio)
    └── config/
        ├── security/        -> Basic Auth validado contra o MS1 e filtro de titularidade
        └── exception/       -> Tratamento global de exceções (padronizado com o MS1)
```

O domínio (`domain/model/Transacao`) não depende de Spring, JPA ou HTTP. As regras de aprovação/rejeição e os limites ficam isoladas em `CreateTransacaoUseCase`.

## Como subir com o Docker Compose

O MS2 sobe junto com o MS1, o banco de transações, o banco de usuários e o pgAdmin a partir da raiz do projeto:

```bash
docker compose up -d
```

- API de transações: `http://localhost:5002`
- Swagger: `http://localhost:5002/swagger-ui.html`

No Compose, o MS2 fala com o MS1 pelo nome do serviço na rede interna (`http://api-usuarios:8080`), não por `localhost`.

Para derrubar o ambiente:

```bash
docker compose down
```

## Como rodar os testes

```bash
./mvnw test
```

Os testes cobrem as regras críticas: aprovação/rejeição de transação (saldo, limite por transação), análise de despesas (agrupamento por categoria, exclusão de rejeitadas e depósitos, período padrão), geração do relatório CSV (cabeçalho, acentos, motivo de rejeição) e conversão de câmbio (resiliência quando a BrasilAPI falha).

O build gera um relatório de cobertura (JaCoCo) em `target/site/jacoco/index.html`.

## Variáveis de ambiente

Definidas no arquivo `.env` na raiz do projeto (veja `.env.example`) e no `compose.yaml`:

| Variável | Descrição |
|---|---|
| `TRANSACTION_DB_NAME` | Nome do banco de dados de transações |
| `TRANSACTION_DB_USER` | Usuário do banco de transações |
| `TRANSACTION_DB_PASSWORD` | Senha do banco de transações |
| `MS1_BASE_URL` | URL base do MS1 (no Compose: `http://api-usuarios:8080`) |
| `BRASILAPI_BASE_URL` | URL base da BrasilAPI (padrão: `https://brasilapi.com.br`) |

A URL da API Mock (saldo e limites) está em `application.properties` (`mockapi.base-url`).

## Autenticação

Todas as rotas em `/api/transacoes/**` exigem Basic Auth (email e senha cadastrados no MS1). O Swagger e a documentação OpenAPI ficam abertos.

O cabeçalho `X-Usuario-Id`, usado para identificar o dono da transação, é validado contra o usuário autenticado: se não coincidirem, a resposta é `403 Forbidden`.

## Endpoints principais

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/transacoes` | Cria uma transação |
| GET | `/api/transacoes/{id}` | Busca uma transação do usuário autenticado |
| GET | `/api/transacoes/usuario` | Lista transações por período e tipo |
| GET | `/api/transacoes/saldo` | Consulta o saldo atual |
| GET | `/api/transacoes/despesas` | Análise de despesas por categoria/dia/mês |
| GET | `/api/transacoes/relatorio` | Download do relatório em CSV |
