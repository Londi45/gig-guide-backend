# React Integration Guide — Gig Guide API
# Complete Payloads, Flows & Expectations

Base URL: `http://localhost:8080`
All authenticated requests need: `Authorization: Bearer <accessToken>`
All request bodies use: `Content-Type: application/json`

---

## CRITICAL FIELD NAMING RULES

These will break your app silently if you get them wrong.

| What you expect | What the API actually sends | Why |
|---|---|---|
| `isActive` | `active` | Lombok strips `is` prefix on boolean getters |
| `isVerified` | `verified` | Same reason |
| `startDateTime` | `"2025-12-01T21:00:00"` | No timezone, no Z — ISO-8601 LocalDateTime |
| `role` | `"CLUB_OWNER"` / `"STAFF"` / `"ADMIN"` | Uppercase string enum |
| `status` | `"DRAFT"` / `"PUBLISHED"` / `"CANCELLED"` / `"COMPLETED"` | Uppercase string enum |
| `price` / `discountValue` | `150.00` (number) | BigDecimal serialises as JSON number |

When sending dates TO the backend, always format as `"yyyy-MM-ddTHH:mm:ss"` — no Z, no offset:
```js
const toBackendDate = (d) => new Date(d).toISOString().slice(0, 19);
// "2025-12-01T21:00:00"
```

---

## SECTION 1 — AXIOS SETUP

Create this file first. Everything else depends on it.

`src/api/axiosInstance.js`

```js
import axios from 'axios';

const api = axios.create({ baseURL: 'http://localhost:8080' });

// Attach access token to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Auto-refresh on 401
api.interceptors.response.use(
  (res) => res,
  async (error) => {
    const original = error.config;
    if (error.response?.status === 401 && !original._retry) {
      original._retry = true;
      try {
        const refreshToken = localStorage.getItem('refreshToken');
        const { data } = await axios.post('http://localhost:8080/api/auth/refresh', { refreshToken });
        localStorage.setItem('accessToken', data.accessToken);
        original.headers.Authorization = `Bearer ${data.accessToken}`;
        return api(original);
      } catch {
        localStorage.clear();
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
```


---

## SECTION 2 — AUTH APIS

### POST /api/auth/register — Club Owner Registration

Creates a Club + User in one shot. Use this for the public sign-up form.

```js
// Request
const payload = {
  username: "jazzclub_owner",
  password: "Secret123",
  email: "owner@jazzclub.com",
  fullName: "John Doe",
  phoneNumber: "0821234567",
  role: "CLUB_OWNER",          // "CLUB_OWNER" only on public register
  clubName: "Jazz Club Cape Town"  // required when role = CLUB_OWNER
};

const { data } = await api.post('/api/auth/register', payload);
// Response 201 — UserResponseDTO
// {
//   id: 1,
//   username: "jazzclub_owner",
//   email: "owner@jazzclub.com",
//   fullName: "John Doe",
//   phoneNumber: "0821234567",
//   role: "CLUB_OWNER",
//   clubId: 5,
//   active: true          // NOTE: "active" not "isActive"
// }
```

After register, the user gets a verification email. They CANNOT log in until they click the link.

---

### POST /api/auth/login

```js
// Request
const payload = {
  username: "owner@jazzclub.com",  // backend accepts email as username
  password: "Secret123"
};

const { data } = await api.post('/api/auth/login', payload);
// Response 200 — AuthResponseDTO
// {
//   accessToken: "eyJhbGci...",
//   refreshToken: "550e8400-e29b-41d4-a716-446655440000",
//   role: "CLUB_OWNER"
// }

localStorage.setItem('accessToken', data.accessToken);
localStorage.setItem('refreshToken', data.refreshToken);
localStorage.setItem('role', data.role);
```

Error cases:
- `401` — wrong credentials
- `403 "Account not verified"` — email not clicked yet
- `403 "Account is deactivated"` — admin deactivated the account

---

### POST /api/auth/refresh

Called automatically by the axios interceptor. You rarely call this manually.

```js
// Request
{ "refreshToken": "550e8400-e29b-41d4-a716-446655440000" }

// Response 200
// { accessToken: "eyJhbGci...", refreshToken: "550e8400...", role: "CLUB_OWNER" }
```

