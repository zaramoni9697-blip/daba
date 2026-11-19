package com.jarvis.ai;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.os.*;
import android.speech.*;
import android.view.View;
import android.widget.*;
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

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        status=findViewById(R.id.status);
        transcript=findViewById(R.id.transcript);
        subtitle=findViewById(R.id.subtitle);
        engine=new JarvisEngine(this);
        tts=new JarvisTts(this);
        executor=new JarvisActionExecutor(this);

        // Voice is now hands-free while JARVIS is open.
        View micButton=findViewById(R.id.micButton);
        if(micButton!=null)micButton.setVisibility(View.GONE);

        findViewById(R.id.brain).setOnClickListener(v->toggleWake());
        findViewById(R.id.settings).setOnClickListener(v->settings());
        findViewById(R.id.history).setOnClickListener(v->showHistory());

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
            showSpeechRecognitionUnavailable();
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
                    status.setVisibility(View.VISIBLE);
                    status.setText("Voice error: "+speechErrorName(e)+" ("+e+")");
                    handler.postDelayed(MainActivity.this::listen,900);
                }
            }
            public void onReadyForSpeech(Bundle b){status.setText("Listening…");}
            public void onBeginningOfSpeech(){}
            public void onRmsChanged(float r){}
            public void onBufferReceived(byte[] b){}
            public void onEndOfSpeech(){}
            public void onPartialResults(Bundle b){}
            public void onEvent(int a, Bundle b){}
        });
        Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,false);
        i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
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

    private void showSpeechRecognitionUnavailable(){
        status.setVisibility(View.VISIBLE);
        status.setText("Speech recognition unavailable");
        subtitle.setText("ফোনে Speech Recognition service পাওয়া যাচ্ছে না");
        new AlertDialog.Builder(this)
                .setTitle("Voice Recognition unavailable")
                .setMessage("Android-এর Speech Recognition service পাওয়া যাচ্ছে না।\n\nSettings খুলে Voice Input / Speech Services চালু বা ইনস্টল করুন।")
                .setNegativeButton("Later", null)
                .setPositiveButton("Open Settings", (d,w)->{
                    try{
                        startActivity(new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS));
                    }catch(Exception e){
                        try{ startActivity(new Intent(Settings.ACTION_SETTINGS)); }catch(Exception ignored){}
                    }
                }).show();
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
        if(a.type==JarvisAction.Type.OPEN_APP){
            externalAppLaunch=true;
            String result=executor.execute(a);
            status.setVisibility(View.VISIBLE);
            status.setText(result);
            subtitle.setText(result);
            if(!"false".equals(SecureStore.get(this,"voice_replies")))tts.speak(result);
            return;
        }
        if(a.type==JarvisAction.Type.ASK_AI){
            status.setVisibility(View.VISIBLE);
            status.setText("Thinking…");
            new Thread(()->{
                String answer=engine.ask(a.value);
                runOnUiThread(()->{
                    status.setText("Ready");
                    subtitle.setText(answer);
                    if(!"false".equals(SecureStore.get(this,"voice_replies")))tts.speak(answer);
                    restartListening(900);
                });
            }).start();
            return;
        }
        String result;
        if(a.type==JarvisAction.Type.HOME||a.type==JarvisAction.Type.BACK||a.type==JarvisAction.Type.RECENTS
                ||a.type==JarvisAction.Type.NOTIFICATIONS||a.type==JarvisAction.Type.QUICK_SETTINGS
                ||a.type==JarvisAction.Type.SCROLL_UP||a.type==JarvisAction.Type.SCROLL_DOWN)
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

    private void showHistory(){
        String last=transcript.getText().toString();
        new AlertDialog.Builder(this).setTitle("JARVIS History")
                .setMessage(last==null||last.isEmpty()?"No recent command yet.":last)
                .setPositiveButton("OK",null).show();
    }

    private void settings(){startActivity(new Intent(this,SettingsActivity.class));}
    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);stopInAppRecognizer();if(tts!=null)tts.shutdown();super.onDestroy();}
}
