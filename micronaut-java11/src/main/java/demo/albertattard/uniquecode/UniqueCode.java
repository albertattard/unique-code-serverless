package demo.albertattard.uniquecode;

import io.micronaut.core.annotation.Introspected;
import lombok.Data;

@Data
@Introspected
public class UniqueCode {

    private final String code;
}
