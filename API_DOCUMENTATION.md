# PTIT Library REST API Documentation

## Overview

PTIT Library System REST API sử dụng JWT (JSON Web Token) để xác thực người dùng.

### Base URL

```
http://localhost:8080/api
```

### Authentication

Tất cả các endpoint cần xác thực phải gửi JWT token trong header:

```
Authorization: Bearer <your-jwt-token>
```

---

## 1. Authentication Endpoints

### 1.1 Đăng nhập

**POST** `/api/auth/login`

**Request Body:**

```json
{
  "username": "string",
  "password": "string"
}
```

**Response (200 OK):**

```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "username": "student001",
      "fullName": "Nguyễn Văn A",
      "role": "ROLE_STUDENT",
      "email": "student@ptit.edu.vn"
    }
  }
}
```

### 1.2 Đăng ký

**POST** `/api/auth/register`

**Request Body:**

```json
{
  "studentCode": "B21DCCN001",
  "fullName": "Nguyễn Văn A",
  "email": "student@ptit.edu.vn",
  "phone": "0123456789",
  "password": "password123"
}
```

**Response (201 Created):**

```json
{
  "success": true,
  "message": "Đăng ký thành công. Mã sinh viên là tên đăng nhập của bạn.",
  "data": {
    "username": "B21DCCN001",
    "email": "student@ptit.edu.vn"
  }
}
```

### 1.3 Quên mật khẩu

**POST** `/api/auth/forgot-password?email={email}`

**Response (200 OK):**

```json
{
  "success": true,
  "message": "Mật khẩu mới đã được gửi đến email của bạn"
}
```

### 1.4 Làm mới Token

**POST** `/api/auth/refresh-token`

**Headers:**

```
Authorization: Bearer <current-token>
```

**Response (200 OK):**

```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "new-jwt-token...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  }
}
```

---

## 2. User Endpoints

### 2.1 Lấy thông tin người dùng hiện tại

**GET** `/api/users/me`

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "B21DCCN001",
    "fullName": "Nguyễn Văn A",
    "email": "student@ptit.edu.vn",
    "phone": "0123456789",
    "role": "ROLE_STUDENT",
    "dateOfBirth": "2003-01-15",
    "gender": "Nam",
    "enrollmentYear": 2021,
    "major": "Công nghệ thông tin"
  }
}
```

### 2.2 Lấy thông tin người dùng theo username

**GET** `/api/users/{username}`

### 2.3 Cập nhật thông tin cá nhân

**PUT** `/api/users/me`

**Request Body:**

```json
{
  "fullName": "Nguyễn Văn A",
  "phone": "0987654321",
  "dateOfBirth": "2003-01-15",
  "gender": "Nam"
}
```

### 2.4 Đổi mật khẩu

**PUT** `/api/users/me/password`

**Request Body:**

```json
{
  "currentPassword": "oldPassword123",
  "newPassword": "newPassword456",
  "confirmPassword": "newPassword456"
}
```

### 2.5 Upload avatar

**POST** `/api/users/me/avatar`

**Content-Type:** `multipart/form-data`

**Form Data:**

- `file`: Image file (JPEG, PNG)

---

## 3. Book Endpoints

### 3.1 Lấy danh sách tất cả sách

**GET** `/api/books`

**Response (200 OK):**

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "bookCode": "IT001",
      "title": "Clean Code",
      "author": "Robert C. Martin",
      "publishedYear": 2008,
      "totalCopies": 10,
      "copiesAvailable": 5,
      "publisher": "Prentice Hall"
    }
  ]
}
```

### 3.2 Lấy chi tiết sách

**GET** `/api/books/{id}`

### 3.3 Tìm kiếm sách

**GET** `/api/books/search`

**Query Parameters:**

- `keyword`: Từ khóa tìm kiếm (bắt buộc)
- `filter1`: Bộ lọc 1 (tùy chọn)
- `filter2`: Bộ lọc 2 (tùy chọn)
- `filter3`: Bộ lọc 3 (tùy chọn)

---

## 4. Borrow Endpoints

### 4.1 Đăng ký mượn sách

**POST** `/api/borrows`

**Request Body:**

```json
{
  "bookIds": [1, 2, 3]
}
```

**Response (200 OK):**