---

### POST /api/auth/logout

```js
const refreshToken = localStorage.getItem('refreshToken');
await api.post('/api/auth/logout', { refreshToken });
localStorage.clear();
// redirect to /login
```

---

### POST /api/auth/forgot-password

```js
await api.post('/api/auth/forgot-password', { email: "owner@jazzclub.com" });
// Always returns 200 with generic message — no enumeration of emails
```

---

### POST /api/auth/reset-password

```js
// token comes from the URL query param in the reset email link
await api.post('/api/auth/reset-password', {
  token: "reset-token-from-email-link",
  newPassword: "NewSecret456"  // min 8 chars, at least 1 letter + 1 digit
});
```

---

### GET /api/auth/verify?token=...

This is the link in the verification email. The React app just needs to handle the redirect:

```js
// On the /verify page, read the token from the URL and call:
const params = new URLSearchParams(window.location.search);
const token = params.get('token');
await api.get(`/api/auth/verify?token=${token}`);
// Show "Email verified! You can now log in."
```


---

## SECTION 3 — CLUBS APIS

### GET /api/clubs — Public list of active clubs (paginated)

```js
// No auth needed
const { data } = await api.get('/api/clubs', {
  params: { page: 0, size: 10, sort: 'name' }
});
// Response 200 — Spring Page<ClubDTO>
// {
//   content: [ ClubDTO, ... ],
//   totalElements: 42,
//   totalPages: 5,
//   number: 0,       // current page (0-indexed)
//   size: 10
// }
```

ClubDTO shape:
```json
{
  "id": 5,
  "name": "Jazz Club Cape Town",
  "description": "Best jazz in the city",
  "email": "info@jazzclub.com",
  "phone": "0211234567",
  "website": "https://jazzclub.co.za",
  "logoUrl": "/images/clubs/5/logo.jpg",
  "coverImageUrl": "/images/clubs/5/cover.jpg",
  "openingHours": "20:00",
  "closingHours": "02:00",
  "dressCode": "Smart casual",
  "hasParking": true,
  "hasVIPArea": true,
  "capacity": 300,
  "active": true,
  "address": {
    "location": "12 Long Street",
    "city": "Cape Town",
    "province": "Western Cape",
    "country": "South Africa",
    "postalCode": "8001"
  },
  "socials": {
    "facebookLink": "https://facebook.com/jazzclub",
    "instagramLink": "https://instagram.com/jazzclub",
    "twitterLink": null,
    "tiktokLink": null
  }
}
```

---

### GET /api/clubs/:id — Single club

```js
const { data } = await api.get(`/api/clubs/${clubId}`);
// Returns ClubDTO above
// 404 if not found or inactive
```

---

### PUT /api/clubs/:id — Update club (CLUB_OWNER or ADMIN)

```js
// Request body — full ClubDTO (id field is ignored, taken from path)
const payload = {
  name: "Jazz Club Cape Town",
  description: "Updated description",
  email: "info@jazzclub.com",
  phone: "0211234567",
  website: "https://jazzclub.co.za",
  logoUrl: null,
  coverImageUrl: null,
  openingHours: "20:00",
  closingHours: "02:00",
  dressCode: "Smart casual",
  hasParking: true,
  hasVIPArea: false,
  capacity: 350,
  address: {
    location: "12 Long Street",
    city: "Cape Town",
    province: "Western Cape",
    country: "South Africa",
    postalCode: "8001"
  },
  socials: {
    facebookLink: "https://facebook.com/jazzclub",
    instagramLink: null,
    twitterLink: null,
    tiktokLink: null
  }
};

const { data } = await api.put(`/api/clubs/${clubId}`, payload);
// Response 200 — updated ClubDTO
```

---

### PATCH /api/clubs/:id/deactivate — Deactivate club (ADMIN only)

```js
await api.patch(`/api/clubs/${clubId}/deactivate`);
// Response 200 — no body
// Side effect: all users linked to this club are also deactivated
```

---

### DELETE /api/clubs/:id — Delete club (ADMIN only)

