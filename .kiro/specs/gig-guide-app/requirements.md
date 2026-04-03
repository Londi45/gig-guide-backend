# Requirements Document

## Introduction

The Gig Guide Application is a Spring Boot REST API that enables clubs to manage their events and allows the general public to browse those events without an account. Three roles exist: Admin (full platform access), Club_Owner (manages own club, events, and staff), and Staff (assists in event management for their assigned club). Authentication is JWT-based with short-lived access tokens and longer-lived refresh tokens. The system builds on an existing codebase that already has Club, Event, and User models, a PostgreSQL database, JWT utilities, and email verification infrastructure.

---

## Glossary

- **System**: The Gig Guide Application backend (Spring Boot REST API)
- **Admin**: A pre-created privileged user with full access to all clubs and events
- **Club**: A registered venue/organisation that owns and manages its own events and staff
- **Club_Owner**: The user account associated with a Club, holding the CLUB_OWNER role
- **Staff**: A user linked to a Club with the STAFF role, able to create, update, and manage events for their assigned club
- **Attendee**: An unauthenticated member of the public who browses events
- **Event**: A scheduled occurrence at a Club, with a name, date/time range, capacity, optional attributes, entry types, and live attendance tracking
- **EventStatus**: The lifecycle state of an Event — one of DRAFT, PUBLISHED, CANCELLED, or COMPLETED
- **EntryType**: A structured pricing tier for an Event (e.g. EARLY_BIRD, GENERAL, VIP) with a name, price, description, and available quantity
- **Discount**: A promotional reduction applied to an event, with a type, value, description, and validity period
- **Live_Attendance**: The real-time count of male and female attendees currently checked in to an Event
- **CheckInAuditEntry**: A record of a single check-in or check-out action, capturing eventId, gender, action type, performedBy, and timestamp
- **JWT**: JSON Web Token used for stateless authentication
- **Access_Token**: A short-lived JWT (1 hour) used to authenticate API requests
- **Refresh_Token**: A longer-lived opaque or signed token (7 days) used to obtain a new Access_Token
- **Auth_Service**: The component responsible for authentication, token issuance, token refresh, and password reset
- **Club_Service**: The component responsible for club creation, updates, and deletion
- **Event_Service**: The component responsible for event creation, updates, deletion, retrieval, discount management, entry type management, image handling, and live attendance tracking
- **User_Service**: The component responsible for user registration, role assignment, and account management
- **Security_Filter**: The JWT filter that validates tokens on protected endpoints
- **Capacity**: The maximum number of attendees permitted at an event
- **Male_Ratio / Female_Ratio**: Integer percentages (summing to 100) representing the target gender split for an event
- **Live_Male_Count / Live_Female_Count**: The actual current number of male and female attendees checked in to a live event
- **Staff_Assignment**: The association between a Staff user and an Event
- **EventDTO**: The data transfer object returned for Event responses — see Requirement 14
- **ClubDTO**: The data transfer object returned for Club responses — see Requirement 14
- **UserResponseDTO**: The data transfer object returned for User responses — see Requirement 14
- **EntryTypeDTO**: The data transfer object representing a single entry/pricing tier — see Requirement 14
- **CheckInAuditDTO**: The data transfer object representing a single audit log entry — see Requirement 14

---

## Requirements

### Requirement 1: Club Registration

**User Story:** As a club owner, I want to register my club with the platform, so that I can manage events under my club's profile.

#### Acceptance Criteria

1. WHEN a registration request is received with a unique email, name, password, contact number, and address, THE System SHALL create a Club record and a linked User account with the CLUB_OWNER role.
2. WHEN a registration request is received with an email that already exists, THE System SHALL return HTTP 409 with a descriptive error message.
3. WHEN a registration request is received with any required field missing or blank, THE System SHALL return HTTP 400 with a field-level validation error.
4. THE Auth_Service SHALL hash the password using BCrypt before persisting the User record.
5. WHEN a Club_Owner account is created, THE System SHALL send a verification email to the registered email address containing a time-limited verification link.
6. WHILE a Club_Owner account is unverified, THE System SHALL reject login attempts with HTTP 403 and the message "Account not verified".

