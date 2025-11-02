package com.mcp.tools.mdd_tools.repository;

import com.mcp.tools.mdd_tools.model.ApiToolMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiToolRepository extends JpaRepository<ApiToolMetadata, Long> {
}
