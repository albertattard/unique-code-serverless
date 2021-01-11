package demo.albertattard.uniquecode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CodeGenerationServiceTest {

    private final CodeGenerationService service = new CodeGenerationService();

    @Test
    @DisplayName("should return a string of the given length containing only uppercase letters and numbers")
    void shouldReturnAStringOfTheGivenLengthContainingOnlyUppercaseLettersAndNumbers() {
        final String code = service.generate(8);
        assertThat(code)
                .describedAs("Code must contains a mix of 8 capital letters and numbers only")
                .isNotNull()
                .matches("[A-Z0-9]{8}");
    }

    @Test
    @DisplayName("should not return duplicates after 1000 attempts")
    void shouldNotReturnDuplicatesAfter1000Attempts() {
        final int numberOfAttempts = 1000;
        final Set<String> observed = new HashSet<>(numberOfAttempts);

        for (int attempt = 1; attempt <= numberOfAttempts; attempt++) {
            final String code = service.generate(6);
            assertThat(observed.add(code))
                    .describedAs("Found a duplicate code '%s' after %d attempts", code, attempt)
                    .isTrue();
        }
    }
}
