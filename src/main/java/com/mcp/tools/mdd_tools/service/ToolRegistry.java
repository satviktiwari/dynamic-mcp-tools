package com.mcp.tools.mdd_tools.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcp.tools.mdd_tools.model.ApiToolMetadata;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ToolRegistry {

    private final List<ApiToolMetadata> registeredTools = new ArrayList<>();

    @PostConstruct
    public void loadToolsFromJson() {
        try (InputStream inputStream = getClass().getResourceAsStream("/tools.json")) {
            if (inputStream == null) {
                System.out.println("No tools.json found in resources.");
                return;
            }
            ObjectMapper mapper = new ObjectMapper();
            List<ApiToolMetadata> tools = mapper.readValue(inputStream, new TypeReference<>() {});
            registeredTools.addAll(tools);
            System.out.println("Loaded " + tools.size() + " tools from JSON");
        } catch (Exception e) {
            System.err.println("Failed to load tools from JSON: " + e.getMessage());
        }
    }

    public List<ApiToolMetadata> getAllTools() {
        return registeredTools;
    }
}
