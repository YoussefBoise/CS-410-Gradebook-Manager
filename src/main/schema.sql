CREATE DATABASE IF NOT EXISTS gradebook;
USE gradebook;

CREATE TABLE classes (
    class_id INT PRIMARY KEY AUTO_INCREMENT,
    course_number VARCHAR(10) NOT NULL,
    term VARCHAR(5) NOT NULL,
    section_number INT NOT NULL,
    class_description TEXT,
    UNIQUE (course_number, term, section_number)
);

CREATE TABLE categories (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    category_type VARCHAR(20) NOT NULL,
    category_weight INT NOT NULL,
    class_id INT NOT NULL,

    FOREIGN KEY (class_id) REFERENCES classes(class_id) 
        ON UPDATE CASCADE 
        ON DELETE RESTRICT,
    UNIQUE (category_type, class_id)
);

CREATE TABLE assignments (
    assignment_id INT PRIMARY KEY AUTO_INCREMENT,
    assignment_name VARCHAR(30) NOT NULL,
    assignment_description TEXT,
    points INT NOT NULL,
    class_id INT NOT NULL,
    category_id INT NOT NULL,

    FOREIGN KEY (class_id) REFERENCES classes(class_id) 
        ON UPDATE CASCADE 
        ON DELETE RESTRICT,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) 
        ON UPDATE CASCADE 
        ON DELETE RESTRICT,
    UNIQUE (assignment_name, class_id)
);

CREATE TABLE students (
    student_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    student_name VARCHAR(50) NOT NULL
);

CREATE TABLE enrollment (
    enrollment_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    class_id INT NOT NULL,

    FOREIGN KEY (student_id) REFERENCES students(student_id) 
        ON UPDATE CASCADE 
        ON DELETE RESTRICT,
    FOREIGN KEY (class_id) REFERENCES classes(class_id) 
        ON UPDATE CASCADE 
        ON DELETE RESTRICT,
    UNIQUE (student_id, class_id)
);

CREATE TABLE grades (
    grade_id INT PRIMARY KEY AUTO_INCREMENT,
    enrollment_id INT NOT NULL,
    assignment_id INT NOT NULL,
    assignment_grade DECIMAL,

    FOREIGN KEY (enrollment_id) REFERENCES enrollment(enrollment_id) 
        ON UPDATE CASCADE 
        ON DELETE RESTRICT,
    FOREIGN KEY (assignment_id) REFERENCES assignments(assignment_id) 
        ON UPDATE CASCADE 
        ON DELETE RESTRICT,
    UNIQUE (enrollment_id, assignment_id)
);

CREATE INDEX idx_grade ON grades(assignment_grade); -- Allows queries for grades to be faster. Useful for grade analysis.
CREATE INDEX idx_course_num ON classes(course_number); -- Allows queries to use course numbers instead of ids without slowing significantly.
CREATE INDEX idx_student_name ON students(student_name); -- Allows queries based on names to be faster. Useful if student id is lost.
CREATE INDEX idx_category_type ON categories(category_type); -- Allows queries based on category types to be faster. Useful if classes want to compare category weights.

DELIMITER |

CREATE TRIGGER check_assignment_belong_to_class
BEFORE INSERT ON grades
FOR EACH ROW
BEGIN
    DECLARE num_valid INT DEFAULT 0;

    SELECT COUNT(*) INTO num_valid
    FROM enrollment E
        JOIN assignments A on E.class_id = A.class_id;

    IF num_valid = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Cannot give an assignment to a student which is not enrolled';
    END IF;
END
|

DELIMITER ;