---

### Requirement 2: Authentication and Token Management

**User Story:** As a registered club owner, staff member, or admin, I want to log in with my credentials and maintain my session securely, so that I can access protected management features without re-authenticating frequently.

#### Acceptance Criteria

1. WHEN a login request is received with a valid email and correct password for a verified, active account, THE Auth_Service SHALL return a signed Access_Token (expiry 1 hour), a Refresh_Token (expiry 7 days), and the user's role.
2. WHEN a login request is received with an unrecognised email or incorrect password, THE Auth_Service SHALL return HTTP 401 with the message "Invalid credentials".
3. WHEN a login request is received for a deactivated account, THE Auth_Service SHALL return HTTP 403 with the message "Account is deactivated".
4. THE Auth_Service SHALL sign Access_Tokens with an expiry of exactly 1 hour.
5. WHEN a request is received on a protected endpoint without a valid JWT token, THE Security_Filter SHALL return HTTP 401.
6. WHEN a request is received on a protected endpoint with an expired Access_Token, THE Security_Filter SHALL return HTTP 401 with the message "Token expired".
7. THE System SHALL expose public (unauthenticated) read endpoints for events and clubs so that Attendees can browse without logging in.
8. WHEN a refresh request is received with a valid, non-expired Refresh_Token, THE Auth_Service SHALL return a new Access_Token with HTTP 200.
9. WHEN a refresh request is received with an expired Refresh_Token, THE Auth_Service SHALL return HTTP 401 with the message "Refresh token expired".
10. WHEN a refresh request is received with an invalid or unrecognised Refresh_Token, THE Auth_Service SHALL return HTTP 401 with the message "Invalid refresh token".
11. WHEN a user logs out, THE Auth_Service SHALL invalidate the associated Refresh_Token so that it cannot be used to obtain further Access_Tokens.

---

### Requirement 3: Admin Account Management

**User Story:** As an admin, I want to manage clubs and users on the platform, so that I can maintain data integrity and handle escalations.

#### Acceptance Criteria

1. THE System SHALL restrict Admin account creation to direct database seeding; no public registration endpoint SHALL accept the ADMIN role.
2. WHEN an Admin sends a request to deactivate a Club, THE Club_Service SHALL set the Club's `isActive` flag to false and deactivate all associated User accounts.
3. WHEN an Admin sends a request to delete a Club, THE Club_Service SHALL remove the Club record and all associated Events and Staff assignments.
4. WHEN an Admin sends a request to create an Event for any Club, THE Event_Service SHALL create the Event linked to the specified Club regardless of which Club the Admin belongs to.
5. WHEN an Admin sends a request to update any Club's details, THE Club_Service SHALL apply the update and return the updated ClubDTO.
6. IF a request to perform an Admin-only action is received from a non-Admin JWT, THEN THE Security_Filter SHALL return HTTP 403.

---

### Requirement 4: Club Event Management

**User Story:** As a club owner or staff member, I want to create and manage events for my club, so that I can promote upcoming nights to the public.

#### Acceptance Criteria

