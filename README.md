
# 🛒 Order Service

O **Order Service** é um microserviço baseado em **Spring Boot**, projetado para gerenciar pedidos utilizando **AWS SQS** e **DynamoDB**. Ele suporta execução local e em **Docker**, integrando-se ao **LocalStack** para simular serviços da AWS em ambiente de desenvolvimento, o order service utiliza **Java 21** e **Spring Boot**, alem disso contamos com os teste unitários e a documentação baseado em **OpenAPI** utilizando o **Swagger**. O order também está preparado com **Docker**.

---

## 🚀 Arquitetura

O serviço segue a arquitetura **event-driven** e **microservices**, processando pedidos de forma assíncrona via SQS.

### 🏗 Componentes

1. **Order API (Spring Boot - RESTful API)**
    - Consulta de  pedidos via HTTP.

2. **SQS Consumer (Listener de Pedidos)**
    - Consome mensagens da fila **order-queue** e armazena no **DynamoDB**.

3. **DynamoDB (Banco NoSQL)**
    - Mantém registros de pedidos para consultas e relatórios.

4. **LocalStack (Simulação AWS)**
    - Emula **DynamoDB** e **SQS** para desenvolvimento local.

---

## 📜 Estrutura Arquitetural


```plaintext
            ┌──────────────────────┐
            │  Cliente B │
            └──────────▲───────────┘
                       │ HTTP Request (Consulta)
                       ▼
            ┌──────────────────────┐
            │      Order API       │
            │    (Spring Boot)     │
            │  Consulta DynamoDB   │
            └──────────▲───────────┘
                       │
               ┌───────┴───────┐
               │   DynamoDB    │
               │ (Armazena pedidos) │
               └───────▲───────┘
                       │
        ┌──────────────┴──────────────┐
        │        Order Listener        │
        │   (Consumer SQS - Spring Boot) │
        │ Consome e grava no DynamoDB   │
        └───────────▲──────────────────┘
                    │
        ┌───────────┴───────────┐
        │        SQS (Cliente A)            │
        │  (Fila de Pedidos)    │
        └───────────────────────┘
```

### 🛠 Como Rodar o Serviço

#### 🏠 Rodando Localmente

Você pode rodar o serviço localmente com o **LocalStack** simulando os serviços da AWS.

1. Clone o repositório:  
   `
   git clone https://github.com/seu-repositorio/order-service.git
   cd order-service

2. Inicie o **LocalStack** para simular a AWS, abaixo explico com executar de forma simples o localstack:
 ```sh
   localstack start
   ```

4. Execute o **Order Service** localmente com Maven:
   ```sh
   mvn clean install
   mvn spring-boot:run
   ```

---

### 🐳 **Rodando com Docker Compose**

Para facilitar a execução local, utilize **Docker Compose** para subir o **LocalStack** e rodar o **Order Service**.

1. **Na raiz do projetos temos o  arquivo `docker-compose.yml`** com o docker e docker-compose pré instalados execute o comando abaixo na pasta do projeto

3. **Rodar os serviços com Docker Compose:**
   ```sh
   docker-compose up -d
   ```

4. O serviço estará disponível em `http://localhost:8080`

---

## 🛠  **Comandos Utéis**
   ```sh
    Criacação da tabela no DynamoDB
    
    aws --endpoint-url=http://localhost:4566 dynamodb create-table --table-name Orders --attribute-definitions AttributeName=id,AttributeType=S --key-schema AttributeName=id,KeyType=HASH --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
   
   
    Criação de fila no SQS
    
    aws --endpoint-url=http://localhost:4566 sqs create-queue --queue-name order-amount-queue
    aws --endpoint-url=http://localhost:4566 sqs create-queue --queue-name order-queue
   
   ```




## 📡 **Testando a API**

1. **Verificar se a API está rodando:**
   ```sh
   curl -X GET http://localhost:8080/health
   ```

2. **Verificar a documentação da API **
   ```sh
	http://localhost:8080/swagger-ui/index.html
   ```    

3. **Consultar pedidos armazenados no DynamoDB:**
   ```sh
   curl -X GET http://localhost:8080/orders/?page=?& limit=?
   ```

---

## 🎯 **Conclusão**

Este **Order Service** é um microserviço **event-driven**, que processa pedidos de forma assíncrona usando **SQS** e **DynamoDB**. Ele pode ser rodado localmente, com **Docker Compose** ou via **Docker Run**, garantindo facilidade no desenvolvimento e testes.