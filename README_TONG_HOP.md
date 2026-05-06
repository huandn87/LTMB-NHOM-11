# EVChargeMobile_TongHop

Project này đã gộp 2 phần:

1. `DangNhap.zip`: giữ luồng đăng nhập/onboarding/home bằng Java.
2. `Tai_khoan.zip`: chuyển các màn tài khoản sang Java + XML và tích hợp vào project chính.

## Luồng đã đồng bộ

- Đăng nhập thành công -> `ManHinhChinh`
- Từ bottom navigation ở `ManHinhChinh`, bấm `Tài khoản` -> `TaiKhoanActivity`
- Từ `TaiKhoanActivity` có thể mở:
  - `MyCarsActivity`
  - `PaymentMethodsActivity`
  - `PersonalInfoActivity`
  - `SecurityActivity`
  - `LanguageActivity`
- Màn `MyCarsActivity` có gọi Supabase bảng `phuongtien` và hỗ trợ bấm dấu `>` để hiện nút xóa đỏ.
- Đăng xuất sẽ clear SharedPreferences và quay về màn `DangNhap`.

## Supabase

File cấu hình API nằm tại:

`app/src/main/java/com/example/voltapp/account/api/SupabaseService.java`

Nếu đổi key, sửa:

```java
public static final String SUPABASE_KEY = "...";
```

## Lưu ý Supabase RLS

Cần có policy cho bảng `phuongtien` nếu app dùng publishable key:

```sql
alter table phuongtien enable row level security;
create policy "public read phuongtien" on phuongtien for select using (true);
create policy "public delete phuongtien" on phuongtien for delete using (true);
```

## Cách mở

Trong Android Studio chọn folder gốc:

`EVChargeMobile_TongHop`

Sau đó Sync Gradle -> Run.
