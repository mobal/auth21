package hu.squarelabs.auth21.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@DisplayName("MapAttributeConverter")
class MapAttributeConverterTest {

  private MapAttributeConverter converter;

  @Nested
  @DisplayName("transformFrom method")
  class TransformFromMethod {

    @Test
    @DisplayName("should convert map to AttributeValue string")
    void shouldConvertMapToAttributeValue() throws Exception {
      converter = new MapAttributeConverter();
      final Map<String, Object> inputMap = new HashMap<>();
      inputMap.put("key1", "value1");

      final AttributeValue result = converter.transformFrom(inputMap);

      assertThat(result).isNotNull();
      assertThat(result.s()).contains("key1").contains("value1");
    }

    @Test
    @DisplayName("should handle empty map")
    void shouldHandleEmptyMap() throws Exception {
      converter = new MapAttributeConverter();
      final Map<String, Object> emptyMap = new HashMap<>();

      final AttributeValue result = converter.transformFrom(emptyMap);

      assertThat(result).isNotNull();
      assertThat(result.s()).isEqualTo("{}");
    }

    @Test
    @DisplayName("should handle null map by returning null attribute")
    void shouldHandleNullMap() {
      converter = new MapAttributeConverter();
      final AttributeValue result = converter.transformFrom(null);

      assertThat(result).isNotNull();
      assertThat(result.nul()).isTrue();
    }
  }

  @Nested
  @DisplayName("transformTo method")
  class TransformToMethod {

    @Test
    @DisplayName("should convert AttributeValue string to map")
    void shouldConvertAttributeValueToMap() throws Exception {
      converter = new MapAttributeConverter();
      final AttributeValue av = AttributeValue.builder().s("{\"key1\":\"value1\"}").build();

      final Map<String, Object> result = converter.transformTo(av);

      assertThat(result).isNotNull().isNotEmpty();
      assertThat(result).containsKey("key1");
    }

    @Test
    @DisplayName("should handle null AttributeValue")
    void shouldHandleNullAttributeValue() {
      converter = new MapAttributeConverter();
      final Map<String, Object> result = converter.transformTo(null);

      assertThat(result).isNull();
    }

    @Test
    @DisplayName("should handle AttributeValue with null flag")
    void shouldHandleAttributeValueWithNullFlag() {
      converter = new MapAttributeConverter();
      final AttributeValue av = AttributeValue.builder().nul(true).build();

      final Map<String, Object> result = converter.transformTo(av);

      assertThat(result).isNull();
    }

    @Test
    @DisplayName("should handle AttributeValue with no string value")
    void shouldHandleAttributeValueWithNoStringValue() {
      converter = new MapAttributeConverter();
      final AttributeValue av = AttributeValue.builder().n("42").build();

      final Map<String, Object> result = converter.transformTo(av);

      assertThat(result).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("should throw RuntimeException on deserialization error")
    void shouldThrowExceptionOnDeserializationError() throws Exception {
      converter = new MapAttributeConverter();
      final AttributeValue av = AttributeValue.builder().s("{invalid}").build();

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
      converter = new MapAttributeConverter();
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
      converter = new MapAttributeConverter();
      final var type = converter.attributeValueType();

      assertThat(type).isNotNull();
    }
  }
}
