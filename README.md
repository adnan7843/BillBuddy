# BillBuddy — AI‑Powered Plan Comparison Assistant

BillBuddy is a Spring Boot app that helps users **compare and choose the best telecom / utility plans** using **RAG (Retrieval‑Augmented Generation)**.  
It stores plan data, generates **vector embeddings**, and answers questions with **recommendations + citations**.

---

## Features

- 🔎 **Semantic plan search** (vector similarity)
- 🤖 **AI‑assisted recommendations** (LLM + RAG)
- 📊 **Structured comparisons** (cost breakdowns, trade‑offs)
- 🧾 **Citations** for transparency
- 🗄️ **H2 in‑memory database** for local development
- 📈 **Observability/logging hooks** (starter instrumentation)

---

## Tech Stack

- **Java 17+**
- **Spring Boot 3.x**
- **Spring Data JPA / Hibernate**
- **H2 Database**
- **OpenAI API** (embeddings + responses)
- **Maven**
- **Lombok**

---

## Project Structure

```text
BillBuddy/
├─ pom.xml
├─ README.md
└─ src/
   ├─ main/
   │  ├─ java/com/billbuddy/
   │  │  ├─ BillBuddyApplication.java
   │  │  ├─ controller/
   │  │  ├─ config/
   │  │  ├─ model/
   │  │  ├─ repository/
   │  │  └─ service/
   │  └─ resources/
   │     └─ application.properties (or application.yml)
   └─ test/
```

> Your exact packages/classes may differ slightly — this is a high‑level view.

---

## Prerequisites

- Java **17+**
- Maven **3.6+**
- An **OpenAI API key**
- IntelliJ IDEA (recommended) or any Java IDE

---

## Setup

### 1) Clone the repository

```bash
git clone https://github.com/adnan7843/BillBuddy.git
cd BillBuddy
```

### 2) Configure the OpenAI API key (required)

**Windows (PowerShell)**

```powershell
$env:OPENAI_API_KEY="sk-your-api-key-here"
```

**macOS / Linux (bash/zsh)**

```bash
export OPENAI_API_KEY="sk-your-api-key-here"
```

✅ **Tip:** Never commit your key. Add `.env` to `.gitignore` and/or use environment variables.

---

## Run the app

```bash
mvn clean install
mvn spring-boot:run
```

Default URLs:

- App: `http://localhost:8080`
- H2 Console (if enabled): `http://localhost:8080/h2-console`

---

## Configuration

BillBuddy reads config from:

- `src/main/resources/application.properties` **or**
- `src/main/resources/application.yml`

Example (YAML style):

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:billbuddy
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
      path: /h2-console
```

---

## API

### Health check

```http
GET /api/health
```

Example:

```bash
curl http://localhost:8080/api/health
```

### Compare plans

```http
POST /api/compare
Content-Type: application/json
```

Example:

```bash
curl -X POST http://localhost:8080/api/compare   -H "Content-Type: application/json"   -d '{"question":"Best internet plan for a family of 4?"}'
```

Example response (shape may vary):

```json
{
  "recommendation": "NBN100 would be ideal ...",
  "citations": [
    { "source": "Plan: Telstra - Family Unlimited NBN", "snippet": "..." }
  ],
  "costBreakdown": {
    "monthly": 99.0,
    "notes": "..."
  }
}
```

---

## Sample Data

On startup, the app can load sample plans (via `DataInitializer`) and index them for search.

If you want to disable startup indexing while testing, you can temporarily comment out the initializer logic.

---

## Troubleshooting

### `Could not resolve placeholder 'OPENAI_API_KEY'`

Your environment variable isn’t visible to the running process.

- Confirm it exists in the same terminal session where you run `mvn spring-boot:run`
- Restart IntelliJ after setting environment variables (or set them in the Run Configuration)

### H2: “Value too long for column EMBEDDING …”

Store embeddings as a **CLOB** (or equivalent large text type) instead of a short VARCHAR.

### Port already in use

Change the port:

```properties
server.port=8081
```

(or YAML equivalent)

---

## License (MIT / Apache‑2.0 )

---

## Contact

**Adnan Nasrullah**  
Email: adnan.nasrullah@gmail.com  
GitHub: [github.com/adnan7843]