1. WHEN a Club_Owner or Staff sends a create-event request with a name, startDateTime, endDateTime, and a valid clubId matching their own club, THE Event_Service SHALL persist the Event with status DRAFT and return the EventDTO with HTTP 201.
2. WHEN a create-event request is received where endDateTime is not after startDateTime, THE Event_Service SHALL return HTTP 400 with the message "End date must be after start date".
3. WHEN a Club_Owner or Staff sends an update-event request for an Event belonging to their own club, THE Event_Service SHALL apply the changes and return the updated EventDTO.
4. IF a Club_Owner or Staff sends an update-event or delete-event request for an Event that does not belong to their club, THEN THE Event_Service SHALL return HTTP 403.
5. WHEN a Club_Owner sends a delete-event request for an Event belonging to their club, THE Event_Service SHALL remove the Event record and return HTTP 204.
6. WHEN a create-event or update-event request is received with a capacity value less than 1, THE Event_Service SHALL return HTTP 400 with the message "Capacity must be at least 1".
7. WHEN a Staff member sends an update-event request for an Event belonging to their assigned club, THE Event_Service SHALL apply the changes and return the updated EventDTO with HTTP 200.
8. IF a Staff member sends a delete-event request, THEN THE Event_Service SHALL return HTTP 403 with the message "Staff are not permitted to delete events".

---

### Requirement 5: Event Capacity and Gender Ratio

**User Story:** As a club owner, I want to define capacity and male/female ratios for my events, so that I can manage entry and maintain the desired crowd balance.

#### Acceptance Criteria

1. THE Event_Service SHALL store a non-negative integer capacity on each Event representing the maximum number of attendees.
2. WHEN an event is created or updated with maleRatio and femaleRatio values, THE Event_Service SHALL validate that maleRatio + femaleRatio equals 100; IF the sum does not equal 100, THEN THE Event_Service SHALL return HTTP 400 with the message "Male and female ratios must sum to 100".
3. WHERE maleRatio and femaleRatio are not provided, THE System SHALL default both values to 50.
4. THE Event_Service SHALL include capacity, maleRatio, and femaleRatio in the EventDTO response.

---

### Requirement 6: Staff Assignment to Events

**User Story:** As a club owner, I want to assign staff members to specific events, so that I can communicate responsibilities and manage my team.

#### Acceptance Criteria

1. WHEN a Club_Owner sends a staff-assignment request with a valid eventId and a userId whose role is STAFF and who belongs to the same club, THE Event_Service SHALL create a Staff_Assignment record and return HTTP 201.
2. IF a staff-assignment request references a userId that does not belong to the same club as the event, THEN THE Event_Service SHALL return HTTP 403.
3. WHEN a Club_Owner sends a request to remove a staff assignment, THE Event_Service SHALL delete the Staff_Assignment record and return HTTP 204.
4. WHEN a request is received to list staff for an event, THE Event_Service SHALL return all Staff_Assignment records for that event including the staff member's name and role.

---

### Requirement 7: Public Event Browsing

**User Story:** As an attendee, I want to browse upcoming events without creating an account, so that I can discover nights out at local clubs.

#### Acceptance Criteria

1. THE Event_Service SHALL expose a public GET endpoint that returns a paginated list of all PUBLISHED, future Events ordered by startDateTime ascending.
2. WHEN a request is received to retrieve a single event by ID, THE Event_Service SHALL return the EventDTO including clubName, genre, dressCode, entryTypes, ageRestriction, capacity, imageUrl, and any active discounts; IF the event status is not PUBLISHED, THEN THE Event_Service SHALL return HTTP 404 with the message "Event not found".
3. WHEN a request is received to list events filtered by clubId, THE Event_Service SHALL return only PUBLISHED Events belonging to that Club.
4. WHEN a request is received to list events filtered by a date range, THE Event_Service SHALL return only PUBLISHED Events whose startDateTime falls within the specified range.
5. IF a request is received for an event ID that does not exist, THEN THE Event_Service SHALL return HTTP 404 with the message "Event not found".

---

### Requirement 8: Public Club Browsing

**User Story:** As an attendee, I want to view club profiles, so that I can learn about venues before attending an event.

#### Acceptance Criteria

