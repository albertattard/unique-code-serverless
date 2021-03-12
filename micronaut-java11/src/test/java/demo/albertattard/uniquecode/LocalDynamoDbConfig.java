package demo.albertattard.uniquecode;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.env.Environment;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Factory
public class LocalDynamoDbConfig {

    @Bean
    @Primary
    DynamoDbClient dynamoDbClient(final Environment environment) {
        return DynamoDbClient.builder()
                .credentialsProvider(SystemPropertyCredentialsProvider.create())
                .region(Region.EU_CENTRAL_1)
                .endpointOverride(LocalDynamoDbExtension.endpoint())
                .build();
    }
}
