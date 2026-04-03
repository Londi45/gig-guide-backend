# Implementation Plan: Gig Guide App

## Overview

Implement the full Gig Guide REST API on top of the existing Spring Boot skeleton. Tasks are ordered by dependency: models → repositories → DTOs/mappers → services → controllers → security → tests. Spring Security is currently commented out and must be re-enabled with JWT filter-based auth.

## Tasks

- [x] 1. Extend and complete domain models
  - [x] 1.1 Update `Event` model with all required fields
    - Add `status` (EventStatus enum: DRAFT, PUBLISHED, CANCELLED, COMPLETED), `capacity` (int), `maleRatio` (int, default 50), `femaleRatio` (int, default 50), `liveMaleCount` (int, default 0), `liveFemaleCount` (int, default 0)
    - Add `@OneToMany` collections for `entryTypes` (List<EntryType>) and `discounts` (List<Discount>)
    - Remove legacy `entryFee` String field
    - _Requirements: 4.1, 5.1, 5.4, 12.1, 14.1, 15.1, 15.2, 16.7_

  - [x] 1.2 Create `EventStatus` enum
    - Values: DRAFT, PUBLISHED, CANCELLED, COMPLETED
    - Place in `Enums` package
    - _Requirements: 15.1_

  - [x] 1.3 Create `EntryType` model
    - Fields: `id` (Long), `name` (String), `price` (BigDecimal), `description` (String), `availableQuantity` (int)
    - `@ManyToOne` to `Event`
    - _Requirements: 16.1, 14.9_

  - [x] 1.4 Create `Discount` model
    - Fields: `id` (Long), `discountType` (String), `discountValue` (BigDecimal), `description` (String), `validFrom` (LocalDateTime), `validUntil` (LocalDateTime)
    - `@ManyToOne` to `Event`
    - _Requirements: 11.2, 14.6_

  - [x] 1.5 Create `StaffAssignment` model
    - Fields: `id` (Long), `event` (ManyToOne Event), `user` (ManyToOne User)
    - _Requirements: 6.1, 6.4_

  - [x] 1.6 Create `CheckInAuditEntry` model
    - Fields: `id` (Long), `event` (ManyToOne Event), `gender` (String), `action` (String — CHECK_IN/CHECK_OUT), `performedBy` (Long), `timestamp` (LocalDateTime)
    - _Requirements: 12.2, 12.3, 14.10_

  - [x] 1.7 Create `RefreshToken` model
    - Fields: `id` (Long), `token` (String, unique), `user` (ManyToOne User), `expiresAt` (LocalDateTime), `revoked` (boolean)
    - _Requirements: 2.8, 2.9, 2.10, 2.11_

  - [x] 1.8 Update `User` model
    - Add `verificationToken` (String), `verificationTokenExpiry` (LocalDateTime), `isVerified` (boolean), `passwordResetToken` (String), `passwordResetTokenExpiry` (LocalDateTime)
    - _Requirements: 1.5, 1.6, 19.1_

  - [x] 1.9 Update `Clubs` model
    - Add `@OneToOne` link to `User` (the club owner) — `owner` field
    - _Requirements: 1.1, 3.2_

- [x] 2. Create repositories
  - [x] 2.1 Create `EntryTypeRepository` extending `JpaRepository<EntryType, Long>`
    - Add `findByEventId(Long eventId)`
    - _Requirements: 16.1, 16.5_

  - [x] 2.2 Create `DiscountRepository` extending `JpaRepository<Discount, Long>`
    - Add `findByEventId(Long eventId)` and `findByEventIdAndValidFromBeforeAndValidUntilAfter(Long eventId, LocalDateTime now1, LocalDateTime now2)`
    - _Requirements: 11.1, 11.8_

  - [x] 2.3 Create `StaffAssignmentRepository` extending `JpaRepository<StaffAssignment, Long>`
    - Add `findByEventId(Long eventId)`, `deleteByEventIdAndUserId(Long eventId, Long userId)`
    - _Requirements: 6.1, 6.3, 6.4_

  - [x] 2.4 Create `CheckInAuditRepository` extending `JpaRepository<CheckInAuditEntry, Long>`
    - Add `findByEventIdOrderByTimestampDesc(Long eventId, Pageable pageable)`
    - _Requirements: 12.7_

  - [x] 2.5 Create `RefreshTokenRepository` extending `JpaRepository<RefreshToken, Long>`
    - Add `findByToken(String token)`, `deleteByUser(User user)`
    - _Requirements: 2.8, 2.11_

  - [x] 2.6 Update `EventRepository`
    - Add `findByClubIdAndStatus(Long clubId, EventStatus status, Pageable pageable)`
    - Add `findByStatusAndStartDateTimeAfter(EventStatus status, LocalDateTime now, Pageable pageable)`
    - Add `findByClubIdAndStartDateTimeBetween(Long clubId, LocalDateTime start, LocalDateTime end, Pageable pageable)`
    - _Requirements: 7.1, 7.3, 7.4, 18.1, 18.2, 18.3_

  - [x] 2.7 Update `UserRepository`
    - Add `findByEmail(String email)`, `findByVerificationToken(String token)`, `findByPasswordResetToken(String token)`, `findByClubId(Long clubId)`
    - _Requirements: 1.2, 9.6, 19.1, 19.3_

