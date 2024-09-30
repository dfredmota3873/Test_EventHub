package com.weathe;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.azure.cosmos.*;
import com.azure.cosmos.models.*;
import com.azure.cosmos.util.CosmosPagedIterable;

import java.util.*;

public class WeatherDataApi {
    @FunctionName("GetWeatherData")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.GET},
                    authLevel = AuthorizationLevel.FUNCTION)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger function processed a request.");

        CosmosClient client = new CosmosClientBuilder()
                .endpoint(System.getenv("CosmosDBEndpoint"))
                .key(System.getenv("CosmosDBKey"))
                .buildClient();

        CosmosDatabase database = client.getDatabase("WeatherDB");
        CosmosContainer container = database.getContainer("WeatherData");

        List<Map<String, Object>> weatherDataList = new ArrayList<>();

        // Query the latest 10 weather data entries
        String query = "SELECT TOP 10 * FROM c ORDER BY c.timestamp DESC";
        CosmosPagedIterable<Map> queryResults = container.queryItems(query, new CosmosQueryRequestOptions(), Map.class);

        for (Map<String, Object> item : queryResults) {
            weatherDataList.add(item);
        }

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(weatherDataList)
                .build();
    }
}