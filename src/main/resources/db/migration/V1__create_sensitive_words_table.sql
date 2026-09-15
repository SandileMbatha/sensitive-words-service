-- Sensitive words table.
-- Assumes the default SQL Server collation (SQL_Latin1_General_CP1_CI_AS, case-insensitive),
-- so the UNIQUE constraint on "word" already enforces case-insensitive uniqueness.
CREATE TABLE sensitive_words (
    id         BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    word       NVARCHAR(255)        NOT NULL,
    created_at DATETIME2            NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2            NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT UQ_sensitive_words_word UNIQUE (word)
);
