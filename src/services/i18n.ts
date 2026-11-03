export type Language = 'en' | 'bn';

export interface Translations {
  appTitle: string;
  appSubtitle: string;
  playVsComputer: string;
  playVsComputerDesc: string;
  playLocal: string;
  playLocalDesc: string;
  playOnline: string;
  playOnlineDesc: string;
  gameHistory: string;
  settings: string;
  profile: string;
  white: string;
  black: string;
  random: string;
  chooseSide: string;
  difficulty: string;
  easy: string;
  medium: string;
  hard: string;
  expert: string;
  timeControl: string;
  noTimer: string;
  bullet1: string;
  bullet2: string;
  blitz3: string;
  blitz3_2: string;
  blitz5: string;
  rapid10: string;
  rapid15_10: string;
  classical20: string;
  classical30: string;
  customTimer: string;
  startGame: string;
  cancel: string;
  turn: string;
  whiteTurn: string;
  blackTurn: string;
  check: string;
  checkmate: string;
  stalemate: string;
  draw: string;
  drawInsufficient: string;
  drawRepetition: string;
  draw50Moves: string;
  timeout: string;
  resigned: string;
  resign: string;
  resignConfirm: string;
  offerDraw: string;
  drawOffered: string;
  acceptDraw: string;
  declineDraw: string;
  undo: string;
  restart: string;
  flipBoard: string;
  pgn: string;
  fen: string;
  exportPgn: string;
  importPgn: string;
  copied: string;
  copy: string;
  pasteFen: string;
  roomCode: string;
  createRoom: string;
  joinRoom: string;
  enterRoomCode: string;
  shareRoom: string;
  shareLinkCopied: string;
  waitingForOpponent: string;
  opponentConnected: string;
  opponentDisconnected: string;
  connected: string;
  disconnected: string;
  spectator: string;
  rematch: string;
  rematchOffered: string;
  backToMenu: string;
  sound: string;
  soundOn: string;
  soundMuted: string;
  boardTheme: string;
  pieceStyle: string;
  showCoordinates: string;
  autoFlip: string;
  language: string;
  english: string;
  bangla: string;
  stats: string;
  wins: string;
  losses: string;
  draws: string;
  totalGames: string;
  winRate: string;
  installApp: string;
  offlineReady: string;
  onlineActive: string;
  downloadProjectZip: string;
  githubDeployGuide: string;
}

