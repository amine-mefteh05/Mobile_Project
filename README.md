# UniMate — University Life, Simplified

UniMate is an offline-first, single-user mobile application built for Android in **Java** and **XML** using an **MVC architecture** with a SQLite repository pattern. It is designed as an intelligent academic assistant to help university students manage tasks, plan weekly/daily schedules, calculate budget expenses, discover study places, find academic opportunities, and receive smart recommendations.

---

## 🚀 How to Open and Run in Android Studio

1. **Extract ZIP File**: Extract `UniMate_Android_Project.zip` to your preferred folder on disk.
2. **Open in Android Studio**:
   - Launch Android Studio (Jellyfish / Koala / Ladybug or newer recommended).
   - Select **File > Open...** and navigate to the extracted folder (`UniMate`).
   - Click **OK**.
3. **Gradle Sync**:
   - Android Studio will automatically perform a Gradle Sync.
   - If prompted, click **Sync Project with Gradle Files**.
4. **Run Application**:
   - Connect an Android device or launch an Emulator running **Android 8.0 (API Level 26) or higher**.
   - Select the `:app` run configuration and click **Run** (or press `Shift + F10`).

---

## ⚙️ Project & SDK Specifications

- **Application Name**: UniMate
- **Package Name**: `com.unimate`
- **Language**: Java & XML Layouts
- **Architecture Pattern**: Model-View-Controller (MVC) + Repository Layer + Pure Java Intelligence Layer
- **Minimum SDK**: 26 (Android 8.0 Oreo)
- **Target SDK / Compile SDK**: 35 / 37
- **Database**: SQLite (`DatabaseHelper` & `SQLiteOpenHelper`)
- **Preferences**: `SharedPreferences` (`PreferencesManager`)
- **Build System**: Gradle Version Catalog / Kotlin DSL (`build.gradle.kts`)

---

## 🏗️ Architecture & Package Overview

UniMate strictly follows the layered architecture specified in the Conception Dossier:

```text
com.unimate
│
├── model/                  # Domain entities (Task, Event, StudyPlace, Opportunity, Expense, Favorite, Student)
├── dto/                    # Data Transfer Objects (Suggestion, TimeSlot, PlanProposal, Weights, ScoredPlace, WeeklySummary)
├── database/               # SQLite helper, SeedData initializer, and PreferencesManager
├── repository/             # Data access layer with raw SQL queries (TaskRepository, EventRepository, StudySpotRepository, etc.)
├── controller/             # Business controllers & form input validators (TaskController, PlannerController, etc.)
├── intelligence/           # Pure Java rule-based recommendation & scheduling algorithms (No Android imports!)
│   ├── TaskPrioritizer.java
│   ├── ScheduleOptimizer.java
│   ├── StudySpotScorer.java
│   ├── BudgetAnalyzer.java
│   ├── ProductivityAnalyzer.java
│   └── RecommendationEngine.java
├── receiver/               # AlarmManager & Notification BroadcastReceivers (ReminderReceiver, ReminderScheduler)
├── adapter/                # RecyclerView Adapters for tasks, events, places, opportunities, expenses
└── view/                   # Activities & Fragments (MainActivity, HomeFragment, PlannerFragment, DiscoverFragment, TasksFragment, ProfileFragment, etc.)
```

---

## 📌 Implementation Highlights & PDF Resolution

Per the initial project specification guidelines:

1. **PDF Conflict Resolution**:
   - Followed the **Conception Dossier** as the first-release spec.
   - **Included Core Modules**: Tasks, Weekly Planner / Daily Agenda, Smart Assistant, StudySpots, Opportunities, Budget, and Profile.
   - **Excluded Out-of-Scope Features**: Community project requests, user registration/login, cloud sync, live maps/feeds, and external AI services are treated as future extensions.
   - **Bottom Navigation**: 5 tabs (`Home`, `Planner`, `Discover`, `Tasks`, `Profile`). Budget is accessible as a supporting screen from Home and Profile.

2. **Database & Data Layer**:
   - 6 SQLite tables (`tasks`, `events`, `study_places`, `opportunities`, `expenses`, `favorites`).
   - Profile, day window (`08:00` - `20:00`), weekly budget limit, ranking weights, and notification settings live in `SharedPreferences`.
   - Sample study places near campus and relative opportunity deadlines are seeded automatically on first launch (`SeedData.java`).
   - Deleting a task automatically removes its linked `STUDY` events from the planner.

3. **Pure Java Rule-Based Smart Assistant**:
   - All classes in `com.unimate.intelligence` are written in **pure Java** (using standard `java.time.*` and collections) without any `android.*` UI/framework dependencies.
   - Tested using JUnit unit tests (`IntelligenceUnitTest`).
   - **"What Should I Do Now?"**: Evaluates study day window, active ongoing events, remaining free slot duration, and pending task priority/urgency. Recommends top matching task and study spot.
   - **"Plan My Week"**: Calculates free slots between `day_start` and `day_end`, caps study blocks at 90 minutes, inserts 15-minute breaks after full blocks, and returns a `PlanProposal`.
   - **Student Control**: Plans are strictly proposals; events are only saved when explicitly approved by the student. Existing lectures/events are never altered or moved.

4. **StudySpot Ranking & Tight Budget Rule**:
   - Places are scored via a multi-factor formula evaluating quietness, Wi-Fi quality, price level, distance, and power outlet availability.
   - **Tight Budget Rule**: When weekly spending reaches >= 80% of the budget limit (`isBudgetTight() == true`), the budget weight is automatically doubled before normalization.

5. **Opportunities & Cross-Module Contract**:
   - Demo deadlines are computed relative to `LocalDate.now()` so they remain valid during evaluation.
   - "Add Deadline as Task" calls `OpportunityController.addDeadlineAsTask()` which invokes `TaskRepository.insert()` inside a `try-catch` defensive integration block.

---

## 🧪 Verification & Build Status

- **Build Verification**: Built successfully using Gradle task `:app:assembleDebug`.
- **Unit Tests Verification**: Ran unit test suite `:app:testDebugUnitTest`. **All 7 tests passed (0 failures, 0 skipped)**.
- **Gradle Version**: Configured with compatible AGP version, Kotlin DSL gradle build scripts, and Android SDK 35/26.