1. THE Club_Service SHALL expose a public GET endpoint that returns a paginated list of all active Clubs.
2. WHEN a request is received to retrieve a single Club by ID, THE Club_Service SHALL return the ClubDTO including name, description, address, openingHours, closingHours, dressCode, hasParking, hasVIPArea, capacity, logoUrl, coverImageUrl, website, phone, email, and socials.
3. IF a request is received for a Club ID that does not exist, THEN THE Club_Service SHALL return HTTP 404 with the message "Club not found".
4. IF a request is received for a Club that has isActive set to false, THEN THE Club_Service SHALL return HTTP 404 with the message "Club not found".

---

### Requirement 9: User Account Management

**User Story:** As a club owner, I want to manage staff accounts linked to my club, so that I can control who has access to manage our events.

#### Acceptance Criteria

1. WHEN a Club_Owner sends a create-staff request with a username, email, password, and fullName, THE User_Service SHALL create a User with the STAFF role linked to the Club_Owner's club and return the UserResponseDTO with HTTP 201.
2. WHEN a Club_Owner sends a deactivate-staff request for a User belonging to their club, THE User_Service SHALL set the User's `isActive` flag to false.
3. IF a Club_Owner sends a deactivate-staff request for a User that does not belong to their club, THEN THE User_Service SHALL return HTTP 403.
4. WHEN a Club_Owner or Staff sends a request to update their own profile (fullName, phoneNumber), THE User_Service SHALL apply the changes and return the updated UserResponseDTO.
5. THE User_Service SHALL not permit a Club_Owner or Staff to change their own role via the update-profile endpoint.
6. WHEN a Staff member sends a request to view all staff in their club, THE User_Service SHALL return a list of UserResponseDTO records for all Staff belonging to the same club.

---

### Requirement 10: Input Validation and Error Handling

**User Story:** As a developer integrating with the API, I want consistent and descriptive error responses, so that I can handle failures gracefully in the client application.

#### Acceptance Criteria

1. THE System SHALL return all error responses as JSON objects containing at minimum a `timestamp`, `status`, `error`, and `message` field.
2. WHEN a request body fails bean validation, THE System SHALL return HTTP 400 with a `errors` array listing each failing field and its constraint message.
3. WHEN an unhandled exception occurs, THE System SHALL log the full stack trace and return HTTP 500 with the message "An unexpected error occurred" without exposing internal details.
4. THE System SHALL validate that email fields conform to standard email format; IF an invalid email format is provided, THEN THE System SHALL return HTTP 400.

---

### Requirement 11: Event Discounts

**User Story:** As a club owner or staff member, I want to add discounts to events, so that I can attract more attendees with promotional pricing.

#### Acceptance Criteria

1. WHEN a Club_Owner or Staff sends a create-discount request for an Event belonging to their club, THE Event_Service SHALL persist a Discount record linked to the Event and return HTTP 201.
2. THE Event_Service SHALL store the following fields on each Discount: `discountType` (e.g. "PERCENTAGE", "FIXED_AMOUNT", "FREE_ENTRY"), `discountValue` (a non-negative decimal), `description` (a human-readable label such as "Ladies free before 11pm"), `validFrom` (LocalDateTime), and `validUntil` (LocalDateTime).
3. WHEN a discount is created or updated with a validUntil that is not after validFrom, THE Event_Service SHALL return HTTP 400 with the message "Discount end time must be after start time".
4. WHEN a discount is created with a discountValue less than 0, THE Event_Service SHALL return HTTP 400 with the message "Discount value must be non-negative".
5. WHEN a Club_Owner or Staff sends an update-discount request for a Discount on an Event belonging to their club, THE Event_Service SHALL apply the changes and return the updated Discount details with HTTP 200.
6. WHEN a Club_Owner or Staff sends a delete-discount request for a Discount on an Event belonging to their club, THE Event_Service SHALL remove the Discount record and return HTTP 204.
7. IF a discount request references an Event that does not belong to the requesting user's club, THEN THE Event_Service SHALL return HTTP 403.
8. WHEN a public request is received to retrieve an Event, THE Event_Service SHALL include all currently active Discounts (where the current time is between validFrom and validUntil) in the EventDTO response.

