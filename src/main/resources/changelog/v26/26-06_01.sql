-- nettoyege des blancs

DELETE FROM hierarchical_relationship
WHERE
    btrim(id_concept1) = ''
   OR btrim(id_thesaurus) = ''
   OR btrim(role) = ''
   OR btrim(id_concept2) = '';

-- pour sécuriser la table hierarchical_relationship et éviter les concepts fantômes
DELETE FROM hierarchical_relationship hr
WHERE NOT EXISTS (
    SELECT 1
    FROM concept c
    WHERE c.id_concept = hr.id_concept1
      AND c.id_thesaurus = hr.id_thesaurus
)
   OR NOT EXISTS (
    SELECT 1
    FROM concept c
    WHERE c.id_concept = hr.id_concept2
      AND c.id_thesaurus = hr.id_thesaurus
);

ALTER TABLE hierarchical_relationship
    ADD CONSTRAINT fk_hr_concept1
        FOREIGN KEY (id_concept1, id_thesaurus)
            REFERENCES concept(id_concept, id_thesaurus)
            ON DELETE CASCADE;

ALTER TABLE hierarchical_relationship
    ADD CONSTRAINT fk_hr_concept2
        FOREIGN KEY (id_concept2, id_thesaurus)
            REFERENCES concept(id_concept, id_thesaurus)
            ON DELETE CASCADE;

-- protection des identifiants contre les blancs et les NULL

DO $$
    BEGIN

        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint WHERE conname = 'chk_hr_id_concept1_not_blank'
        ) THEN
            ALTER TABLE hierarchical_relationship
                ADD CONSTRAINT chk_hr_id_concept1_not_blank
                    CHECK (btrim(id_concept1) <> '');
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint WHERE conname = 'chk_hr_id_thesaurus_not_blank'
        ) THEN
            ALTER TABLE hierarchical_relationship
                ADD CONSTRAINT chk_hr_id_thesaurus_not_blank
                    CHECK (btrim(id_thesaurus) <> '');
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint WHERE conname = 'chk_hr_role_not_blank'
        ) THEN
            ALTER TABLE hierarchical_relationship
                ADD CONSTRAINT chk_hr_role_not_blank
                    CHECK (btrim(role) <> '');
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint WHERE conname = 'chk_hr_id_concept2_not_blank'
        ) THEN
            ALTER TABLE hierarchical_relationship
                ADD CONSTRAINT chk_hr_id_concept2_not_blank
                    CHECK (btrim(id_concept2) <> '');
        END IF;

    END $$;