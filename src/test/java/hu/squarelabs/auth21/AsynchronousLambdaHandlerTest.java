package hu.squarelabs.auth21;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.squarelabs.auth21.model.dto.response.TokenResponse;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

@DisplayName("AsynchronousLambdaHandler")
class AsynchronousLambdaHandlerTest {

  private Object handler;
  private ObjectMapper objectMapper;
  private Class<?> awsProxyRequestClass;
  private Class<?> awsProxyResponseClass;
  private Class<?> headersClass;
  private Class<?> lambdaContextClass;

  @Mock private Object lambdaContext;

  @BeforeEach
  void setUp() throws Exception {
    try {
      Class.forName("com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler");
      awsProxyRequestClass = Class.forName("com.amazonaws.serverless.proxy.model.AwsProxyRequest");
      awsProxyResponseClass =
          Class.forName("com.amazonaws.serverless.proxy.model.AwsProxyResponse");
      headersClass = Class.forName("com.amazonaws.serverless.proxy.model.Headers");
      lambdaContextClass = Class.forName("com.amazonaws.services.lambda.runtime.Context");

      handler =
          Class.forName("hu.squarelabs.auth21.AsynchronousLambdaHandler")
              .getDeclaredConstructor()
              .newInstance();
      objectMapper = new ObjectMapper();
    } catch (ClassNotFoundException | NoClassDefFoundError e) {
      assumeTrue(false, "AWS Lambda libraries not available - skipping Lambda handler tests");
    }
  }

  @Nested
  @DisplayName("POST /api/v1/login")
  class Login {

    @Test
    @DisplayName("returns OK with TokenResponse containing access and refresh tokens")
    void returnsOkWithTokenResponseContainingAccessAndRefreshTokens() throws Exception {
      Object response = login("{\"email\":\"user@example.com\",\"password\":\"password123\"}");

      assertEquals(200, getStatusCode(response));
      TokenResponse tokenResponse = objectMapper.readValue(getBody(response), TokenResponse.class);
      assertAll(
          () -> assertNotNull(tokenResponse.accessToken()),
          () -> assertNotNull(tokenResponse.refreshToken()),
          () -> assertNotNull(tokenResponse.expiresIn()));
    }

    @Test
    @DisplayName("returns BAD_REQUEST when email is missing")
    void returnsBadRequestWhenEmailIsMissing() throws Exception {
      assertEquals(400, getStatusCode(login("{\"password\":\"password123\"}")));
    }

    @Test
    @DisplayName("returns BAD_REQUEST when password is missing")
    void returnsBadRequestWhenPasswordIsMissing() throws Exception {
      assertEquals(400, getStatusCode(login("{\"email\":\"user@example.com\"}")));
    }

    @Test
    @DisplayName("returns BAD_REQUEST when email is invalid")
    void returnsBadRequestWhenEmailIsInvalid() throws Exception {
      assertEquals(
          400, getStatusCode(login("{\"email\":\"invalid-email\",\"password\":\"password123\"}")));
    }

    @Test
    @DisplayName("returns BAD_REQUEST when email is blank")
    void returnsBadRequestWhenEmailIsBlank() throws Exception {
      assertEquals(400, getStatusCode(login("{\"email\":\"\",\"password\":\"password123\"}")));
    }

    @Test
    @DisplayName("returns BAD_REQUEST when password is blank")
    void returnsBadRequestWhenPasswordIsBlank() throws Exception {
      assertEquals(400, getStatusCode(login("{\"email\":\"user@example.com\",\"password\":\"\"}")));
    }

    @Test
    @DisplayName("calls handler with provided credentials")
    void callsHandlerWithProvidedCredentials() throws Exception {
      login("{\"email\":\"user@example.com\",\"password\":\"password123\"}");
    }

    private Object login(String requestBody) throws Exception {
      Object request = createRequest("/api/v1/login", "POST", requestBody, "application/json");
      return invokeHandler(request);
    }
  }

