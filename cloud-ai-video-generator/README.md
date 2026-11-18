# Cloud AI Video Generator

A Next.js MVP that generates videos from text prompts using the Runway API.

## Stack

- Next.js
- React
- TypeScript
- Runway SDK

## Local setup

1. Install Node.js 20+.
2. Copy `.env.example` to `.env.local`.
3. Add your Runway API key to `RUNWAYML_API_SECRET`.
4. Run `npm install`.
5. Run `npm run dev`.
6. Open `http://localhost:3000`.

Keep the Runway API key server-side. Never expose it in client-side code.

## Production roadmap

For a production SaaS, add authentication, PostgreSQL, object storage, background jobs, rate limiting, usage/credits, billing, and durable task status tracking.

## Deployment

This project can be deployed to Vercel or another Node-compatible hosting provider. Configure `RUNWAYML_API_SECRET` as a server-side environment variable.
