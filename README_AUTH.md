# AgroTech Auth API - Guia Completo (Arquitetura Hexagonal 100%)

Este documento explica:

- **Arquitetura hexagonal implementada** no módulo de autenticação
- Separação em 4 camadas: Domain, Application, Infrastructure, Web
- Ports (contrato) e Adapters (implementação)
- Fluxo completo: registro, login, refresh com rotação, logout e autorização
- Passo a passo para testar tudo no Postman

## 1. Visão Geral - Arquitetura Hexagonal

O módulo de autenticação está 100% construído segundo o padrão **Hexagonal (Ports & Adapters)**:

- **DOMAIN LAYER**: Lógica pura, sem dependências do Spring Framework
  - Entidades (User, Role, RefreshToken)
  - Casos de uso (AuthUseCase - interface)
  - Exceptions de negócio (ConflictException, NotFoundException, UnauthorizedException)

- **APPLICATION LAYER**: Regras de negócio e orquestração
  - AuthApplicationService: implementação pura dos casos de uso
  - Ports (interface de dependências externas)
  - Não depende de Spring; depende apenas de ports

- **INFRASTRUCTURE LAYER**: Adaptadores que conectam ao Spring Framework
  - Adapters: implementam ports usando Spring (JPA, Security, JWT)
  - Configuração: ApplicationLayerConfig injeta dependências
  - Serviços técnicos: JwtService, RefreshTokenService, CustomUserDetailsService

- **WEB LAYER**: Controllers e tratamento de exceções
  - Controllers: REST entry points, dependem de AuthUseCase (porta)
  - ApiExceptionHandler: mapeia exceptions de domínio para HTTP status codes

**Benefícios desta arquitetura:**
- Lógica de negócio independente de Spring (testável sem context)
- Fácil de substituir tecnologias (mudar JWT por OAuth, H2 por PostgreSQL, etc.)
- Inversão de controle explícita via ports
- Regras de negócio centraliza no application layer

## 2. Tecnologias

- Java 21
- Spring Boot 3.3.x
- Spring Security (stateless, JWT)
- Spring Data JPA
- H2 Database (memória)
- JJWT 0.12.x (geração e validação de token)
- JUnit 5 + MockMvc (testes de integração)

## 3. Estrutura de Camadas Hexagonal

### 3.1 DOMAIN LAYER (Pura - Zero Spring)

Entidades de negócio e lógica pura. Nenhuma dependência do Spring Framework.

**Entidades:**
- `model/User.java`: Entidade JPA com id, name, email, password (hidden), role, active
  - Anteriormente implementava UserDetails; agora é puro POJO
  - Campo `active` (boolean, padrão true): desativar usuário sem deletar
  - Annotations: @Entity, @Table, @JsonIgnore (para campos sensíveis)
  - Role: enum ADMIN, OPERADOR, ESPECIALISTA

- `model/Role.java`: Enum com os 3 perfis do sistema

- `model/RefreshToken.java`: Entidade JPA para tokens persistidos
  - Campos: token (UUID), user, expiresAt, revoked
  - Cada token é único; pode ser revogado individualmente
  - Suporta detecção de reutilização (token revogado = comprometido)

**Exceptions (Domain-Level):**
- `application/exception/ConflictException`: Email já existe (HTTP 409)
- `application/exception/NotFoundException`: Recurso não encontrado (HTTP 404)
- `application/exception/UnauthorizedException`: Falha de autenticação/token (HTTP 401)

### 3.2 APPLICATION LAYER (Orquestração de Casos de Uso)

Camada de aplicação: pura, sem Spring annotations. Implementa regras de negócio dependendo apenas de **ports** (interfaces, não implementações concretas).

**Porta de Entrada (Inbound):**
- `application/port/in/AuthUseCase.java`: Interface que define o contrato
  - Métodos: register, login, refresh, logout, me, listUsers
  - Implementada por AuthApplicationService

**Serviço de Aplicação:**
- `application/service/AuthApplicationService.java`: Implementação **sem @Service**
  - Implementa AuthUseCase (porta de entrada)
  - Depende de 5 portas de saída (abaixo)
  - Orquestra o fluxo: validação → chamadas às portas → retorno
  - Lança exceptions de domínio (não exceções do Spring)

