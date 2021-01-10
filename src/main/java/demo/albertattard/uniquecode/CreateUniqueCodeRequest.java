package demo.albertattard.uniquecode;

import io.micronaut.core.annotation.Introspected;
import lombok.Data;

@Data
@Introspected
public class CreateUniqueCodeRequest {

    public static final int DEFAULT_LENGTH = 8;

    private String usedBy;
    private int length = DEFAULT_LENGTH;
    private String reference;
    private String description;
}
