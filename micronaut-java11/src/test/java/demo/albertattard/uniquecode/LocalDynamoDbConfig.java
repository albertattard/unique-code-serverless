package demo.albertattard.uniquecode;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.env.Environment;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Factory
public class LocalDynamoDbConfig {

    @Bean
    DynamoDbClient dynamoDbClient(final Environment environment) {
        return DynamoDbClient.builder()
                .credentialsProvider(SystemPropertyCredentialsProvider.create())
                .endpointOverride(LocalDynamoDbExtension.endpoint())
                .build();
    }
}
