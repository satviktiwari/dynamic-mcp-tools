CREATE TABLE api_tools (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  method VARCHAR(10),
  url TEXT,
  input_schema JSONB
);

CREATE INDEX idx_api_tools_name ON api_tools (name);
CREATE INDEX idx_api_tools_method ON api_tools (method);
