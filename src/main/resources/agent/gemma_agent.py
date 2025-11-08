import json
import requests
import asyncio
from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from fastmcp import Client

MCP_SERVER_URL = "http://localhost:8080/sse"
LLM_API_URL = "http://localhost:11434/api/generate"  # Gemma 2B via Ollama

app = FastAPI(title="Gemma MCP Agent", version="1.0")

class QueryRequest(BaseModel):
    user_query: str

class ToolExecutionRequest(BaseModel):
    tool_name: str
    params: dict

async def get_tools():
    """Fetch tools from MCP server."""
    async with Client(MCP_SERVER_URL) as client:
        tools = await client.list_tools()
        return [t.model_dump() if hasattr(t, "model_dump") else t.__dict__ for t in tools]

async def execute_tool(tool_name: str, params: dict):
    async with Client(MCP_SERVER_URL) as client:
        tools = await client.list_tools()
        tool_names = [t.name for t in tools]
        if tool_name not in tool_names:
            raise HTTPException(status_code=404, detail=f"Tool '{tool_name}' not found on MCP server")

        try:
            return await client.call_tool(tool_name, params)
        except Exception as e:
            raise HTTPException(status_code=500, detail=f"Error executing tool '{tool_name}': {e}")


def ask_llm(prompt: str) -> str:
    try:
        with requests.post(
            LLM_API_URL,
            json={"model": "gemma:2b", "prompt": prompt, "stream": True},
            stream=True,
            timeout=180,
        ) as response:
            response.raise_for_status()
            full_response = ""
            for line in response.iter_lines():
                if line:
                    data = json.loads(line.decode("utf-8"))
                    full_response += data.get("response", "")
            return full_response.strip()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"LLM call failed: {e}")

def interpret_user_query(user_query: str, tools: list):
    """Map NL query to a specific MCP tool call."""
    # Shorten tool descriptions to avoid bloating the prompt
    tool_summaries = "\n".join(
        f"- {t['name']}: {t.get('description', '').split('|')[0].strip()} (Fields: {t.get('description', '').split('Columns:')[-1].strip()})"
        for t in tools
    )

    prompt = f"""
You are a smart AI agent that picks a database tool based on user questions.
Each tool corresponds to a specific table and fields.

Tools available:
{tool_summaries}

User question:
"{user_query}"

Your task:
1. Identify which tool is relevant based on what data the user wants.
2. Infer the filter field ('whereField') and its value ('whereValue') from the question.
3. Return a valid JSON like this:
{{
  "tool": "<tool_name>",
  "params": {{
    "whereField": "<column_name>",
    "whereValue": "<value>",
    "limit": 5
  }}
}}

Examples:
Q: "What games were released in 2020?"
A: {{
  "tool": "fetch_games",
  "params": {{
    "whereField": "release_year",
    "whereValue": "2020",
    "limit": 5
  }}
}}

Q: "Who is the worker named Rohit Verma?"
A: {{
  "tool": "fetch_workers",
  "params": {{
    "whereField": "name",
    "whereValue": "Rohit Verma",
    "limit": 5
  }}
}}

If no tool is suitable, return:
{{"tool": "none"}}
"""

    llm_output = ask_llm(prompt)
    try:
        return json.loads(llm_output)
    except Exception:
        return {"tool": "none"}


@app.get("/tools")
async def list_tools():
    """Fetch all tools from MCP."""
    tools = await get_tools()
    return JSONResponse(content={"count": len(tools), "tools": tools})

@app.post("/execute")
async def run_tool(request: ToolExecutionRequest):
    """Directly execute a tool."""
    result = await execute_tool(request.tool_name, request.params)
    return result


@app.post("/query")
async def query_agent(request: QueryRequest):
    """Ask the AI agent a natural language query."""
    tools = await get_tools()
    parsed = interpret_user_query(request.user_query, tools)

    if parsed["tool"] == "none":
        return JSONResponse(content={"message": "No matching tool found."})

    result = await execute_tool(parsed["tool"], parsed.get("params", {}))
    return result


@app.get("/")
def root():
    return {"message": "🤖 Gemma MCP Agent API is running!"}
