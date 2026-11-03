import React from 'react';
import { UserSettings, BoardTheme, PieceTheme } from '../types/chess';
import { Language, translations } from '../services/i18n';
import { X, Volume2, VolumeX, Palette, Globe, Check, Eye } from 'lucide-react';
import { sounds } from '../services/soundService';

interface SettingsModalProps {
  isOpen: boolean;
  onClose: () => void;
  settings: UserSettings;
  onUpdateSettings: (newSettings: Partial<UserSettings>) => void;
}

export const SettingsModal: React.FC<SettingsModalProps> = ({
  isOpen,
  onClose,
  settings,
  onUpdateSettings,
}) => {
  if (!isOpen) return null;
  const t = translations[settings.language];

  const boardThemes: { id: BoardTheme; name: string; preview: [string, string] }[] = [
    { id: 'walnut', name: 'Classic Walnut', preview: ['#f0d9b5', '#b58863'] },
    { id: 'emerald', name: 'Emerald Tournament', preview: ['#eeeed2', '#769656'] },
    { id: 'royal', name: 'Royal Gold', preview: ['#fef9c3', '#b45309'] },
    { id: 'midnight', name: 'Midnight Obsidian', preview: ['#94a3b8', '#334155'] },
    { id: 'marble', name: 'Marble Slate', preview: ['#f1f5f9', '#64748b'] },
  ];

  const pieceThemes: { id: PieceTheme; name: string; tag: string }[] = [
    { id: 'royal', name: 'Royal 3D Gold (রাজকীয়)', tag: '👑 3D Jewels' },
    { id: 'staunton', name: 'Master Staunton (টুর্নামেন্ট)', tag: '🏆 Tournament' },
    { id: 'vintage', name: 'Carved Wood (খোদাইকৃত)', tag: '🪵 Antique' },
    { id: 'neon', name: 'Cyber Neon (নিয়ন সাইবার)', tag: '⚡ Radiant' },
    { id: 'gothic', name: 'Gothic Armor (মধ্যযুগীয়)', tag: '⚔️ Heraldic' },
  ];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-xs p-4 animate-fadeIn">
      <div className="bg-slate-900 border border-amber-500/40 rounded-3xl max-w-md w-full shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-950/60">
          <div className="flex items-center gap-2">
            <Palette className="w-5 h-5 text-amber-400" />
            <h3 className="font-bold text-base text-slate-100">{t.settings}</h3>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 overflow-y-auto space-y-5 text-sm scrollbar-thin">
          {/* Language Selection */}
          <div className="space-y-2">
            <label className="text-xs font-semibold text-slate-400 flex items-center gap-1.5 uppercase tracking-wider">
              <Globe className="w-4 h-4 text-amber-400" />
              {t.language}
            </label>
            <div className="grid grid-cols-2 gap-2">
              <button
                onClick={() => onUpdateSettings({ language: 'en' })}
                className={`flex items-center justify-between px-3 py-2.5 rounded-xl border transition ${
                  settings.language === 'en'
                    ? 'bg-amber-500/20 border-amber-400 text-amber-300 font-bold'
                    : 'bg-slate-950 border-slate-800 text-slate-300 hover:border-slate-700'
                }`}
              >
                <span>English</span>
                {settings.language === 'en' && <Check className="w-4 h-4 text-amber-400" />}
              </button>
              <button
                onClick={() => onUpdateSettings({ language: 'bn' })}
                className={`flex items-center justify-between px-3 py-2.5 rounded-xl border transition ${
                  settings.language === 'bn'
                    ? 'bg-amber-500/20 border-amber-400 text-amber-300 font-bold'
                    : 'bg-slate-950 border-slate-800 text-slate-300 hover:border-slate-700'
                }`}
              >
                <span>বাংলা (Bengali)</span>
                {settings.language === 'bn' && <Check className="w-4 h-4 text-amber-400" />}
              </button>
            </div>
          </div>

          {/* Sound Toggle */}
          <div className="flex items-center justify-between bg-slate-950/80 p-3.5 rounded-2xl border border-slate-800">
            <div className="flex items-center gap-2.5">
              {settings.soundEnabled ? (
                <Volume2 className="w-5 h-5 text-amber-400" />
              ) : (
                <VolumeX className="w-5 h-5 text-slate-500" />
              )}
              <div>
                <span className="font-semibold text-slate-200 block text-xs">{t.sound}</span>
                <span className="text-[11px] text-slate-400">
                  {settings.soundEnabled ? t.soundOn : t.soundMuted}
                </span>
              </div>
            </div>
            <button
              onClick={() => {
                const next = !settings.soundEnabled;
                sounds.setMuted(!next);
                onUpdateSettings({ soundEnabled: next });
                if (next) sounds.playMove();
              }}
              className={`w-12 h-6 rounded-full transition-colors relative p-0.5 ${
                settings.soundEnabled ? 'bg-amber-500' : 'bg-slate-700'
              }`}
            >
              <div
                className={`w-5 h-5 rounded-full bg-white shadow-md transform transition-transform ${
                  settings.soundEnabled ? 'translate-x-6' : 'translate-x-0'
                }`}
              />
            </button>
          </div>

          {/* Board Theme */}
          <div className="space-y-2">
            <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              {t.boardTheme}
            </label>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
              {boardThemes.map((b) => (
                <button
                  key={b.id}
                  onClick={() => onUpdateSettings({ boardTheme: b.id })}
                  className={`flex items-center gap-3 p-2.5 rounded-xl border text-left transition ${
                    settings.boardTheme === b.id
                      ? 'bg-amber-500/15 border-amber-400 text-amber-300'
                      : 'bg-slate-950 border-slate-800 text-slate-300 hover:border-slate-700'
                  }`}
                >
                  <div className="w-6 h-6 rounded-lg overflow-hidden flex border border-slate-700 shrink-0">
                    <div className="w-1/2 h-full" style={{ backgroundColor: b.preview[0] }} />
                    <div className="w-1/2 h-full" style={{ backgroundColor: b.preview[1] }} />
                  </div>
                  <span className="text-xs font-medium truncate">{b.name}</span>
                </button>
              ))}
            </div>
          </div>

          {/* Piece Style */}
          <div className="space-y-2">
            <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              {t.pieceStyle}
            </label>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
              {pieceThemes.map((p) => (
                <button
                  key={p.id}
                  onClick={() => onUpdateSettings({ pieceTheme: p.id })}
                  className={`p-2.5 rounded-xl border text-left transition flex items-center justify-between ${
                    settings.pieceTheme === p.id
                      ? 'bg-amber-500/15 border-amber-400 text-amber-300'
                      : 'bg-slate-950 border-slate-800 text-slate-300 hover:border-slate-700'
                  }`}
                >
                  <span className="text-xs font-semibold">{p.name}</span>
                  <span className="text-[10px] px-1.5 py-0.5 rounded-md bg-slate-900 border border-slate-700 text-slate-400 font-mono">
                    {p.tag}
                  </span>
                </button>
              ))}
            </div>
          </div>

          {/* Board Coordinates Toggle */}
          <div className="flex items-center justify-between bg-slate-950/80 p-3 rounded-2xl border border-slate-800">
            <div className="flex items-center gap-2">
              <Eye className="w-4 h-4 text-amber-400" />
              <span className="text-xs font-medium text-slate-200">{t.showCoordinates}</span>
            </div>
            <button
              onClick={() => onUpdateSettings({ showCoordinates: !settings.showCoordinates })}
              className={`w-11 h-6 rounded-full transition-colors relative p-0.5 ${
                settings.showCoordinates ? 'bg-amber-500' : 'bg-slate-700'
              }`}
            >
              <div
                className={`w-5 h-5 rounded-full bg-white shadow-md transform transition-transform ${
                  settings.showCoordinates ? 'translate-x-5' : 'translate-x-0'
                }`}
              />
            </button>
          </div>

          {/* Auto-flip in Local 2P */}
          <div className="flex items-center justify-between bg-slate-950/80 p-3 rounded-2xl border border-slate-800">
            <div>
              <span className="text-xs font-medium text-slate-200 block">{t.autoFlip}</span>
              <span className="text-[10px] text-slate-500">Rotate board 180° after each move</span>
            </div>
            <button
              onClick={() => onUpdateSettings({ autoFlipLocal: !settings.autoFlipLocal })}
              className={`w-11 h-6 rounded-full transition-colors relative p-0.5 ${
                settings.autoFlipLocal ? 'bg-amber-500' : 'bg-slate-700'
              }`}
            >
              <div
                className={`w-5 h-5 rounded-full bg-white shadow-md transform transition-transform ${
                  settings.autoFlipLocal ? 'translate-x-5' : 'translate-x-0'
                }`}
              />
            </button>
          </div>
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-slate-800 bg-slate-950/60 flex justify-end">
          <button
            onClick={onClose}
            className="px-5 py-2 rounded-xl bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold text-xs transition"
          >
            Save &amp; Close
          </button>
        </div>
      </div>
    </div>
  );
};
