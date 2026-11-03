import React from 'react';
import { Chess, PieceSymbol, Color } from 'chess.js';
import { ChessPiece } from './ChessPiece';
import { PieceTheme } from '../types/chess';
import { Copy, Check, FileText } from 'lucide-react';

interface GameDetailsProps {
  chess: Chess;
  pieceTheme: PieceTheme;
  history: string[];
  whitePlayer: string;
  blackPlayer: string;
}

const PIECE_WEIGHTS: Record<PieceSymbol, number> = {
  p: 1,
  n: 3,
  b: 3,
  r: 5,
  q: 9,
  k: 0,
};

export const GameDetails: React.FC<GameDetailsProps> = ({
  chess,
  pieceTheme,
  history,
  whitePlayer,
  blackPlayer,
}) => {
  const [copiedPgn, setCopiedPgn] = React.useState(false);
  const [copiedFen, setCopiedFen] = React.useState(false);

  // Compute captured pieces by comparing starting pieces to current board
  const board = chess.board();
  const currentPieces: Record<Color, Record<PieceSymbol, number>> = {
    w: { p: 0, n: 0, b: 0, r: 0, q: 0, k: 0 },
    b: { p: 0, n: 0, b: 0, r: 0, q: 0, k: 0 },
  };

  for (let r = 0; r < 8; r++) {
    for (let c = 0; c < 8; c++) {
      const p = board[r][c];
      if (p) {
        currentPieces[p.color][p.type]++;
      }
    }
  }

  const startingPieces: Record<PieceSymbol, number> = {
    p: 8,
    n: 2,
    b: 2,
    r: 2,
    q: 1,
    k: 1,
  };

  const capturedByWhite: { type: PieceSymbol; count: number }[] = [];
  const capturedByBlack: { type: PieceSymbol; count: number }[] = [];

  let whiteMaterial = 0;
  let blackMaterial = 0;

  (['q', 'r', 'b', 'n', 'p'] as PieceSymbol[]).forEach((type) => {
    // Pieces captured by White are Black's lost pieces
    const blackLost = Math.max(0, startingPieces[type] - currentPieces['b'][type]);
    if (blackLost > 0) {
      capturedByWhite.push({ type, count: blackLost });
    }
    // Pieces captured by Black are White's lost pieces
    const whiteLost = Math.max(0, startingPieces[type] - currentPieces['w'][type]);
    if (whiteLost > 0) {
      capturedByBlack.push({ type, count: whiteLost });
    }

    whiteMaterial += currentPieces['w'][type] * PIECE_WEIGHTS[type];
    blackMaterial += currentPieces['b'][type] * PIECE_WEIGHTS[type];
  });

  const whiteAdvantage = whiteMaterial - blackMaterial;
  const blackAdvantage = blackMaterial - whiteMaterial;

  const copyPgn = () => {
    navigator.clipboard.writeText(chess.pgn() || history.join(' '));
    setCopiedPgn(true);
    setTimeout(() => setCopiedPgn(false), 2000);
  };

  const copyFen = () => {
    navigator.clipboard.writeText(chess.fen());
    setCopiedFen(true);
    setTimeout(() => setCopiedFen(false), 2000);
  };

  // Group moves into turns (1. e4 e5, 2. Nf3 Nc6)
  const pairedMoves: { number: number; white: string; black?: string }[] = [];
  for (let i = 0; i < history.length; i += 2) {
    pairedMoves.push({
      number: Math.floor(i / 2) + 1,
      white: history[i],
      black: history[i + 1],
    });
  }

  return (
    <div className="w-full flex flex-col gap-2.5">
      {/* Captured Trays */}
      <div className="grid grid-cols-2 gap-2 text-xs">
        {/* Captured by White */}
        <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-2 flex items-center justify-between min-h-[38px]">
          <div className="flex items-center gap-1 flex-wrap">
            {capturedByWhite.map((item) => (
              <span key={item.type} className="inline-flex items-center">
                <span className="w-4 h-4 inline-block">
                  <ChessPiece piece={item.type} color="b" theme={pieceTheme} />
                </span>
                {item.count > 1 && (
                  <span className="text-[10px] text-slate-400 font-semibold mr-0.5">{item.count}</span>
                )}
              </span>
            ))}
          </div>
          {whiteAdvantage > 0 && (
            <span className="font-bold text-amber-400 text-xs px-1.5 py-0.5 rounded bg-amber-500/10 border border-amber-500/30">
              +{whiteAdvantage}
            </span>
          )}
        </div>

        {/* Captured by Black */}
        <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-2 flex items-center justify-between min-h-[38px]">
          <div className="flex items-center gap-1 flex-wrap">
            {capturedByBlack.map((item) => (
              <span key={item.type} className="inline-flex items-center">
                <span className="w-4 h-4 inline-block">
                  <ChessPiece piece={item.type} color="w" theme={pieceTheme} />
                </span>
                {item.count > 1 && (
                  <span className="text-[10px] text-slate-400 font-semibold mr-0.5">{item.count}</span>
                )}
              </span>
            ))}
          </div>
          {blackAdvantage > 0 && (
            <span className="font-bold text-amber-400 text-xs px-1.5 py-0.5 rounded bg-amber-500/10 border border-amber-500/30">
              +{blackAdvantage}
            </span>
          )}
        </div>
      </div>

      {/* Move History Log */}
      <div className="bg-slate-900/70 border border-slate-800/80 rounded-xl p-2.5">
        <div className="flex items-center justify-between mb-1.5">
          <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
            Move Log ({history.length} moves)
          </span>
          <div className="flex items-center gap-1.5">
            <button
              onClick={copyPgn}
              className="text-[11px] flex items-center gap-1 px-2 py-0.5 rounded bg-slate-800 hover:bg-slate-700 text-slate-300 transition"
              title="Copy PGN"
            >
              {copiedPgn ? <Check className="w-3 h-3 text-emerald-400" /> : <FileText className="w-3 h-3" />}
              PGN
            </button>
            <button
              onClick={copyFen}
              className="text-[11px] flex items-center gap-1 px-2 py-0.5 rounded bg-slate-800 hover:bg-slate-700 text-slate-300 transition"
              title="Copy FEN"
            >
              {copiedFen ? <Check className="w-3 h-3 text-emerald-400" /> : <Copy className="w-3 h-3" />}
              FEN
            </button>
          </div>
        </div>

        <div className="h-20 sm:h-24 overflow-y-auto font-mono text-xs text-slate-300 space-y-0.5 pr-1 scrollbar-thin">
          {pairedMoves.length === 0 ? (
            <div className="text-slate-500 italic text-[11px] py-4 text-center">No moves yet. Make your opening move!</div>
          ) : (
            <div className="grid grid-cols-2 gap-x-2 gap-y-1">
              {pairedMoves.map((turn) => (
                <div key={turn.number} className="flex items-center gap-1.5 px-1 py-0.5 rounded hover:bg-slate-800/50">
                  <span className="text-slate-500 font-medium w-5">{turn.number}.</span>
                  <span className="font-semibold text-amber-200">{turn.white}</span>
                  {turn.black && <span className="font-semibold text-slate-200">{turn.black}</span>}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
