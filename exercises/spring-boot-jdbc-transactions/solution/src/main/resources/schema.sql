CREATE TABLE courses (
    id BIGINT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    available_seats INTEGER NOT NULL CHECK (available_seats >= 0)
);

CREATE TABLE enrollments (
    course_id BIGINT NOT NULL REFERENCES courses(id),
    student VARCHAR(100) NOT NULL,
    CONSTRAINT uk_enrollment UNIQUE (course_id, student)
);