**Portas de Saída (Outbound) - Interfaces:**
- `application/port/out/UserPort.java`: Abstração para persistência de usuário
  - Métodos: findByEmail, save, existsByEmail, findAll
  - Implementada por UserPersistenceAdapter

- `application/port/out/AuthenticationPort.java`: Abstração para validação de credenciais
  - Método: authenticate(email, password)
  - Implementada por SpringAuthenticationAdapter

- `application/port/out/AccessTokenPort.java`: Abstração para geração de JWT
  - Método: generateAccessToken(email, role)
  - Implementada por JwtAccessTokenAdapter

- `application/port/out/RefreshTokenPort.java`: Abstração para ciclo de refresh tokens
  - Métodos: create, rotate, revoke
  - Implementada por RefreshTokenService

- `application/port/out/PasswordHashPort.java`: Abstração para hash de senha
  - Método: hashPassword(rawPassword)
  - Implementada por BCryptPasswordHashAdapter

**Como funciona:**
```
AuthController 
  → AuthUseCase (porta)
    → AuthApplicationService.register() (implementação)
       → UserPort.save() (porta)
         → UserPersistenceAdapter.save() (adapter)
           → UserRepository.save() (Spring Data JPA)
```

### 3.3 INFRASTRUCTURE LAYER (Adaptadores do Spring)

Implementações concretas dos ports usando Spring Framework. Aqui fica o acoplamento.

**Adapters (Implementam Ports):**
- `infrastructure/persistence/UserPersistenceAdapter.java`: @Component
  - Implementa UserPort usando UserRepository
  - Converte dados entre domínio e banco

- `infrastructure/security/SpringAuthenticationAdapter.java`: @Component
  - Implementa AuthenticationPort usando AuthenticationManager
  - Lança UnauthorizedException (domínio) ao invés de ResponseStatusException

- `infrastructure/security/JwtAccessTokenAdapter.java`: @Component
  - Implementa AccessTokenPort usando JwtService
  - Gera JWT com claims da role

- `infrastructure/security/BCryptPasswordHashAdapter.java`: @Component
  - Implementa PasswordHashPort usando PasswordEncoder do Spring
  - Encapsula BCrypt

**Serviços Técnicos (Spring Components):**
- `service/JwtService.java`: @Component
  - Gera access tokens JWT com expiracao configurável
  - Valida assinatura + expiração, trabalha com domínio primitivos (String email, Role)
  - Propriedades: app.jwt.secret, app.jwt.access-token-expiration-ms

- `service/RefreshTokenService.java`: @Component (implementa RefreshTokenPort)
  - create(user): revoga todos os ativos e cria novo (sessão única por usuário)
  - rotate(tokenStr): valida, revoga token atual e emite novo
  - revoke(tokenStr): marca como revogado para logout
  - Detecta reutilização: token revogado = 401

- `service/CustomUserDetailsService.java`: @Component
  - Adapter de domínio User para Spring UserDetails
  - Carrega usuário por email para o Spring Security

- `repository/UserRepository.java`: Spring Data JPA
  - findByEmail, existsByEmail, findAll

- `repository/RefreshTokenRepository.java`: Spring Data JPA
  - findByToken, revokeAllActiveByUser (query JPQL customizada)

**Configuração:**
- `config/ApplicationLayerConfig.java`: @Configuration
  - Bean factory que wira AuthApplicationService com todos os 5 ports
  - Injeta adapters, que injetam Spring components
  - Entry point para toda a orquestração

- `config/SecurityConfig.java`: @Configuration
  - Cadeia de filtros stateless
  - AuthenticationEntryPoint: 401 para não autenticados
  - AccessDeniedHandler: 403 para sem permissão

- `config/JwtAuthenticationFilter.java`: OncePerRequestFilter
  - Extra header Authorization: Bearer <token>
  - Valida JWT e popula SecurityContext

### 3.4 WEB LAYER (REST Entry Points)

**Controllers:**
- `controller/AuthController.java`: @RestController
  - Depende de AuthUseCase (porta, não serviço concreto)
  - POST /api/auth/register
  - POST /api/auth/login
  - GET /api/auth/me
  - POST /api/auth/refresh
  - POST /api/auth/logout

