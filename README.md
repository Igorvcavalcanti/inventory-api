# Inventory API

API REST desenvolvida em **Java 21** e **Spring Boot 4**, responsável por gerenciar **produtos** e **movimentações de estoque**, aplicando boas práticas de arquitetura em camadas, validação, persistência com JPA e tratamento global de erros.

O projeto simula um **sistema básico de controle de estoque**, comum em ERPs, e-commerce e sistemas internos corporativos.

---

## 🎯 Objetivos do Projeto

Este projeto foi criado para consolidar e demonstrar:

- Criação de APIs REST seguindo padrões de mercado
- Arquitetura em camadas (Controller, Service, Repository, DTO)
- Persistência com **Spring Data JPA**
- Uso de **DTOs** para isolamento do domínio
- Validações com **Bean Validation**
- Tratamento global de exceções
- Modelagem de regras de negócio (entrada e saída de estoque)
- Preparação para ambientes reais (H2 / PostgreSQL)

---

## ✨ Funcionalidades

### 📦 Produtos
- Cadastro de produtos
- Consulta de produtos
- Controle de estoque atual
- Validação de dados de entrada

### 🔄 Movimentações de Estoque
- Entrada de estoque
- Saída de estoque
- Validação de saldo disponível
- Registro histórico de movimentações
- Enum para tipo de movimentação (`IN`, `OUT`)

### 🛡️ Confiabilidade
- Tratamento global de erros com `@RestControllerAdvice`
- Exceções de domínio (`ProductNotFoundException`, `InsufficientStockException`)
- Respostas de erro padronizadas

---

## 🧱 Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 4.0.1**
- Spring Web (REST / MVC)
- Spring Data JPA
- Bean Validation (Jakarta Validation)
- Lombok
- H2 Database (desenvolvimento)
- Maven
- JUnit 5
- Jacoco (cobertura de testes)

---

## 📁 Estrutura do Projeto

```
src/main/java/com/igorcavalcanti/inventory_api
│
├── config
│
├── exception
│   ├── ApiError.java
│   └── GlobalExceptionHandler.java
│
├── product
│   ├── controller
│   │   └── ProductController.java
│   ├── dto
│   │   ├── request
│   │   │   └── ProductRequest.java
│   │   └── response
│   │       └── ProductResponse.java
│   ├── entity
│   │   └── Product.java
│   ├── repository
│   │   └── ProductRepository.java
│   └── service
│       ├── ProductService.java
│       └── ProductNotFoundException.java
│
├── stockmovement
│   ├── controller
│   │   └── StockMovementController.java
│   ├── dto
│   │   ├── request
│   │   │   └── StockMovementRequest.java
│   │   └── response
│   │       └── StockMovementResponse.java
│   ├── entity
│   │   ├── StockMovement.java
│   │   └── StockMovementType.java
│   ├── repository
│   │   └── StockMovementRepository.java
│   └── service
│       ├── StockMovementService.java
│       └── InsufficientStockException.java
│
└── InventoryApiApplication.java
```

---

## 🛠️ Como Executar o Projeto

### Pré-requisitos

- Java 21+
- Maven 3.9+
- IDE com suporte a Lombok (IntelliJ / Eclipse)

---

### Executar via terminal

```bash
mvn spring-boot:run
```

Ou execute diretamente a classe:

```
InventoryApiApplication.java
```

A aplicação estará disponível em:

```
http://localhost:8080
```

---

## 📌 Endpoints Principais

### 📦 Produtos

#### Criar produto
`POST /products`

Exemplo de request:
```json
{
  "name": "Notebook Dell",
  "price": 4500.00,
  "quantity": 10
}
```

---

#### Listar produtos
`GET /products`

---

### 🔄 Movimentações de Estoque

#### Registrar movimentação
`POST /stock-movements`

Exemplo de request:
```json
{
  "productId": 1,
  "type": "OUT",
  "quantity": 2
}
```

---

## ❗ Tratamento de Erros

Exemplo de erro de negócio:

```json
{
  "status": 400,
  "message": "Insufficient stock for product ID 1"
}
```

Todos os erros são centralizados em um **handler global**, garantindo respostas consistentes.

---

## 🧠 Arquitetura (Resumo)

- **Controller**  
  Responsável apenas por receber e responder requisições HTTP.

- **Service**  
  Contém regras de negócio, validações e orquestração.

- **Repository**  
  Abstração de acesso a dados com Spring Data JPA.

- **DTOs**  
  Isolam a API do modelo de domínio.

- **Exception**  
  Centraliza erros técnicos e de negócio.

Essa separação facilita **testes**, **manutenção** e **evolução** do sistema.

---

## 🧩 Possíveis Melhorias Futuras (Roadmap)

- Paginação e ordenação de endpoints
- Auditoria de movimentações (data, usuário)
- Dockerização da aplicação
- Versionamento de API (`/v1`)
- Autenticação e autorização (Spring Security + JWT)

---

## 📝 Licença

Projeto livre para uso educacional e profissional.

---

## 🧑‍💻 Autor

**Igor Cavalcanti**  
Desenvolvedor Java | Spring Boot | Golang | APIs & Microsserviços  
🔗 LinkedIn: https://www.linkedin.com/in/igorvcavalcanti/
