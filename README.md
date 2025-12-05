# Projeto de Coleta e Processamento de Dados Meteorológicos com Spark

Este projeto demonstra a integração de um agendador de tarefas (Spring Boot) com o processamento de dados em tempo real (streaming) utilizando Apache Spark.

## Visão Geral

A aplicação é composta por duas partes principais que funcionam em conjunto:

1.  **Coletor de Dados Agendado**: Um componente Spring que, a cada 5 segundos, faz uma requisição à API [OpenWeatherMap](https://openweathermap.org/api) para obter os dados de temperatura e umidade da cidade de Guarabira, PB, Brasil. Esses dados são salvos em um novo arquivo JSON no diretório local `src/main/resources/dados-meteriologicos/`.

2.  **Processamento de Streaming com Spark**: Um job do Apache Spark Structured Streaming é iniciado junto com a aplicação. Ele monitora o diretório onde os dados são salvos. Assim que um novo arquivo JSON é adicionado, o Spark o lê e processa os dados de duas maneiras, dependendo do modo de execução:
    *   `alertas`: Exibe os dados de temperatura e umidade em tempo real, emitindo um alerta se a temperatura for igual ou superior a 30 graus.
    *   `medias`: Calcula a média de temperatura e umidade nas últimas 24 horas.

## Tecnologias Utilizadas

*   **Java 21**
*   **Spring Boot**: Utilizado para criar a aplicação e gerenciar o ciclo de vida dos componentes, incluindo o agendamento de tarefas.
*   **Apache Spark**: Utilizado para o processamento de dados em tempo real (Structured Streaming).
*   **Maven**: Para gerenciamento de dependências e build do projeto.
*   **OpenWeatherMap API**: Fonte externa para os dados de clima.

## Pré-requisitos

1.  **Java 21 ou superior** instalado.
2.  **Maven** instalado.
3.  Uma **chave de API (API Key)** do OpenWeatherMap. Você pode obter uma gratuitamente no site deles.

## Como Configurar e Executar

1.  **Clone o repositório:**
    ```bash
    git clone <URL_DO_REPOSITORIO>
    ```

2.  **Insira sua API Key:**
    Abra o arquivo `src/main/java/com/example/demo/AgendadorClimaApi.java` e substitua o valor da variável `apiKey` pela sua chave da API do OpenWeatherMap.

    ```java
    private String apiKey = "SUA_API_KEY";
    ```

3.  **Compile o projeto com Maven:**
    Use o Maven Wrapper para compilar o projeto.
    ```bash
    ./mvnw clean install
    ```

4.  **Execute a aplicação:**
    Você pode executar a aplicação em dois modos: `alertas` ou `medias`.

    *   **Modo Alertas:**
        Exibe os dados de temperatura e umidade em tempo real.

        ```bash
        ./mvnw spring-boot:run -Dspring-boot.run.arguments=alertas
        ```

        **Exemplo de Saída (alertas):**
        ```
        -------------------------------------------
        Batch: 0
        -------------------------------------------
        +--------------------------------------------------------------------------------------------------+
        |mensagem                                                                                          |
        +--------------------------------------------------------------------------------------------------+
        |Cidade: Guarabira, BR | Temperatura: 25.28 | Umidade: 87.0 | Timestamp: 2025-12-04 21:22:26.372287|
        +--------------------------------------------------------------------------------------------------+
        ```

    *   **Modo Médias:**
        Calcula a média de temperatura e umidade.

        ```bash
        ./mvnw spring-boot:run -Dspring-boot.run.arguments=medias
        ```

        **Exemplo de Saída (medias):**
        ```
        -------------------------------------------
        Batch: 0
        -------------------------------------------
        +------------------------------------------+-------------+-----------------+-------------+
        |window                                    |cidade       |media_temperatura|media_umidade|
        +------------------------------------------+-------------+-----------------+-------------+
        |{2025-12-04 21:00:00, 2025-12-05 21:00:00}|Guarabira, BR|25.28            |87.0         |
        +------------------------------------------+-------------+-----------------+-------------+
        ```

## Estrutura do Projeto

*   `src/main/java/com/example/demo/DemoApplication.java`: Classe principal que inicializa o Spring Boot e o job de streaming do Spark.
*   `src/main/java/com/example/demo/AgendadorClimaApi.java`: Componente que busca os dados da API OpenWeatherMap de forma agendada.
*   `src/main/resources/dados-meteriologicos/`: Diretório monitorado pelo Spark, onde os dados de clima são salvos em formato JSON.
*   `pom.xml`: Arquivo de configuração do Maven com todas as dependências do projeto (Spring, Spark, etc.).