# Sistema de Votação da Cooperativa

API REST para gerenciamento de pautas e votações de uma cooperativa, desenvolvida com Spring Boot.  
A aplicação permite criar pautas, abrir sessões de votação, registrar votos e apurar os resultados.

---

## ✨ Funcionalidades

- **Gerenciamento de Pautas**  
  Criar, listar, atualizar e deletar pautas para votação.

- **Gerenciamento de Sessões**  
  Abrir sessões de votação para pautas, com tempo de duração configurável.

- **Registro de Votos**  
  Permitir que associados registrem seus votos (SIM/NÃO) durante uma sessão aberta.

- **Apuração de Resultados**  
  Obter o resultado da votação para cada sessão, com o total de votos SIM e NÃO.

- **Documentação da API**  
  Documentação interativa com Swagger/OpenAPI para facilitar o uso e teste da API.

---

## 🛠️ Tecnologias Utilizadas

- Java 17
- Spring Boot 3.5.0
- Spring Data JPA
- Maven
- PostgreSQL (Banco de dados de produção)
- H2 Database (Banco de dados para testes)
- Docker
- Springdoc OpenAPI (Swagger)

---

## ✅ Pré-requisitos

Antes de começar, garanta que você tenha instalado em sua máquina:

- Java Development Kit (JDK) 17
- Apache Maven
- Docker e Docker Compose

---

## ▶️ Como Executar a Aplicação

### 1. Clone o Repositório

```bash
git clone https://github.com/drahcir777/cooperativa-voting.git
cd cooperativa-voting
```

### 2. Execute a Aplicação Spring Boot

Utilize o Maven Wrapper (incluído no projeto) para iniciar a aplicação:

```bash
mvn clean package
```

---

> A aplicação estará disponível em: [http://localhost:8080](http://localhost:8080)

### 3. Inicie o Banco de Dados com Docker Compose

O projeto utiliza Docker Compose para configurar e iniciar o banco de dados PostgreSQL. Na raiz do projeto, execute:

```bash
docker-compose up -d
```

> Este comando irá baixar a imagem do PostgreSQL e iniciar um contêiner com o banco de dados `cooperativa_voting`.

---

## 📄 Documentação da API

A documentação da API é gerada automaticamente com o Springdoc OpenAPI e está acessível através do Swagger UI.

Após iniciar a aplicação, acesse no navegador:

- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

> Nesta interface interativa, você pode visualizar todos os endpoints disponíveis, parâmetros, exemplos de requisição e resposta, e até testar as chamadas diretamente pela interface.

---

## 🧪 Como Executar os Testes

Para executar a suíte de testes unitários e de integração, utilize o seguinte comando Maven:

```bash
./mvnw test
```

> **Importante:** Os testes de integração utilizam **Testcontainers** para instanciar um banco de dados PostgreSQL em um contêiner Docker. Portanto, certifique-se de que o **Docker** esteja em execução antes de rodar os testes.


