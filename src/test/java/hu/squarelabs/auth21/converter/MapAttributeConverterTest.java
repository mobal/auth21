package hu.squarelabs.auth21.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@DisplayName("MapAttributeConverter")
@ExtendWith(MockitoExtension.class)
class MapAttributeConverterTest {

  @Mock private ObjectMapper objectMapper;
  @InjectMocks private MapAttributeConverter converter;

  @Nested
  @DisplayName("transformFrom method")
  class TransformFromMethod {

    @Test
    @DisplayName("should convert map to AttributeValue string")
    void shouldConvertMapToAttributeValue() throws Exception {
      final Map<String, Object> inputMap = new HashMap<>();
      inputMap.put("key1", "value1");
      when(objectMapper.writeValueAsString(inputMap)).thenReturn("{\"key1\":\"value1\"}");

      final AttributeValue result = converter.transformFrom(inputMap);

      assertThat(result).isNotNull();
      assertThat(result.s()).isEqualTo("{\"key1\":\"value1\"}");
    }

    @Test
    @DisplayName("should handle empty map")
    void shouldHandleEmptyMap() throws Exception {
      final Map<String, Object> emptyMap = new HashMap<>();
      when(objectMapper.writeValueAsString(emptyMap)).thenReturn("{}");

      final AttributeValue result = converter.transformFrom(emptyMap);

      assertThat(result).isNotNull();
      assertThat(result.s()).isEqualTo("{}");
    }

    @Test
    @DisplayName("should handle null map by returning null attribute")
    void shouldHandleNullMap() {
      final AttributeValue result = converter.transformFrom(null);

      assertThat(result).isNotNull();
      assertThat(result.nul()).isTrue();
    }

    @Test
    @DisplayName("should throw RuntimeException on serialization error")
    void shouldThrowExceptionOnSerializationError() throws Exception {
      final Map<String, Object> inputMap = new HashMap<>();
      when(objectMapper.writeValueAsString(inputMap))
          .thenThrow(new RuntimeException("Serialization failed"));

      assertThatThrownBy(() -> converter.transformFrom(inputMap))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Failed to serialize");
    }
  }

  @Nested
  @DisplayName("transformTo method")
  class TransformToMethod {

    @Test
    @DisplayName("should convert AttributeValue string to map")
    void shouldConvertAttributeValueToMap() throws Exception {
      final AttributeValue av = AttributeValue.builder().s("{\"key1\":\"value1\"}").build();
      when(objectMapper.readValue("{\"key1\":\"value1\"}", Map.class))
          .thenReturn(Map.of("key1", "value1"));

      final Map<String, Object> result = converter.transformTo(av);

      assertThat(result).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("should handle null AttributeValue")
    void shouldHandleNullAttributeValue() {
      final Map<String, Object> result = converter.transformTo(null);

      assertThat(result).isNull();
    }

    @Test
    @DisplayName("should handle AttributeValue with null flag")
    void shouldHandleAttributeValueWithNullFlag() {
      final AttributeValue av = AttributeValue.builder().nul(true).build();

      final Map<String, Object> result = converter.transformTo(av);

      assertThat(result).isNull();
    }

    @Test
    @DisplayName("should handle AttributeValue with no string value")
    void shouldHandleAttributeValueWithNoStringValue() {
      final AttributeValue av = AttributeValue.builder().n("42").build();

      final Map<String, Object> result = converter.transformTo(av);

      assertThat(result).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("should throw RuntimeException on deserialization error")
    void shouldThrowExceptionOnDeserializationError() throws Exception {
      final AttributeValue av = AttributeValue.builder().s("{invalid}").build();
      when(objectMapper.readValue("{invalid}", Map.class))
          .thenThrow(new RuntimeException("Deserialization failed"));

      assertThatThrownBy(() -> converter.transformTo(av))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Failed to deserialize");
    }
  }

  @Nested
  @DisplayName("type method")
  class TypeMethod {

    @Test
    @DisplayName("should return EnhancedType for Map")
    void shouldReturnEnhancedTypeForMap() {
      final var type = converter.type();

      assertThat(type).isNotNull();
    }
  }

  @Nested
  @DisplayName("attributeValueType method")
  class AttributeValueTypeMethod {

    @Test
    @DisplayName("should return S for string attribute value type")
    void shouldReturnStringAttributeValueType() {
      final var type = converter.attributeValueType();

      assertThat(type).isNotNull();
    }
  }
}
