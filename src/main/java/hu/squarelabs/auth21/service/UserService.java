package hu.squarelabs.auth21.service;

import hu.squarelabs.auth21.exception.UserAlreadyExistsException;
import hu.squarelabs.auth21.model.entity.UserEntity;
import hu.squarelabs.auth21.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private static final Logger logger = LogManager.getLogger(UserService.class);

  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;

  public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
    this.passwordEncoder = passwordEncoder;
    this.userRepository = userRepository;
  }

  public String create(String email, String password, String username, String displayName) {
    logger.info("Registering new user with email: {}", email);

    if (!userRepository.findByUsernameOrEmail(email, username).isEmpty()) {
      throw new UserAlreadyExistsException(email, username);
    }

    final String newUserId = UUID.randomUUID().toString();

    UserEntity newUser = new UserEntity();
    newUser.setId(newUserId);
    newUser.setEmail(email);
    newUser.setPassword(this.passwordEncoder.encode(password));
    newUser.setUsername(username);
    newUser.setDisplayName(displayName);

    userRepository.save(newUser);
    logger.info("User registered with ID: {}", newUserId);

    return newUserId;
  }

  public Optional<UserEntity> getUserByEmail(String email) {
    logger.info("Fetching user by email: {}", email);
    return userRepository.findByEmail(email);
  }

  public Optional<UserEntity> getUserById(String userId) {
    logger.info("Fetching user by ID: {}", userId);
    return userRepository.findById(userId);
  }

  public Optional<UserEntity> getUserByUserName(String username) {
    logger.info("Fetching user by username: {}", username);
    return userRepository.findByUserName(username);
  }
}
