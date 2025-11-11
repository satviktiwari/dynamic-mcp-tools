package com.mcp.tools.mdd_tools.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcp.tools.mdd_tools.config.DynamicToolConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

@RestController
@Slf4j
public class ToolCallController {

    private final DynamicToolConfig dynamicToolConfig;
    private final StreamEventBus bus;
    private final ObjectMapper objectMapper;

    public ToolCallController(DynamicToolConfig dynamicToolConfig, StreamEventBus bus, ObjectMapper objectMapper) {
        this.dynamicToolConfig = dynamicToolConfig;
        this.bus = bus;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/call")
    public ResponseEntity<Map<String, Object>> invokeTool(@RequestBody Map<String, Object> req) {
        String cid = (String) req.get("cid");
        String toolName = (String) req.get("name");
        Map<String, Object> arguments = (Map<String, Object>) req.get("arguments");

        System.out.println("Received call for tool: " +  cid + " : " + toolName);

        ToolCallback target = Arrays.stream(dynamicToolConfig.getToolCallbacks())
                .filter(cb -> cb.getToolDefinition().name().equalsIgnoreCase(toolName))
                .findFirst()
                .orElse(null);

        if (target == null) {
            System.out.println("Tool not found: " +  toolName);
            bus.publish(Map.of(
                    "cid", cid,
                    "name", toolName,
                    "status", "error",
                    "error", "Tool not registered"
            ));
            return ResponseEntity.ok(Map.of("ack", "received", "cid", cid));
        }

        try {
            String inputJson = objectMapper.writeValueAsString(arguments);

            // Run tool (sync for now, can move to executor)
            String result = target.call(inputJson);

            bus.publish(Map.of(
                    "cid", cid,
                    "name", toolName,
                    "status", "success",
                    "result", result
            ));
            System.out.println("Tool executed successfully: " + toolName);
        } catch (Exception e) {
            System.out.println("Error: " + toolName + " : " + e);
            bus.publish(Map.of(
                    "cid", cid,
                    "name", toolName,
                    "status", "error",
                    "error", e.getMessage()
            ));
        }

        // Immediate ACK
        return ResponseEntity.ok(Map.of("ack", "received", "cid", cid));
    }
}

