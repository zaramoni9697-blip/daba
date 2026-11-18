package com.jarvis.ai;
import java.util.Locale;

public class JarvisCommandRouter {
    public JarvisAction route(String raw){
        String s=raw.toLowerCase(Locale.ROOT).trim();
        if(s.equals("home")||s.contains("go home")||s.contains("হোমে যাও"))return new JarvisAction(JarvisAction.Type.HOME,null,false);
        if(s.equals("back")||s.contains("go back")||s.contains("পিছনে যাও"))return new JarvisAction(JarvisAction.Type.BACK,null,false);
        if(s.contains("recent apps")||s.contains("recents")||s.contains("রিসেন্ট"))return new JarvisAction(JarvisAction.Type.RECENTS,null,false);
        if(s.contains("notifications")||s.contains("নোটিফিকেশন"))return new JarvisAction(JarvisAction.Type.NOTIFICATIONS,null,false);
        if(s.contains("quick settings")||s.contains("কুইক সেটিং"))return new JarvisAction(JarvisAction.Type.QUICK_SETTINGS,null,false);
        if(s.contains("scroll down")||s.contains("নিচে স্ক্রল"))return new JarvisAction(JarvisAction.Type.SCROLL_DOWN,null,false);
        if(s.contains("scroll up")||s.contains("উপরে স্ক্রল"))return new JarvisAction(JarvisAction.Type.SCROLL_UP,null,false);
        if(s.startsWith("call ")||s.startsWith("কল ")){String n=s.replaceFirst("^(call|কল)\s*","");return new JarvisAction(JarvisAction.Type.CALL,n,true);}
        if(s.startsWith("sms ")||s.startsWith("text ")||s.startsWith("মেসেজ ")){String n=s.replaceFirst("^(sms|text|মেসেজ)\s*","");return new JarvisAction(JarvisAction.Type.SMS,n,true);}
        String[] apps={"youtube","facebook","messenger","telegram","whatsapp","settings","ইউটিউব","ফেসবুক","হোয়াটসঅ্যাপ"};
        for(String app:apps)if(s.contains("open "+app)||s.contains("খুলো "+app)||s.contains("চালু "+app))return new JarvisAction(JarvisAction.Type.OPEN_APP,app,false);
        return new JarvisAction(JarvisAction.Type.ASK_AI,raw,false);
    }
}
