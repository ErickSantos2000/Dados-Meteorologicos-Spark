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

        String modo = (args.length > 0) ? args[0] : "alertas";

        startSparkStreaming(modo);
    }

    private static final SparkSession spark = SparkSession.builder()
            .appName("Meterologia em Streaming")
            .master("local[*]")
            .config("spark.ui.enabled", false)
            .getOrCreate();

    private static final StructType schemaMetoriologico = new StructType()
            .add("cidade", StringType, false)
            .add("temperatura", DoubleType, false)
            .add("umidade", DoubleType, false)
            .add("timestamp", StringType, false);


    private static void startSparkStreaming(String modo) throws Exception {

        String caminhoFluxo = "src/main/resources/dados-meteriologicos";

        Dataset<Row> liveDataStream = spark.readStream()
                .schema(schemaMetoriologico)
                .option("maxFilesPerTrigger", 1)
                .json(caminhoFluxo);

        // ALERTAS
        if (modo.equalsIgnoreCase("alertas")) {

            Dataset<Row> alertas = liveDataStream
                    .withColumn("timestamp_col", to_timestamp(col("timestamp")))
                    .withColumn(
                            "mensagem",
                            when(col("temperatura").geq(30.0),
                                    concat(
                                            lit("ALERTA: Temperatura Alta! "),
                                            lit(" | Cidade: "), col("cidade"),
                                            lit(" | Temperatura: "), col("temperatura")
                                    )
                            ).otherwise(
                                    concat(
                                            lit("Cidade: "), col("cidade"),
                                            lit(" | Temperatura: "), col("temperatura"),
                                            lit(" | Umidade: "), col("umidade"),
                                            lit(" | Timestamp: "), col("timestamp_col")
                                    )
                            )
                    )
                    .select("mensagem");

            StreamingQuery queryAlerta = alertas.writeStream()
                    .outputMode("append")
                    .format("console")
                    .option("truncate", false)
                    .start();

            queryAlerta.awaitTermination();
        }

        // MEDIAS
        if (modo.equalsIgnoreCase("medias")) {

            Dataset<Row> medias = liveDataStream
                    .groupBy("cidade")
                    .agg(
                            avg(col("temperatura")).as("media_temperatura_acumulada"),
                            avg(col("umidade")).as("media_umidade_acumulada")
                    );

            StreamingQuery queryMedias = medias.writeStream()
                    .outputMode("complete")
                    .format("console")
                    .option("truncate", false)
                    .start();

            queryMedias.awaitTermination();
        }
    }
}
