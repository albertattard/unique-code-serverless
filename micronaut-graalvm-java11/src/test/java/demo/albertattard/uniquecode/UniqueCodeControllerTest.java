package demo.albertattard.uniquecode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("Unique code controller test")
public class UniqueCodeControllerTest {

    @Test
    @DisplayName("should create and save the code for the given blank request")
    void shouldCreateAndSaveTheCodeForTheGivenBlankRequest() {
        final DataAccessGateway gateway = mock(DataAccessGateway.class);
        final CodeGenerationService codeGenerationService = mock(CodeGenerationService.class);
        final ClockService clockService = mock(ClockService.class);

        final CreateUniqueCodeRequest request = new CreateUniqueCodeRequest();
        final CreateUniqueCode createUniqueCode = CreateUniqueCode.builder(request)
                .createdOn("2077-04-27T12:34:56+01:00[Europe/Berlin]")
                .code("12345678")
                .build();

        when(clockService.createdOn()).thenReturn(createUniqueCode.getCreatedOn());
        when(codeGenerationService.generate(anyInt())).thenReturn(createUniqueCode.getCode());
        when(gateway.saveUniqueCode(any())).thenReturn(true);

        final UniqueCodeController controller = new UniqueCodeController(gateway, codeGenerationService, clockService);
        final UniqueCode uniqueCode = controller.create(request);

        assertThat(uniqueCode)
                .as("generated code")
                .isNotNull()
                .extracting(UniqueCode::getCode)
                .isEqualTo(createUniqueCode.getCode());

        verify(clockService, times(1)).createdOn();
        verify(codeGenerationService, times(1)).generate(eq(request.getLength()));
        verify(gateway, times(1)).saveUniqueCode(eq(createUniqueCode));
        verifyNoMoreInteractions(clockService, codeGenerationService, gateway);
    }

    @Test
    @DisplayName("should attempt again when a collision is encountered")
    void shouldAttemptAgainWhenACollisionIsEncountered() {
        final DataAccessGateway gateway = mock(DataAccessGateway.class);
        final CodeGenerationService codeGenerationService = mock(CodeGenerationService.class);
        final ClockService clockService = mock(ClockService.class);

        final String existingCode = "AAAAAAAA";
        final CreateUniqueCodeRequest request = new CreateUniqueCodeRequest();
        final CreateUniqueCode createUniqueCode = CreateUniqueCode.builder(request)
                .createdOn("2077-04-27T12:34:56+01:00[Europe/Berlin]")
                .code("12345678")
                .build();

        when(clockService.createdOn()).thenReturn(createUniqueCode.getCreatedOn());
        when(codeGenerationService.generate(anyInt())).thenReturn(existingCode).thenReturn(createUniqueCode.getCode());
        when(gateway.saveUniqueCode(any())).thenReturn(false).thenReturn(true);

        final UniqueCodeController controller = new UniqueCodeController(gateway, codeGenerationService, clockService);
        final UniqueCode uniqueCode = controller.create(request);

        assertThat(uniqueCode)
                .as("generated code")
                .isNotNull()
                .extracting("code")
                .isEqualTo(createUniqueCode.getCode());

        verify(clockService, times(1)).createdOn();
        verify(codeGenerationService, times(2)).generate(eq(request.getLength()));
        verify(gateway, times(1)).saveUniqueCode(eq(createUniqueCode.withCode(existingCode)));
        verify(gateway, times(1)).saveUniqueCode(eq(createUniqueCode));
        verifyNoMoreInteractions(clockService, codeGenerationService, gateway);
    }

    @Test
    @DisplayName("should throw an exception when it fails to create a unique code after five attempts")
    void shouldThrowAnExceptionWhenItFailsToCreateAUniqueCodeAfterFiveAttempts() {
        final DataAccessGateway gateway = mock(DataAccessGateway.class);
        final CodeGenerationService codeGenerationService = mock(CodeGenerationService.class);
        final ClockService clockService = mock(ClockService.class);

        final CreateUniqueCodeRequest request = new CreateUniqueCodeRequest();
        final CreateUniqueCode createUniqueCode = CreateUniqueCode.builder(request)
                .createdOn("2077-04-27T12:34:56+01:00[Europe/Berlin]")
                .code("AAAAAAAA")
                .build();

        when(clockService.createdOn()).thenReturn(createUniqueCode.getCreatedOn());
        when(codeGenerationService.generate(anyInt())).thenReturn(createUniqueCode.getCode());
        when(gateway.saveUniqueCode(any())).thenReturn(false);

        final UniqueCodeController controller = new UniqueCodeController(gateway, codeGenerationService, clockService);
        assertThrows(RuntimeException.class, () -> controller.create(request));

        verify(clockService, times(1)).createdOn();
        verify(codeGenerationService, times(5)).generate(eq(request.getLength()));
        verify(gateway, times(5)).saveUniqueCode(eq(createUniqueCode));
        verifyNoMoreInteractions(clockService, codeGenerationService, gateway);
    }
}
