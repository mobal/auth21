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

  private final MapAttributeConverter converter = new MapAttributeConverter();

  @Nested
  @DisplayName("transformFrom")
  class TransformFrom {

    @Test
    @DisplayName("converts map to AttributeValue string")
    void convertsMapToAttributeValueString() throws Exception {
      Map<String, Object> inputMap = Map.of("key1", "value1");

      AttributeValue result = converter.transformFrom(inputMap);

      assertThat(result.s()).contains("key1").contains("value1");
    }

    @Test
    @DisplayName("handles empty map")
    void handlesEmptyMap() throws Exception {
      AttributeValue result = converter.transformFrom(new HashMap<>());

      assertThat(result.s()).isEqualTo("{}");
    }

    @Test
    @DisplayName("returns null attribute for null map")
    void returnsNullAttributeForNullMap() {
      AttributeValue result = converter.transformFrom(null);

      assertThat(result.nul()).isTrue();
    }
  }

  @Nested
  @DisplayName("transformTo")
  class TransformTo {

    @Test
    @DisplayName("converts AttributeValue string to map")
    void convertsAttributeValueStringToMap() throws Exception {
      AttributeValue av = AttributeValue.builder().s("{\"key1\":\"value1\"}").build();

      Map<String, Object> result = converter.transformTo(av);

      assertThat(result).containsKey("key1");
    }

    @Test
    @DisplayName("handles null AttributeValue")
    void handlesNullAttributeValue() {
      Map<String, Object> result = converter.transformTo(null);

      assertThat(result).isNull();
    }

    @Test
    @DisplayName("handles AttributeValue with null flag")
    void handlesAttributeValueWithNullFlag() {
      AttributeValue av = AttributeValue.builder().nul(true).build();

      Map<String, Object> result = converter.transformTo(av);

      assertThat(result).isNull();
    }

    @Test
    @DisplayName("handles AttributeValue with no string value")
    void handlesAttributeValueWithNoStringValue() {
      AttributeValue av = AttributeValue.builder().n("42").build();

      Map<String, Object> result = converter.transformTo(av);

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("throws RuntimeException on deserialization error")
    void throwsRuntimeExceptionOnDeserializationError() throws Exception {
      AttributeValue av = AttributeValue.builder().s("{invalid}").build();

      assertThatThrownBy(() -> converter.transformTo(av))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Failed to deserialize");
    }
  }

  @Nested
  @DisplayName("type")
  class Type {

    @Test
    @DisplayName("returns EnhancedType for Map")
    void returnsEnhancedTypeForMap() {
      var type = converter.type();

      assertThat(type).isNotNull();
    }
  }

  @Nested
  @DisplayName("attributeValueType")
  class AttributeValueType {

    @Test
    @DisplayName("returns S for string attribute value type")
    void returnsSForStringAttributeValueType() {
      var type = converter.attributeValueType();

      assertThat(type).isNotNull();
    }
  }
}
