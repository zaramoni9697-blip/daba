import express, { Request, Response } from 'express';
import { Chess } from 'chess.js';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const port = process.env.PORT ? parseInt(process.env.PORT, 10) : 3000;

app.use(express.json());

interface TimeControlConfig {
  name: string; // e.g. "Blitz 3+2"
  initialSeconds: number; // e.g. 180
  incrementSeconds: number; // e.g. 2
}

interface RoomPlayer {
  token: string;
  color: 'w' | 'b';
  name: string;
  lastSeen: number;
}

interface RoomState {
  code: string;
  chess: Chess;
  fen: string;
  history: string[]; // SAN notation moves
  status: 'waiting' | 'active' | 'checkmate' | 'draw' | 'resigned' | 'timeout';
  winner: 'w' | 'b' | 'draw' | null;
  winReason?: string;
  host: RoomPlayer;
  guest: RoomPlayer | null;
  timeControl: TimeControlConfig;
  whiteTimeMs: number;
  blackTimeMs: number;
  lastTurnStartTimeMs: number | null;
  drawOfferBy: 'w' | 'b' | null;
  rematchOfferBy: 'w' | 'b' | null;
  createdAt: number;
  lastActiveAt: number;
  listeners: Response[];
}

const rooms = new Map<string, RoomState>();

