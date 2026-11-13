CREATE TABLE published_courses (
    id BIGINT PRIMARY KEY CHECK (id > 0),
    title VARCHAR(200) NOT NULL
);

CREATE TABLE publication_outbox (
    sequence_no BIGINT GENERATED ALWAYS AS IDENTITY UNIQUE,
    event_id CHAR(36) PRIMARY KEY,
    destination VARCHAR(160) NOT NULL,
    course_id BIGINT NOT NULL REFERENCES published_courses(id),
    title VARCHAR(200) NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    stream_id VARCHAR(50),
    attempts INTEGER NOT NULL DEFAULT 0 CHECK (attempts >= 0),
    error_code VARCHAR(40),
    CHECK ((published = FALSE AND stream_id IS NULL) OR (published = TRUE AND stream_id IS NOT NULL))
);
CREATE INDEX idx_publication_pending ON publication_outbox(destination, published, attempts, sequence_no);