```js
await api.delete(`/api/clubs/${clubId}`);
// Response 204 — no body
// Cascades: deletes all events and staff assignments
```


---

## SECTION 4 — EVENTS APIS

### GET /api/events — Public list of published future events

```js
const { data } = await api.get('/api/events', {
  params: { page: 0, size: 10 }
});
// Response 200 — Spring Page<EventDTO>
```

EventDTO shape (full):
```json
{
  "id": 12,
  "name": "Friday Night Jazz",
  "description": "Live jazz with a full band",
  "startDateTime": "2025-12-05T21:00:00",
  "endDateTime": "2025-12-06T02:00:00",
  "genre": "Jazz",
  "dressCode": "Smart casual",
  "ageRestriction": "18+",
  "imageUrl": "/images/events/12/banner.jpg",
  "status": "PUBLISHED",
  "active": true,
  "clubId": 5,
  "clubName": "Jazz Club Cape Town",
  "capacity": 200,
  "maleRatio": 50,
  "femaleRatio": 50,
  "liveMaleCount": 34,
  "liveFemaleCount": 41,
  "liveTotalCount": 75,
  "liveMalePercentage": 45.3,
  "liveFemalePercentage": 54.7,
  "entryTypes": [
    {
      "id": 1,
      "name": "EARLY_BIRD",
      "price": 80.00,
      "description": "Early bird special before 10pm",
      "availableQuantity": 50
    },
    {
      "id": 2,
      "name": "GENERAL",
      "price": 150.00,
      "description": "General admission",
      "availableQuantity": 150
    }
  ],
  "discounts": [
    {
      "id": 3,
      "discountType": "FREE_ENTRY",
      "discountValue": 0.00,
      "description": "Ladies free before 11pm",
      "validFrom": "2025-12-05T20:00:00",
      "validUntil": "2025-12-05T23:00:00"
    }
  ]
}
```

---

### GET /api/events/:id — Single public event

```js
const { data } = await api.get(`/api/events/${eventId}`);
// 404 if event is not PUBLISHED
// discounts array only contains currently active discounts (validFrom <= now <= validUntil)
```

---

### GET /api/events/club/:clubId — Events by club (public, PUBLISHED only)

```js
const { data } = await api.get(`/api/events/club/${clubId}`, {
  params: { page: 0, size: 10 }
});
```

---

### GET /api/events/dashboard — Club dashboard (CLUB_OWNER, STAFF)

Returns ALL statuses for the caller's own club. Supports optional filters.

```js
const { data } = await api.get('/api/events/dashboard', {
  params: {
    page: 0,
    size: 10,
    status: 'DRAFT',                        // optional: DRAFT | PUBLISHED | CANCELLED | COMPLETED
    startDate: '2025-12-01T00:00:00',       // optional ISO-8601
    endDate: '2025-12-31T23:59:59'          // optional ISO-8601
  }
});
```

---

### POST /api/events — Create event (CLUB_OWNER, STAFF, ADMIN)

```js
const payload = {
  name: "Friday Night Jazz",
  description: "Live jazz with a full band",
  startDateTime: "2025-12-05T21:00:00",   // toBackendDate() helper
  endDateTime: "2025-12-06T02:00:00",
  genre: "Jazz",
  dressCode: "Smart casual",
  ageRestriction: "18+",
  clubId: 5,                               // must match caller's club (or any club for ADMIN)
  capacity: 200,
  maleRatio: 50,                           // optional — defaults to 50
  femaleRatio: 50                          // optional — defaults to 50
};

const { data } = await api.post('/api/events', payload);
// Response 201 — EventDTO with status: "DRAFT"
```

Validation errors:
- `400` — endDateTime not after startDateTime
- `400` — capacity < 1
- `400` — maleRatio + femaleRatio != 100

---

### PUT /api/events/:id — Update event (CLUB_OWNER, STAFF, ADMIN)

```js
// Same shape as create payload
const { data } = await api.put(`/api/events/${eventId}`, payload);
// Response 200 — updated EventDTO
// 403 if event doesn't belong to caller's club
```

---

### DELETE /api/events/:id — Delete event (CLUB_OWNER, ADMIN only)

