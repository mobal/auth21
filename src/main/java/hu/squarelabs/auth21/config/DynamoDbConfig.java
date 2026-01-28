package hu.squarelabs.auth21.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class DynamoDbConfig {

  @Value("${aws.region}")
  private String awsRegion;

  @Bean
  public DynamoDbClient client() {
    return DynamoDbClient.builder()
        .region(Region.of(awsRegion))
        .credentialsProvider(ProfileCredentialsProvider.create())
        .build();
  }

  @Bean
  public DynamoDbEnhancedClient enhancedClient(DynamoDbClient client) {
    return DynamoDbEnhancedClient.builder().dynamoDbClient(client).build();
  }
}
