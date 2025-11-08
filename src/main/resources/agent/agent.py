from fastmcp import Client
import asyncio
import json

async def fetch_tools():
    async with Client("http://localhost:8080/sse") as client:
        tools = await client.list_tools()
        print("Available tools:")
        try:
            print(json.dumps(tools, indent=2, ensure_ascii=False))
        except (TypeError, ValueError):
            # fallback for non-serializable objects
            print(json.dumps(tools, default=lambda o: getattr(o, '__dict__', repr(o)), indent=2, ensure_ascii=False))

asyncio.run(fetch_tools())