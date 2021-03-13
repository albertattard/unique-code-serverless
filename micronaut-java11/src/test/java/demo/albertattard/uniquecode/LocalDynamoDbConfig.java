package demo.albertattard.uniquecode;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.env.Environment;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Factory
public class LocalDynamoDbConfig {

    @Bean
    @Primary
    DynamoDbClient dynamoDbClient(final Environment environment) {
        return LocalDynamoDbExtension.createDynamoDbClient();
    }
}
