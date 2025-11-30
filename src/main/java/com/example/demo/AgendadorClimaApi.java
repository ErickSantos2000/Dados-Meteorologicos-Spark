package com.example.demo;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.io.FileWriter;

import java.time.Instant;
import java.util.Locale;

@Component
public class AgendadorClimaApi {

    private String apiKey = "c76d2911c7189b2fbc7429e2d49620e7";
    private String urlApi = "http://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric";
    private String cidade = "Guarabira, BR";

    // O diretório que o Spark vai monitorar. Crie esta pasta em 'src/main/resources'
    private String caminhoDadosMeteriolicos = "src/main/resources/dados-meteriologicos";
    private RestTemplate restTemplate = new RestTemplate();

    // Roda esta função a cada 5 segundos
    @Scheduled(fixedRate = 5000)
    public void buscaDadosMeteorologicos() {
        try {
            // consulta a API
            String url = String.format(urlApi, cidade, apiKey);
            String rawJson = restTemplate.getForObject(url, String.class);

            // é criado extraido o JSON e criamos um objeto Data Transfer
            if (rawJson != null) {
                double temperatura = extracaoValores(rawJson, "\"temp\":", ","); // Exemplo simples de extração
                double humidade = extracaoValores(rawJson, "\"humidity\":", ",");

                // formata o dado para o Spark (um JSON simples por linha)
                String sparkJson = String.format(
                        Locale.US,
                        "{\"cidade\":\"%s\", \"temperatura\":%f, \"umidade\":%f, \"timestamp\":\"%s\"}\n",
                        cidade, temperatura, humidade, Instant.now().toString()
                );

                // escreve no diretório monitorado (Cria um novo arquivo a cada consulta)
                String nomeArquivo = caminhoDadosMeteriolicos + "/meteriologico-" + System.currentTimeMillis() + ".json";

                try (FileWriter writer = new FileWriter(nomeArquivo)) {
                    writer.write(sparkJson);
                    System.out.println("[Spring] novo arquivo JSON criado: " + nomeArquivo);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar dados do clima: " + e.getMessage());
        }
    }

    // função utilitária simples para extrair valores do JSON
    private double extracaoValores(String json, String chaveInicio, String chaveFim) {
        try {
            int inicia = json.indexOf(chaveInicio) + chaveInicio.length();
            int fim = json.indexOf(chaveFim, inicia);
            String valueStr = json.substring(inicia, fim).trim();
            return Double.parseDouble(valueStr);
        } catch (Exception e) {
            return 0.0; // valor padrão em caso de falha na extração
        }
    }
}