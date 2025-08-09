# 🧾 SmartBudget Redis Integration

## 📌 Giới thiệu
SmartBudget là một project luyện tập **Spring Boot + MongoDB + Redis** để:
- Cache kết quả phân tích chi tiêu
- Cache gợi ý ngân sách
- Reminder nhắc nhở user chưa cập nhật chi tiêu
- Quản lý session/token và job control lock

---

## 🎯 Mục tiêu Redis trong project

| Use-case | Key pattern | Data type | TTL | Mục đích |
|----------|-------------|-----------|-----|----------|
| Cache phân tích chi tiêu | `user:summary:{userId}:{month}` | String JSON | 1 ngày | Giảm load DB khi xem thống kê |
| Cache gợi ý ngân sách | `user:suggestion:{userId}` | String JSON | 1 ngày | Không tính lại suggestion mỗi lần |
| Reminder nhắc nhở user | `reminder:{userId}` | String/Set | 1 ngày | Chỉ gửi reminder 1 lần/ngày |
| Session/token | `session:{token}` | Hash/String | Theo session | Lưu thông tin đăng nhập |
| Job control lock | `job:lock:{jobName}` | String | Theo job | Tránh chạy trùng cron job |

---

## 📂 MongoDB Collections

### 1. `users`
```json
{
  "_id": "66b0f8f9c1e123456789abcd",
  "username": "sage",
  "email": "sage@example.com",
  "passwordHash": "...",
  "createdAt": "2025-08-09T10:00:00Z"
}
```

### 2. `expenses`
```json
{
  "_id": "66b0f901c1e123456789abce",
  "userId": "66b0f8f9c1e123456789abcd",
  "category": "Food",
  "amount": 120000,
  "currency": "VND",
  "note": "Lunch with friends",
  "date": "2025-08-08",
  "createdAt": "2025-08-08T12:00:00Z"
}
```

### 3. `categories`
```json
{
  "_id": "66b0f90cc1e123456789abcf",
  "name": "Food",
  "description": "Meals, snacks, drinks",
  "userId": null
}
```

### 4. `budget_plans`
```json
{
  "_id": "66b0f917c1e123456789abd0",
  "userId": "66b0f8f9c1e123456789abcd",
  "month": "2025-08",
  "plannedAmount": 5000000,
  "createdAt": "2025-08-01T00:00:00Z"
}
```

---

## 📡 API Endpoints

### Expense CRUD

| Method   | Endpoint             | Mô tả                |
| -------- | -------------------- | -------------------- |
| `POST`   | `/api/expenses`      | Tạo mới chi tiêu     |
| `GET`    | `/api/expenses/{id}` | Lấy chi tiêu theo ID |
| `PUT`    | `/api/expenses/{id}` | Cập nhật chi tiêu    |
| `DELETE` | `/api/expenses/{id}` | Xoá chi tiêu         |

### Budget Analysis

| Method   | Endpoint                            | Mô tả                                |
| -------- | ----------------------------------- | ------------------------------------ |
| `GET`    | `/api/budget/summary/{month}`       | Lấy phân tích chi tiêu (Redis cache) |
| `DELETE` | `/api/budget/summary/{month}/cache` | Xoá cache phân tích tháng            |

### Budget Suggestion

| Method | Endpoint                 | Mô tả                                  |
| ------ | ------------------------ | -------------------------------------- |
| `GET`  | `/api/budget/suggestion` | Lấy gợi ý ngân sách (Redis @Cacheable) |

### Reminder

| Method | Endpoint                 | Mô tả                               |
| ------ | ------------------------ | ----------------------------------- |
| `POST` | `/api/reminder/{userId}` | Gửi reminder nếu chưa gửi hôm nay   |
| `GET`  | `/api/reminder/{userId}` | Kiểm tra user đã nhận reminder chưa |

### Auth

| Method | Endpoint           | Mô tả                                    |
| ------ | ------------------ | ---------------------------------------- |
| `POST` | `/api/auth/login`  | Đăng nhập và lưu session/token vào Redis |
| `POST` | `/api/auth/logout` | Xoá session/token khỏi Redis             |
| `GET`  | `/api/auth/me`     | Lấy thông tin user từ Redis              |

---

## 🛠 Tech Stack

- Spring Boot 3.5.4
- MongoDB
- Redis 7+
- Maven
- Lombok
- Spring Cache
- Docker, Docker Compose (chạy Redis)

---

## 🚀 Cài đặt nhanh

### 1. Cấu hình `docker-compose`
```yaml
services:
  redis:
    image: redis:7-alpine
    container_name: redis
    restart: unless-stopped
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    command: redis-server --appendonly yes

  redis-insight:
    image: redis/redisinsight:latest
    container_name: redis-insight
    restart: unless-stopped
    ports:
      - "5540:5540"
    volumes:
      - redisinsight-data:/data

volumes:
  redis-data:
  redisinsight-data:
```

### 2. Cấu hình `application.yml`
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/smartbudget
  redis:
    host: localhost
    port: 6379
```

---

## 📚 Tài liệu tham khảo

- [Redis Quick Start](https://redis.io/docs/latest/)
-	[Baeldung Spring Redis](https://www.baeldung.com/spring-data-redis-tutorial)
- [Spring Cache with Redis](https://docs.spring.io/spring-framework/reference/integration/cache.html)
