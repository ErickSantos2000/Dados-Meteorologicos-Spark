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