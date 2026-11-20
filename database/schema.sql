CREATE DATABASE IF NOT EXISTS smartfind CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smartfind;
CREATE TABLE IF NOT EXISTS students (
 student_id BIGINT AUTO_INCREMENT PRIMARY KEY,
 full_name VARCHAR(100) NOT NULL,
 email VARCHAR(150) NOT NULL UNIQUE
);
CREATE TABLE IF NOT EXISTS items (
 item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
 student_id BIGINT NOT NULL,
 item_type ENUM('LOST','FOUND') NOT NULL,
 item_name VARCHAR(100) NOT NULL,
 category VARCHAR(50) NOT NULL,
 color VARCHAR(40) NOT NULL,
 location VARCHAR(100) NOT NULL,
 description VARCHAR(500) NOT NULL,
 incident_date DATE NOT NULL,
 status ENUM('OPEN','RETURNED') NOT NULL DEFAULT 'OPEN',
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT fk_items_student FOREIGN KEY(student_id) REFERENCES students(student_id),
 INDEX idx_items_type_status(item_type,status),
 INDEX idx_items_category(category)
);