- [x] 3. Update and create DTOs
  - [x] 3.1 Update `EventDTO` with all required fields
    - Add `status`, `clubId`, `clubName`, `capacity`, `maleRatio`, `femaleRatio`, `liveMaleCount`, `liveFemaleCount`, `liveTotalCount`, `liveMalePercentage`, `liveFemalePercentage`, `entryTypes` (List<EntryTypeDTO>), `discounts` (List<DiscountDTO>)
    - Remove legacy `entryFee`
    - _Requirements: 14.1_

  - [x] 3.2 Create `EntryTypeDTO`
    - Fields: `id`, `name`, `price` (BigDecimal), `description`, `availableQuantity`
    - _Requirements: 14.9_

  - [x] 3.3 Create `DiscountDTO`
    - Fields: `id`, `discountType`, `discountValue` (BigDecimal), `description`, `validFrom`, `validUntil`
    - _Requirements: 14.6_

  - [x] 3.4 Update `ClubDTO` with all required fields
    - Ensure all fields from Req 14.2 are present: `id`, `name`, `description`, `email`, `phone`, `website`, `logoUrl`, `coverImageUrl`, `openingHours`, `closingHours`, `dressCode`, `hasParking`, `hasVIPArea`, `capacity`, `isActive`, `address` (AddressDTO), `socials` (SocialsDTO)
    - _Requirements: 14.2_

  - [x] 3.5 Update `UserResponseDTO`
    - Ensure fields: `id`, `username`, `email`, `fullName`, `phoneNumber`, `role`, `clubId`, `isActive`
    - _Requirements: 14.5_

  - [x] 3.6 Create `AuthResponseDTO`
    - Fields: `accessToken`, `refreshToken`, `role`
    - _Requirements: 14.11_

  - [x] 3.7 Update `RegisterRequestDTO`
    - Ensure fields: `username`, `password`, `email`, `fullName`, `phoneNumber`, `role` (validated — CLUB_OWNER or STAFF only), `clubId` (required when role is STAFF)
    - Add bean validation annotations (`@NotBlank`, `@Email`, etc.)
    - _Requirements: 14.8, 1.3_

  - [x] 3.8 Create `CheckInAuditDTO`
    - Fields: `id`, `eventId`, `gender`, `action`, `performedBy`, `timestamp`
    - _Requirements: 14.10_

  - [x] 3.9 Create `CheckInRequestDTO`
    - Fields: `eventId` (Long), `gender` (String — MALE or FEMALE)
    - _Requirements: 12.2_

  - [x] 3.10 Create `StatusTransitionRequestDTO`
    - Field: `status` (EventStatus)
    - _Requirements: 15.3_

  - [x] 3.11 Create `PasswordResetRequestDTO` and `ForgotPasswordRequestDTO`
    - `ForgotPasswordRequestDTO`: `email` (String)
    - `PasswordResetRequestDTO`: `token` (String), `newPassword` (String)
    - _Requirements: 19.1, 19.3_

