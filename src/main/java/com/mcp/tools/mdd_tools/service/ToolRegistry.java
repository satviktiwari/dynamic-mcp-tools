package com.mcp.tools.mdd_tools.service;

import com.mcp.tools.mdd_tools.model.ApiToolMetadata;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
public class ToolRegistry {

    private final JdbcTemplate jdbcTemplate;
    private final WebClient webClient;
    private final List<ToolCallback> registeredCallbacks = new ArrayList<>();
    private final List<ApiToolMetadata> registeredTools = new ArrayList<>();

    public ToolRegistry(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.webClient = WebClient.create();
    }

    @PostConstruct
    public void loadToolsFromDatabase() {
        var tools = jdbcTemplate.query(
                "SELECT id, name, description, method, url, input_schema FROM api_tools",
                (rs, rowNum) -> new ApiToolMetadata(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("method"),
                        rs.getString("url"),
                        rs.getString("input_schema")
                )
        );

        registeredTools.clear();
        registeredCallbacks.clear();

        for (ApiToolMetadata meta : tools) {
            registerTool(meta, false);
        }
    }

    public List<ApiToolMetadata> getAllTools() {
        return registeredTools;
    }

    public void registerTool(ApiToolMetadata meta, boolean persist) {
        ToolCallback callback = new ToolCallback() {
            @Override
            public String call(String input) {
                try {
                    if ("POST".equalsIgnoreCase(meta.getMethod())) {
                        return webClient.post()
                                .uri(meta.getUrl())
                                .bodyValue(input)
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();
                    } else {
                        return webClient.get()
                                .uri(meta.getUrl())
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();
                    }
                } catch (Exception e) {
                    return "Error calling " + meta.getUrl() + ": " + e.getMessage();
                }
            }

            @Override
            public ToolDefinition getToolDefinition() {
                return ToolDefinition.builder()
                        .name(meta.getName())
                        .description(meta.getDescription())
                        .inputSchema(meta.getInputSchema())
                        .build();
            }
        };

        registeredCallbacks.add(callback);
        registeredTools.add(meta);

        if (persist) {
            String sql = """
            INSERT INTO api_tools (name, description, method, url, input_schema)
            VALUES (?, ?, ?, ?, ?::jsonb)
            RETURNING id
        """;

            Long id = jdbcTemplate.queryForObject(
                    sql,
                    new Object[]{
                            meta.getName(),
                            meta.getDescription(),
                            meta.getMethod(),
                            meta.getUrl(),
                            meta.getInputSchema()
                    },
                    Long.class
            );

            meta.setId(id);
        }
    }


    public ToolCallback[] getAllCallbacks() {
        return registeredCallbacks.toArray(new ToolCallback[0]);
    }
}