- `controller/UsersController.java`: @RestController
  - GET /api/users (ADMIN only)
  - GET /api/users/operador/dashboard (OPERADOR+)
  - GET /api/users/especialista/dashboard (ESPECIALISTA+)
  - GET /api/users/admin/dashboard (ADMIN only)

**Tratamento de Exceções:**
- `controller/ApiExceptionHandler.java`: @RestControllerAdvice
  - ConflictException → 409
  - NotFoundException → 404
  - UnauthorizedException → 401
  - Mapeia exceptions de domínio para HTTP status codes

**DTOs (Transport Objects):**
- `dto/RegisterRequest.java`: nome, email, password, role
- `dto/LoginRequest.java`: email, password
- `dto/RefreshRequest.java`: refreshToken
- `dto/AuthResponse.java`: accessToken, refreshToken, tokenType, userId, name, email, role

### 3.5 TESTES DE INTEGRAÇÃO

- `AuthControllerTest.java`: 13 cenários (register, login, /me, refresh com rotação, detecção de reutilização, logout)
- `UsersControllerTest.java`: 14 cenários (RBAC matrix completa: 200/401/403 para todas as roles)

**Executar testes:**
```bash
mvn test
```

Resultado esperado: 27 testes, 0 falhas

## 4. Fluxo Completo da Autenticação com Hexagonal

### 4.1 Registro (POST /api/auth/register)

```
POST /api/auth/register
Body: { name, email, password, role }
  ↓
AuthController.register()
  ↓ injeta
AuthUseCase (porta)
  ↓ implementa
AuthApplicationService.register()
  ↓ orquestra:
    1. Valida dados via beans validation do DTO
    2. Chama UserPort.existsByEmail() para verificar duplicação
       → UserPersistenceAdapter.existsByEmail()
         → UserRepository.existsByEmail()
       Se existe: lança ConflictException (409)
    3. Chama PasswordHashPort.hashPassword()
       → BCryptPasswordHashAdapter.hash()
         → PasswordEncoder.encode() (Spring Security)
    4. Cria User (domínio) e chama UserPort.save()
       → UserPersistenceAdapter.save()
         → UserRepository.save()
    5. Chama RefreshTokenPort.create() para criar token
       → RefreshTokenService.create()
         → Revoga anteriores (sessão única)
         → Persiste novo refresh token
    6. Chama AccessTokenPort.generateAccessToken()
       → JwtAccessTokenAdapter.generateAccessToken()
         → JwtService.generateAccessToken(email, role)
           → JJWT gera JWT assinado
    7. Retorna AuthResponse com tokens
  ↓ (exceção ConflictException)
ApiExceptionHandler → 409 Conflict
```

### 4.2 Login (POST /api/auth/login)

```
POST /api/auth/login
Body: { email, password }
  ↓
AuthController.login()
  ↓ injeta
AuthApplicationService.login()
  ↓ orquestra:
    1. Chama AuthenticationPort.authenticate()
       → SpringAuthenticationAdapter.authenticate()
         → AuthenticationManager.authenticate()
           → CustomUserDetailsService.loadUserByUsername()
             → Carrega User do banco
             → Adapta para Spring UserDetails
           → verifica password com BCryptPasswordEncoder
       Se falha: lança UnauthorizedException (401)
    2. Chama RefreshTokenPort.create()
       → RefreshTokenService.create()
         → revokeAllActiveByUser() (nova sessão = revoga antigas)
         → persiste novo token
    3. Chama AccessTokenPort.generateAccessToken()
       → JwtAccessTokenAdapter.generateAccessToken()
         → JwtService.generateAccessToken(email, role)
    4. Retorna AuthResponse com tokens
  ↓ (exceção UnauthorizedException)
ApiExceptionHandler → 401 Unauthorized
```

### 4.3 Acesso a Endpoints Protegidos (GET /api/auth/me)

