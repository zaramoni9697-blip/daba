import React, { useState } from 'react';
import { AIDifficulty } from '../services/chessEngine';
import { TimeControl, TIME_CONTROLS, UserSettings } from '../types/chess';
import { translations } from '../services/i18n';
import { X, Bot, Users, Play, Clock, ShieldCheck, Flame, Zap, Award } from 'lucide-react';

interface GameSetupModalProps {
  isOpen: boolean;
  onClose: () => void;
  mode: 'computer' | 'local';
  settings: UserSettings;
  onStartGame: (config: {
    color: 'w' | 'b' | 'random';
    difficulty: AIDifficulty;
    timeControl: TimeControl;
  }) => void;
}

export const GameSetupModal: React.FC<GameSetupModalProps> = ({
  isOpen,
  onClose,
  mode,
  settings,
  onStartGame,
}) => {
  const [color, setColor] = useState<'w' | 'b' | 'random'>('w');
  const [difficulty, setDifficulty] = useState<AIDifficulty>('medium');
  const [timeControl, setTimeControl] = useState<TimeControl>(TIME_CONTROLS[0]); // Rapid 10+0 (10 Minutes)

  if (!isOpen) return null;
  const t = translations[settings.language];
  const isComputer = mode === 'computer';

  const difficulties: { id: AIDifficulty; label: string; desc: string; icon: React.ReactNode }[] = [
    {
      id: 'easy',
      label: t.easy,
      desc: 'Ideal for beginners learning tactical fundamentals.',
      icon: <Zap className="w-4 h-4 text-emerald-400" />,
    },
    {
      id: 'medium',
      label: t.medium,
      desc: 'Solid club player strength with positional awareness.',
      icon: <ShieldCheck className="w-4 h-4 text-sky-400" />,
    },
    {
      id: 'hard',
      label: t.hard,
      desc: 'Depth 4 search with tactical Quiescence defense.',
      icon: <Flame className="w-4 h-4 text-amber-400" />,
    },
    {
      id: 'expert',
      label: t.expert,
      desc: 'Tournament master evaluation with deep strategic foresight.',
      icon: <Award className="w-4 h-4 text-rose-400" />,
    },
  ];

  const handleStart = () => {
    onStartGame({
      color,
      difficulty,
      timeControl,
    });
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-xs p-4 animate-fadeIn">
      <div className="bg-slate-900 border border-amber-500/40 rounded-3xl max-w-md w-full shadow-2xl overflow-hidden flex flex-col max-h-[92vh]">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-950/60">
          <div className="flex items-center gap-2">
            {isComputer ? (
              <Bot className="w-5 h-5 text-amber-400" />
            ) : (
              <Users className="w-5 h-5 text-amber-400" />
            )}
            <h3 className="font-bold text-base text-slate-100">
              {isComputer ? t.playVsComputer : t.playLocal}
            </h3>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 overflow-y-auto space-y-4 text-xs scrollbar-thin">
          {/* Choose Side (Computer mode) */}
          {isComputer && (
            <div className="space-y-1.5">
              <label className="text-slate-400 font-semibold uppercase text-[10px] tracking-wider">
                {t.chooseSide}
              </label>
              <div className="grid grid-cols-3 gap-2">
                <button
                  onClick={() => setColor('w')}
                  className={`py-2 px-3 rounded-xl border text-center transition font-semibold ${
                    color === 'w'
                      ? 'bg-amber-100 text-slate-900 border-amber-300 shadow-sm'
                      : 'bg-slate-950 border-slate-800 text-slate-300 hover:border-slate-700'
                  }`}
                >
                  ♔ {t.white}
                </button>
                <button
                  onClick={() => setColor('b')}
                  className={`py-2 px-3 rounded-xl border text-center transition font-semibold ${
                    color === 'b'
                      ? 'bg-slate-800 text-white border-amber-400 shadow-sm'
                      : 'bg-slate-950 border-slate-800 text-slate-300 hover:border-slate-700'
                  }`}
                >
                  ♚ {t.black}
                </button>
                <button
                  onClick={() => setColor('random')}
                  className={`py-2 px-3 rounded-xl border text-center transition font-semibold ${
                    color === 'random'
                      ? 'bg-amber-500/20 text-amber-300 border-amber-400 shadow-sm'
                      : 'bg-slate-950 border-slate-800 text-slate-300 hover:border-slate-700'
                  }`}
                >
                  🎲 {t.random}
                </button>
              </div>
            </div>
          )}

          {/* AI Difficulty (Computer mode) */}
          {isComputer && (
            <div className="space-y-1.5">
              <label className="text-slate-400 font-semibold uppercase text-[10px] tracking-wider">
                {t.difficulty}
              </label>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                {difficulties.map((d) => (
                  <button
                    key={d.id}
                    onClick={() => setDifficulty(d.id)}
                    className={`flex items-start gap-2.5 p-2.5 rounded-xl border text-left transition ${
                      difficulty === d.id
                        ? 'bg-amber-500/15 border-amber-400 text-amber-300'
                        : 'bg-slate-950 border-slate-800 text-slate-300 hover:border-slate-700'
                    }`}
                  >
                    <div className="shrink-0 mt-0.5">{d.icon}</div>
                    <div>
                      <span className="font-bold text-xs block">{d.label}</span>
                      <span className="text-[10px] text-slate-400 line-clamp-2">{d.desc}</span>
                    </div>
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* Time Control */}
          <div className="space-y-1.5">
            <label className="text-slate-400 font-semibold uppercase text-[10px] tracking-wider flex items-center gap-1">
              <Clock className="w-3 h-3 text-amber-400" />
              {t.timeControl}
            </label>
            <div className="grid grid-cols-2 gap-2">
              {TIME_CONTROLS.map((tc) => (
                <button
                  key={tc.id}
                  onClick={() => setTimeControl(tc)}
                  className={`p-2 rounded-xl border text-left transition ${
                    timeControl.id === tc.id
                      ? 'bg-amber-500/15 border-amber-400 text-amber-300 font-bold'
                      : 'bg-slate-950 border-slate-800 text-slate-300 hover:border-slate-700'
                  }`}
                >
                  <span className="block truncate text-xs">{tc.name}</span>
                  <span className="text-[10px] text-slate-500">
                    {tc.initialSeconds === 0
                      ? 'Unlimited'
                      : `${Math.floor(tc.initialSeconds / 60)}m ${
                          tc.incrementSeconds > 0 ? `+${tc.incrementSeconds}s` : ''
                        }`}
                  </span>
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-slate-800 bg-slate-950/60 flex items-center justify-between">
          <button
            onClick={onClose}
            className="px-4 py-2 rounded-xl text-slate-400 hover:text-white transition text-xs font-semibold"
          >
            {t.cancel}
          </button>
          <button
            onClick={handleStart}
            className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 font-bold text-xs shadow-lg transition active:scale-95"
          >
            <Play className="w-4 h-4 fill-current" />
            <span>{t.startGame}</span>
          </button>
        </div>
      </div>
    </div>
  );
};
