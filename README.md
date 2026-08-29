# GigGuide

GigGuide is a backend REST API for managing gigs, events, clubs, and attendees. It handles user authentication, event management, staff assignments, check-ins, discounts, and more.

---

## Tech Stack

### Core Framework
- **Java 17**
- **Spring Boot 3.5.7** — application framework
- **Maven** — build and dependency management

### Web & API
- **Spring Web (MVC)** — REST API layer
- **Thymeleaf** — server-side templating
- **SpringDoc OpenAPI (Swagger UI) 2.8.3** — auto-generated API documentation

### Database & Persistence
- **PostgreSQL** — relational database
- **Spring Data JPA (Hibernate)** — ORM and repository layer

### Security & Authentication
- **Spring Security** — authentication and authorization
- **JWT (jjwt 0.11.5)** — stateless token-based auth with access and refresh tokens
- **Spring Validation** — request validation

### Caching
- **Redis** — caching layer (`@EnableCaching`)
- **Spring Data Redis** — Redis integration

### Email
- **Spring Mail** — email service (used for verification, password reset, etc.)

### Developer Utilities
- **Lombok** — boilerplate reduction (getters, setters, builders, etc.)
- **Spring DevTools** — hot reload during development

### Database Migrations
- **Flyway 11.7.2** — versioned schema migrations (`src/main/resources/db.migration/`)

### Messaging
- **Apache Kafka** — asynchronous event streaming
- **Spring Kafka** — Kafka integration (producer, consumer, `KafkaTemplate`)

### Logging
- **SLF4J + Logback** — structured logging on all controllers and services via `@Slf4j`

### Testing
- **Spring Boot Test** — unit and integration testing

---

## Project Structure

```
src/main/java/com/Gig/Guide/GigGuide/
├── Controllers/        # REST endpoints
├── Service/            # Business logic
├── Repositories/       # Data access layer
├── Models/             # JPA entities
├── DTO/                # Data transfer objects
├── Security/           # JWT filter and security config
├── Mapper/             # Entity <-> DTO mappers
├── Enums/              # Application enums (Role, EventStatus)
├── Exceptions/         # Global exception handling
├── Kafka/
│   ├── events/         # Event payload classes (ClubCreatedEvent, etc.)
│   ├── producer/       # Kafka producers (ClubEventProducer)
│   ├── consumer/       # Kafka consumers (ClubEventConsumer)
│   └── job/            # Scheduled retry jobs (FailedKafkaEventRetryJob)
└── utils/              # Utility classes (JWT token util)
```

---

## API Endpoints & Example Payloads

### Auth — `/api/auth`

**POST /api/auth/register**
```json
// Request
{
  "username": "johndoe",
  "password": "secret123",
  "email": "john@example.com",
  "fullName": "John Doe",
  "phoneNumber": "+27821234567",
  "role": "CLUB_OWNER"
}

// Response 201
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "phoneNumber": "+27821234567",
  "role": "CLUB_OWNER",
  "clubId": null,
  "active": true
}
```

**POST /api/auth/register** _(as STAFF — requires a clubId)_
```json
{
  "username": "janedoe",
  "password": "secret123",
  "email": "jane@example.com",
  "fullName": "Jane Doe",
  "phoneNumber": "+27829876543",
  "role": "STAFF",
  "clubId": 1
}
```

**POST /api/auth/login**
```json
// Request
{
  "email": "john@example.com",
  "password": "secret123"
}

// Response 200
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "CLUB_OWNER"
}
```

**POST /api/auth/refresh**
```json
// Request
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

// Response 200 — same shape as login response
```

**POST /api/auth/logout**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
// Response 204 No Content
```

**GET /api/auth/verify?token=`<token>`**
```
// Response 200
"Email verified successfully"
```

**POST /api/auth/forgot-password**
```json
{
  "email": "john@example.com"
}
// Response 200 — generic message to avoid email enumeration
```

**POST /api/auth/reset-password**
```json
{
  "token": "reset-token-from-email",
  "newPassword": "newSecret123"
}
// Response 200
```

---

### Users — `/api/users`

> All endpoints require a valid `Authorization: Bearer <accessToken>` header.

**POST /api/users/staff** _(CLUB_OWNER only)_
```json
// Request
{
  "username": "staffmember1",
  "password": "staffpass1",
  "email": "staff1@example.com",
  "fullName": "Staff Member",
  "phoneNumber": "+27801234567",
  "role": "STAFF",
  "clubId": 1
}

