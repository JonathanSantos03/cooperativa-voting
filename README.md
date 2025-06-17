

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
git clone https://github.com/JonathanSantos03/cooperativa-voting.git
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

O projeto possui uma suíte completa de **110 testes** organizados em diferentes categorias para garantir a qualidade e confiabilidade do código.

### 📊 Cobertura de Testes

- **Testes Unitários:** 63 testes
  - Services: 30 testes
  - Controllers: 33 testes
- **Testes de Integração:** 47 testes  
  - Repositories: 29 testes
  - End-to-End: 18 testes

### 🚀 Comandos para Executar os Testes

#### Executar todos os testes
```bash
./mvnw test
```

#### Executar apenas testes unitários dos services
```bash
./mvnw test -Dtest="*ServiceTest"
```

#### Executar apenas testes unitários dos controllers
```bash
./mvnw test -Dtest="*ControllerTest"
```

#### Executar apenas testes de integração dos repositories
```bash
./mvnw test -Dtest="*RepositoryTest"
```

#### Executar apenas testes de integração end-to-end
```bash
./mvnw test -Dtest="*IntegrationTest"
```

#### Executar um teste específico
```bash
./mvnw test -Dtest="PautaServiceTest"
```

### 🎯 Tipos de Testes

#### **Testes Unitários**
- **PautaServiceTest:** Testa a lógica de negócio das pautas
- **SessaoServiceTest:** Testa a lógica de negócio das sessões  
- **VotoServiceTest:** Testa a lógica de negócio dos votos
- **Controllers:** Testam os endpoints REST com mocks

#### **Testes de Integração**
- **Repositories:** Testam as consultas JPA com banco H2 em memória
- **End-to-End:** Testam fluxos completos da API com MockMvc

### ✅ Cenários Testados

Os testes cobrem todos os cenários principais:

- ✅ **Cenários de sucesso** - operações válidas
- ✅ **Validação de dados** - campos obrigatórios e formatos
- ✅ **Regras de negócio** - títulos únicos, sessões ativas, votos duplicados
- ✅ **Tratamento de erros** - recursos não encontrados, dados inválidos
- ✅ **Integração completa** - fluxos end-to-end de pautas → sessões → votos

### ⚙️ Configuração de Teste

- **Base de dados:** H2 em memória para testes
- **Perfil:** `test` com configurações específicas  
- **Frameworks:** JUnit 5, Mockito, Spring Boot Test, AssertJ
- **MockMvc:** Para testes de controllers e integração

> **Nota:** Os testes utilizam **H2 Database** em memória, portanto não é necessário Docker para executá-los. Todos os testes são independentes e podem ser executados em qualquer ordem.