```js
await api.delete(`/api/events/${eventId}`);
// Response 204
// 403 if STAFF tries this
```

---

### PATCH /api/events/:id/status — Transition event status

```js
const { data } = await api.patch(`/api/events/${eventId}/status`, {
  status: "PUBLISHED"   // DRAFT | PUBLISHED | CANCELLED | COMPLETED
});
// Response 200 — updated EventDTO
// 403 if STAFF tries to set CANCELLED or COMPLETED
```

Valid transitions to know:
- DRAFT → PUBLISHED (all roles)
- PUBLISHED → CANCELLED (CLUB_OWNER, ADMIN only)
- PUBLISHED → COMPLETED (CLUB_OWNER, ADMIN only)


---

## SECTION 5 — ATTENDANCE APIS

Base path: `/api/events/:eventId/attendance`

### POST /api/events/:eventId/attendance/check-in

```js
const { data } = await api.post(`/api/events/${eventId}/attendance/check-in`, {
  gender: "FEMALE"   // "MALE" or "FEMALE"
});
// Response 200
// {
//   liveMaleCount: 34,
//   liveFemaleCount: 42,
//   liveTotalCount: 76
// }
```

---

### POST /api/events/:eventId/attendance/check-out

```js
const { data } = await api.post(`/api/events/${eventId}/attendance/check-out`, {
  gender: "MALE"
});
// Response 200 — same shape as check-in
// 400 "Count cannot go below zero" if count is already 0
```

---

### GET /api/events/:eventId/attendance/audit — Audit log (CLUB_OWNER, ADMIN only)

```js
const { data } = await api.get(`/api/events/${eventId}/attendance/audit`, {
  params: { page: 0, size: 20 }
});
// Response 200 — Spring Page<CheckInAuditDTO>
// {
//   content: [
//     {
//       id: 101,
//       eventId: 12,
//       gender: "FEMALE",
//       action: "CHECK_IN",
//       performedBy: 7,        // userId of staff/owner who scanned
//       timestamp: "2025-12-05T22:14:33"
//     }
//   ],
//   totalElements: 150,
//   totalPages: 8,
//   number: 0,
//   size: 20
// }
// 403 if STAFF tries this
```


---

## SECTION 6 — DISCOUNTS APIS

Base path: `/api/events/:eventId/discounts`

### POST /api/events/:eventId/discounts — Add discount

```js
const payload = {
  discountType: "FREE_ENTRY",       // "PERCENTAGE" | "FIXED_AMOUNT" | "FREE_ENTRY"
  discountValue: 0.00,              // non-negative; use 0 for FREE_ENTRY
  description: "Ladies free before 11pm",
  validFrom: "2025-12-05T20:00:00",
  validUntil: "2025-12-05T23:00:00"
};

const { data } = await api.post(`/api/events/${eventId}/discounts`, payload);
// Response 201 — DiscountDTO (same shape with id added)
```

Validation errors:
- `400` — validUntil not after validFrom
- `400` — discountValue < 0

---

### PUT /api/events/:eventId/discounts/:discountId — Update discount

```js
const { data } = await api.put(`/api/events/${eventId}/discounts/${discountId}`, payload);
// Response 200 — updated DiscountDTO
```

---

### DELETE /api/events/:eventId/discounts/:discountId — Delete discount

```js
await api.delete(`/api/events/${eventId}/discounts/${discountId}`);
// Response 204
```

---

## SECTION 7 — ENTRY TYPES APIS

Base path: `/api/events/:eventId/entry-types`

### POST /api/events/:eventId/entry-types — Add entry type

```js
const payload = {
  name: "EARLY_BIRD",          // free text — "EARLY_BIRD" | "GENERAL" | "VIP" are conventions
  price: 80.00,                // non-negative BigDecimal
  description: "Early bird special before 10pm",
  availableQuantity: 50        // non-negative int
};

const { data } = await api.post(`/api/events/${eventId}/entry-types`, payload);
// Response 201 — EntryTypeDTO (same shape with id added)
```

Validation errors:
- `400` — price < 0
- `400` — availableQuantity < 0

---