```
GET /api/auth/me
Header: Authorization: Bearer <accessToken>
  ↓
JwtAuthenticationFilter (OncePerRequestFilter)
  ↓ intercepta:
    1. Extrai token do header
    2. Chama JwtService.isTokenValid(token, email)
       → Valida assinatura (HMAC-SHA256)
       → Valida expiração
       → Extrai claims (email, role)
    3. Se válido: popula SecurityContext com Authentication
       (email + roles)
    4. Passa para controller
  ↓
AuthController.me()
  ↓ acessa SecurityContext (já preenchido)
  ↓ chama
AuthApplicationService.me()
  → UserPort.findByEmail(email)
    → UserRepository.findByEmail()
  ↓
Retorna User (domínio) com id, name, email, role, active
(password e UserDetails fields ocultados via @JsonIgnore)

  ↓ (sem token ou expirado)
JwtAuthenticationFilter
  ↓ sem SecurityContext
  ↓
SecurityConfig.authenticationEntryPoint()
  ↓ 401 Unauthorized
```

### 4.4 Refresh Token com Rotação (POST /api/auth/refresh)

```
POST /api/auth/refresh
Body: { refreshToken: "<uuid>" }
  ↓
AuthController.refresh()
  ↓
AuthApplicationService.refresh()
  ↓ orquestra:
    1. Chama RefreshTokenPort.rotate(tokenStr)
       → RefreshTokenService.rotate()
         1. Busca token no banco: RefreshTokenRepository.findByToken()
         2. Valida:
            - Se revogado: lança UnauthorizedException (401)
              (Detecção!)
            - Se expirado: lança UnauthorizedException (401)
            - Se ativo: continua
         3. Revoga o token atual
         4. Persiste novo refresh token
         5. Retorna User (para extrair email e role)
    2. Chama AccessTokenPort.generateAccessToken()
       → JwtAccessTokenAdapter.generateAccessToken()
         → JwtService.generateAccessToken(email, role)
    3. Retorna AuthResponse com novo par (accessToken e refreshToken)
  ↓ (token revogado = reutilização detectada)
ApiExceptionHandler → 401 Unauthorized
```

### 4.5 Logout com Revogação (POST /api/auth/logout)

```
POST /api/auth/logout
Header: Authorization: Bearer <accessToken>
Body: { refreshToken: "<uuid>" }
  ↓ (valida token como em /me)
JwtAuthenticationFilter
  ↓ SecurityContext preenchido
  ↓
AuthController.logout()
  ↓
AuthApplicationService.logout()
  ↓ orquestra:
    1. Chama RefreshTokenPort.revoke(tokenStr)
       → RefreshTokenService.revoke()
         1. Busca token: RefreshTokenRepository.findByToken()
         2. Marca como revogado (set revoked = true)
         3. Persiste
         (Operação idempotente: revogar novamente não gera erro)
    2. Retorna 204 No Content (sem corpo)
  ↓ (próxima tentativa de renovação)
Tentativa: POST /api/auth/refresh com mesmo token
  → RefreshTokenPort.rotate() detecta revogado
  → UnauthorizedException (401)
```

## 5. Objetivo do Módulo

Com esta arquitetura, a API garante:

- ✅ Cadastro de usuário com roles (ADMIN, OPERADOR, ESPECIALISTA)
- ✅ Login com retorno de accessToken e refreshToken
- ✅ Renovação de sessão via refresh com **rotação segura** (novo token a cada refresh)
- ✅ **Detecção de reutilização**: token já usado = 401 (comprometimento detectado)
- ✅ Logout com **revogação** do refresh token no banco (idempotente)
- ✅ Endpoint /me com dados limpos (password, UserDetails hidden)
- ✅ Flag `active` para desativar usuários sem deletar
- ✅ Respostas HTTP semanticamente corretas: 401 não-autenticado, 403 sem permissão, 409 conflito
- ✅ Autenticação **stateless** (sem sessão HTTP)
- ✅ **100% Hexagonal**: lógica de negócio pura, testável, agnóstica a framework

## 6. Como Executar

### 6.1 Pré-requisitos
- Java 21 instalado
- Maven 3.8+

### 6.2 Compilar e rodar

```bash
cd auth_projeto_java
mvn spring-boot:run
```

O servidor sobe em:
```
http://localhost:8080
```

### 6.3 Verificar se está rodando

Abra no navegador ou curl:
```bash
curl http://localhost:8080/api/health
```

Ou chame um endpoint público (abaixo no Postman).

## 7. Testes no Postman (Passo a Passo)

### 7.1 Preparar Collection e Variáveis

Crie uma Collection chamada "AgroTech Auth" e configure:

