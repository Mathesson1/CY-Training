BEGIN;
DROP TABLE IF EXISTS "Account";
DROP TABLE IF EXISTS "Account_Email_Verification";
DROP TABLE IF EXISTS "Account_Session";
DROP TABLE IF EXISTS "Account_Session_Refresh";
DROP TABLE IF EXISTS "Account_Role";
DROP TABLE IF EXISTS "Question";
DROP TABLE IF EXISTS "Question_Deletion";
DROP TABLE IF EXISTS "Year_Semester";
DROP TABLE IF EXISTS "Curriculum";
DROP TABLE IF EXISTS "Subject";
DROP TABLE IF EXISTS "Curriculum_Subject";
DROP TABLE IF EXISTS "Subject_Semester";
DROP TABLE IF EXISTS "Question_Subject";
DROP TABLE IF EXISTS "Question_Rejection";
DROP TABLE IF EXISTS "Account_Subject_Verified";
DROP TABLE IF EXISTS "Account_Moderation";
DROP TYPE IF EXISTS role_enum;
DROP TYPE IF EXISTS question_status_enum;
-- Enum types
CREATE TYPE role_enum AS ENUM (
    'admin',
    'moderator',
    'global_verify'
);
CREATE TYPE question_status_enum AS ENUM ('not_verified', 'rejected', 'verified');
CREATE TABLE "Account"(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    -- a random number that is no the id, to
    account_number uuid NOT NULL DEFAULT gen_random_uuid(),
    primary_email VARCHAR(128) NOT NULL UNIQUE,
    secondary_email VARCHAR(128),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    password TEXT NOT NULL,
    username VARCHAR(128) NOT NULL,
    description VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    -- email format validation
    CONSTRAINT account_primary_email_format_constraint CHECK (
        primary_email ~* '^[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,}$'
    ),
    CONSTRAINT account_secondary_email_format_constraint CHECK (
        secondary_email ~* '^[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,}$'
    ),
    -- prevent empty names
    CONSTRAINT account_username_not_blank_constraint CHECK (length(trim(username)) > 0),
    -- check password (hashed) is not weirdly short
    CONSTRAINT account_password_length_constraint CHECK (length(password) >= 32)
);
-- Contains a hashed verification token and expiration date, to validate the primary_email of an account
CREATE TABLE "Account_Email_Verification" (
    token VARCHAR(32) NOT NULL PRIMARY KEY,
    account_id BIGINT UNIQUE REFERENCES "Account"(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
-- Account session
-- Used to save user session for a "short" period of time
CREATE TABLE "Account_Session" (
    token VARCHAR(32) NOT NULL PRIMARY KEY,
    account_id BIGINT REFERENCES "Account"(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
-- Same as account session, but those are longer lived, used when the "remember me" is checked on login
CREATE TABLE "Account_Session_Refresh" (
    token VARCHAR(32) NOT NULL PRIMARY KEY,
    account_id BIGINT REFERENCES "Account"(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
-- Roles
-- Used to link account to roles
CREATE TABLE "Account_Role" (
    account_id BIGINT REFERENCES "Account"(id) ON DELETE CASCADE,
    role role_enum NOT NULL,
    PRIMARY KEY (account_id, role)
);
-- Questions structure
-- The basic question informations. Often accessed informations are in columns, the rest are in the data column, in JSON format.
CREATE TABLE "Question" (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    author BIGINT REFERENCES "Account"(id) ON DELETE
    SET NULL,
        is_private BOOLEAN NOT NULL DEFAULT FALSE,
        is_anonymous BOOLEAN NOT NULL DEFAULT FALSE,
        status question_status_enum NOT NULL DEFAULT 'not_verified',
        data JSONB NOT NULL,
        created_at TIMESTAMP NOT NULL DEFAULT now(),
        updated_at TIMESTAMP NOT NULL DEFAULT now()
);
-- Waiting list for question deletion
CREATE TABLE "Question_Deletion" (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    question_id BIGINT REFERENCES "Question"(id) ON DELETE CASCADE,
    requested_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE TABLE "Year_Semester"(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    year_number INT NOT NULL,
    semester INT NOT NULL CHECK (semester IN (1, 2)),
    UNIQUE (year_number, semester)
);
CREATE TABLE "Curriculum" (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);
CREATE TABLE "Subject"(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);
CREATE TABLE "Curriculum_Subject" (
    curriculum_id BIGINT REFERENCES "Curriculum"(id) ON DELETE CASCADE,
    subject_id BIGINT REFERENCES "Subject"(id) ON DELETE CASCADE,
    PRIMARY KEY (curriculum_id, subject_id)
);
CREATE TABLE "Subject_Semester"(
    subject_id BIGINT REFERENCES "Subject"(id) ON DELETE CASCADE,
    semester_id BIGINT REFERENCES "Year_Semester"(id) ON DELETE CASCADE,
    PRIMARY KEY (subject_id, semester_id)
);
CREATE TABLE "Question_Subject"(
    question_id BIGINT REFERENCES "Question"(id) ON DELETE CASCADE,
    subject_id BIGINT REFERENCES "Subject"(id) ON DELETE CASCADE,
    PRIMARY KEY (question_id, subject_id)
);
CREATE TABLE "Question_Rejection" (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    question_id BIGINT UNIQUE REFERENCES "Question"(id) ON DELETE CASCADE,
    moderator_id BIGINT NOT NULL REFERENCES "Account"(id),
    rejected_at TIMESTAMP NOT NULL DEFAULT now(),
    reason TEXT NOT NULL
);
-- Link an account to the subject it's verified in
-- Global verfied account is a role, see enum above
CREATE TABLE "Account_Subject_Verified" (
    account_id BIGINT REFERENCES "Account"(id) ON DELETE CASCADE,
    subject_id BIGINT REFERENCES "Subject"(id) ON DELETE CASCADE,
    verified_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (account_id, subject_id)
);
-- Bans/Warn
CREATE TABLE "Account_Moderation" (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES "Account"(id) ON DELETE CASCADE,
    moderator_id BIGINT NOT NULL REFERENCES "Account"(id),
    reason TEXT NOT NULL,
    banned_at TIMESTAMP NOT NULL DEFAULT now(),
    -- expires_at is NULL = permanent ban;
    expires_at TIMESTAMP NULL,
    -- unbanned_at is banned_at = WARN
    unbanned_at TIMESTAMP NULL,
    unbanned_by BIGINT REFERENCES "Account"(id),
    CHECK (
        expires_at IS NULL
        OR expires_at > banned_at
    ),
    CHECK (
        unbanned_at IS NULL
        OR unbanned_at >= banned_at
    )
);
-- equivalent of ON UPDATE on mysql
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS TRIGGER AS $$ BEGIN NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER trigger_set_updated_at BEFORE
UPDATE ON "Account" FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trigger_question_set_updated_at BEFORE
UPDATE ON "Question" FOR EACH ROW EXECUTE FUNCTION set_updated_at();
-- index to speed up primary_email lookups
CREATE UNIQUE INDEX account_email_index ON "Account"(primary_email);
COMMIT;