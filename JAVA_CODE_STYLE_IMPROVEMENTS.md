# Java Code Style Improvements

**Date:** January 29, 2026  
**Project:** auth21  
**Focus:** Variable Declaration Best Practices

---

## Overview

This document summarizes code quality improvements made to the auth21 project, focusing on two key Java features for better code readability and maintainability:

1. **Final Keyword** - Promoting immutability
2. **Var Keyword** - Improving readability

---

## 1. Final Keyword Improvements

### Purpose

The `final` keyword prevents accidental reassignment of variables, making code more predictable and thread-safe.

### Where to Use `final`

✅ **SHOULD USE:**

- **Local Variables** - Variables not reassigned after initialization:
  ```java
  final var instant = Instant.now();
  final String email = "user@example.com";
  ```

- **Loop Variables** - Prevent modification within loops:
  ```java
  for (final byte b : bytes) {
    sb.append(String.format("%02x", b));
  }
  ```

- **Method Parameters** - Especially in callback/lambda contexts:
  ```java
  protected void doFilterInternal(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final FilterChain filterChain) { }
  ```

- **Exception Variables** - In catch blocks:
  ```java
  catch (final Exception e) {
    throw new RuntimeException("Error", e);
  }
  ```

- **Lambda Parameters** - When not trivial:
  ```java
  .map(entity -> {
    final Map<String, Object> result = new HashMap<>();
    return result;
  })
  ```

❌ **SHOULD NOT USE:**

- **Field Variables with Setters** - Entity/DTO classes:
  ```java
  // ❌ NO - needs setter
  private final String id;
  
  // ✅ YES - allows Spring/ORM to populate
  private String id;
  ```

- **Constructor/Method Parameters that are Reassigned** - Rare cases where reassignment is intentional

- **Variables in Conditional Branches** - When variable is assigned in different branches:
  ```java
  // May not always assign
  var expTime;
  if (exp == null) {
    expTime = iat.plusSeconds(jwtTokenLifetime);
  } else {
    expTime = iat.plusSeconds(exp);
  }
  ```

### Changes Made to Project

#### Service Classes
- ✅ `AuthService.java` - Made local variables final in token generation methods
- ✅ `TokenService.java` - Made variables final in create/retrieve operations

#### Configuration & Filters
- ✅ `CorrelationIdFilter.java` - Added final to method parameters and local variables

#### Converters & Handlers
- ✅ `MapAttributeConverter.java` - Made exception variables final
- ✅ `GlobalExceptionHandler.java` - Added final to exception parameter

#### Test Classes
- ✅ `AuthServiceTest.java` - Applied final to all test variables
- ✅ `TokenServiceTest.java` - Applied final to all test variables

---

## 2. Var Keyword Best Practices

### Purpose

The `var` keyword (Java 10+) reduces verbosity while maintaining type safety. The type is inferred from the right-hand side.

### When to Use `var`

✅ **SHOULD USE:**

1. **Clear Constructor Calls** - Type is explicit from the right side:
   ```java
   final var instant = Instant.now();           // Type: Instant
   final var tokenEntity = new TokenEntity();   // Type: TokenEntity
   final var jwtToken = new JwtToken();         // Type: JwtToken
   final var bytes = new byte[length];          // Type: byte[]
   ```

2. **Method Returns with Obvious Types**:
   ```java
   final var user = userOpt.get();              // Type: UserEntity
   final var map = Map.of(...);                 // Type: Map<String, Object>
   final var passwordValid = verifyPassword(); // Type: boolean
   ```

3. **Variable Names are Self-Documenting**:
   ```java
   final var newJwtToken = generateToken(...);           // Clear from method name
   final var refreshToken = generateRefreshToken(16);   // Clear intent
   ```

4. **Chained Method Calls** - When the final type is clear:
   ```java
   final var userOpt = userRepository.findByEmail(email);  // Type: Optional<UserEntity>
   final var itemOpt = tokenService.getByRefreshToken(token);
   ```

❌ **SHOULD NOT USE:**

1. **Without Initializer** - Cannot infer type:
   ```java
   // ❌ COMPILE ERROR - var requires initialization
   var expTime;
   expTime = iat.plusSeconds(jwtTokenLifetime);
   
   // ✅ YES - Explicit type with conditional assignment
   final Instant expTime;
   if (exp == null) {
    expTime = iat.plusSeconds(jwtTokenLifetime);
   } else {
    expTime = iat.plusSeconds(exp);
   }
   ```