- Variável: `baseUrl` = `http://localhost:8080`
- Variável: `tokenAdmin` = (vazia)
- Variável: `tokenOperador` = (vazia)
- Variável: `tokenEspecialista` = (vazia)
- Variável: `refreshTokenAdmin` = (vazia)
- Variável: `refreshTokenOperador` = (vazia)
- Variável: `refreshTokenEspecialista` = (vazia)

### 7.2 Request 1 - Registrar ADMIN

- Método: POST
- URL: `{{baseUrl}}/api/auth/register`
- Headers:
  - Content-Type: application/json
- Body (raw JSON):

```json
{
  "name": "Admin 1",
  "email": "admin@agrotech.com",
  "password": "123456",
  "role": "ADMIN"
}
```

Esperado - Status **201 Created**:

```json
{
  "accessToken": "<jwt>",
  "refreshToken": "<uuid>",
  "tokenType": "Bearer",
  "userId": 1,
  "name": "Admin 1",
  "email": "admin@agrotech.com",
  "role": "ADMIN"
}
```

No Postman:
- Abra a aba **Tests**
- Cole:
```javascript
pm.environment.set("tokenAdmin", pm.response.json().accessToken);
pm.environment.set("refreshTokenAdmin", pm.response.json().refreshToken);
```
- Execute a requisição e rode os testes

### 7.3 Request 2 - Registrar OPERADOR

- Método: POST
- URL: `{{baseUrl}}/api/auth/register`
- Body:

```json
{
  "name": "Operador 1",
  "email": "operador@agrotech.com",
  "password": "123456",
  "role": "OPERADOR"
}
```

Esperado: Status **201**. Salve os tokens em `tokenOperador` e `refreshTokenOperador`.

### 7.4 Request 3 - Registrar ESPECIALISTA

- Método: POST
- URL: `{{baseUrl}}/api/auth/register`
- Body:

```json
{
  "name": "Especialista 1",
  "email": "especialista@agrotech.com",
  "password": "123456",
  "role": "ESPECIALISTA"
}
```

Esperado: Status **201**. Salve os tokens em `tokenEspecialista` e `refreshTokenEspecialista`.

### 7.5 Request 4 - Login (Qualquer Usuário)

- Método: POST
- URL: `{{baseUrl}}/api/auth/login`
- Body:

```json
{
  "email": "operador@agrotech.com",
  "password": "123456"
}
```

Esperado - Status **200**: Retorna novo par de accessToken e refreshToken (mesma estrutura do register).

### 7.6 Request 5 - Obter Dados do Usuário Logado (/me)

**⚠️ ATENÇÃO:** O endpoint correto é `GET /api/auth/me`. **NÃO use `/api/users/me`** — esse caminho não existe.

- Método: GET
- URL: `{{baseUrl}}/api/auth/me`
- Header: `Authorization: Bearer {{tokenOperador}}`

Esperado - Status **200**:

```json
{
  "id": 2,
  "name": "Operador 1",
  "email": "operador@agrotech.com",
  "role": "OPERADOR",
  "active": true
}
```

**Nota:** password, authorities e campos internos do Spring Security **não** aparecem na resposta (ocultados via @JsonIgnore).

**Teste negativo:**

- Sem Authorization header: **401 Unauthorized**
- Token inválido ou expirado: **401 Unauthorized**

### 7.7 Request 6 - Refresh Token (Rotação Segura)

- Método: POST
- URL: `{{baseUrl}}/api/auth/refresh`
- Body:

```json
{
  "refreshToken": "{{refreshTokenOperador}}"
}
```

Esperado - Status **200**: Novo par de accessToken e refreshToken.

**Teste negativo 1 - Reutilização:**

- Execute o refresh novamente com o **mesmo** refreshToken anterior
- Esperado: **401 Unauthorized** (token revogado na primeira rotação, reutilização detectada)

**Teste negativo 2 - Token inválido:**

- Use um UUID aleatório no lugar de refreshToken
- Esperado: **401 Unauthorized**

**Teste negativo 3 - Token expirado:**

- (Requer aguardar 7 dias ou editar o banco H2)
- Esperado: **401 Unauthorized**

### 7.8 Request 7 - Logout

- Método: POST
- URL: `{{baseUrl}}/api/auth/logout`
- Header: `Authorization: Bearer {{tokenOperador}}` (opcional para validação)
- Body:

