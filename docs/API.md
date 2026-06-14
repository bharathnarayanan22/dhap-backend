# DHAP — API Reference (Postman)

Base URL: `http://localhost:8080`
Start the app: `./gradlew bootRun`

**Auth:** every endpoint except the 4 public ones requires the header
`Authorization: Bearer <token>` (token from register/login).

**Public (no token):** `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh`, `GET /files/**`

**Pagination:** all list endpoints take `?page=0&size=20` and return Spring's `Page` wrapper:
`{ "content": [...], "totalElements": n, "totalPages": n, "number": 0, "size": 20, ... }`

---

## 1. Auth

### POST /auth/register  (public)
```json
{
  "name": "Bharath",
  "email": "bharath@example.com",
  "password": "secret123",
  "mobile": "9999999999",
  "addressLine": "12 Beach Road",
  "city": "Chennai",
  "country": "India",
  "pincode": "600001",
  "role": "VOLUNTEER"
}
```
`role`: `VOLUNTEER` | `DONOR` | `COORDINATOR`

→ `201`
```json
{
  "id": "8d3f…",
  "email": "bharath@example.com",
  "role": "VOLUNTEER",
  "token": "eyJ…",
  "refreshToken": "eyJ…"
}
```
Errors: `409` duplicate email.

### POST /auth/login  (public)
```json
{ "email": "bharath@example.com", "password": "secret123" }
```
→ `200` same body as register. Errors: `401` wrong email or password.

### POST /auth/refresh  (public)
```json
{ "refreshToken": "eyJ…" }
```
→ `200` new rotated token pair (same shape as login). Errors: `401` invalid/expired refresh token.

### GET /auth/me
No body. → `200`
```json
{
  "id": "8d3f…", "name": "Bharath", "email": "bharath@example.com",
  "mobile": "9999999999", "addressLine": "12 Beach Road", "city": "Chennai",
  "country": "India", "pincode": "600001", "role": "VOLUNTEER", "isSubmitted": false
}
```

### POST /auth/logout
No body. → `200` `"Logged out successfully"` (stateless — client discards the token).

---

## 2. Users

### GET /users?page=0&size=20
→ `200` Page of:
```json
{
  "id": "8d3f…", "name": "Bharath", "email": "…", "mobile": "…",
  "addressLine": "…", "city": "…", "country": "…", "pincode": "…",
  "role": "VOLUNTEER", "inTask": false, "isCoordinator": false, "isSubmitted": false
}
```

### GET /users/{id}
→ `200` one `UserResponse` (above). `404` unknown id.

### PATCH /users/{id} — partial profile update (all fields optional)
```json
{
  "name": "Bharath P",
  "mobile": "8888888888",
  "addressLine": "14 Hill Street",
  "city": "Madurai",
  "country": "India",
  "pincode": "625001"
}
```
→ `200` updated `UserResponse`. Only non-null fields are applied.

### PATCH /users/{id}/role
```json
{ "role": "DONOR" }
```
`role`: `DONOR` | `VOLUNTEER` only (COORDINATOR comes via application acceptance).
→ `200` updated `UserResponse`.

### PATCH /users/{id}/submission-status
```json
{ "isSubmitted": true }
```
→ `200` updated `UserResponse`.

---

## 3. Tasks

`status` values: `PENDING` | `IN_PROGRESS` | `IN_VERIFICATION` | `COMPLETED`

### GET /tasks?page=0&size=20[&status=PENDING][&volunteerId={userId}]
| Use case | Call |
|---|---|
| All tasks (coordinator) | `GET /tasks` |
| Available to accept (volunteer) | `GET /tasks?status=PENDING` |
| Awaiting review (coordinator) | `GET /tasks?status=IN_VERIFICATION` |
| My accepted tasks (volunteer) | `GET /tasks?volunteerId={myUserId}` |

→ `200` Page of `TaskResponse` (see POST below).

