package com.mcp.tools.mdd_tools.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcp.tools.mdd_tools.model.ApiToolMetadata;
import com.mcp.tools.mdd_tools.repository.ApiToolRepository;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class DynamicToolConfig {

    private final ApiToolRepository repository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DynamicToolConfig(ApiToolRepository repository, JdbcTemplate jdbcTemplate) {
        this.repository = repository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Bean
    public ToolCallbackProvider dynamicToolProvider() {
        return () -> {
            List<ApiToolMetadata> toolMetadataList = repository.findAll();
            List<ToolCallback> toolCallbacks = new ArrayList<>();

            for (ApiToolMetadata meta : toolMetadataList) {
                ToolCallback callback = new ToolCallback() {

                    @Override
                    public String call(String input) {
                        try {
                            System.out.println("📥 Tool Input: " + input);
                            JsonNode node = objectMapper.readTree(input);

                            int limit = node.has("limit") ? node.get("limit").asInt() : meta.getDefaultLimit();
                            String whereField = node.has("whereField") ? node.get("whereField").asText() : null;
                            String whereValue = node.has("whereValue") ? node.get("whereValue").asText() : null;

                            // Build dynamic query
                            StringBuilder query = new StringBuilder("SELECT " + meta.getFields() + " FROM " + meta.getTableName());

                            if (whereField != null && whereValue != null) {
                                query.append(" WHERE ").append(whereField).append(" = ?");
                            }

                            query.append(" LIMIT ").append(limit);

                            // 🌟 Type-safe parameter binding
                            Object typedValue = resolveTypedValue(meta.getTableName(), whereField, whereValue);

                            List<?> results = (whereField != null && whereValue != null)
                                    ? jdbcTemplate.queryForList(query.toString(), typedValue)
                                    : jdbcTemplate.queryForList(query.toString());

                            // ✅ Return JSON
                            return objectMapper.writeValueAsString(results);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return "Error fetching data from " + meta.getTableName() + ": " + e.getMessage();
                        }
                    }

                    @Override
                    public ToolDefinition getToolDefinition() {
                        return ToolDefinition.builder()
                                .name(meta.getToolName())
                                .description(meta.getDescription() + " | Columns: " + meta.getFields())
                                .inputSchema("""
                                    {
                                      "type": "object",
                                      "properties": {
                                        "limit": { "type": "integer", "description": "Max rows to fetch (default: 10)" },
                                        "whereField": { "type": "string", "description": "Column name to filter" },
                                        "whereValue": { "type": "string", "description": "Value to filter on" }
                                      }
                                    }
                                """)
                                .build();
                    }

                    /**
                     * Attempts to convert whereValue to the correct type based on DB metadata.
                     */
                    private Object resolveTypedValue(String tableName, String columnName, String whereValue) {
                        if (tableName == null || columnName == null || whereValue == null) {
                            return null;
                        }

                        try {
                            String query = "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND COLUMN_NAME = ?";
                            String typeName = jdbcTemplate.queryForObject(query, new Object[]{tableName, columnName}, String.class);

                            if (typeName == null) return whereValue;

                            switch (typeName.toLowerCase()) {
                                case "int":
                                case "integer":
                                case "bigint":
                                    return Integer.parseInt(whereValue);
                                case "double":
                                case "float":
                                case "decimal":
                                    return Double.parseDouble(whereValue);
                                default:
                                    return whereValue; // Assume string
                            }

                        } catch (Exception e) {
                            // Fallback: return string
                            return whereValue;
                        }
                    }
                };

                toolCallbacks.add(callback);
            }

            return toolCallbacks.toArray(new ToolCallback[0]);
        };
    }
}
