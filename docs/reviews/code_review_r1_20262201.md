# Code Review - User Access Management Service

## Tổng quan
Dự án là một microservices architecture với 3 modules: `auth-service`, `user-service`, và `core-service`. Sử dụng Spring Boot 3.4.7, PostgreSQL, JWT authentication.

---

## 1. CODING CONVENTIONS

### ✅ Điểm tốt
- Sử dụng Lombok để giảm boilerplate code
- Package structure rõ ràng theo domain
- Sử dụng `@FieldDefaults` để enforce immutability

### ❌ Vấn đề cần cải thiện

#### 1.1 Naming Conventions
```java
// ❌ BAD: Repository tên không nhất quán
public interface AuthRepository extends JpaRepository<User, UUID>
public interface UserRepository extends JpaRepository<UserProfiles, String>  // Inconsistent generic type

// ✅ GOOD: Nên đặt tên nhất quán
public interface UserRepository extends JpaRepository<User, UUID>
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID>
```

#### 1.2 Hardcoded Strings
```java
// ❌ BAD: Hardcoded role name
Role defaultRole = roleRepository.findByName("ROLE_USER")
    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

// ✅ GOOD: Sử dụng constants
public class RoleConstants {
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
}
```

#### 1.3 Missing JavaDoc
- Tất cả các public methods, classes cần JavaDoc
- Đặc biệt quan trọng cho API endpoints

#### 1.4 Inconsistent Return Types
```java
// ❌ BAD: Repository có generic type khác nhau
JpaRepository<UserProfiles, String>  // Should be UUID
JpaRepository<User, UUID>
```

#### 1.5 Magic Numbers
```java
// ❌ BAD: Magic number trong SecurityConfig
return new BCryptPasswordEncoder(10);  // Should be constant

// ✅ GOOD
private static final int BCRYPT_STRENGTH = 10;
```

---

## 2. TESTING

### ✅ Điểm tốt
- Có Unit tests, Integration tests, E2E tests
- Sử dụng Testcontainers cho integration tests
- Sử dụng Mockito cho unit tests

### ❌ Vấn đề cần cải thiện

#### 2.1 Test Coverage
- **Thiếu test cases:**
  - `introspect()` method với invalid token
  - `getMe()` với unauthenticated user
  - `update()` với partial updates
  - `delete()` với unauthorized access
  - Edge cases: null values, empty strings, boundary values

#### 2.2 Test Naming
```java
// ❌ BAD: Tên test không mô tả rõ scenario
@Test
void register_success()

// ✅ GOOD: Nên mô tả rõ hơn
@Test
void register_shouldCreateUserWithDefaultRole_whenValidRequest()
```

#### 2.3 Missing Test Data Builders
```java
// ❌ BAD: Tạo test data trực tiếp trong test
RegisterRequest request = new RegisterRequest("user_unit", "password", "");

// ✅ GOOD: Sử dụng Test Data Builder pattern
RegisterRequest request = RegisterRequestTestBuilder.defaultRequest()
    .withUsername("user_unit")
    .build();
```

#### 2.4 Missing Validation Tests
- Không có tests cho:
  - Input validation (null, empty, invalid format)
  - Password strength validation
  - Email format validation
  - Username constraints

#### 2.5 Missing Security Tests
- Không test SQL injection
- Không test XSS
- Không test authentication bypass
- Không test authorization checks

#### 2.6 Test Organization
```java
// ❌ BAD: Tất cả tests trong một class
class AuthServiceUnitTest {
    // 10+ test methods
}

// ✅ GOOD: Tách theo feature
class AuthServiceRegisterTest
class AuthServiceLoginTest
class AuthServiceIntrospectTest
```

---

## 3. SOLID PRINCIPLES

### 3.1 Single Responsibility Principle (SRP)

#### ❌ Vấn đề
```java
// AuthServiceImpl làm quá nhiều việc:
// 1. Business logic
// 2. Role management
// 3. Token generation
// 4. Authentication
public class AuthServiceImpl implements AuthService {
    // Should be split into:
    // - UserRegistrationService
    // - AuthenticationService  
    // - TokenService (already exists but not used properly)
}
```

