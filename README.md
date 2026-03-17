# Memento — Life in Weeks

An Android app that visualizes your life as a grid of weeks, helping you reflect on time passed and time remaining.

## Concept

Each cell in the grid represents one week of your life. Weeks already lived are filled in, and future weeks remain empty — a simple but powerful reminder to be intentional with your time.

## Project Structure

```
life-in-weeks-app/
├── life-in-weeks.html      # App prototype / design reference
├── app/                    # Android app module (created by Android Studio)
│   ├── src/
│   │   └── main/
│   │       ├── java/       # Kotlin/Java source files
│   │       ├── res/        # Resources (layouts, drawables, strings)
│   │       └── AndroidManifest.xml
│   └── build.gradle
├── gradle/
├── build.gradle
└── settings.gradle
```

## Prerequisites

- [Android Studio](https://developer.android.com/studio) (latest stable)
- Android SDK API 26+ (Android 8.0 Oreo)
- JDK 17 or later

## Getting Started

1. Clone the repository:
   ```bash
   git clone <repo-url>
   cd life-in-weeks-app
   ```

2. Open the project in Android Studio:
   - Launch Android Studio
   - Select **Open** and navigate to this folder

3. Let Gradle sync and download dependencies.

4. Run the app on an emulator or physical device via **Run > Run 'app'**.

## Design Reference

The file `life-in-weeks.html` is an interactive browser prototype used to design and iterate on the UI before implementing it natively in Android.

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose / XML layouts
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** Latest stable

## License

This project is for personal study and learning purposes.
