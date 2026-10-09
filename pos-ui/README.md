# 💻 SkyPOS Frontend - React 19 Point of Sale Interface

Ứng dụng Frontend hiện đại dành cho nhân viên bán hàng, quản lý ca làm việc và theo dõi tồn kho. Được xây dựng trên nền tảng **React 19**, **Vite 8**, **Tailwind CSS v4** và **Lucide Icons**.

---

## 🚀 Tính Năng Frontend
- **Giao diện POS bán hàng mượt mà**: Thao tác chọn sản phẩm, thay đổi số lượng, nhập mã giảm giá, tính toán VAT và chiết khấu tức thì.
- **Thanh toán tích hợp Stripe Elements**: Hỗ trợ nhập thẻ tín dụng quốc tế bảo mật theo chuẩn PCI DSS qua `@stripe/react-stripe-js`.
- **Lịch sử thanh toán & Hoàn tiền (Refunds)**: Modal tra cứu nhật ký giao dịch thẻ, mã chuẩn hóa Stripe và thực hiện hoàn tiền trực tiếp.
- **Quản lý ca làm việc (Shift Handover)**: Nhập tiền đầu ca, theo dõi chênh lệch doanh thu và kết ca thu ngân.
- **Quản lý kho hàng (Inventory UI)**: Bảng tồn kho thời gian thực, bộ lọc theo trạng thái (Hết hàng / Sắp hết / Đủ hàng), modal điều chỉnh tăng/giảm tồn kho nhanh.
- **Xử lý mạng & Interceptors chuẩn hóa**: Tự động chuyển hướng khi token hết hạn (401), xử lý mất kết nối, hiển thị Toast thông báo thân thiện tiếng Việt.

---

## 🛠 Hướng Dẫn Cài Đặt & Phát Triển

### 1. Cài đặt Dependencies:
```bash
npm install
```

### 2. Cấu hình Biến Môi Trường (`.env`):
Tạo file `.env` từ `.env.example`:
```bash
cp .env.example .env
```
Nội dung mẫu:
```env
VITE_API_URL=http://localhost:5000
VITE_APP_TITLE=SkyPOS - Next-Gen Cloud POS System
VITE_API_TIMEOUT=15000
VITE_ENABLE_DEV_LOGS=false
```

### 3. Khởi chạy Development Server:
```bash
npm run dev
```
Ứng dụng sẽ chạy tại: [http://localhost:5173](http://localhost:5173)

### 4. Kiểm tra Lint & Định dạng:
```bash
npm run lint
```

### 5. Đóng gói Production Bundle:
```bash
npm run build
```
Thư mục xuất file: `dist/`

---

## 🐳 Chạy Bằng Docker (Nginx)

```bash
docker build -t skypos-ui .
docker run -d -p 80:80 --name skypos-ui skypos-ui
```
Truy cập: [http://localhost](http://localhost)
