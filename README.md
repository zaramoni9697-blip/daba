# JARVIS Android v1.1.0

Full integration foundation for the JARVIS Android assistant.

Pipeline:
Hey JARVIS -> wake service -> command speech -> command router -> confirmation/action or AI -> memory -> TTS

Included:
- Bengali/English wake phrase detection
- Foreground microphone service with notification
- Local conversation memory
- Encrypted API keys via Android Keystore
- Gemini, OpenAI-compatible, Groq and Custom provider routing
- Real app launching for common apps
- User-enabled Accessibility Service for Home, Back, Recents, Notifications, Quick Settings and scrolling
- Confirmation gate for call/SMS
- TTS response
- GitHub Actions debug APK build

Important:
Android permissions and background-service restrictions still apply. Accessibility is never silently enabled.
