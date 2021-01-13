package demo.albertattard.uniquecode;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class DynamoDbConfiguration {

    @Bean
    public DynamoDbClient amazonDynamoDB() {
        /* As recommended by https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/lambda-optimize-starttime.html */
        return DynamoDbClient.builder()
                .region(Region.EU_CENTRAL_1)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .httpClient(UrlConnectionHttpClient.builder().build())
                .build();
    }
}
