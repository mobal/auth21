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
  @DisplayName("doFilterInternal")
  class DoFilterInternal {

    @Test
    @DisplayName("uses existing correlation ID from header")
    void usesExistingCorrelationIdFromHeader() throws ServletException, IOException {
      when(request.getHeader("X-Correlation-ID")).thenReturn("existing-correlation-id");

      filter.doFilterInternal(request, response, filterChain);

      verify(response).setHeader("X-Correlation-ID", "existing-correlation-id");
      verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("generates UUID when correlation ID header is missing")
    void generatesUuidWhenCorrelationIdHeaderIsMissing() throws ServletException, IOException {
      when(request.getHeader("X-Correlation-ID")).thenReturn(null);

      filter.doFilterInternal(request, response, filterChain);

      verify(response).setHeader(eq("X-Correlation-ID"), matches("[a-f0-9-]{36}"));
      verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("generates UUID when correlation ID header is blank")
    void generatesUuidWhenCorrelationIdHeaderIsBlank() throws ServletException, IOException {
      when(request.getHeader("X-Correlation-ID")).thenReturn("   ");

      filter.doFilterInternal(request, response, filterChain);

      verify(response).setHeader(eq("X-Correlation-ID"), matches("[a-f0-9-]{36}"));
      verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("generates UUID when correlation ID header is empty string")
    void generatesUuidWhenCorrelationIdHeaderIsEmptyString() throws ServletException, IOException {
      when(request.getHeader("X-Correlation-ID")).thenReturn("");

      filter.doFilterInternal(request, response, filterChain);

      verify(response).setHeader(eq("X-Correlation-ID"), matches("[a-f0-9-]{36}"));
      verify(filterChain).doFilter(request, response);
    }
  }
}
