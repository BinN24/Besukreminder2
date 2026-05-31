# 🎬 Video Reminder Overlay - Android App

Aplikasi Android yang menampilkan **overlay video pengingat** seperti "notifikasi tab Windows" 
ketika kamu membuka TikTok atau Instagram. Video dari file MP4 kamu sendiri!

---

## ✨ Fitur Utama

- 📱 **Overlay muncul otomatis** saat kamu buka TikTok / Instagram / YouTube / Twitter
- 🎥 **Putar video MP4** dari storage HP kamu sebagai pengingat
- 💬 **Pesan kustom** — tulis judul dan pesan pengingat sendiri
- ⏰ **Tombol Snooze** — tunda 15 menit
- 🚫 **Abaikan Hari Ini** — skip untuk seharian
- 🔄 **Auto-start** setelah HP direstart
- 🎨 **Animasi slide** masuk dari bawah layar

---

## 📋 Cara Build APK Tanpa Komputer (Gratis!)

### Metode: GitHub Actions (GRATIS, 100% online)

**Langkah 1: Buat akun GitHub**
1. Buka https://github.com
2. Klik "Sign up" — gratis
3. Verifikasi email

**Langkah 2: Upload project ini ke GitHub**
1. Di GitHub, klik tombol **"+"** → **"New repository"**
2. Nama: `VideoReminderOverlay`
3. Pilih **Public** (gratis)
4. Klik **"Create repository"**
5. Di halaman repository, klik **"uploading an existing file"**
6. Drag & drop semua folder dan file project ini
7. Klik **"Commit changes"**

**Langkah 3: Jalankan Build**
1. Di repository GitHub, klik tab **"Actions"**
2. Klik workflow **"Build APK"**
3. Klik tombol **"Run workflow"** → **"Run workflow"**
4. Tunggu ± 5-10 menit (ada animasi loading)

**Langkah 4: Download APK**
1. Setelah build selesai (centang hijau ✅)
2. Klik pada workflow yang selesai
3. Scroll ke bawah → bagian **"Artifacts"**
4. Klik **"VideoReminderOverlay-debug"** untuk download
5. Ekstrak zip → dapat file `app-debug.apk`

---

## 📲 Cara Install APK di HP Android

1. **Pindahkan APK** ke HP kamu (via USB, Google Drive, dll)
2. Di HP, buka **Settings → Security**
3. Aktifkan **"Install unknown apps"** / "Sumber tidak dikenal"
4. Buka file manager, cari dan klik `app-debug.apk`
5. Klik **Install**

---

## ⚙️ Cara Pakai Setelah Install

### Langkah Setup (Wajib):

**1. Izin "Tampil di Atas Aplikasi"**
- Buka app → klik **"Aktifkan"** di samping izin pertama
- Di Settings, cari app "Video Reminder" → aktifkan toggle
- Kembali ke app

**2. Izin Accessibility Service**
- Klik **"Aktifkan"** di samping izin kedua  
- Di Settings Accessibility, cari **"Video Reminder Detector"**
- Aktifkan → klik OK/Allow

**3. Pilih Video MP4**
- Klik **"📂 Pilih Video MP4"**
- Pilih file video dari galeri / storage HP kamu
- Video akan diputar sebagai pengingat

**4. Tulis Pesan Pengingat**
- Isi judul: contoh `"STOP! Kamu harus besuk sekarang!"`
- Isi pesan: contoh `"TikTok bisa nanti. Pergi sekarang!"`

**5. Pilih Aplikasi Target**
- Aktifkan toggle untuk TikTok ✅
- Aktifkan toggle untuk Instagram ✅

**6. Aktifkan Reminder**
- Toggle **"AKTIFKAN REMINDER"** → ON
- Klik **"💾 SIMPAN PENGATURAN"**

**7. Test**
- Klik **"🧪 Test Tampilkan Overlay"** untuk melihat hasilnya
- Atau langsung buka TikTok!

---

## 🏗️ Struktur Project

```
VideoReminderOverlay/
├── app/src/main/
│   ├── java/com/reminder/overlay/
│   │   ├── MainActivity.java          ← Halaman pengaturan
│   │   ├── AppDetectorAccessibilityService.java  ← Deteksi app
│   │   ├── OverlayService.java        ← Tampilkan overlay
│   │   ├── BootReceiver.java          ← Auto-start
│   │   └── AppConstants.java          ← Konstanta
│   ├── res/layout/
│   │   ├── activity_main.xml          ← UI pengaturan
│   │   └── overlay_video.xml          ← UI overlay
│   └── AndroidManifest.xml
├── .github/workflows/build.yml        ← GitHub Actions build
└── README.md
```

---

## 🔧 Cara Ubah Aplikasi Target

Edit file `AppConstants.java`:
```java
public static final String PKG_TIKTOK = "com.zhiliaoapp.musically";
public static final String PKG_INSTAGRAM = "com.instagram.android";
// Tambah package name app lain di sini
```

Cara cari package name app: Install "Package Name Viewer" dari Play Store.

---

## ❓ FAQ

**Q: Overlay tidak muncul?**
- Pastikan kedua izin (Overlay + Accessibility) sudah diaktifkan
- Pastikan video sudah dipilih
- Pastikan Master Switch ON
- Coba klik "Test Tampilkan Overlay"

**Q: Video tidak muncul di overlay?**
- Pastikan file MP4 masih ada di HP
- Coba pilih ulang video

**Q: Muncul terlalu sering?**
- Ada cooldown 30 detik — tidak akan muncul lebih dari sekali per 30 detik
- Gunakan fitur "Tunda 15 Mnt" atau "Abaikan Hari Ini"

**Q: Tidak muncul setelah HP restart?**
- Buka app sekali dan aktifkan Master Switch
- App akan auto-start saat boot berikutnya

---

## 📄 Izin yang Dibutuhkan

| Izin | Kenapa Dibutuhkan |
|------|-------------------|
| SYSTEM_ALERT_WINDOW | Tampilkan overlay di atas app lain |
| FOREGROUND_SERVICE | Jalankan service di background |
| READ_MEDIA_VIDEO | Baca file video MP4 kamu |
| Accessibility Service | Deteksi app yang sedang dibuka |
| RECEIVE_BOOT_COMPLETED | Auto-start setelah reboot |

---

*Dibuat dengan ❤️ menggunakan Android Java*
# Besukreminder2
