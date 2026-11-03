import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Chess, Square, PieceSymbol, Color, Move } from 'chess.js';
import {
  GameMode,
  UserSettings,
  PlayerStats,
  GameRecord,
  TimeControl,
  TIME_CONTROLS,
} from './types/chess';
import { AIDifficulty, findBestMove } from './services/chessEngine';
import { sounds } from './services/soundService';
import { translations } from './services/i18n';
import { MainMenu } from './components/MainMenu';
import { ChessBoard } from './components/ChessBoard';
import { ChessClock } from './components/ChessClock';
import { GameDetails } from './components/GameDetails';
import { SettingsModal } from './components/SettingsModal';
import { ProfileModal } from './components/ProfileModal';
import { HistoryModal } from './components/HistoryModal';
import { OnlineLobbyModal } from './components/OnlineLobbyModal';
import { GameSetupModal } from './components/GameSetupModal';
import { GameOverModal } from './components/GameOverModal';
import { GitHubDeployModal } from './components/GitHubDeployModal';
import { OfflineIndicator } from './components/PWAInstallButton';
import {
  ArrowLeft,
  RotateCcw,
  Flag,
  Handshake,
  Volume2,
  VolumeX,
  Repeat,
  Loader2,
  Share2,
} from 'lucide-react';

export default function App() {
  // Navigation State
  const [currentScreen, setCurrentScreen] = useState<'menu' | 'game'>('menu');
  const [gameMode, setGameMode] = useState<'computer' | 'local' | 'online'>('computer');

  // Modals
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const [isHistoryOpen, setIsHistoryOpen] = useState(false);
  const [isOnlineLobbyOpen, setIsOnlineLobbyOpen] = useState(false);
  const [isSetupOpen, setIsSetupOpen] = useState(false);
  const [isDeployOpen, setIsDeployOpen] = useState(false);
  const [isGameOverOpen, setIsGameOverOpen] = useState(false);

  // Settings
  const [settings, setSettings] = useState<UserSettings>(() => {
    const saved = localStorage.getItem('royal_chess_settings');
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch {}
    }
    return {
      boardTheme: 'walnut',
      pieceTheme: 'royal',
      soundEnabled: true,
      showCoordinates: true,
      autoFlipLocal: false,
      language: 'bn',
      playerName: 'Player',
    };
  });

  // Player Stats
  const [stats, setStats] = useState<PlayerStats>(() => {
    const saved = localStorage.getItem('royal_chess_stats');
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch {}
    }
    return {
      gamesPlayed: 0,
      wins: 0,
      losses: 0,
      draws: 0,
      vsComputer: { wins: 0, losses: 0, draws: 0 },
      vsLocal: { games: 0 },
      vsOnline: { wins: 0, losses: 0, draws: 0 },
    };
  });

  // History List
  const [historyList, setHistoryList] = useState<GameRecord[]>(() => {
    const saved = localStorage.getItem('royal_chess_history');
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch {}
    }
    return [];
  });

  // Core Game State
  const [chess, setChess] = useState<Chess>(() => new Chess());
  const [moveHistory, setMoveHistory] = useState<string[]>([]);
  const [lastMove, setLastMove] = useState<{ from: Square; to: Square } | null>(null);
  const [boardOrientation, setBoardOrientation] = useState<Color>('w');
  const [isAiThinking, setIsAiThinking] = useState(false);

  // Game Setup Configurations
  const [aiDifficulty, setAiDifficulty] = useState<AIDifficulty>('medium');
  const [playerColor, setPlayerColor] = useState<Color>('w'); // in vs computer or online
  const [currentTimeControl, setCurrentTimeControl] = useState<TimeControl>(TIME_CONTROLS[0]); // Rapid 10+0 (10 Minutes)

  // Real-time Game Timers (ms) - Default to 10 minutes (600,000ms)
  const [whiteTimeMs, setWhiteTimeMs] = useState<number>(600000);
  const [blackTimeMs, setBlackTimeMs] = useState<number>(600000);
  const [isClockRunning, setIsClockRunning] = useState<boolean>(false);
  const lastTickTimeRef = useRef<number | null>(null);

  // Game Outcome
  const [gameResult, setGameResult] = useState<'win' | 'loss' | 'draw' | null>(null);
  const [winnerColor, setWinnerColor] = useState<'w' | 'b' | 'draw' | null>(null);
  const [winReason, setWinReason] = useState<string>('');

  // Online Multiplayer State
  const [onlineRoomCode, setOnlineRoomCode] = useState<string | null>(null);
  const [onlinePlayerToken, setOnlinePlayerToken] = useState<string | null>(null);
  const [onlineOpponentName, setOnlineOpponentName] = useState<string>('Opponent');
  const [onlineOpponentConnected, setOnlineOpponentConnected] = useState<boolean>(false);
  const [onlineDrawOffered, setOnlineDrawOffered] = useState<boolean>(false);
  const [onlineRematchOffered, setOnlineRematchOffered] = useState<boolean>(false);
  const eventSourceRef = useRef<EventSource | null>(null);

  const t = translations[settings.language];

  // Save Settings
  const handleUpdateSettings = (newSettings: Partial<UserSettings>) => {
    setSettings((prev) => {
      const updated = { ...prev, ...newSettings };
      localStorage.setItem('royal_chess_settings', JSON.stringify(updated));
      return updated;
    });
  };

  // Reset Stats
  const handleResetStats = () => {
    const emptyStats: PlayerStats = {
      gamesPlayed: 0,
      wins: 0,
      losses: 0,
      draws: 0,
      vsComputer: { wins: 0, losses: 0, draws: 0 },
      vsLocal: { games: 0 },
      vsOnline: { wins: 0, losses: 0, draws: 0 },
    };
    setStats(emptyStats);
    localStorage.setItem('royal_chess_stats', JSON.stringify(emptyStats));
  };

  // Record Game Outcome
  const recordGameOutcome = useCallback((
    result: 'win' | 'loss' | 'draw',
    reason: string,
    winner: 'w' | 'b' | 'draw' | null
  ) => {
    setGameResult(result);
    setWinnerColor(winner);
    setWinReason(reason);
    setIsClockRunning(false);
    setIsGameOverOpen(true);

    // Save Game Record
    const record: GameRecord = {
      id: 'game_' + Date.now(),
      date: new Date().toLocaleDateString(),
      mode: gameMode,
      result,
      reason,
      whitePlayer: gameMode === 'computer' ? (playerColor === 'w' ? settings.playerName : `AI (${aiDifficulty})`) : gameMode === 'online' ? (playerColor === 'w' ? settings.playerName : onlineOpponentName) : 'Player 1',
      blackPlayer: gameMode === 'computer' ? (playerColor === 'b' ? settings.playerName : `AI (${aiDifficulty})`) : gameMode === 'online' ? (playerColor === 'b' ? settings.playerName : onlineOpponentName) : 'Player 2',
      timeControl: currentTimeControl.name,
      pgn: chess.pgn() || moveHistory.join(' '),
      movesCount: moveHistory.length,
    };

    setHistoryList((prev) => {
      const updated = [record, ...prev].slice(0, 50);
      localStorage.setItem('royal_chess_history', JSON.stringify(updated));
      return updated;
    });

    // Update Player Stats
    setStats((prev) => {
      const updated = { ...prev };
      updated.gamesPlayed++;
      if (result === 'win') updated.wins++;
      else if (result === 'loss') updated.losses++;
      else updated.draws++;

      if (gameMode === 'computer') {
        if (result === 'win') updated.vsComputer.wins++;
        else if (result === 'loss') updated.vsComputer.losses++;
        else updated.vsComputer.draws++;
      } else if (gameMode === 'local') {
        updated.vsLocal.games++;
      } else if (gameMode === 'online') {
        if (result === 'win') updated.vsOnline.wins++;
        else if (result === 'loss') updated.vsOnline.losses++;
        else updated.vsOnline.draws++;
      }

      localStorage.setItem('royal_chess_stats', JSON.stringify(updated));
      return updated;
    });
  }, [chess, gameMode, playerColor, settings.playerName, aiDifficulty, onlineOpponentName, currentTimeControl.name, moveHistory]);

  // Check Game State for Checkmate, Stalemate, Draws
  const checkGameOverStatus = useCallback((activeChess: Chess) => {
    if (activeChess.isCheckmate()) {
      const losingColor = activeChess.turn();
      const winningColor = losingColor === 'w' ? 'b' : 'w';
      const isUserWin = gameMode === 'computer' ? winningColor === playerColor : gameMode === 'online' ? winningColor === playerColor : true;
      recordGameOutcome(
        isUserWin ? 'win' : 'loss',
        `Checkmate! ${winningColor === 'w' ? 'White' : 'Black'} wins`,
        winningColor
      );
      return true;
    }

    if (activeChess.isStalemate()) {
      recordGameOutcome('draw', 'Stalemate - Game drawn', 'draw');
      return true;
    }

    if (activeChess.isThreefoldRepetition()) {
      recordGameOutcome('draw', 'Threefold repetition - Game drawn', 'draw');
      return true;
    }

    if (activeChess.isInsufficientMaterial()) {
      recordGameOutcome('draw', 'Insufficient material - Game drawn', 'draw');
      return true;
    }

    if (activeChess.isDraw()) {
      recordGameOutcome('draw', 'Draw by 50-move rule', 'draw');
      return true;
    }

    return false;
  }, [gameMode, playerColor, recordGameOutcome]);

  // Handle Timeout (Flag Fall)
  const handleTimeout = useCallback((timedOutColor: Color) => {
    setIsClockRunning(false);
    const opponentColor = timedOutColor === 'w' ? 'b' : 'w';

    // FIDE Insufficient Material Check on Timeout
    // If opponent does not have sufficient material to checkmate, it is a DRAW!
    if (chess.isInsufficientMaterial()) {
      recordGameOutcome('draw', `${timedOutColor === 'w' ? 'White' : 'Black'} timed out, but opponent has insufficient material (Draw)`, 'draw');
      return;
    }

    const isUserWin = gameMode === 'computer' ? opponentColor === playerColor : gameMode === 'online' ? opponentColor === playerColor : false;
    recordGameOutcome(
      isUserWin ? 'win' : 'loss',
      `${timedOutColor === 'w' ? 'White' : 'Black'} ran out of time! ${opponentColor === 'w' ? 'White' : 'Black'} wins`,
      opponentColor
    );
  }, [chess, gameMode, playerColor, recordGameOutcome]);

  // Real-Time Clock Ticker (High-Precision RequestAnimationFrame / Delta Interval)
  useEffect(() => {
    if (!isClockRunning || currentTimeControl.initialSeconds === 0 || isGameOverOpen) {
      lastTickTimeRef.current = null;
      return;
    }

    lastTickTimeRef.current = performance.now();

    const interval = setInterval(() => {
      if (!lastTickTimeRef.current) {
        lastTickTimeRef.current = performance.now();
        return;
      }

      const now = performance.now();
      const delta = now - lastTickTimeRef.current;
      lastTickTimeRef.current = now;

      const turn = chess.turn();

      if (turn === 'w') {
        setWhiteTimeMs((prev) => {
          const next = Math.max(0, prev - delta);
          if (next <= 0) {
            clearInterval(interval);
            handleTimeout('w');
          }
          return next;
        });
      } else {
        setBlackTimeMs((prev) => {
          const next = Math.max(0, prev - delta);
          if (next <= 0) {
            clearInterval(interval);
            handleTimeout('b');
          }
          return next;
        });
      }
    }, 100);

    return () => clearInterval(interval);
  }, [isClockRunning, currentTimeControl.initialSeconds, chess, isGameOverOpen, handleTimeout]);

  // Execute Move
  const executeMove = (from: Square, to: Square, promotion: PieceSymbol = 'q'): boolean => {
    if (isAiThinking || isGameOverOpen) return false;

    // In Online mode, verify turn
    if (gameMode === 'online') {
      if (chess.turn() !== playerColor) return false;
    }

    // In Computer mode, verify it's user's turn
    if (gameMode === 'computer') {
      if (chess.turn() !== playerColor) return false;
    }

    try {
      const move = chess.move({
        from,
        to,
        promotion,
      });

      if (!move) return false;

      // Sound feedback
      if (chess.isCheck()) {
        sounds.playCheck();
      } else if (move.captured) {
        sounds.playCapture();
      } else if (move.san.includes('O-O')) {
        sounds.playCastle();
      } else {
        sounds.playMove();
      }

      // Add time increment to current mover
      if (currentTimeControl.incrementSeconds > 0) {
        if (move.color === 'w') {
          setWhiteTimeMs((prev) => prev + currentTimeControl.incrementSeconds * 1000);
        } else {
          setBlackTimeMs((prev) => prev + currentTimeControl.incrementSeconds * 1000);
        }
      }

      // Start clock if not yet running
      if (!isClockRunning && currentTimeControl.initialSeconds > 0) {
        setIsClockRunning(true);
      }

      setLastMove({ from, to });
      setMoveHistory((prev) => [...prev, move.san]);

      // Online Multiplayer Sync
      if (gameMode === 'online' && onlineRoomCode && onlinePlayerToken) {
        fetch(`/api/rooms/${onlineRoomCode}/move`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            token: onlinePlayerToken,
            from,
            to,
            promotion,
          }),
        }).catch(console.error);
      }

      // Check game over
      const isOver = checkGameOverStatus(chess);
      if (isOver) {
        setIsClockRunning(false);
        return true;
      }

      // Auto-flip for Local 2P
      if (gameMode === 'local' && settings.autoFlipLocal) {
        setBoardOrientation(chess.turn());
      }

      // Trigger AI Move if vs Computer
      if (gameMode === 'computer' && chess.turn() !== playerColor) {
        triggerAiMove();
      }

      return true;
    } catch (e) {
      return false;
    }
  };

  // AI Move Execution
  const triggerAiMove = async () => {
    setIsAiThinking(true);
    // Snappy, deliberate thinking time (120ms - 220ms)
    const thinkingDelay = aiDifficulty === 'easy' ? 100 : aiDifficulty === 'medium' ? 180 : 220;

    setTimeout(async () => {
      try {
        const result = await findBestMove(chess, aiDifficulty);
        if (result && result.move) {
          const move = chess.move(result.move);
          if (move) {
            if (chess.isCheck()) {
              sounds.playCheck();
            } else if (move.captured) {
              sounds.playCapture();
            } else if (move.san.includes('O-O')) {
              sounds.playCastle();
            } else {
              sounds.playMove();
            }

            if (currentTimeControl.incrementSeconds > 0) {
              if (move.color === 'w') {
                setWhiteTimeMs((prev) => prev + currentTimeControl.incrementSeconds * 1000);
              } else {
                setBlackTimeMs((prev) => prev + currentTimeControl.incrementSeconds * 1000);
              }
            }

            setLastMove({ from: move.from as Square, to: move.to as Square });
            setMoveHistory((prev) => [...prev, move.san]);
            checkGameOverStatus(chess);
          }
        }
      } catch (err) {
        console.error('AI Move Error:', err);
      } finally {
        setIsAiThinking(false);
      }
    }, thinkingDelay);
  };

  // Start New Game Session
  const startNewGame = (config: {
    color: 'w' | 'b' | 'random';
    difficulty: AIDifficulty;
    timeControl: TimeControl;
  }) => {
    const newChess = new Chess();
    setChess(newChess);
    setMoveHistory([]);
    setLastMove(null);
    setIsGameOverOpen(false);
    setGameResult(null);
    setWinnerColor(null);
    setWinReason('');
    setAiDifficulty(config.difficulty);
    setCurrentTimeControl(config.timeControl);

    // Initial Clocks
    const initMs = config.timeControl.initialSeconds * 1000;
    setWhiteTimeMs(initMs);
    setBlackTimeMs(initMs);
    setIsClockRunning(false);

    // Color Assignment
    let assignedColor: Color = 'w';
    if (config.color === 'random') {
      assignedColor = Math.random() < 0.5 ? 'w' : 'b';
    } else {
      assignedColor = config.color;
    }

    setPlayerColor(assignedColor);
    setBoardOrientation(assignedColor);
    setCurrentScreen('game');

    // If computer plays white, computer takes the opening move
    if (gameMode === 'computer' && assignedColor === 'b') {
      setTimeout(() => {
        setIsClockRunning(true);
        triggerAiMove();
      }, 500);
    }
  };

  // Online Multiplayer Room Management
  const handleCreateOnlineRoom = async (
    color: 'w' | 'b' | 'random',
    timeControl: TimeControl,
    playerName: string
  ): Promise<string | null> => {
    try {
      const res = await fetch('/api/rooms/create', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          hostColor: color,
          playerName,
          timeControl,
        }),
      });
      const data = await res.json();
      if (data.success) {
        setOnlineRoomCode(data.code);
        setOnlinePlayerToken(data.token);
        setPlayerColor(data.color);
        setBoardOrientation(data.color);
        setCurrentTimeControl(timeControl);
        setGameMode('online');
        setupOnlineStream(data.code, data.token);
        return data.code;
      }
      return null;
    } catch {
      return null;
    }
  };

  const handleJoinOnlineRoom = async (code: string, playerName: string): Promise<boolean> => {
    try {
      const res = await fetch('/api/rooms/join', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ code, playerName }),
      });
      const data = await res.json();
      if (data.success) {
        setOnlineRoomCode(data.code);
        setOnlinePlayerToken(data.token);
        setPlayerColor(data.color);
        setBoardOrientation(data.color);
        setGameMode('online');
        setCurrentScreen('game');
        setupOnlineStream(data.code, data.token);
        return true;
      }
      return false;
    } catch {
      return false;
    }
  };

  // SSE Real-time Online Sync
  const setupOnlineStream = (code: string, token: string) => {
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
    }

    const sse = new EventSource(`/api/rooms/${code}/stream?token=${token}`);
    eventSourceRef.current = sse;

    sse.onmessage = (event) => {
      try {
        const state = JSON.parse(event.data);
        if (!state) return;

        // Sync chess board
        const newChess = new Chess(state.fen);
        setChess(newChess);
        setMoveHistory(state.history || []);

        // Sync clocks
        setWhiteTimeMs(state.whiteTimeMs);
        setBlackTimeMs(state.blackTimeMs);
        setIsClockRunning(state.status === 'active');

        // Opponent info
        const isHost = state.yourColor === state.host.color;
        const opponent = isHost ? state.guest : state.host;
        if (opponent) {
          setOnlineOpponentName(opponent.name);
          setOnlineOpponentConnected(opponent.connected);
        }

        // Draw offer
        if (state.drawOfferBy && state.drawOfferBy !== state.yourColor) {
          setOnlineDrawOffered(true);
        } else {
          setOnlineDrawOffered(false);
        }

        // Game over status from server
        if (state.status === 'checkmate' || state.status === 'draw' || state.status === 'resigned' || state.status === 'timeout') {
          const isUserWinner = state.winner === state.yourColor;
          recordGameOutcome(
            isUserWinner ? 'win' : state.winner === 'draw' ? 'draw' : 'loss',
            state.winReason || 'Game Ended',
            state.winner
          );
        }

        // If guest just joined, transition to game screen
        if (state.status === 'active') {
          setCurrentScreen('game');
          setIsOnlineLobbyOpen(false);
        }
      } catch (err) {
        console.error('SSE Error:', err);
      }
    };

    sse.onerror = () => {
      setOnlineOpponentConnected(false);
    };
  };

  // Cleanup SSE on unmount
  useEffect(() => {
    return () => {
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
      }
    };
  }, []);

  // Check URL query params for auto-join room (?room=CODE)
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const roomParam = params.get('room');
    if (roomParam) {
      setIsOnlineLobbyOpen(true);
    }
  }, []);

  // Actions
  const handleResign = () => {
    if (confirm(t.resignConfirm)) {
      const winner = playerColor === 'w' ? 'b' : 'w';
      recordGameOutcome('loss', `${playerColor === 'w' ? 'White' : 'Black'} resigned`, winner);
      if (gameMode === 'online' && onlineRoomCode && onlinePlayerToken) {
        fetch(`/api/rooms/${onlineRoomCode}/action`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ token: onlinePlayerToken, action: 'resign' }),
        }).catch(console.error);
      }
    }
  };

  const handleOfferDraw = () => {
    if (gameMode === 'computer') {
      // AI accepts draw if position is balanced (within 100 centipawns)
      const evalScore = chess.isDraw() ? 0 : 50;
      if (Math.abs(evalScore) < 150) {
        recordGameOutcome('draw', 'Draw agreed with AI', 'draw');
      } else {
        alert('AI declined the draw offer.');
      }
    } else if (gameMode === 'online' && onlineRoomCode && onlinePlayerToken) {
      fetch(`/api/rooms/${onlineRoomCode}/action`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token: onlinePlayerToken, action: 'draw_offer' }),
      }).catch(console.error);
    } else {
      if (confirm('Opponent accepts draw offer?')) {
        recordGameOutcome('draw', 'Draw agreed by mutual agreement', 'draw');
      }
    }
  };

  const handleUndoMove = () => {
    if (gameMode === 'online') {
      alert('Undo is not permitted in competitive online matches.');
      return;
    }
    if (isAiThinking || moveHistory.length === 0) return;

    if (gameMode === 'computer') {
      // Undo both AI move and Player move
      chess.undo();
      chess.undo();
      setMoveHistory((prev) => prev.slice(0, -2));
    } else {
      chess.undo();
      setMoveHistory((prev) => prev.slice(0, -1));
    }
    setLastMove(null);
    sounds.playMove();
  };

  const handleRematch = () => {
    setIsGameOverOpen(false);
    startNewGame({
      color: playerColor === 'w' ? 'b' : 'w', // swap colors for rematch!
      difficulty: aiDifficulty,
      timeControl: currentTimeControl,
    });
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans select-none overflow-x-hidden">
      {/* Offline Mode Indicator */}
      <OfflineIndicator />

      {/* Main Menu Screen */}
      {currentScreen === 'menu' ? (
        <MainMenu
          settings={settings}
          onSelectMode={(mode) => {
            setGameMode(mode);
            if (mode === 'online') {
              setIsOnlineLobbyOpen(true);
            } else {
              setIsSetupOpen(true);
            }
          }}
          onOpenSettings={() => setIsSettingsOpen(true)}
          onOpenProfile={() => setIsProfileOpen(true)}
          onOpenHistory={() => setIsHistoryOpen(true)}
          onOpenDeploy={() => setIsDeployOpen(true)}
        />
      ) : (
        /* Active Game Screen */
        <div className="flex-1 flex flex-col items-center justify-between p-2 sm:p-4 max-w-lg mx-auto w-full">
          {/* Top Bar Header */}
          <div className="w-full flex items-center justify-between py-1.5 px-2">
            <button
              onClick={() => {
                if (confirm('Return to main menu? Current match will be forfeited.')) {
                  setCurrentScreen('menu');
                  setIsClockRunning(false);
                }
              }}
              className="flex items-center gap-1.5 px-2.5 py-1.5 rounded-xl bg-slate-900/80 hover:bg-slate-800 text-slate-300 text-xs font-semibold border border-slate-800 transition"
            >
              <ArrowLeft className="w-4 h-4" />
              <span>{t.backToMenu}</span>
            </button>

            <div className="flex items-center gap-1.5 sm:gap-2">
              <span className="text-xs font-black text-amber-400 tracking-wider uppercase">
                {gameMode === 'computer'
                  ? `AI · ${aiDifficulty}`
                  : gameMode === 'local'
                  ? 'Local 2P'
                  : `Online · ${onlineRoomCode}`}
              </span>

              <span className="text-[10px] font-medium px-2 py-0.5 rounded-full bg-slate-900 border border-amber-500/30 text-amber-300 flex items-center gap-1">
                <span>⏱️</span>
                <span>{currentTimeControl.initialSeconds === 0 ? 'আনলিমিটেড' : `${Math.floor(currentTimeControl.initialSeconds / 60)} মি.`}</span>
              </span>

              {isAiThinking && (
                <div className="flex items-center gap-1 text-[11px] text-amber-300 bg-amber-500/15 px-2 py-0.5 rounded-full border border-amber-500/30 animate-pulse">
                  <Loader2 className="w-3 h-3 animate-spin" />
                  <span>Thinking...</span>
                </div>
              )}
            </div>

            <div className="flex items-center gap-1.5">
              <button
                onClick={() => {
                  const muted = sounds.toggleMute();
                  handleUpdateSettings({ soundEnabled: !muted });
                }}
                className="p-1.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-300 border border-slate-800 transition"
                title={settings.soundEnabled ? 'Mute' : 'Unmute'}
              >
                {settings.soundEnabled ? (
                  <Volume2 className="w-4 h-4 text-amber-400" />
                ) : (
                  <VolumeX className="w-4 h-4 text-slate-500" />
                )}
              </button>

              <button
                onClick={() => setBoardOrientation((prev) => (prev === 'w' ? 'b' : 'w'))}
                className="p-1.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-300 border border-slate-800 transition"
                title="Flip Board"
              >
                <Repeat className="w-4 h-4" />
              </button>
            </div>
          </div>

          {/* Opponent Clock (Top Player) */}
          <div className="w-full my-1">
            <ChessClock
              color={boardOrientation === 'w' ? 'b' : 'w'}
              timeMs={boardOrientation === 'w' ? blackTimeMs : whiteTimeMs}
              isActive={isClockRunning && chess.turn() === (boardOrientation === 'w' ? 'b' : 'w')}
              playerName={
                gameMode === 'computer'
                  ? aiDifficulty === 'easy'
                    ? '🤖 শিক্ষানবিস এআই (Novice ~800)'
                    : aiDifficulty === 'medium'
                    ? '🛡️ রয়েল নাইট এআই (Knight ~1400)'
                    : aiDifficulty === 'hard'
                    ? '⚔️ মাস্টার এআই (Master ~1900)'
                    : '👑 গ্র্যান্ডমাস্টার এআই (DeepRoyal ~2400)'
                  : gameMode === 'online'
                  ? `⚔️ ${onlineOpponentName}`
                  : boardOrientation === 'w'
                  ? '♚ কালো সুলতান (Black Monarch)'
                  : '♔ সাদা রাজা (White Sovereign)'
              }
              isOnlineOpponent={gameMode === 'online'}
              connected={onlineOpponentConnected}
            />
          </div>

          {/* Interactive Chessboard */}
          <div className="w-full flex-1 flex items-center justify-center my-1">
            <ChessBoard
              chess={chess}
              boardOrientation={boardOrientation}
              boardTheme={settings.boardTheme}
              pieceTheme={settings.pieceTheme}
              showCoordinates={settings.showCoordinates}
              interactive={!isAiThinking && !isGameOverOpen}
              lastMove={lastMove}
              onMakeMove={executeMove}
            />
          </div>

          {/* User Clock (Bottom Player) */}
          <div className="w-full my-1">
            <ChessClock
              color={boardOrientation === 'w' ? 'w' : 'b'}
              timeMs={boardOrientation === 'w' ? whiteTimeMs : blackTimeMs}
              isActive={isClockRunning && chess.turn() === (boardOrientation === 'w' ? 'w' : 'b')}
              playerName={
                gameMode === 'local'
                  ? boardOrientation === 'w'
                    ? '♔ সাদা রাজা (White Sovereign)'
                    : '♚ কালো সুলতান (Black Monarch)'
                  : `👑 ${settings.playerName || 'রাজকীয় খেলোয়াড়'}`
              }
            />
          </div>

          {/* In-Game Action Bar */}
          <div className="w-full grid grid-cols-4 gap-2 my-2">
            <button
              onClick={handleUndoMove}
              disabled={gameMode === 'online' || moveHistory.length === 0 || isAiThinking}
              className="flex items-center justify-center gap-1.5 py-2 px-2 rounded-xl bg-slate-900/80 hover:bg-slate-800 text-slate-300 text-xs font-semibold border border-slate-800 transition disabled:opacity-40"
              title="Undo Move"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              <span>{t.undo}</span>
            </button>

            <button
              onClick={handleOfferDraw}
              disabled={isGameOverOpen}
              className="flex items-center justify-center gap-1.5 py-2 px-2 rounded-xl bg-slate-900/80 hover:bg-slate-800 text-slate-300 text-xs font-semibold border border-slate-800 transition"
              title="Offer Draw"
            >
              <Handshake className="w-3.5 h-3.5" />
              <span>{t.offerDraw}</span>
            </button>

            <button
              onClick={handleResign}
              disabled={isGameOverOpen}
              className="flex items-center justify-center gap-1.5 py-2 px-2 rounded-xl bg-slate-900/80 hover:bg-rose-950/40 text-rose-300 text-xs font-semibold border border-slate-800 hover:border-rose-800 transition"
              title="Resign Game"
            >
              <Flag className="w-3.5 h-3.5" />
              <span>{t.resign}</span>
            </button>

            <button
              onClick={() => {
                if (confirm('Start a new match? Current board will reset.')) {
                  startNewGame({
                    color: playerColor,
                    difficulty: aiDifficulty,
                    timeControl: currentTimeControl,
                  });
                }
              }}
              className="flex items-center justify-center gap-1.5 py-2 px-2 rounded-xl bg-amber-500/15 hover:bg-amber-500/25 text-amber-300 text-xs font-semibold border border-amber-500/30 transition"
              title="Restart"
            >
              <span>{t.restart}</span>
            </button>
          </div>

          {/* Captured Trays & Move Log */}
          <div className="w-full">
            <GameDetails
              chess={chess}
              pieceTheme={settings.pieceTheme}
              history={moveHistory}
              whitePlayer={boardOrientation === 'w' ? 'You' : 'Opponent'}
              blackPlayer={boardOrientation === 'b' ? 'You' : 'Opponent'}
            />
          </div>
        </div>
      )}

      {/* Online Draw Offer Notification Toast */}
      {onlineDrawOffered && (
        <div className="fixed top-4 left-1/2 -translate-x-1/2 z-50 bg-slate-900 border-2 border-amber-400 p-4 rounded-2xl shadow-2xl flex items-center gap-3 animate-fadeIn">
          <span className="text-xs text-white font-medium">{t.drawOffered}</span>
          <button
            onClick={() => {
              if (onlineRoomCode && onlinePlayerToken) {
                fetch(`/api/rooms/${onlineRoomCode}/action`, {
                  method: 'POST',
                  headers: { 'Content-Type': 'application/json' },
                  body: JSON.stringify({ token: onlinePlayerToken, action: 'draw_accept' }),
                }).catch(console.error);
                setOnlineDrawOffered(false);
              }
            }}
            className="px-3 py-1 bg-emerald-500 text-slate-950 font-bold text-xs rounded-lg"
          >
            {t.acceptDraw}
          </button>
          <button
            onClick={() => {
              if (onlineRoomCode && onlinePlayerToken) {
                fetch(`/api/rooms/${onlineRoomCode}/action`, {
                  method: 'POST',
                  headers: { 'Content-Type': 'application/json' },
                  body: JSON.stringify({ token: onlinePlayerToken, action: 'draw_decline' }),
                }).catch(console.error);
                setOnlineDrawOffered(false);
              }
            }}
            className="px-3 py-1 bg-slate-800 text-slate-300 text-xs rounded-lg"
          >
            {t.declineDraw}
          </button>
        </div>
      )}

      {/* Modals */}
      <SettingsModal
        isOpen={isSettingsOpen}
        onClose={() => setIsSettingsOpen(false)}
        settings={settings}
        onUpdateSettings={handleUpdateSettings}
      />

      <ProfileModal
        isOpen={isProfileOpen}
        onClose={() => setIsProfileOpen(false)}
        stats={stats}
        settings={settings}
        onResetStats={handleResetStats}
      />

      <HistoryModal
        isOpen={isHistoryOpen}
        onClose={() => setIsHistoryOpen(false)}
        historyList={historyList}
        settings={settings}
        onClearHistory={() => {
          setHistoryList([]);
          localStorage.removeItem('royal_chess_history');
        }}
      />

      <OnlineLobbyModal
        isOpen={isOnlineLobbyOpen}
        onClose={() => setIsOnlineLobbyOpen(false)}
        settings={settings}
        onCreateRoom={handleCreateOnlineRoom}
        onJoinRoom={handleJoinOnlineRoom}
        activeRoomCode={onlineRoomCode}
        isWaitingForGuest={!!onlineRoomCode && !onlineOpponentConnected}
      />

      <GameSetupModal
        isOpen={isSetupOpen}
        onClose={() => setIsSetupOpen(false)}
        mode={gameMode as 'computer' | 'local'}
        settings={settings}
        onStartGame={startNewGame}
      />

      <GameOverModal
        isOpen={isGameOverOpen}
        onClose={() => setIsGameOverOpen(false)}
        result={gameResult || 'draw'}
        winnerColor={winnerColor}
        winReason={winReason}
        pgn={chess.pgn() || moveHistory.join(' ')}
        settings={settings}
        onRematch={handleRematch}
        onBackToMenu={() => {
          setIsGameOverOpen(false);
          setCurrentScreen('menu');
        }}
      />

      <GitHubDeployModal
        isOpen={isDeployOpen}
        onClose={() => setIsDeployOpen(false)}
      />
    </div>
  );
}
