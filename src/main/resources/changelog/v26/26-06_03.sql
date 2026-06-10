CREATE EXTENSION IF NOT EXISTS "pgcrypto";
DROP table if exists ai_provider;
CREATE TABLE ai_provider (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             name VARCHAR(255) NOT NULL UNIQUE,
                             provider_type VARCHAR(50) NOT NULL,
                             base_url TEXT NOT NULL,
                             method VARCHAR(10) NOT NULL
                                 CHECK (method IN ('GET', 'POST', 'PUT', 'DELETE')),
                             headers_template TEXT,
                             body_template TEXT,
                             response_path TEXT NOT NULL,
                             response_format VARCHAR(20) NOT NULL
                                 CHECK (response_format IN ('TEXT', 'JSON', 'JSON_PATH')),
                             active BOOLEAN NOT NULL DEFAULT TRUE,
                             created_at TIMESTAMP NOT NULL DEFAULT now(),
                             updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_ai_provider_active ON ai_provider(active);
CREATE INDEX idx_ai_provider_type ON ai_provider(provider_type);
ALTER TABLE ai_provider ADD CONSTRAINT chk_base_url_not_empty CHECK (length(base_url) > 0);

CREATE TABLE ai_provider_secret (
                                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                    provider_id UUID NOT NULL,
                                    tenant_id UUID NOT NULL,
                                    encrypted_api_key TEXT NOT NULL,
                                    created_at TIMESTAMP DEFAULT now(),
                                    updated_at TIMESTAMP DEFAULT now(),
                                    CONSTRAINT fk_provider
                                        FOREIGN KEY (provider_id)
                                            REFERENCES ai_provider(id)
);

CREATE TABLE thesaurus_ai_config (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     thesaurus_id VARCHAR NOT NULL,
                                     provider_id UUID NOT NULL,
                                     provider_secret_id UUID,
                                     is_default BOOLEAN NOT NULL DEFAULT FALSE,
                                     enabled BOOLEAN NOT NULL DEFAULT TRUE,
                                     created_at TIMESTAMP DEFAULT now(),
                                     updated_at TIMESTAMP DEFAULT now(),
                                     CONSTRAINT fk_provider
                                         FOREIGN KEY (provider_id)
                                             REFERENCES ai_provider(id),
                                     CONSTRAINT fk_secret
                                         FOREIGN KEY (provider_secret_id)
                                             REFERENCES ai_provider_secret(id)
);


INSERT INTO ai_provider (
    name,
    provider_type,
    base_url,
    method,
    headers_template,
    body_template,
    response_path,
    response_format,
    active
)
VALUES (
           'Ollama GPT OSS',
           'OLLAMA',
           'https://ollama.com/api/generate',
           'POST',
           '{"Authorization":"Bearer {{apiKey}}","Content-Type":"application/json"}',
           '{"model":"gpt-oss:120b","prompt":"{{prompt}}","stream":false}',
           'response',
           'TEXT',
           true
       );

INSERT INTO ai_provider_secret (
    provider_id,
    tenant_id,
    encrypted_api_key
)
VALUES (
           (SELECT id FROM ai_provider WHERE name = 'Ollama GPT OSS'),
           '11111111-1111-1111-1111-111111111111',
           'ENCRYPTED_OLLAMA_KEY_HERE'
       );

INSERT INTO thesaurus_ai_config (
    thesaurus_id,
    provider_id,
    provider_secret_id,
    is_default,
    enabled
)
VALUES (
           'th2',
           (SELECT id FROM ai_provider WHERE name = 'Ollama GPT OSS'),
           (SELECT id FROM ai_provider_secret WHERE encrypted_api_key = 'ENCRYPTED_OLLAMA_KEY_HERE'),
           true,
           true
       );