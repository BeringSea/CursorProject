# Payment Microservices

A pair of Spring Boot 3 / Java 21 microservices demonstrating a simple payment domain:

| Service | Port | Description |
|---|---|---|
| **cards-service** | `8081` | Manages credit/debit cards |
| **person-service** | `8082` | Manages persons; fetches their cards via Feign |

---

## Architecture

```
┌─────────────────────┐          Feign Client          ┌─────────────────────┐
│   person-service    │  ──── GET /api/cards/person ──►│   cards-service     │
│     port 8082       │◄────────────────────────────── │     port 8081       │
│                     │        List<CardDto>            │                     │
│  H2 in-memory DB    │                                 │  H2 in-memory DB    │
└─────────────────────┘                                 └─────────────────────┘
```

Each service:
- Uses **H2** in-memory database (console available at `/h2-console`)
- Exposes a full **CRUD REST API**
- Has a **service interface** (`ICardService` / `IPersonService`) with a concrete implementation
- Has **global exception handling** via `@RestControllerAdvice`
- Has **JUnit 5 integration tests** using `MockMvc`

---

## Prerequisites

- Java 21+
- Maven 3.9+

---

## Running the Services

Open **two separate terminals**.

### 1. Cards Service (port 8081)

```bash
cd cards-service
./mvnw spring-boot:run
```

### 2. Person Service (port 8082)

```bash
cd person-service
./mvnw spring-boot:run
```

> **Order matters**: start `cards-service` first so the Feign client can reach it.  
> If `cards-service` is down, the Feign fallback returns an empty card list — the person endpoints remain functional.

---

## Running Tests

```bash
# Cards service
cd cards-service
./mvnw test

# Person service
cd person-service
./mvnw test
```

---

## H2 Console

| Service | URL | JDBC URL |
|---|---|---|
| cards-service | http://localhost:8081/h2-console | `jdbc:h2:mem:cardsdb` |
| person-service | http://localhost:8082/h2-console | `jdbc:h2:mem:persondb` |

Username: `sa` · Password: *(empty)*

---

## Cards Service API — `http://localhost:8081`

### Card Fields

| Field | Type | Description |
|---|---|---|
| `id` | Long | Auto-generated primary key |
| `cardNumber` | String | Unique card number |
| `cardType` | Enum | `CREDIT` or `DEBIT` |
| `creditLimit` | Double | Total credit limit |
| `amountUsed` | Double | Amount already spent |
| `availableAmount` | Double | Computed: `creditLimit - amountUsed` |
| `personId` | Long | Owner's ID (from person-service) |

### Endpoints

#### Create Card
```
POST /api/cards
Content-Type: application/json

{
  "cardNumber": "4111-1111-1111-1111",
  "cardType": "CREDIT",
  "creditLimit": 5000.0,
  "amountUsed": 1000.0,
  "personId": 1
}
```
**Response** `201 Created`

---

#### Get Card by ID
```
GET /api/cards/{id}
```
**Response** `200 OK`

---

#### Get Card by Card Number
```
GET /api/cards/number/{cardNumber}
```
**Response** `200 OK`

---

#### Get All Cards for a Person
```
GET /api/cards/person/{personId}
```
**Response** `200 OK` — array of cards

---

#### Get All Cards
```
GET /api/cards
```
**Response** `200 OK` — array of all cards

---

#### Update Card
```
PUT /api/cards/{id}
Content-Type: application/json

{
  "cardNumber": "4111-1111-1111-1111",
  "cardType": "CREDIT",
  "creditLimit": 5000.0,
  "amountUsed": 2500.0,
  "personId": 1
}
```
**Response** `200 OK`

---

#### Delete Card
```
DELETE /api/cards/{id}
```
**Response** `204 No Content`

---

### Error Responses

| Status | Scenario |
|---|---|
| `400 Bad Request` | Validation failed (missing / invalid fields) |
| `404 Not Found` | Card not found by id or card number |
| `409 Conflict` | Card number already exists |
| `500 Internal Server Error` | Unexpected error |

Error body example:
```json
{
  "apiPath": "/api/cards/99",
  "errorCode": 404,
  "errorMessage": "Card with id '99' not found",
  "errorTime": "2026-06-11T10:00:00"
}
```

---

## Person Service API — `http://localhost:8082`

### Person Fields

| Field | Type | Description |
|---|---|---|
| `id` | Long | Auto-generated primary key |
| `firstName` | String | First name |
| `lastName` | String | Last name |
| `email` | String | Unique email address |
| `mobileNumber` | String | Unique mobile number (10–15 digits) |
| `dateOfBirth` | LocalDate | Date of birth (`YYYY-MM-DD`) |
| `gender` | Enum | `MALE`, `FEMALE`, or `OTHER` |
| `address` | String | Postal address |
| `cards` | List | Cards fetched from cards-service via Feign |