- [x] 4. Update mappers
  - [x] 4.1 Update `ClubMapper` to map all ClubDTO fields including nested AddressDTO and SocialsDTO
    - _Requirements: 14.2, 14.3, 14.4_

  - [x] 4.2 Create `EventMapper`
    - Map `Event` ↔ `EventDTO` including computed fields (`liveTotalCount`, `liveMalePercentage`, `liveFemalePercentage`, `clubName`)
    - Map nested `entryTypes` and `discounts` lists
    - _Requirements: 14.1, 12.4, 12.5_

  - [x] 4.3 Create `UserMapper`
    - Map `User` ↔ `UserResponseDTO`
    - _Requirements: 14.5_

- [x] 5. Implement JWT and token infrastructure
  - [x] 5.1 Update `JwtTokenUtil` to support role claims and 1-hour access token expiry
    - Add `generateToken(String email, String role)` that embeds role as a claim
    - Set `EXPIRATION_TIME` to 3600000 (1 hour)
    - Add `extractRole(String token)` method
    - _Requirements: 2.1, 2.4_

  - [x] 5.2 Create `JwtAuthFilter` (extends `OncePerRequestFilter`)
    - Extract Bearer token from `Authorization` header
    - Validate token; set `UsernamePasswordAuthenticationToken` in `SecurityContextHolder`
    - Return HTTP 401 with "Token expired" on `ExpiredJwtException`
    - Return HTTP 401 on missing/invalid token for protected endpoints
    - _Requirements: 2.5, 2.6_

  - [x] 5.3 Create `RefreshTokenService`
    - `createRefreshToken(User user)` — generate UUID token, persist with 7-day expiry
    - `validateRefreshToken(String token)` — return user or throw with appropriate message
    - `revokeRefreshToken(String token)` — mark as revoked
    - `deleteByUser(User user)` — for logout
    - _Requirements: 2.8, 2.9, 2.10, 2.11_

- [x] 6. Implement Security configuration
  - [x] 6.1 Re-enable and rewrite `SecurityConfig`
    - Add Spring Security dependency to pom.xml (`spring-boot-starter-security`)
    - Configure stateless session (`SessionCreationPolicy.STATELESS`)
    - Register `JwtAuthFilter` before `UsernamePasswordAuthenticationFilter`
    - Permit public endpoints: `GET /api/events/**`, `GET /api/clubs/**`, `POST /api/auth/**`
    - Require authentication for all other endpoints
    - Configure `BCryptPasswordEncoder` bean and `AuthenticationManager` bean
    - _Requirements: 2.5, 2.7, 3.6_

  - [x] 6.2 Create `GlobalExceptionHandler` (`@RestControllerAdvice`)
    - Handle `MethodArgumentNotValidException` → HTTP 400 with `errors` array
    - Handle `AccessDeniedException` → HTTP 403
    - Handle `EntityNotFoundException` / custom `ResourceNotFoundException` → HTTP 404
    - Handle `ResponseStatusException` → pass through status and message
    - Handle all other `Exception` → HTTP 500 with "An unexpected error occurred" (log stack trace)
    - Return JSON with `timestamp`, `status`, `error`, `message` fields
    - _Requirements: 10.1, 10.2, 10.3_

  - [x] 6.3 Create `ResourceNotFoundException` (extends `RuntimeException`)
    - Used for 404 cases throughout services
    - _Requirements: 7.5, 8.3, 8.4_

- [x] 7. Implement AuthService and registration
  - [x] 7.1 Create `AuthService` interface and `AuthServiceImpl`
    - `register(RegisterRequestDTO dto)` — validate unique email, create `Clubs` + `User` (CLUB_OWNER), hash password with BCrypt, send verification email, return `UserResponseDTO`
    - Reject ADMIN role in registration with HTTP 400 "Invalid role specified"
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 3.1, 14.8_

  - [x] 7.2 Implement login in `AuthServiceImpl`
    - `login(LoginRequestDTO dto)` — look up user by email, verify BCrypt password, check `isVerified` (403 "Account not verified"), check `isActive` (403 "Account is deactivated"), generate access token (1h) + refresh token (7d), return `AuthResponseDTO`
    - _Requirements: 2.1, 2.2, 2.3, 1.6_

  - [x] 7.3 Implement token refresh in `AuthServiceImpl`
    - `refresh(String refreshToken)` — validate token via `RefreshTokenService`, issue new access token, return `AuthResponseDTO`
    - _Requirements: 2.8, 2.9, 2.10_

  - [x] 7.4 Implement logout in `AuthServiceImpl`
    - `logout(String refreshToken)` — revoke refresh token via `RefreshTokenService`
    - _Requirements: 2.11_

  - [x] 7.5 Implement email verification in `AuthServiceImpl`
    - `verifyEmail(String token)` — find user by token, check expiry, set `isVerified = true`, clear token
    - _Requirements: 1.5, 1.6_

  - [x] 7.6 Implement password reset in `AuthServiceImpl`
    - `forgotPassword(String email)` — generate reset token (1h), store on user, send email; always return generic 200 message
    - `resetPassword(String token, String newPassword)` — validate token and expiry, hash and save new password, clear token
    - _Requirements: 19.1, 19.2, 19.3, 19.4, 19.5, 19.6_

