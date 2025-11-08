package com.mcp.tools.mdd_tools.service;

import com.mcp.tools.mdd_tools.model.ApiToolMetadata;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ToolRegistry {

    private final JdbcTemplate jdbcTemplate;
    private final List<ToolCallback> registeredCallbacks = new ArrayList<>();
    private final List<ApiToolMetadata> registeredTools = new ArrayList<>();

    public ToolRegistry(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void loadToolsFromDatabase() {
        var tools = jdbcTemplate.query(
                "SELECT id, tool_name, description, table_name, fields, default_limit FROM tools_metadata",
                (rs, rowNum) -> new ApiToolMetadata(
                        rs.getLong("id"),
                        rs.getString("tool_name"),
                        rs.getString("description"),
                        rs.getString("table_name"),
                        rs.getString("fields"),
                        rs.getInt("default_limit")
                )
        );

        registeredTools.clear();
        registeredCallbacks.clear();

        for (ApiToolMetadata meta : tools) {
            registerTool(meta);
        }
    }

    public List<ApiToolMetadata> getAllTools() {
        return registeredTools;
    }

    public void registerTool(ApiToolMetadata meta) {
        ToolCallback callback = new ToolCallback() {
            @Override
            public String call(String input) {
                try {
                    String query = String.format(
                            "SELECT %s FROM %s LIMIT %d",
                            meta.getFields(),
                            meta.getTableName(),
                            meta.getDefaultLimit()
                    );
                    List<?> results = jdbcTemplate.queryForList(query);
                    return results.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining("\n"));
                } catch (Exception e) {
                    return "Error fetching data from " + meta.getTableName() + ": " + e.getMessage();
                }
            }

            @Override
            public ToolDefinition getToolDefinition() {
                return ToolDefinition.builder()
                        .name(meta.getToolName())
                        .description(meta.getDescription())
                        .inputSchema("""
                            {
                                "type": "object",
                                "properties": {
                                    "limit": { "type": "integer", "description": "Number of rows to fetch (default: 10)" }
                                }
                            }
                        """)
                        .build();
            }
        };

        registeredCallbacks.add(callback);
        registeredTools.add(meta);
    }

    public ToolCallback[] getAllCallbacks() {
        return registeredCallbacks.toArray(new ToolCallback[0]);
    }
}
