import React, { useState } from 'react';
import JSZip from 'jszip';
import { Download, X, Smartphone, Globe, Check, BookOpen, Layers } from 'lucide-react';

interface GitHubDeployModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const GitHubDeployModal: React.FC<GitHubDeployModalProps> = ({ isOpen, onClose }) => {
  const [isGenerating, setIsGenerating] = useState(false);
  const [downloadSuccess, setDownloadSuccess] = useState(false);
  const [activeTab, setActiveTab] = useState<'guide' | 'zip'>('guide');

  if (!isOpen) return null;

  const handleDownloadZip = async () => {
    setIsGenerating(true);
    try {
      const zip = new JSZip();

      // Read current icon SVG if available or use inline SVG
      const iconSvg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" width="512" height="512">
  <defs>
    <radialGradient id="bg" cx="50%" cy="30%" r="75%">
      <stop offset="0%" stop-color="#1e293b"/>
      <stop offset="100%" stop-color="#020617"/>
    </radialGradient>
    <linearGradient id="gold" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#fef08a"/>
      <stop offset="40%" stop-color="#eab308"/>
      <stop offset="80%" stop-color="#ca8a04"/>
      <stop offset="100%" stop-color="#a16207"/>
    </linearGradient>
    <linearGradient id="shield" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#334155"/>
      <stop offset="100%" stop-color="#0f172a"/>
    </linearGradient>
  </defs>
  <rect width="512" height="512" rx="112" fill="url(#bg)"/>
  <rect width="504" height="504" x="4" y="4" rx="108" fill="none" stroke="url(#gold)" stroke-width="4" stroke-opacity="0.4"/>
  <path d="M 256 80 L 380 140 L 380 270 C 380 360 256 420 256 420 C 256 420 132 360 132 270 L 132 140 Z" fill="url(#shield)" stroke="url(#gold)" stroke-width="6"/>
  <path d="M 196 170 L 216 220 L 256 160 L 296 220 L 316 170 L 316 240 L 196 240 Z" fill="url(#gold)"/>
  <path d="M 210 350 C 210 320 220 295 240 280 C 240 270 230 260 226 248 C 220 230 234 218 248 226 C 260 216 278 222 284 236 C 294 258 288 286 278 304 C 290 310 302 324 302 350 Z" fill="url(#gold)"/>
  <rect x="180" y="348" width="152" height="18" rx="6" fill="url(#gold)" />
</svg>`;

      const manifestContent = JSON.stringify({
        id: "./",
        name: "Royal Chess - Mobile & Desktop Chess Master",
        short_name: "RoyalChess",
        description: "Professional chess game with AI difficulty levels, 2-player local, and offline PWA support.",
        start_url: "./index.html",
        scope: "./",
        display: "standalone",
        orientation: "portrait-primary",
        theme_color: "#0b1120",
        background_color: "#020617",
        icons: [
          {
            src: "./icons/icon.svg",
            sizes: "any",
            type: "image/svg+xml",
            purpose: "any"
          }
        ]
      }, null, 2);

      const swContent = `// Standalone Royal Chess Service Worker for GitHub Pages
const CACHE_NAME = 'royal-chess-gh-v1';
const PRECACHE = ['./', './index.html', './manifest.json', './icons/icon.svg'];

self.addEventListener('install', (e) => {
  e.waitUntil(caches.open(CACHE_NAME).then((c) => c.addAll(PRECACHE)).then(() => self.skipWaiting()));
});

self.addEventListener('activate', (e) => {
  e.waitUntil(caches.keys().then((keys) => Promise.all(keys.filter((k) => k !== CACHE_NAME).map((k) => caches.delete(k)))).then(() => self.clients.claim()));
});

self.addEventListener('fetch', (e) => {
  if (e.request.method !== 'GET') return;
  e.respondWith(
    caches.match(e.request).then((res) => res || fetch(e.request).then((net) => {
      if (net && net.status === 200) {
        const clone = net.clone();
        caches.open(CACHE_NAME).then((c) => c.put(e.request, clone));
      }
      return net;
    }).catch(() => caches.match('./index.html')))
  );
});`;

      const readmeContent = `# Royal Chess 👑
A professional, mobile-first Chess application designed for Android and desktop browsers.

## 🚀 How to Deploy on GitHub Pages Using Only an Android Phone

You do **not** need a computer or PC to host this game. You can deploy it in 3 minutes entirely from your Android phone:

### Step 1: Open GitHub in Desktop Mode on Android
1. Open **Google Chrome** on your Android phone.
2. Go to **[https://github.com](https://github.com)** and log in.
3. Tap the **three vertical dots (⋮)** in the top-right corner of Chrome.
4. Check the box **"Desktop site"** (this enables full file uploading on phone).

### Step 2: Create a New Repository
1. Tap the green **"New"** button (or top-right **+** -> **New repository**).
2. Repository name: \`royal-chess\`.
3. Choose **Public**.
4. Leave other options as default and tap **Create repository**.

### Step 3: Upload the Project Files
1. On your new repository screen, tap the link that says: **"uploading an existing file"**.
2. Tap **"choose your files"** and select all the files from this unzipped folder:
   - \`index.html\`
   - \`manifest.json\`
   - \`service-worker.js\`
   - \`icons/\` folder
   - \`README.md\`
3. Scroll down to "Commit changes" and tap the green **Commit changes** button.

### Step 4: Turn On GitHub Pages
1. In your repository menu, tap **Settings** (gear icon).
2. In the left sidebar, tap **Pages**.
3. Under **Build and deployment** -> **Branch**:
   - Change \`None\` to **\`main\`** (or \`master\`).
   - Leave folder as **\`/ (root)\`**.
   - Tap **Save**.

### Step 5: Play & Install on Your Phone!
- Wait 60 seconds, then refresh the page.
- GitHub Pages will show: *"Your site is live at https://<your-username>.github.io/royal-chess/"*.
- Open that link on your phone in Chrome!
- Tap the **"Install Royal Chess"** button or Chrome menu -> **Add to Home screen**.
- Royal Chess will now work **completely offline** anytime, anywhere!
`;

      const standaloneHtml = `<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover" />
  <title>Royal Chess - Mobile & Desktop Chess Master</title>
  <meta name="theme-color" content="#0b1120" />
  <link rel="manifest" href="./manifest.json" />
  <link rel="icon" type="image/svg+xml" href="./icons/icon.svg" />
  <script src="https://cdn.tailwindcss.com"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/chess.js/0.13.4/chess.min.js"></script>
</head>
<body class="bg-slate-950 text-slate-100 min-h-screen flex flex-col items-center justify-center p-4">
  <div class="max-w-md w-full bg-slate-900 border border-amber-500/40 rounded-3xl p-6 shadow-2xl text-center">
    <div class="w-16 h-16 mx-auto mb-4 bg-amber-500/20 rounded-2xl flex items-center justify-center text-3xl border border-amber-500/50">👑</div>
    <h1 class="text-2xl font-black text-amber-400 tracking-wide">ROYAL CHESS</h1>
    <p class="text-xs text-slate-400 mt-1 mb-6">Mobile-First PWA Edition</p>
    <div id="game-container" class="space-y-4">
      <div id="status" class="text-sm font-semibold text-slate-200 bg-slate-800/80 py-2 rounded-xl border border-slate-700">White to move</div>
      <div id="board" class="grid grid-cols-8 grid-rows-8 aspect-square w-full rounded-2xl overflow-hidden border-2 border-amber-600/60 shadow-xl bg-amber-900"></div>
      <div class="flex gap-2 justify-center pt-2">
        <button id="reset-btn" class="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs font-bold border border-slate-700">Restart</button>
        <button id="flip-btn" class="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs font-bold border border-slate-700">Flip</button>
      </div>
    </div>
  </div>
  <script>
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.register('./service-worker.js').catch(console.error);
    }
    const chess = new Chess();
    let flipped = false;
    let selected = null;
    const boardEl = document.getElementById('board');
    const statusEl = document.getElementById('status');
    const PIECES = { p: '♟', r: '♜', n: '♞', b: '♝', q: '♛', k: '♚' };

    function render() {
      boardEl.innerHTML = '';
      const b = chess.board();
      const ranks = flipped ? [0,1,2,3,4,5,6,7] : [7,6,5,4,3,2,1,0];
      const files = flipped ? [7,6,5,4,3,2,1,0] : [0,1,2,3,4,5,6,7];
      for (const r of ranks) {
        for (const f of files) {
          const sq = String.fromCharCode(97 + f) + (r + 1);
          const p = chess.get(sq);
          const isDark = (r + f) % 2 === 0;
          const cell = document.createElement('div');
          cell.className = 'flex items-center justify-center text-3xl sm:text-4xl select-none cursor-pointer ' + (isDark ? 'bg-amber-800 text-amber-200' : 'bg-amber-100 text-amber-950');
          if (selected === sq) cell.classList.add('ring-4', 'ring-yellow-400');
          if (p) {
            cell.innerText = PIECES[p.type];
            if (p.color === 'w') cell.style.color = '#ffffff';
            else cell.style.color = '#1e293b';
            cell.style.textShadow = '0 2px 4px rgba(0,0,0,0.6)';
          }
          cell.onclick = () => {
            if (selected) {
              const move = chess.move({ from: selected, to: sq, promotion: 'q' });
              selected = null;
              render();
              updateStatus();
              return;
            }
            if (p && p.color === chess.turn()) {
              selected = sq;
              render();
            }
          };
          boardEl.appendChild(cell);
        }
      }
    }

    function updateStatus() {
      if (chess.in_checkmate()) statusEl.innerText = 'CHECKMATE! ' + (chess.turn() === 'w' ? 'Black' : 'White') + ' Wins!';
      else if (chess.in_draw()) statusEl.innerText = 'Game Draw!';
      else statusEl.innerText = (chess.turn() === 'w' ? 'White' : 'Black') + "'s turn" + (chess.in_check() ? ' (CHECK!)' : '');
    }

    document.getElementById('reset-btn').onclick = () => { chess.reset(); selected = null; render(); updateStatus(); };
    document.getElementById('flip-btn').onclick = () => { flipped = !flipped; render(); };
    render();
    updateStatus();
  </script>
</body>
</html>`;

      // Add files to ZIP
      zip.file('index.html', standaloneHtml);
      zip.file('manifest.json', manifestContent);
      zip.file('service-worker.js', swContent);
      zip.file('README.md', readmeContent);
      zip.folder('icons')?.file('icon.svg', iconSvg);

      // Generate blob
      const blob = await zip.generateAsync({ type: 'blob' });
      const downloadUrl = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = downloadUrl;
      link.download = 'royal-chess-github-pages.zip';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(downloadUrl);

      setDownloadSuccess(true);
      setTimeout(() => setDownloadSuccess(false), 4000);
    } catch (err) {
      console.error('ZIP generation error:', err);
    } finally {
      setIsGenerating(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-xs p-4 animate-fadeIn">
      <div className="bg-slate-900 border border-amber-500/40 rounded-3xl max-w-lg w-full max-h-[90vh] flex flex-col shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-950/60">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-xl bg-amber-500/20 text-amber-400 border border-amber-500/30">
              <Globe className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-bold text-base text-slate-100">Deploy to GitHub Pages</h3>
              <p className="text-xs text-slate-400">Step-by-step Android phone guide & standalone ZIP</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Tab Selector */}
        <div className="flex border-b border-slate-800 bg-slate-950/40 px-6 pt-2">
          <button
            onClick={() => setActiveTab('guide')}
            className={`flex items-center gap-2 pb-3 px-3 text-xs font-bold border-b-2 transition ${
              activeTab === 'guide'
                ? 'border-amber-400 text-amber-400'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            <Smartphone className="w-4 h-4" />
            Android Phone Guide
          </button>
          <button
            onClick={() => setActiveTab('zip')}
            className={`flex items-center gap-2 pb-3 px-3 text-xs font-bold border-b-2 transition ${
              activeTab === 'zip'
                ? 'border-amber-400 text-amber-400'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            <Download className="w-4 h-4" />
            Download ZIP
          </button>
        </div>

        {/* Content */}
        <div className="p-6 overflow-y-auto space-y-4 text-xs text-slate-300 scrollbar-thin">
          {activeTab === 'guide' ? (
            <div className="space-y-4">
              <div className="bg-amber-500/10 border border-amber-500/30 rounded-2xl p-3.5 flex items-start gap-3">
                <BookOpen className="w-5 h-5 text-amber-400 shrink-0 mt-0.5" />
                <div>
                  <h4 className="font-bold text-amber-300 text-xs mb-0.5">No Computer Needed!</h4>
                  <p className="text-[11px] text-slate-300 leading-relaxed">
                    You can deploy this chess app directly to GitHub Pages using only your Android smartphone’s Chrome browser.
                  </p>
                </div>
              </div>

              <div className="space-y-3">
                <div className="flex items-start gap-3 bg-slate-950/60 p-3 rounded-2xl border border-slate-800">
                  <span className="w-6 h-6 rounded-full bg-amber-500/20 text-amber-300 font-bold flex items-center justify-center shrink-0 text-xs">
                    1
                  </span>
                  <div>
                    <strong className="text-white block mb-0.5">Desktop Site Mode in Chrome</strong>
                    <p className="text-slate-400 text-[11px]">
                      Open Chrome on Android, go to <code>github.com</code>, tap the 3 dots (⋮), and check <strong>Desktop site</strong>.
                    </p>
                  </div>
                </div>

                <div className="flex items-start gap-3 bg-slate-950/60 p-3 rounded-2xl border border-slate-800">
                  <span className="w-6 h-6 rounded-full bg-amber-500/20 text-amber-300 font-bold flex items-center justify-center shrink-0 text-xs">
                    2
                  </span>
                  <div>
                    <strong className="text-white block mb-0.5">Create Public Repo</strong>
                    <p className="text-slate-400 text-[11px]">
                      Tap <strong>New</strong> repository, name it <code>royal-chess</code>, select <strong>Public</strong>, and tap Create.
                    </p>
                  </div>
                </div>

                <div className="flex items-start gap-3 bg-slate-950/60 p-3 rounded-2xl border border-slate-800">
                  <span className="w-6 h-6 rounded-full bg-amber-500/20 text-amber-300 font-bold flex items-center justify-center shrink-0 text-xs">
                    3
                  </span>
                  <div>
                    <strong className="text-white block mb-0.5">Upload Downloaded ZIP Files</strong>
                    <p className="text-slate-400 text-[11px]">
                      Download the ZIP below, extract it on your phone, then tap <strong>uploading an existing file</strong> on GitHub and commit changes.
                    </p>
                  </div>
                </div>

                <div className="flex items-start gap-3 bg-slate-950/60 p-3 rounded-2xl border border-slate-800">
                  <span className="w-6 h-6 rounded-full bg-amber-500/20 text-amber-300 font-bold flex items-center justify-center shrink-0 text-xs">
                    4
                  </span>
                  <div>
                    <strong className="text-white block mb-0.5">Enable GitHub Pages</strong>
                    <p className="text-slate-400 text-[11px]">
                      Go to <strong>Settings</strong> &gt; <strong>Pages</strong> &gt; Branch: <strong>main</strong> / (root) &gt; Save. Your chess game will be live in 60 seconds!
                    </p>
                  </div>
                </div>
              </div>
            </div>
          ) : (
            <div className="space-y-4">
              <div className="bg-slate-950/60 border border-slate-800 rounded-2xl p-4 space-y-2">
                <div className="flex items-center gap-2 text-amber-400 font-bold">
                  <Layers className="w-4 h-4" />
                  <span>Package Contents</span>
                </div>
                <ul className="space-y-1.5 text-slate-300 text-[11px] list-disc list-inside">
                  <li><code>index.html</code> (Complete standalone chess app)</li>
                  <li><code>manifest.json</code> (PWA offline installation)</li>
                  <li><code>service-worker.js</code> (Asset precaching)</li>
                  <li><code>icons/icon.svg</code> (Vector app icon)</li>
                  <li><code>README.md</code> (Full Android deployment guide)</li>
                </ul>
              </div>

              <div className="text-center pt-2">
                <button
                  onClick={handleDownloadZip}
                  disabled={isGenerating}
                  className="w-full flex items-center justify-center gap-2 py-3 px-4 rounded-2xl bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 font-bold shadow-lg transition active:scale-95 disabled:opacity-50"
                >
                  {downloadSuccess ? (
                    <>
                      <Check className="w-4 h-4" />
                      <span>ZIP Downloaded!</span>
                    </>
                  ) : (
                    <>
                      <Download className="w-4 h-4" />
                      <span>{isGenerating ? 'Packaging Files...' : 'Download royal-chess-github-pages.zip'}</span>
                    </>
                  )}
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-slate-800 bg-slate-950/60 flex items-center justify-between">
          <span className="text-[11px] text-slate-500">Royal Chess v1.0 · PWA Ready</span>
          <button
            onClick={handleDownloadZip}
            disabled={isGenerating}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-amber-500/20 text-amber-300 border border-amber-500/40 text-xs font-semibold hover:bg-amber-500/30 transition"
          >
            <Download className="w-3.5 h-3.5" />
            <span>Download ZIP</span>
          </button>
        </div>
      </div>
    </div>
  );
};
