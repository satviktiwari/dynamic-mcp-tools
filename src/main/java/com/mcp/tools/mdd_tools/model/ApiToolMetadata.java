package com.mcp.tools.mdd_tools.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tools_metadata")
public class ApiToolMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tool_name")
    private String toolName;

    private String description;

    @Column(name = "table_name")
    private String tableName;

    private String fields;

    @Column(name = "default_limit")
    private Integer defaultLimit;

    public ApiToolMetadata() {}

    public ApiToolMetadata(Long id, String toolName, String description, String tableName, String fields, Integer defaultLimit) {
        this.id = id;
        this.toolName = toolName;
        this.description = description;
        this.tableName = tableName;
        this.fields = fields;
        this.defaultLimit = defaultLimit;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public String getFields() { return fields; }
    public void setFields(String fields) { this.fields = fields; }

    public Integer getDefaultLimit() { return defaultLimit; }
    public void setDefaultLimit(Integer defaultLimit) { this.defaultLimit = defaultLimit; }
}
