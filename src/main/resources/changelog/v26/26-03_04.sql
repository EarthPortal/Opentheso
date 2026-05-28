ALTER TABLE alignement_source
    ADD COLUMN IF NOT EXISTS is_global boolean NOT NULL DEFAULT true;

ALTER TABLE alignement_source
    ADD COLUMN IF NOT EXISTS id_thesaurus_owner varchar NULL;

ALTER TABLE alignement_source
DROP CONSTRAINT IF EXISTS alignement_source_source_key;

CREATE UNIQUE INDEX IF NOT EXISTS alignement_source_unique
    ON alignement_source (source, COALESCE(id_thesaurus_owner, 'GLOBAL'));