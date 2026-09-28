# HỆ THỐNG QUẢN LÝ BÁN HÀNG & BẢO MẬT UTESHOP
> **Môn học:** Lập trình Web (WEBPR330479) - HCMUTE  
> **Giảng viên:** ThS. Nguyễn Hữu Trung  
> **Sinh viên thực hiện:** Đinh Quốc Anh - MSSV: 24133003  
> **Dự án:** [web_assignment_spring_security](file:///d:/uni/hk1nam3/web/web_assignment_spring_security)

---

## 1. Công nghệ & Kiến trúc sử dụng
- **Backend Framework:** Spring Boot 4.1.1 & Spring Security 7 (cấu hình hiện đại theo `SecurityFilterChain`, `DaoAuthenticationProvider(userDetailsService)`).
- **Mapper:** MapStruct 1.6.3 (chuyển đổi Entity sang DTO và ngược lại).
- **Database:** Microsoft SQL Server (kết nối qua `mssql-jdbc` và Spring Data JPA).
- **Lưu trữ hình ảnh:** Cloudinary API tích hợp tự động với cơ chế Fallback lưu trữ cục bộ (`/uploads/`).
- **Xác thực OTP 2 lớp:** `JavaMailSender` + Lưu trữ `OtpToken` với thời hạn 5 phút + In mã OTP ra Console phục vụ test/chấm điểm nhanh.
- **Frontend & Template:** Thymeleaf + Thymeleaf Extras Spring Security 6 + Vanilla CSS hiện đại (Inter / Plus Jakarta Sans & Outfit, Dark/Light palettes, responsive).
- **Quản lý cấu hình bảo mật:** Biến môi trường `.env` tách biệt thông tin nhạy cảm, có mẫu `.env.example` và được bảo vệ bởi `.gitignore`.

---

## 2. Mô hình Dữ liệu (Database Schema)
1. **`roles`**:
   - `id`: Khóa chính
   - `name`: Tên vai trò (`ROLE_ADMIN`, `ROLE_USER`)
2. **`users`**:
   - `id`: Khóa chính
   - `username`: Tên đăng nhập (Unique)
   - `email`: Địa chỉ email (Unique)
   - `password`: Mật khẩu mã hóa BCrypt
   - `full_name`: Họ và tên đầy đủ
   - `images`: Đường dẫn ảnh đại diện (Cloudinary / Local URL)
   - `enabled`: Trạng thái kích hoạt (chờ xác thực OTP khi đăng ký)
   - `role_id`: Khóa ngoại liên kết bảng `roles`
   - `products`: Mối quan hệ **1 User - n Product** (`@OneToMany`)
3. **`otp_tokens`**:
   - `id`: Khóa chính
   - `email`: Email nhận mã
   - `token`: Mã OTP 6 chữ số
   - `type`: Loại mã (`REGISTER`, `FORGOT_PASSWORD`)
   - `expiry_date`: Thời điểm hết hạn (5 phút)
   - `used`: Đã sử dụng hay chưa
4. **`products`**:
   - `id`: Khóa chính
   - `name`: Tên sản phẩm
   - `price`: Giá bán (VNĐ)
   - `description`: Mô tả chi tiết
   - `image`: Đường dẫn hình ảnh (Cloudinary / Local URL)
   - `user_id`: Khóa ngoại liên kết bảng `users` (chủ sở hữu sản phẩm)

---

## 3. Các chức năng đã triển khai chi tiết
### 1. Đăng ký tài khoản & Xác nhận OTP Email
- Truy cập: `/register`
- Nhập họ tên, username, email, mật khẩu, xác nhận mật khẩu và upload ảnh đại diện (lên Cloudinary).
- Hệ thống tạo tài khoản ở trạng thái `enabled = false`, sinh mã OTP 6 chữ số ngẫu nhiên, lưu vào bảng `otp_tokens` và gửi email cho người dùng (đồng thời in trực tiếp ra Console).
- Tự động chuyển hướng sang trang `/verify-otp?email=...&type=REGISTER` để nhập mã kích hoạt tài khoản.
- Hỗ trợ nút **Gửi lại mã OTP** (`/resend-otp`).

### 2. Đăng nhập lưu Session & Hiển thị thông tin Header
- Truy cập: `/login`
- Đăng nhập linh hoạt bằng **Username** hoặc **Email** kèm mật khẩu.
- Lưu Session chuẩn Spring Security với `SecurityContext` và cookie `JSESSIONID`.
- Sau khi đăng nhập, tại `header.html` hiển thị:
  - Ảnh đại diện Avatar (`#authentication.principal.images`)
  - Họ và tên (`#authentication.principal.fullName`)
  - Username (`#authentication.principal.username`)
  - Email (`#authentication.principal.email`)
  - Huy hiệu vai trò (`#authentication.principal.role`)
  - Form Đăng xuất bảo mật có mã CSRF (`/logout`).

### 3. Quên mật khẩu & Gửi OTP qua Email
- Truy cập: `/forgot-password`
- Nhập email để nhận mã OTP khôi phục mật khẩu.
- Chuyển hướng sang `/reset-password?email=...` để nhập mã OTP và đặt mật khẩu mới.
- Mật khẩu mới được mã hóa BCrypt và cập nhật trực tiếp vào cơ sở dữ liệu.

### 4. Quản lý Người Dùng (Admin CRUD, Phân trang, Tìm kiếm, Đếm số sản phẩm)
- Truy cập: `/admin/users` (Chỉ tài khoản `ROLE_ADMIN` mới có quyền truy cập, các tài khoản khác bị chặn chuyển sang trang 403 Forbidden).
- **Tìm kiếm:** Theo username, email hoặc họ tên.
- **Phân trang:** Phân trang Spring Data JPA linh hoạt.
- **Thống kê:**
  - Tổng số người dùng hệ thống (`countUsers`).
  - Tổng số sản phẩm hệ thống (`countProducts`).
  - **Đếm số lượng sản phẩm của từng người dùng** (`user.productCount`) hiển thị dạng huy hiệu trực quan.
- **Thao tác:**
  - Thêm người dùng mới (`/admin/users/new`) kèm tải ảnh đại diện lên Cloudinary.
  - Sửa thông tin người dùng (`/admin/users/edit/{id}`).
  - Khóa / Mở khóa tài khoản (`/admin/users/toggle-status/{id}`).
  - Xóa người dùng (`/admin/users/delete/{id}`).

### 5. Quản lý Sản Phẩm (CRUD, Phân trang, Tìm kiếm, Lọc theo User)
- Truy cập: `/products`
- **Tìm kiếm:** Theo tên hoặc mô tả sản phẩm.
- **Phân trang:** Hỗ trợ điều hướng trang trước/sau và theo số trang.
- **Lọc:** Tab **"Tất cả sản phẩm"** và tab **"Sản phẩm của tôi"** cho người dùng đã đăng nhập.
- **Đếm số lượng:** Hiển thị tổng số sản phẩm và số lượng sản phẩm do chính người dùng hiện tại đăng bán.
- **Thêm sản phẩm:** `/products/new` (Upload hình ảnh lên Cloudinary).
- **Chi tiết sản phẩm:** `/products/{id}` hiển thị thông tin sản phẩm và thông tin người bán.
- **Sửa / Xóa sản phẩm:** Chỉ chủ sở hữu sản phẩm hoặc quản trị viên (Admin) mới có quyền sửa/xóa.

---

## 4. Tài khoản mẫu kiểm thử
| Vai trò | Tên đăng nhập | Email | Mật khẩu | Quyền hạn |
| :--- | :--- | :--- | :--- | :--- |
| **Quản trị viên (Admin)** | `admin` | `admin@gmail.com` | `123456` | Toàn quyền quản trị user, sản phẩm |
| **Người dùng 1 (User)** | `user01` | `user01@gmail.com` | `123456` | Đinh Quốc Anh (đăng sản phẩm, quản lý sản phẩm của mình) |
| **Người dùng 2 (User)** | `user02` | `user02@gmail.com` | `123456` | Đăng sản phẩm, quản lý sản phẩm của mình |

---

## 5. Hướng dẫn chạy chương trình
1. Tạo file `.env` từ `.env.example`:
   ```bash
   cp .env.example .env
   ```
2. Cấu hình thông tin SQL Server trong file `.env`:
   ```properties
   DB_URL=jdbc:sqlserver://localhost:1433;databaseName=web_spring_security;encrypt=false;trustServerCertificate=true;sslProtocol=TLSv1.2;characterEncoding=UTF-8
   DB_USERNAME=sa
   DB_PASSWORD=your_password
   ```
3. Chạy ứng dụng bằng Maven Wrapper:
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Java\jdk-26.0.2.1"
   ./mvnw spring-boot:run
   ```
4. Truy cập các đường dẫn:
   - Trang chủ: [http://localhost:8080/](http://localhost:8080/)
   - Đăng nhập: [http://localhost:8080/login](http://localhost:8080/login)
   - Đăng ký: [http://localhost:8080/register](http://localhost:8080/register)
   - Quản lý người dùng: [http://localhost:8080/admin/users](http://localhost:8080/admin/users)
   - Danh sách sản phẩm: [http://localhost:8080/products](http://localhost:8080/products)
