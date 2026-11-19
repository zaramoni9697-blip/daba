package com.jarvis.ai;
import java.util.Locale;

public class JarvisCommandRouter {
    public JarvisAction route(String raw){
        String s=raw.toLowerCase(Locale.ROOT).trim();
        if(s.equals("home")||s.contains("go home")||s.contains("হোমে যাও")||s.contains("হোমে নিয়ে যাও"))return new JarvisAction(JarvisAction.Type.HOME,null,false);
        if(s.equals("back")||s.contains("go back")||s.contains("পিছনে যাও")||s.contains("ব্যাক কর"))return new JarvisAction(JarvisAction.Type.BACK,null,false);
        if(s.contains("recent apps")||s.contains("recents")||s.contains("রিসেন্ট"))return new JarvisAction(JarvisAction.Type.RECENTS,null,false);
        if(s.contains("notifications")||s.contains("নোটিফিকেশন"))return new JarvisAction(JarvisAction.Type.NOTIFICATIONS,null,false);
        if(s.contains("quick settings")||s.contains("কুইক সেটিং"))return new JarvisAction(JarvisAction.Type.QUICK_SETTINGS,null,false);
        if(s.contains("scroll down")||s.contains("নিচে স্ক্রল")||s.contains("নিচে স্ক্রল কর"))return new JarvisAction(JarvisAction.Type.SCROLL_DOWN,null,false);
        if(s.contains("scroll up")||s.contains("উপরে স্ক্রল")||s.contains("উপরে স্ক্রল কর"))return new JarvisAction(JarvisAction.Type.SCROLL_UP,null,false);
        if(s.startsWith("call ")||s.startsWith("কল ")){String n=s.replaceFirst("^(call|কল)\\s*","");return new JarvisAction(JarvisAction.Type.CALL,n,true);}
        if(s.startsWith("sms ")||s.startsWith("text ")||s.startsWith("মেসেজ ")){String n=s.replaceFirst("^(sms|text|মেসেজ)\\s*","");return new JarvisAction(JarvisAction.Type.SMS,n,true);}

        String[][] apps={
            {"youtube","com.google.android.youtube"},
            {"facebook","com.facebook.katana","com.facebook.lite"},
            {"messenger","com.facebook.orca"},
            {"telegram","org.telegram.messenger"},
            {"whatsapp","com.whatsapp"},
            {"settings","com.android.settings"},
            {"ইউটিউব","com.google.android.youtube"},
            {"ফেসবুক","com.facebook.katana","com.facebook.lite"},
            {"মেসেঞ্জার","com.facebook.orca"},
            {"টেলিগ্রাম","org.telegram.messenger"},
            {"হোয়াটসঅ্যাপ","com.whatsapp"},
            {"হোয়াটসঅ্যাপ","com.whatsapp"},
            {"সেটিংস","com.android.settings"}
        };
        String[] openWords={"open","open কর","open করে","ওপেন","খুলো","খুলে দাও","খুলে দিন","চালু","চালু কর","অন কর","অন করে দাও","আমার"};
        for(String[] app:apps){
            if(s.contains(app[0])){
                boolean explicit=false;
                for(String w:openWords)if(s.contains(w)){explicit=true;break;}
                if(explicit)return new JarvisAction(JarvisAction.Type.OPEN_APP,app[0],false);
            }
        }
        // Common natural commands: "open facebook", "facebook খুলে দাও", "facebook চালু কর".
        if((s.contains("facebook")||s.contains("ফেসবুক")) &&
                (s.contains("খুল")||s.contains("চালু")||s.contains("open")||s.contains("start")))
            return new JarvisAction(JarvisAction.Type.OPEN_APP,"facebook",false);
        return new JarvisAction(JarvisAction.Type.ASK_AI,raw,false);
    }
}
