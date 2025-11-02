package com.mcp.tools.mdd_tools.config;

import com.mcp.tools.mdd_tools.model.ApiToolMetadata;
import com.mcp.tools.mdd_tools.repository.ApiToolRepository;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DynamicToolConfig {

    private final WebClient webClient = WebClient.create();
    private final ApiToolRepository repository;

    public DynamicToolConfig(ApiToolRepository repository) {
        this.repository = repository;
    }

    @Bean
    public ToolCallbackProvider dynamicToolProvider() {
        // Return a provider that always reads latest data when called
        return () -> {
            List<ApiToolMetadata> toolMetadataList = repository.findAll();
            List<ToolCallback> toolCallbacks = new ArrayList<>();

            for (ApiToolMetadata meta : toolMetadataList) {
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
                                .inputSchema(meta.getInputSchema() != null ? meta.getInputSchema() : """
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

            return toolCallbacks.toArray(new ToolCallback[0]);
        };
    }
}
