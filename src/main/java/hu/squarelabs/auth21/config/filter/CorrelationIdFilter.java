package hu.squarelabs.auth21.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(1)
public class CorrelationIdFilter extends OncePerRequestFilter {

  public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
  public static final String CORRELATION_ID_MDC_KEY = "correlationId";

  @Override
  protected void doFilterInternal(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final FilterChain filterChain)
      throws ServletException, IOException {

    try {
      final String correlationId = getOrGenerateCorrelationId(request);
      ThreadContext.put(CORRELATION_ID_MDC_KEY, correlationId);
      response.setHeader(CORRELATION_ID_HEADER, correlationId);
      filterChain.doFilter(request, response);

    } finally {
      ThreadContext.remove(CORRELATION_ID_MDC_KEY);
      ThreadContext.clearAll();
    }
  }

  private String getOrGenerateCorrelationId(HttpServletRequest request) {
    final String correlationId = request.getHeader(CORRELATION_ID_HEADER);
    return (correlationId != null && !correlationId.isBlank())
        ? correlationId
        : UUID.randomUUID().toString();
  }
}
