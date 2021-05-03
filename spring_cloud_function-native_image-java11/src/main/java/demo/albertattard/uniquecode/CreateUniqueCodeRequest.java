package demo.albertattard.uniquecode;

import lombok.Data;

@Data
public class CreateUniqueCodeRequest {

    public static final int DEFAULT_LENGTH = 8;

    private String usedBy;
    private int length = DEFAULT_LENGTH;
    private String reference;
    private String description;
}
