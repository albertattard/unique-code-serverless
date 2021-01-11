package demo.albertattard.uniquecode;

import java.util.Random;
import java.util.function.IntPredicate;

public class CodeGenerationService {

    /* TODO: Use domain primitives instead of the Java types */
    public String generate(final int length) {
        checkLength(length);

        final int origin = 48; // number '0'
        final int bound = 91; // capital letter 'Z' (90) + 1

        final Random random = new Random();
        return random.ints(origin, bound)
                .filter(onlyCapitalLettersAndNumbers())
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    private void checkLength(final int length) throws IllegalArgumentException {
        if (length < 1 || length > 256) {
            throw new IllegalArgumentException("Invalid length " + length + ".  Length must be between 1 and 256 both inclusive.");
        }
    }

    private IntPredicate onlyCapitalLettersAndNumbers() {
        /* Ignore anything between the numbers and the capital letters */
        return i -> (i <= 57 || i >= 65);
    }
}