export const translations: Record<Language, Translations> = {
  en: {
    appTitle: 'Royal Chess',
    appSubtitle: 'Master the Royal Game · Offline & Online',
    playVsComputer: 'Play vs Computer',
    playVsComputerDesc: 'Challenge our tournament-calibrated AI engine across 4 difficulty levels.',
    playLocal: 'Two Player (Local)',
    playLocalDesc: 'Pass and play with a friend on the same screen with auto-flip options.',
    playOnline: 'Play with Friends',
    playOnlineDesc: 'Instant private rooms, real-time sync, and live clocks.',
    gameHistory: 'Game History',
    settings: 'Settings',
    profile: 'User Profile',
    white: 'White',
    black: 'Black',
    random: 'Random',
    chooseSide: 'Choose Your Side',
    difficulty: 'AI Difficulty',
    easy: 'Easy (~800)',
    medium: 'Medium (~1400)',
    hard: 'Hard (~1900)',
    expert: 'Expert (~2300+)',
    timeControl: 'Time Control',
    noTimer: 'Casual (No Timer)',
    bullet1: 'Bullet 1+0',
    bullet2: 'Bullet 2+1',
    blitz3: 'Blitz 3+0',
    blitz3_2: 'Blitz 3+2',
    blitz5: 'Blitz 5+3 (Active)',
    rapid10: 'Rapid 10+0 (Standard)',
    rapid15_10: 'Rapid 15+10 (Relaxed)',
    classical20: 'Classical 20+0 (Deep Strategy)',
    classical30: 'Classical 30+0 (Tournament)',
    customTimer: 'Custom',
    startGame: 'Start Game',
    cancel: 'Cancel',
    turn: "'s turn",
    whiteTurn: "White's turn",
    blackTurn: "Black's turn",
    check: 'CHECK!',
    checkmate: 'CHECKMATE!',
    stalemate: 'STALEMATE (Draw)',
    draw: 'DRAW',
    drawInsufficient: 'Draw by Insufficient Material',
    drawRepetition: 'Draw by Threefold Repetition',
    draw50Moves: 'Draw by 50-Move Rule',
    timeout: 'Time Out!',
    resigned: 'Resigned',
    resign: 'Resign',
    resignConfirm: 'Are you sure you want to resign this game?',
    offerDraw: 'Offer Draw',
    drawOffered: 'Opponent offered a draw. Accept?',
    acceptDraw: 'Accept',
    declineDraw: 'Decline',
    undo: 'Undo',
    restart: 'New Game',
    flipBoard: 'Flip Board',
    pgn: 'PGN Notation',
    fen: 'FEN Position',
    exportPgn: 'Export PGN',
    importPgn: 'Import PGN / FEN',
    copied: 'Copied to clipboard!',
    copy: 'Copy',
    pasteFen: 'Paste FEN or PGN string...',
    roomCode: 'Room Code',
    createRoom: 'Create Private Room',
    joinRoom: 'Join Room',
    enterRoomCode: 'Enter 6-character code',
    shareRoom: 'Share Room Code',
    shareLinkCopied: 'Room link copied to clipboard!',
    waitingForOpponent: 'Waiting for friend to join...',
    opponentConnected: 'Opponent Connected',
    opponentDisconnected: 'Opponent Disconnected',
    connected: 'Online',
    disconnected: 'Offline',
    spectator: 'Spectating',
    rematch: 'Rematch',
    rematchOffered: 'Opponent requested a rematch. Accept?',
    backToMenu: 'Main Menu',
    sound: 'Sound Effects',
    soundOn: 'Audio Enabled',
    soundMuted: 'Audio Muted',
    boardTheme: 'Board Theme',
    pieceStyle: 'Piece Style',
    showCoordinates: 'Show Board Coordinates',
    autoFlip: 'Auto-Flip on Turn (Local 2P)',
    language: 'Language / ভাষা',
    english: 'English',
    bangla: 'বাংলা (Bengali)',
    stats: 'Your Statistics',
    wins: 'Wins',
    losses: 'Losses',
    draws: 'Draws',
    totalGames: 'Total Games Played',
    winRate: 'Win Rate',
    installApp: 'Install Royal Chess App',
    offlineReady: 'Offline Ready (PWA Active)',
    onlineActive: 'Online Server Connected',
    downloadProjectZip: 'Download Standalone ZIP for GitHub Pages',
    githubDeployGuide: 'Android Deployment Guide',
  },
  bn: {
    appTitle: 'রয়্যাল চেস',
    appSubtitle: 'রাজকীয় দাবা খেলা · অফলাইন ও অনলাইন',
    playVsComputer: 'কম্পিউটারের সাথে খেলুন',
    playVsComputerDesc: '৪টি দক্ষতার স্তরে আমাদের শক্তিশালী এআই ইঞ্জিনের সাথে খেলুন।',
    playLocal: 'দুইজন খেলোয়াড় (একই ফোন)',
    playLocalDesc: 'একই স্ক্রিনে বন্ধুর সাথে খেলুন, প্রতি চালে বোর্ড ঘোরার সুবিধা সহ।',
    playOnline: 'বন্ধুর সাথে খেলুন (অনলাইন)',
    playOnlineDesc: 'প্রাইভেট রুম কোড দিয়ে লাইভ ক্লক ও রিয়েল-টাইমে খেলুন।',
    gameHistory: 'খেলার ইতিহাস',
    settings: 'সেটিংস',
    profile: 'ইউজার প্রোফাইল',
    white: 'সাদা',
    black: 'কালো',
    random: 'যেকোনো একটি',
    chooseSide: 'আপনার গুটির রঙ বাছুন',
    difficulty: 'এআই দক্ষতা স্তর',
    easy: 'সহজ (~৮০০)',
    medium: 'মাঝারি (~১৪০০)',
    hard: 'কঠিন (~১৯০০)',
    expert: 'বিশেষজ্ঞ (~২৩০০+)',
    timeControl: 'টাইমার বা সময় নিয়ন্ত্রণ',
    noTimer: 'ক্যাজুয়াল (সময় ছাড়া)',
    bullet1: 'বুলেট ১+০',
    bullet2: 'বুলেট ২+১',
    blitz3: 'ব্লিটজ ৩+০',
    blitz3_2: 'ব্লিটজ ৩+২',
    blitz5: 'ব্লিটজ ৫+৩ (অ্যাক্টিভ)',
    rapid10: 'র‌্যাপিড ১০ মিনিট (স্ট্যান্ডার্ড)',
    rapid15_10: 'র‌্যাপিড ১৫ মিনিট + ১০ সে.',
    classical20: 'ক্লাসিক্যাল ২০ মিনিট (কৌশলগত)',
    classical30: 'ক্লাসিক্যাল ৩০ মিনিট (টুর্নামেন্ট)',
    customTimer: 'কাস্টম',
    startGame: 'খেলা শুরু করুন',
    cancel: 'বাতিল',
    turn: ' এর চাল',
    whiteTurn: 'সাদার চাল',
    blackTurn: 'কালোর চাল',
    check: 'চেক!',
    checkmate: 'চেকমেট!',
    stalemate: 'স্টেলমেট (ড্র)',
    draw: 'ড্র',
    drawInsufficient: 'অপর্যাপ্ত গুটির কারণে ড্র',
    drawRepetition: 'তিনবার একই চাল পুনরাবৃত্তিতে ড্র',
    draw50Moves: '৫০ চালের নিয়মে ড্র',
    timeout: 'সময় শেষ!',
    resigned: 'পদত্যাগ করেছেন',
    resign: 'হার স্বীকার / পদত্যাগ',
    resignConfirm: 'আপনি কি নিশ্চিত যে পদত্যাগ করতে চান?',
    offerDraw: 'ড্র প্রস্তাব করুন',
    drawOffered: 'প্রতিপক্ষ ড্র প্রস্তাব করেছেন। মানবেন?',
    acceptDraw: 'সম্মত',
    declineDraw: 'প্রত্যাখ্যান',
    undo: 'চাল ফেরত নিন',
    restart: 'নতুন ম্যাচ',
    flipBoard: 'বোর্ড ঘোরান',
    pgn: 'পিজিএন নোটেশন',
    fen: 'এফইএন অবস্থান',
    exportPgn: 'পিজিএন সংরক্ষণ',
    importPgn: 'পিজিএন বা এফইএন ইমপোর্ট',
    copied: 'ক্লিপবোর্ডে কপি হয়েছে!',
    copy: 'কপি',
    pasteFen: 'এফইএন বা পিজিএন টেক্সট দিন...',
    roomCode: 'রুম কোড',
    createRoom: 'প্রাইভেট রুম বানান',
    joinRoom: 'রুমে যোগ দিন',
    enterRoomCode: '৬ অক্ষরের কোড দিন',
    shareRoom: 'রুম লিংক শেয়ার করুন',
    shareLinkCopied: 'রুম লিংক কপি হয়েছে!',
    waitingForOpponent: 'বন্ধুর যোগদানের অপেক্ষায়...',
    opponentConnected: 'প্রতিপক্ষ যুক্ত হয়েছে',
    opponentDisconnected: 'প্রতিপক্ষ সংযোগ বিচ্ছিন্ন',
    connected: 'অনলাইন',
    disconnected: 'অফলাইন',
    spectator: 'দর্শক',
    rematch: 'পুনরায় খেলুন',
    rematchOffered: 'প্রতিপক্ষ রি-ম্যাচ চেয়েছে। খেলবেন?',
    backToMenu: 'মেনু',
    sound: 'সাউন্ড এফেক্ট',
    soundOn: 'সাউন্ড চালু',
    soundMuted: 'সাউন্ড বন্ধ',
    boardTheme: 'বোর্ডের থিম',
    pieceStyle: 'গুটির ডিজাইন',
    showCoordinates: 'বোর্ডের স্থানাঙ্ক দেখান (A-H, 1-8)',
    autoFlip: 'চালে বোর্ড স্বয়ংক্রিয় ঘোরান (লোকাল ২পি)',
    language: 'ভাষা / Language',
    english: 'English',
    bangla: 'বাংলা',
    stats: 'আপনার পরিসংখ্যান',
    wins: 'জয়',
    losses: 'পরাজয়',
    draws: 'ড্র',
    totalGames: 'মোট ম্যাচ',
    winRate: 'জয়ের হার',
    installApp: 'রয়্যাল চেস অ্যাপ ইনস্টল করুন',
    offlineReady: 'অফলাইন প্রস্তুত (PWA সক্রিয়)',
    onlineActive: 'অনলাইন সার্ভার সক্রিয়',
    downloadProjectZip: 'গিটহাব পেজে ব্যবহারের জিপ ডাউনলোড করুন',
    githubDeployGuide: 'অ্যান্ড্রয়েড ফোন থেকে গিটহাবে তোলার নির্দেশিকা',
  },
};