### PUT /api/events/:eventId/entry-types/:entryTypeId — Update entry type

```js
const { data } = await api.put(`/api/events/${eventId}/entry-types/${entryTypeId}`, payload);
// Response 200 — updated EntryTypeDTO
```

---

### DELETE /api/events/:eventId/entry-types/:entryTypeId — Delete entry type

```js
await api.delete(`/api/events/${eventId}/entry-types/${entryTypeId}`);
// Response 204
```


---

## SECTION 8 — IMAGE APIS

Base path: `/api/events/:eventId/image`

### POST /api/events/:eventId/image/upload — Upload image file

```js
const formData = new FormData();
formData.append('file', fileInputRef.current.files[0]);

const { data } = await api.post(`/api/events/${eventId}/image/upload`, formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
});
// Response 200 — full EventDTO with updated imageUrl
// imageUrl will be something like: "/images/events/12/12_uuid.jpg"
// Serve it as: http://localhost:8080/images/events/12/12_uuid.jpg
```

Max file size: 5 MB (configurable on backend).

---

### PUT /api/events/:eventId/image/url — Set external image URL

```js
const { data } = await api.put(`/api/events/${eventId}/image/url`, {
  url: "https://cdn.example.com/my-event-banner.jpg"
});
// Response 200 — full EventDTO with updated imageUrl
```

---

## SECTION 9 — STAFF ASSIGNMENT APIS

Base path: `/api/events/:eventId/staff`

### POST /api/events/:eventId/staff — Assign staff to event (CLUB_OWNER only)

```js
const { data } = await api.post(`/api/events/${eventId}/staff`, {
  staffUserId: 9    // must be a STAFF user belonging to the same club
});
// Response 201 — no body
// 403 if staffUserId belongs to a different club
```

---

### DELETE /api/events/:eventId/staff/:userId — Remove staff assignment (CLUB_OWNER only)

```js
await api.delete(`/api/events/${eventId}/staff/${staffUserId}`);
// Response 204
```

---

### GET /api/events/:eventId/staff — List staff assigned to event

```js
const { data } = await api.get(`/api/events/${eventId}/staff`);
// Response 200 — Array<UserResponseDTO>
// [
//   {
//     id: 9,
//     username: "jane_staff",
//     email: "jane@jazzclub.com",
//     fullName: "Jane Smith",
//     phoneNumber: "0831234567",
//     role: "STAFF",
//     clubId: 5,
//     active: true
//   }
// ]
```

---

## SECTION 10 — USER MANAGEMENT APIS

Base path: `/api/users`

### POST /api/users/staff — Create staff account (CLUB_OWNER only)

```js
const payload = {
  username: "jane_staff",
  password: "Staff123",
  email: "jane@jazzclub.com",
  fullName: "Jane Smith",
  phoneNumber: "0831234567",
  role: "STAFF",
  clubId: 5    // must match the CLUB_OWNER's own club
};

const { data } = await api.post('/api/users/staff', payload);
// Response 201 — UserResponseDTO
```

---

### PATCH /api/users/staff/:userId/deactivate — Deactivate staff (CLUB_OWNER or ADMIN)

```js
await api.patch(`/api/users/staff/${staffUserId}/deactivate`);
// Response 200 — no body
// 403 if staff doesn't belong to caller's club
```

---

### PUT /api/users/profile — Update own profile (CLUB_OWNER or STAFF)

```js
const { data } = await api.put('/api/users/profile', {
  fullName: "John Updated",
  phoneNumber: "0829876543"
  // role is ignored even if you send it
});
// Response 200 — UserResponseDTO
```

---

### GET /api/users/staff — List all staff in own club (CLUB_OWNER or STAFF)

```js
const { data } = await api.get('/api/users/staff');
// Response 200 — Array<UserResponseDTO>
```


---

## SECTION 11 — ERROR RESPONSE SHAPE

Every error from the API looks like this:

```json
{
  "timestamp": "2025-12-05T21:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "End date must be after start date",
  "errors": null
}
```

On validation failure, `errors` is populated:

```json
{
  "timestamp": "2025-12-05T21:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    { "field": "email", "message": "must be a well-formed email address" },
    { "field": "password", "message": "must not be blank" }
  ]
}
```

