package demo.albertattard.uniquecode;

import org.apache.commons.lang3.RandomStringUtils;

import javax.inject.Singleton;
import java.util.Locale;

@Singleton
public class RandomCodeGenerationService implements CodeGenerationService {

    @Override
    public String generate(int length) {
        return RandomStringUtils
                .randomAlphanumeric(length)
                .toUpperCase(Locale.ROOT);
    }
}
