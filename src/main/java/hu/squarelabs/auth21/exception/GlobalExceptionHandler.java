package hu.squarelabs.auth21.exception;

import java.time.LocalDateTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

  public ProblemDetail handleException(Exception ex) {
    logger.error("An unexpected error occurred", ex);

    final var problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);

    problem.setTitle("Internal Server Error");
    problem.setDetail("An unexpected error occurred. Please try again later.");
    problem.setProperty("timestamp", LocalDateTime.now());

    return problem;
  }
}
