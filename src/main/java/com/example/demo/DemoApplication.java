package com.example.demo;

import org.apache.spark.sql.*;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.types.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.apache.spark.sql.functions.*;
import static org.apache.spark.sql.types.DataTypes.*;

@EnableScheduling // habilita o suporte para execução de tarefas agendadas
@SpringBootApplication // marca a classe como uma aplicação spring boot
public class DemoApplication {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(DemoApplication.class, args);

        // Inicia o processo de streaming do Spark
        startSparkStreaming();
    }

    // cria e configura a sessão do spark
    private static final SparkSession spark = SparkSession.builder()
            .appName("Meterologia em Streaming")

            .master("local[*]")
            // desabilita a interface web do Spark para esta aplicação, pois gera conflito com spring
            .config("spark.ui.enabled", false)
            .getOrCreate();

    // Define o esquema (estrutura) que o Spark usará para ler os dados JSON.
    // Isso é crucial no Structured Streaming, pois o Spark precisa saber a
    // estrutura dos dados antes de começar a lê-los.

    // define um schema para os dados meteriologicos
    private static final StructType schemaMetoriologico = new StructType()
            .add("cidade", StringType, false)
            .add("temperatura", DoubleType, false)
            .add("umidade", DoubleType, false)
            .add("timestamp", StringType, false); // Será convertido para TimestampType depois

    private static void startSparkStreaming() throws Exception {
        // O diretório monitorado (deve ser o mesmo usado pelo Scheduler)
        String caminhoFluxo = "src/main/resources/dados-meteriologicos";

        // 2. Leitura do Stream (Source)
        Dataset<Row> liveDataStream = spark.readStream()
                .schema(schemaMetoriologico)
                .option("maxFilesPerTrigger", 1) // Garante que cada arquivo é processado individualmente
                .json(caminhoFluxo); // Monitora a pasta para novos arquivos JSON

        // 3. Transformação: Detecção de Anomalias
        Dataset<Row> alertas = liveDataStream
                // Converte a string de timestamp para um tipo Timestamp
                .withColumn("timestamp_col", to_timestamp(col("timestamp")))
                // Filtra dados para gerar um ALERTA (Temperatura > 30°C)
                // .filter(col("temperatura").gt(30.0))
                .withColumn("alerta", lit("🚨 ALERTA: Temperatura Alta Detectada!"))
                .select( "cidade", "temperatura" ,"umidade" ,"timestamp_col");

        // 4. Carregamento (Sink)
        Dataset<Row> medias = alertas

                .groupBy() // faz agrupamento
                .agg(
                        avg(col("temperatura")).as("media_temperatura_acumulada"),
                        avg(col("umidade")).as("media_umidade_acumulada")
                );

        StreamingQuery query1 = alertas.writeStream()
                .outputMode("append")
                .format("console") // exibe os resultados no console
                .option("truncate", false) // exibe todas as colunas
                .start();

        StreamingQuery query2 = medias.writeStream()
                    .outputMode("complete")
                    .format("console") // Exibe os resultados no console
                    .option("truncate", false) // Exibe todas as colunas
                    .start();

        query1.awaitTermination(); // Mantém o processo do Spark rodando
        query2.awaitTermination();
    }
}