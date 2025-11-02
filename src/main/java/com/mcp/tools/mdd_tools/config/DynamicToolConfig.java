package com.mcp.tools.mdd_tools.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcp.tools.mdd_tools.model.ApiToolMetadata;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DynamicToolConfig {

    private final WebClient webClient = WebClient.create();

    @Bean
    public ToolCallbackProvider dynamicToolProvider() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Load metadata JSON
        InputStream inputStream = getClass().getResourceAsStream("/tools.json");
        List<ApiToolMetadata> toolMetadataList = mapper.readValue(
                inputStream,
                new TypeReference<List<ApiToolMetadata>>() {}
        );

        List<ToolCallback> toolCallbacks = new ArrayList<>();

        for (ApiToolMetadata meta : toolMetadataList) {
            ToolCallback callback = new ToolCallback() {

                @Override
                public String call(String input) {
                    try {
                        // Perform actual API call — here it's a simple GET
                        return webClient
                                .get()
                                .uri(meta.getUrl())
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();
                    } catch (Exception e) {
                        return "Error calling " + meta.getUrl() + ": " + e.getMessage();
                    }
                }

                @Override
                public ToolDefinition getToolDefinition() {
                    return ToolDefinition.builder()
                            .name(meta.getName())
                            .description(meta.getDescription())
                            .inputSchema("""
                {
                    "type": "object",
                    "properties": {
                        "input": { "type": "string", "description": "Optional input text or parameters" }
                    }
                }
            """)
                            .build();
                }

            };

            toolCallbacks.add(callback);
        }

        // ✅ Convert List to Array before returning
        return () -> toolCallbacks.toArray(new ToolCallback[0]);
    }
}
