package demo.albertattard.uniquecode;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@AllArgsConstructor
public class UniqueCodeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UniqueCodeController.class);

    private final DataAccessGateway gateway;
    private final CodeGenerationService codeGenerationService;
    private final ClockService clockService;

    @Post
    public UniqueCode create(@Body final CreateUniqueCodeRequest request) {
        final CreateUniqueCode.Builder builder = CreateUniqueCode.builder(request)
                .createdOn(clockService.createdOn());

        for (int attempt = 1, limit = 5; attempt <= limit; attempt++) {
            LOGGER.debug("Creating unique code (Attempt {} of {})", attempt, limit);

            final CreateUniqueCode item = builder.code(codeGenerationService.generate(request.getLength())).build();
            final boolean successful = gateway.saveUniqueCode(item);
            if (successful) {
                LOGGER.debug("Unique code generated {} after {} attempt of {}", item.getCode(), attempt, limit);
                return new UniqueCode(item.getCode());
            }

            LOGGER.warn("Failed to create a unique code (Attempt {} of {})", attempt, limit);
        }

        throw new RuntimeException("Failed to create a unique code");
    }
}
