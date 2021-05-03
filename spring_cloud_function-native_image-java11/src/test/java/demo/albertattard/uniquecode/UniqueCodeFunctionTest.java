package demo.albertattard.uniquecode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class UniqueCodeFunctionTest {

    @Test
    @DisplayName("should create and save the code for the given blank request")
    void shouldCreateAndSaveTheCodeForTheGivenBlankRequest() {
        final DataAccessGateway dataAccessGateway = mock(DataAccessGateway.class);
        final CodeGenerationService codeGenerationService = mock(CodeGenerationService.class);
        final ClockService clockService = mock(ClockService.class);

        final CreateUniqueCodeRequest request = new CreateUniqueCodeRequest();
        final CreateUniqueCode createUniqueCode = CreateUniqueCode.builder(request)
                .createdOn("2077-04-27T12:34:56+01:00[Europe/Berlin]")
                .code("12345678")
                .build();

        when(codeGenerationService.generate(anyInt())).thenReturn(createUniqueCode.getCode());
        when(clockService.createdOn()).thenReturn(createUniqueCode.getCreatedOn());
        when(dataAccessGateway.saveUniqueCode(any())).thenReturn(true);

        final Function<CreateUniqueCodeRequest, UniqueCode> handler =
                new UniqueCodeFunction(dataAccessGateway, codeGenerationService, clockService);

        final UniqueCode response = handler.apply(request);

        assertThat(response)
                .as("generated code")
                .isNotNull()
                .extracting(UniqueCode::getCode)
                .isEqualTo(createUniqueCode.getCode());

        verify(clockService, times(1)).createdOn();
        verify(codeGenerationService, times(1)).generate(eq(request.getLength()));
        verify(dataAccessGateway, times(1)).saveUniqueCode(eq(createUniqueCode));
        verifyNoMoreInteractions(dataAccessGateway, codeGenerationService, clockService);
    }

    @Test
    @DisplayName("should attempt again when a collision is encountered")
    void shouldAttemptAgainWhenACollisionIsEncountered() {
        final DataAccessGateway dataAccessGateway = mock(DataAccessGateway.class);
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
        when(dataAccessGateway.saveUniqueCode(any())).thenReturn(false).thenReturn(true);

        final Function<CreateUniqueCodeRequest, UniqueCode> handler =
                new UniqueCodeFunction(dataAccessGateway, codeGenerationService, clockService);

        final UniqueCode response = handler.apply(request);

        assertThat(response)
                .as("generated code")
                .isNotNull()
                .extracting("code")
                .isEqualTo(createUniqueCode.getCode());

        verify(clockService, times(1)).createdOn();
        verify(codeGenerationService, times(2)).generate(eq(request.getLength()));
        verify(dataAccessGateway, times(1)).saveUniqueCode(eq(createUniqueCode.withCode(existingCode)));
        verify(dataAccessGateway, times(1)).saveUniqueCode(eq(createUniqueCode));
        verifyNoMoreInteractions(clockService, codeGenerationService, dataAccessGateway);
    }

    @Test
    @DisplayName("should throw an exception when it fails to create a unique code after five attempts")
    void shouldThrowAnExceptionWhenItFailsToCreateAUniqueCodeAfterFiveAttempts() {
        final DataAccessGateway dataAccessGateway = mock(DataAccessGateway.class);
        final CodeGenerationService codeGenerationService = mock(CodeGenerationService.class);
        final ClockService clockService = mock(ClockService.class);

        final CreateUniqueCodeRequest request = new CreateUniqueCodeRequest();
        final CreateUniqueCode createUniqueCode = CreateUniqueCode.builder(request)
                .createdOn("2077-04-27T12:34:56+01:00[Europe/Berlin]")
                .code("AAAAAAAA")
                .build();

        when(clockService.createdOn()).thenReturn(createUniqueCode.getCreatedOn());
        when(codeGenerationService.generate(anyInt())).thenReturn(createUniqueCode.getCode());
        when(dataAccessGateway.saveUniqueCode(any())).thenReturn(false);

        final Function<CreateUniqueCodeRequest, UniqueCode> handler =
                new UniqueCodeFunction(dataAccessGateway, codeGenerationService, clockService);

        assertThrows(RuntimeException.class, () -> handler.apply(request));

        verify(clockService, times(1)).createdOn();
        verify(codeGenerationService, times(5)).generate(eq(request.getLength()));
        verify(dataAccessGateway, times(5)).saveUniqueCode(eq(createUniqueCode));
        verifyNoMoreInteractions(clockService, codeGenerationService, dataAccessGateway);
    }
}