```json
{
  "refreshToken": "{{refreshTokenOperador}}"
}
```

Esperado: Status **204 No Content** (sem corpo na resposta).

**Verificação:** Tente renovar com o mesmo refreshToken:

- Método: POST
- URL: `{{baseUrl}}/api/auth/refresh`
- Body:

```json
{
  "refreshToken": "{{refreshTokenOperador}}"
}
```

Esperado: **401 Unauthorized** (token revogado no logout)

### 7.9 Request 8 - Endpoints por Role (RBAC Matrix)

Para todos os requests abaixo:
- Método: **GET**
- Headers: `Authorization: Bearer {{token}}`

| Endpoint                          | OPERADOR | ESPECIALISTA | ADMIN | Nenhum Token |
|-----------------------------------|----------|--------------|-------|--------------|
| /api/users/operador/dashboard     | **200**  | **200**      | **200** | **401**    |
| /api/users/especialista/dashboard | **403**  | **200**      | **200** | **401**    |
| /api/users/admin/dashboard        | **403**  | **403**      | **200** | **401**    |
| /api/users                        | **403**  | **403**      | **200** | **401**    |

**Teste cada combinação para validar RBAC.**

## 8. Mapa Rápido de Status HTTP

| Status | Significado                                              | Exemplo                                  |
|--------|----------------------------------------------------------|------------------------------------------|
| **200** | Sucesso em leitura, login, refresh, atualização         | GET /api/auth/me, POST /api/auth/login   |
| **201** | Recurso criado com sucesso                              | POST /api/auth/register                  |
| **204** | Operação bem-sucedida sem corpo (ex: delete)            | POST /api/auth/logout                    |
| **400** | Payload inválido (campo vazio, email mal formatado etc.) | POST /api/auth/register com name vazio   |
| **401** | Não autenticado: token ausente, inválido ou expirado    | GET /api/auth/me sem Authorization       |
| **403** | Autenticado, mas sem role necessária para o endpoint    | OPERADOR em /api/users/admin/dashboard   |
| **409** | Conflito: email já cadastrado                           | POST /api/auth/register com email duplo  |

## 9. Dicas de Troubleshooting

### 9.1 401 em /api/auth/me, /api/users/* ou outros endpoints protegidos

- **Problema:** Header Authorization mal formatado
- **Solução:** Verifique se é exatamente `Authorization: Bearer <token>` (sem aspas, sem BEARER em lowercase)

- **Problema:** Token expirou
- **Solução:** Token access expira em 15 minutos (padrão). Faça refresh com o refreshToken

- **Problema:** Chama endpoint errado
- **Solução:** Para obter dados do usuário logado, use `GET /api/auth/me`, **não** `/api/users/me`

### 9.2 401 no /api/auth/refresh

- **Problema:** refreshToken já foi utilizado (rotação)
- **Solução:** Cada refresh token pode ser usado **apenas uma vez**. Após o primeiro uso, é revogado automaticamente e gera um novo. Tentativa de reutilizar = 401

- **Problema:** refreshToken foi revogado via logout
- **Solução:** Após logout, esse token não pode ser reutilizado. Faça login novamente

- **Problema:** refreshToken expirou
- **Solução:** Refresh tokens expiram em 7 dias (padrão). Faça login novamente

### 9.3 403 em endpoints de role

- **Problema:** Token é válido, mas usuário não tem role necessária
- **Solução:** Verifique a role que foi cadastrada no register. Se OPERADOR tenta acessar `/api/users/admin/dashboard` → 403

### 9.4 409 no register

- **Problema:** Email já existe no banco
- **Solução:** Use um email diferente ou reinicie a aplicação (H2 em memória, dados são perdidos)

### 9.5 400 no register

- **Problema:** name vazio
- **Solução:** name não pode ser vazio

- **Problema:** email inválido
- **Solução:** Email deve ter formato válido, ex: usuario@dominio.com

- **Problema:** password muito curta
- **Solução:** Password deve ter no mínimo 6 caracteres

### 9.6 Porta já em uso (Error: bind exception)

