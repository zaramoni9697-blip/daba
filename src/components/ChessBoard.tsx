import React, { useState } from 'react';
import { Chess, Square, PieceSymbol, Color, Move } from 'chess.js';
import { ChessPiece } from './ChessPiece';
import { BOARD_THEMES } from '../utils/themeUtils';
import { BoardTheme, PieceTheme } from '../types/chess';

interface ChessBoardProps {
  chess: Chess;
  boardOrientation: Color; // 'w' or 'b'
  boardTheme: BoardTheme;
  pieceTheme: PieceTheme;
  showCoordinates: boolean;
  interactive: boolean;
  lastMove: { from: Square; to: Square } | null;
  onMakeMove: (from: Square, to: Square, promotion?: PieceSymbol) => boolean;
}

export const ChessBoard: React.FC<ChessBoardProps> = ({
  chess,
  boardOrientation,
  boardTheme,
  pieceTheme,
  showCoordinates,
  interactive,
  lastMove,
  onMakeMove,
}) => {
  const [selectedSquare, setSelectedSquare] = useState<Square | null>(null);
  const [pendingPromotion, setPendingPromotion] = useState<{ from: Square; to: Square } | null>(null);
  const [draggedSquare, setDraggedSquare] = useState<Square | null>(null);

  const colors = BOARD_THEMES[boardTheme];
  const board = chess.board();
  const turn = chess.turn();

  // Find King square if under check
  let checkSquare: Square | null = null;
  if (chess.isCheck()) {
    for (let r = 0; r < 8; r++) {
      for (let c = 0; c < 8; c++) {
        const piece = board[r][c];
        if (piece && piece.type === 'k' && piece.color === turn) {
          checkSquare = (String.fromCharCode('a'.charCodeAt(0) + c) + (8 - r)) as Square;
          break;
        }
      }
      if (checkSquare) break;
    }
  }

  // Get legal moves for selected square
  const legalMovesForSelected: Move[] = selectedSquare
    ? chess.moves({ square: selectedSquare, verbose: true })
    : [];

  const legalTargets = new Map<Square, Move>();
  legalMovesForSelected.forEach((m) => {
    legalTargets.set(m.to, m);
  });

  const handleSquareClick = (square: Square) => {
    if (!interactive) return;

    // If already selected, clicking another legal target executes the move
    if (selectedSquare) {
      if (legalTargets.has(square)) {
        attemptMove(selectedSquare, square);
        return;
      }

      // If clicking own piece again, switch selection
      const pieceOnSquare = chess.get(square);
      if (pieceOnSquare && pieceOnSquare.color === turn) {
        setSelectedSquare(square);
        return;
      }

      // Otherwise deselect
      setSelectedSquare(null);
      return;
    }

    // Select piece if it's the current player's piece
    const piece = chess.get(square);
    if (piece && piece.color === turn) {
      setSelectedSquare(square);
    }
  };

  const attemptMove = (from: Square, to: Square) => {
    const piece = chess.get(from);
    if (!piece) return;

    // Check if promotion is required
    const isPawnPromotion =
      piece.type === 'p' &&
      ((piece.color === 'w' && to[1] === '8') || (piece.color === 'b' && to[1] === '1'));

    if (isPawnPromotion) {
      setPendingPromotion({ from, to });
      return;
    }

    const success = onMakeMove(from, to);
    if (success) {
      setSelectedSquare(null);
    }
  };

  const handlePromotionSelect = (promotionPiece: PieceSymbol) => {
    if (!pendingPromotion) return;
    const { from, to } = pendingPromotion;
    onMakeMove(from, to, promotionPiece);
    setPendingPromotion(null);
    setSelectedSquare(null);
  };

  // Drag and Drop support
  const handleDragStart = (e: React.DragEvent, square: Square) => {
    if (!interactive) {
      e.preventDefault();
      return;
    }
    const piece = chess.get(square);
    if (!piece || piece.color !== turn) {
      e.preventDefault();
      return;
    }
    setDraggedSquare(square);
    setSelectedSquare(square);
    e.dataTransfer.setData('text/plain', square);
    e.dataTransfer.effectAllowed = 'move';
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
  };

  const handleDrop = (e: React.DragEvent, targetSquare: Square) => {
    e.preventDefault();
    const sourceSquare = draggedSquare || (e.dataTransfer.getData('text/plain') as Square);
    setDraggedSquare(null);

    if (sourceSquare && sourceSquare !== targetSquare) {
      const legal = chess.moves({ square: sourceSquare, verbose: true }).some((m) => m.to === targetSquare);
      if (legal) {
        attemptMove(sourceSquare, targetSquare);
      }
    }
  };

  // Generate ranks and files based on orientation
  const ranks = boardOrientation === 'w' ? [8, 7, 6, 5, 4, 3, 2, 1] : [1, 2, 3, 4, 5, 6, 7, 8];
  const files = boardOrientation === 'w' ? ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'] : ['h', 'g', 'f', 'e', 'd', 'c', 'b', 'a'];

  return (
    <div className="relative mx-auto w-full max-w-[min(92vw,480px)] aspect-square select-none shadow-2xl rounded-2xl overflow-hidden border-4 border-slate-900 bg-slate-950 p-1 sm:p-2">
      {/* 8x8 Board Container */}
      <div className={`grid grid-cols-8 grid-rows-8 w-full h-full rounded-xl overflow-hidden border ${colors.borderColor} shadow-inner`}>
        {ranks.map((rank, rankIdx) =>
          files.map((file, fileIdx) => {
            const square = `${file}${rank}` as Square;
            const isDarkSquare = (rankIdx + fileIdx) % 2 === 1;
            const piece = chess.get(square);

            const isSelected = selectedSquare === square;
            const isLastMoveSquare = lastMove && (lastMove.from === square || lastMove.to === square);
            const isCheckSquare = checkSquare === square;
            const isLegalTarget = legalTargets.has(square);
            const isLegalCapture = isLegalTarget && !!piece;

            // Coordinates positioning
            const showRankCoord = showCoordinates && fileIdx === 0;
            const showFileCoord = showCoordinates && rankIdx === 7;

            return (
              <div
                key={square}
                onClick={() => handleSquareClick(square)}
                onDragOver={handleDragOver}
                onDrop={(e) => handleDrop(e, square)}
                className={`relative flex items-center justify-center cursor-pointer transition-colors ${
                  isDarkSquare ? colors.darkSquare : colors.lightSquare
                }`}
              >
                {/* Highlight Overlays */}
                {isLastMoveSquare && !isSelected && (
                  <div className={`absolute inset-0 ${colors.lastMoveSquare} pointer-events-none`} />
                )}
                {isSelected && (
                  <div className={`absolute inset-0 ${colors.selectedSquare} pointer-events-none ring-2 ring-amber-400`} />
                )}
                {isCheckSquare && (
                  <div className={`absolute inset-0 ${colors.checkSquare} pointer-events-none animate-pulse`} />
                )}

                {/* Rank coordinate label (1-8) */}
                {showRankCoord && (
                  <span
                    className={`absolute top-0.5 left-1 font-bold text-[9px] sm:text-[11px] pointer-events-none ${
                      isDarkSquare ? colors.coordColorDark : colors.coordColorLight
                    }`}
                  >
                    {rank}
                  </span>
                )}

                {/* File coordinate label (a-h) */}
                {showFileCoord && (
                  <span
                    className={`absolute bottom-0.5 right-1 font-bold text-[9px] sm:text-[11px] pointer-events-none ${
                      isDarkSquare ? colors.coordColorDark : colors.coordColorLight
                    }`}
                  >
                    {file}
                  </span>
                )}

                {/* Piece Element */}
                {piece && (
                  <div
                    draggable={interactive && piece.color === turn}
                    onDragStart={(e) => handleDragStart(e, square)}
                    className="w-[92%] h-[92%] flex items-center justify-center transform active:scale-90 hover:scale-105 transition-transform duration-75 select-none"
                  >
                    <ChessPiece piece={piece.type} color={piece.color} theme={pieceTheme} />
                  </div>
                )}

                {/* Legal Move Indicator Dot */}
                {isLegalTarget && !isLegalCapture && (
                  <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                    <div className={`w-3.5 h-3.5 sm:w-4 sm:h-4 rounded-full ${colors.possibleMoveDot} shadow-sm animate-pulse`} />
                  </div>
                )}

                {/* Legal Capture Ring Indicator */}
                {isLegalCapture && (
                  <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                    <div className={`w-[86%] h-[86%] rounded-full border-4 ${colors.possibleCaptureRing} animate-pulse`} />
                  </div>
                )}
              </div>
            );
          })
        )}
      </div>

      {/* Pawn Promotion Modal */}
      {pendingPromotion && (
        <div className="absolute inset-0 z-50 flex items-center justify-center bg-slate-950/80 backdrop-blur-xs p-4 animate-fadeIn">
          <div className="bg-slate-900 border-2 border-amber-500/80 rounded-2xl p-4 shadow-2xl max-w-xs w-full text-center">
            <h3 className="text-amber-400 font-bold text-sm tracking-wider uppercase mb-3">
              Promote Pawn
            </h3>
            <div className="grid grid-cols-4 gap-2">
              {(['q', 'r', 'b', 'n'] as PieceSymbol[]).map((pType) => (
                <button
                  key={pType}
                  onClick={() => handlePromotionSelect(pType)}
                  className="flex flex-col items-center justify-center p-2 rounded-xl bg-slate-800 hover:bg-slate-700 border border-slate-700 hover:border-amber-400 transition transform hover:scale-105"
                >
                  <div className="w-10 h-10">
                    <ChessPiece piece={pType} color={turn} theme={pieceTheme} />
                  </div>
                  <span className="text-[10px] text-slate-300 uppercase mt-1 font-semibold">
                    {pType === 'q' ? 'Queen' : pType === 'r' ? 'Rook' : pType === 'b' ? 'Bishop' : 'Knight'}
                  </span>
                </button>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
