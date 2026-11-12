CREATE TABLE inbox_receipts (
    consumer_group VARCHAR(64) NOT NULL,
    event_id CHAR(36) NOT NULL,
    course_id BIGINT NOT NULL CHECK (course_id > 0),
    title VARCHAR(200) NOT NULL,
    PRIMARY KEY (consumer_group, event_id)
);

CREATE TABLE course_notifications (
    consumer_group VARCHAR(64) NOT NULL,
    event_id CHAR(36) NOT NULL,
    course_id BIGINT NOT NULL CHECK (course_id > 0),
    title VARCHAR(200) NOT NULL,
    PRIMARY KEY (consumer_group, event_id),
    FOREIGN KEY (consumer_group, event_id) REFERENCES inbox_receipts(consumer_group, event_id)
);
