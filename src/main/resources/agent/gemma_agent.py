import json
import requests
import aiohttp
import asyncio
from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse
from pydantic import BaseModel

# 🔗 Updated endpoints
TOOLS_URL = "http://localhost:8080/tools"
CALL_URL = "http://localhost:8080/call"
STREAM_URL = "http://localhost:8080/stream"
LLM_API_URL = "http://localhost:11434/api/generate"  # Gemma 2B via Ollama

app = FastAPI(title="Gemma Stream Agent", version="2.0")


# ----------------------------
# 📦 Models
# ----------------------------

class QueryRequest(BaseModel):
    user_query: str


class ToolExecutionRequest(BaseModel):
    tool_name: str
    params: dict


# ----------------------------
# ⚙️ Streamable HTTPS Agent Logic
# ----------------------------

async def get_tools():
    """Fetch tools from /tools endpoint."""
    async with aiohttp.ClientSession() as session:
        async with session.get(TOOLS_URL) as resp:
            if resp.status != 200:
                raise HTTPException(status_code=resp.status, detail="Failed to fetch tools")
            data = await resp.json()
            # ✅ handle both cases
            if isinstance(data, list):
                return data
            elif isinstance(data, dict) and "tools" in data:
                return data["tools"]
            else:
                return []


async def execute_tool(tool_name: str, params: dict):
    """Call /call endpoint to execute a specific tool."""
    cid = f"call_{int(asyncio.get_event_loop().time() * 1000)}"
    payload = {"cid": cid, "name": tool_name, "arguments": params}

    async with aiohttp.ClientSession() as session:
        async with session.post(CALL_URL, json=payload) as resp:
            if resp.status != 200:
                raise HTTPException(status_code=resp.status, detail="Failed to execute tool")
            ack = await resp.json()
            print(f"✅ Tool call acknowledged: {ack}")
            return await wait_for_result(cid)


async def wait_for_result(cid: str):
    """Listen to /stream until we find a matching CID result."""
    async with aiohttp.ClientSession() as session:
        async with session.get(STREAM_URL) as resp:
            async for line in resp.content:
                line = line.decode().strip()
                if not line:
                    continue
                try:
                    event = json.loads(line)
                    if event.get("cid") == cid:
                        return event
                except json.JSONDecodeError:
                    continue
    raise HTTPException(status_code=504, detail=f"No response found for call_id={cid}")


# ----------------------------
# 🧠 LLM Integration (Gemma 2B)
# ----------------------------

def ask_llm(prompt: str) -> str:
    """Send prompt to local Gemma LLM."""
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
    """Map user NL query to a tool + params using the LLM."""
    tool_summaries = "\n".join(
        f"- {t['name']}: {t.get('description', '').split('|')[0].strip()} "
        f"(Fields: {t.get('description', '').split('Columns:')[-1].strip()})"
        for t in tools
    )

    prompt = f"""
You are a JSON-only decision-making agent.
You must respond ONLY with a valid JSON object that follows this structure:
{{
  "tool": "<tool_name>",
  "params": {{
    "whereField": "<column_name>",
    "whereValue": "<value>",
    "limit": 5
  }}
}}

DO NOT add explanations or extra text — only output JSON.

Here are the available tools:
{json.dumps([{t['name']: t['description']} for t in tools], indent=2)}

User question:
"{user_query}"

Pick the most relevant tool name based on the user's intent.
Infer which field and value to filter by.
If you don't know, return {{"tool": "none"}} only.
"""


    llm_output = ask_llm(prompt)
    try:
        return json.loads(llm_output)
    except Exception:
        return {"tool": "none"}


# ----------------------------
# 🌐 FastAPI Routes
# ----------------------------

@app.get("/tools")
async def list_tools():
    """Return all available tools from /tools."""
    tools = await get_tools()
    return JSONResponse(content={"count": len(tools), "tools": tools})


@app.post("/execute")
async def run_tool(request: ToolExecutionRequest):
    """Directly execute a tool."""
    result = await execute_tool(request.tool_name, request.params)
    return JSONResponse(content=result)


@app.post("/query")
async def query_agent(request: QueryRequest):
    """Ask natural language query → find tool → execute → return result."""
    tools = await get_tools()
    for tool in tools:
        print(tool)
    parsed = interpret_user_query(request.user_query, tools)

    if parsed["tool"] == "none":
        return JSONResponse(content={"message": "No matching tool found."})

    result = await execute_tool(parsed["tool"], parsed.get("params", {}))
    return JSONResponse(content=result)


@app.get("/")
def root():
    return {"message": "🤖 Gemma Stream Agent API is running with Streamable HTTPS!"}
