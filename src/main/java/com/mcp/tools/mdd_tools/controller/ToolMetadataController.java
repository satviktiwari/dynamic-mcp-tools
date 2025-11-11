package com.mcp.tools.mdd_tools.controller;

import com.mcp.tools.mdd_tools.config.DynamicToolConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tools")
public class ToolMetadataController {

    private final DynamicToolConfig dynamicToolConfig;

    public ToolMetadataController(DynamicToolConfig dynamicToolConfig) {
        this.dynamicToolConfig = dynamicToolConfig;
    }

    @GetMapping
    public List<Map<String, String>> listTools() {
        return Arrays.stream(dynamicToolConfig.getToolCallbacks())
                .map(cb -> {
                    var def = cb.getToolDefinition();
                    return Map.of(
                            "name", def.name(),
                            "description", def.description(),
                            "inputSchema", def.inputSchema()
                    );
                })
                .toList();
    }

    @GetMapping("/{toolName}")
    public ResponseEntity<ToolDefinition> getTool(@PathVariable String toolName) {
        return dynamicToolConfig.getToolDefinition(toolName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
