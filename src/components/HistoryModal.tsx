import React, { useState } from 'react';
import { GameRecord, UserSettings } from '../types/chess';
import { translations } from '../services/i18n';
import { X, History, Trophy, Clock, Check, Copy } from 'lucide-react';

interface HistoryModalProps {
  isOpen: boolean;
  onClose: () => void;
  historyList: GameRecord[];
  settings: UserSettings;
  onClearHistory: () => void;
}

export const HistoryModal: React.FC<HistoryModalProps> = ({
  isOpen,
  onClose,
  historyList,
  settings,
  onClearHistory,
}) => {
  const [copiedId, setCopiedId] = useState<string | null>(null);
  if (!isOpen) return null;
  const t = translations[settings.language];

  const handleCopyPgn = (id: string, pgn: string) => {
    navigator.clipboard.writeText(pgn);
    setCopiedId(id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-xs p-4 animate-fadeIn">
      <div className="bg-slate-900 border border-amber-500/40 rounded-3xl max-w-lg w-full shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-950/60">
          <div className="flex items-center gap-2">
            <History className="w-5 h-5 text-amber-400" />
            <h3 className="font-bold text-base text-slate-100">{t.gameHistory}</h3>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 overflow-y-auto space-y-3 text-xs text-slate-300 scrollbar-thin">
          {historyList.length === 0 ? (
            <div className="text-center py-12 space-y-2">
              <div className="w-12 h-12 mx-auto rounded-2xl bg-slate-800 flex items-center justify-center text-slate-500">
                <Trophy className="w-6 h-6" />
              </div>
              <p className="text-slate-400 font-medium">No completed games yet.</p>
              <p className="text-[11px] text-slate-500">Play matches vs computer or online to record your history.</p>
            </div>
          ) : (
            historyList.map((game) => {
              const isWin = game.result === 'win';
              const isLoss = game.result === 'loss';
              const isDraw = game.result === 'draw';

              return (
                <div
                  key={game.id}
                  className="bg-slate-950/80 border border-slate-800/80 rounded-2xl p-3.5 space-y-2 hover:border-slate-700 transition"
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <span
                        className={`px-2 py-0.5 rounded-full font-bold text-[10px] uppercase ${
                          isWin
                            ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30'
                            : isLoss
                            ? 'bg-rose-500/20 text-rose-300 border border-rose-500/30'
                            : 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
                        }`}
                      >
                        {game.result}
                      </span>
                      <span className="text-[11px] font-semibold text-slate-300 capitalize">
                        {game.mode === 'computer' ? 'vs AI Engine' : game.mode === 'online' ? 'Online Match' : 'Local 2P'}
                      </span>
                    </div>

                    <div className="flex items-center gap-1.5 text-[11px] text-slate-500">
                      <Clock className="w-3 h-3" />
                      <span>{game.timeControl}</span>
                    </div>
                  </div>

                  <div className="flex items-center justify-between text-[11px] text-slate-400">
                    <div>
                      <span className="text-slate-300 font-medium">{game.whitePlayer}</span> (White) vs{' '}
                      <span className="text-slate-300 font-medium">{game.blackPlayer}</span> (Black)
                    </div>
                    <span>{game.movesCount} moves</span>
                  </div>

                  <div className="flex items-center justify-between pt-1 border-t border-slate-800/60 text-[10px] text-slate-500">
                    <span className="italic">{game.reason}</span>
                    <button
                      onClick={() => handleCopyPgn(game.id, game.pgn)}
                      className="flex items-center gap-1 text-amber-400 hover:text-amber-300 transition"
                    >
                      {copiedId === game.id ? <Check className="w-3 h-3 text-emerald-400" /> : <Copy className="w-3 h-3" />}
                      <span>Copy PGN</span>
                    </button>
                  </div>
                </div>
              );
            })
          )}
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-slate-800 bg-slate-950/60 flex items-center justify-between">
          {historyList.length > 0 ? (
            <button
              onClick={() => {
                if (confirm('Clear all game history records?')) {
                  onClearHistory();
                }
              }}
              className="text-[11px] text-slate-500 hover:text-rose-400 transition"
            >
              Clear History
            </button>
          ) : (
            <div />
          )}
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
