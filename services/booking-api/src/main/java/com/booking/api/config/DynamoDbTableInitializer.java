package com.booking.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;

/**
 * Creates the Bookings table and its GSI on startup when running against DynamoDB Local for dev purpose.
 * In AWS, provision the table via IaC (CloudFormation/Terraform/CDK) instead and disable
 * this with app.dynamodb.auto-create-table=false.
 */
@Component
public class DynamoDbTableInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DynamoDbTableInitializer.class);

    private final DynamoDbClient dynamoDbClient;

    @Value("${app.dynamodb.table-name}")
    private String tableName;

    @Value("${app.dynamodb.auto-create-table:true}")
    private boolean autoCreateTable;

    public DynamoDbTableInitializer(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public void run(String... args) {
        if (!autoCreateTable) {
            return;
        }

        List<String> existingTables = dynamoDbClient.listTables().tableNames();
        if (existingTables.contains(tableName)) {
            log.info("DynamoDB table '{}' already exists", tableName);
            return;
        }

        log.info("Creating DynamoDB table '{}'", tableName);
        dynamoDbClient.createTable(CreateTableRequest.builder()
                .tableName(tableName)
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .attributeDefinitions(
                        AttributeDefinition.builder().attributeName("bookingId").attributeType(ScalarAttributeType.S).build(),
                        AttributeDefinition.builder().attributeName("timeslotStart").attributeType(ScalarAttributeType.S).build()
                )
                .keySchema(
                        KeySchemaElement.builder().attributeName("bookingId").keyType(KeyType.HASH).build()
                )
                .globalSecondaryIndexes(
                        GlobalSecondaryIndex.builder()
                                .indexName("timeslot-index")
                                .keySchema(KeySchemaElement.builder().attributeName("timeslotStart").keyType(KeyType.HASH).build())
                                .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                                .build()
                )
                .build());

        dynamoDbClient.waiter().waitUntilTableExists(DescribeTableRequest.builder().tableName(tableName).build());
        log.info("DynamoDB table '{}' created", tableName);
    }
}
