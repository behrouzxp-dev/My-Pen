<div dir="rtl">

# ما‌ی پن (My Pen)

برنامهٔ اندروید فارسی (راست‌به‌چپ) برای مدیریت موجودی قلم‌های انسولین: تاریخ انقضا، اولویت مصرف، یادآورها و پشتیبان‌گیری/بازیابی.

## معرفی

ما‌ی پن (`com.arena.mypen`) به افراد دارای رژیم انسولین کمک می‌کند موجودی قلم‌های خود را مدیریت کنند:

- **موجودی** قلم‌های انسولین بلنداثر و کوتاه‌اثر با وضعیت هر قلم (`ACTIVE`، `OPENED`، `DEFECTIVE`، `EXPIRED`).
- **اولویت مصرف**: قلم‌ها بر اساس تاریخ انقضا رتبه‌بندی می‌شوند (زودتر = اولویت اول)؛ قلم‌های با تاریخ انقضای یکسان، رتبهٔ یکسان می‌گیرند.
- **ثبت گروهی**: افزودن چند قلم یکسان در یک تراکنش اتمیک؛ هر قلم رکورد مستقل خودش را دارد.
- **یادآوری انقضا**: آستانه‌های قابل تنظیم ۳۰، ۱۴ و ۷ روز قبل از انقضا با حذف تکراری‌ها در جدول لاگ محلی. یک کار دوره‌ای WorkManager و چک هنگام بازگشت به برنامه، هر یادآور را برای هر قلم فقط یک‌بار می‌فرستد.
- **انتقال خودکار به منقضی‌شده**: قلم‌هایی که تاریخ انقضایشان گذشته به‌طور خودکار به وضعیت `EXPIRED` منتقل می‌شوند (به ازای هر قلم یک رویداد تاریخچه ثبت می‌شود).
- **تاریخچه**: ثبت تمام رویدادها (ورود، باز شدن، خرابی، انقضا، ویرایش).
- **پشتیبان‌گیری و بازیابی**: خروجی/ورود کامل دیتابیس به‌صورت JSON نسخه‌دار (`schemaVersion: 1`). بازیابی اتمیک است و پس از تأیید، همهٔ داده‌ها جایگزین می‌شوند.

## فناوری‌ها

- Kotlin + Jetpack Compose (Material 3)، چیدمان RTL اجباری
- SQLite خام از طریق `SQLiteOpenHelper` (سینگلتون)، بدون Room
- WorkManager برای چک دوره‌ای انقضا
- مرتب‌سازی/نامرتب‌سازی دستی JSON (`org.json`) برای فایل‌های پشتیبان
- تاریخ‌های مبتنی بر منطقهٔ زمانی محلی دستگاه (`java.util.Calendar`/`SimpleDateFormat`)

## نیازمندی‌ها

- JDK 17
- Android SDK Platform 34 (compileSdk/targetSdk = 34)، minSdk = 24
- Gradle 8.6+ (CI نسخهٔ ۸٫۷ را نصب می‌کند؛ فعلاً Gradle Wrapper در ریپو نیست — رجوع کنید به [RELEASES.md](../RELEASES.md) یادداشت‌های انتشار)

## ساخت و تست

```bash
# تست‌های واحد (هرمتیک؛ بدون دستگاه)
gradle testDebugUnitTest

# APK دیباگ (بدون نیاز به keystore)
gradle assembleDebug

# APK ریلیز (بدون secretهای امضا، خروجی بدون امضاست؛ رجوع کنید به SECURITY.md)
gradle assembleRelease
```

CI روی هر Pull Request به شاخهٔ `main` همین مراحل را اجرا می‌کند (`.github/workflows/ci.yml`). تغییرات صرفاً مستنداتی CI را اجرا نمی‌کنند.

## ساختار پروژه

```
app/src/main/java/com/arena/mypen/
├── MainActivity.kt          # پوستهٔ برنامه، ناوبری، بارگذاری پس‌زمینهٔ داده‌ها
├── MyPenApp.kt              # کلاس Application: ساخت channel اعلان + کار دوره‌ای
├── data/                    # AppDatabase (SQLite) + PenRepository
├── model/                   # InsulinPen، HistoryEvent، اینام‌ها، تنظیمات
├── notification/            # NotificationHelper، ExpiryCheckWorker
├── ui/screens/              # صفحات Compose (داشبورد، موجودی، ...)
├── ui/theme/                # رنگ‌بندی (منبع واحد حقیقت رنگ‌ها)
└── util/                    # DateUtils (منطقهٔ زمانی محلی)، PriorityCalculator

app/src/test/                # تست‌های JVM + fixture پشتیبان v0 (از classpath)
```

## مستندات

- [ARCHITECTURE.md](../docs/ARCHITECTURE.md) — معماری، مدل داده، فرمت فایل پشتیبان
- [REQUIREMENTS.md](../docs/REQUIREMENTS.md) — نیازمندی‌های تابعی و غیرتابعی
- [CHANGELOG.md](../CHANGELOG.md) — تاریخچهٔ تغییرات (Keep a Changelog + SemVer)
- [RELEASES.md](../RELEASES.md) — فرآیند انتشار و نام‌گذاری آرتیفکت‌ها
- [SECURITY.md](../SECURITY.md) — سیاست کلید امضا و گزارش‌ اشکالات امنیتی
- [AGENTS.md](../AGENTS.md) — قوانین دائمی مشارکت/عامل‌ها در این ریپو

## مجوز

انحصاری (Proprietary) — تمام حقوق محفوظ است؛ رجوع کنید به [LICENSE](../LICENSE).

</div>
