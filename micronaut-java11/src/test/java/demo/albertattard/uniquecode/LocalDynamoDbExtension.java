package demo.albertattard.uniquecode;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

import java.net.URI;
import java.util.function.Consumer;

public class LocalDynamoDbExtension implements AfterAllCallback, BeforeAllCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalDynamoDbExtension.class);

    private static final String portNumber = "8000";
    private static final URI endpoint = URI.create(String.format("http://localhost:%s", portNumber));

    private DynamoDBProxyServer server;

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        deleteTable();
        stopUnchecked(server);
    }

    @Override
    public void beforeAll(final ExtensionContext extensionContext) throws Exception {
        setupPropertiesNeededBySqlite4Java();
        setupPropertiesNeededByAwsApi();
        startLocalDynamoDBServer();
        createTable();
    }

    private void startLocalDynamoDBServer() throws Exception {
        LOGGER.debug("Starting DynamoDB locally on port {}", portNumber);
        this.server = ServerRunner.createServerFromCommandLineArgs(new String[]{"-inMemory", "-port", portNumber});
        server.start();
    }

    private void setupPropertiesNeededBySqlite4Java() {
        LOGGER.debug("Setting up the Sqlite 4 system properties");

        /*
         * This following property is needed by the Sqlite 4 Java.  Gradle is copying the native libraries into the
         * "build/libs" folder and we need to set the property to point to this directory.
         */
        System.setProperty("sqlite4java.library.path", "build/libs");
    }

    private void setupPropertiesNeededByAwsApi() {
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
    public void createTable() {
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

    private void withClient(final Consumer<DynamoDbClient> consumer) {
        final DynamoDbClient client = DynamoDbClient.builder()
                .credentialsProvider(SystemPropertyCredentialsProvider.create())
                .endpointOverride(endpoint)
                .build();
        consumer.accept(client);
    }

    public void deleteTable() {
        LOGGER.debug("Deleting DynamoDB table");
        try {
            withClient(client -> client.deleteTable(builder -> builder.tableName("UniqueCodes")));
        } catch (final RuntimeException e) {
            LOGGER.warn("Failed to delete the DynamoDB table", e);
        }
    }

    protected void stopUnchecked(DynamoDBProxyServer dynamoDbServer) {
        LOGGER.debug("Stopping DynamoDB locally on port {}", portNumber);
        try {
            dynamoDbServer.stop();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    static URI endpoint() {
        return endpoint;
    }
}