### POST /tasks — coordinator creates a task
```json
{
  "title": "Deliver food packets",
  "description": "Pick up 50 packets from the depot and deliver to the shelter",
  "volunteer": 3,
  "startAddress": "Depot, 1 Market Road, Chennai",
  "endAddress": "Shelter, 9 Lake View, Chennai",
  "startLocation": { "latitude": 13.0827, "longitude": 80.2707 },
  "endLocation":   { "latitude": 13.0500, "longitude": 80.2121 }
}
```
Required: `title`, `volunteer ≥ 1`. → `201`
```json
{
  "id": "a1b2…", "title": "Deliver food packets", "description": "…",
  "volunteer": 3, "volunteersAccepted": 0,
  "startAddress": "…", "endAddress": "…",
  "startLocation": { "latitude": 13.0827, "longitude": 80.2707 },
  "endLocation":   { "latitude": 13.05,   "longitude": 80.2121 },
  "status": "PENDING", "assignedUserIds": [], "proofs": []
}
```

### POST /tasks/{id}/accept — volunteer accepts
No body (user resolved from JWT). → `200` updated `TaskResponse`
(`volunteersAccepted` +1, your id in `assignedUserIds`, status → `IN_PROGRESS`).
Errors: `409` task full · `409` already accepted by you · `404` unknown task.

### PATCH /tasks/{id} — proof submission / status change
Volunteer submits proof:
```json
{
  "status": "IN_VERIFICATION",
  "proofs": [
    {
      "message": "Delivered all 50 packets",
      "mediaPaths": ["http://localhost:8080/files/2026/06/ab12_proof.jpg"]
    }
  ]
}
```
Coordinator approves completion (clears `inTask` for all assigned volunteers):
```json
{ "status": "COMPLETED" }
```
→ `200` updated `TaskResponse`.

