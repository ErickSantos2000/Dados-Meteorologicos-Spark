# Projeto de Coleta e Processamento de Dados Meteorológicos com Spark

Este projeto demonstra a integração de um agendador de tarefas (Spring Boot) com o processamento de dados em tempo real (streaming) utilizando Apache Spark.

## Visão Geral

A aplicação é composta por duas partes principais que funcionam em conjunto:

1.  **Coletor de Dados Agendado**: Um componente Spring que, a cada 5 segundos, faz uma requisição à API [OpenWeatherMap](https://openweathermap.org/api) para obter os dados de temperatura e umidade da cidade de Guarabira, PB, Brasil. Esses dados são salvos em um novo arquivo JSON no diretório local `src/main/resources/dados-meteriologicos/`.

2.  **Processamento de Streaming com Spark**: Um job do Apache Spark Structured Streaming é iniciado junto com a aplicação. Ele monitora o diretório onde os dados são salvos. Assim que um novo arquivo JSON é adicionado, o Spark o lê, processa os dados para calcular a média acumulada de temperatura e umidade, e exibe o resultado no console.

## Tecnologias Utilizadas

*   **Java 17**
*   **Spring Boot**: Utilizado para criar a aplicação e gerenciar o ciclo de vida dos componentes, incluindo o agendamento de tarefas.
*   **Apache Spark**: Utilizado para o processamento de dados em tempo real (Structured Streaming).
*   **Maven**: Para gerenciamento de dependências e build do projeto.
*   **OpenWeatherMap API**: Fonte externa para os dados de clima.

## Pré-requisitos

1.  **Java 17 ou superior** instalado.
2.  **Maven** instalado.
3.  Uma **chave de API (API Key)** do OpenWeatherMap. Você pode obter uma gratuitamente no site deles.

## Como Configurar e Executar

1.  **Clone o repositório:**
    ```bash
    git clone <URL_DO_REPOSITORIO>
    cd Clima-Metorologio
    ```

2.  **Crie o diretório para os dados:**
    O Spark precisa que o diretório de leitura exista antes de iniciar.
    ```bash
    mkdir -p src/main/resources/dados-meteriologicos
    ```

3.  **Insira sua API Key:**
    Abra o arquivo `src/main/java/com/example/demo/AgendadorClimaApi.java` e substitua o placeholder `"SUA_API_KEY"` pela sua chave da API do OpenWeatherMap.

    ```java
    // Linha 24 (aproximadamente)
    private final String apiKey = "SUA_API_KEY";
    ```

4.  **Recompile o projeto com Maven:**
    Use o Maven Wrapper para compilar e executar o projeto.
    ```bash
    # compila o projeto com Maven
    ./mvnw clean install
    
    # executa o projeto spring boot
    # aparece informações de temperatura e possivel alerte se temperatura estiver acima de 30 graus
    ./mvnw spring-boot:run -Dspring-boot.run.arguments=alertas
    
    # aparece as medias de temperatura por cidade
    ./mvnw spring-boot:run -Dspring-boot.run.arguments=medias
    ```

Após a inicialização, você verá no console a saída do Spark sendo atualizada a cada 5 segundos com a média de temperatura e umidade, à medida que novos dados são coletados e processados.

## Estrutura do Projeto

*   `src/main/java/com/example/demo/DemoApplication.java`: Classe principal que inicializa o Spring Boot e o job de streaming do Spark.
*   `src/main/java/com/example/demo/AgendadorClimaApi.java`: Componente que busca os dados da API OpenWeatherMap de forma agendada.
*   `src/main/resources/dados-meteriologicos/`: Diretório monitorado pelo Spark, onde os dados de clima são salvos em formato JSON.
*   `pom.xml`: Arquivo de configuração do Maven com todas as dependências do projeto (Spring, Spark, etc.).