- [x] 8. Implement UserService
  - [x] 8.1 Create `UserService` interface and `UserServiceImpl`
    - `createStaff(RegisterRequestDTO dto, Long clubOwnerId)` — create User with STAFF role linked to owner's club, return `UserResponseDTO`
    - `deactivateStaff(Long userId, Long requesterId)` — verify requester is CLUB_OWNER of same club, set `isActive = false`
    - `updateProfile(Long userId, UpdateProfileDTO dto)` — update `fullName` and `phoneNumber` only (no role change)
    - `getStaffByClub(Long clubId)` — return list of `UserResponseDTO` for all STAFF in club
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 13.7_

- [x] 9. Implement ClubService updates
  - [x] 9.1 Update `ClubService` interface and `ClubServiceImpl` for security-aware operations
    - `deactivateClub(Long clubId)` — set `isActive = false` on club and all linked users (Admin only)
    - `deleteClub(Long clubId)` — delete club, cascade events and staff assignments (Admin only)
    - Update `getAllClubs()` to return paginated `Page<ClubDTO>` of active clubs sorted by name ASC
    - Update `getClubById()` to throw `ResourceNotFoundException` for inactive or missing clubs
    - _Requirements: 3.2, 3.3, 3.5, 8.1, 8.2, 8.3, 8.4, 20.5_

