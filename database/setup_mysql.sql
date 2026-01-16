-- =============================================
-- PTIT Library Database Setup for MySQL
-- =============================================
-- Sử dụng script này để tạo database trong phpMyAdmin/MySQL
-- Thực hiện: Copy toàn bộ và chạy trong phpMyAdmin SQL tab

-- Tạo database (nếu chưa tạo thủ công)
CREATE DATABASE IF NOT EXISTS PtitLibrary 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE PtitLibrary;

-- =============================================
-- 1. Bảng Students (Sinh viên)
-- =============================================
CREATE TABLE IF NOT EXISTS Students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_code VARCHAR(20) UNIQUE NOT NULL,
    full_name VARCHAR(100),
    date_of_birth DATE,
    gender VARCHAR(20),
    enrollment_year INT,
    major VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    address VARCHAR(255),
    gpa DECIMAL(3,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 2. Bảng Users (Người dùng)
-- =============================================
CREATE TABLE IF NOT EXISTS Users (
    username VARCHAR(20) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    role VARCHAR(20) DEFAULT 'USER',
    avatar VARCHAR(255) DEFAULT '/images/avatar.jpg',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 3. Bảng Books (Sách)
-- =============================================
CREATE TABLE IF NOT EXISTS Books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    book_code VARCHAR(20) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255),
    publisher VARCHAR(100),
    published_year INT,
    isbn VARCHAR(20),
    category VARCHAR(50),
    total_copies INT DEFAULT 0,
    copies_available INT DEFAULT 0,
    description TEXT,
    cover_image VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (copies_available >= 0),
    CHECK (copies_available <= total_copies)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 4. Bảng BorrowRecords (Lịch sử mượn sách)
-- =============================================
CREATE TABLE IF NOT EXISTS BorrowRecords (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_code VARCHAR(20) NOT NULL,
    book_id INT NOT NULL,
    borrow_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    status VARCHAR(50) DEFAULT 'Đang chờ',
    fine_amount DECIMAL(10,2) DEFAULT 0.00,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_code) REFERENCES Students(student_code) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES Books(id) ON DELETE CASCADE,
    INDEX idx_student (student_code),
    INDEX idx_book (book_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 5. Bảng Messages (Tin nhắn)
-- =============================================
CREATE TABLE IF NOT EXISTS Messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id VARCHAR(20) NOT NULL,
    receiver_id VARCHAR(20) NOT NULL,
    content TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    message_type VARCHAR(20) DEFAULT 'text',
    attachment_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES Users(username) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES Users(username) ON DELETE CASCADE,
    INDEX idx_sender (sender_id),
    INDEX idx_receiver (receiver_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 6. Bảng Notifications (Thông báo)
-- =============================================
CREATE TABLE IF NOT EXISTS Notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL,
    title VARCHAR(255),
    content TEXT,
    notification_type VARCHAR(50),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(username) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 7. Bảng Friendships (Quan hệ bạn bè)
-- =============================================
-- cap nhat bang moi
-- cap nhat bang moi
-- cap nhat bang moi
-- cap nhat bang moi

create table friendships
(
    id                  int auto_increment
        primary key,
    created_at          datetime(6)  null,
    friend_id           varchar(20)  not null,
    last_interaction_at datetime(6)  null,
    requested_by        varchar(20)  null,
    responded_at        datetime(6)  null,
    status              varchar(20)  not null,
    u_max               varchar(255) null,
    u_min               varchar(255) null,
    user_id             varchar(20)  not null,
    constraint uq_friendships_unordered
        unique (u_min, u_max)
);

create index idx_friendships_status_friend
    on friendships (friend_id, status);

create index idx_friendships_status_user
    on friendships (user_id, status);

-- =============================================
-- DỮ LIỆU MẪU (Sample Data)
-- =============================================

-- Insert Students
INSERT INTO Students (student_code, full_name, date_of_birth, gender, enrollment_year, major, email, gpa) VALUES
('B23DCCN393', 'Nguyễn Việt Huy', '2004-05-15', 'Nam', 2023, 'CNTT', 'HuyNV.B23DCCN393@stu.ptit.edu.vn', 3.45),
('B23DCCN155', 'Trần Thị Mai', '2004-03-20', 'Nữ', 2023, 'CNTT', 'MaiTT.B23DCCN155@stu.ptit.edu.vn', 3.67),
('B23DCCN256', 'Lê Văn Nam', '2004-07-10', 'Nam', 2023, 'KHMT', 'NamLV.B23DCCN256@stu.ptit.edu.vn', 3.23),
('B23DCCN489', 'Phạm Thị Lan', '2004-11-25', 'Nữ', 2023, 'HTTT', 'LanPT.B23DCCN489@stu.ptit.edu.vn', 3.89),
('B23DCAT067', 'Hoàng Văn Tùng', '2004-01-30', 'Nam', 2023, 'ATTT', 'TungHV.B23DCAT067@stu.ptit.edu.vn', 3.56),
('B23DCCN720', 'Nguyễn Duy Sơn', '2005-04-21', 'Nam', 2023, 'CNTT', 'uchihason5@gmail.com', 3.50);

-- Insert Users
INSERT INTO Users (username, password, name, email, role) VALUES
('B23DCCN393', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Nguyễn Việt Huy', 'HuyNV.B23DCCN393@stu.ptit.edu.vn', 'USER'),
('B23DCCN155', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Trần Thị Mai', 'MaiTT.B23DCCN155@stu.ptit.edu.vn', 'USER'),
('B23DCCN256', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Lê Văn Nam', 'NamLV.B23DCCN256@stu.ptit.edu.vn', 'USER'),
('B23DCCN720', '$2a$10$FsEOLsbA91RZ0V9Pw1KoeOr4KgCpo8RFfG.l7KTBneSQI9r.cDavu', 'Nguyễn Duy Sơn', 'uchihason5@gmail.com', 'USER'),
('admin', '$2a$10$FsEOLsbA91RZ0V9Pw1KoeOr4KgCpo8RFfG.l7KTBneSQI9r.cDavu', 'Administrator', 'admin@ptit.edu.vn', 'ADMIN');
-- Password mẫu: "12345678" (đã mã hóa BCrypt)
-- Avatar sẽ được trigger tự động set dựa trên gender của student (Nam: avatar.jpg, Nữ: avatar_girl.jpg)
-- Admin giữ avatar mặc định: avatar.jpg

-- Insert Books
INSERT INTO Books (book_code, title, author, publisher, published_year, category, total_copies, copies_available) VALUES
('BK001', 'Toán Giải Tích 1', 'Vũ Gia Tê', 'Bộ GD&ĐT', 2019, 'Toán học', 50, 45),
('BK002', 'Lịch sử Đảng CSVN', 'Đào Mạnh Ninh', 'Bộ TT&TT', 2021, 'Chính trị', 30, 28),
('BK003', 'Cấu trúc dữ liệu và giải thuật', 'Trần Hạnh Nhi', 'NXB ĐHQG', 2020, 'Tin học', 40, 35),
('BK004', 'Lập trình hướng đối tượng Java', 'Nguyễn Quang Hoan', 'NXB Thống Kê', 2021, 'Tin học', 35, 30),
('BK005', 'Cơ sở dữ liệu', 'Nguyễn Tuấn Anh', 'NXB Giáo dục', 2020, 'Tin học', 45, 40),
('BK006', 'Tiếng Anh chuyên ngành CNTT', 'English Dept', 'PTIT Press', 2023, 'Ngoại ngữ', 25, 20),
('BK007', 'Tư tưởng Hồ Chí Minh', 'Bộ GD&ĐT', 'NXB CTQG', 2021, 'Chính trị', 60, 55),
('BK008', 'Mạng máy tính', 'Trương Công Tuấn', 'NXB KHKT', 2019, 'Tin học', 38, 33),
('BK009', 'An toàn thông tin', 'Phạm Văn At', 'NXB Thông tin', 2023, 'Tin học', 30, 25),
('BK010', 'Trí tuệ nhân tạo', 'Nguyễn Thanh Thủy', 'NXB ĐH Quốc Gia', 2023, 'Tin học', 28, 24);

-- Insert Sample Borrow Records
INSERT INTO BorrowRecords (student_code, book_id, borrow_date, due_date, status) VALUES
('B23DCCN393', 1, '2024-11-01', '2024-11-15', 'Đang chờ'),
('B23DCCN393', 3, '2024-10-25', '2024-11-08', 'Đang mượn'),
('B23DCCN155', 2, '2024-11-02', '2024-11-16', 'Đang chờ'),
('B23DCCN256', 4, '2024-10-20', '2024-11-03', 'Quá hạn');

-- Insert Sample Notifications
INSERT INTO Notifications (user_id, title, content, notification_type) VALUES
('B23DCCN393', 'Sách sắp đến hạn trả', 'Bạn có sách "Cấu trúc dữ liệu và giải thuật" sẽ đến hạn trả vào ngày 08/11/2024', 'due_soon'),
('B23DCCN256', 'Sách quá hạn', 'Bạn có sách quá hạn trả. Vui lòng trả sách để tránh phạt.', 'overdue');

-- Insert Default Friendships (all users are friends with admin by default)
INSERT INTO Friendships (user_id, friend_id, status, requested_by, created_at, responded_at, last_interaction_at) VALUES
('B23DCCN393', 'admin', 'accepted', 'admin', NOW(), NOW(), NOW()),
('B23DCCN155', 'admin', 'accepted', 'admin', NOW(), NOW(), NOW()),
('B23DCCN256', 'admin', 'accepted', 'admin', NOW(), NOW(), NOW()),
('B23DCCN720', 'admin', 'accepted', 'admin', NOW(), NOW(), NOW());

-- =============================================
-- VIEWS & STORED PROCEDURES (Tùy chọn)
-- =============================================

-- View: Thống kê sách được mượn nhiều nhất
CREATE OR REPLACE VIEW v_most_borrowed_books AS
SELECT 
    b.id,
    b.book_code,
    b.title,
    b.author,
    COUNT(br.id) as borrow_count
FROM Books b
LEFT JOIN BorrowRecords br ON b.id = br.book_id
GROUP BY b.id, b.book_code, b.title, b.author
ORDER BY borrow_count DESC;

-- View: Danh sách sinh viên có sách quá hạn
CREATE OR REPLACE VIEW v_overdue_students AS
SELECT 
    s.student_code,
    s.full_name,
    b.title as book_title,
    br.borrow_date,
    br.due_date,
    DATEDIFF(CURDATE(), br.due_date) as days_overdue
FROM BorrowRecords br
JOIN Students s ON br.student_code = s.student_code
JOIN Books b ON br.book_id = b.id
WHERE br.status = 'Quá hạn' 
  AND br.return_date IS NULL;