// Response 201 — UserResponseDTO
{
  "id": 5,
  "username": "staffmember1",
  "email": "staff1@example.com",
  "fullName": "Staff Member",
  "phoneNumber": "+27801234567",
  "role": "STAFF",
  "clubId": 1,
  "active": true
}
```

**PATCH /api/users/staff/{userId}/deactivate** _(CLUB_OWNER only)_
```
// No body — Response 200
```

**PUT /api/users/profile**
```json
// Request
{
  "fullName": "John Updated",
  "phoneNumber": "+27829999999"
}

// Response 200 — UserResponseDTO
```

**GET /api/users/staff**
```json
// Response 200
[
  {
    "id": 5,
    "username": "staffmember1",
    "email": "staff1@example.com",
    "fullName": "Staff Member",
    "phoneNumber": "+27801234567",
    "role": "STAFF",
    "clubId": 1,
    "active": true
  }
]
```

---

### Clubs — `/api/clubs`

**POST /api/clubs** _(ADMIN only)_
```json
// Request
{
  "name": "Club Havana",
  "description": "Premium nightclub in the CBD",
  "email": "info@havana.co.za",
  "phone": "+27111234567",
  "website": "https://havana.co.za",
  "openingHours": "21:00",
  "closingHours": "04:00",
  "dressCode": "Smart Casual",
  "hasParking": true,
  "hasVIPArea": true,
  "capacity": 500,
  "address": {
    "location": "12 Long Street",
    "city": "Cape Town",
    "province": "Western Cape",
    "country": "South Africa",
    "postalCode": "8001"
  },
  "socials": {
    "facebookLink": "https://facebook.com/clubhavana",
    "instagramLink": "https://instagram.com/clubhavana",
    "twitterLink": "https://twitter.com/clubhavana",
    "tiktokLink": "https://tiktok.com/@clubhavana"
  }
}

