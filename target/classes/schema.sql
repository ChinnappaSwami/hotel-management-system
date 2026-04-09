CREATE DATABASE IF NOT EXISTS hotel_management;
USE hotel_management;

CREATE TABLE IF NOT EXISTS rooms (
    room_id INT PRIMARY KEY AUTO_INCREMENT,
    room_number VARCHAR(10) NOT NULL UNIQUE,
    room_type VARCHAR(20) NOT NULL,
    price_per_day DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
);

CREATE TABLE IF NOT EXISTS customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20) NOT NULL,
    address VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bookings (
    booking_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    room_id INT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    number_of_days INT NOT NULL,
    tax_percent DECIMAL(5,2) DEFAULT 0,
    total_amount DECIMAL(10,2) NOT NULL,
    is_checked_out TINYINT(1) NOT NULL DEFAULT 0,
    booked_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bookings_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    CONSTRAINT fk_bookings_room FOREIGN KEY (room_id) REFERENCES rooms(room_id)
);

INSERT INTO rooms (room_number, room_type, price_per_day, status) VALUES
('101', 'SINGLE', 1500.00, 'AVAILABLE'),
('102', 'SINGLE', 1500.00, 'AVAILABLE'),
('103', 'SINGLE', 1500.00, 'AVAILABLE'),
('104', 'SINGLE', 1600.00, 'AVAILABLE'),
('105', 'SINGLE', 1600.00, 'AVAILABLE'),
('201', 'DOUBLE', 2500.00, 'AVAILABLE'),
('202', 'DOUBLE', 2500.00, 'AVAILABLE'),
('203', 'DOUBLE', 2700.00, 'AVAILABLE'),
('204', 'DOUBLE', 2700.00, 'AVAILABLE'),
('205', 'DOUBLE', 2800.00, 'AVAILABLE'),
('301', 'SUITE',  4000.00, 'AVAILABLE'),
('302', 'SUITE',  4000.00, 'AVAILABLE'),
('303', 'SUITE',  4500.00, 'AVAILABLE'),
('304', 'SUITE',  4500.00, 'AVAILABLE'),
('305', 'SUITE',  5000.00, 'AVAILABLE');

INSERT INTO customers (full_name, email, phone, address) VALUES
('Aarav Sharma',   'aarav.sharma@gmail.com',   '9876543210', '12 MG Road, Mumbai, Maharashtra'),
('Priya Menon',    'priya.menon@gmail.com',    '9123456780', '45 Anna Salai, Chennai, Tamil Nadu'),
('Rohan Kapoor',   'rohan.kapoor@mail.com',    '9988776655', '7 Connaught Place, New Delhi'),
('Sneha Iyer',     'sneha.iyer@mail.com',      '8899001122', '23 Brigade Road, Bengaluru, Karnataka'),
('Vikram Patel',   'vikram.patel@gmail.com',   '9001234567', '8 Ashram Road, Ahmedabad, Gujarat');

INSERT INTO bookings (customer_id, room_id, check_in_date, check_out_date, number_of_days, tax_percent, total_amount) VALUES
(1, 1,  '2026-04-01', '2026-04-04', 3,  18.00,  5310.00);

INSERT INTO bookings (customer_id, room_id, check_in_date, check_out_date, number_of_days, tax_percent, total_amount) VALUES
(2, 7,  '2026-04-02', '2026-04-07', 5,  18.00, 14750.00);

INSERT INTO bookings (customer_id, room_id, check_in_date, check_out_date, number_of_days, tax_percent, total_amount) VALUES
(3, 11, '2026-04-03', '2026-04-05', 2,  18.00,  9440.00);

INSERT INTO bookings (customer_id, room_id, check_in_date, check_out_date, number_of_days, tax_percent, total_amount) VALUES
(4, 3,  '2026-04-05', '2026-04-09', 4,  12.00,  6720.00);

INSERT INTO bookings (customer_id, room_id, check_in_date, check_out_date, number_of_days, tax_percent, total_amount) VALUES
(5, 8,  '2026-04-06', '2026-04-09', 3,  18.00,  9558.00);

INSERT INTO bookings (customer_id, room_id, check_in_date, check_out_date, number_of_days, tax_percent, total_amount) VALUES
(1, 12, '2026-04-15', '2026-04-22', 7,  18.00, 33040.00);

INSERT INTO bookings (customer_id, room_id, check_in_date, check_out_date, number_of_days, tax_percent, total_amount) VALUES
(2, 4,  '2026-04-20', '2026-04-22', 2,  12.00,  3584.00);