- **Problema:** Porta 8080 já tem outro processo
- **Solução:** (Verificar terminais anteriores) Mate o processo ou use outra porta
```bash
# No PowerShell (Windows):
Get-Process -Id <PID> | Stop-Process -Force

# Ou mude a porta em application.yml:
server:
  port: 8081
```

## 10. Implementações Realizadas

### ✅ Autenticação e Autorização

- Registro com roles (ADMIN, OPERADOR, ESPECIALISTA)
- Login com validação de credenciais
- Access Token JWT com expiração configurável (15 min padrão)
- Refresh Token persistido no banco (uuid, expiração 7 dias)
- Logout com revogação idempotente de refresh token
- Endpoint /me retornando dados limpos do usuário
- Flag `active` para desativar usuários sem deletar
- Proteção de endpoints por role (RBAC)
- Respostas HTTP semanticamente corretas (401, 403, 409, 404, 201, 200, 204)

### ✅ Segurança

- Autenticação **stateless** (sem sessão HTTP)
- **Refresh token rotation**: novo token a cada refresh, antigo revogado
- **Reutilization detection**: tentativa de usar token revogado = 401
- **Single session per user**: logout + refresh sempre criam nova sessão
- Senha criptografada com BCrypt (nunca armazenada em texto plano)
- Campos sensíveis ocultados no JSON (@JsonIgnore)
- Validação de JWT com assinatura HMAC-SHA256

### ✅ Arquitetura Hexagonal (100% Implementada)

- **Domain Layer**: Entidades puras (User, Role, RefreshToken), exceptions (ConflictException, NotFoundException, UnauthorizedException)
- **Application Layer**: AuthUseCase (porta), AuthApplicationService (puro, sem @Service)
- **Infrastructure Layer**: 5 adapters (UserPersistenceAdapter, SpringAuthenticationAdapter, JwtAccessTokenAdapter, BCryptPasswordHashAdapter, RefreshTokenService)
- **Configuration Layer**: ApplicationLayerConfig (bean factory para wiring)
- **Web Layer**: Controllers (dependem de portas), ApiExceptionHandler (mapeia exceptions de domínio)
- **Inversão de Controle**: Controllers → Portas → Serviço de Aplicação → Portas de Saída → Adapters

### ✅ Testes de Integração

- 27 testes cobrindo 100% dos fluxos
- 13 testes AuthControllerTest: register, login, /me, refresh com rotação, detecção de reutilização, logout
- 14 testes UsersControllerTest: RBAC matrix completa (200/401/403 para todas as combinações role/endpoint)
- Todos os cenários de sucesso e erro cobertos

### ✅ Configuração

- application.yml com porta, banco H2, JPA, JWT
- Propriedades configuráveis: app.jwt.secret, access-token-expiration-ms, refresh-token-expiration-ms
- SecurityConfig com stateless chain, entry point customizado, denied handler customizado
- JwtAuthenticationFilter como OncePerRequestFilter

## 11. Próximos Passos Sugeridos

### Curto Prazo (Evoluir Auth)

- [ ] Endpoint PATCH `/api/users/{id}/active` para ADMIN ativar/desativar usuários
- [ ] Implementar "Remember Me" (cookie de longa duração para Web clients)
- [ ] Rate limiting nos endpoints de autenticação (ex: máx 5 tentativas/min)
- [ ] Auditoria: log de login/logout/refresh com timestamps
- [ ] Suporte a OAuth2/OpenID Connect (delegação de autenticação)
- [ ] 2FA (Two-Factor Authentication) com TOTP

### Médio Prazo (Robustecer)

- [ ] Migrar banco H2 → PostgreSQL para production
- [ ] Configurar CORS para integração com frontend
- [ ] SSL/TLS (HTTPS) para comunicação segura
- [ ] Implementar refresh token claim (JTI - JWT ID) para rastreamento
- [ ] Suporte a social login (Google, GitHub, etc.)
- [ ] Testes de carga e segurança (OWASP)

### Longo Prazo (Escalar)

- [ ] Aplicar hexagonal pattern aos remaining modules
- [ ] CQRS para operações de leitura intensiva
- [ ] Event sourcing para auditoria completa
- [ ] Distributed tracing (OpenTelemetry)
- [ ] Migração para microserviços (Auth como serviço separado)

## 12. Resumo da Arquitetura em Diagrama

