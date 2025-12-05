package com.example.demo;

import org.apache.spark.sql.*;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.types.*;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.apache.spark.sql.functions.*;
import static org.apache.spark.sql.types.DataTypes.*;

@EnableScheduling
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) throws Exception {

        SpringApplication.run(DemoApplication.class, args);

        String modo = "alertas";

        if(args.length > 0){
            modo = args[0];
        }

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


        if (modo.equalsIgnoreCase("alertas")) {

            Dataset<Row> alertas = liveDataStream
                    .withColumn("timestamp_col", to_timestamp(col("timestamp")))

                    .withColumn(
                            "mensagem",
                            when(col("temperatura").geq(30.0),
                                    concat(
                                            lit("Cidade: "), col("cidade"),
                                            lit(" | Temperatura: "), col("temperatura"),
                                            lit(" | Umidade: "), col("umidade"),
                                            lit(" | Timestamp: "), col("timestamp_col"),
                                            lit(" | ALERTA: Temperatura Alta! ")
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


        if (modo.equalsIgnoreCase("medias")) {

            Dataset<Row> medias = liveDataStream
                    .withColumn("timestamp_col", to_timestamp(col("timestamp")))
                    .groupBy(
                            window(col("timestamp_col"), "24 hours"),
                            col("cidade")
                    )
                    .agg(
                            avg(col("temperatura")).as("media_temperatura"),
                            avg(col("umidade")).as("media_umidade")
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