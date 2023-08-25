package org.auditTrial.functions;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Azure Functions with Event Grid trigger.
 */
public class AuditTrailListener1 {
    /**
     * This function will be invoked when an event is received from Event Grid.
     */
    @FunctionName("AuditTrailListener1")
    public void run(@EventGridTrigger(name = "eventGridEvent") String message,
                    final ExecutionContext context) {
        context.getLogger().info("Java Event Grid trigger function executed.");
        context.getLogger().info(message);


        sendToDatabase(message, context);
    }

    void sendToDatabase(String eventData, final ExecutionContext context) {
        String auditDbEndpoint =
                "https://pd-audittrial-cosmosdb.documents.azure.com:443/";
        String auditDbKey =
                "Lz3LYsQCvEYq6Mdn9MM1fyH19dH3tKpKC6K4rmxXX8GZ7xOEz1B9EWapJHB9daps2gxdGiLT6EFZACDbowhHIA==";

        String auditTrailDatabaseId = "audit-trial";
        String auditTrailContainerId = "audit-trial";

        //Setting Up the connection to the Database
        CosmosClient cosmosClient = new CosmosClientBuilder()
                .endpoint(auditDbEndpoint)
                .key(auditDbKey)
                .directMode()
                .buildClient();

        //gettting the database
        CosmosDatabase database = cosmosClient.getDatabase(auditTrailDatabaseId);

        //getting the container
        CosmosContainer container = database.getContainer(auditTrailContainerId);


        try {
            // takes message and maps to Json Object
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode eventJson = objectMapper.readTree(eventData);

            // Adds Json to container in Databse
            container.createItem(eventJson);
        } catch (Exception e) {
            // Handle exception appropriately
            context.getLogger().info(e.toString());
        } finally {

            cosmosClient.close();

        }
    }
}