#### ✅ Giải pháp
```java
@Service
public class UserRegistrationService {
    public String register(RegisterRequest request) { ... }
}

@Service  
public class AuthenticationService {
    public AuthResponse login(LoginRequest request) { ... }
}

@Service
public class TokenIntrospectionService {
    public IntrospectResponse introspect(IntrospectRequest request) { ... }
}
```

### 3.2 Open/Closed Principle (OCP)

#### ❌ Vấn đề
```java
// Hard to extend - phải sửa code khi thêm role mới
if (!roleRequest.startsWith("ROLE_")) {
    roleRequest = "ROLE_" + roleRequest;
}
```

#### ✅ Giải pháp
```java
// Sử dụng Strategy pattern
public interface RoleValidator {
    boolean isValid(String role);
    String normalize(String role);
}

@Component
public class DefaultRoleValidator implements RoleValidator { ... }
```

### 3.3 Liskov Substitution Principle (LSP)
✅ OK - Interfaces được implement đúng

### 3.4 Interface Segregation Principle (ISP)
✅ OK - Interfaces không quá lớn

### 3.5 Dependency Inversion Principle (DIP)
✅ OK - Depend on abstractions (interfaces)

---

## 4. DESIGN PATTERNS

### ❌ Thiếu Design Patterns

#### 4.1 Factory Pattern
```java
// ❌ BAD: Tạo User trực tiếp trong service
User user = AuthMapper.toUser(request);

// ✅ GOOD: Sử dụng Factory
@Component
public class UserFactory {
    public User createUser(RegisterRequest request, Set<Role> roles) {
        return User.builder()
            .id(UUID.randomUUID())
            .username(request.getUsername())
            .roles(roles)
            .build();
    }
}
```

#### 4.2 Strategy Pattern cho Validation
```java
// ✅ GOOD: Strategy pattern cho validation
public interface ValidationStrategy<T> {
    ValidationResult validate(T input);
}

@Component
public class UsernameValidationStrategy implements ValidationStrategy<String> { ... }
@Component
public class PasswordValidationStrategy implements ValidationStrategy<String> { ... }
```

#### 4.3 Builder Pattern
✅ Đã sử dụng tốt với Lombok `@Builder`

#### 4.4 Repository Pattern
⚠️ Cần cải thiện:
```java
// ❌ BAD: Repository extends JpaRepository trực tiếp
// Nên có custom repository interface
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
}

// ✅ GOOD: Tách custom methods
public interface UserRepositoryCustom {
    Optional<User> findByUsernameWithRoles(String username);
}
```

---

## 5. CLEAN CODE

### ❌ Vấn đề

#### 5.1 Long Methods
```java
// ❌ BAD: Method quá dài (>20 lines)
@Override
public String register(RegisterRequest request) {
    // 30+ lines of code
}

// ✅ GOOD: Tách thành smaller methods
@Override
public String register(RegisterRequest request) {
    validateRegistrationRequest(request);
    User user = createUserFromRequest(request);
    Set<Role> roles = assignRoles(request);
    user.setRoles(roles);
    return saveUser(user);
}
```

#### 5.2 Code Duplication
```java
// ❌ BAD: Duplicate code trong UserServiceImpl
private UUID getUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (!(auth instanceof JwtAuthenticationToken jwt)) {
        throw new AppException(ErrorCode.UNAUTHORIZED);
    }
    return UUID.fromString(jwt.getName());
}

// Tương tự trong AuthServiceImpl.getMe()
// ✅ GOOD: Extract thành utility class
@Component
public class SecurityContextHelper {
    public UUID getCurrentUserId() { ... }
    public String getCurrentUsername() { ... }
    public List<String> getCurrentRoles() { ... }
}
```

