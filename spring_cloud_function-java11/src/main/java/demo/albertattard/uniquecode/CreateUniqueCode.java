package demo.albertattard.uniquecode;

import lombok.Data;

@Data
public class CreateUniqueCode {

    public static class Builder {

        private final String usedBy;
        private final String reference;
        private final String description;
        private String createdOn;
        private String code;

        private Builder(final CreateUniqueCodeRequest request) {
            this.usedBy = request.getUsedBy();
            this.reference = request.getReference();
            this.description = request.getDescription();
        }

        public Builder createdOn(final String createdOn) {
            this.createdOn = createdOn;
            return this;
        }

        public Builder code(final String code) {
            this.code = code;
            return this;
        }

        public CreateUniqueCode build() {
            return new CreateUniqueCode(this);
        }
    }

    private final String usedBy;
    private final String createdOn;
    private final String code;
    private final String reference;
    private final String description;

    private CreateUniqueCode(final Builder builder) {
        this(builder.usedBy, builder.createdOn, builder.code, builder.reference, builder.description);
    }

    private CreateUniqueCode(final String usedBy, final String createdOn, final String code, final String reference, final String description) {
        this.usedBy = usedBy;
        this.createdOn = createdOn;
        this.code = code;
        this.reference = reference;
        this.description = description;
    }

    public CreateUniqueCode withCode(final String code) {
        return new CreateUniqueCode(usedBy, createdOn, code, reference, description);
    }

    public static Builder builder(final CreateUniqueCodeRequest request) {
        return new Builder(request);
    }
}
