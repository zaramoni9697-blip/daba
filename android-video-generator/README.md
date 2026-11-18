# Cloud AI Video Generator — Android

Native Android app for text-to-video generation with Runway Dev.

Features:
- Seedance 2.5 text-to-video
- 4–15 second duration
- 16:9 and 9:16
- Runway API key entered from Settings
- Encrypted API-key storage using AndroidX Security Crypto
- Task polling until completion
- Open, save and share generated videos

Important: a user-supplied API key can be stored encrypted on the device, but no client-side secret is perfectly protected. For a public multi-user production app, use an app → backend → Runway architecture so the Runway secret stays on the server.

Build with Android Studio, JDK 17 and Android SDK 35:
    ./gradlew assembleDebug

APK:
    app/build/outputs/apk/debug/app-debug.apk

Run:
1. Install the APK.
2. Open Settings.
3. Enter your Runway API key.
4. Enter a prompt.
5. Choose duration and ratio.
6. Generate.

The app uses Runway API version 2024-11-06 over HTTPS.
