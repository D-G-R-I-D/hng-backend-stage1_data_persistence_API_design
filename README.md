# HNG Backend Stage 1 - Data Persistence API

REST API for profile management using Spring Boot, PostgreSQL, and external APIs (Genderize, Agify, Nationalize).

## Endpoints

- `POST /api/profiles` — Create or retrieve a profile by name
- `GET /api/profiles` — List all profiles (supports filtering)
- `GET /api/profiles/{id}` — Get a single profile by ID
- `DELETE /api/profiles/{id}` — Delete a profile

## Filters

- `?gender=male|female`
- `?country_id=NG`
- `?age_group=child|teenager|adult|senior`

## Tech Stack

- Java 21, Spring Boot 3.5
- PostgreSQL
- Genderize.io, Agify.io, Nationalize.io