- [x] 10. Implement EventService
  - [x] 10.1 Create `EventService` interface and `EventServiceImpl` — core CRUD
    - `createEvent(EventDTO dto, Long userId)` — validate club ownership, validate endDateTime > startDateTime, validate capacity ≥ 1, default ratios to 50/50, set status DRAFT, persist and return `EventDTO`
    - `updateEvent(Long eventId, EventDTO dto, Long userId)` — verify club ownership (403 if not), apply changes, return `EventDTO`
    - `deleteEvent(Long eventId, Long userId)` — verify CLUB_OWNER role (403 for STAFF), verify club ownership, delete and return 204
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 5.2, 5.3, 13.1, 13.2, 13.3_

  - [x] 10.2 Implement public event browsing in `EventServiceImpl`
    - `getPublishedEvents(Pageable pageable)` — return paginated PUBLISHED future events sorted by startDateTime ASC
    - `getEventById(Long id)` — return EventDTO with active discounts; 404 if not PUBLISHED or not found
    - `getEventsByClub(Long clubId, Pageable pageable)` — return PUBLISHED events for club
    - `getEventsByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable)` — return PUBLISHED events in range
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 20.4_

  - [x] 10.3 Implement club dashboard in `EventServiceImpl`
    - `getDashboardEvents(Long userId, EventStatus status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable)` — return all events for user's club with optional filters
    - _Requirements: 18.1, 18.2, 18.3, 18.4, 18.5, 18.6_

  - [x] 10.4 Implement event status transitions in `EventServiceImpl`
    - `transitionStatus(Long eventId, EventStatus newStatus, Long userId)` — verify ownership, enforce STAFF restrictions (403 for CANCELLED/COMPLETED), apply transition
    - _Requirements: 15.3, 15.4, 15.5, 15.7, 15.8, 15.9, 13.8, 13.9_

  - [x] 10.5 Implement entry type management in `EventServiceImpl`
    - `createEntryType(Long eventId, EntryTypeDTO dto, Long userId)` — verify club ownership, validate price ≥ 0 and quantity ≥ 0, persist and return `EntryTypeDTO`
    - `updateEntryType(Long entryTypeId, EntryTypeDTO dto, Long userId)` — verify ownership, apply changes
    - `deleteEntryType(Long entryTypeId, Long userId)` — verify ownership, delete and return 204
    - _Requirements: 16.1, 16.2, 16.3, 16.4, 16.5, 16.6_

  - [x] 10.6 Implement discount management in `EventServiceImpl`
    - `createDiscount(Long eventId, DiscountDTO dto, Long userId)` — verify club ownership, validate validUntil > validFrom, validate discountValue ≥ 0, persist and return `DiscountDTO`
    - `updateDiscount(Long discountId, DiscountDTO dto, Long userId)` — verify ownership, apply changes
    - `deleteDiscount(Long discountId, Long userId)` — verify ownership, delete and return 204
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.7_

  - [x] 10.7 Implement live attendance tracking in `EventServiceImpl`
    - `checkIn(Long eventId, String gender, Long userId)` — verify club membership, increment count, persist `CheckInAuditEntry`, return updated counts
    - `checkOut(Long eventId, String gender, Long userId)` — verify club membership, decrement count (400 if already 0), persist audit entry
    - `getAuditLog(Long eventId, Long userId)` — verify CLUB_OWNER or ADMIN role (403 for STAFF), return paginated `CheckInAuditDTO` ordered by timestamp DESC
    - _Requirements: 12.1, 12.2, 12.3, 12.6, 12.7, 12.8_

  - [x] 10.8 Implement staff assignment management in `EventServiceImpl`
    - `assignStaff(Long eventId, Long staffUserId, Long requesterId)` — verify requester is CLUB_OWNER, verify staff belongs to same club, create `StaffAssignment`
    - `removeStaffAssignment(Long eventId, Long staffUserId, Long requesterId)` — verify ownership, delete assignment
    - `getEventStaff(Long eventId)` — return list of staff with name and role
    - _Requirements: 6.1, 6.2, 6.3, 6.4_

  - [x] 10.9 Implement image handling in `EventServiceImpl`
    - `uploadImage(Long eventId, MultipartFile file, Long userId)` — verify club ownership, store file (local filesystem or configurable path), set `imageUrl`, return updated `EventDTO`
    - `setImageUrl(Long eventId, String url, Long userId)` — verify club ownership, update `imageUrl`
    - Enforce max file size; return 400 "Image file size exceeds the maximum allowed limit" if exceeded
    - _Requirements: 17.1, 17.2, 17.3, 17.4, 17.5_

