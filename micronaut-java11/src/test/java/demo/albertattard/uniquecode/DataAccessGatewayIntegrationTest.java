package demo.albertattard.uniquecode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;

import static demo.albertattard.uniquecode.LocalDynamoDbExtension.createDynamoDbClient;
import static demo.albertattard.uniquecode.LocalDynamoDbExtension.populateTableWithDummyValues;
import static demo.albertattard.uniquecode.LocalDynamoDbExtension.scanAllItems;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(LocalDynamoDbExtension.class)
class DataAccessGatewayIntegrationTest {

    @Test
    @DisplayName("should save unique code and return true when the code does not already exists")
    void shouldSaveUniqueCodeAndReturnTrueWhenTheCodeDoesNotAlreadyExists() {
        /* The DynamoDB table should be empty before the test */
        assertThat(scanAllItems())
                .as("The local dynamo DB should be empty before the test")
                .isEmpty();

        final CreateUniqueCodeRequest request = new CreateUniqueCodeRequest();
        final CreateUniqueCode createUniqueCode = CreateUniqueCode.builder(request)
                .createdOn("2077-04-27T12:34:56+01:00[Europe/Berlin]")
                .code("12345678")
                .build();

        final DataAccessGateway dataAccessGateway = new DataAccessGateway(createDynamoDbClient());
        final boolean successful = dataAccessGateway.saveUniqueCode(createUniqueCode);
        assertTrue(successful);

        final List<Map<String, AttributeValue>> allDataInDynamoDb = scanAllItems();
        assertThat(allDataInDynamoDb)
                .as("The local dynamo DB should have one item after the test run")
                .hasSize(1);

        final Map<String, AttributeValue> attributesByName = allDataInDynamoDb.get(0);
        assertThat(attributesByName).hasSize(2);
        assertThat(attributesByName.get("Code")).as("code").isEqualTo(toAttribute(createUniqueCode.getCode()));
        assertThat(attributesByName.get("CreatedOn")).as("created on").isNotNull();
    }

    @Test
    @DisplayName("should not save the given unique code and return false when the code already exists")
    void shouldNotSaveTheGivenUniqueCodeAndReturnFalseWhenTheCodeAlreadyExists() {
        /* The DynamoDB table should be empty before the test */
        assertThat(scanAllItems())
                .as("The local dynamo DB should be empty before the test")
                .isEmpty();

        final CreateUniqueCodeRequest request = new CreateUniqueCodeRequest();
        final CreateUniqueCode createUniqueCode = CreateUniqueCode.builder(request)
                .createdOn("2077-04-27T12:34:56+01:00[Europe/Berlin]")
                .code("12345678")
                .build();

        /* Add a value to the table to force the collision */
        populateTableWithDummyValues(createUniqueCode.getCode());

        final DataAccessGateway dataAccessGateway = new DataAccessGateway(createDynamoDbClient());
        final boolean successful = dataAccessGateway.saveUniqueCode(createUniqueCode);
        assertFalse(successful);

        final List<Map<String, AttributeValue>> allDataInDynamoDb = scanAllItems();
        assertThat(allDataInDynamoDb)
                .as("The local dynamo DB should have one item after the test run")
                .hasSize(1);

        final Map<String, AttributeValue> attributesByName = allDataInDynamoDb.get(0);
        assertThat(attributesByName).hasSize(1);
        assertThat(attributesByName.get("Code")).as("code").isEqualTo(toAttribute(createUniqueCode.getCode()));
    }

    private static AttributeValue toAttribute(String code) {
        return AttributeValue.builder().s(code).build();
    }
}