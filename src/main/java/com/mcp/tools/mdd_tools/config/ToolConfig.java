package com.mcp.tools.mdd_tools.config;

import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.tool.ToolCallbackProvider;

import com.mcp.tools.mdd_tools.tools.MathematicalTools;

@Configuration
public class ToolConfig {

    @Bean
    public ToolCallbackProvider mathematicalToolProvider(MathematicalTools mathematicalTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(mathematicalTools)
                .build();
    }
}
