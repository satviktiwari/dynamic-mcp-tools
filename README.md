# MDD Tools – Dynamic API Tool Loader

This Spring Boot application dynamically provisions and exposes a list of API tools from a JSON configuration file. Each tool (for example, call_api_1, call_api_2, etc.) is defined in tools.json and automatically registered at application startup. The application provides REST endpoints to view all currently registered tools.

## Overview

The MDD Tools project enables flexible and configuration-driven management of tools that represent API operations. Instead of statically defining each tool in code, tools are declared in a JSON file, making it simple to add, modify, or remove them without changing the codebase.

This pattern is useful for metadata-driven AI platforms, automation systems, or microservice orchestration frameworks where tools (API calls) may frequently change.

## Component Summary
1. DynamicToolConfig

- Loads tool metadata from tools.json at startup.

- Converts JSON entries into ApiToolMetadata objects.

- Registers all parsed tools in the in-memory ToolRegistry.

2. ToolRegistry

- Central in-memory registry for all loaded tools.

- Provides APIs to retrieve or manage the list of tools.

- Can be enhanced later to support runtime reloading.

3. ToolController

- Exposes REST endpoints for tool management.

- Current endpoint:

- GET /tools → Lists all currently registered tools.

4. ApiToolMetadata

- Represents metadata for a tool.

- Typically contains:

- toolName

- toolDescription

- toolOtherDetails

## Example Output (on startup)

If your tools.json contains three entries (call_api_1, call_api_2, and call_api_3), then the /tools endpoint will return all three tools after startup.

## Future Enhancements

- Add /tools/reload endpoint to refresh tool configuration without restart.

- Implement /tools/add endpoint to persist new tools dynamically.

- Add validation for tool schema and duplicate names.

- Extend support to trigger API calls directly via the registered tool metadata.

### Validation from a sample MCP agent:

<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/3d95a611-2c61-4dac-8444-bfb1f87a391f" />

### Postman screenshot

<img width="1920" height="1080" alt="Screenshot from 2025-11-02 13-45-17" src="https://github.com/user-attachments/assets/136674e7-db46-473a-bfa4-54d4e89d7208" />

