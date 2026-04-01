# AgroTech Auth API (Estrutura Atual Simplificada)

Este projeto esta no modo simplificado de autenticacao, com foco em cadastro, login e consulta de usuario.

## Stack Atual

- Java 21
- Spring Boot 3.3.x
- Spring Web
- Spring Security (configuracao simplificada)
- Spring Data JPA
- H2 em memoria

## Estrutura do Projeto

```text
src/main/java/com/agrotech/system
├── AgroTechSystemApplication.java
├── config/
│   └── SecurityConfig.java
├── controller/
│   └── AuthController.java
├── model/
│   ├── Role.java
│   └── User.java
├── repository/
│   └── UserRepository.java
└── service/
    └── AuthService.java

src/main/resources
└── application.yml
```

## Como Executar

```bash
mvn spring-boot:run
```

Base URL: `http://localhost:8080`

## Endpoints Disponiveis

### 1. Registrar usuario

- Metodo: `POST`
- URL: `/api/auth/register`

Body JSON:

```json
{
  "name": "Operador 1",
  "email": "operador@agrotech.com",
  "password": "123456",
  "role": "OPERADOR"
}
```

Exemplos de role na chamada de cadastro:

```json
{
  "name": "Operador 1",
  "email": "operador@agrotech.com",
  "password": "123456",
  "role": "OPERADOR"
}
```

```json
{
  "name": "Especialista 1",
  "email": "especialista@agrotech.com",
  "password": "123456",
  "role": "ESPECIALISTA"
}
```

```json
{
  "name": "Admin 1",
  "email": "admin@agrotech.com",
  "password": "123456",
  "role": "ADMIN"
}
```

Resposta: `201 Created`

```json
{
  "id": 1,
  "name": "Operador 1",
  "email": "operador@agrotech.com",
  "role": "OPERADOR",
  "enabled": true,
  "username": "operador@agrotech.com",
  "accountNonLocked": true,
  "credentialsNonExpired": true,
  "accountNonExpired": true,
  "authorities": [
    {
      "authority": "ROLE_OPERADOR"
    }
  ]
}
```

Observacao: o campo `password` nao e retornado (foi protegido com `@JsonIgnore`).

### 2. Login

- Metodo: `POST`
- URL: `/api/auth/login`

Body JSON:

```json
{
  "email": "operador@agrotech.com",
  "password": "123456"
}
```

Resposta: `200 OK`

Retorna o objeto do usuario autenticado (mesmo formato do register, sem senha).

### 3. Buscar usuario (me)

- Metodo: `GET`
- URL: `/api/auth/me?email=operador@agrotech.com`

Resposta: `200 OK`

Retorna o usuario pelo email informado.

### 4. Refresh

- Metodo: `POST`
- URL: `/api/auth/refresh`

Status atual: endpoint desativado na versao simplificada.

Resposta: `501 Not Implemented`

## Regras Atuais

- Roles suportadas: `OPERADOR`, `ESPECIALISTA`, `ADMIN`
- Endpoints de auth liberados via `SecurityConfig`
- Nao ha JWT ativo nesta versao simplificada

## Exemplo rapido (cURL)

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Operador 1","email":"operador@agrotech.com","password":"123456","role":"OPERADOR"}'
```

Exemplos cURL com roles diferentes:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Especialista 1","email":"especialista@agrotech.com","password":"123456","role":"ESPECIALISTA"}'
```

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Admin 1","email":"admin@agrotech.com","password":"123456","role":"ADMIN"}'
```
