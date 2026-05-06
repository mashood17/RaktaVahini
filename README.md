<div align="center">

# 🩸 Rakta-Vahini
### Filtered Blood Donor Network

*Connecting Donors. Saving Lives.*

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![Firestore](https://img.shields.io/badge/Database-Firestore-FF6F00?style=for-the-badge&logo=firebase&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)

---

> **"Every 2 seconds, someone in India needs blood. Rakta-Vahini makes sure they find it in time."**

</div>

---

## 📖 Table of Contents

- [The Problem We Solve](#-the-problem-we-solve)
- [What Is Rakta-Vahini?](#-what-is-rakta-vahini)
- [Key Features](#-key-features)
- [App Screens & Flow](#-app-screens--flow)
- [Tech Stack](#-tech-stack)
- [Database Structure](#-database-structure)
- [Eligibility Logic](#-eligibility-logic)
- [Getting Started](#-getting-started)
- [Firebase Setup](#-firebase-setup)
- [Project Structure](#-project-structure)
- [Known Issues & Fixes](#-known-issues--fixes)
- [Future Roadmap](#-future-roadmap)
- [Contributing](#-contributing)
- [Author](#-author)

---

## 🚨 The Problem We Solve

In a blood emergency, families in India still rely on mass WhatsApp forwards — messages that go to hundreds of people, most of whom have the wrong blood group, donated recently, or simply don't respond. Critical hours are lost in a situation where every minute counts.

Three specific gaps exist in the current system:

| Gap | Impact |
|-----|--------|
| **No filtering** | Requests reach the wrong people. O+ messages go to AB- donors who cannot help. |
| **No eligibility awareness** | Willing donors may not know they must wait 90 days between donations, creating false leads. |
| **No privacy layer** | Sharing phone numbers on public groups discourages many people from volunteering at all. |

Rakta-Vahini was built to close all three gaps — with a filtered, eligibility-aware, privacy-respecting blood donor directory that works in real time.

---

## 💡 What Is Rakta-Vahini?

**Rakta-Vahini** (Sanskrit: *carrier of blood*) is an Android application that connects people who urgently need blood with verified, eligible donors — instantly, accurately, and privately.

Donors register once with their blood group and donation history. When someone needs blood, they open the app, select the required blood group, and see **only** those donors who match the group and are currently eligible to donate. No noise. No irrelevant results. Just the people who can actually help, right now.

---

## ✨ Key Features

### 🔐 Secure OTP Authentication
Phone number-based login using Firebase Authentication. No passwords to forget, no email to verify — just a 6-digit OTP on the donor's own device. Session is preserved across app restarts so donors don't log in repeatedly.

### 🧾 Smart Donor Registration
A clean, guided registration form that captures everything needed to make a donor searchable and useful:
- Full name, blood group, city, and phone
- Last donation date via a date picker (or "Never Donated" for first-timers)
- Phone number pre-filled and locked from OTP verification — no manual entry errors

Supported blood groups: `A+` `A-` `B+` `B-` `O+` `O-` `AB+` `AB-` `Bombay (hh)`

### 🔍 Real-Time Blood Search
The core of the app. Select a blood group, tap Search — and see only donors who:
- Match the requested blood group
- Are marked as available
- Have crossed the 90-day eligibility window since their last donation

Results update in real time via Firestore snapshot listeners. If a donor marks themselves unavailable while you're looking at the results, they disappear from your screen instantly — no refresh needed.

### 🩸 90-Day Eligibility Engine
The eligibility calculation is the heart of what makes Rakta-Vahini trustworthy. The rule is medically grounded: a person must wait at least 90 days between blood donations. This logic lives in a single place (`Donor.kt`) and is applied consistently across every screen.

```kotlin
fun isEligible(): Boolean {
    if (lastDonationDate == 0L) return true
    val daysSince = (System.currentTimeMillis() - lastDonationDate) / (1000 * 60 * 60 * 24)
    return daysSince >= 90
}
```

### 👤 Donor Profile Management
Every registered donor has a live profile screen that shows:
- Their current blood group and city
- Real-time eligibility status and countdown
- An availability toggle — donors can mark themselves unavailable temporarily (travel, illness, personal reasons) without losing their registration
- A one-tap "Log a Donation Today" button

### 📊 Donation History Tracking
Every donation a donor logs is recorded with a timestamp and displayed in a scrollable history. The app shows total donation count, last donation date, and the exact number of days until the donor is eligible again.

### 📞 Privacy-Safe Calling
The donor's phone number is never displayed as text anywhere in the app. The "Call Donor" button passes the number directly to Android's system dialer via `ACTION_DIAL` — the number only appears inside the system phone app, which the user controls. No `CALL_PHONE` permission is required.

---

## 📱 App Screens & Flow

```
┌─────────────────────────────────────────────────────────┐
│                   LoginActivity                         │
│         Enter phone → Receive OTP → Verify             │
└──────────────────────────┬──────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                   MainActivity                          │
│    Welcome screen · Dynamic based on profile status    │
└────────┬─────────────┬────────────────┬────────────────┘
         │             │                │
         ▼             ▼                ▼
  ┌──────────┐  ┌──────────────┐  ┌─────────────────┐
  │ Register │  │ Search Blood │  │  Donor Profile  │
  │  Donor   │  │  (Core UX)  │  │  + Donate Log   │
  └──────────┘  └──────────────┘  └─────────────────┘
```

| Screen | Purpose |
|--------|---------|
| **Login** | Phone OTP authentication |
| **Home** | Entry point — adapts to whether user is registered donor or visitor |
| **Register Donor** | One-time registration form with OTP-verified phone |
| **Search Blood** | Real-time filtered donor search by blood group |
| **Donor Profile** | Live profile view, availability toggle, log donation |
| **Donation Log** | Full history, total count, next eligibility date |

---

## 🛠 Tech Stack

| Layer | Technology | Why |
|-------|-----------|-----|
| Language | Kotlin | Modern, concise, officially recommended for Android |
| IDE | Android Studio Panda (2025.3.1) | Standard Android development environment |
| UI | XML Layouts + ViewBinding | Stable, type-safe, no boilerplate `findViewById` |
| Lists | RecyclerView | Efficient scrolling for donor lists and history |
| Authentication | Firebase Auth (Phone OTP) | No-password, SMS-verified login |
| Database | Firebase Cloud Firestore | Real-time, cloud-hosted, scales to thousands of donors |
| Real-time sync | `addSnapshotListener()` | Live UI updates without manual refreshes |
| Calling | Android `ACTION_DIAL` Intent | Native, privacy-preserving, zero dependencies |
| Notifications | Material Snackbar | Lightweight feedback without push notification complexity |
| Date logic | Java Calendar | Built-in, no external library needed |

---

## 🗄 Database Structure

All donor data lives in a single Firestore collection. Each document is keyed by the donor's Firebase Auth UID — this permanently links authentication to the donor profile across all devices.

```
Firestore
└── donors/
    └── {uid}/                        ← Firebase Auth UID
        ├── name           (String)   → "Ravi Kumar"
        ├── bloodGroup     (String)   → "O+"
        ├── city           (String)   → "Mangalore"
        ├── phone          (String)   → "+919876543210"
        ├── lastDonationDate (Long)   → 0  (epoch ms, 0 = never donated)
        ├── isAvailable    (Boolean)  → true
        ├── donationHistory (Array)   → ["15/03/2025", "10/07/2024"]
        └── uid            (String)   → mirrors document ID
```

**Why UID as document ID?**
Using the Auth UID as the Firestore document ID means a donor who logs in on a new device automatically loads their existing profile — no lookup query needed, no risk of duplicates.

---

## 🧠 Eligibility Logic

The eligibility engine is the most critical business logic in the app. It lives entirely in `Donor.kt` so every screen uses the same calculation with zero duplication.

```kotlin
data class Donor(
    var id: String = "",
    val name: String = "",
    val bloodGroup: String = "",
    val city: String = "",
    val phone: String = "",
    val lastDonationDate: Long = 0L,
    val isAvailable: Boolean = true
) {
    // Returns true if donor can donate today
    fun isEligible(): Boolean {
        if (lastDonationDate == 0L) return true   // Never donated = always eligible
        val daysSince = (System.currentTimeMillis() - lastDonationDate) / (1000L * 60 * 60 * 24)
        return daysSince >= 90
    }

    // Returns how many days remain until the donor is eligible again
    fun daysUntilEligible(): Long {
        val daysSince = (System.currentTimeMillis() - lastDonationDate) / (1000L * 60 * 60 * 24)
        return (90 - daysSince).coerceAtLeast(0)
    }
}
```

---

## 🚀 Getting Started

### Prerequisites

Before you begin, make sure you have:

- Android Studio **Panda (2025.3.1)** or newer
- JDK 11 or higher
- A Firebase account (free tier is sufficient)
- A physical Android device or emulator running **API 24 (Android 7.0)** or higher

---

### 1. Clone the Repository

```bash
git clone https://github.com/mashood17/RaktaVahini.git
cd rakta-vahini
```

### 2. Open in Android Studio

```
File → Open → select the cloned folder → click OK
```

Wait for the initial Gradle sync to complete before proceeding.

---

## 🔥 Firebase Setup

This step is required. Without it, the app cannot authenticate users or read/write donor data.

### Step 1 — Create a Firebase Project

1. Go to [console.firebase.google.com](https://console.firebase.google.com)
2. Click **Add Project** → Name it `RaktaVahini` → Continue
3. Disable Google Analytics (not needed) → **Create Project**

### Step 2 — Register Your Android App

1. Click the **Android icon** (➕ Add app)
2. Package name: `com.raktavahini.app`
3. App nickname: `Rakta-Vahini`
4. Click **Register App**
5. Download **`google-services.json`**
6. Place it in `app/` directory (not the project root — the `app/` subfolder)

### Step 3 — Enable Phone Authentication

1. Firebase Console → **Authentication** → **Sign-in method**
2. Click **Phone** → Enable → Save

> ⚠️ **Important:** For OTP to work on a physical device, your Firebase project must have a valid SHA-1 fingerprint registered. Go to **Project Settings → Your Apps → Add Fingerprint** and add the debug SHA-1 from Android Studio's Gradle panel.

### Step 4 — Enable Firestore

1. Firebase Console → **Firestore Database**
2. Click **Create Database**
3. Choose **Start in test mode** (for development)
4. Region: **asia-south1** (Mumbai — best for Indian users)

### Step 5 — Firestore Security Rules (Production)

When you are ready to deploy beyond testing, update your Firestore rules:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /donors/{uid} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == uid;
    }
  }
}
```

### Step 6 — Build and Run

```
Build → Rebuild Project → Run on Device/Emulator
```

---

## 📁 Project Structure

```
app/
└── src/main/
    ├── java/com/raktavahini/app/
    │   ├── LoginActivity.kt          ← OTP phone authentication
    │   ├── MainActivity.kt           ← Home screen, adapts to profile state
    │   ├── RegisterDonorActivity.kt  ← Donor registration form
    │   ├── SearchBloodActivity.kt    ← Real-time donor search
    │   ├── DonorProfileActivity.kt   ← Profile, toggle, log donation
    │   ├── DonationLogActivity.kt    ← Donation history list
    │   ├── Donor.kt                  ← Data model + eligibility engine
    │   ├── DonorAdapter.kt           ← RecyclerView adapter for donor cards
    │   └── DonationLogAdapter.kt     ← RecyclerView adapter for history
    │
    └── res/
        ├── layout/
        │   ├── activity_login.xml
        │   ├── activity_main.xml
        │   ├── activity_register_donor.xml
        │   ├── activity_search_blood.xml
        │   ├── activity_donor_profile.xml
        │   ├── activity_donation_log.xml
        │   ├── item_donor_card.xml
        │   └── item_donation_log.xml
        └── values/
            ├── colors.xml            ← Red/white medical theme
            └── themes.xml
```

---

## 🐛 Known Issues & Fixes

A record of real problems encountered during development and exactly how they were resolved. Documented here so future contributors don't hit the same walls.

| Issue | Root Cause | Fix Applied |
|-------|-----------|-------------|
| `HasConvention` Gradle error | AGP version incompatible with Gradle 9+ | Pinned AGP to `8.9.2` + Gradle `8.11.1` for Panda |
| OTP not sending | Missing SHA-1 fingerprint in Firebase | Added debug SHA-1 via Project Settings → Your Apps |
| Firestore returning 0 documents | Collection was accidentally named `"Collection ID: donors"` instead of `"donors"` | Deleted and recreated collection with correct name |
| Spinner not triggering search | `onItemSelectedListener` race condition in CardView | Replaced auto-trigger with explicit Search button |
| Profile not loading on new device | Donor ID was stored in SharedPreferences (device-specific) | Switched to Firebase Auth UID as Firestore document ID |
| Availability toggle firing twice | Setting `isChecked` programmatically triggered the listener | Detach listener before setting value, reattach after |
| Donor data not mapping from Firestore | `toObject()` silently failing on type mismatch | Replaced with manual `doc.getString()` / `doc.getLong()` extraction |

---

## 🔮 Future Roadmap

Features planned for upcoming versions, in order of priority:

| Version | Feature | Description |
|---------|---------|-------------|
| v2.0 | GPS Radius Filter | Show donors within 10km/20km of the requester's location |
| v2.0 | Push Notifications | Alert eligible donors near an emergency request in real time |
| v2.1 | Urgency Tags | Allow requesters to mark requests as Critical / Planned / Scheduled |
| v2.1 | WhatsApp Share | Generate a shareable donor info card for existing WhatsApp networks |
| v3.0 | Admin Dashboard | Verified admin panel to review and approve donor registrations |
| v3.0 | Offline Support | Firestore offline caching so the app works with poor connectivity |
| v3.0 | Kannada Language | Full localization for rural Karnataka users |
| v3.0 | Dark Mode | AMOLED-friendly dark theme |
| Future | AI Recommendations | Predict donor availability based on historical donation patterns |
| Future | Hospital Integration | Direct API tie-in with blood bank inventory systems |

---

## 🤝 Contributing

Contributions are welcome. If you find a bug, have a feature idea, or want to improve the code, here is how to get involved:

1. Fork the repository
2. Create a new branch: `git checkout -b feature/your-feature-name`
3. Make your changes with clear, descriptive commits
4. Push to your fork: `git push origin feature/your-feature-name`
5. Open a Pull Request with a clear description of what you changed and why

Please keep pull requests focused on a single change. Large PRs that touch everything are hard to review and slow to merge.

---

## 👨‍💻 Author

**Mahammad Mashood**
Student, Computer Science Engineering
Srinivas Institute of Technology, Mangalore

📧 [Mashoodrenja17@gmail.com](mailto:Mashoodrenja17@gmail.com)

> *This project was developed as part of a 6-week Android Development internship focused on real-time mobile application development using Firebase and Kotlin.*

---

<div align="center">

**Built with purpose. Designed for emergencies. Made for India.**

🩸 *Rakta-Vahini — "Connecting Donors. Saving Lives."*

</div>