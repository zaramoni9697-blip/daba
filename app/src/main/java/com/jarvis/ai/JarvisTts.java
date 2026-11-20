package com.jarvis.ai;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;\nimport android.media.AudioAttributes;
import android.widget.Toast;
import java.util.Locale;

public class JarvisTts {
    private final Context context;
    private TextToSpeech tts;
    private boolean ready=false;
    private String pending=null;

    public JarvisTts(Context c){
        context=c.getApplicationContext();
        init();
    }

    private void init(){
        tts=new TextToSpeech(context,status->{
            if(status==TextToSpeech.SUCCESS){
                ready=true;
                tts.setSpeechRate(0.95f);
                tts.setPitch(1.0f);\n                try{\n                    tts.setAudioAttributes(new AudioAttributes.Builder()\n                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)\n                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)\n                            .build());\n                }catch(Exception ignored){}
                if(pending!=null){
                    String p=pending;
                    pending=null;
                    speak(p);
                }
            }else{
                ready=false;
                Toast.makeText(context,"JARVIS voice engine unavailable. Enable Text-to-Speech.",Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean hasBangla(String text){
        if(text==null)return false;
        int bn=0,latin=0;
        for(int i=0;i<text.length();i++){
            char ch=text.charAt(i);
            if(ch>='\u0980'&&ch<='\u09FF')bn++;
            else if((ch>='A'&&ch<='Z')||(ch>='a'&&ch<='z'))latin++;
        }
        return bn>0&&bn>=latin;
    }

    public void speak(String text){
        if(text==null||text.trim().isEmpty())return;
        if(tts==null||!ready){
            pending=text;
            return;
        }
        Locale target=hasBangla(text)?new Locale("bn","BD"):Locale.US;
        int r=tts.setLanguage(target);
        if(r==TextToSpeech.LANG_MISSING_DATA||r==TextToSpeech.LANG_NOT_SUPPORTED){
            if(hasBangla(text)){
                try{
                    Intent i=new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                }catch(Exception ignored){}
            }
            tts.setLanguage(Locale.US);
        }
        int result=tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,"jarvis_"+System.currentTimeMillis());
        if(result==TextToSpeech.ERROR){
            tts.stop();
            tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,"jarvis_retry_"+System.currentTimeMillis());
        }
    }

    public void openVoiceSettings(){
        try{
            Intent i=new Intent("com.android.settings.TTS_SETTINGS");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }catch(Exception ignored){}
    }

    public boolean isReady(){return ready;}

    public void shutdown(){
        if(tts!=null){
            tts.stop();
            tts.shutdown();
            tts=null;
            ready=false;
        }
    }
}