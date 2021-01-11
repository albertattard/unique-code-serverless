package demo.albertattard.uniquecode;

import javax.inject.Singleton;
import java.util.Random;

@Singleton
public class CodeGenerationService {

    public String generate(int length) {
        final int leftLimit = 48; // numeral '0'
        final int rightLimit = 90; // letter 'Z'

        final Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65)) /* Ignore anything between the numbers and the capital letters*/
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
