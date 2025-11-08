🧠 MDD Tools – Dynamic Database-Driven Tool Server for AI Agents (MCP Compatible)

This Spring Boot application dynamically provisions MCP-compatible tools backed by database tables, enabling real-time, metadata-driven API actions for AI agents like Gemma, GPT, Claude, and others.

LLMs or agent frameworks can discover and call database-backed tools without requiring any code changes or server restarts, making it ideal for scalable AI-driven applications.

🚀 Key Features

Dynamic Tool Loading
Tools are stored in a metadata database and loaded at runtime. No redeployment needed.

MCP Server Compatible
Tools are exposed over Server-Sent Events (SSE) to any MCP client (e.g., Python, JS, LangChain agents).

Database-Backed Tools
Each row in the metadata table represents a callable tool, such as fetch_workers or fetch_countries.

Smart SQL Query Builder
Automatically builds SQL based on user-provided parameters like filters and limits. Handles type conversion.

LLM-Friendly Schemas
Tools advertise input schemas in JSON format, allowing local or remote AI models to call them correctly.

No Restart Required
Add or modify tools in the database and the changes show up immediately in the MCP tool list.

🧩 How It Works

Tool metadata is stored in a database table (api_tool_metadata).

Spring Boot auto-loads these definitions and exposes them as callable MCP tools.

Each tool supports parameters like limit, whereField, and whereValue.

Tools execute SQL queries against the database, with proper type casting.

Results are returned as JSON via the MCP protocol, ready for consumption by agents.

🛠️ Architecture Highlights

Spring Boot Backend that reads metadata and builds ToolCallbacks dynamically.

JDBC with Auto-Typing, ensuring the values passed in from MCP clients are correctly mapped to DB column types.

SSE Stream for Tools, meaning any client that speaks MCP can load and call tools in real time.

🧠 Common Use Cases

Give LLM-powered agents dynamic access to database tables.

Build multi-agent or multi-query workflows without backend changes.

Ideal for AI dashboards, natural language interfaces to structured data, and metadata-driven automation.

🔮 What's Next

Planned enhancements include:

Support for joining across multiple tables.

Optional fuzzy matching (LIKE/ILIKE) in SQL filters.

A user interface for live tool creation.

Live schema updates pushed instantly to agent apps.

✅ Summary

This project enables database-backed API tools for AI agents through a metadata-driven architecture.
Simply add a row in the database → get a dynamic API tool → usable instantly by agents like Gemma or GPT via MCP.
No restart. No manual code changes. Fully LLM-ready.