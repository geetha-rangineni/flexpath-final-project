-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS healthTracker;
USE healthTracker;

-- Drop existing tables if they exist
DROP TABLE IF EXISTS entry_groups, entries, roles, users;

-- Create users table
CREATE TABLE users (
    username VARCHAR(255) PRIMARY KEY,
    password VARCHAR(255)
);

-- Create roles table
CREATE TABLE roles (
    username VARCHAR(255) NOT NULL,
    role VARCHAR(250) NOT NULL,
    PRIMARY KEY (username, role),
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
);

-- Create entry_groups table
CREATE TABLE entry_groups (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    visibility ENUM('PUBLIC', 'PRIVATE') DEFAULT 'PRIVATE',
    created_by VARCHAR(255),
    FOREIGN KEY (created_by) REFERENCES users(username) ON DELETE SET NULL
);

-- Create entries table
CREATE TABLE entries (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    type ENUM('Workout', 'Diet', 'Symptom', 'Other') NOT NULL,
    description TEXT,
    visibility ENUM('PUBLIC', 'PRIVATE') DEFAULT 'PRIVATE',
    date DATE NOT NULL,
    created_by VARCHAR(255),
    group_id INT,
    FOREIGN KEY (created_by) REFERENCES users(username) ON DELETE SET NULL,
    FOREIGN KEY (group_id) REFERENCES entry_groups(id) ON DELETE SET NULL
);

-- Insert initial admin user
INSERT INTO users (username, password) VALUES
('admin', '$2a$10$tBTfzHzjmQVKza3VSa5lsOX6/iL93xPVLlLXYg2FhT6a.jb1o6VDq');

-- Assign admin role
INSERT INTO roles (username, role) VALUES
('admin', 'ADMIN');

