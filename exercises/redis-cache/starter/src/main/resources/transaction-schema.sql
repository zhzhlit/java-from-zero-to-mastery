CREATE TABLE courses (
    id BIGINT PRIMARY KEY,
    title VARCHAR(100) NOT NULL UNIQUE
);
INSERT INTO courses (id, title) VALUES (1, 'Java Basics');
INSERT INTO courses (id, title) VALUES (2, 'Spring Boot');
