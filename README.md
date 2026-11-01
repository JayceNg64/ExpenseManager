# Ví Chi Tiêu — Ứng dụng quản lý chi tiêu cá nhân

Bài tập lớn môn **Phát triển ứng dụng cho thiết bị di động** — Đại học Bách khoa Hà Nội.

## Chức năng

| Chức năng | Kiến thức áp dụng |
|---|---|
| Thêm / sửa / xóa khoản thu chi, phân loại theo danh mục | Room (SQLite), DAO, Coroutine |
| 4 màn hình chính với thanh điều hướng dưới | Fragment, Navigation Component, Bottom Navigation |
| Danh sách giao dịch, tìm kiếm, vuốt để xóa + hoàn tác | RecyclerView, ListAdapter/DiffUtil, SearchView, ItemTouchHelper, Snackbar |
| Tổng quan tháng: số dư, thu, chi, tiến độ ngân sách | ViewModel, LiveData, MediatorLiveData, switchMap |
| Thống kê: biểu đồ tròn theo danh mục, biểu đồ cột theo ngày | MPAndroidChart, truy vấn GROUP BY |
| Quy đổi ngoại tệ sang VND | Retrofit, Gson, REST API, kiểm tra kết nối mạng |
| Chụp ảnh hóa đơn đính kèm | Camera Intent, Activity Result API, FileProvider, Runtime Permission |
| Nhắc ghi chi tiêu hằng ngày | WorkManager, Notification, Notification Channel, PendingIntent |
| Cảnh báo vượt hạn mức ngân sách | Notification mức ưu tiên cao |
| Lên lịch lại nhắc nhở sau khi khởi động máy | BroadcastReceiver (BOOT_COMPLETED) |
| Chia sẻ báo cáo tháng qua Zalo, Gmail… | Intent ngầm định ACTION_SEND |
| Cài đặt hạn mức, chế độ tối, giờ nhắc | SharedPreferences, AppCompatDelegate, TimePickerDialog |

## Công nghệ

- Kotlin 1.9, Android Gradle Plugin 8.5, minSdk 26 (Android 8.0), targetSdk 34
- Kiến trúc MVVM: View (Activity/Fragment) → ViewModel → Repository → Room / Retrofit
- Giao diện XML + ViewBinding, Material Design 3

## Cách chạy

1. Cài **Android Studio** (bản Koala 2024.1 trở lên).
2. `File → Open` → chọn thư mục `ExpenseManager`.
3. Chờ Gradle Sync tải thư viện (cần Internet).
4. Chọn máy ảo (AVD) hoặc điện thoại thật đã bật *USB debugging* → nhấn **Run ▶**.

Kiểm thử đơn vị: `./gradlew test` (Windows: `gradlew.bat test`).

Gợi ý demo nhắc nhở nhanh: vào **Cài đặt → Gửi thử thông báo**.
Demo cảnh báo ngân sách: đặt hạn mức nhỏ (ví dụ 100000) rồi thêm một khoản chi lớn hơn.

## Cấu trúc mã nguồn

```
app/src/main/java/vn/edu/hust/expensemanager/
├── ExpenseApp.kt                 # Application: khởi tạo DB, kênh thông báo, lịch nhắc
├── data/
│   ├── local/                    # Room: Transaction (Entity), TransactionDao, AppDatabase, Categories
│   ├── remote/                   # Retrofit: ExchangeRateApi, RetrofitClient
│   ├── prefs/                    # SettingsManager (SharedPreferences)
│   └── repository/               # TransactionRepository
├── ui/
│   ├── MainActivity.kt, MainViewModel.kt
│   ├── home/                     # HomeFragment
│   ├── transactions/             # TransactionsFragment, TransactionAdapter
│   ├── stats/                    # StatsFragment
│   ├── settings/                 # SettingsFragment
│   ├── add/                      # AddEditTransactionActivity, AddEditViewModel
│   └── currency/                 # CurrencyActivity
├── worker/                       # ReminderWorker, ReminderScheduler (WorkManager)
├── receiver/                     # BootReceiver
└── util/                         # NotificationHelper, BudgetChecker, Formatters
```

## API sử dụng

`GET https://open.er-api.com/v6/latest/{base}` — tỷ giá miễn phí, không cần API key.
