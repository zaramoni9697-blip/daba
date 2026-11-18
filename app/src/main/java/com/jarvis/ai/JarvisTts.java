package com.jarvis.ai;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import java.util.Locale;
public class JarvisTts {
    private TextToSpeech tts;
    public JarvisTts(Context c){tts=new TextToSpeech(c,status->{if(status==TextToSpeech.SUCCESS)tts.setLanguage(new Locale("bn","BD"));});}
    public void speak(String text){if(tts!=null)tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,"jarvis");}
    public void shutdown(){if(tts!=null){tts.stop();tts.shutdown();}}
}
