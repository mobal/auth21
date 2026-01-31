package hu.squarelabs.auth21;

import hu.squarelabs.auth21.repository.UserRepository;
import hu.squarelabs.auth21.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class ApplicationTests {

  // Mock the missing UserService dependency to allow context to load
  @MockitoBean private UserService userService;
  @MockitoBean private UserRepository userRepository;

  @Test
  void contextLoads() {}
}
