import aiohttp
import asyncio
import json

BASE_URL = "http://localhost:8080"

async def list_tools(session):
    async with session.get(f"{BASE_URL}/tools") as resp:
        tools = await resp.json()
        print("\n🔧 Available Tools:")
        print(json.dumps(tools, indent=2, ensure_ascii=False))
        return tools

async def call_tool(session, name, args):
    cid = f"call_{int(asyncio.get_event_loop().time()*1000)}"
    payload = {"cid": cid, "name": name, "arguments": args}

    async with session.post(f"{BASE_URL}/call", json=payload) as resp:
        ack = await resp.json()
        print(f"\n📤 Sent call → {ack}")
        return cid

async def stream_results(session):
    print("\n📡 Listening for streamed responses:\n")
    async with session.get(f"{BASE_URL}/stream") as resp:
        async for line in resp.content:
            line = line.decode().strip()
            if line:
                try:
                    event = json.loads(line)
                    print("📥", json.dumps(event, indent=2, ensure_ascii=False))
                except json.JSONDecodeError:
                    print("⚠️ Invalid NDJSON:", line)

async def main():
    async with aiohttp.ClientSession() as session:
        # Fetch tools
        await list_tools(session)

        # Start listening to stream in background
        asyncio.create_task(stream_results(session))

        # Call a dynamic tool
        await asyncio.sleep(1)
        await call_tool(session, "fetch_workers", {
            "whereField": "id",
            "whereValue": "1"
        })

        # Keep alive for 10s to receive streamed result
        await asyncio.sleep(10)

asyncio.run(main())
