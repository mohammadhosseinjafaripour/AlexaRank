# AlexaRank (Android)

Simple Android app that fetches and displays Alexa rank information for a
website using the public minisite endpoint. The UI lets users enter a URL and
shows global rank, country rank, and linking sites count, plus a country flag.

## What This App Does

- Accepts a website URL
- Normalizes to a domain and queries Alexa minisite info
- Displays global rank, country rank, and linking sites count

## Features

- URL validation and normalization
- Fetch rank data from `https://www.alexa.com/minisiteinfo/<domain>`
- Shows global rank, country rank, and linking sites
- Loading indicator and small UI animations

## Tech Stack

- Android (Java)
- Data Binding
- Jsoup (HTML parsing)
- Glide (image + GIF loading)
- Material Components

## Requirements

- Android Studio
- Android SDK 29 (compile/target)
- JDK 8+

## Getting Started

1. Open the project in Android Studio.
2. Let Gradle sync dependencies.
3. Run the `app` configuration on an emulator or device.

## Build

From the project root:

```
./gradlew assembleDebug
```

On Windows (PowerShell):

```
.\gradlew.bat assembleDebug
```

## Permissions

- `android.permission.INTERNET` (required for network calls)

## Notes

- The data source is Alexa's minisite endpoint. If the endpoint changes or is
  unavailable, the app will not return rank data.
- Amazon shut down the Alexa.com ranking service on May 1, 2022.

