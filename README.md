# Royal Chess 👑
> **Mobile & Desktop Chess Master** — A production-quality, mobile-first Chess application built with React, TypeScript, Tailwind CSS, Chess.js, Minimax AI engine, and real-time multiplayer room synchronization.

---

## 🌟 Key Features

1. **Play Against Computer AI**
   - 4 calibrated AI difficulty levels: **Easy (~800)**, **Medium (~1400)**, **Hard (~1900)**, and **Expert (~2300+)**.
   - Tournament-grade Minimax search with Alpha-Beta pruning, Piece-Square Tables (PST) for midgame and endgame, and Quiescence search to eliminate horizon effects.
   - Choose White, Black, or Random side.
   - Realistic deliberate thinking time (300ms–900ms).

2. **Two Player Local Mode**
   - Pass & play on the same screen (ideal for Android phones and tablets).
   - Optional 180° auto-board flip on each turn.
   - Move undo, restart, and game analysis.

3. **Online Multiplayer with Friends**
   - Instant private rooms with unique 6-character room codes (e.g. `ROY-8429`).
   - Shareable direct invite links (`?room=CODE`).
   - Server-Sent Events (SSE) sub-second move synchronization and clock updates.
   - Live opponent connection status (active/disconnected detection).
   - Mutual draw offers and resignation actions.

4. **Real-Time Clocks & Time Controls**
   - **Bullet**: 1+0, 2+1
   - **Blitz**: 3+0, 3+2, 5+0
   - **Rapid**: 10+0, 15+10
   - **Casual**: Unlimited (no clock)
   - High-precision tenths-of-a-second readout under 10 seconds.
   - Low-time audio warnings & pulsing alarms.
   - FIDE Insufficient Material rule evaluation on timeout (Draw if opponent cannot mate).

5. **Visual Themes & Audio**
   - **Board Themes**: Classic Walnut Wood, Emerald Tournament, Royal Gold, Midnight Obsidian, Marble Slate.
   - **Piece Themes**: Classic Neo, Staunton Tournament, Vintage Wood, Modern Geometric.
   - **Procedural Synthesizer**: Web Audio API sounds for moves, captures, checks, castling, and victories (100% offline, 0kb audio downloads).

6. **Bilingual Support**
   - Full native localization in **English** and **বাংলা (Bengali)**.

7. **Progressive Web App (PWA) & Offline**
   - Complete offline functionality via Service Worker caching.
   - In-app install banner with Android & iOS Home Screen guidance.

---

## 📱 How to Deploy to GitHub Pages Using Only an Android Phone

You do **not** need a computer to host this game. Follow these steps on your phone:

### Step 1: Switch Chrome on Android to Desktop Site
1. Open **Google Chrome** on your phone.
2. Go to **[https://github.com](https://github.com)** and log in.
3. Tap the **three vertical dots (⋮)** in the top-right corner of Chrome.
4. Tap **"Desktop site"** to check the box.

### Step 2: Create a New GitHub Repository
1. Tap the green **New** repository button.
2. Set the repository name to `royal-chess`.
3. Choose **Public**.
4. Tap **Create repository**.

### Step 3: Upload Project Files
1. Open Royal Chess and tap the **GitHub / ZIP** button on the main menu.
2. Tap **Download royal-chess-github-pages.zip**.
3. Extract the downloaded ZIP file using your Android Files app.
4. On your new GitHub repository page, tap the link: **"uploading an existing file"**.
5. Select the extracted files (`index.html`, `manifest.json`, `service-worker.js`, `README.md`, and `icons/` folder).
6. Tap **Commit changes**.

### Step 4: Turn On GitHub Pages
1. Go to repository **Settings** (gear icon) > **Pages**.
2. Under **Build and deployment** > **Branch**:
   - Change `None` to **`main`** (or `master`).
   - Folder: **`/(root)`**.
   - Tap **Save**.

### Step 5: Play Anywhere!
- Your game will be live at `https://<your-github-username>.github.io/royal-chess/`.
- Open that link on any smartphone, tap **Install Royal Chess**, and enjoy offline chess forever!
