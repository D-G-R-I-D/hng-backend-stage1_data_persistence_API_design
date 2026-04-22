# HNG Backend Stage 1 - Data Persistence API



# Profile Enrichment API

A Spring Boot REST API that accepts a name, enriches it by calling Genderize, Agify, and Nationalize in parallel, stores the result, and exposes endpoints for filtering, pagination, and natural language search.

---

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/profiles` | Create a profile by name |
| GET | `/api/profiles/{id}` | Get a single profile by UUID |
| GET | `/api/profiles` | Get all profiles with optional filters |
| GET | `/api/profiles/search?q=` | Natural language search |
| DELETE | `/api/profiles/{id}` | Delete a profile |

### Filter Parameters (GET /api/profiles)

| Parameter | Type | Example |
|-----------|------|---------|
| `gender` | string | `male`, `female` |
| `age_group` | string | `child`, `teenager`, `adult`, `senior` |
| `country_id` | string | `NG`, `US`, `GB` |
| `min_age` | integer | `20` |
| `max_age` | integer | `40` |
| `min_gender_probability` | double | `0.8` |
| `min_country_probability` | double | `0.5` |
| `sort_by` | string | `age`, `created_at`, `gender_probability` |
| `order` | string | `asc`, `desc` |
| `page` | integer | `1` |
| `limit` | integer | `10` (max 50) |

---

## Natural Language Parsing

The `/api/profiles/search?q=` endpoint accepts plain English queries and converts them into structured filters before hitting the database. No external AI or NLP library is used — parsing is done entirely with regex patterns and keyword matching in `NaturalLanguageParser.java`.

### How It Works

The parser runs five detection steps in sequence on the lowercased, filler-stripped query:

**Step 1 — Filler word removal**

Common words that carry no filter meaning are stripped before any detection runs:

```
i want, i need, give me, show me, find, get, fetch, list,
display, all the, all, the, a, an, of, for, in, that, with, and, or
```

So `"give me all the female adults from nigeria"` becomes `"female adults nigeria"` before parsing.

**Step 2 — Gender detection**

The parser scans for keyword presence:

| Keywords detected | Maps to |
|-------------------|---------|
| `male`, `men`, `man`, `boy`, `boys` | `gender = "male"` |
| `female`, `women`, `woman`, `girl`, `girls` | `gender = "female"` |
| Both sets present in same query | No gender filter (treated as both) |

**Step 3 — Age range detection (regex)**

Four patterns are checked in order:

| Pattern | Example query | Result |
|---------|---------------|--------|
| `above/over/older than/greater than N` | `"above 30"` | `minAge = 30` |
| `below/under/younger than/less than N` | `"under 18"` | `maxAge = 17` |
| `between N and M` / `aged N to M` | `"between 20 and 40"` | `minAge = 20, maxAge = 40` |
| `young/youth/youngster/younger` | `"young women"` | `minAge = 16, maxAge = 24` |

**Step 4 — Age group detection (keyword)**

If a named group is found, it sets both the `ageGroup` filter and default age bounds (which are overridden if the query also contains explicit numbers):

| Keywords | Maps to | Default age range |
|----------|---------|-------------------|
| `child`, `children` | `ageGroup = "child"` | 0 – 12 |
| `teenager`, `teen`, `adolescent` | `ageGroup = "teenager"` | 13 – 19 |
| `adult`, `adults`, `grown`, `grown-up` | `ageGroup = "adult"` | 20 – 59 |
| `senior`, `seniors`, `elderly`, `old`, `aged` | `ageGroup = "senior"` | 60+ |

**Step 5 — Country detection**

Two passes are made:

1. A direct substring scan against a 90+ entry map of country names to ISO codes (`"nigeria"` → `"NG"`, `"united kingdom"` → `"GB"`, etc.)
2. A regex scan for the pattern `from <place>` which extracts the place name and looks it up in the same map

The first match wins and sets `countryId`.

### Example Queries

| Query | Parsed Filters |
|-------|----------------|
| `show me female adults from nigeria` | `gender=female, ageGroup=adult, countryId=NG` |
| `male teenagers` | `gender=male, ageGroup=teenager` |
| `seniors above 65 from the uk` | `ageGroup=senior, minAge=65, countryId=GB` |
| `young men from ghana` | `gender=male, minAge=16, maxAge=24, countryId=GH` |
| `women between 25 and 35` | `gender=female, minAge=25, maxAge=35` |
| `children from india` | `ageGroup=child, minAge=0, maxAge=12, countryId=IN` |

If no recognisable filters are found in the query, the endpoint returns a 200 with `"Unable to interpret query"` rather than returning all records.

---

## Limitations

### Parser limitations

**1. No semantic understanding**
The parser works on exact keyword and regex matches only. Paraphrased or unusual phrasing will not be recognised. `"grown ups"` works, `"working age people"` does not. `"not too old"` returns nothing.

**2. Conflicting ranges are not resolved**
If a query contains both `"adults"` and `"above 70"`, both rules fire and the stricter bounds win by assignment order, not by logic. The result may be an empty result set with no warning to the user.

**3. "Young" is hardcoded to 16–24**
The word `"young"` maps to a fixed age range. Context like `"young children"` would incorrectly set minAge to 16 rather than deferring to the child group range.

**4. Only the first country match is used**
The country scan stops at the first match found. Queries like `"profiles from nigeria or ghana"` will only filter by Nigeria.

**5. Multi-word country names can be partially matched**
The substring scan means `"guinea"` could match before `"guinea bissau"` or `"equatorial guinea"` depending on map iteration order, since `HashMap` does not guarantee order.

**6. No negation support**
Queries like `"not from nigeria"` or `"everyone except seniors"` are not handled. The negation word is stripped as a filler and the remaining keyword fires as a positive filter.

**7. Gender ambiguity not flagged**
If both male and female keywords appear, the parser silently applies no gender filter. The user receives no feedback that their query was treated as gender-neutral.

**8. Probability filters not parseable**
The natural language endpoint cannot set `minGenderProbability` or `minCountryProbability` — those are only available through the structured query parameters on `GET /api/profiles`.

**9. No spelling correction**
`"nigerria"`, `"femal"`, `"adutls"` will all produce no results. There is no fuzzy matching or suggestion.

### API limitations

**1. Country name resolution is static**
The country name displayed (e.g. "Nigeria") comes from a hardcoded map in the service layer. If Nationalize returns a country code not in that map, the code itself is stored as the name.

**2. Batch filter query does not support partial filters cleanly**
The JPA method used for `GET /api/profiles` requires all filter fields to be passed. Null filters are substituted with wildcard defaults (`"%"` for strings, `0`/`120` for ages), which works but means the SQL always includes all WHERE clauses rather than omitting unused ones.

**3. No update endpoint**
There is no `PATCH /api/profiles/{id}`. To update a profile's data you must delete and re-create it.

**4. External API failures return defaults silently**
If Genderize, Agify, or Nationalize return an error or empty response, the profile is still saved with `"unknown"` gender, `0` age, and `"unknown"` country rather than surfacing the failure to the caller.







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