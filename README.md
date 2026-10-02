# Wafa PDF

<p align="center">
  <img src="app/src/main/res/drawable/img_wafa_logo_1790900970624.jpg" width="128" height="128" alt="Wafa PDF Logo" style="border-radius: 24px;" />
</p>

<p align="center">
  <b>PDF &amp; Document Toolkit</b><br>
  <i>Made with ❤️ by Mehedi364</i>
</p>

---

## 🌟 Overview

**Wafa PDF** is a high-performance Android document workstation engineered for complete offline autonomy, zero-compromise privacy, and AI-powered document intelligence. It supports extensive PDF manipulation, rendering, scanning, compression, watermarking, print preparation, and multilingual OCR (Bangla, English, Arabic).

---

## ✨ Features

### 📖 PDF Reader
- Continuous scroll, single-page, and book (two-page) reading modes
- Pinch-to-zoom (1.0x to 3.5x) and quick zoom controls
- Light, Dark, and warm Sepia reading filters
- In-document text search & bookmark manager
- Keep screen awake during reading
- Annotation toolbar: Pen, Highlighter, Underline, Shapes, Text Notes, and Signatures

### 📷 Document Scanner
- Camera capture & multi-image batch scan
- Document edge detection & perspective correction
- Enhancement filters: Original Color, Enhanced, Grayscale, and High-Contrast Black & White
- Instant export to multi-page PDF or OCR text extraction

### ⚡ PDF Tools Suite
- **PDF Compressor**: Presets for Web, Print, Mobile, WhatsApp, and custom DPI targeting
- **Watermark Studio**: Custom text, opacity, rotation, tiled stamps, and dynamic macros (`{NAME}`, `{DATE}`, `{PAGE}`)
- **Ready Print**: Automated 2-up, 4-up, and booklet preparation for A4, A5, and Letter sheets
- **Merge & Split**: Combine multiple PDFs or extract selected page ranges
- **Format Conversion**: Images → PDF and Text → PDF

### 🔍 Multilingual OCR
- On-device text recognition with first-class support for **Bangla (বাংলা)**, **English**, and **Arabic (العربية)**
- Instant clipboard copy, TXT / PDF export, and text search
- Integrated **Text-to-Speech (TTS)** Read Aloud engine

### 🤖 AI PDF Assistant
- Supports OpenRouter API and configurable cloud models (e.g., `google/gemini-2.5-flash`, `deepseek-chat`)
- Intelligent on-device synthetic fallback when offline
- Features: Ask PDF, Executive Summary, Concept Explanation, Translation, Study Notes, Quiz/MCQ generation, Flashcards, and Key Points

### 🔒 Privacy & Vault
- Private Vault secured by user master PIN
- Offline-first execution: No document is uploaded to any server unless explicitly requested
- Temporary cache and metadata cleaner

---

## 🏗️ Architecture & Technology Stack

- **Platform**: Android (Min SDK 26, Target SDK 36)
- **Language**: Kotlin 2.x
- **UI Toolkit**: Jetpack Compose with Material Design 3 (M3)
- **Local Persistence**: Room Database with KSP code generation
- **PDF Core**: Native Android `PdfRenderer` and `PdfDocument`
- **Network**: Retrofit & OkHttp
- **Asynchrony**: Kotlin Coroutines & Flow

---

## 🚀 Building & Generating APK

### Debug APK Build
```bash
gradle assembleDebug
```
The installable Debug APK will be produced at:
- `app/build/outputs/apk/debug/app-debug.apk`
- `APK_DOWNLOAD/app-debug.apk`
- `.build-outputs/app-debug.apk`

### Android App Bundle (AAB) Build
```bash
gradle bundleDebug
```

### Running Tests
```bash
gradle testDebugUnitTest
```

---

## 👤 Credits

Developed with dedication by **Mehedi364**.
Licensed under Apache 2.0.