---

### Requirement 12: Live Attendance Tracking and Check-in Audit

**User Story:** As a club owner or staff member, I want to track the live female-to-male ratio of attendees and maintain a full audit trail of check-in actions, so that I can manage entry in real time and review historical data.

#### Acceptance Criteria

1. THE Event_Service SHALL store `liveMaleCount` and `liveFemaleCount` as non-negative integer fields on each Event, both defaulting to 0 when the event is created.
2. WHEN a Club_Owner or Staff sends a check-in request specifying an eventId and a gender ("MALE" or "FEMALE"), THE Event_Service SHALL increment the corresponding live count by 1, persist a CheckInAuditEntry recording the eventId, gender, action "CHECK_IN", the performedBy userId, and the current timestamp, and return the updated live counts with HTTP 200.
3. WHEN a Club_Owner or Staff sends a check-out request specifying an eventId and a gender, THE Event_Service SHALL decrement the corresponding live count by 1 and persist a CheckInAuditEntry with action "CHECK_OUT"; IF the count is already 0, THEN THE Event_Service SHALL return HTTP 400 with the message "Count cannot go below zero".
4. WHEN a request is received to retrieve a single Event, THE Event_Service SHALL include `liveMaleCount`, `liveFemaleCount`, and `liveTotalCount` (the sum of both) in the EventDTO response.
5. WHEN a request is received to retrieve a single Event, THE Event_Service SHALL include `liveMalePercentage` and `liveFemalePercentage` (each rounded to one decimal place) in the EventDTO response; WHERE liveTotalCount is 0, THE Event_Service SHALL return both percentages as 0.0.
6. IF a check-in or check-out request is received from a user who is not a Club_Owner or Staff of the event's club, THEN THE Event_Service SHALL return HTTP 403.
7. WHEN a Club_Owner or Admin sends a request to retrieve the check-in audit log for an event, THE Event_Service SHALL return a paginated list of CheckInAuditDTO records for that event ordered by timestamp descending.
8. IF a Staff member sends a request to retrieve the check-in audit log, THEN THE Event_Service SHALL return HTTP 403 with the message "Staff are not permitted to access audit logs".

---

### Requirement 13: Staff Permissions

**User Story:** As a staff member, I want to perform event management tasks for my club, so that I can assist the club owner without needing owner-level access.

#### Acceptance Criteria

1. WHEN a Staff member sends a create-event request with a valid clubId matching their assigned club, THE Event_Service SHALL create the Event and return the EventDTO with HTTP 201.
2. WHEN a Staff member sends an update-event request for an Event belonging to their assigned club, THE Event_Service SHALL apply the changes and return the updated EventDTO with HTTP 200.
3. IF a Staff member sends a delete-event request, THEN THE Event_Service SHALL return HTTP 403 with the message "Staff are not permitted to delete events".
4. WHEN a Staff member sends a create-discount or update-discount request for an Event belonging to their assigned club, THE Event_Service SHALL process the request identically to a Club_Owner request.
5. WHEN a Staff member sends a check-in or check-out request for an Event belonging to their assigned club, THE Event_Service SHALL process the request identically to a Club_Owner request.
6. IF a Staff member sends a request to create, update, or delete a Club record, THEN THE Club_Service SHALL return HTTP 403 with the message "Staff are not permitted to modify club details".
7. IF a Staff member sends a request to create or deactivate another Staff account, THEN THE User_Service SHALL return HTTP 403 with the message "Staff are not permitted to manage user accounts".
8. WHEN a Staff member sends a request to transition an Event status from DRAFT to PUBLISHED for an Event belonging to their assigned club, THE Event_Service SHALL apply the transition and return the updated EventDTO with HTTP 200.
9. IF a Staff member sends a request to transition an Event status to CANCELLED or COMPLETED, THEN THE Event_Service SHALL return HTTP 403 with the message "Staff are not permitted to cancel or complete events".