2. **Generic Collections with Ambiguous Types**:
   ```java
   // ❌ COMPILE ERROR - HashMap infers HashMap<Object, Object>
   final var jwtTokenMap = new HashMap<>();
   tokenEntity.setJwtToken(jwtTokenMap);  // Expects Map<String, Object>
   
   // ✅ YES - Type parameter specified
   final Map<String, Object> jwtTokenMap = new HashMap<>();
   ```

3. **String, Primitive Types** - Better explicit in business logic:
   ```java
   // Less clear
   final var email = "test@example.com";
   
   // Better for clarity
   final String email = "test@example.com";
   ```

4. **Field Declarations** - Not allowed by language:
   ```java
   // ❌ COMPILE ERROR
   private var authService;
   
   // ✅ YES
   private final AuthService authService;
   ```

5. **Method/Constructor Parameters** - Not allowed by language:
   ```java
   // ❌ COMPILE ERROR
   public void login(var email, var password) { }
   
   // ✅ YES
   public void login(String email, String password) { }
   ```

6. **Lambda Parameters** - Usually better explicit for readability:
   ```java
   // Less clear
   .forEach(item -> {})
   
   // Better for clarity
   .forEach((String item) -> {})
   ```

### Current Project Usage (Balanced Approach)

✅ **Uses `var` for:**
- `Instant.now()` - Method return type is explicit
- Constructor calls - `new JwtToken()`, `new TokenEntity()`, `new StringBuilder()`
- `Map.of()` - Map interface returned is clear
- Optional operations - `.get()` on known types
- Byte arrays - `new byte[]` type is obvious

✅ **Uses explicit types for:**
- `HashMap<String, Object>` - Avoids type inference issues
- `String`, `int` primitives - Better clarity in code
- Entity classes in tests - Self-documentation
- Method return types from chains - When ambiguous

---

## Code Quality Benefits

### Before Improvements
```java
// Less safe - can be accidentally reassigned
String email = "test@example.com";
Map userData = new HashMap();
Instant expTime;  // if-else assignment

// More verbose
ObjectMapper objectMapper = getObjectMapper();
TokenEntity entity = new TokenEntity();
```

### After Improvements
```java
// Safe - cannot be reassigned
final String email = "test@example.com";
final Map<String, Object> userData = new HashMap<>();
final Instant expTime;  // if-else assignment (requires explicit type)

// Less verbose where appropriate
final var objectMapper = getObjectMapper();
final var entity = new TokenEntity();
```

---

## Files Modified

### Source Code
- `src/main/java/hu/squarelabs/auth21/service/AuthService.java`
- `src/main/java/hu/squarelabs/auth21/service/TokenService.java`
- `src/main/java/hu/squarelabs/auth21/config/filter/CorrelationIdFilter.java`
- `src/main/java/hu/squarelabs/auth21/converter/MapAttributeConverter.java`
- `src/main/java/hu/squarelabs/auth21/exception/GlobalExceptionHandler.java`

### Test Code
- `src/test/java/hu/squarelabs/auth21/service/AuthServiceTest.java`
- `src/test/java/hu/squarelabs/auth21/service/TokenServiceTest.java`

---

## Impact Summary

| Aspect | Impact |
|--------|--------|
| **Immutability** | Improved - Variables marked final prevent accidental modification |
| **Readability** | Enhanced - `var` reduces verbosity while maintaining clarity |
| **Type Safety** | Maintained - Explicit types used where needed for safety |
| **Maintainability** | Better - Code intent is clearer with strategic use of keywords |
| **IDE Support** | Better - Type inference helps with autocomplete and refactoring |
| **Build Status** | ✅ All tests passing |

---

## Key Takeaways

1. **Use `final` liberally** on local variables to prevent bugs
2. **Use `var` judiciously** where type is obvious from context
3. **Keep explicit types** for clarity when type is not immediately obvious
4. **Follow language constraints** - `var` not allowed for fields or parameters
5. **Prioritize readability** - Sometimes verbosity is worth the clarity

---

## References

- [JEP 286: Local-Variable Type Inference](https://openjdk.java.net/jeps/286)
- [Java Coding Style Guide - Final Variables](https://google.github.io/styleguide/javaguide.html#s4.8.2.1-finals)
- [Effective Java - Item 17: Minimize Mutability](https://www.oreilly.com/library/view/effective-java-3rd/9780134685991/)

