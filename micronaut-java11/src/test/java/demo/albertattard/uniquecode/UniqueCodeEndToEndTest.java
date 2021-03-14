package demo.albertattard.uniquecode;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder;
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.function.aws.proxy.MicronautLambdaHandler;
import io.micronaut.function.aws.test.annotation.MicronautLambdaTest;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;

import static demo.albertattard.uniquecode.LocalDynamoDbExtension.scanAllItems;
import static demo.albertattard.uniquecode.LocalDynamoDbExtension.toAttributeValue;
import static org.assertj.core.api.Assertions.assertThat;

@MicronautLambdaTest
@ExtendWith(LocalDynamoDbExtension.class)
class UniqueCodeEndToEndTest {

    private static final Context lambdaContext = new MockLambdaContext();
    private static MicronautLambdaHandler handler;
    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setupSpec() {
        try {
            handler = new MicronautLambdaHandler();
            objectMapper = handler.getApplicationContext().getBean(ObjectMapper.class);
        } catch (ContainerInitializationException e) {
            throw new RuntimeException("Failed to initialise the container", e);
        }
    }

    @AfterAll
    static void cleanupSpec() {
        if (handler != null) {
            handler.getApplicationContext().close();
        }
    }

    @Test
    @DisplayName("should return a new random code with default length when a blank request is made")
    void shouldReturnANewRandomCodeWithDefaultLengthWhenABlankRequestIsMade() throws JsonProcessingException {
        /* The DynamoDB table should be empty before the test */
        assertThat(scanAllItems())
                .as("The local dynamo DB should be empty before the test")
                .isEmpty();

        final String json = objectMapper.writeValueAsString(new CreateUniqueCodeRequest());

        final AwsProxyRequest request = new AwsProxyRequestBuilder("/", HttpMethod.POST.toString())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .body(json)
                .build();
        final AwsProxyResponse response = handler.handleRequest(request, lambdaContext);
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK.getCode());

        final UniqueCode uniqueCode = objectMapper.readValue(response.getBody(), UniqueCode.class);
        assertThat(uniqueCode)
                .isNotNull();

        assertThat(uniqueCode.getCode())
                .as("Code must contains a mix of %d capital letters and numbers only", CreateUniqueCodeRequest.DEFAULT_LENGTH)
                .isNotNull()
                .hasSize(CreateUniqueCodeRequest.DEFAULT_LENGTH)
                .matches("^[A-Z0-9]{" + CreateUniqueCodeRequest.DEFAULT_LENGTH + "}$");

        final List<Map<String, AttributeValue>> allDataInDynamoDb = scanAllItems();
        assertThat(allDataInDynamoDb)
                .as("The local dynamo DB should have one item after the test run")
                .hasSize(1);

        final Map<String, AttributeValue> attributesByName = allDataInDynamoDb.get(0);
        assertThat(attributesByName).hasSize(2);
        assertThat(attributesByName.get("Code")).as("code").isEqualTo(toAttributeValue(uniqueCode.getCode()));
        assertThat(attributesByName.get("CreatedOn")).as("created on").isNotNull();
    }

    @Test
    @DisplayName("should return a new random code with the requested length and add the provided attributes to the DynamoDb table")
    void shouldReturnANewRandomCodeWithTheRequestedLengthAndAddTheProvidedAttributesToTheDynamoDbTable() throws JsonProcessingException {
        /* The DynamoDB table should be empty before the test */
        assertThat(scanAllItems())
                .as("The local dynamo DB should be empty before the test")
                .isEmpty();

        final CreateUniqueCodeRequest createUniqueCodeRequest = new CreateUniqueCodeRequest();
        createUniqueCodeRequest.setLength(12);
        createUniqueCodeRequest.setUsedBy("used-by-test");
        createUniqueCodeRequest.setReference("reference-test");
        createUniqueCodeRequest.setDescription("description-test");
        final String json = objectMapper.writeValueAsString(createUniqueCodeRequest);

        final AwsProxyRequest request = new AwsProxyRequestBuilder("/", HttpMethod.POST.toString())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .body(json)
                .build();
        final AwsProxyResponse response = handler.handleRequest(request, lambdaContext);
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK.getCode());

        final UniqueCode uniqueCode = objectMapper.readValue(response.getBody(), UniqueCode.class);
        assertThat(uniqueCode)
                .isNotNull();

        assertThat(uniqueCode.getCode())
                .as("Code must contains a mix of %d capital letters and numbers only", createUniqueCodeRequest.getLength())
                .isNotNull()
                .hasSize(createUniqueCodeRequest.getLength())
                .matches("^[A-Z0-9]{" + createUniqueCodeRequest.getLength() + "}$");

        final List<Map<String, AttributeValue>> allDataInDynamoDb = scanAllItems();
        assertThat(allDataInDynamoDb)
                .as("The local dynamo DB should have one item after the test run")
                .hasSize(1);

        final Map<String, AttributeValue> attributesByName = allDataInDynamoDb.get(0);
        assertThat(attributesByName).hasSize(5);
        assertThat(attributesByName.get("Code")).as("code").isEqualTo(toAttributeValue(uniqueCode.getCode()));
        assertThat(attributesByName.get("CreatedOn")).as("created on").isNotNull();
        assertThat(attributesByName.get("UsedBy")).as("used by").isEqualTo(toAttributeValue(createUniqueCodeRequest.getUsedBy()));
        assertThat(attributesByName.get("Reference")).as("reference").isEqualTo(toAttributeValue(createUniqueCodeRequest.getReference()));
        assertThat(attributesByName.get("Description")).as("description").isEqualTo(toAttributeValue(createUniqueCodeRequest.getDescription()));
    }
}
