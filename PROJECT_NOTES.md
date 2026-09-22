# MotionPulse – Project Documentation & Summary Notes

**Application Name:** MotionPulse  
**Package Name:** `com.the5watermelons.motionpulse`  
**Development Team:** The 5 Watermelons  
**Course Assignment:** Part Two of the POE for the Open Source Coding Assignment  
**Platform:** Native Android (Kotlin)  
**Architecture:** MVVM (Model-View-ViewModel) + Offline-First Repository Pattern  

---

## 1. Executive Summary
**MotionPulse** is a feature-rich, modern Android application designed to promote self-improvement, habit consistency, emotional well-being, and community accountability. Combining habit tracking, mood journaling, interactive analytics, background reminders, and social feeds, MotionPulse provides users with a complete digital ecosystem to build healthier daily routines and stay motivated.

---

## 2. Key Features & Modules

### 📱 Authentication & User Onboarding
- **Splash Screen (`SplashFragment`):** Smooth app launch experience with session checks.
- **Login & Registration (`LoginFragment`, `RegisterFragment`):** Secure user authentication, input validation, and user profile initialization.

### 🏠 Home Dashboard (`HomeFragment`)
- **Daily Snapshot:** Overview of daily tasks, active streaks, and top habits due for completion.
- **Quick Logging:** Seamless interaction to mark habits as complete directly from the dashboard.

### 📋 Habit Tracking & Management (`HabitsFragment`, `AddHabitFragment`)
- **Custom Habit Creation:** Support for personalized habit names, categories, target frequencies, and custom schedules.
- **Progress Tracking:** Real-time streak tracking and completion logs using a responsive `RecyclerView` adapter (`HabitAdapter`).

### 📊 Analytics & Progress Visualization (`StatsFragment`, `StatsViewModel`)
- **Interactive Weekly Chart (`WeeklyLineChartView`):** Custom-built line chart view illustrating weekly habit completion trends.
- **Detailed Metrics:** Breakdown of habit performance, consistency scores, and activity heatmaps (`StatsHabitAdapter`).

### 🧠 Mood Tracking & Journaling (`MoodFragment`)
- **Emotional Logging:** Intuitive mood picker with custom intensity scales, mood categories, and text journaling.
- **Trend Reflection:** Historical view of emotional states alongside habit completion data to reveal lifestyle-mood correlations.

### 👥 Social Community Tab (`CommunityFragment`)
- **Dynamic Activity Feed:** Real-time social feed displaying friends' milestones, active streaks, and habit achievements.
- **Interactive Reactions:** Engaging user interactions, including sending 🔥 Flame reactions and 👋 Motivational Nudges.
- **Group Challenges:** "14-Day Morning Walk Squad" challenge cards featuring visual progress indicators and participant avatars.
- **Friend Invitations:** Custom dialogue allowing users to invite friends via email to participate in shared habit challenges.

### 👤 Profile & App Settings (`ProfileFragment`, `ProfileViewModel`)
- **User Profile Management:** View personal stats, total completed habits, active streaks, and account details.
- **App Preferences:** Dark mode toggle, notification configuration, sync options, and secure user logout.

---

## 3. Technical Architecture & Tech Stack

### Architecture
- **MVVM Pattern:** Strict separation of concerns between UI components (Fragments), presentation logic (ViewModels), and data layers (Repositories).
- **Navigation Component:** Single-Activity architecture (`MainActivity`) driven by `Fragment` navigation graphs (`bottom_nav_graph.xml`) and bottom menu navigation (`bottom_nav_menu.xml`).

### Libraries & Frameworks
- **Language:** 100% Kotlin
- **UI Framework:** Android Jetpack XML Layouts with Material Design 3, Vector Drawables, and Custom Views.
- **Local Database (Room):** `AppDatabase`, `HabitDao`, `HabitCompletionDao`, `HabitEntity`, and `HabitCompletionEntity` for offline caching and instant UI feedback.
- **REST API Backend (Retrofit & Gson):** `MotionPulseApiService` connecting to backend endpoints for social community feeds, challenge invites, and remote sync.
- **Background Operations (WorkManager):** `SyncScheduler` & `SyncWorker` for deferred background database synchronization; `ReminderScheduler` & `ReminderWorker` for scheduled local habit notifications.
- **Push Notifications (Firebase Cloud Messaging):** `MotionPulseMessagingService` and `NotificationHelper` for real-time engagement alerts and reminders.

---

## 4. Database Schema & Data Flow

```
[ UI Layer: Fragments ] 
       │
       ▼
[ ViewModels ] 
       │
       ▼
[ HabitRepository ] ◄───────────► [ Local Room Database ]
       │                               (AppDatabase)
       ▼
[ REST API / Retrofit ]
(MotionPulseApiService)
```

1. **Offline-First:** User actions are immediately persisted to the local Room database (`AppDatabase`) for instant responsiveness without network latency.
2. **Background Sync:** `SyncWorker` periodically syncs pending local state with the REST backend in the background using `WorkManager`.
3. **Remote Integration:** Social feed activities and challenge invites are handled directly via Retrofit REST endpoints (`getCommunityFeed`, `sendChallengeInvite`).

---

## 5. Summary of Achievements
- Built and connected the full **Social Community Tab** featuring dynamic friend feeds, interactive nudges, and challenge cards.
- Integrated **Retrofit REST API endpoints** for community feeds and invite challenges.
- Implemented **WorkManager background tasks** and **Local Notification scheduling** for habit reminders.
- Developed **custom graphic UI elements** (`WeeklyLineChartView`) for habit completion metrics.
- Verified end-to-end architecture integrity and successful build status.
