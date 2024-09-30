package com.weathe;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.azure.cosmos.*;
import com.azure.cosmos.models.*;

import java.util.*;

public class WeatherDataProcessor {
    @FunctionName("WeatherDataProcessor")
    public void run(
            @EventHubTrigger(
                    name = "message",
                    eventHubName = "weather-data",
                    connection = "EventHubConnectionString") String message,
            final ExecutionContext context
    ) {
        context.getLogger().info("Weather data received: " + message);

        CosmosClient client = new CosmosClientBuilder()
                .endpoint(System.getenv("CosmosDBEndpoint"))
                .key(System.getenv("CosmosDBKey"))
                .buildClient();

        CosmosDatabase database = client.getDatabase("WeatherDB");
        CosmosContainer container = database.getContainer("WeatherData");

        // Create a new document
        Map<String, Object> weatherDataDocument = new HashMap<>();
        weatherDataDocument.put("id", UUID.randomUUID().toString());
        weatherDataDocument.put("data", message);
        weatherDataDocument.put("timestamp", new Date());

        // Insert the document
        container.createItem(weatherDataDocument);

        context.getLogger().info("Weather data saved to Cosmos DB.");
    }
}