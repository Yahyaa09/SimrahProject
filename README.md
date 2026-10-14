# Skin Routine Builder

Spring Boot API (`/`) + React/Vite frontend (`/frontend`).

## Run locally

```bash
# Backend on http://localhost:8080 (in-memory H2, demo data auto-seeded)
./mvnw spring-boot:run

# Frontend on http://localhost:5173
cd frontend && npm install && npm run dev
```

Demo cases: **Body Acne** (Salicylic Acid + Physical Scrub clash), **Strawberry Skin**
(Glycolic Acid + Retinol clash), **Hyperpigmentation** (safe).

## API

| Method | Path | Body |
| --- | --- | --- |
| POST | `/api/v1/routine/build` | `{"primaryConcern": "Body Acne", "allergies": "TREE_NUTS, niacinamide"}` |
| POST | `/api/v1/routine/analyze-skin` | multipart form: `image` (JPG/PNG), optional `allergies` |
| GET | `/api/v1/routine/allergens` | – (list of common allergens) |
| GET | `/api/v1/routine/health` | – |

`allergies` is a comma-separated list of allergen keys, common names (e.g. `nuts`, `aspirin`, `sls`)
or any ingredient name. Products containing them are swapped for an allergy-safe alternative of
the same category, or removed if none exists (see `allergyAdjustments` in the response).

`analyze-skin` uses Hugging Face zero-shot image classification (CLIP) when
`HUGGINGFACE_API_TOKEN` is set, otherwise (or on failure) a built-in colour heuristic.

## Backend environment variables

| Variable | Default | Purpose |
| --- | --- | --- |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Comma-separated frontend URLs |
| `SPRING_DATASOURCE_URL` | H2 in-memory | e.g. Supabase JDBC URL |
| `SPRING_DATASOURCE_USERNAME` / `_PASSWORD` | `sa` / empty | DB credentials |
| `HUGGINGFACE_API_TOKEN` | empty | Enables real AI image analysis |
| `SEED_DATA` | `true` | Seeds demo data into empty tables |

## Deploy (free tier)

**Backend – Render.com**
1. New → Blueprint (uses `render.yaml`) or New → Web Service → Docker.
2. Set env vars. For Supabase use the **Session pooler** connection (IPv4), e.g.
   `SPRING_DATASOURCE_URL=jdbc:postgresql://aws-0-<region>.pooler.supabase.com:5432/postgres?sslmode=require`,
   `SPRING_DATASOURCE_USERNAME=postgres.<project-ref>`.
3. Set `CORS_ALLOWED_ORIGINS=

**Frontend – Netlify**
1. Import the GitHub repo (settings come from `netlify.toml`).
2. Add env var `VITE_API_BASE_URL=https://<your-api>.onrender.com`, then deploy .

For a local live demo, leave `VITE_API_BASE_URL` unset and it defaults to `http://localhost:8080`.