---

### Requirement 14: DTO Specifications

**User Story:** As a developer integrating with the API, I want clearly defined response shapes for all major resources, so that I can build a reliable client without ambiguity.

#### Acceptance Criteria

1. THE System SHALL return EventDTO responses containing the following fields: `id` (Long), `name` (String), `description` (String), `startDateTime` (ISO-8601 LocalDateTime), `endDateTime` (ISO-8601 LocalDateTime), `genre` (String), `dressCode` (String), `ageRestriction` (String), `imageUrl` (String), `status` (String — one of DRAFT, PUBLISHED, CANCELLED, COMPLETED), `isActive` (boolean), `clubId` (Long), `clubName` (String), `capacity` (int), `maleRatio` (int), `femaleRatio` (int), `liveMaleCount` (int), `liveFemaleCount` (int), `liveTotalCount` (int), `liveMalePercentage` (double), `liveFemalePercentage` (double), `entryTypes` (list of EntryTypeDTO), and `discounts` (list of DiscountDTO). The legacy `entryFee` (String) field is replaced by `entryTypes`.
2. THE System SHALL return ClubDTO responses containing the following fields: `id` (Long), `name` (String), `description` (String), `email` (String), `phone` (String), `website` (String), `logoUrl` (String), `coverImageUrl` (String), `openingHours` (String), `closingHours` (String), `dressCode` (String), `hasParking` (boolean), `hasVIPArea` (boolean), `capacity` (int), `isActive` (boolean), `address` (AddressDTO), and `socials` (SocialsDTO).
3. THE System SHALL return AddressDTO responses containing the following fields: `location` (String), `city` (String), `province` (String), `country` (String), and `postalCode` (String).
4. THE System SHALL return SocialsDTO responses containing the following fields: `facebookLink` (String), `instagramLink` (String), `twitterLink` (String), and `tiktokLink` (String).
5. THE System SHALL return UserResponseDTO responses containing the following fields: `id` (Long), `username` (String), `email` (String), `fullName` (String), `phoneNumber` (String), `role` (String), `clubId` (Long), and `isActive` (boolean).
6. THE System SHALL return DiscountDTO responses containing the following fields: `id` (Long), `discountType` (String), `discountValue` (BigDecimal), `description` (String), `validFrom` (ISO-8601 LocalDateTime), and `validUntil` (ISO-8601 LocalDateTime).
7. THE System SHALL return LoginRequestDTO requests containing the following fields: `username` (String) and `password` (String).
8. THE System SHALL accept RegisterRequestDTO requests containing the following fields: `username` (String), `password` (String), `email` (String), `fullName` (String), `phoneNumber` (String), `role` (String — one of CLUB_OWNER or STAFF only), and `clubId` (Long, required when role is STAFF); IF the `role` field contains any value other than CLUB_OWNER or STAFF (including ADMIN), THEN THE System SHALL return HTTP 400 with the message "Invalid role specified".
9. THE System SHALL return EntryTypeDTO responses containing the following fields: `id` (Long), `name` (String — e.g. "EARLY_BIRD", "GENERAL", "VIP"), `price` (BigDecimal), `description` (String), and `availableQuantity` (int).
10. THE System SHALL return CheckInAuditDTO responses containing the following fields: `id` (Long), `eventId` (Long), `gender` (String — "MALE" or "FEMALE"), `action` (String — "CHECK_IN" or "CHECK_OUT"), `performedBy` (Long — userId of the Staff or Club_Owner who performed the action), and `timestamp` (ISO-8601 LocalDateTime).
11. THE System SHALL return AuthResponseDTO responses containing the following fields: `accessToken` (String), `refreshToken` (String), and `role` (String).

---

### Requirement 15: Event Status Lifecycle

**User Story:** As a club owner, I want to manage the lifecycle of my events through defined statuses, so that I can draft events privately before making them visible to the public.

