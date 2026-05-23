# PetClinic Angular Front-End

## Description

Angular 16 single-page application for the ASID Spring PetClinic microservices stack. It communicates exclusively with the **API Gateway** (`http://localhost:8000`) — never directly with individual backend services.

## Tech Stack

| Concern | Technology |
|---|---|
| Framework | Angular 16.2.x |
| Language | TypeScript |
| HTTP | Angular `HttpClient` |
| Styling | Bootstrap 3 |

## Prerequisites

- Node.js 18+
- npm 9+

## Getting Started

### 1. Install dependencies
```bash
npm install
```

### 2. Start the backend stack
Ensure the API Gateway and all backend services are running before starting the front-end:
```bash
cd ~/ASID/API-Gateway
docker compose up -d
```

### 3. Run the development server
```bash
ng serve
```
Navigate to `http://localhost:4200`. The app hot-reloads on file changes.

## API Gateway Configuration

The base URL is configured in `src/environments/environment.ts`:
```typescript
export const environment = {
  production: false,
  REST_API_URL: 'http://localhost:8000/api/'
};
```

All Angular services build their endpoint URLs relative to `REST_API_URL`, so every request is routed through the gateway at port **8000**.

## Features

| Feature | Routes |
|---|---|
| Owners | List, create, view, edit, delete |
| Pets | Add pets to owners, view pet details |
| Pet Types | List, create, edit, delete |
| Vets | List, create, edit, delete |
| Specialties | List, create, edit, delete |
| Visits | Create and view visits per pet |

## Build

```bash
# Development build
ng build

# Production build
ng build --configuration production
```

Build artifacts are output to the `dist/` directory.

## Running Tests

```bash
# Unit tests (Karma)
ng test

# End-to-end tests (Protractor)
ng e2e
```
