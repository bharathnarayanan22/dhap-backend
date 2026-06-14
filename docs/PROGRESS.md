# DHAP — Project Progress

> Disaster Help & Assistance Platform (Spring Boot 4 + MongoDB + JWT)
> Last updated: 2026-06-06

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 4.0.6 (Java 25) |
| Database | MongoDB Atlas (`dhap` database) |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| Build | Gradle |

---

## ✅ What Is Done

### 1. Authentication (fully working)

Complete register / login / me / logout flow with JWT.

**Endpoints** (base URL: `http://localhost:8080`):

| Method | Endpoint | Auth required | Returns |
|--------|----------|---------------|---------|
| POST | `/auth/register` | No | `201` — id, email, role, token, refreshToken |
| POST | `/auth/login` | No | `200` — id, email, role, token, refreshToken |
| GET | `/auth/me` | Bearer token | `200` — full user profile |
| POST | `/auth/logout` | Bearer token | `200` — message (stateless; client discards token) |

**Error handling:**
- Duplicate email on register → `409 Conflict`
- Wrong email or password on login → `401 Unauthorized` (same message for both, prevents account enumeration)
- Missing/invalid/expired token on protected routes → `401`/`403`

### 2. Security layer

- `SecurityConfig` — stateless sessions, CSRF disabled, only `/auth/register` and `/auth/login` are public; everything else requires a valid JWT
- `JwtService` — token + refresh-token generation and validation (HS256). Configured via `jwt.secret`, `jwt.expiration` (24h), `jwt.refresh-expiration` (7d) in `application.properties`
- `JwtAuthenticationFilter` — reads `Authorization: Bearer <token>` header, validates **before** parsing (invalid token falls through to 401, not a 500), loads the user into the security context
- `CustomUserDetailsService` — loads users by email from MongoDB
- Passwords hashed with **BCrypt**

### 3. User entity & repository

- `User` entity (`users` collection): id (UUID), name, email (unique index), password, mobile, addressLine, city, country, pincode, role (`VOLUNTEER` / `DONOR` / `COORDINATOR`), isSubmitted
- `UserRepository` extends `MongoRepository` with `findByEmail` / `existsByEmail`

### 4. DTOs

`RegisterRequest`, `LoginRequest`, `AuthResponse` (id, email, role, token, refreshToken), `MeResponse` (full profile) — all public-field style.

### 5. Fixes applied (2026-06-06)

| Problem | Fix |
|---------|-----|
| `CustomUserDetailService.java` — class/file name mismatch, wrong package `com.dhap.api` | Recreated as `CustomUserDetailsService.java` under `com.example.dhap.security` |
| Lombok annotations used but Lombok not a dependency | Replaced with explicit constructors everywhere |
| `JwtService` in wrong package | Moved to `com.example.dhap.security` |
| `SecurityConfig` permitted `/api/v1/auth/**` but routes are `/auth/**` (login always 401) | Permits the actual auth routes |
| `UserRepository` extended `JpaRepository` in a Mongo project (compile error) | → `MongoRepository<User, UUID>` |
| `AuthResponse` / `MeResponse` private fields without getters (compile + serialization failure) | Public fields |
| `AuthService` called nonexistent `jwtService.getCurrentUser()` and wrong `generateToken(User)` | `getCurrentUser()` now reads email from `SecurityContextHolder`; token generated from `user.getEmail()` |
| Bare `RuntimeException`s surfaced as 500s | `ResponseStatusException` with proper status codes |
| JWT filter parsed token before validating (expired token → 500) | Validate first, then parse |

Verified: `./gradlew compileJava` → **BUILD SUCCESSFUL**.

### 6. Task endpoints (added 2026-06-06)

| Method | Endpoint | Who | Notes |
|--------|----------|-----|-------|
| GET | `/tasks?page=&size=[&status=][&volunteerId=]` | Both | All 4 filter combinations |
| POST | `/tasks` | Coordinator | `201`; `@Valid` (title required, volunteer ≥ 1) |
| POST | `/tasks/{id}/accept` | Volunteer | User from JWT; `409` if full / already accepted; sets `user.inTask=true`; PENDING→IN_PROGRESS |
| PATCH | `/tasks/{id}` | Both | Proof submission (`IN_VERIFICATION` + proofs) or status update; on `COMPLETED` bulk-clears `inTask` (single updateMany) |
| DELETE | `/tasks/{id}` | Coordinator | `204`; safe only while PENDING |

Supporting pieces: `Task`/`Proof`/`Location` entities (proofs + locations embedded), `TaskStatus` enum (PENDING / IN_PROGRESS / IN_VERIFICATION / COMPLETED), `TaskRepository` with array-contains queries, `MongoConfig` (`MongoTransactionManager` — accept and complete are multi-document transactions; requires replica set, Atlas has one).

### 7. User endpoints (added 2026-06-06)

