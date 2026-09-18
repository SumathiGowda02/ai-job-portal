# AI-Powered Job Portal

A full-stack job portal where the standout feature is **AI-based resume-to-job-description
skill matching**: when a candidate applies, an LLM reads the resume and the job description
and returns a 0–100 match score plus a short rationale — instead of a plain keyword search.

## Demo Video

[Watch the walkthrough](your-video-link-here) — covers job posting, search, resume upload,
and the AI matching flow end-to-end.

## Why I Built It This Way

**Why Spring Boot instead of the MERN stack I already knew** — I'd built full-stack projects
in MERN during my internship, and it would've been the easier, faster path here. I deliberately
picked Spring Boot and Java instead for two reasons: I wanted to prove to myself (and to anyone
reviewing this) that I could pick up a new backend ecosystem independently rather than sticking
to what's comfortable, and Java/Spring Boot shows up far more often in the fresher SDE roles I'm
targeting at product companies. This project is as much a "can I learn X on my own" exercise as
it is a portfolio piece.

**Why AI resume-matching instead of a plain CRUD job board** — a straightforward "post a job,
apply to a job" board is one of the most common portfolio projects out there, and I wanted
something that actually stood out rather than looking like every other fresher's third project.
Adding LLM-based resume-to-job matching gave me a real reason to get hands-on with AI
integration — designing a prompt that returns structured, parseable output, handling the case
where the AI call fails or is unavailable (a keyword-overlap fallback kicks in automatically),
and thinking about cost/latency trade-offs between a hosted API and a local model. It turned a
routine project into one with an actual technical decision behind every layer.

**Other decisions worth calling out:**
- **Redis caching on job search** — added to reduce repeated database hits on a frequently-hit
  read endpoint. Getting this working correctly with Spring Data's `Page<T>` turned into a
  genuinely tricky debugging exercise (Jackson doesn't serialize Spring Data's internal
  `Page`/`Sort` types cleanly through Redis), which I solved by caching a plain, framework-free
  `PagedResult<T>` DTO instead of the raw `Page` object.
- **JWT auth with role-based access** (`@PreAuthorize`) — chose stateless auth over sessions
  since the frontend and backend are fully decoupled, and role separation (recruiter vs
  candidate) needed to be enforced at the API level, not just hidden in the UI.
- **Pluggable resume storage** — resumes save to local disk by default (`storage.provider=local`)
  so the whole app runs end-to-end with zero AWS setup during development; setting
  `storage.provider=s3` switches to real S3 storage for production with no code changes.
- **Docker Compose for local dev** — kept MySQL, Redis, and the app itself containerized so the
  whole stack spins up consistently on any machine, without "works on my machine" surprises.

## Tech Stack

| Layer      | Tech |
|------------|------|
| Backend    | Java 17, Spring Boot 3, Spring Security (JWT), Spring Data JPA, Spring Cache |
| Frontend   | React 18, React Router, Axios |
| Database   | MySQL 8 |
| Cache      | Redis 7 (job search results) |
| Storage    | Local disk (dev) or AWS S3 (production) — toggled via `STORAGE_PROVIDER` |
| AI         | LLM API — Anthropic by default, or a local Ollama model for cost-free development |
| Infra      | Docker, docker-compose, GitHub Actions, AWS EC2 + RDS |

## Folder Structure

```
ai-job-portal/
├── backend/                      Spring Boot app
│   ├── src/main/java/com/jobportal/
│   │   ├── config/                SecurityConfig, RedisConfig, OpenApiConfig
│   │   ├── controller/             AuthController, JobController, ResumeController, ApplicationController
│   │   ├── model/                  User, Job, Resume, Application, Role
│   │   ├── repository/             Spring Data JPA repositories
│   │   ├── security/                JwtUtil, JwtAuthFilter, JwtAuthEntryPoint
│   │   ├── service/                 AuthService, JobService, ResumeService, ApplicationService, AiMatchService, LocalStorageService, S3Service
│   │   ├── dto/                     Request/response DTOs, PagedResult (cache-safe paging)
│   │   └── exception/               Custom exceptions + global handler
│   ├── src/main/resources/application.yml
│   ├── src/test/java/...            Context-load test
│   ├── pom.xml
│   └── Dockerfile
├── frontend/                     React app
│   ├── src/
│   │   ├── pages/                   Login, Register, JobList, JobDetail, PostJob, Resumes, Dashboard, Home
│   │   ├── components/              Navbar, ProtectedRoute, MatchScoreBadge
│   │   ├── context/AuthContext.js
│   │   └── services/api.js
│   ├── package.json
│   ├── nginx.conf
│   └── Dockerfile
├── .github/workflows/deploy.yml  CI/CD: build+test → build frontend → SSH deploy to EC2
├── docker-compose.yml            app + MySQL + Redis
├── .gitignore
├── .env.example
└── README.md
```