function generateRoomCode(): string {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
  let code = '';
  for (let i = 0; i < 6; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return code;
}

function broadcastRoom(room: RoomState) {
  const payload = `data: ${JSON.stringify(serializeRoom(room))}\n\n`;
  room.listeners.forEach((res) => {
    try {
      res.write(payload);
    } catch {
      // Stream broken
    }
  });
}

function serializeRoom(room: RoomState, playerToken?: string) {
  // Compute current live clock if active
  let whiteTime = room.whiteTimeMs;
  let blackTime = room.blackTimeMs;
  if (room.status === 'active' && room.lastTurnStartTimeMs) {
    const elapsed = Date.now() - room.lastTurnStartTimeMs;
    const turn = room.chess.turn();
    if (turn === 'w') {
      whiteTime = Math.max(0, whiteTime - elapsed);
    } else {
      blackTime = Math.max(0, blackTime - elapsed);
    }
  }

  const now = Date.now();
  const hostConnected = now - room.host.lastSeen < 15000;
  const guestConnected = room.guest ? now - room.guest.lastSeen < 15000 : false;

  let yourColor: 'w' | 'b' | 'spectator' = 'spectator';
  if (playerToken) {
    if (room.host.token === playerToken) yourColor = room.host.color;
    else if (room.guest?.token === playerToken) yourColor = room.guest.color;
  }

  return {
    code: room.code,
    fen: room.chess.fen(),
    turn: room.chess.turn(),
    isCheck: room.chess.isCheck(),
    isGameOver: room.chess.isGameOver() || room.status !== 'waiting' && room.status !== 'active',
    status: room.status,
    winner: room.winner,
    winReason: room.winReason,
    history: room.history,
    timeControl: room.timeControl,
    whiteTimeMs: whiteTime,
    blackTimeMs: blackTime,
    host: {
      color: room.host.color,
      name: room.host.name,
      connected: hostConnected,
    },
    guest: room.guest
      ? {
          color: room.guest.color,
          name: room.guest.name,
          connected: guestConnected,
        }
      : null,
    drawOfferBy: room.drawOfferBy,
    rematchOfferBy: room.rematchOfferBy,
    yourColor,
  };
}

// Check room clock timeouts every 500ms
setInterval(() => {
  const now = Date.now();
  for (const room of rooms.values()) {
    if (room.status === 'active' && room.lastTurnStartTimeMs && room.timeControl.initialSeconds > 0) {
      const elapsed = now - room.lastTurnStartTimeMs;
      const turn = room.chess.turn();
      const currentRemaining = (turn === 'w' ? room.whiteTimeMs : room.blackTimeMs) - elapsed;
      if (currentRemaining <= 0) {
        if (turn === 'w') {
          room.whiteTimeMs = 0;
          room.status = 'timeout';
          room.winner = 'b';
          room.winReason = 'White ran out of time';
        } else {
          room.blackTimeMs = 0;
          room.status = 'timeout';
          room.winner = 'w';
          room.winReason = 'Black ran out of time';
        }
        broadcastRoom(room);
      }
    }
  }
}, 500);

// Cleanup inactive rooms older than 2 hours
setInterval(() => {
  const cutoff = Date.now() - 2 * 60 * 60 * 1000;
  for (const [code, room] of rooms.entries()) {
    if (room.lastActiveAt < cutoff) {
      rooms.delete(code);
    }
  }
}, 15 * 60 * 1000);

// API Endpoints
app.post('/api/rooms/create', (req: Request, res: Response) => {
  const { hostColor = 'random', playerName = 'Player 1', timeControl } = req.body;
  let code = generateRoomCode();
  while (rooms.has(code)) {
    code = generateRoomCode();
  }

  const assignedHostColor: 'w' | 'b' =
    hostColor === 'random' ? (Math.random() < 0.5 ? 'w' : 'b') : hostColor === 'b' ? 'b' : 'w';

  const initialSec = typeof timeControl?.initialSeconds === 'number' ? timeControl.initialSeconds : 600;
  const incSec = typeof timeControl?.incrementSeconds === 'number' ? timeControl.incrementSeconds : 0;
  const tcName = timeControl?.name || (initialSec === 0 ? 'Unlimited' : `${Math.floor(initialSec / 60)} min`);

  const tc: TimeControlConfig = {
    name: tcName,
    initialSeconds: initialSec,
    incrementSeconds: incSec,
  };

  const hostToken = 'token_' + Math.random().toString(36).substring(2) + Date.now();
  const room: RoomState = {
    code,
    chess: new Chess(),
    fen: 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1',
    history: [],
    status: 'waiting',
    winner: null,
    host: {
      token: hostToken,
      color: assignedHostColor,
      name: playerName || 'Host',
      lastSeen: Date.now(),
    },
    guest: null,
    timeControl: tc,
    whiteTimeMs: initialSec * 1000,
    blackTimeMs: initialSec * 1000,
    lastTurnStartTimeMs: null,
    drawOfferBy: null,
    rematchOfferBy: null,
    createdAt: Date.now(),
    lastActiveAt: Date.now(),
    listeners: [],
  };

  rooms.set(code, room);

  res.json({
    success: true,
    code,
    token: hostToken,
    color: assignedHostColor,
    room: serializeRoom(room, hostToken),
  });
});

app.post('/api/rooms/join', (req: Request, res: Response) => {
  const { code: rawCode, playerName = 'Player 2' } = req.body;
  const code = (rawCode || '').trim().toUpperCase();
  const room = rooms.get(code);

  if (!room) {
    res.status(404).json({ success: false, error: 'Room not found' });
    return;
  }

  if (room.guest) {
    res.status(400).json({ success: false, error: 'Room is already full' });
    return;
  }

  const guestToken = 'token_' + Math.random().toString(36).substring(2) + Date.now();
  const guestColor: 'w' | 'b' = room.host.color === 'w' ? 'b' : 'w';

  room.guest = {
    token: guestToken,
    color: guestColor,
    name: playerName || 'Guest',
    lastSeen: Date.now(),
  };

  // Start game
  room.status = 'active';
  room.lastTurnStartTimeMs = Date.now();
  room.lastActiveAt = Date.now();

  broadcastRoom(room);

  res.json({
    success: true,
    code,
    token: guestToken,
    color: guestColor,
    room: serializeRoom(room, guestToken),
  });
});

app.get('/api/rooms/:code', (req: Request, res: Response) => {
  const code = req.params.code.toUpperCase();
  const token = req.query.token as string | undefined;
  const room = rooms.get(code);

  if (!room) {
    res.status(404).json({ success: false, error: 'Room not found' });
    return;
  }

  // Update presence
  if (token) {
    if (room.host.token === token) room.host.lastSeen = Date.now();
    else if (room.guest?.token === token) room.guest.lastSeen = Date.now();
  }

  res.json({ success: true, room: serializeRoom(room, token) });
});

app.get('/api/rooms/:code/stream', (req: Request, res: Response) => {
  const code = req.params.code.toUpperCase();
  const token = req.query.token as string | undefined;
  const room = rooms.get(code);

  if (!room) {
    res.status(404).end();
    return;
  }

  res.setHeader('Content-Type', 'text/event-stream');
  res.setHeader('Cache-Control', 'no-cache');
  res.setHeader('Connection', 'keep-alive');
  res.flushHeaders();

  room.listeners.push(res);

  // Send immediate state
  res.write(`data: ${JSON.stringify(serializeRoom(room, token))}\n\n`);

  req.on('close', () => {
    room.listeners = room.listeners.filter((r) => r !== res);
  });
});

app.post('/api/rooms/:code/move', (req: Request, res: Response) => {
  const code = req.params.code.toUpperCase();
  const { token, from, to, promotion = 'q' } = req.body;
  const room = rooms.get(code);

  if (!room) {
    res.status(404).json({ success: false, error: 'Room not found' });
    return;
  }

  if (room.status !== 'active') {
    res.status(400).json({ success: false, error: 'Game is not active' });
    return;
  }

  // Verify player identity and turn
  const isHost = room.host.token === token;
  const isGuest = room.guest?.token === token;
  if (!isHost && !isGuest) {
    res.status(403).json({ success: false, error: 'Invalid player token' });
    return;
  }

  const playerColor = isHost ? room.host.color : room.guest!.color;
  const currentTurn = room.chess.turn();

  if (playerColor !== currentTurn) {
    res.status(400).json({ success: false, error: 'Not your turn' });
    return;
  }

  // Calculate elapsed time & update clock
  const now = Date.now();
  if (room.lastTurnStartTimeMs && room.timeControl.initialSeconds > 0) {
    const elapsed = now - room.lastTurnStartTimeMs;
    if (playerColor === 'w') {
      room.whiteTimeMs = Math.max(0, room.whiteTimeMs - elapsed) + room.timeControl.incrementSeconds * 1000;
      if (room.whiteTimeMs <= 0) {
        room.status = 'timeout';
        room.winner = 'b';
        room.winReason = 'White ran out of time';
        broadcastRoom(room);
        res.json({ success: true, room: serializeRoom(room, token) });
        return;
      }
    } else {
      room.blackTimeMs = Math.max(0, room.blackTimeMs - elapsed) + room.timeControl.incrementSeconds * 1000;
      if (room.blackTimeMs <= 0) {
        room.status = 'timeout';
        room.winner = 'w';
        room.winReason = 'Black ran out of time';
        broadcastRoom(room);
        res.json({ success: true, room: serializeRoom(room, token) });
        return;
      }
    }
  }

  // Attempt move
  try {
    const moveResult = room.chess.move({
      from,
      to,
      promotion: promotion || 'q',
    });

    if (!moveResult) {
      res.status(400).json({ success: false, error: 'Illegal move' });
      return;
    }

    room.history.push(moveResult.san);
    room.fen = room.chess.fen();
    room.lastTurnStartTimeMs = now;
    room.lastActiveAt = now;
    room.drawOfferBy = null; // Clear draw offers on move

    // Check game over
    if (room.chess.isCheckmate()) {
      room.status = 'checkmate';
      room.winner = playerColor;
      room.winReason = `Checkmate! ${playerColor === 'w' ? 'White' : 'Black'} wins`;
    } else if (room.chess.isStalemate()) {
      room.status = 'draw';
      room.winner = 'draw';
      room.winReason = 'Stalemate - Draw';
    } else if (room.chess.isThreefoldRepetition()) {
      room.status = 'draw';
      room.winner = 'draw';
      room.winReason = 'Threefold Repetition - Draw';
    } else if (room.chess.isInsufficientMaterial()) {
      room.status = 'draw';
      room.winner = 'draw';
      room.winReason = 'Insufficient Material - Draw';
    } else if (room.chess.isDraw()) {
      room.status = 'draw';
      room.winner = 'draw';
      room.winReason = 'Draw (50-move rule)';
    }

    broadcastRoom(room);
    res.json({ success: true, room: serializeRoom(room, token) });
  } catch (err: unknown) {
    const errorMsg = err instanceof Error ? err.message : 'Invalid move';
    res.status(400).json({ success: false, error: errorMsg });
  }
});

app.post('/api/rooms/:code/action', (req: Request, res: Response) => {
  const code = req.params.code.toUpperCase();
  const { token, action } = req.body;
  const room = rooms.get(code);

  if (!room) {
    res.status(404).json({ success: false, error: 'Room not found' });
    return;
  }

  const isHost = room.host.token === token;
  const isGuest = room.guest?.token === token;
  if (!isHost && !isGuest) {
    res.status(403).json({ success: false, error: 'Invalid token' });
    return;
  }

  const playerColor = isHost ? room.host.color : room.guest!.color;

  if (action === 'resign') {
    if (room.status === 'active') {
      room.status = 'resigned';
      room.winner = playerColor === 'w' ? 'b' : 'w';
      room.winReason = `${playerColor === 'w' ? 'White' : 'Black'} resigned`;
      broadcastRoom(room);
    }
  } else if (action === 'draw_offer') {
    if (room.status === 'active') {
      room.drawOfferBy = playerColor;
      broadcastRoom(room);
    }
  } else if (action === 'draw_accept') {
    if (room.status === 'active' && room.drawOfferBy && room.drawOfferBy !== playerColor) {
      room.status = 'draw';
      room.winner = 'draw';
      room.winReason = 'Draw agreed by mutual consent';
      room.drawOfferBy = null;
      broadcastRoom(room);
    }
  } else if (action === 'draw_decline') {
    room.drawOfferBy = null;
    broadcastRoom(room);
  } else if (action === 'rematch_offer') {
    room.rematchOfferBy = playerColor;
    broadcastRoom(room);
  } else if (action === 'rematch_accept') {
    if (room.rematchOfferBy && room.rematchOfferBy !== playerColor) {
      // Swap colors for rematch
      const oldHostColor = room.host.color;
      room.host.color = oldHostColor === 'w' ? 'b' : 'w';
      if (room.guest) {
        room.guest.color = room.host.color === 'w' ? 'b' : 'w';
      }
      room.chess = new Chess();
      room.fen = room.chess.fen();
      room.history = [];
      room.status = 'active';
      room.winner = null;
      room.winReason = undefined;
      room.drawOfferBy = null;
      room.rematchOfferBy = null;
      room.whiteTimeMs = room.timeControl.initialSeconds * 1000;
      room.blackTimeMs = room.timeControl.initialSeconds * 1000;
      room.lastTurnStartTimeMs = Date.now();
      broadcastRoom(room);
    }
  }

  res.json({ success: true, room: serializeRoom(room, token) });
});

// Mount Vite or serve static assets
async function startServer() {
  const isProduction = process.env.NODE_ENV === 'production';

  if (!isProduction) {
    const { createServer: createViteServer } = await import('vite');
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: 'spa',
    });
    app.use(vite.middlewares);
  } else {
    app.use(express.static(path.resolve(__dirname, 'dist')));
    app.get('*', (_req, res) => {
      res.sendFile(path.resolve(__dirname, 'dist', 'index.html'));
    });
  }

  app.listen(port, '0.0.0.0', () => {
    console.log(`Royal Chess server listening on port ${port}`);
  });
}

startServer();
