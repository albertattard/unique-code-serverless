package demo.albertattard.uniquecode;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ReturnConsumedCapacity;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class UniqueCodeControllerTest {

    /* The easiest way to capture generic types is to use the '@Captor' annotation */
    @Captor
    private ArgumentCaptor<Consumer<PutItemRequest.Builder>> putItemArgumentCaptor;

    /* The easiest way to capture generic types is to use the '@Captor' annotation */
    @Captor
    private ArgumentCaptor<Map<String, AttributeValue>> itemArgumentCaptor;

    @Test
    @DisplayName("should return a new random code with default length when a blank request is made")
    void shouldReturnANewRandomCodeWithDefaultLengthWhenABlankRequestIsMade() {
        /* TODO: Mocking DynamoDbClient is not a great idea and we should wrap this within a gateway */
        final DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);
        final CodeGenerationService codeGenerationService = mock(CodeGenerationService.class);
        final ClockService clockService = mock(ClockService.class);
        final Context context = mock(Context.class);

        final String expectedCode = "12345678";
        final String expectedCreatedOn = "2077-04-27T12:34:56.123456+01:00[Europe/Berlin]";
        final CreateUniqueCodeRequest request = new CreateUniqueCodeRequest();

        when(codeGenerationService.generate(anyInt())).thenReturn(expectedCode);
        when(clockService.createdOn()).thenReturn(expectedCreatedOn);
        when(dynamoDbClient.putItem(ArgumentMatchers.<Consumer<PutItemRequest.Builder>>any())).thenReturn(null);

        final RequestHandler<CreateUniqueCodeRequest, UniqueCode> handler =
                new UniqueCodeController(dynamoDbClient, codeGenerationService, clockService);

        final UniqueCode response = handler.handleRequest(request, context);

        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo(expectedCode);

        verify(codeGenerationService).generate(request.getLength());
        verify(clockService).createdOn();
        verify(dynamoDbClient).putItem(putItemArgumentCaptor.capture());
        verifyNoMoreInteractions(dynamoDbClient, codeGenerationService, clockService, context);

        /*
         * TODO: Mocking DynamoDbClient is not a great idea and we should wrap this within a gateway.  The following
         *  captures are needed because of that.
         */
        final PutItemRequest.Builder putItemBuilder = mock(PutItemRequest.Builder.class);
        when(putItemBuilder.tableName(any())).thenReturn(putItemBuilder);
        when(putItemBuilder.returnConsumedCapacity(any(ReturnConsumedCapacity.class))).thenReturn(putItemBuilder);
        when(putItemBuilder.item(ArgumentMatchers.any())).thenReturn(putItemBuilder);
        when(putItemBuilder.conditionExpression(any(String.class))).thenReturn(putItemBuilder);
        when(putItemBuilder.build()).thenReturn(null);

        final Consumer<PutItemRequest.Builder> capturedPutItemConsumer = putItemArgumentCaptor.getValue();
        capturedPutItemConsumer.accept(putItemBuilder);

        verify(putItemBuilder).tableName("UniqueCodes");
        verify(putItemBuilder).returnConsumedCapacity(ReturnConsumedCapacity.NONE);
        verify(putItemBuilder).item(itemArgumentCaptor.capture());
        verify(putItemBuilder).conditionExpression("attribute_not_exists(Code)");
        verify(putItemBuilder).build();
        verifyNoMoreInteractions(putItemBuilder);

        final Map<String, AttributeValue> capturedItem = itemArgumentCaptor.getValue();
        assertThat(capturedItem)
                .isEqualTo(Map.of(
                        "Code", AttributeValue.builder().s(expectedCode).build(),
                        "CreatedOn", AttributeValue.builder().s(expectedCreatedOn).build()
                ));
    }
}
