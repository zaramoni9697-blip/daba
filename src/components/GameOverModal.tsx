import React, { useEffect } from 'react';
import confetti from 'canvas-confetti';
import { UserSettings } from '../types/chess';
import { translations } from '../services/i18n';
import { Trophy, RotateCcw, Home, Copy, Check, Award } from 'lucide-react';
import { sounds } from '../services/soundService';

interface GameOverModalProps {
  isOpen: boolean;
  onClose: () => void;
  result: 'win' | 'loss' | 'draw';
  winnerColor: 'w' | 'b' | 'draw' | null;
  winReason: string;
  pgn: string;
  settings: UserSettings;
  onRematch: () => void;
  onBackToMenu: () => void;
}

export const GameOverModal: React.FC<GameOverModalProps> = ({
  isOpen,
  onClose,
  result,
  winnerColor,
  winReason,
  pgn,
  settings,
  onRematch,
  onBackToMenu,
}) => {
  const [copied, setCopied] = React.useState(false);
  const t = translations[settings.language];

  useEffect(() => {
    if (isOpen) {
      if (result === 'win') {
        sounds.playVictory();
        confetti({
          particleCount: 80,
          spread: 70,
          origin: { y: 0.6 },
          colors: ['#eab308', '#fef08a', '#ca8a04', '#38bdf8', '#ffffff'],
        });
      } else if (result === 'loss') {
        sounds.playDefeat();
      } else {
        sounds.playDefeat();
      }
    }
  }, [isOpen, result]);

  if (!isOpen) return null;

  const isWin = result === 'win';
  const isDraw = result === 'draw';

  const copyPgn = () => {
    navigator.clipboard.writeText(pgn);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-xs p-4 animate-fadeIn">
      <div className="bg-slate-900 border-2 border-amber-500/50 rounded-3xl max-w-sm w-full p-6 text-center shadow-2xl relative overflow-hidden">
        {/* Glow accent */}
        <div className="absolute -top-16 left-1/2 -translate-x-1/2 w-48 h-48 bg-amber-500/15 rounded-full blur-2xl pointer-events-none" />

        {/* Icon Emblem */}
        <div
          className={`w-16 h-16 mx-auto mb-4 rounded-2xl flex items-center justify-center shadow-lg border ${
            isWin
              ? 'bg-amber-500/20 text-amber-300 border-amber-500/40'
              : isDraw
              ? 'bg-slate-800 text-slate-300 border-slate-700'
              : 'bg-rose-500/20 text-rose-300 border-rose-500/40'
          }`}
        >
          {isWin ? <Trophy className="w-8 h-8" /> : isDraw ? <Award className="w-8 h-8" /> : <RotateCcw className="w-8 h-8" />}
        </div>

        {/* Title */}
        <h2 className="text-2xl font-black text-white tracking-wide uppercase">
          {isWin ? 'Victory!' : isDraw ? t.draw : 'Defeat'}
        </h2>

        {/* Subtitle / Reason */}
        <p className="text-xs text-amber-300/90 font-medium mt-1 mb-4">{winReason}</p>

        {/* Copy PGN Button */}
        <div className="mb-5">
          <button
            onClick={copyPgn}
            className="inline-flex items-center gap-1.5 text-xs text-slate-400 hover:text-white px-3 py-1.5 rounded-xl bg-slate-950/80 border border-slate-800 hover:border-slate-700 transition"
          >
            {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
            <span>{copied ? t.copied : t.exportPgn}</span>
          </button>
        </div>

        {/* Action Buttons */}
        <div className="space-y-2">
          <button
            onClick={onRematch}
            className="w-full flex items-center justify-center gap-2 py-3 px-4 rounded-xl bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 font-bold text-xs shadow-lg transition active:scale-95"
          >
            <RotateCcw className="w-4 h-4" />
            <span>{t.rematch}</span>
          </button>

          <button
            onClick={onBackToMenu}
            className="w-full flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 font-semibold text-xs border border-slate-700 transition"
          >
            <Home className="w-4 h-4" />
            <span>{t.backToMenu}</span>
          </button>
        </div>
      </div>
    </div>
  );
};