#### 5.3 Magic Strings/Numbers
```java
// ❌ BAD: Magic strings
if (!roleRequest.startsWith("ROLE_")) {
    roleRequest = "ROLE_" + roleRequest;
}

// ✅ GOOD: Constants
public class RoleConstants {
    public static final String ROLE_PREFIX = "ROLE_";
}
```

#### 5.4 Missing Input Validation
```java
// ❌ BAD: Không validate input
public String register(RegisterRequest request) {
    // No validation
}

// ✅ GOOD: Validate input
public String register(RegisterRequest request) {
    validateRequest(request);
    // ...
}

private void validateRequest(RegisterRequest request) {
    if (request.getUsername() == null || request.getUsername().isBlank()) {
        throw new AppException(ErrorCode.INVALID_REQUEST);
    }
    // More validations
}
```

#### 5.5 Error Handling
```java
// ❌ BAD: Generic exception handling
catch (Exception e) {
    log.warn("Token introspect failed");
    return IntrospectResponse.builder().valid(false).build();
}

// ✅ GOOD: Specific exception handling
catch (JwtException e) {
    log.warn("Token introspect failed: {}", e.getMessage());
    return IntrospectResponse.builder().valid(false).build();
} catch (Exception e) {
    log.error("Unexpected error during token introspection", e);
    throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
}
```

#### 5.6 Commented Code
```java
// ❌ BAD: Commented code trong AuthMapper
//    public static UserResponse toUserResponse(User user) {
//        ...
//    }

// ✅ GOOD: Xóa hoặc uncomment và sử dụng
```

---

## 6. PRODUCTION STANDARDS

### ❌ Vấn đề nghiêm trọng

#### 6.1 Security Issues

##### Hardcoded Secrets
```yaml
# ❌ BAD: Hardcoded JWT secret trong application.yaml
jwt:
  signerKey: qxDRHYT3pRMIJkG7pFsaUkbSkFr3+X3hi3n1ci64B7mAS7RH+Ws4V0ao/nHyyBgGWZY2FpEKCgTXNUpf79t2Tw==
```

**✅ Giải pháp:**
- Sử dụng environment variables
- Sử dụng Spring Cloud Config hoặc Vault
- Không commit secrets vào git

##### Missing Input Sanitization
```java
// ❌ BAD: Không sanitize input
user.setUsername(request.getUsername());

// ✅ GOOD: Sanitize và validate
user.setUsername(sanitizeUsername(request.getUsername()));
```

##### Missing Rate Limiting
- Không có rate limiting cho login/register endpoints
- Dễ bị brute force attack

##### Missing CSRF Protection
```java
// ❌ BAD: Disable CSRF
.csrf(AbstractHttpConfigurer::disable)

// ✅ GOOD: Enable CSRF cho state-changing operations
```

#### 6.2 Missing Input Validation

```java
// ❌ BAD: Không validate
@PostMapping("/register")
ApiResponse<String> register(@RequestBody RegisterRequest request) {
    return ApiResponse.<String>builder()
        .result(authService.register(request))
        .build();
}

// ✅ GOOD: Sử dụng Bean Validation
@PostMapping("/register")
ApiResponse<String> register(@Valid @RequestBody RegisterRequest request) {
    // ...
}

// RegisterRequest.java
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", 
             message = "Password must contain at least one uppercase, one lowercase, and one number")
    private String password;
}
```

#### 6.3 Missing API Versioning
```java
// ❌ BAD: Không có versioning
@PostMapping("/register")

// ✅ GOOD: Có versioning
@PostMapping("/v1/register")
// Hoặc
@RequestMapping("/api/v1/auth")
```

#### 6.4 Missing Monitoring & Logging

##### Structured Logging
```java
// ❌ BAD: Simple logging
log.info("User created: userId={}, roles={}", user.getId(), roles);

// ✅ GOOD: Structured logging với MDC
MDC.put("userId", user.getId().toString());
MDC.put("action", "USER_CREATED");
log.info("User created successfully", 
    kv("userId", user.getId()),
    kv("roles", roles),
    kv("timestamp", Instant.now())
);
```

