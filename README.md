
---

# 🧩 MDD Tools – Dynamic API Tool Loader (Database-Driven)

This Spring Boot application dynamically provisions and manages **API tools** from a **database (Supabase / PostgreSQL)** instead of a JSON file.
Each tool represents a callable API operation (e.g., `call_api_1`, `call_google`, etc.) and is automatically registered and exposed via REST endpoints.

This enables flexible, metadata-driven management of APIs — ideal for **AI agents**, **MCP servers**, or **automation frameworks** where tools (API integrations) frequently change.

---

## 🚀 Overview

MDD Tools is a **metadata-driven, runtime-extensible tool registry**.
It automatically loads tool definitions from a **database**, builds corresponding executable callbacks, and exposes APIs for registration and retrieval — all without requiring a restart.

---

## 🧱 Core Architecture

### 1. `ApiToolMetadata` (Model)

Represents tool metadata stored in the database.

| Field          | Type      | Description                           |
| -------------- | --------- | ------------------------------------- |
| `id`           | BIGSERIAL | Unique tool ID                        |
| `name`         | VARCHAR   | Unique name for the tool              |
| `description`  | TEXT      | Description of the tool’s function    |
| `method`       | VARCHAR   | HTTP method (`GET`, `POST`, etc.)     |
| `url`          | TEXT      | Target API endpoint                   |
| `input_schema` | TEXT      | JSON schema defining input parameters |

---

### 2. `ApiToolRepository`

Spring Data JPA repository for CRUD operations on the `api_tools` table.

---

### 3. `DynamicToolRegistry`

Central **in-memory tool registry** that:

* Loads tools from the database at startup.
* Exposes them as executable `ToolCallback` instances.
* Supports runtime refresh without restarting the app.

---

### 4. `ApiToolController`

Exposes REST APIs for tool management:

| Method | Endpoint             | Description                                   |
| ------ | -------------------- | --------------------------------------------- |
| `GET`  | `/api/tools`         | List all registered tools                     |
| `POST` | `/api/tools`         | Register a new tool (auto-refreshes registry) |
| `POST` | `/api/tools/refresh` | Manually refresh tools from DB                |

When a new tool is added using `POST /api/tools`, it is automatically persisted in the database **and becomes immediately available** without restarting.

---

### 5. `DynamicToolConfig`

Registers `DynamicToolRegistry` as a Spring Bean so that tool definitions are available to the entire application and any connected MCP server.

---

## 🗄️ Database Schema (Supabase / PostgreSQL)

```sql
CREATE TABLE api_tools (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  method VARCHAR(10),
  url TEXT,
  input_schema TEXT
);
```

Configure Supabase credentials in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://db.<your-supabase-id>.supabase.co:5432/postgres
    username: <your-username>
    password: <your-password>
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

---

## 💡 Example Usage

### ✅ Create a Tool

```bash
curl -X POST http://localhost:8080/api/tools \
  -H "Content-Type: application/json" \
  -d '{
    "name": "call_google",
    "description": "Makes a call to Google",
    "method": "GET",
    "url": "https://www.google.com",
    "inputSchema": "{\"type\": \"object\", \"properties\": {\"input\": {\"type\": \"string\"}}}"
  }'
```

**Response:**

```json
{
  "id": 7,
  "name": "call_google",
  "description": "Makes a call to Google",
  "method": "GET",
  "url": "https://www.google.com",
  "inputSchema": "{\"type\":\"object\",\"properties\":{\"input\":{\"type\":\"string\"}}}"
}
```

---

### ✅ Get All Tools

```bash
curl http://localhost:8080/api/tools
```

**Response:**

```json
[
  {
    "id": 1,
    "name": "call_api_1",
    "description": "Makes a call to API 1",
    "method": "GET",
    "url": "https://localhost:8000/api1",
    "inputSchema": "{\"type\": \"object\", \"properties\": {\"input\": {\"type\": \"string\"}}}"
  },
  {
    "id": 7,
    "name": "call_google",
    "description": "Makes a call to Google",
    "method": "GET",
    "url": "https://www.google.com",
    "inputSchema": "{\"type\": \"object\", \"properties\": {\"input\": {\"type\": \"string\"}}}"
  }
]
```

---

### ✅ Manually Refresh Tools

If tools are updated directly in the database, trigger a refresh:

```bash
curl -X POST http://localhost:8080/api/tools/refresh
```

**Response:**
`Tools refreshed successfully`

---

## ⚙️ Runtime Behavior

* Tools are loaded dynamically from the database on startup.
* When a new tool is registered:

    * It is persisted in Supabase.
    * The registry reloads in memory.
    * It becomes available to any connected MCP/AI agent instantly.
* No application restart is needed.

---

## 🧠 Use Cases

* **MCP or Agentic AI Platforms**: Register callable APIs dynamically as tools.
* **Automation Systems**: Manage microservice actions without redeployment.
* **Metadata-Driven Frameworks**: Store tool definitions centrally in a DB.

---

## 🔮 Future Enhancements

* Add `DELETE /api/tools/{id}` for tool removal.
* Add input validation and duplicate tool name checks.
* Support versioning and audit tracking for tools.
* Add live push notification for tool changes to connected agents.

---

## 🧾 Summary

| Feature              | Old (JSON-based) | New (DB-based)                    |
| -------------------- | ---------------- | --------------------------------- |
| Storage              | `tools.json`     | Supabase/PostgreSQL               |
| Reload               | On app restart   | Automatic or via `/refresh`       |
| Add Tool             | Manual JSON edit | REST API (`POST /api/tools`)      |
| Persistence          | File-based       | Database                          |
| Scalability          | Limited          | High – supports concurrent writes |
| AI/MCP Compatibility | Partial          | Full (real-time dynamic registry) |

---

