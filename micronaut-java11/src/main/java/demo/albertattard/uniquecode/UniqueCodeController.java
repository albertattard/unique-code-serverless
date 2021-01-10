package demo.albertattard.uniquecode;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import lombok.Data;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ReturnConsumedCapacity;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Data
@Controller
public class UniqueCodeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UniqueCodeController.class);

    private final DynamoDbClient client;
    private final CodeGenerationService codeGenerationService;

    @Post
    public UniqueCode create(@Body final CreateUniqueCodeRequest request) {
        final String random = codeGenerationService.generate(request.getLength());

        final Map<String, AttributeValue> item = new HashMap<>();
        item.put("Code", AttributeValue.builder().s(random).build());
        item.put("CreatedOn", AttributeValue.builder().s(createdOn()).build());
        addIfNotBlank("UsedBy", request.getUsedBy(), item);
        addIfNotBlank("Reference", request.getReference(), item);
        addIfNotBlank("Description", request.getDescription(), item);

        final PutItemRequest a = PutItemRequest.builder()
                .tableName("UniqueCodes")
                .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                .item(item)
                .build();
        final PutItemResponse response = client.putItem(a);
        LOGGER.debug("Unique code created {}", response);

        return new UniqueCode(random);
    }

    private static void addIfNotBlank(final String key, final String value, final Map<String, AttributeValue> item) {
        if (StringUtils.isNotBlank(value)) {
            item.put(key, AttributeValue.builder().s(value).build());
        }
    }

    private static String createdOn() {
        return DateTimeFormatter.ISO_DATE_TIME.format(ZonedDateTime.now());
    }
}
