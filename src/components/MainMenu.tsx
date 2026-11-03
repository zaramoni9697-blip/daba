import React from 'react';
import { UserSettings } from '../types/chess';
import { translations } from '../services/i18n';
import { PWAInstallButton } from './PWAInstallButton';
import {
  Bot,
  Users,
  Globe,
  History,
  Settings,
  User,
  Crown,
  Sparkles,
  Download,
  Share2,
} from 'lucide-react';

interface MainMenuProps {
  settings: UserSettings;
  onSelectMode: (mode: 'computer' | 'local' | 'online') => void;
  onOpenSettings: () => void;
  onOpenProfile: () => void;
  onOpenHistory: () => void;
  onOpenDeploy: () => void;
}

export const MainMenu: React.FC<MainMenuProps> = ({
  settings,
  onSelectMode,
  onOpenSettings,
  onOpenProfile,
  onOpenHistory,
  onOpenDeploy,
}) => {
  const t = translations[settings.language];

  return (
    <div className="relative min-h-screen w-full flex flex-col items-center justify-between p-4 sm:p-6 overflow-hidden">
      {/* Background Decorative Gradients & Vignette */}
      <div className="absolute inset-0 bg-radial from-slate-900/90 via-slate-950 to-slate-950 pointer-events-none" />
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[500px] bg-amber-500/10 rounded-full blur-3xl pointer-events-none" />

      {/* Top Bar Navigation */}
      <header className="relative z-10 w-full max-w-4xl flex items-center justify-between pt-2 pb-4">
        <div className="flex items-center gap-2.5">
          <div className="w-10 h-10 rounded-2xl bg-gradient-to-br from-amber-400 via-amber-500 to-amber-700 flex items-center justify-center text-slate-950 shadow-lg shadow-amber-500/20 border border-amber-300">
            <Crown className="w-5 h-5 fill-current" />
          </div>
          <div>
            <h1 className="text-lg font-black tracking-wider text-white uppercase flex items-center gap-1.5">
              <span>{t.appTitle}</span>
              <span className="text-[10px] px-1.5 py-0.5 rounded-full bg-amber-500/20 text-amber-300 font-bold border border-amber-500/40">
                PRO
              </span>
            </h1>
            <p className="text-[10px] text-slate-400 -mt-0.5">{t.appSubtitle}</p>
          </div>
        </div>

        {/* Header Quick Actions */}
        <div className="flex items-center gap-2">
          <PWAInstallButton label={t.installApp} />

          <button
            onClick={onOpenProfile}
            className="p-2 rounded-xl bg-slate-900/80 hover:bg-slate-800 text-slate-300 hover:text-white border border-slate-800 transition"
            title={t.profile}
          >
            <User className="w-4 h-4 text-amber-400" />
          </button>

          <button
            onClick={onOpenSettings}
            className="p-2 rounded-xl bg-slate-900/80 hover:bg-slate-800 text-slate-300 hover:text-white border border-slate-800 transition"
            title={t.settings}
          >
            <Settings className="w-4 h-4 text-slate-300" />
          </button>
        </div>
      </header>

      {/* Hero Showcase Centerpiece */}
      <main className="relative z-10 w-full max-w-md my-auto flex flex-col items-center text-center space-y-6 py-4">
        {/* Emblem */}
        <div className="relative group">
          <div className="w-24 h-24 sm:w-28 sm:h-28 rounded-3xl bg-gradient-to-b from-amber-300 via-amber-500 to-amber-700 p-0.5 shadow-2xl shadow-amber-500/25">
            <div className="w-full h-full bg-slate-950 rounded-[22px] flex items-center justify-center text-5xl sm:text-6xl select-none">
              ♚
            </div>
          </div>
          <div className="absolute -bottom-2 -right-2 p-1.5 rounded-xl bg-amber-500 text-slate-950 shadow-md">
            <Sparkles className="w-4 h-4" />
          </div>
        </div>

        {/* Title Lockup */}
        <div className="space-y-1">
          <h2 className="text-2xl sm:text-3xl font-black text-transparent bg-clip-text bg-gradient-to-r from-amber-200 via-amber-400 to-yellow-500 tracking-wide">
            {t.appTitle}
          </h2>
          <p className="text-xs text-slate-400 max-w-xs mx-auto">
            {t.appSubtitle}
          </p>
        </div>

        {/* Primary Game Mode Action Cards */}
        <div className="w-full space-y-2.5 pt-2">
          {/* Mode 1: Play vs Computer */}
          <button
            onClick={() => onSelectMode('computer')}
            className="w-full group relative flex items-center gap-4 p-4 rounded-2xl bg-gradient-to-r from-slate-900 via-slate-900/90 to-slate-900 border border-amber-500/30 hover:border-amber-400 hover:shadow-xl hover:shadow-amber-500/10 transition-all text-left transform active:scale-98"
          >
            <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-amber-500/20 to-amber-600/30 text-amber-300 flex items-center justify-center shrink-0 border border-amber-500/40 group-hover:scale-105 transition-transform">
              <Bot className="w-6 h-6" />
            </div>
            <div className="flex-1 min-w-0">
              <div className="flex items-center justify-between">
                <h3 className="font-bold text-sm text-white group-hover:text-amber-300 transition-colors">
                  {t.playVsComputer}
                </h3>
                <span className="text-[10px] font-bold text-emerald-400 bg-emerald-500/15 border border-emerald-500/30 px-2 py-0.5 rounded-full">
                  Offline AI
                </span>
              </div>
              <p className="text-xs text-slate-400 truncate mt-0.5">
                {t.playVsComputerDesc}
              </p>
            </div>
          </button>

          {/* Mode 2: Two Player Local */}
          <button
            onClick={() => onSelectMode('local')}
            className="w-full group relative flex items-center gap-4 p-4 rounded-2xl bg-gradient-to-r from-slate-900 via-slate-900/90 to-slate-900 border border-slate-800 hover:border-amber-500/50 hover:shadow-xl hover:shadow-amber-500/10 transition-all text-left transform active:scale-98"
          >
            <div className="w-12 h-12 rounded-xl bg-slate-800 text-slate-200 flex items-center justify-center shrink-0 border border-slate-700 group-hover:scale-105 transition-transform">
              <Users className="w-6 h-6" />
            </div>
            <div className="flex-1 min-w-0">
              <div className="flex items-center justify-between">
                <h3 className="font-bold text-sm text-white group-hover:text-amber-300 transition-colors">
                  {t.playLocal}
                </h3>
                <span className="text-[10px] font-bold text-sky-400 bg-sky-500/15 border border-sky-500/30 px-2 py-0.5 rounded-full">
                  Pass &amp; Play
                </span>
              </div>
              <p className="text-xs text-slate-400 truncate mt-0.5">
                {t.playLocalDesc}
              </p>
            </div>
          </button>

          {/* Mode 3: Play with Friends (Online) */}
          <button
            onClick={() => onSelectMode('online')}
            className="w-full group relative flex items-center gap-4 p-4 rounded-2xl bg-gradient-to-r from-slate-900 via-slate-900/90 to-slate-900 border border-slate-800 hover:border-amber-500/50 hover:shadow-xl hover:shadow-amber-500/10 transition-all text-left transform active:scale-98"
          >
            <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-amber-500/15 to-yellow-600/20 text-amber-300 flex items-center justify-center shrink-0 border border-amber-500/30 group-hover:scale-105 transition-transform">
              <Globe className="w-6 h-6" />
            </div>
            <div className="flex-1 min-w-0">
              <div className="flex items-center justify-between">
                <h3 className="font-bold text-sm text-white group-hover:text-amber-300 transition-colors">
                  {t.playOnline}
                </h3>
                <span className="text-[10px] font-bold text-amber-400 bg-amber-500/15 border border-amber-500/30 px-2 py-0.5 rounded-full">
                  Room Codes
                </span>
              </div>
              <p className="text-xs text-slate-400 truncate mt-0.5">
                {t.playOnlineDesc}
              </p>
            </div>
          </button>
        </div>

        {/* Secondary Navigation Row */}
        <div className="grid grid-cols-2 gap-2 w-full pt-1">
          <button
            onClick={onOpenHistory}
            className="flex items-center justify-center gap-2 py-2.5 px-3 rounded-xl bg-slate-900/70 hover:bg-slate-800/80 text-xs font-semibold text-slate-300 border border-slate-800 transition"
          >
            <History className="w-4 h-4 text-amber-400" />
            <span>{t.gameHistory}</span>
          </button>

          <button
            onClick={onOpenDeploy}
            className="flex items-center justify-center gap-2 py-2.5 px-3 rounded-xl bg-amber-500/15 hover:bg-amber-500/25 text-xs font-semibold text-amber-300 border border-amber-500/30 transition"
          >
            <Download className="w-4 h-4" />
            <span>GitHub / ZIP</span>
          </button>
        </div>
      </main>

      {/* Footer */}
      <footer className="relative z-10 w-full max-w-4xl flex items-center justify-between pt-4 pb-2 text-[11px] text-slate-500 border-t border-slate-900">
        <div className="flex items-center gap-2">
          <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
          <span>FIDE Standard Chess Rules · Offline PWA</span>
        </div>

        <button
          onClick={onOpenSettings}
          className="hover:text-slate-300 transition underline underline-offset-2"
        >
          {settings.language === 'en' ? 'বাংলা সংস্করণ' : 'English Version'}
        </button>
      </footer>
    </div>
  );
};