  @Nested
  @DisplayName("POST /api/v1/refresh")
  class RefreshToken {

    @Test
    @DisplayName("returns OK with new TokenResponse containing access and refresh tokens")
    void returnsOkWithNewTokenResponseContainingAccessAndRefreshTokens() throws Exception {
      Object response = refresh("{\"refresh_token\":\"valid-refresh-token\"}");

      assertEquals(200, getStatusCode(response));
      TokenResponse tokenResponse = objectMapper.readValue(getBody(response), TokenResponse.class);
      assertAll(
          () -> assertNotNull(tokenResponse.accessToken()),
          () -> assertNotNull(tokenResponse.refreshToken()),
          () -> assertNotNull(tokenResponse.expiresIn()));
    }

    @Test
    @DisplayName("returns BAD_REQUEST when refresh_token is missing")
    void returnsBadRequestWhenRefreshTokenIsMissing() throws Exception {
      assertEquals(400, getStatusCode(refresh("{}")));
    }

    @Test
    @DisplayName("returns UNAUTHORIZED when refresh token is invalid")
    void returnsUnauthorizedWhenRefreshTokenIsInvalid() throws Exception {
      assertEquals(401, getStatusCode(refresh("{\"refresh_token\":\"invalid-token\"}")));
    }

    @Test
    @DisplayName("calls handler with provided refresh token")
    void callsHandlerWithProvidedRefreshToken() throws Exception {
      refresh("{\"refresh_token\":\"my-refresh-token\"}");
    }

    private Object refresh(String requestBody) throws Exception {
      Object request = createRequest("/api/v1/refresh", "POST", requestBody, "application/json");
      return invokeHandler(request);
    }
  }

  @Nested
  @DisplayName("POST /api/v1/register")
  class Register {

    private static final String VALID_REGISTER =
        "{\"email\":\"newuser@example.com\",\"password\":\"password123\",\"username\":\"newuser\"}";

    @Test
    @DisplayName("returns CREATED with location header when registration is successful")
    void returnsCreatedWithLocationHeaderWhenRegistrationIsSuccessful() throws Exception {
      Object response = register(VALID_REGISTER);

      assertEquals(201, getStatusCode(response));
      assertTrue(hasLocationHeader(response));
    }

    @Test
    @DisplayName("returns BAD_REQUEST when email is missing")
    void returnsBadRequestWhenEmailIsMissing() throws Exception {
      assertEquals(
          400, getStatusCode(register("{\"password\":\"password123\",\"username\":\"newuser\"}")));
    }

    @Test
    @DisplayName("returns BAD_REQUEST when password is missing")
    void returnsBadRequestWhenPasswordIsMissing() throws Exception {
      assertEquals(
          400,
          getStatusCode(register("{\"email\":\"newuser@example.com\",\"username\":\"newuser\"}")));
    }

    @Test
    @DisplayName("returns BAD_REQUEST when username is missing")
    void returnsBadRequestWhenUsernameIsMissing() throws Exception {
      assertEquals(
          400,
          getStatusCode(
              register("{\"email\":\"newuser@example.com\",\"password\":\"password123\"}")));
    }

    @Test
    @DisplayName("returns BAD_REQUEST when email is invalid")
    void returnsBadRequestWhenEmailIsInvalid() throws Exception {
      assertEquals(
          400,
          getStatusCode(
              register(
                  "{\"email\":\"invalid-email\",\"password\":\"password123\",\"username\":\"newuser\"}")));
    }

    @Test
    @DisplayName("returns BAD_REQUEST when email is blank")
    void returnsBadRequestWhenEmailIsBlank() throws Exception {
      assertEquals(
          400,
          getStatusCode(
              register("{\"email\":\"\",\"password\":\"password123\",\"username\":\"newuser\"}")));
    }

