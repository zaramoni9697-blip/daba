package com.jarvis.ai;

import android.app.*;
import android.content.*;
import android.os.*;
import android.speech.*;
import java.util.*;

public class JarvisWakeService extends Service {
    private SpeechRecognizer recognizer;
    private Handler handler;
    private boolean running;
    private boolean awaitingCommand;
    private boolean commandConsumed;
    private boolean listening;
    private long lastListenAt;
    private static volatile boolean active;
    private static final String CH = "jarvis_v1";
    private static final int NOTIFICATION_ID = 1001;

    private JarvisCommandRouter router;
    private JarvisActionExecutor executor;
    private JarvisEngine engine;
    private JarvisTts tts;
    private JarvisFloatingOverlay overlay;

    public static boolean isRunning() { return active; }

    @Override public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        active = true;
        router = new JarvisCommandRouter();
        executor = new JarvisActionExecutor(this);
        engine = new JarvisEngine(this);
        tts = new JarvisTts(this);
        overlay = new JarvisFloatingOverlay(this);
        overlay.show();
        channel();

        Intent open = new Intent(this, MainActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 1, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification n = new Notification.Builder(this, CH)
                .setContentTitle("JARVIS • Background Listener")
                .setContentText("Listening for “Hey JARVIS” • বাংলা / English")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setContentIntent(pi)
                .setOngoing(true)
                .build();
        startForeground(NOTIFICATION_ID, n);
        startRecognizer();
    }

    private void startRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            active = false;
            stopSelf();
            return;
        }
        running = true;
        awaitingCommand = false;
        commandConsumed = false;
        createRecognizer();
        scheduleListen(250);
    }

    private void createRecognizer() {
        if (recognizer != null) {
            try { recognizer.cancel(); } catch (Exception ignored) {}
            try { recognizer.destroy(); } catch (Exception ignored) {}
            recognizer = null;
        }
        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognitionListener() {
            public void onResults(Bundle b) {
                listening = false;
                if (!running || commandConsumed) return;
                ArrayList<String> results = b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                handleResults(results);
                if (!commandConsumed) scheduleListen(awaitingCommand ? 250 : 450);
            }
            public void onPartialResults(Bundle b) {
                // Ignore partial text here. Only final results execute commands.
            }
            public void onError(int e) {
                listening = false;
                if (!running) return;
                commandConsumed = false;
                recreateRecognizerAndListen(awaitingCommand ? 350 : 500);
            }
            public void onReadyForSpeech(Bundle b) {
                listening = true;
                lastListenAt = SystemClock.elapsedRealtime();
            }
            public void onBeginningOfSpeech() { listening = true; }
            public void onRmsChanged(float r) {}
            public void onBufferReceived(byte[] b) {}
            public void onEndOfSpeech() {}
            public void onEvent(int a, Bundle b) {}
        });
    }

    private void recreateRecognizerAndListen(long delay) {
        if (!running) return;
        handler.removeCallbacksAndMessages(null);
        if (recognizer != null) {
            try { recognizer.cancel(); } catch (Exception ignored) {}
            try { recognizer.destroy(); } catch (Exception ignored) {}
            recognizer = null;
        }
        handler.postDelayed(() -> {
            if (!running) return;
            createRecognizer();
            listen();
        }, delay);
    }

    private void scheduleListen(long delay) {
        if (!running) return;
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(() -> {
            if (!running) return;
            createRecognizer();
            listen();
        }, delay);
    }

    private void listen() {
        if (!running || recognizer == null || listening) return;
        commandConsumed = false;
        listening = true;
        lastListenAt = SystemClock.elapsedRealtime();
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, awaitingCommand ? 2500 : 1800);
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, awaitingCommand ? 800 : 1000);
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, awaitingCommand ? 1800 : 1200);

        String lang = SecureStore.get(this, "speech_language");
        if (lang == null || lang.isEmpty() || "AUTO".equals(lang)) {
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD");
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "bn-BD,en-US");
        } else {
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang);
        }

        try {
            recognizer.startListening(i);
        } catch (Exception e) {
            listening = false;
            recreateRecognizerAndListen(1000);
        }
    }

    private void handleResults(ArrayList<String> results) {
        if (results == null || results.isEmpty()) return;

        for (String raw : results) {
            if (raw == null || raw.trim().isEmpty()) continue;
            String s = raw.trim();

            if (!awaitingCommand) {
                String command = commandAfterWake(s);
                if (command != null) {
                    awaitingCommand = true;
                    if (!command.isEmpty()) {
                        commandConsumed = true;
                        runBackgroundCommand(command);
                    } else {
                        speak("হ্যাঁ, বলুন।");
                        scheduleListen(250);
                    }
                    return;
                }
            } else {
                commandConsumed = true;
                runBackgroundCommand(s);
                return;
            }
        }
    }

    private String commandAfterWake(String value) {
        if (value == null) return null;
        String x = value.toLowerCase(Locale.ROOT).trim()
                .replace("হে জার্ভিস", "হে জারভিস")
                .replace("হেই জার্ভিস", "হেই জারভিস")
                .replace("জার্ভিস", "জারভিস");

        String[] wake = {
                "hey jarvis", "hey, jarvis", "hey jarvis,",
                "হেই জারভিস", "হে জারভিস"
        };

        for (String w : wake) {
            int p = x.indexOf(w);
            if (p >= 0) {
                String rest = x.substring(p + w.length()).trim();
                if (rest.startsWith(",") || rest.startsWith(":")) rest = rest.substring(1).trim();
                return rest;
            }
        }
        return null;
    }

    private void runBackgroundCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            awaitingCommand = false;
            commandConsumed = false;
            scheduleListen(350);
            return;
        }

        final String text = command.trim();
        new Thread(() -> {
            JarvisAction action = router.route(text);
            String answer;

            if (action.type == JarvisAction.Type.ASK_AI) {
                answer = engine.ask(text);
            } else {
                answer = executor.execute(action);
            }

            final String reply = answer;
            handler.post(() -> {
                if (reply != null && !reply.trim().isEmpty()
                        && !"false".equals(SecureStore.get(this, "voice_replies"))) {
                    tts.speak(reply);
                }
                awaitingCommand = false;
                commandConsumed = false;
                scheduleListen(700);
            });
        }).start();
    }

    private void speak(String text) {
        if (!"false".equals(SecureStore.get(this, "voice_replies"))) {
            tts.speak(text);
        }
    }

    private void channel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel(
                    CH, "JARVIS Background Listener", NotificationManager.IMPORTANCE_LOW);
            c.setDescription("Keeps JARVIS ready for the Hey JARVIS wake phrase and commands.");
            getSystemService(NotificationManager.class).createNotificationChannel(c);
        }
    }

    @Override public int onStartCommand(Intent i, int flags, int startId) {
        return START_STICKY;
    }

    @Override public void onDestroy() {
        running = false;
        active = false;
        if (handler != null) handler.removeCallbacksAndMessages(null);
        listening = false;
        if (recognizer != null) {
            try { recognizer.destroy(); } catch (Exception ignored) {}
            recognizer = null;
        }
        if (overlay != null) {
            overlay.remove();
            overlay = null;
        }
        if (tts != null) {
            try { tts.shutdown(); } catch (Exception ignored) {}
            tts = null;
        }
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent i) { return null; }
}
