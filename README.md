# Processamento Apache Spark com Spring Boot

## Introdução
Este projeto integra o Apache Spark com uma aplicação Spring Boot para processamento de dados.

## Requisitos
Para construir e executar esta aplicação, você precisa:
- Java 17 ou mais recente
- Maven 3.5.3 ou mais recente

## Tecnologias Utilizadas
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Apache Spark](https://spark.apache.org/)

## Primeiros Passos

### Dependências do Apache Spark
Atualmente, o projeto inclui apenas a configuração básica do Spring Boot. Para habilitar as funcionalidades do Apache Spark, as seguintes dependências precisam ser adicionadas ao seu `pom.xml`:

```xml
<!-- https://mvnrepository.com/artifact/org.apache.spark/spark-core -->
<dependency>
    <groupId>org.apache.spark</groupId>
    <artifactId>spark-core_2.13</artifactId>
    <version>3.5.3</version>
</dependency>

<!-- https://mvnrepository.com/artifact/org.apache.spark/spark-sql -->
<dependency>
    <groupId>org.apache.spark</groupId>
    <artifactId>spark-sql_2.13</artifactId>
    <version>3.5.3</version>
    <scope>provided</scope>
</dependency>
```

Após adicionar essas dependências, você deve recompilar seu `pom.xml` usando o Maven para garantir que elas sejam instaladas corretamente:

```bash
mvn install
```

### Construir a aplicação
Para compilar o projeto e empacotá-lo em um arquivo JAR, execute o seguinte comando:
```bash
./mvnw clean install
```

### Executar a aplicação
Você pode executar a aplicação diretamente da linha de comando usando o Maven:
```bash
./mvnw spring-boot:run
```
Alternativamente, após a construção, você pode executar o arquivo JAR:
```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### Executar testes
Para executar os testes unitários e de integração, use o seguinte comando:
```bash
./mvnw test
```

Aplicação Spark:

package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

@SpringBootApplication
public class DemoApplication {

    /* [Cria SparkSession]
     *  é apenas o construtor, ou seja, um builder vazio
     *  ele não cria a sessão
     *  ele so começa a configuração da sessão Spark
     *  ele so começa a configuração do Spark
     * */
    private static final SparkSession spark = SparkSession.builder()
            // Você abre a caixa de ferramentas.
            // Mas ainda não construiu nada.
            // Depois que você chama builder(), você configura a sessão:

            .appName("Demo") // define o nome do app
            .master("local[*]") //diz para rodar localmente usando todos os cores disponíveis

            // Cria a SparkSession se ela ainda não existir
            // Retorna a SparkSession já existente (reutiliza)
            .getOrCreate(); // cria (ou retorna, se já existir) a sessão Spark, instanciando internamente

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);

        // Caminho do CSV
        String path = "/home/erick/Downloads/demo/src/main/resources/dados.csv";

        // Ler o CSV
        Dataset<Row> df = spark.read().csv(path);

        df.show();
    }

}