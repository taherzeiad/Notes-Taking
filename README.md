# 📖 Intellectual Sanctuary

> A smart, AI-powered note-taking app built for people who think deeply.

![Android](https://img.shields.io/badge/Platform-Android-green?logo=android)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue?logo=jetpackcompose)
![License](https://img.shields.io/badge/License-MIT-orange)
![Status](https://img.shields.io/badge/Status-In%20Testing-yellow)

---

## ✨ Overview

Intellectual Sanctuary is a feature-rich Android note-taking application that goes beyond simple text capture. It combines a flexible block-based editor with AI-powered tools to help you write better, stay organized, and make sense of your thoughts — all in one place.

Built entirely with **Kotlin** and **Jetpack Compose**, following **MVVM + Clean Architecture** principles, with full support for **Arabic (RTL)** and **English (LTR)**.

---

## 🚀 Features

### 📝 Block-Based Editor
- Mix **text**, **images**, **voice recordings**, **bullet points**, and **links** in a single note
- Bold and italic formatting
- Real-time word count and reading time

### 🤖 AI Tools (Powered by Groq — llama-3.3-70b-versatile)
- **Rephrase** your writing with one tap
- **Arabic diacritization** (Tashkeel) automatically
- **Auto task extraction** from note content
- **Daily AI summary** of everything you wrote today
- **Auto classification** of notes into categories
- All AI responses adapt to the app language (Arabic / English)

### ✅ Smart Task Management
- Tasks extracted automatically from notes via AI
- Manual task creation using bullet blocks
- Three tabs: In Progress / Completed / All
- Search tasks by title or source
- AI progress bar showing completion rate
- Background AI processing when internet is available (WorkManager)

### 🎙️ Voice Recording
- Record audio directly inside any note
- Attach existing audio files from device storage
- Playback with a real-time progress bar and seek support

### 🔒 Privacy Center
- Toggle AI processing on/off
- Control voice recording storage
- Export all notes and tasks as a `.txt` file
- Delete all data with one tap
- View and manage real system permissions

### 🔔 Smart Reminders
- Daily reminder with custom time picker
- Write your own notification message
- Persistent across device reboots

### 🌍 Full Bilingual Support
- Arabic RTL layout
- English LTR layout
- Automatic language detection
- AI responses match app language

### 🎨 Design
- Clean, warm minimal UI
- Light and Dark mode
- Smooth 60fps with Jetpack Compose
- Adaptive color scheme

---

## 🏗️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| Database | Room (with Migrations) |
| Async | Kotlin Coroutines + Flow |
| Background Work | WorkManager |
| AI | Groq API (llama-3.3-70b-versatile) |
| Networking | Retrofit + OkHttp |
| Image Loading | Coil |
| Navigation | Jetpack Navigation Compose |
| DI | Manual (Factory Pattern) |
| Notifications | AlarmManager + BroadcastReceiver |