##### Missing Metrics
- Không có custom metrics
- Nên thêm: request count, error rate, response time

#### 6.5 Missing API Documentation
- Không có Swagger/OpenAPI documentation
- Không có API contract

#### 6.6 Missing Transaction Management
```java
// ❌ BAD: Không có @Transactional
public String register(RegisterRequest request) {
    // Multiple DB operations
}

// ✅ GOOD: Sử dụng @Transactional
@Transactional
public String register(RegisterRequest request) {
    // ...
}
```

#### 6.7 Missing Pagination Validation
```java
// ❌ BAD: Chỉ check <= 0
if (page <= 0 || size <= 0) {
    throw new AppException(ErrorCode.INVALID_REQUEST);
}

// ✅ GOOD: Validate max size
if (page <= 0 || size <= 0 || size > MAX_PAGE_SIZE) {
    throw new AppException(ErrorCode.INVALID_REQUEST);
}
```

#### 6.8 Missing Error Details
```java
// ❌ BAD: Generic error message
catch (Exception e) {
    return IntrospectResponse.builder().valid(false).build();
}

// ✅ GOOD: Include error details (trong development)
catch (Exception e) {
    log.error("Token introspection failed", e);
    return IntrospectResponse.builder()
        .valid(false)
        .error(e.getMessage())  // Only in dev
        .build();
}
```

#### 6.9 Missing Health Checks
- Có actuator nhưng cần custom health checks cho:
  - Database connectivity
  - External service dependencies

#### 6.10 Missing Configuration Validation
```java
// ❌ BAD: Không validate configuration
@Value("${jwt.signerKey}")
private String SIGNER_KEY;

// ✅ GOOD: Validate at startup
@ConfigurationProperties(prefix = "jwt")
@Validated
public class JwtProperties {
    @NotBlank
    private String signerKey;
    
    @Min(1)
    private long expiry;
}
```

---

## 7. KIẾN TRÚC VÀ THIẾT KẾ

### ❌ Vấn đề

#### 7.1 Data Model Issues
```java
// ❌ BAD: UserProfiles lưu roles dạng String
@Column(nullable = false)
String roles;  // "USER,ADMIN" - không normalized

// ✅ GOOD: Nên có relationship table hoặc JSON column
@ElementCollection
@CollectionTable(name = "user_profile_roles")
List<String> roles;
```

#### 7.2 Missing DTOs cho Internal Communication
- Service layer trả về Entity thay vì DTO
- Nên có internal DTOs

#### 7.3 Missing Caching
- Không có caching cho:
  - Role lookups
  - User lookups
  - Token validation

#### 7.4 Missing Async Processing
- Các operations như email sending, logging nên async

---

## 8. PRIORITY FIXES

### 🔴 CRITICAL (Phải fix ngay)
1. **Security: Hardcoded JWT secret** - Move to environment variables
2. **Security: Missing input validation** - Add Bean Validation
3. **Security: Missing rate limiting** - Implement rate limiting
4. **Bug: UserRepository generic type** - Should be UUID not String

### 🟡 HIGH (Nên fix sớm)
1. **Code Quality: Extract SecurityContextHelper** - Remove duplication
2. **Code Quality: Split AuthServiceImpl** - Apply SRP
3. **Testing: Increase test coverage** - Add missing test cases
4. **Documentation: Add Swagger/OpenAPI** - API documentation

### 🟢 MEDIUM (Cải thiện dần)
1. **Design: Add Factory pattern** - For object creation
2. **Design: Add Strategy pattern** - For validation
3. **Monitoring: Add structured logging** - Better observability
4. **Architecture: Normalize roles storage** - Fix data model

---

## 9. RECOMMENDATIONS

### 9.1 Immediate Actions
1. Move all secrets to environment variables
2. Add input validation với Bean Validation
3. Fix UserRepository generic type bug
4. Add rate limiting cho auth endpoints

### 9.2 Short-term (1-2 weeks)
1. Refactor AuthServiceImpl theo SRP
2. Extract SecurityContextHelper
3. Add comprehensive test coverage
4. Add Swagger documentation

