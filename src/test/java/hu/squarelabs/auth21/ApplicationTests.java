package hu.squarelabs.auth21;

import hu.squarelabs.auth21.repository.TokenRepository;
import hu.squarelabs.auth21.repository.UserRepository;
import io.awspring.cloud.autoconfigure.dynamodb.DynamoDbAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@EnableAutoConfiguration(exclude = {DynamoDbAutoConfiguration.class})
class ApplicationTests {

  // Mock AWS-dependent repositories to allow context to load without AWS credentials
  @MockitoBean private UserRepository userRepository;
  @MockitoBean private TokenRepository tokenRepository;

  @Test
  void contextLoads() {}
}