### Endpoints

#### Create Person
```
POST /api/persons
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "mobileNumber": "+1234567890",
  "dateOfBirth": "1990-05-15",
  "gender": "MALE",
  "address": "123 Main St, Springfield"
}
```
**Response** `201 Created`

---

#### Get Person by ID *(includes cards)*
```
GET /api/persons/{id}
```
**Response** `200 OK` — person with embedded `cards` array

---

#### Get Person by Email *(includes cards)*
```
GET /api/persons/email/{email}
```
**Response** `200 OK`

---

#### Get All Persons *(each includes their cards)*
```
GET /api/persons
```
**Response** `200 OK` — array of persons

---

#### Update Person
```
PUT /api/persons/{id}
Content-Type: application/json

{
  "firstName": "Jonathan",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "mobileNumber": "+1234567890",
  "dateOfBirth": "1990-05-15",
  "gender": "MALE",
  "address": "999 New St, Chicago"
}
```
**Response** `200 OK`

---

#### Delete Person
```
DELETE /api/persons/{id}
```
**Response** `204 No Content`

---

### Error Responses

| Status | Scenario |
|---|---|
| `400 Bad Request` | Validation failed |
| `404 Not Found` | Person not found |
| `409 Conflict` | Email or mobile number already exists |
| `500 Internal Server Error` | Unexpected error |

---

## Inter-Service Communication (Feign)

`person-service` uses Spring Cloud OpenFeign to call `cards-service`:

```
GET http://localhost:8081/api/cards/person/{personId}
```

The Feign client is declared in `CardsClient.java`:

```java
@FeignClient(name = "cards-service", url = "${cards-service.url}", fallback = CardsClientFallback.class)
public interface CardsClient {
    @GetMapping("/api/cards/person/{personId}")
    List<CardDto> getCardsByPersonId(@PathVariable Long personId);
}
```

If `cards-service` is unavailable the `CardsClientFallback` returns an empty list, so `person-service` degrades gracefully.

---

## Project Structure

```
SampleProject/
├── cards-service/
│   ├── src/main/java/com/example/cards/
│   │   ├── CardsApplication.java
│   │   ├── controller/CardsController.java
│   │   ├── dto/
│   │   │   ├── CardRequestDto.java
│   │   │   ├── CardResponseDto.java
│   │   │   └── ErrorResponseDto.java
│   │   ├── entity/Card.java
│   │   ├── exception/
│   │   │   ├── CardAlreadyExistsException.java
│   │   │   ├── CardNotFoundException.java
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── repository/CardRepository.java
│   │   └── service/
│   │       ├── ICardService.java
│   │       └── CardServiceImpl.java
│   └── src/test/java/com/example/cards/
│       └── CardsControllerTest.java
│
└── person-service/
    ├── src/main/java/com/example/person/
    │   ├── PersonApplication.java
    │   ├── client/
    │   │   ├── CardsClient.java
    │   │   └── CardsClientFallback.java
    │   ├── controller/PersonController.java
    │   ├── dto/
    │   │   ├── CardDto.java
    │   │   ├── ErrorResponseDto.java
    │   │   ├── PersonRequestDto.java
    │   │   └── PersonResponseDto.java
    │   ├── entity/Person.java
    │   ├── exception/
    │   │   ├── GlobalExceptionHandler.java
    │   │   ├── PersonAlreadyExistsException.java
    │   │   └── PersonNotFoundException.java
    │   ├── repository/PersonRepository.java
    │   └── service/
    │       ├── IPersonService.java
    │       └── PersonServiceImpl.java
    └── src/test/java/com/example/person/
        └── PersonControllerTest.java
```

---

## Quick Smoke Test (curl)

```bash
# 1. Create a person
curl -s -X POST http://localhost:8082/api/persons \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"john@example.com","mobileNumber":"+1234567890","dateOfBirth":"1990-05-15","gender":"MALE","address":"123 Main St"}' | jq .

# 2. Create a card for person id=1
curl -s -X POST http://localhost:8081/api/cards \
  -H "Content-Type: application/json" \
  -d '{"cardNumber":"4111-1111-1111-1111","cardType":"CREDIT","creditLimit":5000,"amountUsed":1000,"personId":1}' | jq .

# 3. Get person with embedded cards
curl -s http://localhost:8082/api/persons/1 | jq .
```
