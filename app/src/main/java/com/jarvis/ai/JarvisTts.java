package com.jarvis.ai;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import java.util.Locale;
public class JarvisTts {
    private TextToSpeech tts;
    public JarvisTts(Context c){tts=new TextToSpeech(c,status->{if(status==TextToSpeech.SUCCESS)tts.setLanguage(new Locale("bn","BD"));});}
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
    public void speak(String text){
        if(tts==null)return;
        Locale target=hasBangla(text)?new Locale("bn","BD"):Locale.US;
        int r=tts.setLanguage(target);
        if(r==TextToSpeech.LANG_MISSING_DATA||r==TextToSpeech.LANG_NOT_SUPPORTED){
            tts.setLanguage(new Locale("bn","BD"));
        }
        tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,"jarvis");
    }
    public void shutdown(){if(tts!=null){tts.stop();tts.shutdown();}}
}