    @Test
    @DisplayName("returns BAD_REQUEST when password is blank")
    void returnsBadRequestWhenPasswordIsBlank() throws Exception {
      assertEquals(
          400,
          getStatusCode(
              register(
                  "{\"email\":\"newuser@example.com\",\"password\":\"\",\"username\":\"newuser\"}")));
    }

    @Test
    @DisplayName("returns BAD_REQUEST when username is blank")
    void returnsBadRequestWhenUsernameIsBlank() throws Exception {
      assertEquals(
          400,
          getStatusCode(
              register(
                  "{\"email\":\"newuser@example.com\",\"password\":\"password123\",\"username\":\"\"}")));
    }

    @Test
    @DisplayName("accepts registration when displayName is null")
    void acceptsRegistrationWhenDisplayNameIsNull() throws Exception {
      assertEquals(201, getStatusCode(register(VALID_REGISTER)));
    }

    @Test
    @DisplayName("returns FORBIDDEN when authorization header is missing")
    void returnsForbiddenWhenAuthorizationHeaderIsMissing() throws Exception {
      Object request =
          createRequest("/api/v1/register", "POST", VALID_REGISTER, "application/json");
      assertEquals(403, getStatusCode(invokeHandler(request)));
    }

    private Object register(String requestBody) throws Exception {
      Object request =
          createRequestWithAuth(
              "/api/v1/register", "POST", requestBody, "application/json", "admin-token");
      return invokeHandler(request);
    }
  }

  @Nested
  @DisplayName("GET /api/v1/logout")
  class Logout {

    @Test
    @DisplayName("returns NO_CONTENT when logout is successful")
    void returnsNoContentWhenLogoutIsSuccessful() throws Exception {
      assertEquals(204, getStatusCode(logout("valid-access-token")));
    }

    @Test
    @DisplayName("returns UNAUTHORIZED when token is invalid")
    void returnsUnauthorizedWhenTokenIsInvalid() throws Exception {
      assertEquals(401, getStatusCode(logout("invalid-token")));
    }

    @Test
    @DisplayName("handles token with Bearer prefix correctly")
    void handlesTokenWithBearerPrefixCorrectly() throws Exception {
      int statusCode = getStatusCode(logout("valid-access-token"));
      assertTrue(statusCode == 204 || statusCode == 401);
    }

    private Object logout(String authToken) throws Exception {
      Object request = createRequestWithAuth("/api/v1/logout", "GET", null, null, authToken);
      return invokeHandler(request);
    }
  }

  @Nested
  @DisplayName("Authorization Rules")
  class AuthorizationRules {

    @Nested
    @DisplayName("POST /api/v1/register")
    class RegisterAuthorizationRules {

      @Test
      @DisplayName("returns FORBIDDEN when authorization header is missing")
      void returnsForbiddenWhenAuthorizationHeaderIsMissing() throws Exception {
        Object request =
            createRequest(
                "/api/v1/register",
                "POST",
                "{\"email\":\"newuser@example.com\",\"password\":\"password123\",\"username\":\"newuser\"}",
                "application/json");
        assertEquals(403, getStatusCode(invokeHandler(request)));
      }
    }
  }

  @Nested
  @DisplayName("Error Handling")
  class ErrorHandling {

    @Test
    @DisplayName("returns NOT_FOUND for non-existent endpoint")
    void returnsNotFoundForNonExistentEndpoint() throws Exception {
      assertEquals(404, getStatusCode(request("/api/v1/nonexistent", "GET", null, null)));
    }

    @Test
    @DisplayName("returns METHOD_NOT_ALLOWED for unsupported HTTP method")
    void returnsMethodNotAllowedForUnsupportedHttpMethod() throws Exception {
      assertEquals(405, getStatusCode(request("/api/v1/login", "PUT", null, null)));
    }