```json
{
  "success": true,
  "message": "Đăng ký mượn sách thành công",
  "data": {
    "borrowedCount": 3,
    "failedBookIds": []
  }
}
```

### 4.2 Lấy danh sách phiếu mượn

**GET** `/api/borrows/records`

**Query Parameters:**

- `username`: Username của người mượn (tùy chọn, mặc định là user hiện tại)

**Response (200 OK):**

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "recordId": 1,
      "borrowCode": "1",
      "title": "Clean Code",
      "author": "Robert C. Martin",
      "year": 2008,
      "registerDate": "2024-01-15",
      "dueDate": "2024-01-29",
      "status": "Đang chờ",
      "username": "B21DCCN001"
    }
  ]
}
```

### 4.3 Xác nhận cho mượn (Admin)

**POST** `/api/borrows/{recordId}/issue`

### 4.4 Xác nhận trả sách

**POST** `/api/borrows/{recordId}/return`

---

## 5. Notification Endpoints

### 5.1 Lấy tất cả thông báo

**GET** `/api/notifications`

### 5.2 Lấy thông báo chưa đọc

**GET** `/api/notifications/unread`

### 5.3 Đánh dấu thông báo đã đọc

**PUT** `/api/notifications/{id}/read`

### 5.4 Đánh dấu tất cả đã đọc

**PUT** `/api/notifications/read-all`

### 5.5 Tạo thông báo (Admin)

**POST** `/api/notifications`

**Request Body:**

```json
{
  "title": "Thông báo mới",
  "message": "Nội dung thông báo",
  "recipientUsername": "B21DCCN001"
}
```

---

## 6. Message Endpoints

### 6.1 Gửi tin nhắn

**POST** `/api/messages`

**Request Body:**

```json
{
  "receiverUsername": "B21DCCN002",
  "content": "Xin chào!"
}
```

### 6.2 Lấy cuộc hội thoại

**GET** `/api/messages/conversation/{userId}`

### 6.3 Lấy tin nhắn chưa đọc

**GET** `/api/messages/unread`

### 6.4 Đánh dấu đã đọc

**PUT** `/api/messages/{id}/read`

---

## 7. Friend Endpoints

### 7.1 Lấy danh sách bạn bè

**GET** `/api/friends`

### 7.2 Gửi lời mời kết bạn

**POST** `/api/friends/request`

**Request Body:**

```json
{
  "friendUsername": "B21DCCN002"
}
```

### 7.3 Chấp nhận lời mời

**PUT** `/api/friends/accept/{friendUsername}`

### 7.4 Từ chối lời mời

**DELETE** `/api/friends/reject/{friendUsername}`

### 7.5 Hủy kết bạn

**DELETE** `/api/friends/{friendUsername}`

### 7.6 Lấy lời mời đang chờ

**GET** `/api/friends/pending`

### 7.7 Kiểm tra trạng thái bạn bè

**GET** `/api/friends/status/{username}`

---

## 8. Ranking Endpoints

### 8.1 Lấy bảng xếp hạng

**GET** `/api/ranking`

**Query Parameters:**

- `page`: Số trang (mặc định: 1)
- `size`: Số lượng mỗi trang (mặc định: 20)

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "studentCode": "B21DCCN001",
        "fullName": "Nguyễn Văn A",
        "enrollmentYear": "2021",
        "major": "Công nghệ thông tin",
        "borrowCount": 50,
        "online": false
      }
    ],
    "page": 1,
    "size": 20,
    "totalPages": 5,
    "totalElements": 100,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

---

## Error Response Format

Tất cả lỗi được trả về với format:

```json
{
  "success": false,
  "message": "Mô tả lỗi",
  "errorCode": 400
}
```

### HTTP Status Codes:

- `200 OK`: Thành công
- `201 Created`: Tạo mới thành công
- `400 Bad Request`: Request không hợp lệ
- `401 Unauthorized`: Chưa xác thực
- `403 Forbidden`: Không có quyền
- `404 Not Found`: Không tìm thấy resource
- `500 Internal Server Error`: Lỗi server

---

## WebSocket (Real-time Messaging)

### Endpoint

```
ws://localhost:8080/ws
```

### Subscribe to Messages

```
/user/queue/messages
```

### Send Private Message

```
Destination: /app/chat.private
Payload: {
  "receiver": "username",
  "content": "message content",
  "clientMsgId": "unique-client-id"
}
```
