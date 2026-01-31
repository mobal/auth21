package hu.squarelabs.auth21.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Component
public class MapAttributeConverter implements AttributeConverter<Map<String, Object>> {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public AttributeValue transformFrom(Map<String, Object> input) {
    if (input == null) {
      return AttributeValue.builder().nul(true).build();
    }
    try {
      final String jsonString = objectMapper.writeValueAsString(input);
      return AttributeValue.builder().s(jsonString).build();
    } catch (final Exception e) {
      throw new RuntimeException("Failed to serialize map to JSON", e);
    }
  }

  @Override
  public Map<String, Object> transformTo(AttributeValue input) {
    if (input == null || input.nul() != null && input.nul()) {
      return null;
    }
    if (input.s() == null) {
      return new HashMap<>();
    }
    try {
      return objectMapper.readValue(input.s(), Map.class);
    } catch (final Exception e) {
      throw new RuntimeException("Failed to deserialize JSON to map", e);
    }
  }

  @Override
  public EnhancedType<Map<String, Object>> type() {
    return EnhancedType.mapOf(String.class, Object.class);
  }

  @Override
  public AttributeValueType attributeValueType() {
    return AttributeValueType.S;
  }
}
