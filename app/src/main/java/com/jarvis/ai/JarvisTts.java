package com.jarvis.ai;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import java.util.Locale;

public class JarvisTts {
    private TextToSpeech tts;
    private boolean ready=false;
    private String pending=null;

    public JarvisTts(Context c){
        tts=new TextToSpeech(c.getApplicationContext(),status->{
            if(status==TextToSpeech.SUCCESS){
                ready=true;
                setBestLanguage();
                if(pending!=null){
                    String p=pending;
                    pending=null;
                    speak(p);
                }
            }
        });
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener(){
            public void onStart(String id){}
            public void onDone(String id){}
            public void onError(String id){}
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
        return bn>0 && bn>=latin;
    }

    private void setBestLanguage(){
        if(tts==null)return;
        Locale target=new Locale("bn","BD");
        int r=tts.setLanguage(target);
        if(r==TextToSpeech.LANG_MISSING_DATA||r==TextToSpeech.LANG_NOT_SUPPORTED)
            tts.setLanguage(Locale.US);
    }

    public void speak(String text){
        if(text==null||text.trim().isEmpty())return;
        if(tts==null)return;
        if(!ready){
            pending=text;
            return;
        }
        Locale target=hasBangla(text)?new Locale("bn","BD"):Locale.US;
        int r=tts.setLanguage(target);
        if(r==TextToSpeech.LANG_MISSING_DATA||r==TextToSpeech.LANG_NOT_SUPPORTED){
            target=Locale.US;
            tts.setLanguage(target);
        }
        tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,"jarvis");
    }

    public void shutdown(){
        if(tts!=null){
            tts.stop();
            tts.shutdown();
        }
    }
}
