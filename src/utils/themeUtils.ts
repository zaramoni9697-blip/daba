import { BoardTheme } from '../types/chess';

export interface BoardThemeColors {
  lightSquare: string;
  darkSquare: string;
  selectedSquare: string;
  lastMoveSquare: string;
  possibleMoveDot: string;
  possibleCaptureRing: string;
  checkSquare: string;
  borderColor: string;
  coordColorLight: string;
  coordColorDark: string;
}

export const BOARD_THEMES: Record<BoardTheme, BoardThemeColors> = {
  walnut: {
    lightSquare: 'bg-[#f0d9b5]',
    darkSquare: 'bg-[#b58863]',
    selectedSquare: 'bg-amber-400/70',
    lastMoveSquare: 'bg-yellow-300/40',
    possibleMoveDot: 'bg-stone-900/30',
    possibleCaptureRing: 'border-red-500/70',
    checkSquare: 'bg-red-600/70 shadow-inner',
    borderColor: 'border-[#78350f]',
    coordColorLight: 'text-[#b58863]',
    coordColorDark: 'text-[#f0d9b5]',
  },
  emerald: {
    lightSquare: 'bg-[#eeeed2]',
    darkSquare: 'bg-[#769656]',
    selectedSquare: 'bg-emerald-300/70',
    lastMoveSquare: 'bg-lime-400/40',
    possibleMoveDot: 'bg-emerald-950/30',
    possibleCaptureRing: 'border-emerald-700/80',
    checkSquare: 'bg-red-600/70 shadow-inner',
    borderColor: 'border-[#3f6212]',
    coordColorLight: 'text-[#769656]',
    coordColorDark: 'text-[#eeeed2]',
  },
  royal: {
    lightSquare: 'bg-[#fef9c3]',
    darkSquare: 'bg-[#b45309]',
    selectedSquare: 'bg-yellow-400/70',
    lastMoveSquare: 'bg-amber-300/45',
    possibleMoveDot: 'bg-amber-950/35',
    possibleCaptureRing: 'border-yellow-600/80',
    checkSquare: 'bg-red-600/70 shadow-inner',
    borderColor: 'border-[#713f12]',
    coordColorLight: 'text-[#b45309]',
    coordColorDark: 'text-[#fef9c3]',
  },
  midnight: {
    lightSquare: 'bg-[#94a3b8]',
    darkSquare: 'bg-[#334155]',
    selectedSquare: 'bg-cyan-500/60',
    lastMoveSquare: 'bg-blue-400/35',
    possibleMoveDot: 'bg-slate-950/40',
    possibleCaptureRing: 'border-cyan-400/80',
    checkSquare: 'bg-rose-600/70 shadow-inner',
    borderColor: 'border-[#1e293b]',
    coordColorLight: 'text-[#334155]',
    coordColorDark: 'text-[#94a3b8]',
  },
  marble: {
    lightSquare: 'bg-[#f1f5f9]',
    darkSquare: 'bg-[#64748b]',
    selectedSquare: 'bg-indigo-300/60',
    lastMoveSquare: 'bg-sky-200/40',
    possibleMoveDot: 'bg-slate-900/30',
    possibleCaptureRing: 'border-indigo-600/70',
    checkSquare: 'bg-red-600/70 shadow-inner',
    borderColor: 'border-[#475569]',
    coordColorLight: 'text-[#64748b]',
    coordColorDark: 'text-[#f1f5f9]',
  },
};
