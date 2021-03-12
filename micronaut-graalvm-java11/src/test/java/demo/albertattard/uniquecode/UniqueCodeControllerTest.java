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
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

@MicronautLambdaTest
@ExtendWith(LocalDynamoDbExtension.class)
class UniqueCodeControllerTest {

    private static final Context lambdaContext = new MockLambdaContext();
    private static MicronautLambdaHandler handler;
    private static ObjectMapper objectMapper;

    /*
     * TODO: Cannot mock the services as yet, as @MicronautTest is incomplete.
     *  Reference: https://github.com/micronaut-projects/micronaut-test/issues/330
     */
    // @Inject
    // CodeGenerationService codeGenerationService;

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
        final String json = objectMapper.writeValueAsString(new CreateUniqueCodeRequest());

        // final String expectedCode = "12345678";
        // when(codeGenerationService.generate(CreateUniqueCodeRequest.DEFAULT_LENGTH)).thenReturn(expectedCode);

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

        final String code = uniqueCode.getCode();
        assertThat(code)
                // .isEqualTo(expectedCode)
                .describedAs("Code must contains a mix of %d capital letters and numbers only", CreateUniqueCodeRequest.DEFAULT_LENGTH)
                .isNotNull()
                .matches("[A-Z0-9]{" + CreateUniqueCodeRequest.DEFAULT_LENGTH + "}")
        ;

        // verify(codeGenerationService).generate(CreateUniqueCodeRequest.DEFAULT_LENGTH);
    }

    // @MockBean(RandomCodeGenerationService.class)
    // CodeGenerationService codeGenerationService() {
    //     return mock(CodeGenerationService.class);
    // }
}
