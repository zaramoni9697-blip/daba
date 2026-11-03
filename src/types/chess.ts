import { PieceSymbol, Color, Square } from 'chess.js';

export type GameMode = 'menu' | 'computer' | 'local' | 'online' | 'history' | 'settings' | 'profile';

export type BoardTheme = 'walnut' | 'emerald' | 'royal' | 'midnight' | 'marble';
export type PieceTheme = 'royal' | 'staunton' | 'vintage' | 'neon' | 'gothic';

export interface TimeControl {
  id: string;
  name: string;
  initialSeconds: number; // 0 = unlimited
  incrementSeconds: number;
}

export const TIME_CONTROLS: TimeControl[] = [
  { id: 'rapid10', name: 'Rapid 10+0 (Standard)', initialSeconds: 600, incrementSeconds: 0 },
  { id: 'rapid15_10', name: 'Rapid 15+10 (Relaxed)', initialSeconds: 900, incrementSeconds: 10 },
  { id: 'classical20', name: 'Classical 20+0 (Deep)', initialSeconds: 1200, incrementSeconds: 0 },
  { id: 'classical30', name: 'Classical 30+0 (Tournament)', initialSeconds: 1800, incrementSeconds: 0 },
  { id: 'blitz5_3', name: 'Blitz 5+3 (Active)', initialSeconds: 300, incrementSeconds: 3 },
  { id: 'none', name: 'Casual (Unlimited Time)', initialSeconds: 0, incrementSeconds: 0 },
  { id: 'bullet2_1', name: 'Bullet 2+1 (Speed)', initialSeconds: 120, incrementSeconds: 1 },
];

export interface PlayerStats {
  gamesPlayed: number;
  wins: number;
  losses: number;
  draws: number;
  vsComputer: {
    wins: number;
    losses: number;
    draws: number;
  };
  vsLocal: {
    games: number;
  };
  vsOnline: {
    wins: number;
    losses: number;
    draws: number;
  };
}

export interface GameRecord {
  id: string;
  date: string;
  mode: 'computer' | 'local' | 'online';
  result: 'win' | 'loss' | 'draw';
  reason: string;
  whitePlayer: string;
  blackPlayer: string;
  timeControl: string;
  pgn: string;
  movesCount: number;
}

export interface UserSettings {
  boardTheme: BoardTheme;
  pieceTheme: PieceTheme;
  soundEnabled: boolean;
  showCoordinates: boolean;
  autoFlipLocal: boolean;
  language: 'en' | 'bn';
  playerName: string;
}

export interface MoveDetail {
  from: Square;
  to: Square;
  san: string;
  piece: PieceSymbol;
  color: Color;
  captured?: PieceSymbol;
  promotion?: PieceSymbol;
  fenAfter: string;
}
