# 📚 PTIT Library System

Hệ thống quản lý thư viện PTIT với **Backend REST API** (Spring Boot + JWT) và **Frontend SPA** (HTML/CSS/JS).

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)
![Java](https://img.shields.io/badge/Java-17-orange)
![JWT](https://img.shields.io/badge/Auth-JWT-blue)

---

## 🚀 Công nghệ sử dụng

### Backend

| Công nghệ        | Phiên bản | Mô tả               |
| ---------------- | --------- | ------------------- |
| Spring Boot      | 3.2.0     | REST API Framework  |
| Spring Security  | 6.x       | JWT Authentication  |
| Spring Data JPA  | 3.x       | Database ORM        |
| Spring WebSocket | 3.x       | Real-time messaging |
| MySQL            | 8.x       | Database            |
| jjwt             | 0.12.3    | JWT Library         |

### Frontend

| Công nghệ      | Mô tả                   |
| -------------- | ----------------------- |
| HTML5/CSS3     | Giao diện người dùng    |
| Vanilla JS     | Single Page Application |
| Material Icons | Icon system             |
| Inter Font     | Typography              |

---

## 📁 Cấu trúc dự án

```
PTITLibrary/
├── 📂 backend (src/)
│   └── main/java/com/ptit/library/
│       ├── PTITLibraryApplication.java
│       ├── config/
│       │   ├── SecurityConfig.java      # JWT + CORS
│       │   ├── WebConfig.java           # Web config
│       │   └── WebSocketConfig.java
│       ├── controller/api/              # REST Controllers
│       │   ├── AuthRestController.java
│       │   ├── BookRestController.java
│       │   ├── BorrowRestController.java
│       │   ├── UserRestController.java
│       │   ├── NotificationRestController.java
│       │   ├── MessageRestController.java
│       │   ├── FriendRestController.java
│       │   └── RankingRestController.java
│       ├── dto/
│       │   ├── request/                 # LoginRequest, RegisterRequest...
│       │   └── response/                # ApiResponse, AuthResponse...
│       ├── exception/
│       │   └── GlobalExceptionHandler.java
│       ├── model/                       # JPA Entities
│       ├── repository/                  # Data Access
│       ├── security/                    # JWT Components
│       │   ├── JwtTokenProvider.java
│       │   ├── JwtAuthenticationFilter.java
│       │   └── JwtAuthenticationEntryPoint.java
│       ├── service/                     # Business Logic
│       └── util/
│
├── 📂 frontend/
│   ├── index.html                       # SPA Entry
│   ├── css/
│   │   └── main.css                     # Modern CSS
│   ├── js/
│   │   ├── api.js                       # API Service
│   │   └── app.js                       # App Logic
│   ├── images/                          # Ảnh, logo
│   └── data/                            # JSON data
│
├── 📂 database/
│   └── setup_mysql.sql
├── pom.xml
├── API_DOCUMENTATION.md
└── README.md
```

---

## ⚙️ Cài đặt và Chạy

### Yêu cầu

- ☕ JDK 17+
- 📦 Maven 3.6+
- 🗄️ MySQL 8.x
- 🌐 Browser (Chrome, Firefox, Edge)

### Bước 1: Clone dự án

```bash
git clone https://github.com/your-repo/PTITLibrary.git
cd PTITLibrary
```

### Bước 2: Cấu hình Database

```sql
CREATE DATABASE ptitlibrary;

```

Chạy script: `mysql -u root -p ptitlibrary < database/setup_mysql.sql`

### Bước 3: Cấu hình Backend

Sửa file `src/main/resources/application.properties`:

```properties
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
jwt.secret=YOUR_SECRET_KEY_64_CHARS_MIN
```

### Bước 4: Chạy Backend

```bash
mvn clean install
mvn spring-boot:run
```

Backend chạy tại: `http://localhost:8080`

### Bước 5: Chạy Frontend

Mở file `frontend/index.html` trực tiếp trong browser hoặc dùng Live Server.

---

## 🔐 JWT Authentication

```
1. POST /api/auth/login → Nhận accessToken
2. Gửi token trong header: Authorization: Bearer <token>
3. Token hết hạn → POST /api/auth/refresh-token
```

**Ví dụ:**

```bash
# Đăng nhập
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"B21DCCN001","password":"123456"}'

# Gọi API với token
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 📡 API Endpoints

| Nhóm             | Endpoint                                            | Mô tả      |
| ---------------- | --------------------------------------------------- | ---------- |
| 🔑 Auth          | `/api/auth/login, register, forgot-password`        | Xác thực   |
| 👤 Users         | `/api/users/me, /{username}`                        | Người dùng |
| 📖 Books         | `/api/books, /search, /{id}`                        | Sách       |
| 📋 Borrows       | `/api/borrows, /records, /{id}/issue, /{id}/return` | Mượn sách  |
| 🔔 Notifications | `/api/notifications, /unread, /{id}/read`           | Thông báo  |
| 💬 Messages      | `/api/messages, /conversation/{userId}`             | Tin nhắn   |
| 👥 Friends       | `/api/friends, /request, /accept, /reject`          | Bạn bè     |
| 🏆 Ranking       | `/api/ranking`                                      | Xếp hạng   |

> 📄 Chi tiết: [API_DOCUMENTATION.md](API_DOCUMENTATION.md)

---

## 📦 Response Format

```json
// Success
{ "success": true, "message": "OK", "data": {...} }

// Error
{ "success": false, "message": "Lỗi", "errorCode": 400 }

// Pagination
{ "success": true, "data": { "items": [], "page": 1, "totalPages": 5 } }
```

---

## 🗃️ Database

| Bảng          | Mô tả               |
| ------------- | ------------------- |
| users         | Tài khoản đăng nhập |
| students      | Thông tin sinh viên |
| books         | Danh sách sách      |
| book_records  | Phiếu mượn sách     |
| messages      | Tin nhắn            |
| notifications | Thông báo           |
| friends       | Quan hệ bạn bè      |

---

## 🐛 Troubleshooting

| Lỗi                 | Giải pháp                                  |
| ------------------- | ------------------------------------------ |
| 401 Unauthorized    | Kiểm tra token trong header                |
| CORS Error          | Backend đã cấu hình CORS cho tất cả origin |
| Database Connection | Kiểm tra MySQL đang chạy                   |

---

## 👨‍💻 Tác giả

**PTIT Library Team** - 📧 uchihason5@gmail.com
