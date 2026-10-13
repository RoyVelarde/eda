ALTER TABLE personal
    ALTER COLUMN nivel TYPE VARCHAR(20)
        USING CASE nivel
            WHEN 2 THEN 'NIVEL_2'
            WHEN 3 THEN 'NIVEL_3'
            ELSE NULL
    END;

ALTER TABLE personal DROP CONSTRAINT chk_personal_nivel;

ALTER TABLE personal ADD CONSTRAINT chk_personal_nivel CHECK (nivel IN ('NIVEL_2', 'NIVEL_3'));