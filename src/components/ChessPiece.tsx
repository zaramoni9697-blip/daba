import React from 'react';
import { PieceSymbol, Color } from 'chess.js';
import { PieceTheme } from '../types/chess';

interface ChessPieceProps {
  piece: PieceSymbol;
  color: Color;
  theme?: PieceTheme;
  className?: string;
}

export const ChessPiece: React.FC<ChessPieceProps> = ({
  piece,
  color,
  theme = 'royal',
  className = 'w-full h-full',
}) => {
  const isWhite = color === 'w';
  const idPrefix = `p_${theme}_${color}_${piece}`;

  // Palette configurations per theme
  const getThemePalette = () => {
    switch (theme) {
      case 'neon':
        return {
          gradStart: isWhite ? '#38bdf8' : '#ec4899',
          gradMid: isWhite ? '#0284c7' : '#be185d',
          gradEnd: isWhite ? '#0c4a6e' : '#831843',
          stroke: isWhite ? '#e0f2fe' : '#fce7f3',
          strokeWidth: '1.8',
          accent: isWhite ? '#7dd3fc' : '#f472b6',
          jewel: isWhite ? '#67e8f9' : '#fb7185',
          glow: isWhite ? 'rgba(56, 189, 248, 0.6)' : 'rgba(236, 72, 153, 0.6)',
        };
      case 'vintage':
        return {
          gradStart: isWhite ? '#fffbeb' : '#451a03',
          gradMid: isWhite ? '#fef3c7' : '#290e02',
          gradEnd: isWhite ? '#d97706' : '#140501',
          stroke: isWhite ? '#92400e' : '#fef3c7',
          strokeWidth: '1.6',
          accent: isWhite ? '#b45309' : '#f59e0b',
          jewel: '#d97706',
          glow: 'rgba(0,0,0,0.3)',
        };
      case 'gothic':
        return {
          gradStart: isWhite ? '#f1f5f9' : '#1e293b',
          gradMid: isWhite ? '#cbd5e1' : '#0f172a',
          gradEnd: isWhite ? '#64748b' : '#020617',
          stroke: isWhite ? '#1e293b' : '#94a3b8',
          strokeWidth: '1.8',
          accent: isWhite ? '#dc2626' : '#ef4444',
          jewel: '#ef4444',
          glow: 'rgba(0,0,0,0.4)',
        };
      case 'staunton':
        return {
          gradStart: isWhite ? '#fafaf9' : '#292524',
          gradMid: isWhite ? '#e7e5e4' : '#1c1917',
          gradEnd: isWhite ? '#a8a29e' : '#0c0a09',
          stroke: isWhite ? '#44403c' : '#d6d3d1',
          strokeWidth: '1.6',
          accent: isWhite ? '#d6d3d1' : '#78716c',
          jewel: isWhite ? '#eab308' : '#f59e0b',
          glow: 'rgba(0,0,0,0.35)',
        };
      case 'royal':
      default:
        // Ultra-luxurious 3D Imperial Gold & Royal Obsidian
        return {
          gradStart: isWhite ? '#ffffff' : '#1e293b',
          gradMid: isWhite ? '#fef3c7' : '#0f172a',
          gradEnd: isWhite ? '#fde047' : '#020617',
          stroke: isWhite ? '#854d0e' : '#facc15',
          strokeWidth: '1.6',
          accent: isWhite ? '#ca8a04' : '#eab308',
          jewel: isWhite ? '#e11d48' : '#38bdf8',
          goldTrim: '#eab308',
          glow: isWhite ? 'rgba(234, 179, 8, 0.4)' : 'rgba(0,0,0,0.5)',
        };
    }
  };

  const pal = getThemePalette();

  const renderDefinitions = () => (
    <defs>
      {/* Primary body 3D gradient */}
      <linearGradient id={`${idPrefix}_grad`} x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stopColor={pal.gradStart} />
        <stop offset="45%" stopColor={pal.gradMid} />
        <stop offset="100%" stopColor={pal.gradEnd} />
      </linearGradient>

      {/* Gold metallic sheen */}
      <linearGradient id={`${idPrefix}_gold`} x1="0%" y1="0%" x2="100%" y2="0%">
        <stop offset="0%" stopColor="#fef08a" />
        <stop offset="50%" stopColor="#eab308" />
        <stop offset="100%" stopColor="#a16207" />
      </linearGradient>

      {/* Top highlight for glossy 3D curve */}
      <linearGradient id={`${idPrefix}_highlight`} x1="0%" y1="0%" x2="0%" y2="100%">
        <stop offset="0%" stopColor="#ffffff" stopOpacity={isWhite ? '0.85' : '0.4'} />
        <stop offset="100%" stopColor="#ffffff" stopOpacity="0.0" />
      </linearGradient>

      {/* Subtle depth drop shadow */}
      <filter id={`${idPrefix}_shadow`} x="-20%" y="-20%" width="140%" height="140%">
        <feDropShadow dx="0" dy="2.5" stdDeviation="2" floodColor="#000000" floodOpacity="0.45" />
      </filter>
    </defs>
  );

  const renderPiecePath = () => {
    switch (piece) {
      case 'p': // Pawn (রয়্যাল সৈন্য / বোড়ে)
        return (
          <g filter={`url(#${idPrefix}_shadow)`}>
            {/* Base Plinth */}
            <path
              d="M 12 42 L 38 42 C 37 39, 35 38, 33 37 L 17 37 C 15 38, 13 39, 12 42 Z"
              fill={`url(#${idPrefix}_grad)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
            />
            {/* Lower Torso Ring */}
            <rect
              x="15"
              y="34"
              width="20"
              height="3.5"
              rx="1.7"
              fill={`url(#${idPrefix}_gold)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
            />
            {/* Tapered Waist */}
            <path
              d="M 17 34 C 18 27, 21 24, 21 21 L 29 21 C 29 24, 32 27, 33 34 Z"
              fill={`url(#${idPrefix}_grad)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
            />
            {/* Collar Band */}
            <ellipse
              cx="25"
              cy="20.5"
              rx="7"
              ry="2.2"
              fill={`url(#${idPrefix}_gold)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
            />
            {/* Spherical Head with 3D highlight */}
            <circle
              cx="25"
              cy="13.5"
              r="7.5"
              fill={`url(#${idPrefix}_grad)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
            />
            {/* Light reflection gleam */}
            <ellipse
              cx="22.5"
              cy="10.8"
              rx="3.2"
              ry="2"
              fill={`url(#${idPrefix}_highlight)`}
            />
            {/* Small jewel tip */}
            <circle cx="25" cy="5.8" r="1.3" fill={pal.jewel} />
          </g>
        );

      case 'r': // Rook (দুর্গ / নৌকা)
        return (
          <g filter={`url(#${idPrefix}_shadow)`}>
            {/* Fortress Base Plinth */}
            <path
              d="M 10 42 L 40 42 C 38 38, 36 37, 33 36 L 17 36 C 14 37, 12 38, 10 42 Z"
              fill={`url(#${idPrefix}_grad)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
            />
            <rect
              x="13"
              y="33.5"
              width="24"
              height="3"
              rx="1.5"
              fill={`url(#${idPrefix}_gold)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
            />
            {/* Heavy Stone Tower Walls */}
            <path
              d="M 16 33.5 L 18 19 L 32 19 L 34 33.5 Z"
              fill={`url(#${idPrefix}_grad)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
            />
            {/* Masonry relief / arrow slit */}
            <line x1="25" y1="23" x2="25" y2="29" stroke={pal.accent} strokeWidth="2" strokeLinecap="round" />
            <line x1="22" y1="26" x2="28" y2="26" stroke={pal.accent} strokeWidth="1.5" strokeLinecap="round" />

            {/* Cornice ring */}
            <rect
              x="13.5"
              y="16.5"
              width="23"
              height="3"
              rx="1.2"
              fill={`url(#${idPrefix}_gold)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
            />
            {/* Castellated Battlements with 4 crenels */}
            <path
              d="M 13 16.5 L 13 9.5 L 17.5 9.5 L 17.5 13 L 22 13 L 22 9.5 L 28 9.5 L 28 13 L 32.5 13 L 32.5 9.5 L 37 9.5 L 37 16.5 Z"
              fill={`url(#${idPrefix}_grad)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
            />
            {/* Battlement golden rivets */}
            <circle cx="15.2" cy="11.5" r="1.1" fill={pal.jewel} />
            <circle cx="25" cy="11.5" r="1.1" fill={pal.jewel} />
            <circle cx="34.8" cy="11.5" r="1.1" fill={pal.jewel} />
          </g>
        );

      case 'n': // Knight (ঘোড়া / রয়্যাল অশ্ব)
        return (
          <g filter={`url(#${idPrefix}_shadow)`}>
            {/* Pedestal Base */}
            <path
              d="M 12 42 L 38 42 C 37 38, 35 37, 32 36 L 18 36 C 15 37, 13 38, 12 42 Z"
              fill={`url(#${idPrefix}_grad)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
            />
            <rect
              x="14"
              y="33.5"
              width="22"
              height="3"
              rx="1.5"
              fill={`url(#${idPrefix}_gold)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
            />
            {/* Proud Muscular Knight Body with flowing mane */}
            <path
              d="M 15 34 C 15 30, 18 28, 20 26 C 17 25, 14 22, 13.5 18 C 13 14, 15 11, 19 9 C 23 7, 27 7.5, 30 10 C 33 12.5, 35 16, 35 22 C 35 25, 34 29, 32 34 Z"
              fill={`url(#${idPrefix}_grad)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
              strokeLinejoin="round"
            />
            {/* Snout and Lower Jaw */}
            <path
              d="M 19 9 C 16 11, 12 14, 12 17 C 12 20, 15 21, 18 20 C 18 23, 22 25, 25 24"
              fill={`url(#${idPrefix}_grad)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
            />
            {/* Piercing Eye */}
            <circle cx="21" cy="13.5" r="1.7" fill={pal.jewel} />
            <circle cx="20.5" cy="13" r="0.6" fill="#ffffff" />

            {/* Crested Carved Mane Tufts */}
            <path
              d="M 28 8.5 C 31 10, 34 13, 34.5 16 M 30 14 C 33 17, 35 21, 35 24 M 31 22 C 33 25, 34.5 28, 34 31"
              fill="none"
              stroke={pal.accent}
              strokeWidth="2.2"
              strokeLinecap="round"
            />
            {/* Royal Bridle / Medallion */}
            <circle cx="22" cy="19.5" r="1.6" fill={pal.accent} stroke={pal.stroke} strokeWidth="1" />
            <path d="M 15 17.5 L 22 19.5 L 29 20" fill="none" stroke={pal.stroke} strokeWidth="1.2" />
          </g>
        );

      case 'b': // Bishop (গজ / বিশপ)
        return (
          <g filter={`url(#${idPrefix}_shadow)`}>
            {/* Base Plinth */}
            <path
              d="M 12 42 L 38 42 C 36 38, 34 37, 31 36 L 19 36 C 16 37, 14 38, 12 42 Z"
              fill={`url(#${idPrefix}_grad)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
            />
            <rect
              x="14"
              y="33"
              width="22"
              height="3.5"
              rx="1.5"
              fill={`url(#${idPrefix}_gold)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
            />
            {/* Tapered Pillar Body */}
            <path
              d="M 17 33 C 18 27, 21 24, 21 22 L 29 22 C 29 24, 32 27, 33 33 Z"
              fill={`url(#${idPrefix}_grad)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
            />
            {/* Golden Collar with Jewel */}
            <ellipse
              cx="25"
              cy="21.5"
              rx="7.5"
              ry="2.2"
              fill={`url(#${idPrefix}_gold)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
            />
            <circle cx="25" cy="21.5" r="1.3" fill={pal.jewel} />

            {/* Mitre Head (pointed arched egg) */}
            <path
              d="M 25 7 C 17 9, 16 20, 21 22 L 29 22 C 34 20, 33 9, 25 7 Z"
              fill={`url(#${idPrefix}_grad)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
            />
            {/* Deep Mitre Cut Slice */}
            <path
              d="M 22 11 L 28 17"
              stroke={pal.accent}
              strokeWidth="2.4"
              strokeLinecap="round"
            />
            {/* Cross on Crown tip */}
            <circle cx="25" cy="6" r="2.2" fill={pal.jewel} stroke={pal.stroke} strokeWidth="1.2" />
            <line x1="25" y1="3.5" x2="25" y2="8.5" stroke={pal.accent} strokeWidth="1.5" strokeLinecap="round" />
            <line x1="22.5" y1="6" x2="27.5" y2="6" stroke={pal.accent} strokeWidth="1.5" strokeLinecap="round" />
          </g>
        );

      case 'q': // Queen (উজির / রানী)
        return (
          <g filter={`url(#${idPrefix}_shadow)`}>
            {/* Imperial Base */}
            <path
              d="M 10 42 L 40 42 C 38 38, 35 37, 32 36 L 18 36 C 15 37, 12 38, 10 42 Z"
              fill={`url(#${idPrefix}_grad)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
            />
            <rect
              x="13"
              y="33"
              width="24"
              height="3.5"
              rx="1.7"
              fill={`url(#${idPrefix}_gold)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
            />
            {/* Flowing Royal Robe Torso */}
            <path
              d="M 16 33 C 17 26, 21 23, 21 21 L 29 21 C 29 23, 33 26, 34 33 Z"
              fill={`url(#${idPrefix}_grad)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
            />
            {/* Royal Gold Sash */}
            <ellipse
              cx="25"
              cy="21"
              rx="8.5"
              ry="2.4"
              fill={`url(#${idPrefix}_gold)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
            />
            <circle cx="25" cy="21" r="1.5" fill={pal.jewel} />

            {/* 5-Peak Queen Crown */}
            <path
              d="M 14 20 L 12 11 L 18 16 L 25 8 L 32 16 L 38 11 L 36 20 Z"
              fill={`url(#${idPrefix}_grad)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
              strokeLinejoin="round"
            />
            {/* Glowing Crown Pearls on each peak */}
            <circle cx="12" cy="10" r="2.2" fill={pal.jewel} stroke={pal.stroke} strokeWidth="1" />
            <circle cx="18" cy="15" r="1.8" fill={pal.jewel} stroke={pal.stroke} strokeWidth="1" />
            <circle cx="25" cy="7" r="2.8" fill={`url(#${idPrefix}_gold)`} stroke={pal.stroke} strokeWidth="1.2" />
            <circle cx="25" cy="7" r="1.3" fill={pal.jewel} />
            <circle cx="32" cy="15" r="1.8" fill={pal.jewel} stroke={pal.stroke} strokeWidth="1" />
            <circle cx="38" cy="10" r="2.2" fill={pal.jewel} stroke={pal.stroke} strokeWidth="1" />
          </g>
        );

      case 'k': // King (রাজা / সুলতান)
        return (
          <g filter={`url(#${idPrefix}_shadow)`}>
            {/* Imperial Base */}
            <path
              d="M 10 42 L 40 42 C 38 38, 35 37, 32 36 L 18 36 C 15 37, 12 38, 10 42 Z"
              fill={`url(#${idPrefix}_grad)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
            />
            <rect
              x="13"
              y="33"
              width="24"
              height="3.5"
              rx="1.7"
              fill={`url(#${idPrefix}_gold)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
            />
            {/* Regal Robe Body */}
            <path
              d="M 16 33 C 17 26, 21 23, 21 20.5 L 29 20.5 C 29 23, 33 26, 34 33 Z"
              fill={`url(#${idPrefix}_grad)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
            />
            {/* Ornamental Imperial Belt */}
            <ellipse
              cx="25"
              cy="20.5"
              rx="8.5"
              ry="2.4"
              fill={`url(#${idPrefix}_gold)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
            />
            <circle cx="25" cy="20.5" r="1.5" fill={pal.jewel} />

            {/* Regal Imperial Crown Dome with Ribs */}
            <path
              d="M 14 20.5 C 13 14, 18 12, 25 12 C 32 12, 37 14, 36 20.5 Z"
              fill={`url(#${idPrefix}_grad)`}
              stroke={pal.stroke}
              strokeWidth={pal.strokeWidth}
            />
            {/* Crown Arch Ribs */}
            <path
              d="M 19 20 C 19 15, 22 13, 25 13 C 28 13, 31 15, 31 20"
              fill="none"
              stroke={pal.accent}
              strokeWidth="1.8"
            />

            {/* Imperial Cross Finial on Top with Central Ruby/Sapphire */}
            <g>
              <circle cx="25" cy="11.5" r="2.2" fill={`url(#${idPrefix}_gold)`} stroke={pal.stroke} strokeWidth="1" />
              {/* Vertical Cross Shaft */}
              <line x1="25" y1="4" x2="25" y2="12" stroke={pal.accent} strokeWidth="2.8" strokeLinecap="square" />
              {/* Horizontal Cross Bar */}
              <line x1="21.5" y1="7" x2="28.5" y2="7" stroke={pal.accent} strokeWidth="2.8" strokeLinecap="square" />
              {/* Core jewel */}
              <circle cx="25" cy="7" r="1.4" fill={pal.jewel} />
            </g>
          </g>
        );

      default:
        return null;
    }
  };

  return (
    <svg
      viewBox="0 0 50 50"
      className={`${className} pointer-events-none select-none transition-transform duration-100 ease-out`}
    >
      {renderDefinitions()}
      {renderPiecePath()}
    </svg>
  );
};