// Response 201
{
  "id": 1,
  "name": "Club Havana",
  "description": "Premium nightclub in the CBD",
  "email": "info@havana.co.za",
  "phone": "+27111234567",
  "website": "https://havana.co.za",
  "openingHours": "21:00",
  "closingHours": "04:00",
  "dressCode": "Smart Casual",
  "hasParking": true,
  "hasVIPArea": true,
  "capacity": 500,
  "active": true,
  "address": {
    "location": "12 Long Street",
    "city": "Cape Town",
    "province": "Western Cape",
    "country": "South Africa",
    "postalCode": "8001"
  },
  "socials": {
    "facebookLink": "https://facebook.com/clubhavana",
    "instagramLink": "https://instagram.com/clubhavana",
    "twitterLink": "https://twitter.com/clubhavana",
    "tiktokLink": "https://tiktok.com/@clubhavana"
  }
}
```

**GET /api/clubs?page=0&size=10&sort=name**
```json
// Response 200 — paginated
{
  "content": [
    {
      "id": 1,
      "name": "Club Havana",
      "description": "Premium nightclub in the CBD",
      "email": "info@havana.co.za",
      "phone": "+27111234567",
      "website": "https://havana.co.za",
      "logoUrl": "https://cdn.example.com/logo.png",
      "coverImageUrl": "https://cdn.example.com/cover.png",
      "openingHours": "21:00",
      "closingHours": "04:00",
      "dressCode": "Smart Casual",
      "hasParking": true,
      "hasVIPArea": true,
      "capacity": 500,
      "active": true,
      "address": {
        "location": "12 Long Street",
        "city": "Cape Town",
        "province": "Western Cape",
        "country": "South Africa",
        "postalCode": "8001"
      },
      "socials": {
        "facebookLink": "https://facebook.com/clubhavana",
        "instagramLink": "https://instagram.com/clubhavana",
        "twitterLink": "https://twitter.com/clubhavana",
        "tiktokLink": "https://tiktok.com/@clubhavana"
      }
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

**GET /api/clubs/{id}**
```json
// Response 200 — single ClubDTO (same shape as above content item)
```

**PUT /api/clubs/{id}** _(ADMIN or CLUB_OWNER)_
```json
// Request — full ClubDTO body
{
  "name": "Club Havana Updated",
  "description": "Updated description",
  "email": "new@havana.co.za",
  "phone": "+27111234567",
  "website": "https://havana.co.za",
  "openingHours": "20:00",
  "closingHours": "05:00",
  "dressCode": "Formal",
  "hasParking": true,
  "hasVIPArea": true,
  "capacity": 600,
  "active": true,
  "address": {
    "location": "15 Long Street",
    "city": "Cape Town",
    "province": "Western Cape",
    "country": "South Africa",
    "postalCode": "8001"
  },
  "socials": {
    "instagramLink": "https://instagram.com/clubhavana"
  }
}
// Response 200 — updated ClubDTO
```

**DELETE /api/clubs/{id}** _(ADMIN only)_
```
// Response 204 No Content
```

**PATCH /api/clubs/{id}/deactivate** _(ADMIN only)_
```
// No body — Response 200
```

---

### Events — `/api/events`

**GET /api/events?page=0&size=10**
```json
// Response 200 — paginated published events
{
  "content": [
    {
      "id": 10,
      "name": "Summer Rave 2026",
      "description": "Annual summer rave at Club Havana",
      "startDateTime": "2026-12-20T21:00:00",
      "endDateTime": "2026-12-21T04:00:00",
      "genre": "Techno",
      "dressCode": "All Black",
      "ageRestriction": "18+",
      "imageUrl": "https://cdn.example.com/events/rave.jpg",
      "status": "PUBLISHED",
      "active": true,
      "clubId": 1,
      "clubName": "Club Havana",
      "capacity": 500,
      "maleRatio": 60,
      "femaleRatio": 40,
      "liveMaleCount": 120,
      "liveFemaleCount": 80,
      "liveTotalCount": 200,
      "liveMalePercentage": 60.0,
      "liveFemalePercentage": 40.0,
      "entryTypes": [
        {
          "id": 1,
          "name": "General",
          "price": 150.00,
          "description": "Standard entry",
          "availableQuantity": 400
        },
        {
          "id": 2,
          "name": "VIP",
          "price": 500.00,
          "description": "VIP lounge access",
          "availableQuantity": 100
        }
      ],
      "discounts": [
        {
          "id": 1,
          "discountType": "EARLY_BIRD",
          "discountValue": 20.00,
          "description": "20% off before Dec 1",
          "validFrom": "2026-11-01T00:00:00",
          "validUntil": "2026-12-01T23:59:59"
        }
      ]
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

**POST /api/events** _(CLUB_OWNER, STAFF, ADMIN)_
```json
// Request
{
  "name": "Summer Rave 2026",
  "description": "Annual summer rave at Club Havana",
  "startDateTime": "2026-12-20T21:00:00",
  "endDateTime": "2026-12-21T04:00:00",
  "genre": "Techno",
  "dressCode": "All Black",
  "ageRestriction": "18+",
  "clubId": 1,
  "capacity": 500,
  "maleRatio": 60,
  "femaleRatio": 40
}
// Response 201 — EventDTO
```

**PUT /api/events/{id}** _(CLUB_OWNER, STAFF, ADMIN)_
```json
// Request — same shape as POST, all fields
// Response 200 — updated EventDTO
```

**PATCH /api/events/{id}/status** _(CLUB_OWNER, STAFF, ADMIN)_
```json
// Request — valid values: DRAFT, PUBLISHED, ONGOING, COMPLETED, CANCELLED
{
  "status": "PUBLISHED"
}
// Response 200 — updated EventDTO
```

**DELETE /api/events/{id}** _(CLUB_OWNER, ADMIN)_
```
// Response 204 No Content
```

**GET /api/events/club/{clubId}?page=0&size=10**
```json
// Response 200 — paginated EventDTO list for a specific club
```

**GET /api/events/dashboard?status=PUBLISHED&page=0&size=10** _(CLUB_OWNER, STAFF)_
```json
// Optional query params: status, startDate, endDate
// Response 200 — paginated events for the authenticated user's club
```

---

### Entry Types — `/api/events/{eventId}/entry-types`

**POST /api/events/{eventId}/entry-types** _(CLUB_OWNER, STAFF, ADMIN)_
```json
// Request
{
  "name": "VIP",
  "price": 500.00,
  "description": "VIP lounge access with bottle service",
  "availableQuantity": 100
}
// Response 201 — EntryTypeDTO
{
  "id": 2,
  "name": "VIP",
  "price": 500.00,
  "description": "VIP lounge access with bottle service",
  "availableQuantity": 100
}
```

**PUT /api/events/{eventId}/entry-types/{entryTypeId}**
```json
// Request — same shape as POST
// Response 200 — updated EntryTypeDTO
```

**DELETE /api/events/{eventId}/entry-types/{entryTypeId}**
```
// Response 204 No Content
```

---

### Discounts — `/api/events/{eventId}/discounts`

**POST /api/events/{eventId}/discounts** _(CLUB_OWNER, STAFF, ADMIN)_
```json
// Request
{
  "discountType": "EARLY_BIRD",
  "discountValue": 20.00,
  "description": "20% off for early bird bookings",
  "validFrom": "2026-11-01T00:00:00",
  "validUntil": "2026-12-01T23:59:59"
}
// Response 201 — DiscountDTO
{
  "id": 1,
  "discountType": "EARLY_BIRD",
  "discountValue": 20.00,
  "description": "20% off for early bird bookings",
  "validFrom": "2026-11-01T00:00:00",
  "validUntil": "2026-12-01T23:59:59"
}
```

**PUT /api/events/{eventId}/discounts/{discountId}**
```json
// Request — same shape as POST
// Response 200 — updated DiscountDTO
```

**DELETE /api/events/{eventId}/discounts/{discountId}**
```
// Response 204 No Content
```

---

### Attendance — `/api/events/{eventId}/attendance`

**POST /api/events/{eventId}/attendance/check-in** _(CLUB_OWNER, STAFF)_
```json
// Request
{
  "gender": "MALE"
}
// Response 200
{
  "liveMaleCount": 121,
  "liveFemaleCount": 80,
  "liveTotalCount": 201,
  "liveMalePercentage": 60.2,
  "liveFemalePercentage": 39.8
}
```

**POST /api/events/{eventId}/attendance/check-out** _(CLUB_OWNER, STAFF)_
```json
// Request
{
  "gender": "FEMALE"
}
// Response 200 — same shape as check-in response
```

**GET /api/events/{eventId}/attendance/audit?page=0&size=20** _(CLUB_OWNER, ADMIN)_
```json
// Response 200 — paginated audit log
{
  "content": [
    {
      "id": 1,
      "eventId": 10,
      "gender": "MALE",
      "action": "CHECK_IN",
      "performedBy": 5,
      "timestamp": "2026-12-20T21:15:30"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

---

### Staff Assignments — `/api/events/{eventId}/staff`

**POST /api/events/{eventId}/staff** _(CLUB_OWNER only)_
```json
// Request
{
  "staffUserId": 5
}
// Response 201 No Content
```

**GET /api/events/{eventId}/staff** _(CLUB_OWNER, STAFF)_
```json
// Response 200
[
  {
    "id": 5,
    "username": "staffmember1",
    "email": "staff1@example.com",
    "fullName": "Staff Member",
    "phoneNumber": "+27801234567",
    "role": "STAFF",
    "clubId": 1,
    "active": true
  }
]
```

**DELETE /api/events/{eventId}/staff/{userId}** _(CLUB_OWNER only)_
```
// Response 204 No Content
```

---

### Event Image — `/api/events/{eventId}/image`

**POST /api/events/{eventId}/image/upload** _(multipart/form-data)_
```
// Form field: file = <image file>
// Response 200 — updated EventDTO with imageUrl populated
```

**PUT /api/events/{eventId}/image/url**
```json
// Request
{
  "url": "https://cdn.example.com/events/my-event.jpg"
}
// Response 200 — updated EventDTO
```

---

## Database Migrations

Flyway manages all schema changes. Migration scripts live in `src/main/resources/db.migration/` and run automatically on startup.

| Version | Script | Description |
|---------|--------|-------------|
| V1 | `V1__create_addresses_table.sql` | `addresses` table — indexes on `city`, `province`, `country` |
| V2 | `V2__create_socials_table.sql` | `socials` table |
| V3 | `V3__create_owners_table.sql` | `owners` legacy table |
| V4 | `V4__create_clubs_table.sql` | `clubs` table — indexes on `active`, `name`, `address_id` |
| V5 | `V5__create_users_table.sql` | `users` table — indexes on `email`, `username`, `club_id + role`, verification/reset tokens |
| V6 | `V6__add_owner_to_clubs.sql` | Adds `owner_user_id` FK + index to clubs |
| V7 | `V7__create_refresh_tokens_table.sql` | `refresh_tokens` — indexes on `token`, `user_id`, `expires_at` |
| V8 | `V8__create_events_table.sql` | `events` — indexes on `status + start_date`, `club_id`, `club + status`, `club + date` |
| V9 | `V9__create_entry_types_table.sql` | `entry_types` — index on `event_id` |
| V10 | `V10__create_discounts_table.sql` | `discounts` — indexes on `event_id`, `valid_from`, `valid_until` |
| V11 | `V11__create_staff_assignments_table.sql` | `staff_assignments` — indexes on `event_id`, `user_id` + unique constraint |
| V12 | `V12__create_check_in_audit_entries_table.sql` | `check_in_audit_entries` — indexes on `event + timestamp`, `event + action`, `performed_by` |
| V13 | `V13__create_my_app_users_table.sql` | `my_app_user` legacy table — index on `email` |
| V14 | `V14__create_failed_kafka_events_table.sql` | `failed_kafka_events` — indexes on `resolved`, `topic`, `failed_at` |

### Indexes Summary

| Table | Index | Purpose |
|-------|-------|---------|
| `addresses` | `city`, `province`, `country` | Filter clubs by location |
| `clubs` | `active`, `name`, `address_id`, `owner_user_id` | Active club listings and lookups |
| `users` | `email`, `username` | Auth and security filter lookups |
| `users` | `club_id`, `club_id + role` | Load staff list per club |
| `users` | `verification_token`, `password_reset_token` | Token-based email flows (partial indexes) |
| `refresh_tokens` | `token`, `user_id`, `expires_at` | Token validation and cleanup |
| `events` | `status + start_date_time` | Public published event browsing |
| `events` | `club_id`, `club_id + status`, `club_id + start` | Dashboard queries with filters |
| `entry_types` | `event_id` | Load entry types with parent event |
| `discounts` | `event_id`, `valid_from`, `valid_until` | Load and filter active discounts |
| `staff_assignments` | `event_id`, `user_id` | Staff lookup per event and per user |
| `check_in_audit_entries` | `event_id + timestamp DESC` | Paginated audit log per event |
| `check_in_audit_entries` | `event_id + action`, `performed_by` | Count check-ins and staff activity |

> All scripts use `IF NOT EXISTS` so they are safe to run against a database that already has tables created by Hibernate.

---

## Logging

All controllers and service implementations use `@Slf4j` (Logback via SLF4J). Log entries include:

- **Controllers** — method, path params, authenticated user ID, and result counts on every request
- **Services** — business operation details, entity IDs, and warnings on validation failures or unauthorized access
- **EmailService** — errors logged via `log.error` instead of `System.err`

Example log output:
```
INFO  POST /api/auth/login - email=john@example.com
INFO  Login successful - userId=1, role=CLUB_OWNER
INFO  POST /api/events - userId=1, eventName=Summer Rave 2026, clubId=1
INFO  Event created - id=10, name=Summer Rave 2026, clubId=1, status=DRAFT
INFO  POST /api/events/10/attendance/check-in - userId=5, gender=MALE
INFO  Check-in recorded - eventId=10, gender=MALE, liveTotal=121
```

---

## Kafka — Asynchronous Event Streaming

When a club is created, a `ClubCreatedEvent` is published asynchronously to the `club-created` Kafka topic. The HTTP response returns immediately — Kafka delivery happens in the background.

### Flow

```
POST /api/clubs
      │
      ▼
ClubServiceImpl.createClub()
      │
      ├── saves club to PostgreSQL  ──► returns 201 to client
      │
      └── ClubEventProducer.publishClubCreated()
                │
                ▼
         Kafka Topic: club-created
                │
                ▼
         ClubEventConsumer (@KafkaListener)
         logs event / triggers downstream actions
```

### Event Payload — `ClubCreatedEvent`

```json
{
  "clubId": 1,
  "clubName": "Club Havana",
  "email": "info@havana.co.za",
  "city": "Cape Town",
  "createdAt": "2026-08-29T14:00:00"
}
```

### Kafka Classes

| Class | Package | Purpose |
|-------|---------|---------|
| `ClubCreatedEvent` | `Kafka/events` | Event payload serialized to JSON |
| `ClubEventProducer` | `Kafka/producer` | Publishes events using `KafkaTemplate` |
| `ClubEventConsumer` | `Kafka/consumer` | Listens on `club-created` with `@KafkaListener` |
| `FailedKafkaEventRetryJob` | `Kafka/job` | Scheduled retry job for failed deliveries |

### Kafka Config (`application.properties`)

```properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.consumer.group-id=gig-guide-group
spring.kafka.consumer.auto-offset-reset=earliest
app.kafka.topic.club-created=club-created
```

---

## Failed Kafka Events

When Kafka delivery fails (broker down, network error, etc.), the event is not lost — it is persisted to the `failed_kafka_events` table with the full JSON payload and error message.

### `failed_kafka_events` Table

| Column | Description |
|--------|-------------|
| `id` | Auto-generated primary key |
| `topic` | Kafka topic the message was intended for |
| `message_key` | Message key (e.g. clubId) |
| `payload` | Full event serialized as JSON |
| `error_message` | The Kafka error that caused the failure |
| `retry_count` | How many retry attempts have been made |
| `resolved` | `true` once the event is successfully re-published |
| `failed_at` | Timestamp of the original failure |
| `last_retried_at` | Timestamp of the most recent retry attempt |

### Retry Job

`FailedKafkaEventRetryJob` runs every **2 hours** via `@Scheduled`. It:
1. Loads all records where `resolved = false`
2. Deserializes the JSON payload back to the correct event class
3. Re-publishes to Kafka synchronously
4. Marks `resolved = true` on success, increments `retry_count` on failure
5. Retries **indefinitely** — never gives up until Kafka accepts the message

```properties
# Retry interval (2 hours)
app.kafka.retry.fixed-rate-ms=7200000
```

### Running Kafka Locally (Docker)

```bash
docker-compose up -d
```

This starts:
- **Zookeeper** on port `2181`
- **Kafka broker** on port `9092`
- **Kafka UI** at `http://localhost:8090` — browse topics and messages visually

---

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Redis 6+
- Docker (for Kafka)

### Environment Setup

Create a PostgreSQL database:
```sql
CREATE DATABASE gig_guide_db;
```

Update `src/main/resources/application.properties` with your credentials:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/gig_guide_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

Make sure Redis is running on `localhost:6379` (default), or update:
```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

Start Kafka locally:
```bash
docker-compose up -d
```

### Run the App

```bash
./mvnw spring-boot:run
```

Flyway will automatically apply all pending migrations on startup.

### API Docs

Once running, visit:

```
http://localhost:8080/swagger-ui/index.html
```

### Kafka UI

Browse topics and messages at:

```
http://localhost:8090
```
