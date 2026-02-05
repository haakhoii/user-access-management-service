# Acceptance Checklist (Pass/Fail) — User Access Management Service

Nguồn đối chiếu: `docs/reviews/code_review_r1_20262201.md`  
Mục tiêu: bảng nghiệm thu **ready-to-copy** theo mức độ **CRITICAL / HIGH / MEDIUM**, kèm **file path** làm bằng chứng.

> Quy ước trạng thái:
>
> - **PASS**: đã có trong source hiện tại
> - **FAIL**: chưa có / vẫn vi phạm
> - **PARTIAL**: có nhưng chưa đầy đủ / còn lệch tiêu chí review

---

## CRITICAL (Phải fix ngay)


| Hạng mục (theo review)                                         | Pass/Fail                                           | Bằng chứng / file liên quan                                                                                                                                                                                                                                                                                                                                                                                                               |
| -------------------------------------------------------------- | --------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Security: Hardcoded JWT secret – move to env**               | **PARTIAL**                                         | **PASS (runtime config)**: `auth-service/src/main/resources/application.yaml` (jwt.signerKey: `${JWT_SIGNER_KEY}`), `user-service/src/main/resources/application.yaml` (jwt.signerKey: `${JWT_SIGNER_KEY}`), `.env.example`, `docker-compose.yaml`. **FAIL (test config)**: `auth-service/src/main/resources/application-test.yaml` (hardcoded signerKey), `user-service/src/main/resources/application-test.yaml` (hardcoded signerKey). |
| **Security: Missing input validation – add Bean Validation**   | **PASS**                                            | Controller dùng `@Valid`: `auth-service/src/main/java/com/r2s/auth/controller/UserController.java`, `user-service/src/main/java/com/r2s/user/controller/UserProfilesController.java`. DTO request có constraints: `core-service/src/main/java/com/r2s/core/dto/request/{RegisterRequest,LoginRequest,IntrospectRequest,UserCreatedRequest,UserUpdatedRequest}.java`.                                                                      |
| **Security: Missing rate limiting – implement rate limiting**  | **PASS** *(nên nghiệm thu thêm bằng test/cấu hình)* | Có filter & service: `auth-service/src/main/java/com/r2s/auth/config/RateLimitBlockFilter.java`, `auth-service/src/main/java/com/r2s/auth/domain/rateLimit/*`, `auth-service/src/main/java/com/r2s/auth/config/RedisConfig.java`. Được gắn vào chain: `auth-service/src/main/java/com/r2s/auth/config/SecurityConfig.java` (addFilterBefore).                                                                                             |
| **Bug: UserRepository generic type should be UUID not String** | **PASS**                                            | Không còn repo `extends JpaRepository<..., String>` trong source (chỉ còn ví dụ trong file review). Repo hiện tại: `user-service/src/main/java/com/r2s/user/repository/UserProfileRepository.java`, `auth-service/src/main/java/com/r2s/auth/repository/UserRepository.java` *(khi nghiệm thu nên confirm `UUID` ở generic type).*                                                                                                        |


---

## HIGH (Nên fix sớm)


| Hạng mục (theo review)                              | Pass/Fail                                                           | Bằng chứng / file liên quan                                                                                                                                                                                                                                                                                                                |
| --------------------------------------------------- | ------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **Code Quality: Extract SecurityContextHelper**     | **PASS**                                                            | `auth-service/src/main/java/com/r2s/auth/domain/helper/SecurityContextHelper.java`, `user-service/src/main/java/com/r2s/user/domain/helper/SecurityContextHelper.java`. Usage: `auth-service/src/main/java/com/r2s/auth/service/impl/{AuthenticationServiceImpl,UserServiceImpl}.java`.                                                    |
| **Code Quality: Split AuthServiceImpl theo SRP**    | **PARTIAL**                                                         | Đã tách thành `AuthenticationServiceImpl` và `UserServiceImpl`: `auth-service/src/main/java/com/r2s/auth/service/impl/AuthenticationServiceImpl.java`, `auth-service/src/main/java/com/r2s/auth/service/impl/UserServiceImpl.java`. Tuy nhiên vẫn còn các mục production khác chưa đạt (transaction boundary, versioning, observability…). |
| **Testing: Increase test coverage (missing cases)** | **FAIL** *(chưa thấy bằng chứng đã cover đủ theo checklist review)* | Tests hiện có: `auth-service/src/test/java/com/r2s/auth/test/{unit,integration,e2e}/*`, `user-service/src/test/java/com/r2s/user/{unit,integration}/*`. Review yêu cầu thêm negative/edge/security cases (invalid token, unauthenticated `/me`, unauthorized access, boundary values, validation failure, rate limit triggered…).          |
| **Documentation: Add Swagger/OpenAPI**              | **FAIL**                                                            | Không tìm thấy `springdoc`/OpenAPI/Swagger config hoặc annotations trong codebase (ngoài nội dung trong file review).                                                                                                                                                                                                                      |


