# conservation-service (Aryan Ayush)

Rescue and rehabilitation, vehicles and equipment (with allocation checks) and conservation
programmes for the WildGuard project.

- Spring Boot 4.1.1, Java 17, same as the platform app in the repo root
- runs on port **8084**
- uses the **same login** as the platform app: it reads the JWT token the platform gives at login
- keeps its tables in its own database schema called `conservation`

## What is in this delivery

| Path | New or changed | What it is |
|---|---|---|
| `conservation-service/` | replaces the old skeleton folder | the whole service |
| `src/main/resources/templates/rescues.html`, `resources.html`, `programmes.html` | new | the three pages |
| `src/main/resources/static/js/conservation-api.js`, `rescues.js`, `resources.js`, `programmes.js` | new | page scripts |
| `src/main/resources/static/css/conservation.css` | new | page styles |
| `src/main/java/com/example/Hackathon/controller/PageController.java` | changed | 3 page urls added |
| `src/main/java/com/example/Hackathon/config/SecurityConfig.java` | changed | the 3 page urls added to `permitAll` |
| `src/main/resources/static/js/shell.js` | changed | menu links and page titles |

The three changed files are based on commit `3ef62c4` ("Rebase Conservation-service").
If one of them was edited after that commit, do not overwrite it. Add these lines by hand instead:

**PageController.java**: add three methods like the existing ones:

```java
@GetMapping("/rescues")
public String rescues() { return "rescues"; }

@GetMapping("/resources")
public String resources() { return "resources"; }

@GetMapping("/programmes")
public String programmes() { return "programmes"; }
```

**SecurityConfig.java**: in the first `requestMatchers(...).permitAll()` list, after `"/risk-radar",` add:

```java
"/rescues",
"/resources",
"/programmes",
```

**shell.js**: in the `menus` object replace the "Rescue Cases ... upcoming" line of ADMIN and
RESCUE_OFFICER (and add to FOREST_OFFICER) with:

```js
{ label: "Rescue Cases", icon: "♡", href: "/rescues" },
{ label: "Resources", icon: "▣", href: "/resources" },
{ label: "Programmes", icon: "♧", href: "/programmes" },
```

and add the three page titles next to the `/risk-radar` one.

## How to put it in the repo

1. Delete the old `conservation-service` folder in the repo.
2. Unzip the delivery in the repo root (the folder that has `pom.xml` and `mvnw`). Allow overwrite.
3. Commit.

## How to run

Start the platform app as usual (port 8081). Then start this service with the **same database**:

```bash
cd conservation-service
export SPRING_DATASOURCE_URL='jdbc:postgresql://<host>/<database>?sslmode=require'
export SPRING_DATASOURCE_USERNAME='<user>'
export SPRING_DATASOURCE_PASSWORD='<password>'
../mvnw spring-boot:run
```

In Eclipse / STS / IntelliJ: import `conservation-service` as a Maven project, put the three
variables in the run configuration and run `ConservationApplication`.

Without the variables the service looks for a local PostgreSQL:
database `hackathon`, user `wildguard`, password `wildguard`.

On the first start the service creates the schema `conservation`, its 7 tables and some demo data
(4 rescue cases, 12 resources, 4 programmes). Nothing in the `public` schema is touched.

Check that it is up: http://localhost:8084/api/conservation/health

## How to try it

1. Log in on http://localhost:8081 with an **active** account that has the role ADMIN,
   FOREST_OFFICER or RESCUE_OFFICER (the admin sets this on the User Management page).
   Log out and in again after a role change, because the role is inside the login token.
2. The menu now shows **Rescue Cases**, **Resources** and **Programmes**.

| Role | Can do |
|---|---|
| RESCUE_OFFICER | open rescue cases, change stage, add treatment, request a release review, allocate and release resources |
| FOREST_OFFICER | open cases, change stage, **decide** release reviews, manage resources, override an allocation, manage programmes |
| ADMIN | everything |
| USER | nothing in this part (the service answers 403) |

Short demo:

1. As rescue officer: open **RES-031**, add a treatment, move it to Rehabilitation, request a release review.
2. As forest officer: open the same case, approve the review, move it to Released.
3. Resources: allocate **VH-05** to patrol `PAT-205`. Try to allocate it again: refused,
   "VH-05 is already allocated to PAT-205. Release it first." Release it.
4. Programmes: open **Water Source Restoration** (12 of 20 = 60%), record 2 more: 70%.

## REST API (all need `Authorization: Bearer <token>`)

| Method | Url | Roles |
|---|---|---|
| GET | `/api/rescues`, `/api/rescues/{id}` | all three staff roles |
| POST | `/api/rescues` | ADMIN, FOREST_OFFICER, RESCUE_OFFICER |
| PATCH | `/api/rescues/{id}/stage` | ADMIN, FOREST_OFFICER, RESCUE_OFFICER |
| POST | `/api/rescues/{id}/treatments` | ADMIN, RESCUE_OFFICER |
| POST | `/api/rescues/{id}/release-review` | ADMIN, RESCUE_OFFICER |
| POST | `/api/rescues/{id}/release-decision` | ADMIN, FOREST_OFFICER |
| GET | `/api/resources`, `/api/resources/{id}` | all three staff roles |
| POST / PUT | `/api/resources`, `/api/resources/{id}` | ADMIN, FOREST_OFFICER |
| PATCH | `/api/resources/{id}/status` | ADMIN, FOREST_OFFICER |
| GET | `/api/allocations` | all three staff roles |
| POST | `/api/allocations` | ADMIN, FOREST_OFFICER, RESCUE_OFFICER |
| PATCH | `/api/allocations/{id}/release` | ADMIN, FOREST_OFFICER, RESCUE_OFFICER |
| GET | `/api/programmes`, `/api/programmes/{id}` | all three staff roles |
| POST / PUT | `/api/programmes`, `/api/programmes/{id}` | ADMIN, FOREST_OFFICER |
| POST | `/api/programmes/{id}/activities` | ADMIN, FOREST_OFFICER |
| GET | `/api/conservation/summary` | all three staff roles |
| GET | `/api/conservation/health` | no login needed |

Errors always look like `{ "status": 409, "message": "..." }`.

## Notes for the team

- **Login secret.** `wildguard.jwt-secret` in `application.properties` must be the same text as
  the `secret` in the platform's `JwtService`. If that one changes, change this one too
  (or set the environment variable `WILDGUARD_JWT_SECRET`).
- **Pages on another address.** The pages call `http://<same host>:8084`. If the platform is not
  opened on `localhost:8081`, add its address to `wildguard.allowed-origins`.
- **Links to operations-service and wildlife-service.** A rescue case can carry an incident id
  and an animal id, and a resource can be given to a patrol id. Those services are not built yet,
  so for now the ids are saved as typed. When they are ready (`GET /api/incidents/{id}`,
  `GET /api/patrols/{id}`, `GET /api/animals/{id}`), set `wildguard.strict-links=true` and unknown
  ids are refused. When a rescue case is opened for an incident this service also tries
  `PATCH /api/incidents/{id}/status` with `RESCUE_IN_PROGRESS`; nothing breaks if that url is missing.
- **Names.** The login token only has the email, so history rows show the email of the user.
- **No incident report here.** The Reports page of the platform already does the incident report
  and CSV export, so it is not repeated in this service.
