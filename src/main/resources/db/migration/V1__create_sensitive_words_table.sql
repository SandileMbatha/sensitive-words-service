-- Sensitive words table.
-- so the UNIQUE constraint on "name" already enforces case-insensitive uniqueness.
CREATE TABLE sensitive_words (
    id         BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    name       NVARCHAR(255)        NOT NULL,
    created_at DATETIME2            NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2            NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT UQ_sensitive_words_name UNIQUE (name)
);
