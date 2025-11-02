package com.mcp.tools.mdd_tools.controller;

import com.mcp.tools.mdd_tools.model.ApiToolMetadata;
import com.mcp.tools.mdd_tools.service.ToolRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tools")
public class ToolController {

    private final ToolRegistry toolRegistry;

    public ToolController(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    @GetMapping
    public List<ApiToolMetadata> listAllTools() {
        return toolRegistry.getAllTools();
    }

    @PostMapping
    public String createTool(@RequestBody ApiToolMetadata newTool) {
        toolRegistry.registerTool(newTool, true);
        return "New tool registered successfully: " + newTool.getName();
    }

    @PostMapping("/reload")
    public ResponseEntity<String> reloadTools() {
        toolRegistry.loadToolsFromDatabase();
        return ResponseEntity.ok("Tools reloaded from database");
    }
}