#### Acceptance Criteria

1. THE Event_Service SHALL store a `status` field on each Event with valid values: DRAFT, PUBLISHED, CANCELLED, and COMPLETED.
2. WHEN an Event is created, THE Event_Service SHALL set the initial status to DRAFT.
3. WHEN a Club_Owner or Admin sends a status-transition request for an Event belonging to their club, THE Event_Service SHALL apply the transition and return the updated EventDTO with HTTP 200.
4. WHEN a Staff member sends a status-transition request to move an Event from DRAFT to PUBLISHED for an Event belonging to their assigned club, THE Event_Service SHALL apply the transition and return the updated EventDTO with HTTP 200.
5. IF a Staff member sends a status-transition request to CANCELLED or COMPLETED, THEN THE Event_Service SHALL return HTTP 403 with the message "Staff are not permitted to cancel or complete events".
6. THE Event_Service SHALL only include Events with status PUBLISHED in all public browsing endpoints.
7. WHEN a Club_Owner or Admin sends a request to transition an Event to CANCELLED, THE Event_Service SHALL set the status to CANCELLED and return the updated EventDTO with HTTP 200.
8. WHEN a Club_Owner or Admin sends a request to transition an Event to COMPLETED, THE Event_Service SHALL set the status to COMPLETED and return the updated EventDTO with HTTP 200.
9. IF a status-transition request is received for an Event that does not belong to the requesting user's club, THEN THE Event_Service SHALL return HTTP 403.

---

### Requirement 16: Ticket and Entry Types

**User Story:** As a club owner or staff member, I want to define multiple entry pricing tiers for an event, so that I can offer options such as early bird, general admission, and VIP to attendees.

#### Acceptance Criteria

1. WHEN a Club_Owner or Staff sends a create-entry-type request for an Event belonging to their club with a name, price (BigDecimal), description, and availableQuantity, THE Event_Service SHALL persist the EntryType linked to the Event and return the EntryTypeDTO with HTTP 201.
2. WHEN a create-entry-type or update-entry-type request is received with a price less than 0, THE Event_Service SHALL return HTTP 400 with the message "Entry type price must be non-negative".
3. WHEN a create-entry-type or update-entry-type request is received with an availableQuantity less than 0, THE Event_Service SHALL return HTTP 400 with the message "Available quantity must be non-negative".
4. WHEN a Club_Owner or Staff sends an update-entry-type request for an EntryType on an Event belonging to their club, THE Event_Service SHALL apply the changes and return the updated EntryTypeDTO with HTTP 200.
5. WHEN a Club_Owner or Staff sends a delete-entry-type request for an EntryType on an Event belonging to their club, THE Event_Service SHALL remove the EntryType record and return HTTP 204.
6. IF an entry-type request references an Event that does not belong to the requesting user's club, THEN THE Event_Service SHALL return HTTP 403.
7. WHEN a request is received to retrieve an Event, THE Event_Service SHALL include all associated EntryType records as a list of EntryTypeDTO in the EventDTO response.

---

### Requirement 17: Event Image Upload

**User Story:** As a club owner or staff member, I want to attach an image to an event either by uploading a file or providing an external URL, so that the event listing is visually appealing to attendees.

#### Acceptance Criteria

1. WHEN a Club_Owner or Staff sends a multipart/form-data upload request containing an image file for an Event belonging to their club, THE Event_Service SHALL store the image, set the Event's `imageUrl` to the stored file's accessible URL, and return the updated EventDTO with HTTP 200.
2. WHEN a Club_Owner or Staff sends a request to set the imageUrl for an Event to an external URL string, THE Event_Service SHALL update the Event's `imageUrl` to the provided URL and return the updated EventDTO with HTTP 200.
3. IF an image upload request is received for an Event that does not belong to the requesting user's club, THEN THE Event_Service SHALL return HTTP 403.
4. WHEN a request is received to retrieve an Event, THE Event_Service SHALL include the `imageUrl` field in the EventDTO; WHERE no image has been set, THE Event_Service SHALL return `imageUrl` as null.
5. IF an uploaded file exceeds the maximum permitted size, THEN THE Event_Service SHALL return HTTP 400 with the message "Image file size exceeds the maximum allowed limit".

