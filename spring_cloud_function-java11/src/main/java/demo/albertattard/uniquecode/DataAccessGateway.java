package demo.albertattard.uniquecode;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.ReturnConsumedCapacity;

import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class DataAccessGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataAccessGateway.class);

    private final DynamoDbClient client;

    public boolean saveUniqueCode(final CreateUniqueCode item) {
        try {
            client.putItem(builder -> builder
                    .tableName("UniqueCodes")
                    .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                    .item(toAttributesByName(item))
                    .conditionExpression("attribute_not_exists(Code)")
                    .build());
            return true;
        } catch (final ConditionalCheckFailedException e) {
            LOGGER.warn("The code {} already exists", item.getCode());
            return false;
        }
    }

    private Map<String, AttributeValue> toAttributesByName(final CreateUniqueCode request) {
        final Map<String, AttributeValue> item = new HashMap<>();
        addIfNotBlank("Code", request.getCode(), item);
        addIfNotBlank("CreatedOn", request.getCreatedOn(), item);
        addIfNotBlank("UsedBy", request.getUsedBy(), item);
        addIfNotBlank("Reference", request.getReference(), item);
        addIfNotBlank("Description", request.getDescription(), item);
        return item;
    }

    private static void addIfNotBlank(final String key, final String value, final Map<String, AttributeValue> item) {
        if (value != null && !value.trim().isEmpty()) {
            item.put(key, AttributeValue.builder().s(value).build());
        }
    }
}