| Method | Endpoint | Notes |
|--------|----------|-------|
| GET | `/users?page=&size=` | Paginated |
| GET | `/users/{id}` | |
| PATCH | `/users/{id}` | Partial profile update (non-null fields only) |
| PATCH | `/users/{id}/role` | DONOR ↔ VOLUNTEER |
| PATCH | `/users/{id}/submission-status` | Coordinator-application flag |

`UserResponse` never exposes the password; `isCoordinator` is derived from role (no backing field).

### 8. Refresh endpoint (added 2026-06-06)

`POST /auth/refresh` with `{ "refreshToken": "..." }` → `200` with a rotated token pair. Public route (it's called when the access token is expired). `401` on invalid/expired refresh token.

### 9. Fixes applied (2026-06-06, second pass — task/user code didn't compile, 100 errors)

| Problem | Fix |
|---------|-----|
| New code imported singular packages (`entity`, `repository`, `service`) | → actual plural packages (`entities`, `repositories`, `services`); `TaskStatus` → `enums` |
| `Task.java` declared `package …entity` while in `entities/` folder | Package corrected |
| `CreateTaskRequest`, `UpdateTaskRequest`, `TaskResponse`, `LocationDto` referenced but missing | Created in `dto/task/` |
| New code accessed `User` private fields directly (`user.email`, `user.role.name()`) | Getters/setters; `role` is a `String`, not an enum |
| `User.inTask` set by task flow but field didn't exist | Added `boolean inTask` to `User` |
| **`User.id` was `UUID` but `UserRepository<User, String>` + `assignedUserIds.contains(user.id)` compared UUID vs String (always false)** | `User.id` migrated to `String` (`UUID.randomUUID().toString()`); `AuthResponse`/`MeResponse` ids → String. ⚠️ Existing Atlas user docs have binary-UUID `_id`s — delete & re-register test users |
| `jakarta.validation` not on classpath | Added `spring-boot-starter-validation` to `build.gradle` |
| `AuthService.refresh` called nonexistent `jwtService.isTokenValid()` / `extractUsername()` | → existing `isValid()` / `extractEmail()`; response built via shared `buildAuthResponse()` |
| `/auth/refresh` was behind JWT auth (unusable with an expired token) | Added to `permitAll` in `SecurityConfig` |
| `UserController` duplicate import + fully-qualified field type; pasted advice comment in `UserRepository` | Cleaned up |
| `UpdateRoleRequest` imported `entities.Role` (enum lives in `enums`) | Import fixed |

Verified: `./gradlew compileJava` → **BUILD SUCCESSFUL**.

---

## 🚧 What Is NOT Done Yet

### Empty controllers / services (skeletons only, no endpoints)

- `requestController` / `requestService` — request entity exists
- `responseController` / `responseService` — response entity exists
- `resourceController` / `resourceService` — resource entity exists
- `applicationController` / `applicationService` — coordinatorApplication entity exists

### Other gaps

- [ ] `corsConfig` is empty — needed only if targeting Flutter **web** (mobile doesn't use CORS)
- [ ] Input validation only on a few DTOs (`RefreshRequest`, `UpdateRoleRequest`, `CreateTaskRequest`) — no email format / password strength on register
- [ ] No role-based authorization (`@PreAuthorize` / route rules per VOLUNTEER / DONOR / COORDINATOR) — e.g. any authenticated user can currently create/delete tasks or change another user's profile
- [ ] No tests beyond the default context-load test
- [ ] `TaskService.delete` doesn't clear `inTask` on assigned users — only safe to delete PENDING tasks
- [ ] ⚠️ **User id migration:** `User.id` changed UUID → String — old Atlas user documents have binary-UUID `_id`s and won't load; delete & re-register test users
- [ ] ⚠️ **Security:** MongoDB Atlas credentials are hardcoded in `application.properties` — move to env var (`${MONGODB_URI}`) and rotate the password before sharing/pushing the repo. Same for `jwt.secret`.

---

## How to Run & Test

```bash
./gradlew bootRun        # starts on http://localhost:8080
```

```bash
# Register
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","email":"test@example.com","password":"secret123","mobile":"9999999999","role":"VOLUNTEER"}'

# Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"secret123"}'

# Me (use token from login response)
curl http://localhost:8080/auth/me -H "Authorization: Bearer <token>"
```

## Flutter Integration

| Flutter runs on | Base URL |
|---|---|
| Android emulator | `http://10.0.2.2:8080` |
| iOS simulator | `http://localhost:8080` |
| Physical device (same Wi-Fi) | `http://<Mac-LAN-IP>:8080` |
| Flutter web | `http://localhost:8080` + CORS config required |

Android needs `android:usesCleartextTraffic="true"` in the manifest for plain HTTP during development. Store the JWT with `flutter_secure_storage` and send it as `Authorization: Bearer <token>` on protected calls.