---

### Requirement 18: Club Owner Dashboard

**User Story:** As a club owner or staff member, I want to retrieve all events belonging to my club in a single authenticated endpoint with optional filtering, so that I can manage my event calendar efficiently.

#### Acceptance Criteria

1. WHEN a Club_Owner or Staff sends an authenticated request to the club dashboard events endpoint, THE Event_Service SHALL return a paginated list of all Events belonging to their club regardless of status.
2. WHEN the dashboard request includes an optional `status` query parameter, THE Event_Service SHALL filter the returned Events to only those matching the specified EventStatus value.
3. WHEN the dashboard request includes optional `startDate` and `endDate` query parameters, THE Event_Service SHALL filter the returned Events to only those whose startDateTime falls within the specified range.
4. IF a dashboard request is received without a valid JWT token, THEN THE Security_Filter SHALL return HTTP 401.
5. IF a dashboard request is received from a user whose role is neither CLUB_OWNER nor STAFF, THEN THE Security_Filter SHALL return HTTP 403.
6. THE Event_Service SHALL include all EventStatus values (DRAFT, PUBLISHED, CANCELLED, COMPLETED) in dashboard results so that Club_Owners and Staff can see the full state of their event calendar.

---

### Requirement 19: Password Reset

**User Story:** As a registered user, I want to reset my password via email when I have forgotten it, so that I can regain access to my account without contacting support.

#### Acceptance Criteria

1. WHEN a forgot-password request is received with a registered email address, THE Auth_Service SHALL generate a time-limited reset token (valid for 1 hour), store it against the user account, and send a password reset email containing the token link to the provided address.
2. WHEN a forgot-password request is received with an email address that does not match any registered account, THE Auth_Service SHALL return HTTP 200 with a generic message "If that email is registered, a reset link has been sent" to avoid user enumeration.
3. WHEN a reset-password request is received with a valid, non-expired reset token and a new password, THE Auth_Service SHALL update the user's password (hashed with BCrypt), invalidate the reset token, and return HTTP 200 with the message "Password updated successfully".
4. WHEN a reset-password request is received with an expired reset token, THE Auth_Service SHALL return HTTP 400 with the message "Reset token has expired".
5. WHEN a reset-password request is received with an invalid or unrecognised reset token, THE Auth_Service SHALL return HTTP 400 with the message "Invalid reset token".
6. WHEN a reset-password request is received with a new password that does not meet minimum complexity requirements, THE Auth_Service SHALL return HTTP 400 with a descriptive validation message.

---

### Requirement 20: Pagination Parameters

**User Story:** As a developer integrating with the API, I want all paginated endpoints to accept consistent query parameters, so that I can implement predictable list navigation in the client application.

#### Acceptance Criteria

1. THE System SHALL accept a `page` query parameter (0-indexed integer, default 0) on all paginated endpoints.
2. THE System SHALL accept a `size` query parameter (integer, default 20, maximum 100) on all paginated endpoints; IF a size value greater than 100 is provided, THEN THE System SHALL cap the page size at 100 without returning an error.
3. THE System SHALL accept a `sort` query parameter on all paginated endpoints in the format `field,direction` (e.g. `startDateTime,asc`).
4. THE Event_Service SHALL apply a default sort of `startDateTime ASC` on all event list endpoints when no `sort` parameter is provided.
5. THE Club_Service SHALL apply a default sort of `name ASC` on all club list endpoints when no `sort` parameter is provided.
6. THE System SHALL return paginated responses including `content` (list of items), `totalElements` (long), `totalPages` (int), `page` (int), and `size` (int) fields.
