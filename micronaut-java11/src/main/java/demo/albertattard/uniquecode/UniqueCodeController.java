package demo.albertattard.uniquecode;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.ReturnConsumedCapacity;

import java.util.HashMap;
import java.util.Map;

@Controller
@AllArgsConstructor
public class UniqueCodeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UniqueCodeController.class);

    private final DynamoDbClient client;
    private final CodeGenerationService codeGenerationService;
    private final ClockService clockService;

    @Post
    public UniqueCode create(@Body final CreateUniqueCodeRequest request) {
        final Map<String, AttributeValue> item = createAttributes(request);
        addMetadataAttributes(item);

        for (int attempt = 1, limit = 5; attempt <= limit; attempt++) {
            LOGGER.debug("Creating unique code (Attempt {} of {})", attempt, limit);
            final String random = codeGenerationService.generate(request.getLength());
            item.put("Code", AttributeValue.builder().s(random).build());

            boolean successful = saveUniqueCode(item);
            if (successful) {
                LOGGER.debug("Unique code generated {} after {} attempt of {}", random, attempt, limit);
                return new UniqueCode(random);
            }

            LOGGER.warn("Failed to create a unique code (Attempt {} of {})", attempt, limit);
        }

        throw new RuntimeException("Failed to create a unique code");
    }

    private boolean saveUniqueCode(final Map<String, AttributeValue> item) {
        try {
            client.putItem(builder -> builder
                    .tableName("UniqueCodes")
                    .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                    .item(item)
                    .conditionExpression("attribute_not_exists(Code)")
                    .build());
            return true;
        } catch (final ConditionalCheckFailedException e) {
            LOGGER.warn("The code {} already exists", item.get("Code"));
            return false;
        }
    }

    private Map<String, AttributeValue> createAttributes(final CreateUniqueCodeRequest request) {
        final Map<String, AttributeValue> item = new HashMap<>();
        addIfNotBlank("UsedBy", request.getUsedBy(), item);
        addIfNotBlank("Reference", request.getReference(), item);
        addIfNotBlank("Description", request.getDescription(), item);
        return item;
    }

    private void addMetadataAttributes(final Map<String, AttributeValue> item) {
        final String createdOn = clockService.createdOn();
        item.put("CreatedOn", AttributeValue.builder().s(createdOn).build());
    }

    private static void addIfNotBlank(final String key, final String value, final Map<String, AttributeValue> item) {
        if (value != null && !value.trim().isEmpty()) {
            item.put(key, AttributeValue.builder().s(value).build());
        }
    }
}