- [x] 11. Implement controllers
  - [x] 11.1 Create `AuthController` (`/api/auth`)
    - `POST /register` — calls `AuthService.register()`
    - `POST /login` — calls `AuthService.login()`
    - `POST /refresh` — calls `AuthService.refresh()`
    - `POST /logout` — calls `AuthService.logout()`
    - `GET /verify?token=` — calls `AuthService.verifyEmail()`
    - `POST /forgot-password` — calls `AuthService.forgotPassword()`
    - `POST /reset-password` — calls `AuthService.resetPassword()`
    - _Requirements: 1.1, 2.1, 2.8, 2.11, 1.5, 19.1_

  - [x] 11.2 Update `ClubController` (`/api/clubs`)
    - `GET /` — public, paginated list of active clubs
    - `GET /{id}` — public, single club
    - `PUT /{id}` — ADMIN or CLUB_OWNER (own club)
    - `DELETE /{id}` — ADMIN only
    - `PATCH /{id}/deactivate` — ADMIN only
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 3.2, 3.3, 3.5, 13.6_

  - [x] 11.3 Create `EventController` (`/api/events`)
    - `GET /` — public, paginated published events
    - `GET /{id}` — public, single event
    - `GET /club/{clubId}` — public, events by club
    - `POST /` — CLUB_OWNER or STAFF
    - `PUT /{id}` — CLUB_OWNER or STAFF
    - `DELETE /{id}` — CLUB_OWNER only
    - `PATCH /{id}/status` — CLUB_OWNER, STAFF (DRAFT→PUBLISHED only), or ADMIN
    - `GET /dashboard` — CLUB_OWNER or STAFF (authenticated)
    - _Requirements: 4.1, 4.5, 7.1, 7.2, 7.3, 15.3, 18.1_

  - [x] 11.4 Create `EntryTypeController` (`/api/events/{eventId}/entry-types`)
    - `POST /` — CLUB_OWNER or STAFF
    - `PUT /{entryTypeId}` — CLUB_OWNER or STAFF
    - `DELETE /{entryTypeId}` — CLUB_OWNER or STAFF
    - _Requirements: 16.1, 16.4, 16.5_

  - [x] 11.5 Create `DiscountController` (`/api/events/{eventId}/discounts`)
    - `POST /` — CLUB_OWNER or STAFF
    - `PUT /{discountId}` — CLUB_OWNER or STAFF
    - `DELETE /{discountId}` — CLUB_OWNER or STAFF
    - _Requirements: 11.1, 11.5, 11.6_

  - [x] 11.6 Create `AttendanceController` (`/api/events/{eventId}/attendance`)
    - `POST /check-in` — CLUB_OWNER or STAFF
    - `POST /check-out` — CLUB_OWNER or STAFF
    - `GET /audit` — CLUB_OWNER or ADMIN only
    - _Requirements: 12.2, 12.3, 12.7, 12.8_

  - [x] 11.7 Create `StaffAssignmentController` (`/api/events/{eventId}/staff`)
    - `POST /` — CLUB_OWNER only
    - `DELETE /{userId}` — CLUB_OWNER only
    - `GET /` — CLUB_OWNER or STAFF
    - _Requirements: 6.1, 6.3, 6.4_

  - [x] 11.8 Update `UserControllers` — create `UserController` (`/api/users`)
    - `POST /staff` — CLUB_OWNER only
    - `PATCH /staff/{userId}/deactivate` — CLUB_OWNER only
    - `PUT /profile` — CLUB_OWNER or STAFF (own profile)
    - `GET /staff` — CLUB_OWNER or STAFF
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.6_

  - [x] 11.9 Create `ImageController` (`/api/events/{eventId}/image`)
    - `POST /upload` — multipart/form-data, CLUB_OWNER or STAFF
    - `PUT /url` — CLUB_OWNER or STAFF
    - _Requirements: 17.1, 17.2_

- [x] 12. Checkpoint — wire everything together
  - Ensure all beans are correctly wired (no missing `@Autowired` / constructor injection)
  - Confirm `application.properties` has JWT secret key, Redis config, mail config, and file upload max size
  - Ensure `spring-boot-starter-security` is uncommented/added in `pom.xml`
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 13. Write tests
  - [ ] 13.1* Write unit tests for `AuthServiceImpl`
    - Test registration with duplicate email returns 409
    - Test login with unverified account returns 403
    - Test login with deactivated account returns 403
    - Test token refresh with expired token returns 401
    - _Requirements: 1.2, 1.6, 2.2, 2.3, 2.9_

  - [ ] 13.2* Write unit tests for `EventServiceImpl`
    - Test create event with endDateTime before startDateTime returns 400
    - Test create event with capacity < 1 returns 400
    - Test ratio validation: maleRatio + femaleRatio ≠ 100 returns 400
    - Test STAFF delete event returns 403
    - Test check-out when count is 0 returns 400
    - _Requirements: 4.2, 4.6, 4.8, 5.2, 12.3_

  - [ ] 13.3* Write unit tests for `DiscountServiceImpl`
    - Test discount with validUntil before validFrom returns 400
    - Test discount with negative value returns 400
    - _Requirements: 11.3, 11.4_

  - [ ] 13.4* Write unit tests for `ClubServiceImpl`
    - Test getClubById for inactive club returns 404
    - Test deactivateClub deactivates all linked users
    - _Requirements: 8.4, 3.2_

  - [ ] 13.5* Write integration tests for public endpoints
    - Test `GET /api/events` returns only PUBLISHED future events
    - Test `GET /api/clubs` returns only active clubs
    - Test `GET /api/events/{id}` for DRAFT event returns 404
    - _Requirements: 7.1, 7.2, 8.1, 15.6_

- [x] 14. Final checkpoint
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Spring Security dependency is currently commented out in `pom.xml` — task 6.1 re-enables it
- Redis is already in `pom.xml` and can be used for refresh token storage or caching
- All paginated responses must include `content`, `totalElements`, `totalPages`, `page`, `size` (Req 20.6)
- Property tests are not included as the design document did not define formal correctness properties
