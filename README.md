# Processamento Apache Spark com Spring Boot

## Introdução
Este projeto integra o Apache Spark com uma aplicação Spring Boot para processamento de dados.

## Requisitos
Para construir e executar esta aplicação, você precisa:
- Java 17 ou mais recente
- Maven 3.6.3 ou mais recente

## Tecnologias Utilizadas
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Apache Spark](https://spark.apache.org/)

## Primeiros Passos

### Configuração do Projeto (pom.xml)

Para garantir a compatibilidade e a execução correta do Apache Spark com o Spring Boot em Java 17, foram realizadas as seguintes alterações no arquivo `pom.xml`:

1.  **Configuração de Argumentos JVM para o Spring Boot Maven Plugin:**
    Adicionado `jvmArguments` ao `spring-boot-maven-plugin` para resolver `IllegalAccessError` ao acessar APIs internas do Java 17.

    ```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <jvmArguments>--add-opens java.base/sun.nio.ch=ALL-UNNAMED</jvmArguments>
                </configuration>
            </plugin>
        </plugins>
    </build>
    ```

2.  **Configuração de Argumentos JVM para o Maven Surefire Plugin (Testes):**
    Adicionado `argLine` ao `maven-surefire-plugin` para passar os mesmos argumentos JVM durante a execução dos testes, evitando o `IllegalAccessError` também nos testes.

    ```xml
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <configuration>
            <argLine>--add-opens java.base/sun.nio.ch=ALL-UNNAMED</argLine>
        </configuration>
    </plugin>
    ```

3.  **Dependências do Apache Spark e Servlet API:**
    As dependências para `spark-core` e `spark-sql` foram adicionadas. Além disso, para resolver problemas de `NoClassDefFoundError` relacionados à API Servlet, a dependência `javax.servlet-api` foi explicitamente incluída. A dependência `jakarta.servlet-api` é utilizada pelo Spring Boot 3.x, mas `javax.servlet-api` é necessária para compatibilidade com componentes internos do Spark, como sua UI (mesmo que desabilitada).

    ```xml
    <dependencies>
        <!-- ... outras dependências ... -->

        <dependency>
            <groupId>org.apache.spark</groupId>
            <artifactId>spark-core_2.13</artifactId>
            <version>3.5.3</version>
        </dependency>

        <dependency>
            <groupId>org.apache.spark</groupId>
            <artifactId>spark-sql_2.13</artifactId>
            <version>3.5.3</version>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
            <version>3.1.0</version>
            <scope>compile</scope>
        </dependency>

    </dependencies>
    ```

### Configuração da Aplicação (DemoApplication.java)

Para evitar conflitos relacionados à inicialização da UI do Spark, a UI foi explicitamente desativada na criação do `SparkSession`:

```java
@SpringBootApplication
public class DemoApplication {

    private static final SparkSession spark = SparkSession.builder()
            .appName("Demo")
            .master("local[*]")
            .config("spark.ui.enabled", false) // Desativa a UI do Spark
            .getOrCreate();

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);

        // Exemplo de uso do Spark:
        String path = "/home/erick/Downloads/demo/src/main/resources/dados.csv";
        Dataset<Row> df = spark.read().csv(path);
        df.show();
    }
}
```

### Compilar e Instalar o Projeto
Após realizar as alterações no `pom.xml` e no `DemoApplication.java`, execute o seguinte comando para compilar o projeto e instalar as dependências:

```bash
./mvnw clean install
```

### Executar a Aplicação
Você pode executar a aplicação diretamente da linha de comando usando o Maven:

```bash
./mvnw spring-boot:run
```
Alternativamente, após a construção, você pode executar o arquivo JAR:
```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### Executar Testes
Para executar os testes unitários e de integração, use o seguinte comando:

```bash
./mvnw test
```