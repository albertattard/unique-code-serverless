package demo.albertattard.uniquecode;

import javax.inject.Singleton;

@FunctionalInterface
public interface CodeGenerationService {

    /* TODO: Use custom classes instead of the Java types */
    String generate(int length);
}
