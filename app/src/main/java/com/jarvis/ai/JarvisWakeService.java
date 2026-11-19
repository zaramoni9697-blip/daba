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
    private static volatile boolean active;
    private static final String CH = "jarvis_v1";
    private static final int NOTIFICATION_ID = 1001;

    public static boolean isRunning() { return active; }

    @Override public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        active = true;
        channel();

        Intent open = new Intent(this, MainActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 1, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification n = new Notification.Builder(this, CH)
                .setContentTitle("JARVIS • Wake Listener")
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
        createRecognizer();
        scheduleListen(250);
    }

    private void createRecognizer() {
        if (recognizer != null) {
            try { recognizer.destroy(); } catch (Exception ignored) {}
        }
        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognitionListener() {
            public void onResults(Bundle b) { check(b); scheduleListen(300); }
            public void onPartialResults(Bundle b) { check(b); }
            public void onError(int e) { scheduleListen(700); }
            public void onReadyForSpeech(Bundle b) {}
            public void onBeginningOfSpeech() {}
            public void onRmsChanged(float r) {}
            public void onBufferReceived(byte[] b) {}
            public void onEndOfSpeech() {}
            public void onEvent(int a, Bundle b) {}
        });
    }

    private void scheduleListen(long delay) {
        if (!running) return;
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(this::listen, delay);
    }

    private void listen() {
        if (!running || recognizer == null) return;
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        String lang = SecureStore.get(this, "speech_language");

        // Wake phrase must work in both Bengali and English.
        // Bengali is primary for normal commands, but English wake words are accepted too.
        if (lang == null || lang.isEmpty() || "AUTO".equals(lang)) {
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "bn-BD,en-US");
        } else {
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang);
        }

        try {
            recognizer.startListening(i);
        } catch (Exception e) {
            scheduleListen(1200);
        }
    }

    private void check(Bundle b) {
        ArrayList<String> results = b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (results == null) return;

        for (String s : results) {
            if (isWakePhrase(s)) {
                triggerWake();
                return;
            }
        }
    }

    private boolean isWakePhrase(String value) {
        if (value == null) return false;
        String x = value.toLowerCase(Locale.ROOT).trim()
                .replace("হে জার্ভিস", "হে জারভিস")
                .replace("হেই জার্ভিস", "হেই জারভিস")
                .replace("জার্ভিস", "জারভিস");

        return x.contains("hey jarvis")
                || x.contains("hey, jarvis")
                || x.contains("হেই জারভিস")
                || x.contains("হে জারভিস")
                || x.contains("হে জার্ভিস")
                || x.contains("হেই জার্ভিস");
    }

    private void triggerWake() {
        handler.removeCallbacksAndMessages(null);
        Intent in = new Intent(this, MainActivity.class);
        in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        in.putExtra("wake", true);
        startActivity(in);
        scheduleListen(1800);
    }

    private void channel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel(
                    CH, "JARVIS Wake Listener", NotificationManager.IMPORTANCE_LOW);
            c.setDescription("Keeps JARVIS ready for the Hey JARVIS wake phrase.");
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
        if (recognizer != null) {
            try { recognizer.destroy(); } catch (Exception ignored) {}
            recognizer = null;
        }
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent i) { return null; }
}