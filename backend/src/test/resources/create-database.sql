create database if not exists healthTracker;
use healthTracker;

drop table if exists entry_groups, entries, roles, users;

create table users (
    username varchar(255) primary key,
    password varchar(255)
);

create table roles (
    username varchar(255) not null,
    role varchar(250) not null,
    primary key (username, role),
    foreign key (username) references users(username) on delete cascade
);

CREATE TABLE entry_groups (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  visibility ENUM('PUBLIC', 'PRIVATE') DEFAULT 'PRIVATE',
  created_by VARCHAR(255),

);

CREATE TABLE entries (
  id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  type ENUM('Workout', 'Diet', 'Symptom', 'Other') NOT NULL,
  description TEXT,
  visibility ENUM('PUBLIC', 'PRIVATE') DEFAULT 'PRIVATE',
  date DATE NOT NULL,
  created_by VARCHAR(255),
  group_id INT,

  FOREIGN KEY (group_id) REFERENCES entry_groups(id) ON DELETE SET NULL
);

-- Insert data
insert into users (username, password) values
('admin', '$2a$10$tBTfzHzjmQVKza3VSa5lsOX6/iL93xPVLlLXYg2FhT6a.jb1o6VDq');

insert into roles (username, role) values
('admin', 'ADMIN');



-- Only run deletions later
-- DELETE FROM order_items WHERE order_id IN (SELECT id FROM orders WHERE username = 'admin');
-- DELETE FROM orders WHERE username = 'admin';
-- DELETE FROM roles WHERE username = 'admin';
-- DELETE FROM users WHERE username = 'admin';