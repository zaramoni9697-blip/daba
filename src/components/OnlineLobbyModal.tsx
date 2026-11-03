import React, { useState } from 'react';
import { TimeControl, TIME_CONTROLS, UserSettings } from '../types/chess';
import { translations } from '../services/i18n';
import { X, Users, Copy, Check, Share2, Play, ArrowRight, Loader2, Clock } from 'lucide-react';

interface OnlineLobbyModalProps {
  isOpen: boolean;
  onClose: () => void;
  settings: UserSettings;
  onCreateRoom: (color: 'w' | 'b' | 'random', timeControl: TimeControl, playerName: string) => Promise<string | null>;
  onJoinRoom: (code: string, playerName: string) => Promise<boolean>;
  activeRoomCode: string | null;
  isWaitingForGuest: boolean;
}

export const OnlineLobbyModal: React.FC<OnlineLobbyModalProps> = ({
  isOpen,
  onClose,
  settings,
  onCreateRoom,
  onJoinRoom,
  activeRoomCode,
  isWaitingForGuest,
}) => {
  const [tab, setTab] = useState<'create' | 'join'>('create');
  const [chosenColor, setChosenColor] = useState<'w' | 'b' | 'random'>('w');
  const [chosenTimeControl, setChosenTimeControl] = useState<TimeControl>(TIME_CONTROLS[0]); // Rapid 10+0 (10 Minutes) default
  const [playerName, setPlayerName] = useState(settings.playerName || 'Player');
  const [joinCode, setJoinCode] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [copiedLink, setCopiedLink] = useState(false);
  const [copiedCode, setCopiedCode] = useState(false);

  if (!isOpen) return null;
  const t = translations[settings.language];

  const handleCreate = async () => {
    setIsLoading(true);
    setErrorMsg(null);
    try {
      const code = await onCreateRoom(chosenColor, chosenTimeControl, playerName);
      if (!code) {
        setErrorMsg('Failed to create room. Please try again.');
      }
    } catch (err: unknown) {
      setErrorMsg(err instanceof Error ? err.message : 'Error creating room');
    } finally {
      setIsLoading(false);
    }
  };

  const handleJoin = async () => {
    if (!joinCode.trim()) {
      setErrorMsg('Please enter a valid room code');
      return;
    }
    setIsLoading(true);
    setErrorMsg(null);
    try {
      const success = await onJoinRoom(joinCode.trim().toUpperCase(), playerName);
      if (!success) {
        setErrorMsg('Could not join room. Check code or room may be full.');
      } else {
        onClose();
      }
    } catch (err: unknown) {
      setErrorMsg(err instanceof Error ? err.message : 'Error joining room');
    } finally {
      setIsLoading(false);
    }
  };

  const copyRoomCode = () => {
    if (!activeRoomCode) return;
    navigator.clipboard.writeText(activeRoomCode);
    setCopiedCode(true);
    setTimeout(() => setCopiedCode(false), 2000);
  };

  const copyRoomLink = () => {
    if (!activeRoomCode) return;
    const url = `${window.location.origin}${window.location.pathname}?room=${activeRoomCode}`;
    navigator.clipboard.writeText(url);
    setCopiedLink(true);
    setTimeout(() => setCopiedLink(false), 2000);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-xs p-4 animate-fadeIn">
      <div className="bg-slate-900 border border-amber-500/40 rounded-3xl max-w-md w-full shadow-2xl overflow-hidden flex flex-col max-h-[92vh]">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-950/60">
          <div className="flex items-center gap-2">
            <Users className="w-5 h-5 text-amber-400" />
            <h3 className="font-bold text-base text-slate-100">{t.playOnline}</h3>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Room Waiting Screen */}
        {isWaitingForGuest && activeRoomCode ? (
          <div className="p-6 text-center space-y-5">
            <div className="w-14 h-14 mx-auto rounded-2xl bg-amber-500/20 text-amber-400 flex items-center justify-center border border-amber-500/30">
              <Loader2 className="w-7 h-7 animate-spin" />
            </div>

            <div>
              <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider block">
                {t.roomCode}
              </span>
              <div className="text-3xl font-black font-mono tracking-widest text-amber-300 mt-1">
                {activeRoomCode}
              </div>
            </div>

            <p className="text-xs text-slate-300 max-w-xs mx-auto">
              {t.waitingForOpponent}
            </p>

            <div className="grid grid-cols-2 gap-2 max-w-xs mx-auto">
              <button
                onClick={copyRoomCode}
                className="flex items-center justify-center gap-1.5 py-2.5 px-3 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs font-bold text-slate-200 border border-slate-700 transition"
              >
                {copiedCode ? <Check className="w-4 h-4 text-emerald-400" /> : <Copy className="w-4 h-4" />}
                <span>{copiedCode ? t.copied : 'Copy Code'}</span>
              </button>
              <button
                onClick={copyRoomLink}
                className="flex items-center justify-center gap-1.5 py-2.5 px-3 rounded-xl bg-amber-500 hover:bg-amber-400 text-xs font-bold text-slate-950 transition"
              >
                {copiedLink ? <Check className="w-4 h-4 text-slate-950" /> : <Share2 className="w-4 h-4" />}
                <span>{copiedLink ? t.copied : 'Share Link'}</span>
              </button>
            </div>

            <div className="pt-4 border-t border-slate-800/80">
              <button
                onClick={onClose}
                className="text-xs text-slate-400 hover:text-white transition"
              >
                Close &amp; Keep Room Open
              </button>
            </div>
          </div>
        ) : (
          <>
            {/* Tabs */}
            <div className="flex border-b border-slate-800 bg-slate-950/40 px-6 pt-2">
              <button
                onClick={() => {
                  setTab('create');
                  setErrorMsg(null);
                }}
                className={`flex-1 py-3 text-xs font-bold border-b-2 text-center transition ${
                  tab === 'create'
                    ? 'border-amber-400 text-amber-400'
                    : 'border-transparent text-slate-400 hover:text-slate-200'
                }`}
              >
                {t.createRoom}
              </button>
              <button
                onClick={() => {
                  setTab('join');
                  setErrorMsg(null);
                }}
                className={`flex-1 py-3 text-xs font-bold border-b-2 text-center transition ${
                  tab === 'join'
                    ? 'border-amber-400 text-amber-400'
                    : 'border-transparent text-slate-400 hover:text-slate-200'
                }`}
              >
                {t.joinRoom}
              </button>
            </div>

            {/* Form Content */}
            <div className="p-6 overflow-y-auto space-y-4 text-xs scrollbar-thin">
              {errorMsg && (
                <div className="bg-red-500/15 border border-red-500/40 text-red-300 p-3 rounded-xl text-xs">
                  {errorMsg}
                </div>
              )}

              {/* Player Name */}
              <div className="space-y-1.5">
                <label className="text-slate-400 font-semibold uppercase text-[10px] tracking-wider">
                  Your Display Name
                </label>
                <input
                  type="text"
                  value={playerName}
                  onChange={(e) => setPlayerName(e.target.value)}
                  maxLength={18}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2.5 text-slate-100 font-medium focus:outline-hidden focus:border-amber-400 text-xs"
                  placeholder="Enter your name"
                />
              </div>

              {tab === 'create' ? (
                <>
                  {/* Choose Color */}
                  <div className="space-y-1.5">
                    <label className="text-slate-400 font-semibold uppercase text-[10px] tracking-wider">
                      {t.chooseSide}
                    </label>
                    <div className="grid grid-cols-3 gap-2">
                      <button
                        onClick={() => setChosenColor('w')}
                        className={`py-2 px-3 rounded-xl border text-center transition font-semibold ${
                          chosenColor === 'w'
                            ? 'bg-amber-100 text-slate-900 border-amber-300 shadow-sm'
                            : 'bg-slate-950 border-slate-800 text-slate-300 hover:border-slate-700'
                        }`}
                      >
                        ♔ {t.white}
                      </button>
                      <button
                        onClick={() => setChosenColor('b')}
                        className={`py-2 px-3 rounded-xl border text-center transition font-semibold ${
                          chosenColor === 'b'
                            ? 'bg-slate-800 text-white border-amber-400 shadow-sm'
                            : 'bg-slate-950 border-slate-800 text-slate-300 hover:border-slate-700'
                        }`}
                      >
                        ♚ {t.black}
                      </button>
                      <button
                        onClick={() => setChosenColor('random')}
                        className={`py-2 px-3 rounded-xl border text-center transition font-semibold ${
                          chosenColor === 'random'
                            ? 'bg-amber-500/20 text-amber-300 border-amber-400 shadow-sm'
                            : 'bg-slate-950 border-slate-800 text-slate-300 hover:border-slate-700'
                        }`}
                      >
                        🎲 {t.random}
                      </button>
                    </div>
                  </div>

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
                          onClick={() => setChosenTimeControl(tc)}
                          className={`p-2 rounded-xl border text-left transition ${
                            chosenTimeControl.id === tc.id
                              ? 'bg-amber-500/15 border-amber-400 text-amber-300 font-bold'
                              : 'bg-slate-950 border-slate-800 text-slate-300 hover:border-slate-700'
                          }`}
                        >
                          <span className="block truncate text-xs">{tc.name}</span>
                          <span className="text-[10px] text-slate-500">
                            {tc.initialSeconds === 0
                              ? 'No clock'
                              : `${Math.floor(tc.initialSeconds / 60)} min ${
                                  tc.incrementSeconds > 0 ? `+${tc.incrementSeconds}s` : ''
                                }`}
                          </span>
                        </button>
                      ))}
                    </div>
                  </div>

                  {/* Create Button */}
                  <button
                    onClick={handleCreate}
                    disabled={isLoading}
                    className="w-full flex items-center justify-center gap-2 py-3 px-4 rounded-xl bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 font-bold shadow-lg transition active:scale-95 disabled:opacity-50 mt-2"
                  >
                    {isLoading ? (
                      <Loader2 className="w-4 h-4 animate-spin" />
                    ) : (
                      <>
                        <Play className="w-4 h-4 fill-current" />
                        <span>{t.createRoom}</span>
                      </>
                    )}
                  </button>
                </>
              ) : (
                <>
                  {/* Join Room */}
                  <div className="space-y-1.5">
                    <label className="text-slate-400 font-semibold uppercase text-[10px] tracking-wider">
                      {t.roomCode}
                    </label>
                    <input
                      type="text"
                      value={joinCode}
                      onChange={(e) => setJoinCode(e.target.value.toUpperCase())}
                      maxLength={8}
                      placeholder="e.g. 7K4MZ9"
                      className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-center text-lg font-mono font-black tracking-widest text-amber-300 focus:outline-hidden focus:border-amber-400 uppercase placeholder:text-slate-600 placeholder:text-sm placeholder:font-sans"
                    />
                  </div>

                  <p className="text-[11px] text-slate-400 text-center">
                    Ask your friend for their 6-character room code or open their invitation link.
                  </p>

                  {/* Join Button */}
                  <button
                    onClick={handleJoin}
                    disabled={isLoading || !joinCode.trim()}
                    className="w-full flex items-center justify-center gap-2 py-3 px-4 rounded-xl bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 font-bold shadow-lg transition active:scale-95 disabled:opacity-50 mt-2"
                  >
                    {isLoading ? (
                      <Loader2 className="w-4 h-4 animate-spin" />
                    ) : (
                      <>
                        <span>{t.joinRoom}</span>
                        <ArrowRight className="w-4 h-4" />
                      </>
                    )}
                  </button>
                </>
              )}
            </div>
          </>
        )}
      </div>
    </div>
  );
};
