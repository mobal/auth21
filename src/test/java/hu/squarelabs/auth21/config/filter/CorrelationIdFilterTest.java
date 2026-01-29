package hu.squarelabs.auth21.config.filter;

import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("CorrelationIdFilter")
@ExtendWith(MockitoExtension.class)
class CorrelationIdFilterTest {

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private FilterChain filterChain;
  private CorrelationIdFilter filter;

  @BeforeEach
  void setUp() {
    filter = new CorrelationIdFilter();
  }

  @Nested
  @DisplayName("doFilterInternal method")
  class DoFilterInternalMethod {

    @Test
    @DisplayName("should pass request to filter chain")
    void shouldPassRequestToFilterChain() throws ServletException, IOException {
      when(request.getHeader("X-Correlation-ID")).thenReturn("test-correlation-id");

      filter.doFilterInternal(request, response, filterChain);

      verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("should use provided correlation ID from header")
    void shouldUseProvidedCorrelationIdFromHeader() throws ServletException, IOException {
      final String correlationId = "existing-correlation-id-123";
      when(request.getHeader("X-Correlation-ID")).thenReturn(correlationId);

      filter.doFilterInternal(request, response, filterChain);

      verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("should generate new correlation ID when header not present")
    void shouldGenerateNewCorrelationIdWhenHeaderNotPresent() throws ServletException, IOException {
      when(request.getHeader("X-Correlation-ID")).thenReturn(null);

      filter.doFilterInternal(request, response, filterChain);

      verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("should set correlation ID in response header")
    void shouldSetCorrelationIdInResponseHeader() throws ServletException, IOException {
      final String correlationId = "test-correlation-id";
      when(request.getHeader("X-Correlation-ID")).thenReturn(correlationId);

      filter.doFilterInternal(request, response, filterChain);

      verify(response).setHeader("X-Correlation-ID", correlationId);
    }
  }

  @Nested
  @DisplayName("with existing correlation ID header")
  class WithExistingCorrelationId {

    @Test
    @DisplayName("should set existing ID in response header")
    void shouldSetExistingIdInResponseHeader() throws ServletException, IOException {
      final String existingId = "existing-id-abc123";
      when(request.getHeader("X-Correlation-ID")).thenReturn(existingId);

      filter.doFilterInternal(request, response, filterChain);

      verify(response).setHeader("X-Correlation-ID", existingId);
    }

    @Test
    @DisplayName("should pass request with existing ID to filter chain")
    void shouldPassRequestWithExistingIdToFilterChain() throws ServletException, IOException {
      when(request.getHeader("X-Correlation-ID")).thenReturn("existing-id");

      filter.doFilterInternal(request, response, filterChain);

      verify(filterChain).doFilter(request, response);
    }
  }

  @Nested
  @DisplayName("with missing correlation ID header")
  class WithMissingCorrelationId {

    @Test
    @DisplayName("should generate and set UUID in response header")
    void shouldGenerateAndSetUuidInResponseHeader() throws ServletException, IOException {
      when(request.getHeader("X-Correlation-ID")).thenReturn(null);

      filter.doFilterInternal(request, response, filterChain);

      // Verify that setHeader was called with a UUID-formatted ID
      verify(response).setHeader(eq("X-Correlation-ID"), matches("[a-f0-9-]{36}"));
    }

    @Test
    @DisplayName("should pass request with generated ID to filter chain")
    void shouldPassRequestWithGeneratedIdToFilterChain() throws ServletException, IOException {
      when(request.getHeader("X-Correlation-ID")).thenReturn(null);

      filter.doFilterInternal(request, response, filterChain);

      verify(filterChain).doFilter(request, response);
    }
  }

  @Nested
  @DisplayName("with blank correlation ID header")
  class WithBlankCorrelationId {

    @Test
    @DisplayName("should treat blank string as missing and generate UUID")
    void shouldTreatBlankStringAsMissingAndGenerateUuid() throws ServletException, IOException {
      when(request.getHeader("X-Correlation-ID")).thenReturn("   ");

      filter.doFilterInternal(request, response, filterChain);

      // Should generate a UUID for blank string
      verify(response).setHeader(eq("X-Correlation-ID"), matches("[a-f0-9-]{36}"));
    }

    @Test
    @DisplayName("should treat empty string as missing and generate UUID")
    void shouldTreatEmptyStringAsMissingAndGenerateUuid() throws ServletException, IOException {
      when(request.getHeader("X-Correlation-ID")).thenReturn("");

      filter.doFilterInternal(request, response, filterChain);

      // Should generate a UUID for empty string
      verify(response).setHeader(eq("X-Correlation-ID"), matches("[a-f0-9-]{36}"));
    }
  }
}
