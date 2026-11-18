# Cloud AI Video Generator — Android

A native Android text-to-video app powered by Runway Dev / Seedance 2.5.

## Included

- Text-to-video
- Seedance 2.5
- 4–30 second duration
- 480p / 720p / 1080p / 4K quality presets
- 16:9 / 9:16 / 1:1 / 4:3 / 3:4
- Draft preview mode
- Generated audio toggle
- Settings screen for the user's Runway API key
- Encrypted API-key storage
- Generation history (last 30)
- Open, save and share generated videos
- Custom launcher icon
- Material 3 UI

## Runway support

Seedance 2.5 supports 4–30 seconds and pixel-based ratios across 480p, 720p, 1080p and 4K. 4K uses 3840×2160 for 16:9 and 2160×3840 for 9:16.

## Security

The API key is stored encrypted on the device. This is appropriate for a personal app where the user supplies their own key, but a client-side secret cannot be made absolutely unrecoverable. For a public SaaS, move the Runway key to a backend.

## Build

Requirements:
- Android Studio
- JDK 17
- Android SDK 35

Open this directory in Android Studio, sync Gradle, then:
- Build > Generate Bundle(s) / APK(s) > Generate APK(s), or
- Build > Generate Signed Bundle / APK for a Play Store release.

The Android documentation recommends a signed Android App Bundle for Play distribution.

Debug APK:
app/build/outputs/apk/debug/app-debug.apk
