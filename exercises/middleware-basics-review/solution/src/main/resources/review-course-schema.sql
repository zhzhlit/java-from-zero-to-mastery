CREATE TABLE review_course_details (
    course_id BIGINT PRIMARY KEY REFERENCES published_courses(id),
    description VARCHAR(4000) NOT NULL,
    course_level VARCHAR(20) NOT NULL CHECK (course_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED'))
);