Handle it in React like this:

```js
try {
  await api.post('/api/auth/register', payload);
} catch (err) {
  const { status, data } = err.response;
  if (status === 400 && data.errors) {
    // field-level validation errors
    data.errors.forEach(e => console.log(`${e.field}: ${e.message}`));
  } else {
    // single message error
    console.log(data.message);
  }
}
```

---

## SECTION 12 — COMPLETE APP FLOW

### Flow 1: Public Attendee (no login)

```
/ (Home)
  └── GET /api/events?page=0&size=10          → event cards grid
  └── GET /api/clubs?page=0&size=10           → club cards grid

/events/:id
  └── GET /api/events/:id                     → full event detail + entry types + active discounts

/clubs/:id
  └── GET /api/clubs/:id                      → club profile + address + socials
  └── GET /api/events/club/:id                → events for this club
```

---

### Flow 2: Club Owner Registration & Login

```
/register
  └── POST /api/auth/register (role: CLUB_OWNER, clubName required)
      → on 201: show "Check your email to verify your account"

/verify (user clicks email link → /verify?token=...)
  └── GET /api/auth/verify?token=...
      → on 200: redirect to /login

/login
  └── POST /api/auth/login
      → store accessToken, refreshToken, role in localStorage
      → redirect to /dashboard
```

---

### Flow 3: Club Owner Dashboard

```
/dashboard
  └── GET /api/events/dashboard               → all events for own club (all statuses)
      supports ?status=DRAFT&page=0

/dashboard/events/new
  └── POST /api/events                        → create event (status = DRAFT)

/dashboard/events/:id/edit
  └── GET /api/events/:id                     → load current data
  └── PUT /api/events/:id                     → save changes

/dashboard/events/:id/publish
  └── PATCH /api/events/:id/status { status: "PUBLISHED" }

/dashboard/events/:id/cancel
  └── PATCH /api/events/:id/status { status: "CANCELLED" }

/dashboard/events/:id/entry-types
  └── POST /api/events/:id/entry-types        → add tier
  └── PUT  /api/events/:id/entry-types/:etId  → edit tier
  └── DELETE /api/events/:id/entry-types/:etId

/dashboard/events/:id/discounts
  └── POST /api/events/:id/discounts          → add discount
  └── PUT  /api/events/:id/discounts/:dId     → edit discount
  └── DELETE /api/events/:id/discounts/:dId

/dashboard/events/:id/image
  └── POST /api/events/:id/image/upload       → file upload
  └── PUT  /api/events/:id/image/url          → external URL

/dashboard/events/:id/attendance
  └── POST /api/events/:id/attendance/check-in  { gender: "MALE"|"FEMALE" }
  └── POST /api/events/:id/attendance/check-out { gender: "MALE"|"FEMALE" }
  └── GET  /api/events/:id/attendance/audit   → audit log (CLUB_OWNER only)

/dashboard/staff
  └── GET  /api/users/staff                   → list all staff
  └── POST /api/users/staff                   → create staff account
  └── PATCH /api/users/staff/:id/deactivate   → deactivate staff

/dashboard/events/:id/staff
  └── GET    /api/events/:id/staff            → assigned staff
  └── POST   /api/events/:id/staff            → assign staff { staffUserId }
  └── DELETE /api/events/:id/staff/:userId    → remove assignment

/dashboard/club/edit
  └── GET /api/clubs/:clubId                  → load current club data
  └── PUT /api/clubs/:clubId                  → save changes

/profile
  └── PUT /api/users/profile                  → update own name/phone
```

---

### Flow 4: Staff Login

```
/login
  └── POST /api/auth/login
      → role = "STAFF" in response
      → redirect to /dashboard (same dashboard, fewer options)

Staff CAN:
  - View dashboard events
  - Create / update events (not delete)
  - DRAFT → PUBLISHED transition only
  - Add/update/delete discounts and entry types
  - Check in / check out attendees
  - View assigned staff for an event
  - Update own profile

Staff CANNOT:
  - Delete events (403)
  - CANCELLED / COMPLETED transitions (403)
  - View audit log (403)
  - Create/deactivate staff accounts (403)
  - Modify club details (403)
```