## Core Features

- **JWT auth**, two roles: `RECRUITER` and `CANDIDATE`, enforced with `@PreAuthorize` on every protected endpoint.
- **Job posting CRUD** with server-side search (keyword, location, job type) — search results cached in Redis for 10 minutes, cache evicted on any create/update/delete.
- **Resume upload** — saved locally by default, or to S3 in production (`STORAGE_PROVIDER` env var); text is extracted server-side with Apache PDFBox and stored for AI matching.
- **AI skill-matching** — `AiMatchService` sends the resume text + job description + required skills to an LLM and parses back a JSON `{score, summary, matchedSkills, missingSkills}`. If no `AI_API_KEY` is configured it transparently falls back to a keyword-overlap score, so the app still runs end-to-end without an API key.
- **Recruiter dashboard** — applicants for a job, ranked by AI match score, with shortlist/reject/hire actions.
- **Candidate dashboard** — all applications with their match scores and status.

## API Overview

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/auth/register` | Public |
| POST | `/api/auth/login` | Public |
| GET | `/api/jobs` | Public (search: `keyword`, `location`, `jobType`) |
| GET | `/api/jobs/{id}` | Public |
| POST/PUT/DELETE | `/api/jobs`, `/api/jobs/{id}` | RECRUITER |
| GET | `/api/jobs/my` | RECRUITER |
| POST | `/api/resumes/upload` | CANDIDATE |
| GET | `/api/resumes/my` | CANDIDATE |
| POST | `/api/applications?jobId=&resumeId=` | CANDIDATE |
| GET | `/api/applications/my` | CANDIDATE |
| GET | `/api/applications/job/{jobId}` | RECRUITER |
| PATCH | `/api/applications/{id}/status?status=` | RECRUITER |

Full interactive docs at `/swagger-ui.html` once the backend is running.

## Running Locally with Docker

```bash
git clone <your-repo-url>
cd ai-job-portal
cp .env.example .env        # fill in JWT_SECRET and AI_API_KEY
docker compose up -d --build
```

- Frontend → http://localhost:3000
- Backend API → http://localhost:8080/api
- Swagger → http://localhost:8080/swagger-ui.html

With `STORAGE_PROVIDER=local` (the default), resume upload works immediately with no AWS setup.
Set `AI_API_KEY` for real AI matching, or leave it blank to use the keyword-overlap fallback —
both are fully functional. Set `STORAGE_PROVIDER=s3` plus real AWS credentials to use S3 instead.

## Running Without Docker (dev mode)

Backend:
```bash
cd backend
# have local MySQL + Redis running (e.g. `docker compose up -d mysql redis`),
# or point DB_HOST/REDIS_HOST at remote instances
mvn spring-boot:run
```

Frontend:
```bash
cd frontend
cp .env.example .env
npm install
npm start
```

## Deployment (AWS EC2 + RDS)

1. Create an **RDS MySQL** instance; note its endpoint, username, password.
2. Launch an **EC2** instance (Ubuntu, t2.micro/t3.small), install Docker + Docker Compose, open ports 22/80/8080.
3. Create an **S3 bucket** for resumes and an IAM user scoped to it.
4. In your GitHub repo settings → **Secrets and variables → Actions**, add:
   - `EC2_HOST` — public IP or DNS of the EC2 instance
   - `EC2_USER` — SSH user (e.g. `ubuntu`)
   - `EC2_SSH_KEY` — private key matching the instance's key pair
   - `ENV_FILE` — the full contents of your production `.env` (`JWT_SECRET`, `STORAGE_PROVIDER=s3`,
     `AWS_*`, `AI_API_KEY`, etc.) — `.github/workflows/deploy.yml` writes this directly to `.env`
     on the server during deploy
5. Push to `main` — the workflow builds & tests the backend, builds the frontend, then SSHes into
   EC2 and runs `docker compose up -d --build`.

## Interview Talking Points

- Why Redis: job search is read-heavy and rarely changes between writes — cache-aside with
  targeted eviction on writes keeps results fresh without hammering MySQL. Caching Spring Data's
  `Page<T>` directly turned out to be fragile (type erasure and lazy-loaded fields don't survive
  JSON round-tripping through Redis) — fixed by caching a plain DTO instead.
- Why the AI call has a fallback path: a fresher project that hard-fails without a paid API
  key is a bad demo experience; the keyword fallback keeps the whole flow runnable end-to-end.
  During development I used a local Ollama model to avoid burning API credits while iterating,
  then switched to a hosted model for final testing.
- Why pluggable storage: local disk for zero-friction development, S3 for production — same
  interface (`ResumeStorageService`), different implementation selected by one config value.
- Security: stateless JWT, BCrypt password hashing, method-level `@PreAuthorize` per role,
  ownership checks in the service layer (a recruiter can only edit/see applicants for their
  own postings).