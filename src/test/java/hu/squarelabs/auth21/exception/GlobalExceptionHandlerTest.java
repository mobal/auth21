package hu.squarelabs.auth21.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Nested
  @DisplayName("handleException method")
  class HandleExceptionMethod {

    @Test
    @DisplayName("should return ProblemDetail for any exception")
    void shouldReturnProblemDetailForAnyException() {
      final Exception exception = new RuntimeException("Test error message");

      final ProblemDetail result = handler.handleException(exception);

      assertThat(result).isNotNull();
      assertThat(result.getStatus()).isEqualTo(500);
    }

    @Test
    @DisplayName("should set status code to 500")
    void shouldSetStatusCodeTo500() {
      final Exception exception = new RuntimeException("Runtime error");

      final ProblemDetail result = handler.handleException(exception);

      assertThat(result.getStatus()).isEqualTo(500);
    }

    @Test
    @DisplayName("should include detail message")
    void shouldIncludeDetailMessage() {
      final Exception exception = new IllegalArgumentException("Invalid argument");

      final ProblemDetail result = handler.handleException(exception);

      assertThat(result.getDetail()).isNotEmpty();
    }

    @Test
    @DisplayName("should set title")
    void shouldSetTitle() {
      final Exception exception = new RuntimeException();

      final ProblemDetail result = handler.handleException(exception);

      assertThat(result.getTitle()).isNotEmpty();
    }

    @Test
    @DisplayName("should handle various exception types")
    void shouldHandleVariousExceptionTypes() {
      final Exception ioException = new java.io.IOException("IO error");
      final Exception nullException = new NullPointerException("Null ref");

      final ProblemDetail result1 = handler.handleException(ioException);
      final ProblemDetail result2 = handler.handleException(nullException);

      assertThat(result1.getStatus()).isEqualTo(500);
      assertThat(result2.getStatus()).isEqualTo(500);
    }

    @Test
    @DisplayName("should include timestamp in properties")
    void shouldIncludeTimestampInProperties() {
      final Exception exception = new RuntimeException("Test error");

      final ProblemDetail result = handler.handleException(exception);

      assertThat(result.getProperties()).containsKey("timestamp");
    }
  }
}
