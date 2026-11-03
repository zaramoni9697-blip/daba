import React, { useEffect, useState } from 'react';
import { Color } from 'chess.js';
import { sounds } from '../services/soundService';

interface ChessClockProps {
  color: Color;
  timeMs: number;
  isActive: boolean;
  playerName: string;
  isOnlineOpponent?: boolean;
  connected?: boolean;
  avatarIcon?: React.ReactNode;
  materialAdvantage?: number; // e.g. +3
}

export const formatClockTime = (ms: number): string => {
  if (ms <= 0) return '00:00';
  const totalSeconds = Math.floor(ms / 1000);
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;

  // Show tenths of a second when under 10 seconds for high-stakes Bullet/Blitz feel!
  if (totalSeconds < 10) {
    const tenths = Math.floor((ms % 1000) / 100);
    return `${seconds}.${tenths}`;
  }

  const mm = String(minutes).padStart(2, '0');
  const ss = String(seconds).padStart(2, '0');
  return `${mm}:${ss}`;
};

export const ChessClock: React.FC<ChessClockProps> = ({
  color,
  timeMs,
  isActive,
  playerName,
  isOnlineOpponent = false,
  connected = true,
  avatarIcon,
  materialAdvantage = 0,
}) => {
  const isWhite = color === 'w';
  const [displayMs, setDisplayMs] = useState(timeMs);

  useEffect(() => {
    setDisplayMs(timeMs);
  }, [timeMs]);

  const isLowTime = displayMs > 0 && displayMs <= 20000;
  const isCriticalTime = displayMs > 0 && displayMs <= 10000;

  // Play subtle warning audio on low time
  useEffect(() => {
    if (isActive && isCriticalTime) {
      sounds.playClockTick();
    }
  }, [Math.floor(displayMs / 1000), isActive, isCriticalTime]);

  return (
    <div
      className={`flex items-center justify-between px-3.5 py-2.5 rounded-xl border transition-all duration-200 select-none ${
        isActive
          ? 'bg-slate-900/90 border-amber-400/80 shadow-[0_0_15px_rgba(234,179,8,0.25)] ring-1 ring-amber-400/40'
          : 'bg-slate-950/70 border-slate-800/80 opacity-90'
      }`}
    >
      {/* Player Identity */}
      <div className="flex items-center gap-2.5 min-w-0">
        <div
          className={`w-7 h-7 rounded-lg flex items-center justify-center font-bold text-xs shadow-sm ${
            isWhite
              ? 'bg-gradient-to-br from-amber-100 to-amber-300 text-slate-900 border border-amber-200'
              : 'bg-gradient-to-br from-slate-800 to-slate-950 text-slate-100 border border-slate-700'
          }`}
        >
          {avatarIcon || (isWhite ? '♔' : '♚')}
        </div>

        <div className="flex flex-col min-w-0">
          <div className="flex items-center gap-1.5">
            <span className="font-semibold text-sm text-slate-100 truncate max-w-[110px] sm:max-w-[160px]">
              {playerName}
            </span>
            {isOnlineOpponent && (
              <span
                className={`w-2 h-2 rounded-full ${
                  connected ? 'bg-emerald-400 animate-pulse' : 'bg-red-400'
                }`}
                title={connected ? 'Connected' : 'Disconnected'}
              />
            )}
          </div>

          <div className="flex items-center gap-2 text-[11px] text-slate-400">
            <span>{isWhite ? 'White' : 'Black'}</span>
            {materialAdvantage > 0 && (
              <span className="px-1.5 py-0.2 rounded bg-amber-500/20 text-amber-300 font-bold text-[10px]">
                +{materialAdvantage}
              </span>
            )}
          </div>
        </div>
      </div>

      {/* Clock Digital Display */}
      {timeMs >= 0 ? (
        <div
          className={`tabular-nums font-mono text-lg sm:text-xl font-bold px-3 py-1 rounded-lg border flex items-center gap-1 transition-colors ${
            displayMs <= 0
              ? 'bg-red-950/80 text-red-400 border-red-700 animate-pulse'
              : isCriticalTime
              ? 'bg-red-950/60 text-red-300 border-red-500/80 animate-pulse'
              : isLowTime
              ? 'bg-amber-950/40 text-amber-300 border-amber-600/50'
              : isActive
              ? 'bg-slate-800 text-amber-400 border-amber-500/40'
              : 'bg-slate-900 text-slate-300 border-slate-800'
          }`}
        >
          {formatClockTime(displayMs)}
        </div>
      ) : (
        <div className="text-xs text-slate-500 italic">Casual</div>
      )}
    </div>
  );
};
