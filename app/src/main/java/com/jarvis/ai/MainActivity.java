package com.jarvis.ai;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.os.*;
import android.speech.*;
import android.view.View;
import android.view.Gravity;
import android.widget.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import org.json.JSONArray;
import org.json.JSONObject;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private static final int MIC=50,SPEECH=60;
    private TextView status,transcript,subtitle;
    private JarvisEngine engine;
    private JarvisTts tts;
    private JarvisActionExecutor executor;
    private final JarvisCommandRouter router=new JarvisCommandRouter();
    private final Handler handler=new Handler(Looper.getMainLooper());
    private boolean continuousVoice=true;
    private boolean externalAppLaunch=false;
    private boolean recognitionInProgress=false;
    private SpeechRecognizer recognizer;
    private boolean activityVisible=false;
    private boolean cloudVoiceInProgress=false;
    private FrameLayout mainRoot;
    private LinearLayout historyList;
    private View drawerOverlay;
    private View historyDrawer;

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        status=findViewById(R.id.status);
        transcript=findViewById(R.id.transcript);
        subtitle=findViewById(R.id.subtitle);
        mainRoot=findViewById(R.id.mainRoot);
        engine=new JarvisEngine(this);
        tts=new JarvisTts(this);
        executor=new JarvisActionExecutor(this);

        // Voice is now hands-free while JARVIS is open.
        View micButton=findViewById(R.id.micButton);
        if(micButton!=null)micButton.setVisibility(View.GONE);

        findViewById(R.id.brain).setOnClickListener(v->toggleWake());
        findViewById(R.id.settings).setOnClickListener(v->settings());
        findViewById(R.id.history).setOnClickListener(v->openHistoryDrawer());
        buildHistoryDrawer();

        continuousVoice=true;
        if(!mic()){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},MIC);
        }else{
            handler.postDelayed(this::listen,500);
        }

        if(getIntent().getBooleanExtra("wake",false))
            handler.postDelayed(this::listen,700);
    }

    @Override protected void onResume(){
        super.onResume();
        activityVisible=true;
        // Never allow the background wake recognizer to compete with the
        // in-app recognizer while JARVIS is visible.
        handler.removeCallbacksAndMessages(null);
        if(JarvisWakeService.isRunning()){
            try { stopService(new Intent(this,JarvisWakeService.class)); } catch(Exception ignored) {}
        }
        externalAppLaunch=false;
        if(continuousVoice && mic())
            handler.postDelayed(this::listen,700);
    }

    @Override protected void onPause(){
        super.onPause();
        activityVisible=false;
        if(isFinishing()) return;
        handler.removeCallbacksAndMessages(null);
        stopInAppRecognizer();
        if(mic() && !"false".equals(SecureStore.get(this,"wake_enabled")))
            handler.postDelayed(this::ensureWakeService,700);
    }

    private void stopInAppRecognizer(){
        recognitionInProgress=false;
        if(recognizer!=null){
            try { recognizer.cancel(); } catch(Exception ignored) {}
            try { recognizer.destroy(); } catch(Exception ignored) {}
            recognizer=null;
        }
    }

    private void ensureWakeService(){
        if(!mic() || JarvisWakeService.isRunning()) return;
        try { ContextCompat.startForegroundService(this,new Intent(this,JarvisWakeService.class)); }
        catch(Exception ignored) {}
    }

    private boolean mic(){
        return ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)
                ==PackageManager.PERMISSION_GRANTED;
    }

    private void toggleWake(){
        if(!mic()){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},MIC);
            return;
        }
        status.setVisibility(View.VISIBLE);
        if(JarvisWakeService.isRunning()){
            stopService(new Intent(this,JarvisWakeService.class));
            SecureStore.put(this,"wake_enabled","false");
            status.setText("Wake listener stopped");
            subtitle.setText("Hands-free voice inside JARVIS remains available");
        }else{
            SecureStore.put(this,"wake_enabled","true");
            ContextCompat.startForegroundService(this,new Intent(this,JarvisWakeService.class));
            status.setText("Wake listener active");
            subtitle.setText("Hands-free mode • বাংলা / English");
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] results){
        super.onRequestPermissionsResult(requestCode,permissions,results);
        if(requestCode==MIC && mic()){
            SecureStore.put(this,"wake_enabled","true");
            ensureWakeService();
            handler.postDelayed(this::listen,400);
        }
    }

    private void listen(){
        if(!activityVisible || !continuousVoice || recognitionInProgress || !mic()) return;
        recognitionInProgress=true;
        if(!SpeechRecognizer.isRecognitionAvailable(this)){
            recognitionInProgress=false;
            listenWithCloudStt();
            return;
        }
        if(recognizer!=null){
            try { recognizer.destroy(); } catch(Exception ignored) {}
        }
        recognizer=SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognitionListener(){
            public void onResults(Bundle b){
                recognitionInProgress=false;
                ArrayList<String> a=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if(a!=null&&!a.isEmpty()){
                    String cmd=a.get(0);
                    transcript.setText(cmd);
                    transcript.setVisibility(View.VISIBLE);
                    execute(cmd);
                }else restartListening(400);
            }
            public void onError(int e){
                recognitionInProgress=false;
                if(activityVisible && continuousVoice){
                    // Give very quiet speech a longer retry. If Groq is configured,
                    // use it silently as a fallback for low-volume recognition.
                    if(e==SpeechRecognizer.ERROR_NO_MATCH || e==SpeechRecognizer.ERROR_SPEECH_TIMEOUT){
                        String key=SecureStore.get(MainActivity.this,"groq");
                        if(key!=null && !key.trim().isEmpty()){
                            handler.postDelayed(MainActivity.this::listenWithCloudStt,250);
                            return;
                        }
                    }
                    status.setVisibility(View.VISIBLE);
                    status.setText("Listening…");
                    handler.postDelayed(MainActivity.this::listen,350);
                }
            }
            public void onReadyForSpeech(Bundle b){status.setText("Listening…");}
            public void onBeginningOfSpeech(){}
            public void onRmsChanged(float r){}
            public void onBufferReceived(byte[] b){}
            public void onEndOfSpeech(){}
            public void onPartialResults(Bundle b){
                ArrayList<String> p=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if(p!=null&&!p.isEmpty()&&p.get(0)!=null&&!p.get(0).trim().isEmpty()){
                    transcript.setText(p.get(0));
                    transcript.setVisibility(View.VISIBLE);
                }
            }
            public void onEvent(int a, Bundle b){}
        });
        Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,true);
        i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,3200);
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,2500);
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,2200);
        String lang=SecureStore.get(this,"speech_language");
        if(lang==null||lang.isEmpty()||lang.equals("AUTO")){
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"bn-BD");
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"bn-BD,en-US");
        }else i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,lang);
        try { recognizer.startListening(i); }
        catch(Exception e){
            recognitionInProgress=false;
            handler.postDelayed(this::listen,1200);
            return;
        }
        status.setVisibility(View.VISIBLE);
        status.setText("Listening…");
        subtitle.setText("বলুন — আমি শুনছি • বাংলা / English");
    }

    private void listenWithCloudStt(){
        if(!activityVisible || cloudVoiceInProgress || !mic()) return;
        String key=SecureStore.get(this,"groq");
        if(key==null || key.trim().isEmpty()){
            showSpeechRecognitionUnavailable();
            return;
        }
        cloudVoiceInProgress=true;
        status.setVisibility(View.VISIBLE);
        status.setText("Listening…");
        subtitle.setText("Cloud voice mode • বলুন — আমি শুনছি");
        new Thread(()->{
            final String[] result=new String[1];
            result[0]=JarvisCloudStt.transcribe(this,key.trim());
            runOnUiThread(()->{
                cloudVoiceInProgress=false;
                if(result[0]!=null && !result[0].trim().isEmpty()){
                    transcript.setText(result[0]);
                    transcript.setVisibility(View.VISIBLE);
                    execute(result[0]);
                }else{
                    status.setText("Voice input failed");
                    transcript.setText("কথা ঠিকমতো শনাক্ত করা যায়নি");
                    transcript.setVisibility(View.VISIBLE);
                    subtitle.setText("কথা বোঝা যায়নি — আবার চেষ্টা করছি");
                    handler.postDelayed(this::listen,1000);
                }
            });
        }).start();
    }

    private void showSpeechRecognitionUnavailable(){
        status.setVisibility(View.VISIBLE);
        status.setText("Voice service unavailable");
        subtitle.setText("Groq API key দিন অথবা ফোনের Speech Services চালু করুন");
        // Do not show a blocking dialog here. The old dialog caused a popup
        // every time the continuous listener retried.
        handler.postDelayed(this::listen,2500);
    }

    private void openVoiceSettings(){
        try{
            startActivity(new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS));
        }catch(Exception e){
            try{
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            }catch(Exception ignored){}
        }
    }

    private void openSpeechServiceStore(){
        try{
            Intent i=new Intent(Intent.ACTION_VIEW);
            i.setData(android.net.Uri.parse("market://details?id=com.google.android.tts"));
            startActivity(i);
        }catch(Exception e){
            try{
                Intent i=new Intent(Intent.ACTION_VIEW);
                i.setData(android.net.Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.tts"));
                startActivity(i);
            }catch(Exception ignored){}
        }
    }

    @Override public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        // When the user returns from Voice Settings / Play Store, retry after
        // Android has had time to register the recognition service.
        if(hasFocus && activityVisible && continuousVoice && mic())
            handler.postDelayed(this::listen,900);
    }

    private String speechErrorName(int e){
        switch(e){
            case SpeechRecognizer.ERROR_AUDIO: return "AUDIO";
            case SpeechRecognizer.ERROR_CLIENT: return "CLIENT";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: return "PERMISSION";
            case SpeechRecognizer.ERROR_NETWORK: return "NETWORK";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: return "NETWORK_TIMEOUT";
            case SpeechRecognizer.ERROR_NO_MATCH: return "NO_MATCH";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: return "RECOGNIZER_BUSY";
            case SpeechRecognizer.ERROR_SERVER: return "SERVER";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: return "SPEECH_TIMEOUT";
            case SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED: return "LANGUAGE_NOT_SUPPORTED";
            case SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE: return "LANGUAGE_UNAVAILABLE";
            default: return "UNKNOWN";
        }
    }

    private void execute(String cmd){
        cmd=normalizeSpeech(cmd);
        JarvisAction a=router.route(cmd);
        if(a.confirmation)
            new AlertDialog.Builder(this).setTitle("JARVIS Confirmation")
                    .setMessage("এই কাজটি করতে অনুমতি দেবেন?\n"+cmd)
                    .setNegativeButton("Cancel",(d,w)->restartListening())
                    .setPositiveButton("Confirm",(d,w)->run(a)).show();
        else run(a);
    }

    private void run(JarvisAction a){
        if(a.type==JarvisAction.Type.CALL && ContextCompat.checkSelfPermission(this,Manifest.permission.CALL_PHONE)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CALL_PHONE},61);
            status.setText("Call permission required");
            return;
        }
        if(a.type==JarvisAction.Type.OPEN_APP){
            externalAppLaunch=true;
            String result=executor.execute(a);
            status.setVisibility(View.VISIBLE);
            status.setText(result);
            subtitle.setText(result);
            if(!"false".equals(SecureStore.get(this,"voice_replies")))tts.speak(result);
            JarvisHistory.add(this, transcript.getText().toString(), result);
            return;
        }
        if(a.type==JarvisAction.Type.ASK_AI){
            status.setVisibility(View.VISIBLE);
            status.setText("Thinking…");
            new Thread(()->{
                String answer=engine.ask(a.value);
                final String finalAnswer=(answer==null || answer.trim().isEmpty())?"দুঃখিত, এখন উত্তর তৈরি করতে পারছি না।":answer;
                runOnUiThread(()->{
                    status.setText("Ready");
                    subtitle.setText(finalAnswer);
                    if(!"false".equals(SecureStore.get(this,"voice_replies")))tts.speak(finalAnswer);
                    JarvisHistory.add(this, transcript.getText().toString(), finalAnswer);
                    restartListening(900);
                });
            }).start();
            return;
        }
        String result;
        if(a.type==JarvisAction.Type.HOME||a.type==JarvisAction.Type.BACK||a.type==JarvisAction.Type.RECENTS
                ||a.type==JarvisAction.Type.NOTIFICATIONS||a.type==JarvisAction.Type.QUICK_SETTINGS
                ||a.type==JarvisAction.Type.SCROLL_UP||a.type==JarvisAction.Type.SCROLL_DOWN
                ||a.type==JarvisAction.Type.VOLUME_UP||a.type==JarvisAction.Type.VOLUME_DOWN||a.type==JarvisAction.Type.MUTE||a.type==JarvisAction.Type.FLASHLIGHT_ON||a.type==JarvisAction.Type.FLASHLIGHT_OFF
                ||a.type==JarvisAction.Type.BRIGHTNESS_UP||a.type==JarvisAction.Type.BRIGHTNESS_DOWN||a.type==JarvisAction.Type.SCREENSHOT||a.type==JarvisAction.Type.READ_NOTIFICATIONS
                ||a.type==JarvisAction.Type.WIFI_SETTINGS||a.type==JarvisAction.Type.BLUETOOTH_SETTINGS||a.type==JarvisAction.Type.MOBILE_NETWORK_SETTINGS||a.type==JarvisAction.Type.AIRPLANE_MODE_SETTINGS
                ||a.type==JarvisAction.Type.WHATSAPP_MESSAGE||a.type==JarvisAction.Type.MESSENGER_MESSAGE
                ||a.type==JarvisAction.Type.CLICK_TEXT||a.type==JarvisAction.Type.TYPE_TEXT)
            result=executor.execute(a);
        else if(a.type==JarvisAction.Type.CALL)
            result="Call confirmation accepted. Phone-number/contact resolution can be added next.";
        else if(a.type==JarvisAction.Type.SMS)
            result="SMS confirmation accepted. Message/contact composition can be added next.";
        else result="Done.";
        status.setVisibility(View.VISIBLE);
        status.setText(result);
        subtitle.setText(result);
        if(!"false".equals(SecureStore.get(this,"voice_replies")))tts.speak(result);
        JarvisHistory.add(this, transcript.getText().toString(), result);
        restartListening(700);
    }

    private void restartListening(){restartListening(500);}
    private void restartListening(long delay){
        if(continuousVoice && !externalAppLaunch)
            handler.postDelayed(this::listen,delay);
    }

    private String normalizeSpeech(String s){
        if(s==null)return "";
        return s.replace("হেই জারভিস","").replace("হে জারভিস","").replace("hey jarvis","").trim();
    }

    private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
    private GradientDrawable drawerBg(int color,float radius){
        GradientDrawable g=new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        return g;
    }

    private TextView drawerText(String text,float size,int color){
        TextView t=new TextView(this);
        t.setText(text); t.setTextSize(size); t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);
        return t;
    }

    private void buildHistoryDrawer(){
        if(mainRoot==null)return;
        FrameLayout overlay=new FrameLayout(this);
        overlay.setBackgroundColor(Color.argb(150,0,0,0));
        overlay.setVisibility(View.GONE);
        overlay.setClickable(true);
        mainRoot.addView(overlay,new FrameLayout.LayoutParams(-1,-1));
        drawerOverlay=overlay;
        overlay.setOnClickListener(v->closeHistoryDrawer());

        LinearLayout drawer=new LinearLayout(this);
        drawer.setOrientation(LinearLayout.VERTICAL);
        drawer.setPadding(dp(18),dp(18),dp(12),dp(16));
        drawer.setBackgroundColor(Color.rgb(11,10,15));
        drawer.setElevation(dp(18));

        LinearLayout header=new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView logo=drawerText("JARVIS",24,Color.WHITE);logo.setTypeface(null,1);
        header.addView(logo,new LinearLayout.LayoutParams(0,dp(58),1));
        TextView close=drawerText("×",32,Color.WHITE);close.setGravity(Gravity.CENTER);
        header.addView(close,new LinearLayout.LayoutParams(dp(48),dp(58)));
        drawer.addView(header);
        close.setOnClickListener(v->closeHistoryDrawer());

        Button newChat=new Button(this);
        newChat.setText("＋  New chat");
        newChat.setTextSize(14); newChat.setAllCaps(false); newChat.setTextColor(Color.WHITE);
        newChat.setGravity(Gravity.CENTER_VERTICAL);
        newChat.setBackground(drawerBg(Color.rgb(28,27,38),18));
        drawer.addView(newChat,new LinearLayout.LayoutParams(-1,dp(50)));
        newChat.setOnClickListener(v->{transcript.setText("");transcript.setVisibility(View.GONE);subtitle.setText("বলুন — আমি শুনছি • বাংলা / English");closeHistoryDrawer();});

        Button textChat=new Button(this);
        textChat.setText("▣  Text Chat");
        textChat.setTextSize(14); textChat.setAllCaps(false); textChat.setTextColor(Color.BLACK);
        textChat.setGravity(Gravity.CENTER_VERTICAL);
        textChat.setBackground(drawerBg(Color.rgb(0,225,255),18));
        LinearLayout.LayoutParams tcp=new LinearLayout.LayoutParams(-1,dp(50));
        tcp.setMargins(0,dp(9),0,dp(10));
        drawer.addView(textChat,tcp);
        textChat.setOnClickListener(v->{closeHistoryDrawer();startActivity(new Intent(this,TextChatActivity.class));});

        TextView section=drawerText("RECENTS",12,Color.rgb(145,140,158));section.setTypeface(null,1);
        section.setPadding(0,dp(8),0,dp(6));
        drawer.addView(section);

        ScrollView scroll=new ScrollView(this);
        historyList=new LinearLayout(this);historyList.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(historyList);
        drawer.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        TextView clear=drawerText("⌫  Clear history",13,Color.rgb(190,185,200));
        clear.setGravity(Gravity.CENTER_VERTICAL);
        clear.setPadding(dp(10),dp(12),dp(10),dp(12));
        drawer.addView(clear,new LinearLayout.LayoutParams(-1,dp(48)));
        clear.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Clear history?")
                .setMessage("সব JARVIS chat history মুছে যাবে।")
                .setNegativeButton("Cancel",null)
                .setPositiveButton("Clear",(d,w)->{JarvisHistory.clear(this);refreshHistoryDrawer();}).show());

        int width=Math.min(dp(330),(int)(getResources().getDisplayMetrics().widthPixels*0.86f));
        FrameLayout.LayoutParams drawerParams=new FrameLayout.LayoutParams(width,-1,Gravity.START);
        mainRoot.addView(drawer,drawerParams);
        historyDrawer=drawer;
        drawer.setTranslationX(-width);
        drawer.setVisibility(View.GONE);
    }

    private void refreshHistoryDrawer(){
        if(historyList==null)return;
        historyList.removeAllViews();
        JSONArray arr=JarvisHistory.read(this);
        if(arr.length()==0){
            TextView empty=drawerText("No conversations yet.\nStart talking to JARVIS.",13,Color.rgb(130,125,142));
            empty.setPadding(dp(8),dp(18),dp(8),dp(18));
            historyList.addView(empty);
            return;
        }
        for(int i=0;i<arr.length();i++){
            try{
                JSONObject o=arr.getJSONObject(i);
                String user=o.optString("user","JARVIS chat");
                String answer=o.optString("assistant","");
                String time=o.optString("time","");
                LinearLayout item=new LinearLayout(this);
                item.setOrientation(LinearLayout.VERTICAL);
                item.setPadding(dp(10),dp(9),dp(8),dp(9));
                item.setBackground(drawerBg(Color.rgb(15,14,20),14));
                TextView title=drawerText(user,14,Color.rgb(235,232,240));
                title.setMaxLines(2);title.setEllipsize(android.text.TextUtils.TruncateAt.END);
                item.addView(title);
                TextView meta=drawerText(time,10,Color.rgb(120,115,132));
                item.addView(meta);
                LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(66));
                p.setMargins(0,dp(3),0,dp(3));historyList.addView(item,p);
                final String fu=user, fa=answer;
                item.setOnClickListener(v->new AlertDialog.Builder(this).setTitle(fu).setMessage(fa.isEmpty()?"No response saved.":fa).setPositiveButton("OK",null).show());
            }catch(Exception ignored){}
        }
    }

    private void openHistoryDrawer(){
        if(historyDrawer==null)return;
        refreshHistoryDrawer();
        historyDrawer.setVisibility(View.VISIBLE);
        drawerOverlay.setVisibility(View.VISIBLE);
        historyDrawer.animate().translationX(0).setDuration(220).start();
    }

    private void closeHistoryDrawer(){
        if(historyDrawer==null)return;
        int width=historyDrawer.getLayoutParams().width;
        historyDrawer.animate().translationX(-width).setDuration(180).withEndAction(()->{
            historyDrawer.setVisibility(View.GONE);
            if(drawerOverlay!=null)drawerOverlay.setVisibility(View.GONE);
        }).start();
    }

    private void settings(){startActivity(new Intent(this,SettingsActivity.class));}
    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);stopInAppRecognizer();if(tts!=null)tts.shutdown();super.onDestroy();}
}