---

## MEDIUM (Cải thiện dần)


| Hạng mục (theo review)                                         | Pass/Fail                                    | Bằng chứng / file liên quan                                                                                                                                                                                                                                         |
| -------------------------------------------------------------- | -------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Design: Factory pattern for object creation**                | **PASS**                                     | `auth-service/src/main/java/com/r2s/auth/domain/factory/UserFactory.java`, `user-service/src/main/java/com/r2s/user/domain/factory/UserProfileFactory.java`.                                                                                                        |
| **Design: Strategy/Normalizer cho role/validation**            | **PASS**                                     | Role normalizer/resolver: `auth-service/src/main/java/com/r2s/auth/domain/role/normalizer/*`, `auth-service/src/main/java/com/r2s/auth/domain/role/RoleNormalizerResolver.java`. Validation classes: `auth-service/src/main/java/com/r2s/auth/domain/validation/*`. |
| **Monitoring: structured logging (MDC), better observability** | **FAIL** *(chưa thấy implement theo review)* | Hiện log chủ yếu dạng string (ví dụ ở `auth-service/src/main/java/com/r2s/auth/controller/UserController.java` và các service impl). Chưa thấy pattern MDC + structured logging như review đề xuất.                                                                 |
| **Architecture: Normalize roles storage**                      | **PASS**                                     | `user-service/src/main/java/com/r2s/user/entity/UserProfiles.java` dùng `@ElementCollection` + table `user_profile_roles`.                                                                                                                                          |


---

## Production criteria bổ sung (từ review nhưng không nằm “Priority Fixes”)


| Hạng mục                                                                 | Pass/Fail                              | File liên quan                                                                                                                                                                                                                                                                                                                               |
| ------------------------------------------------------------------------ | -------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **CSRF** (review cảnh báo disable)                                       | **FAIL / cần quyết định threat model** | `.csrf(AbstractHttpConfigurer::disable)` trong `auth-service/src/main/java/com/r2s/auth/config/SecurityConfig.java` và `user-service/src/main/java/com/r2s/user/config/SecurityConfig.java`.                                                                                                                                                 |
| **API Versioning**                                                       | **FAIL**                               | Endpoint hiện là `/register`, `/login`, `/me`…: `auth-service/src/main/java/com/r2s/auth/controller/UserController.java` *(chưa có `/api/v1/...`).*                                                                                                                                                                                          |
| **Configuration validation (`@ConfigurationProperties` + `@Validated`)** | **FAIL**                               | JWT key đang inject bằng `@Value("${jwt.signerKey}")`: `auth-service/src/main/java/com/r2s/auth/token/impl/JwtTokenImpl.java`, `auth-service/src/main/java/com/r2s/auth/config/HmacJwtDecoder.java`, `user-service/src/main/java/com/r2s/user/config/HmacJwtDecoder.java`. Không thấy class `JwtProperties` thật (chỉ có trong file review). |
| **Transaction management cho business methods**                          | **FAIL**                               | Không thấy `@Transactional` trong `auth-service/src/main/java` (ví dụ `UserServiceImpl.register()`): `auth-service/src/main/java/com/r2s/auth/service/impl/UserServiceImpl.java`. *(Hiện chỉ thấy `@Transactional` trong integration tests.)*                                                                                                |
| **Hardcoded secrets trong test profile**                                 | **FAIL**                               | `auth-service/src/main/resources/application-test.yaml`, `user-service/src/main/resources/application-test.yaml`.                                                                                                                                                                                                                            |


