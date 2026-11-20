package com.jarvis.ai;
import java.util.Locale;

public class JarvisCommandRouter {
    public JarvisAction route(String raw){
        String s=raw==null?"":raw.toLowerCase(Locale.ROOT).trim();
        if(s.equals("home")||s.contains("go home")||s.contains("হোমে যাও")||s.contains("হোমে নিয়ে যাও"))return action(JarvisAction.Type.HOME,null);
        if(s.equals("back")||s.contains("go back")||s.contains("পিছনে যাও")||s.contains("ব্যাক কর"))return action(JarvisAction.Type.BACK,null);
        if(s.contains("recent apps")||s.contains("recents")||s.contains("রিসেন্ট"))return action(JarvisAction.Type.RECENTS,null);
        if(s.contains("notifications")||s.contains("নোটিফিকেশন"))return action(JarvisAction.Type.NOTIFICATIONS,null);
        if(s.contains("quick settings")||s.contains("কুইক সেটিং"))return action(JarvisAction.Type.QUICK_SETTINGS,null);
        if(s.contains("scroll down")||s.contains("নিচে স্ক্রল"))return action(JarvisAction.Type.SCROLL_DOWN,null);
        if(s.contains("scroll up")||s.contains("উপরে স্ক্রল")||s.contains("উপরে যাও"))return action(JarvisAction.Type.SCROLL_UP,null);
        if(s.contains("volume up")||s.contains("শব্দ বাড়াও")||s.contains("ভলিউম বাড়াও")||s.contains("ভলিউম বাড়িয়ে"))return action(JarvisAction.Type.VOLUME_UP,null);
        if(s.contains("volume down")||s.contains("শব্দ কমাও")||s.contains("ভলিউম কমাও")||s.contains("ভলিউম কমিয়ে"))return action(JarvisAction.Type.VOLUME_DOWN,null);
        if(s.contains("mute")||s.contains("silent")||s.contains("মিউট")||s.contains("সাইলেন্ট"))return action(JarvisAction.Type.MUTE,null);
        if(s.contains("volume down")||s.contains("শব্দ কমাও")||s.contains("ভলিউম কমাও"))return new JarvisAction(JarvisAction.Type.ASK_AI,raw,false);
        if(s.startsWith("call ")||s.startsWith("কল ")){String n=s.replaceFirst("^(call|কল)\\s*","");return new JarvisAction(JarvisAction.Type.CALL,n,true);}
        if(s.startsWith("sms ")||s.startsWith("text ")||s.startsWith("মেসেজ ")){String n=s.replaceFirst("^(sms|text|মেসেজ)\\s*","");return new JarvisAction(JarvisAction.Type.SMS,n,true);}
        if(s.startsWith("click ")||s.startsWith("tap ")||s.startsWith("ক্লিক ")||s.startsWith("ট্যাপ ")){String v=s.replaceFirst("^(click|tap|ক্লিক|ট্যাপ)\\s*","").trim();return action(JarvisAction.Type.CLICK_TEXT,v);}
        if(s.startsWith("type ")||s.startsWith("লিখ ")||s.startsWith("টাইপ ")){String v=s.replaceFirst("^(type|লিখ|টাইপ)\\s*","").trim();return action(JarvisAction.Type.TYPE_TEXT,v);}

        // App names are detected independently of the exact sentence.
        // This handles Bengali/English mixed commands such as:
        // "আমার ফেসবুকটা খুলে দাও", "youtube open কর", "ফেসবুক চালু কর".
        if(has(s,"facebook","ফেসবুক")) return action(JarvisAction.Type.OPEN_APP,"facebook");
        if(has(s,"youtube","ইউটিউব","ইউটিউবটা")) return action(JarvisAction.Type.OPEN_APP,"youtube");
        if(has(s,"messenger","মেসেঞ্জার")) return action(JarvisAction.Type.OPEN_APP,"messenger");
        if(has(s,"telegram","টেলিগ্রাম")) return action(JarvisAction.Type.OPEN_APP,"telegram");
        if(has(s,"whatsapp","হোয়াটসঅ্যাপ","হোয়াটসঅ্যাপ","হোয়াটসঅ্যাপটা")) return action(JarvisAction.Type.OPEN_APP,"whatsapp");
        if(has(s,"settings","সেটিংস")) return action(JarvisAction.Type.OPEN_APP,"settings");
        if(has(s,"instagram","ইনস্টাগ্রাম")) return action(JarvisAction.Type.OPEN_APP,"instagram");
        if(has(s,"chrome","ক্রোম")) return action(JarvisAction.Type.OPEN_APP,"chrome");
        if(has(s,"camera","ক্যামেরা")) return action(JarvisAction.Type.OPEN_APP,"camera");
        if(has(s,"gallery","গ্যালারি")) return action(JarvisAction.Type.OPEN_APP,"gallery");
        if(has(s,"files","file manager","ফাইল")) return action(JarvisAction.Type.OPEN_APP,"files");

        return new JarvisAction(JarvisAction.Type.ASK_AI,raw,false);
    }

    private boolean has(String s,String... names){
        for(String n:names)if(s.contains(n))return true;
        return false;
    }
    private JarvisAction action(JarvisAction.Type type,String value){
        return new JarvisAction(type,value,false);
    }
}
