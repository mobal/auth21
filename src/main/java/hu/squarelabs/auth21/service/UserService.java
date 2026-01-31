package hu.squarelabs.auth21.service;

import hu.squarelabs.auth21.model.entity.UserEntity;
import hu.squarelabs.auth21.repository.UserRepository;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private static final Logger logger = LogManager.getLogger(UserService.class);

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
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
