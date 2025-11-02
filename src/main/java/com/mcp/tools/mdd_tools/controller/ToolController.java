package com.mcp.tools.mdd_tools.controller;

import com.mcp.tools.mdd_tools.model.ApiToolMetadata;
import com.mcp.tools.mdd_tools.service.ToolRegistry;
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
    public List<ApiToolMetadata> getRegisteredTools() {
        return toolRegistry.getAllTools();
    }
}
