# 🔐 Enigma Machine Simulator

<div align="center">

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Swing](https://img.shields.io/badge/Java_Swing-GUI-4CAF50?style=for-the-badge)
![Cryptography](https://img.shields.io/badge/Cryptography-Enigma-9C27B0?style=for-the-badge)
![Release](https://img.shields.io/badge/Release-v1.0-success?style=for-the-badge)
![License](https://img.shields.io/badge/License-GPL_v3-blue?style=for-the-badge)

**Simulasi mesin sandi Enigma khas Perang Dunia II, dibangun dari nol menggunakan Java.**  
Implementasi akurat Rotor · Reflector · Plugboard dengan antarmuka GUI modern bergaya terminal.

[⬇️ Download JAR](https://github.com/putra-as-kyuutora/enigma-simulator-java/releases/tag/java) · [📂 Source Code](https://github.com/putra-as-kyuutora/enigma-simulator-java/tree/main/src/enigmaproject)

</div>

---

## 📖 Tentang Proyek

**Mesin Enigma** adalah perangkat enkripsi elektromekanis yang digunakan Nazi Jerman selama Perang Dunia II untuk mengamankan komunikasi militer. Dengan kombinasi rotor berputar, panel plugboard, dan reflektor, mesin ini dianggap tak bisa dipecahkan — hingga tim Alan Turing di Bletchley Park berhasil membongkarnya.

Proyek ini mereplikasi cara kerja Enigma secara penuh dalam Java: mulai dari algoritma enkripsi per karakter yang akurat, mekanisme *double-stepping* rotor yang historis, hingga antarmuka GUI yang modern dan interaktif.

---

## ✨ Fitur

| Fitur | Deskripsi |
|---|---|
| 🔄 **3 Rotor Simultan** | Wiring historis Enigma I dengan stepping mechanism yang akurat |
| 🪞 **Reflector B** | Implementasi reflektor: `YRUHQSLDPXNGOKMIEBFZCWVJAT` |
| 🔌 **Plugboard** | Mendukung pasangan swap karakter (contoh: `AT BS DE`) |
| ⚙️ **Ring Settings** | Konfigurasi ring setting per rotor (A–Z) |
| 📍 **Initial Positions** | Posisi awal rotor yang dapat dikonfigurasi |
| ⏩ **Double-Stepping** | Mekanisme stepping ganda persis seperti mesin asli |
| ⌨️ **Real-time Enkripsi** | Karakter terenkripsi langsung saat diketik |
| 🎨 **Dark / Light Theme** | Toggle tema gelap (hijau terminal) dan terang |
| 💾 **Simpan & Muat Pesan** | Ekspor/impor pesan terenkripsi ke file `.txt` |
| 🔧 **Configuration Dialog** | Ubah semua pengaturan Enigma melalui GUI |
| ⌨️ **Keyboard Shortcuts** | `Ctrl+R`, `Ctrl+S`, `Ctrl+L`, `Ctrl+A`, `F1` |

---

## 🏗️ Cara Kerja (Alur Enkripsi)

Setiap karakter yang diketik melewati 5 tahap sebelum menjadi output:

```
Input → [Plugboard] → [Rotor 3 → Rotor 2 → Rotor 1] → [Reflector]
      → [Rotor 1 → Rotor 2 → Rotor 3] → [Plugboard] → Output
```

**Poin penting:**
- Setiap karakter yang diketik, **Rotor 3 selalu maju** satu langkah
- Rotor 2 maju ketika Rotor 3 mencapai posisi *notch*-nya
- **Double-stepping:** Rotor 2 bisa maju dua kali berturut-turut (anomali mesin asli)
- Reflektor memastikan enkripsi bersifat **simetris**: pesan yang dienkripsi dengan setting sama akan terdekripsi kembali ke teks asli

### Struktur Kode

```
src/enigmaproject/
├── Enigma.java      # Core engine dengan inner classes: Rotor, Reflector, Plugboard
└── EnigmaGUI.java   # Antarmuka Swing: tema, real-time enkripsi, file I/O, shortcuts
```

### Konfigurasi Default (Enigma I — Historis)

| Komponen | Nilai | Notch |
|---|---|---|
| Rotor I | `EKMFLGDQVZNTOWYHXUSPAIBRCJ` | Q |
| Rotor II | `AJDKSIRUXBLHWTMCQGZNPYFVOE` | E |
| Rotor III | `BDFHJLCPRTXVZNYEIWGAKMUSQO` | V |
| Reflector B | `YRUHQSLDPXNGOKMIEBFZCWVJAT` | — |
| Ring Settings | `A A A` | — |
| Initial Positions | `A A A` | — |

---

## 🚀 Cara Menjalankan

### Option A — Langsung Pakai JAR (Termudah)

1. Download file JAR dari halaman **[Releases](https://github.com/putra-as-kyuutora/enigma-simulator-java/releases/tag/java)**
2. Pastikan **Java 8+** sudah terinstall
3. Jalankan:

```bash
java -jar enigma-simulator.jar
```

atau **double-click** file JAR-nya langsung.

### Option B — Build dari Source

```bash
# Clone repo
git clone https://github.com/putra-as-kyuutora/enigma-simulator-java.git
cd enigma-simulator-java

# Compile
javac -d bin src/enigmaproject/*.java

# Jalankan
java -cp bin enigmaproject.EnigmaGUI
```

**Via NetBeans:** Buka project → tekan `F6`

---

## 💻 Contoh Penggunaan

### Enkripsi & Dekripsi (Sifat Simetris Enigma)

```
Setting: Ring = A A A | Posisi = A A A | Plugboard = (kosong)

# Enkripsi
Input  : HELLOWORLD
Output : MFNCZBBLTG   ← (hasil berubah tiap ketikan karena rotor berputar)

# Reset rotor ke A A A, lalu dekripsi
Input  : MFNCZBBLTG
Output : HELLOWORLD   ← Kembali ke teks asli!
```

### Keyboard Shortcuts

| Shortcut | Fungsi |
|---|---|
| `Ctrl + A` | Encrypt All — enkripsi seluruh teks input sekaligus |
| `Ctrl + R` | Reset Rotor ke posisi awal |
| `Ctrl + S` | Simpan output ke file `.txt` |
| `Ctrl + L` | Muat pesan dari file |
| `F1` | Buka jendela Configuration |

### Format File yang Tersimpan

```
=== ENIGMA ENCRYPTED MESSAGE ===
Date: 2025-09-01T12:00:00
Input: HELLO
Output: MFNCZ
Ring Settings: A A A
Initial Positions: A A A
Plugboard Pairs:
=== END MESSAGE ===
```

---

## 🎯 Konsep yang Dipelajari

- ✅ **Kriptografi klasik** — Implementasi algoritmik mesin Enigma secara akurat
- ✅ **Inner classes** — `Rotor`, `Reflector`, `Plugboard` sebagai komponen terenkapsulasi
- ✅ **Aritmatika modular** — Encoding karakter dengan offset, ring setting, dan posisi rotor
- ✅ **Double-stepping mechanism** — Anomali historis yang diimplementasi dengan benar
- ✅ **Swing GUI modern** — Custom border, dark/light theme, rounded button dengan hover effect
- ✅ **Event-driven programming** — `DocumentListener` real-time, `InputMap/ActionMap` untuk shortcuts
- ✅ **File I/O** — Simpan dan muat pesan terenkripsi lengkap dengan metadata
- ✅ **Separation of concerns** — Engine (`Enigma.java`) terpisah dari presentasi (`EnigmaGUI.java`)

---

## 👤 Developer

**Eka Alssah Putra** — Mahasiswa Informatika

[![GitHub](https://img.shields.io/badge/GitHub-putra--as--kyuutora-181717?style=flat-square&logo=github)](https://github.com/putra-as-kyuutora)
[![Email](https://img.shields.io/badge/Email-putra.dvpr@gmail.com-EA4335?style=flat-square&logo=gmail)](mailto:putra.dvpr@gmail.com)

---

## 📄 Lisensi

Proyek ini dilisensikan di bawah [GNU General Public License v3.0](LICENSE).

---

<div align="center">

⭐ Kalau project ini bermanfaat, jangan lupa beri bintang! ⭐

*"The most secure system is one the enemy cannot crack." — Terinspirasi oleh Alan Turing*

</div>
