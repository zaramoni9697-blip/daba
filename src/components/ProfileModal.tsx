import React from 'react';
import { PlayerStats, UserSettings } from '../types/chess';
import { translations } from '../services/i18n';
import { X, Trophy, Award, User, Target, RotateCcw } from 'lucide-react';

interface ProfileModalProps {
  isOpen: boolean;
  onClose: () => void;
  stats: PlayerStats;
  settings: UserSettings;
  onResetStats: () => void;
}

export const ProfileModal: React.FC<ProfileModalProps> = ({
  isOpen,
  onClose,
  stats,
  settings,
  onResetStats,
}) => {
  if (!isOpen) return null;
  const t = translations[settings.language];

  const total = stats.wins + stats.losses + stats.draws;
  const winRate = total > 0 ? Math.round((stats.wins / total) * 100) : 0;

  // Title assignment based on wins
  let rankTitle = 'Chess Initiate';
  if (stats.wins >= 25) rankTitle = 'Grandmaster of the Realm';
  else if (stats.wins >= 15) rankTitle = 'Master Tactician';
  else if (stats.wins >= 8) rankTitle = 'Royal Knight';
  else if (stats.wins >= 3) rankTitle = 'Seasoned Challenger';

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-xs p-4 animate-fadeIn">
      <div className="bg-slate-900 border border-amber-500/40 rounded-3xl max-w-md w-full shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-950/60">
          <div className="flex items-center gap-2">
            <User className="w-5 h-5 text-amber-400" />
            <h3 className="font-bold text-base text-slate-100">{t.profile}</h3>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 overflow-y-auto space-y-5 text-xs text-slate-300 scrollbar-thin">
          {/* Avatar and Badge */}
          <div className="text-center space-y-2 pb-2">
            <div className="w-20 h-20 mx-auto rounded-3xl bg-gradient-to-tr from-amber-600 via-amber-400 to-yellow-200 p-0.5 shadow-xl">
              <div className="w-full h-full bg-slate-950 rounded-[22px] flex items-center justify-center text-4xl">
                👑
              </div>
            </div>
            <h2 className="text-lg font-bold text-white">{settings.playerName || 'Royal Player'}</h2>
            <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-amber-500/10 border border-amber-500/30 text-amber-300 font-semibold text-xs">
              <Award className="w-3.5 h-3.5" />
              <span>{rankTitle}</span>
            </div>
          </div>

          {/* Quick Stats Grid */}
          <div className="grid grid-cols-4 gap-2 text-center">
            <div className="bg-slate-950/80 p-3 rounded-2xl border border-slate-800">
              <span className="block text-slate-500 text-[10px] uppercase font-bold">{t.wins}</span>
              <span className="text-lg font-black text-emerald-400">{stats.wins}</span>
            </div>
            <div className="bg-slate-950/80 p-3 rounded-2xl border border-slate-800">
              <span className="block text-slate-500 text-[10px] uppercase font-bold">{t.losses}</span>
              <span className="text-lg font-black text-rose-400">{stats.losses}</span>
            </div>
            <div className="bg-slate-950/80 p-3 rounded-2xl border border-slate-800">
              <span className="block text-slate-500 text-[10px] uppercase font-bold">{t.draws}</span>
              <span className="text-lg font-black text-amber-400">{stats.draws}</span>
            </div>
            <div className="bg-slate-950/80 p-3 rounded-2xl border border-slate-800">
              <span className="block text-slate-500 text-[10px] uppercase font-bold">{t.winRate}</span>
              <span className="text-lg font-black text-sky-400">{winRate}%</span>
            </div>
          </div>

          {/* Detailed Mode Breakdown */}
          <div className="bg-slate-950/60 p-4 rounded-2xl border border-slate-800 space-y-3">
            <div className="flex items-center gap-2 font-bold text-amber-400 text-xs">
              <Trophy className="w-4 h-4" />
              <span>Performance by Game Mode</span>
            </div>

            <div className="space-y-2 text-[11px]">
              <div className="flex items-center justify-between py-1 border-b border-slate-800/80">
                <span className="text-slate-400">{t.playVsComputer}</span>
                <span className="font-semibold text-slate-200">
                  {stats.vsComputer.wins}W / {stats.vsComputer.losses}L / {stats.vsComputer.draws}D
                </span>
              </div>
              <div className="flex items-center justify-between py-1 border-b border-slate-800/80">
                <span className="text-slate-400">{t.playLocal}</span>
                <span className="font-semibold text-slate-200">{stats.vsLocal.games} matches</span>
              </div>
              <div className="flex items-center justify-between py-1">
                <span className="text-slate-400">{t.playOnline}</span>
                <span className="font-semibold text-slate-200">
                  {stats.vsOnline.wins}W / {stats.vsOnline.losses}L / {stats.vsOnline.draws}D
                </span>
              </div>
            </div>
          </div>

          {/* Reset Stats */}
          <div className="pt-2 text-center">
            <button
              onClick={() => {
                if (confirm('Are you sure you want to reset all game statistics?')) {
                  onResetStats();
                }
              }}
              className="inline-flex items-center gap-1.5 text-[11px] text-slate-500 hover:text-rose-400 transition"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              <span>Reset Game Statistics</span>
            </button>
          </div>
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-slate-800 bg-slate-950/60 flex justify-end">
          <button
            onClick={onClose}
            className="px-5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 font-semibold text-xs transition"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};