### DELETE /tasks/{id}
No body. → `204`. (Only safe while status is `PENDING` — doesn't clear `inTask`.)

---

## 4. Files (proof upload)

### POST /files/upload
Body → **form-data**, key `file` (type **File**), pick an image/video.
Limits: ≤ 10MB, content type must be `image/*` or `video/*` (no SVG).
→ `201`
```json
{ "url": "http://localhost:8080/files/2026/06/ab12cd34_photo.jpg" }
```
Errors: `400` empty file · `415` wrong type.
Use the returned `url` inside `proofs[].mediaPaths` on `PATCH /tasks/{id}`.

### GET /files/{year}/{month}/{filename}  (public)
Open the returned URL directly in the browser — no token needed.

---

## 5. Resources (donations)

`resourceType` values: `FOOD` | `MEDICAL` | `CLOTHING` | `SHELTER` | `WATER` | `OTHER`

### GET /resources?page=0&size=20[&donorId={userId}]
No `donorId` → all donations. With `donorId` → that donor's "My Contributions".
→ `200` Page of `ResourceResponse` (see POST below).

### POST /resources — donor donates (identity from JWT)
```json
{
  "resource": "Rice bags",
  "quantity": 25,
  "address": "Warehouse 4, Guindy, Chennai",
  "location": { "latitude": 13.0067, "longitude": 80.2206 },
  "resourceType": "FOOD"
}
```
Required: `resource`, `quantity ≥ 1`. → `201`
```json
{
  "id": "c3d4…", "resource": "Rice bags", "quantity": 25,
  "address": "…", "location": { "latitude": 13.0067, "longitude": 80.2206 },
  "donorId": "8d3f…", "donorName": "Bharath", "resourceType": "FOOD"
}
```

### DELETE /resources/{id}
No body. → `204`. `404` unknown id.

---

## 6. Requests (coordinator's published needs)

### GET /requests?page=0&size=20
→ `200` Page of `RequestResponse` (see POST below).

### POST /requests — coordinator publishes a need
```json
{
  "resource": "Blankets",
  "quantity": 100,
  "description": "Needed for the flood relief camp",
  "address": "Relief Camp, Velachery, Chennai",
  "location": { "latitude": 12.9815, "longitude": 80.2180 }
}
```
Required: `resource`, `quantity ≥ 1`. → `201`
```json
{
  "id": "e5f6…", "resource": "Blankets", "quantity": 100,
  "description": "…", "address": "…",
  "location": { "latitude": 12.9815, "longitude": 80.218 },
  "status": "PENDING", "quantityPledged": 0
}
```
`status` flips to `ACCEPTED` automatically when pledges fill the quantity.

### GET /requests/{requestId}/responses?page=0&size=20
All pledges for one request. → `200` Page of `ResponseResponse` (see §7).

---

## 7. Responses (donor pledges)

### GET /responses?page=0&size=20
→ `200` Page of `ResponseResponse` (see POST below).

### POST /responses — donor pledges against a request (identity from JWT)
```json
{
  "requestId": "e5f6…",
  "message": "I can provide 40 blankets",
  "quantityProvided": 40,
  "address": "23 North Street, Chennai",
  "location": { "latitude": 13.0418, "longitude": 80.2341 }
}
```
Required: `requestId`, `quantityProvided ≥ 1`. → `201`
```json
{
  "id": "g7h8…", "requestId": "e5f6…",
  "responderId": "8d3f…", "responderName": "Bharath",
  "message": "I can provide 40 blankets", "quantityProvided": 40,
  "address": "…", "location": { "latitude": 13.0418, "longitude": 80.2341 },
  "taskAssigned": false
}
```
Side effect: parent request's `quantityPledged` += 40; auto-`ACCEPTED` when filled.
Errors: `404` unknown requestId.

### PATCH /responses/{id}/assign-task
No body. Marks the pledge as converted to a delivery task. → `200` with `"taskAssigned": true`.

---

## 8. Applications (become a coordinator)

`status` values: `PENDING` | `ACCEPTED` | `REJECTED`

### GET /applications?page=0&size=20[&status=PENDING]
→ `200` Page of `ApplicationResponse` (see POST below).

### POST /applications — apply (identity from JWT)
```json
{ "message": "I have 5 years of NGO field experience" }
```
→ `201`
```json
{
  "id": "i9j0…", "userId": "8d3f…", "email": "bharath@example.com",
  "message": "…", "status": "PENDING", "submittedAt": "2026-06-06T18:30:00"
}
```
Side effect: your `isSubmitted` → `true`.
Errors: `409` already submitted · `409` already a coordinator.

### PATCH /applications/{id}/accept
No body. → `200` application with `"status": "ACCEPTED"`.
Side effect: applicant's `role` → `COORDINATOR`.

### PATCH /applications/{id}/reject
No body. → `200` application with `"status": "REJECTED"`.
Side effect: applicant's `isSubmitted` → `false` (can re-apply).

---

## Common error responses

| Code | Meaning |
|---|---|
| 400 | Validation failed (missing `@NotBlank` field, `@Min` violated, empty file) |
| 401 | Missing/invalid/expired token; wrong credentials; bad refresh token |
| 403 | Token present but rejected by the filter chain |
| 404 | Unknown id (task / user / request / response / resource / application) |
| 409 | Duplicate email · task full · task already accepted · application already submitted |
| 415 | Upload that isn't image/video |

---

## Suggested end-to-end test flow

1. `POST /auth/register` ×3 — a **COORDINATOR**, a **VOLUNTEER**, a **DONOR** (save each `token`)
2. Coordinator: `POST /tasks` → save task `id`
3. Volunteer: `GET /tasks?status=PENDING` → `POST /tasks/{id}/accept` (accept twice → expect `409`)
4. Volunteer: `POST /files/upload` (form-data) → save `url` → `PATCH /tasks/{id}` with `IN_VERIFICATION` + proof
5. Open the proof `url` in a browser with no token → image loads (public GET)
6. Coordinator: `GET /tasks?status=IN_VERIFICATION` → `PATCH /tasks/{id}` `{"status": "COMPLETED"}` → `GET /users/{volunteerId}` shows `inTask: false`
7. Coordinator: `POST /requests` (need 100) → Donor: `POST /responses` pledging 100 → `GET /requests` shows `status: "ACCEPTED"`, `quantityPledged: 100`
8. Donor: `POST /resources` → `GET /resources?donorId={donorId}` shows it
9. Volunteer: `POST /applications` (re-apply → expect `409`) → Coordinator: `PATCH /applications/{id}/accept` → `GET /auth/me` as that user shows `role: "COORDINATOR"`
10. `POST /auth/refresh` with the saved `refreshToken` → new token pair works on `GET /auth/me`

> ⚠️ No role enforcement yet — technically *any* logged-in user can call the coordinator
> endpoints. The "Coordinator:" / "Donor:" labels above describe the intended flow.