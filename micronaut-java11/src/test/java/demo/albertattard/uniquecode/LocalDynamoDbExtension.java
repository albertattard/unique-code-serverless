package demo.albertattard.uniquecode;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ReturnConsumedCapacity;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class LocalDynamoDbExtension implements BeforeEachCallback, AfterAllCallback, BeforeAllCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalDynamoDbExtension.class);

    private static final String PORT_NUMBER = "8000";
    private static final URI ENDPOINT = URI.create(String.format("http://localhost:%s", PORT_NUMBER));

    private DynamoDBProxyServer server;

    @Override
    public void beforeAll(final ExtensionContext extensionContext) throws Exception {
        setupPropertiesNeededBySqlite4Java();
        setupPropertiesNeededByAwsApi();
        startLocalDynamoDBServer();
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        deleteTable();
        createTable();
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        stopUnchecked(server);
    }

    private void startLocalDynamoDBServer() throws Exception {
        LOGGER.debug("Starting DynamoDB locally on port {}", PORT_NUMBER);
        this.server = ServerRunner.createServerFromCommandLineArgs(new String[]{"-inMemory", "-port", PORT_NUMBER});
        server.start();
    }

    private static void setupPropertiesNeededBySqlite4Java() {
        LOGGER.debug("Setting up the Sqlite 4 system properties");

        /*
         * This following property is needed by the Sqlite 4 Java.  Gradle is copying the native libraries into the
         * "build/libs" folder and we need to set the property to point to this directory.
         */
        System.setProperty("sqlite4java.library.path", "build/libs");
    }

    private static void setupPropertiesNeededByAwsApi() {
        LOGGER.debug("Setting up the AWS API system properties");

        /*
         * The following properties are required by the AWS client.  The values for the access key and secret can be
         * any random thing.  The local DynamoDB does not use them.
         */
        System.setProperty("aws.accessKeyId", "anything");
        System.setProperty("aws.secretAccessKey", "anything");
        System.setProperty("aws.region", "eu-central-1");
    }

    @SuppressWarnings("unchecked")
    private static void createTable() {
        LOGGER.debug("Creating DynamoDB table");
        /* This needs to match what we defined in the terraform files */
        withClient(client ->
                client.createTable(builder ->
                        builder.tableName("UniqueCodes")
                                .attributeDefinitions(b -> b.attributeName("Code").attributeType(ScalarAttributeType.S).build())
                                .keySchema(b -> b.attributeName("Code").keyType(KeyType.HASH))
                                .billingMode(BillingMode.PROVISIONED)
                                .provisionedThroughput(b -> b.readCapacityUnits(3L).writeCapacityUnits(3L))
                )
        );
    }

    private static void withClient(final Consumer<DynamoDbClient> consumer) {
        consumer.accept(createDynamoDbClient());
    }

    private static <T> T withClientReturn(final Function<DynamoDbClient, T> consumer) {
        return consumer.apply(createDynamoDbClient());
    }

    public static void populateTableWithDummyValues(final String code) {
        final Map<String, AttributeValue> item = new HashMap<>();
        item.put("Code", AttributeValue.builder().s(code).build());

        withClient(client ->
                client.putItem(builder -> builder
                        .tableName("UniqueCodes")
                        .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                        .item(item)
                        .conditionExpression("attribute_not_exists(Code)")
                        .build()
                )
        );
    }

    public static List<Map<String, AttributeValue>> scanAllItems() {
        return withClientReturn(client ->
                client.scan(builder -> builder
                        .tableName("UniqueCodes"))
                        .items()
        );
    }

    private static void deleteTable() {
        LOGGER.debug("Deleting DynamoDB table");
        try {
            withClient(client -> client.deleteTable(builder -> builder.tableName("UniqueCodes")));
        } catch (final ResourceNotFoundException e) {
            /* The table did not exist */
        } catch (final RuntimeException e) {
            LOGGER.warn("Failed to delete the DynamoDB table", e);
        }
    }

    private static void stopUnchecked(final DynamoDBProxyServer dynamoDbServer) {
        LOGGER.debug("Stopping DynamoDB locally on port {}", PORT_NUMBER);
        if (dynamoDbServer != null) {
            try {
                dynamoDbServer.stop();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static DynamoDbClient createDynamoDbClient() {
        return DynamoDbClient.builder()
                .endpointOverride(ENDPOINT)
                .credentialsProvider(SystemPropertyCredentialsProvider.create())
                .region(Region.EU_CENTRAL_1)
                .build();
    }
}
