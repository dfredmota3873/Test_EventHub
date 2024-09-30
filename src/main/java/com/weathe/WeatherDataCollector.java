package com.weathe;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.azure.messaging.eventhubs.*;
import java.time.*;
import java.net.http.*;
import java.net.URI;

public class WeatherDataCollector {
    @FunctionName("WeatherDataCollector")
    public void run(
            @TimerTrigger(name = "timerInfo", schedule = "0 */2 * * * *") String timerInfo,
            final ExecutionContext context
    ) {
        context.getLogger().info("Weather data collector function executed at: " + LocalDateTime.now());

        try {
            // Fetch data from OpenWeatherMap API
            String apiKey = System.getenv("OpenWeatherMapApiKey");
            String city = "London"; // Example city
            String url = String.format("http://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s", city, apiKey);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String weatherData = response.body();

            // Send data to Event Hub
            EventHubProducerClient producer = new EventHubClientBuilder()
                    .connectionString(System.getenv("EventHubConnectionString"))
                    .eventHubName(System.getenv("EventHubName"))
                    .buildProducerClient();

            EventDataBatch batch = producer.createBatch();
            batch.tryAdd(new EventData(weatherData));
            producer.send(batch);
            producer.close();

            context.getLogger().info("Weather data sent to Event Hub: " + weatherData);
        } catch (Exception e) {
            context.getLogger().severe("Error occurred: " + e.getMessage());
        }
    }
}
