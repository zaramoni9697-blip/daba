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
    private long lastCommandAt=0;

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
            public void onResults(Bundle b) { check(b); scheduleListen(500); }
            public void onPartialResults(Bundle b) {
                // Only wake phrases are accepted from partial speech.
                // Ordinary commands wait for final results so full sentences are heard.
                ArrayList<String> results = b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (results != null) {
                    for (String s : results) {
                        if (isWakePhrase(s)) {
                            lastCommandAt = System.currentTimeMillis();
                            triggerWake(stripWakePhrase(s));
                            return;
                        }
                    }
                }
            }
            public void onError(int e) {
                // Recreate SpeechRecognizer after background errors; reusing a
                // failed instance can stop outside-app listening after one command.
                resetRecognizer();
                scheduleListen(700);
            }
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
        if(System.currentTimeMillis()-lastCommandAt<2500) return;
        ArrayList<String> results = b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (results == null) return;

        for (String s : results) {
            if (isWakePhrase(s)) {
                lastCommandAt=System.currentTimeMillis();
                String command=stripWakePhrase(s);
                triggerWake(command);
                return;
            }
            // Final results only: direct phone commands work while another app
            // is in the foreground, without requiring JARVIS UI to be visible.
            if(isDirectCommand(s)){
                lastCommandAt=System.currentTimeMillis();
                triggerWake(s);
                return;
            }
        }
    }

    private void resetRecognizer() {
        if (recognizer != null) {
            try { recognizer.cancel(); } catch (Exception ignored) {}
            try { recognizer.destroy(); } catch (Exception ignored) {}
            recognizer = null;
        }
        if (!running || !SpeechRecognizer.isRecognitionAvailable(this)) return;
        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognitionListener() {
            public void onResults(Bundle b) { check(b); scheduleListen(500); }
            public void onPartialResults(Bundle b) {
                ArrayList<String> results = b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (results != null) {
                    for (String s : results) {
                        if (isWakePhrase(s)) {
                            lastCommandAt = System.currentTimeMillis();
                            triggerWake(stripWakePhrase(s));
                            return;
                        }
                    }
                }
            }
            public void onError(int e) { resetRecognizer(); scheduleListen(700); }
            public void onReadyForSpeech(Bundle b) {}
            public void onBeginningOfSpeech() {}
            public void onRmsChanged(float r) {}
            public void onBufferReceived(byte[] b) {}
            public void onEndOfSpeech() {}
            public void onEvent(int a, Bundle b) {}
        });
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

    private boolean isDirectCommand(String value){
        if(value==null)return false;
        String x=value.toLowerCase(Locale.ROOT).trim();
        return x.contains("খুলো") || x.contains("খোল") || x.contains("open ")
                || x.contains("চালু") || x.contains("বন্ধ") || x.contains("ফেসবুক")
                || x.contains("ইউটিউব") || x.contains("মেসেঞ্জার") || x.contains("হোয়াটসঅ্যাপ")
                || x.contains("whatsapp") || x.contains("facebook") || x.contains("youtube")
                || x.contains("volume") || x.contains("ভলিউম") || x.contains("flashlight")
                || x.contains("টর্চ") || x.contains("screenshot") || x.contains("স্ক্রিনশট")
                || x.contains("back") || x.contains("home") || x.contains("ফিরে যাও");
    }

    private String stripWakePhrase(String value){
        String x=value==null?"":value.trim();
        x=x.replaceAll("(?i)hey[, ]*jarvis","").trim();
        x=x.replace("হেই জারভিস","").replace("হে জারভিস","").replace("হেই জার্ভিস","").replace("হে জার্ভিস","").trim();
        return x;
    }

    private void triggerWake(String command) {
        handler.removeCallbacksAndMessages(null);
        if (recognizer != null) {
            try { recognizer.cancel(); } catch (Exception ignored) {}
            try { recognizer.destroy(); } catch (Exception ignored) {}
            recognizer = null;
        }
        Intent in = new Intent(this, MainActivity.class);
        in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        in.putExtra("wake", true);
        if(command!=null && !command.trim().isEmpty()) in.putExtra("wake_command",command.trim());
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
        if(!running) startRecognizer();
        return START_STICKY;
    }

    @Override public void onTaskRemoved(Intent rootIntent) {
        if(!"false".equals(SecureStore.get(this,"wake_enabled"))){
            try {
                Intent restart=new Intent(this,JarvisWakeService.class);
                if(Build.VERSION.SDK_INT>=26) startForegroundService(restart);
                else startService(restart);
            } catch(Exception ignored) {}
        }
        super.onTaskRemoved(rootIntent);
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