---

### Flow 5: Token Expiry Handling

The axios interceptor (Section 1) handles this automatically:
1. Request fails with 401
2. Interceptor calls POST /api/auth/refresh with stored refreshToken
3. New accessToken stored, original request retried
4. If refresh also fails (expired/invalid) → localStorage.clear() + redirect to /login

---

## SECTION 13 — QUICK REFERENCE TABLE

| Action | Method | Path | Auth | Body key fields |
|--------|--------|------|------|-----------------|
| Register club owner | POST | /api/auth/register | None | username, password, email, fullName, phoneNumber, role:"CLUB_OWNER", clubName |
| Login | POST | /api/auth/login | None | username (email), password |
| Refresh token | POST | /api/auth/refresh | None | refreshToken |
| Logout | POST | /api/auth/logout | Bearer | refreshToken |
| Forgot password | POST | /api/auth/forgot-password | None | email |
| Reset password | POST | /api/auth/reset-password | None | token, newPassword |
| Verify email | GET | /api/auth/verify?token= | None | — |
| List clubs | GET | /api/clubs | None | ?page, ?size, ?sort |
| Get club | GET | /api/clubs/:id | None | — |
| Update club | PUT | /api/clubs/:id | CLUB_OWNER/ADMIN | full ClubDTO |
| Deactivate club | PATCH | /api/clubs/:id/deactivate | ADMIN | — |
| Delete club | DELETE | /api/clubs/:id | ADMIN | — |
| List public events | GET | /api/events | None | ?page, ?size |
| Get event | GET | /api/events/:id | None | — |
| Events by club | GET | /api/events/club/:clubId | None | ?page, ?size |
| Dashboard events | GET | /api/events/dashboard | OWNER/STAFF | ?status, ?startDate, ?endDate |
| Create event | POST | /api/events | OWNER/STAFF/ADMIN | name, startDateTime, endDateTime, clubId, capacity |
| Update event | PUT | /api/events/:id | OWNER/STAFF/ADMIN | same as create |
| Delete event | DELETE | /api/events/:id | OWNER/ADMIN | — |
| Transition status | PATCH | /api/events/:id/status | OWNER/STAFF/ADMIN | status |
| Check in | POST | /api/events/:id/attendance/check-in | OWNER/STAFF | gender |
| Check out | POST | /api/events/:id/attendance/check-out | OWNER/STAFF | gender |
| Audit log | GET | /api/events/:id/attendance/audit | OWNER/ADMIN | ?page, ?size |
| Add discount | POST | /api/events/:id/discounts | OWNER/STAFF | discountType, discountValue, description, validFrom, validUntil |
| Update discount | PUT | /api/events/:id/discounts/:dId | OWNER/STAFF | same |
| Delete discount | DELETE | /api/events/:id/discounts/:dId | OWNER/STAFF | — |
| Add entry type | POST | /api/events/:id/entry-types | OWNER/STAFF | name, price, description, availableQuantity |
| Update entry type | PUT | /api/events/:id/entry-types/:etId | OWNER/STAFF | same |
| Delete entry type | DELETE | /api/events/:id/entry-types/:etId | OWNER/STAFF | — |
| Upload image | POST | /api/events/:id/image/upload | OWNER/STAFF | multipart file field: "file" |
| Set image URL | PUT | /api/events/:id/image/url | OWNER/STAFF | url |
| Assign staff to event | POST | /api/events/:id/staff | OWNER | staffUserId |
| Remove staff from event | DELETE | /api/events/:id/staff/:userId | OWNER | — |
| List event staff | GET | /api/events/:id/staff | OWNER/STAFF | — |
| Create staff account | POST | /api/users/staff | OWNER | username, password, email, fullName, phoneNumber, role:"STAFF", clubId |
| Deactivate staff | PATCH | /api/users/staff/:id/deactivate | OWNER/ADMIN | — |
| Update own profile | PUT | /api/users/profile | OWNER/STAFF | fullName, phoneNumber |
| List club staff | GET | /api/users/staff | OWNER/STAFF | — |