```
┌─────────────────────────────────────────────────────────────┐
│                        WEB LAYER                             │
│  AuthController ─────┐                                       │
│  UsersController     │ dependem de portas                    │
│  ApiExceptionHandler │ (não dependem de impl concretas)      │
└─────────┬────────────┴────────────────────────────────────────┘
          │ injeta
          ▼
┌─────────────────────────────────────────────────────────────┐
│              APPLICATION LAYER (Pura)                        │
│  AuthApplicationService implementa AuthUseCase              │
│  - Sem @Service annotation                                  │
│  - Depende de 5 portas de saída (abstrações)                │
│  - Lança exceptions de domínio                              │
└──┬────────┬─────────┬───────────┬──────────────────────────┐
   │        │         │           │                           │
   ▼ UserPort ▼ Authentication ▼ AccessToken ▼ RefreshToken  ▼ PasswordHash
┌──────────┐┌────────┐┌──────────┐┌────────────┐┌────────────┐
│ INFRASTRUCTURE    │ adapters que usam Spring │              │
│ UserPersistence  │ Repository   Security    │              │
│ SpringAuth    ◄──│ JwtService   Encoder    │              │
│ JwtAccessToken  │ RefreshTokenService     │              │
│ BCryptPassword  │ CustomUserDetailsService│              │
├──────────┤├────────┤├──────────┤├────────────┤├────────────┤
│ Spring   ││ Spring ││ JJWT     ││ JPA        ││ BCrypt     │
│ Data JPA ││Security││ Library  ││ Entities   ││ Encoder    │
└──────────┘└────────┘└──────────┘└────────────┘└────────────┘
      │            │              │              │
      ▼            ▼              ▼              ▼
   UserRepository  AuthenticationManager  RefreshTokenRepository
        │                    ▼
        └──────────────┬─────────────────┬─────────────┐
                       ▼                 ▼             ▼
                    ┌─────────────────────────────────┐
                    │  Database (H2 em memória)       │
                    │  - users                        │
                    │  - refresh_tokens               │
                    └─────────────────────────────────┘

DOMÍNIO (sem Spring):
  User, Role, RefreshToken entities
  ConflictException, NotFoundException, UnauthorizedException
  (Lógica pura, testável sem context)
```

## 13. Arquivos Principais

```
src/main/java/com/agrotech/system/
├── AgroTechSystemApplication.java
├── model/
│   ├── User.java
│   ├── Role.java
│   └── RefreshToken.java
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   └── AuthUseCase.java
│   │   └── out/
│   │       ├── UserPort.java
│   │       ├── AuthenticationPort.java
│   │       ├── AccessTokenPort.java
│   │       ├── RefreshTokenPort.java
│   │       └── PasswordHashPort.java
│   ├── service/
│   │   └── AuthApplicationService.java
│   └── exception/
│       ├── ConflictException.java
│       ├── NotFoundException.java
│       └── UnauthorizedException.java
├── infrastructure/
│   ├── persistence/
│   │   └── UserPersistenceAdapter.java
│   ├── security/
│   │   ├── SpringAuthenticationAdapter.java
│   │   ├── JwtAccessTokenAdapter.java
│   │   └── BCryptPasswordHashAdapter.java
├── repository/
│   ├── UserRepository.java
│   └── RefreshTokenRepository.java
├── service/
│   ├── JwtService.java
│   ├── RefreshTokenService.java
│   └── CustomUserDetailsService.java
├── config/
│   ├── ApplicationLayerConfig.java
│   ├── SecurityConfig.java
│   └── JwtAuthenticationFilter.java
├── controller/
│   ├── AuthController.java
│   ├── UsersController.java
│   └── ApiExceptionHandler.java
└── dto/
    ├── RegisterRequest.java
    ├── LoginRequest.java
    ├── RefreshRequest.java
    └── AuthResponse.java

src/main/resources/
└── application.yml (config de JWT, porta, banco)

src/test/java/.../
└── controller/
    ├── AuthControllerTest.java (13 testes)
    └── UsersControllerTest.java (14 testes)
```

---

**Status Atual:** ✅ 100% Hexagonal | ✅ 27/27 Testes Passando | ✅ Build SUCCESS

Dúvidas? Consulte as seções 4 (Fluxo) e 9 (Troubleshooting) acima.