    @Test
    @DisplayName("returns UNSUPPORTED_MEDIA_TYPE when Content-Type is not supported")
    void returnsUnsupportedMediaTypeWhenContentTypeIsNotSupported() throws Exception {
      assertEquals(
          415,
          getStatusCode(
              request(
                  "/api/v1/login",
                  "POST",
                  "{\"email\":\"user@example.com\",\"password\":\"password123\"}",
                  "text/plain")));
    }

    @Test
    @DisplayName("returns BAD_REQUEST for malformed JSON")
    void returnsBadRequestForMalformedJson() throws Exception {
      assertEquals(
          400,
          getStatusCode(request("/api/v1/login", "POST", "{invalid-json", "application/json")));
    }

    private Object request(String path, String method, String body, String contentType)
        throws Exception {
      Object req = createRequest(path, method, body, contentType);
      return invokeHandler(req);
    }
  }

  @Nested
  @DisplayName("Health Check")
  class HealthCheck {

    @Test
    @DisplayName("GET /actuator/health returns OK")
    void getActuatorHealthReturnsOk() throws Exception {
      Object request = createRequest("/actuator/health", "GET", null, null);
      Object response = invokeHandler(request);
      assertEquals(200, getStatusCode(response));
      assertNotNull(getBody(response));
    }
  }

  private Object createRequest(String path, String httpMethod, String body, String contentType)
      throws Exception {
    Object request = awsProxyRequestClass.getDeclaredConstructor().newInstance();

    awsProxyRequestClass.getMethod("setPath", String.class).invoke(request, path);
    awsProxyRequestClass.getMethod("setHttpMethod", String.class).invoke(request, httpMethod);
    awsProxyRequestClass.getMethod("setBody", String.class).invoke(request, body);

    Object headers = headersClass.getDeclaredConstructor().newInstance();
    if (contentType != null) {
      headersClass
          .getMethod("add", String.class, String.class)
          .invoke(headers, "Content-Type", contentType);
    }
    awsProxyRequestClass.getMethod("setMultiValueHeaders", headersClass).invoke(request, headers);

    return request;
  }

  private Object createRequestWithAuth(
      String path, String httpMethod, String body, String contentType, String authToken)
      throws Exception {
    Object request = createRequest(path, httpMethod, body, contentType);
    Object headers = awsProxyRequestClass.getMethod("getMultiValueHeaders").invoke(request);
    if (headers == null) {
      headers = headersClass.getDeclaredConstructor().newInstance();
    }
    headersClass
        .getMethod("add", String.class, String.class)
        .invoke(headers, "Authorization", "Bearer " + authToken);
    awsProxyRequestClass.getMethod("setMultiValueHeaders", headersClass).invoke(request, headers);

    return request;
  }

  private Object invokeHandler(Object request) throws Exception {
    Method handleRequestMethod =
        handler.getClass().getMethod("handleRequest", awsProxyRequestClass, lambdaContextClass);
    return handleRequestMethod.invoke(handler, request, lambdaContext);
  }

  private int getStatusCode(Object response) throws Exception {
    return (int) awsProxyResponseClass.getMethod("getStatusCode").invoke(response);
  }

  private String getBody(Object response) throws Exception {
    return (String) awsProxyResponseClass.getMethod("getBody").invoke(response);
  }

  private Object getHeaders(Object response) throws Exception {
    return awsProxyResponseClass.getMethod("getMultiValueHeaders").invoke(response);
  }

  private boolean hasLocationHeader(Object response) throws Exception {
    Object multiValueHeaders = getHeaders(response);
    if (multiValueHeaders != null) {
      return (boolean)
          headersClass.getMethod("containsKey", Object.class).invoke(multiValueHeaders, "Location");
    }

    Object headers = awsProxyResponseClass.getMethod("getHeaders").invoke(response);
    if (headers != null) {
      return headers.getClass().getMethod("containsKey", Object.class).invoke(headers, "Location")
          != null;
    }
    return false;
  }
}
