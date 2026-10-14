ALTER TABLE personal ADD COLUMN ranking INTEGER;

ALTER TABLE personal ADD CONSTRAINT chk_personal_ranking CHECK (ranking IS NULL OR ranking > 0);