package com.mcp.tools.mdd_tools.model;

import jakarta.persistence.*;

@Entity
@Table(name = "api_tools")
public class ApiToolMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String method;
    private String url;

    @Column(name = "input_schema", columnDefinition = "TEXT")
    private String inputSchema;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getInputSchema() { return inputSchema; }
    public void setInputSchema(String inputSchema) { this.inputSchema = inputSchema; }

    public ApiToolMetadata() {
    }

    public ApiToolMetadata(Long id, String name, String description, String method, String url, String inputSchema) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.method = method;
        this.url = url;
        this.inputSchema = inputSchema;
    }
}
