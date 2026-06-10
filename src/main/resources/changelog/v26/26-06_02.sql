ALTER TABLE hierarchical_relationship
    DROP CONSTRAINT IF EXISTS fk_hr_concept1,
    DROP CONSTRAINT IF EXISTS fk_hr_concept2;