### 9.3 Long-term (1 month+)
1. Implement caching strategy
2. Add monitoring và metrics
3. Refactor data model (roles storage)
4. Add async processing cho non-critical operations

---

## 10. CODE EXAMPLES - BEFORE/AFTER

### Example 1: Register Method Refactoring

#### ❌ BEFORE
```java
@Override
public String register(RegisterRequest request) {
    if (authRepository.findByUsername(request.getUsername()).isPresent()) {
        throw new AppException(ErrorCode.USER_EXISTS);
    }

    User user = AuthMapper.toUser(request);
    user.setPassword(passwordEncoder.encode(request.getPassword()));

    Set<Role> roles = new HashSet<>();
    Role defaultRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
    roles.add(defaultRole);
    if (request.getRole() != null && !request.getRole().isBlank()) {
        String roleRequest = request.getRole().trim().toUpperCase();
        if (!roleRequest.startsWith("ROLE_")) {
            roleRequest = "ROLE_" + roleRequest;
        }
        Role role = roleRepository.findByName(roleRequest)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        roles.add(role);
    }
    user.setRoles(roles);
    authRepository.save(user);

    log.info("User created: userId={}, roles={}",
            user.getId(),
            roles.stream().map(Role::getName).toList());

    return "User created successfully with userId = " + user.getId();
}
```

#### ✅ AFTER
```java
@Override
@Transactional
public String register(RegisterRequest request) {
    validateRegistrationRequest(request);
    checkUsernameAvailability(request.getUsername());
    
    User user = userFactory.createUser(request);
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    
    Set<Role> roles = roleService.assignRoles(request);
    user.setRoles(roles);
    
    User savedUser = authRepository.save(user);
    
    logUserCreated(savedUser, roles);
    
    return String.format("User created successfully with userId = %s", savedUser.getId());
}

private void validateRegistrationRequest(RegisterRequest request) {
    if (request == null) {
        throw new AppException(ErrorCode.INVALID_REQUEST);
    }
    // Additional validations
}

private void checkUsernameAvailability(String username) {
    if (authRepository.findByUsername(username).isPresent()) {
        throw new AppException(ErrorCode.USER_EXISTS);
    }
}

private void logUserCreated(User user, Set<Role> roles) {
    MDC.put("userId", user.getId().toString());
    log.info("User created successfully", 
        kv("userId", user.getId()),
        kv("username", user.getUsername()),
        kv("roles", roles.stream().map(Role::getName).toList())
    );
    MDC.clear();
}
```

### Example 2: SecurityContextHelper

#### ✅ NEW Utility Class
```java
@Component
@Slf4j
public class SecurityContextHelper {
    
    public UUID getCurrentUserId() {
        JwtAuthenticationToken jwt = getJwtAuthentication();
        return UUID.fromString(jwt.getName());
    }
    
    public String getCurrentUsername() {
        JwtAuthenticationToken jwt = getJwtAuthentication();
        return jwt.getToken().getClaimAsString("username");
    }
    
    public List<String> getCurrentRoles() {
        JwtAuthenticationToken jwt = getJwtAuthentication();
        String scope = jwt.getToken().getClaimAsString("scope");
        return scope != null ? List.of(scope.split(" ")) : List.of();
    }
    
    private JwtAuthenticationToken getJwtAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwt)) {
            log.error("Authentication is not JWT token");
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return jwt;
    }
}
```

---

## KẾT LUẬN

Codebase có cấu trúc tốt và sử dụng các best practices cơ bản, nhưng cần cải thiện nhiều điểm về:
- **Security**: Critical issues với hardcoded secrets và missing validations
- **Code Quality**: Cần refactor để tuân thủ SOLID principles
- **Testing**: Cần tăng coverage và thêm security tests
- **Production Readiness**: Thiếu monitoring, documentation, và error handling

Ưu tiên fix các critical security issues trước, sau đó cải thiện code quality và testing.
