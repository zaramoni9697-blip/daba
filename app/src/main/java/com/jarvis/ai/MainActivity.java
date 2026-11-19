package com.jarvis.ai;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.provider.Settings;
import android.speech.*;
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

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        status=findViewById(R.id.status);
        transcript=findViewById(R.id.transcript);
        subtitle=findViewById(R.id.subtitle);
        engine=new JarvisEngine(this);
        tts=new JarvisTts(this);
        executor=new JarvisActionExecutor(this);

        findViewById(R.id.micButton).setOnClickListener(v->listen());
        findViewById(R.id.brain).setOnClickListener(v->toggleWake());
        findViewById(R.id.settings).setOnClickListener(v->settings());
        findViewById(R.id.history).setOnClickListener(v->showHistory());

        if(getIntent().getBooleanExtra("wake",false))
            new Handler().postDelayed(this::listen,700);

        // Once the user grants microphone permission, keep the wake listener available
        // outside the app. The user can disable it with the brain button.
        if(mic() && "false".equals(SecureStore.get(this,"wake_enabled")) == false) {
            new Handler().postDelayed(this::ensureWakeService,500);
        }
    }

    @Override protected void onResume(){
        super.onResume();
        if(mic() && "false".equals(SecureStore.get(this,"wake_enabled")) == false)
            new Handler().postDelayed(this::ensureWakeService,300);
    }

    private void ensureWakeService(){
        if(!mic() || JarvisWakeService.isRunning()) return;
        try {
            ContextCompat.startForegroundService(this,new Intent(this,JarvisWakeService.class));
        } catch(Exception ignored) {}
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
        status.setVisibility(TextView.VISIBLE);
        if(JarvisWakeService.isRunning()){
            stopService(new Intent(this,JarvisWakeService.class));
            SecureStore.put(this,"wake_enabled","false");
            status.setText("Wake listener stopped");
            subtitle.setText("Tap the microphone to speak");
        }else{
            SecureStore.put(this,"wake_enabled","true");
            ContextCompat.startForegroundService(this,new Intent(this,JarvisWakeService.class));
            status.setText("Wake listener active");
            subtitle.setText("Listening for “Hey Jarvis” • বাংলা / English");
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] results){
        super.onRequestPermissionsResult(requestCode,permissions,results);
        if(requestCode==MIC && mic()){
            SecureStore.put(this,"wake_enabled","true");
            ensureWakeService();
        }
    }

    private void listen(){
        if(!mic()){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},MIC);
            return;
        }
        Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,false);
        String lang=SecureStore.get(this,"speech_language");
        if(lang==null||lang.isEmpty()||lang.equals("AUTO")){
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"bn-BD");
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"bn-BD,en-US");
        }else i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,lang);
        startActivityForResult(i,SPEECH);
        status.setVisibility(TextView.VISIBLE);
        status.setText("Listening…");
        subtitle.setText("বাংলা / English — আমি শুনছি");
    }

    @Override protected void onActivityResult(int r,int c,Intent d){
        super.onActivityResult(r,c,d);
        if(r!=SPEECH||d==null)return;
        ArrayList<String>a=d.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if(a==null||a.isEmpty())return;
        String cmd=a.get(0);
        transcript.setText(cmd);
        transcript.setVisibility(TextView.VISIBLE);
        execute(cmd);
    }

    private void execute(String cmd){
        cmd=normalizeSpeech(cmd);
        JarvisAction a=router.route(cmd);
        if(a.confirmation)
            new AlertDialog.Builder(this).setTitle("JARVIS Confirmation")
                    .setMessage("এই কাজটি করতে অনুমতি দেবেন?\n"+cmd)
                    .setNegativeButton("Cancel",null)
                    .setPositiveButton("Confirm",(d,w)->run(a)).show();
        else run(a);
    }

    private void run(JarvisAction a){
        if(a.type==JarvisAction.Type.ASK_AI){
            status.setVisibility(TextView.VISIBLE);
            status.setText("Thinking…");
            new Thread(()->{
                String answer=engine.ask(a.value);
                runOnUiThread(()->{
                    status.setText("Ready");
                    subtitle.setText(answer);
                    if(!"false".equals(SecureStore.get(this,"voice_replies")))tts.speak(answer);
                });
            }).start();
            return;
        }
        String result;
        if(a.type==JarvisAction.Type.HOME||a.type==JarvisAction.Type.BACK||a.type==JarvisAction.Type.RECENTS
                ||a.type==JarvisAction.Type.NOTIFICATIONS||a.type==JarvisAction.Type.QUICK_SETTINGS
                ||a.type==JarvisAction.Type.SCROLL_UP||a.type==JarvisAction.Type.SCROLL_DOWN
                ||a.type==JarvisAction.Type.OPEN_APP)
            result=executor.execute(a);
        else if(a.type==JarvisAction.Type.CALL)
            result="Call confirmation accepted. Phone-number/contact resolution can be added next.";
        else if(a.type==JarvisAction.Type.SMS)
            result="SMS confirmation accepted. Message/contact composition can be added next.";
        else result="Done.";
        status.setVisibility(TextView.VISIBLE);
        status.setText(result);
        subtitle.setText(result);
        if(!"false".equals(SecureStore.get(this,"voice_replies")))tts.speak(result);
    }

    private String normalizeSpeech(String s){
        if(s==null)return "";
        return s.replace("হেই জারভিস","").replace("হে জারভিস","").trim();
    }

    private void showHistory(){
        String last=transcript.getText().toString();
        new AlertDialog.Builder(this).setTitle("JARVIS History")
                .setMessage(last==null||last.isEmpty()?"No recent command yet.":last)
                .setPositiveButton("OK",null).show();
    }

    private void settings(){startActivity(new Intent(this,SettingsActivity.class));}
    @Override protected void onDestroy(){if(tts!=null)tts.shutdown();super.onDestroy();}
}