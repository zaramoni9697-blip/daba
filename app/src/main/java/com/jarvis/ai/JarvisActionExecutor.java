package com.jarvis.ai;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.provider.Settings;
import android.net.Uri;
import android.content.Intent;
import android.provider.Settings;
import android.provider.ContactsContract;
import android.database.Cursor;
import java.util.Locale;

public class JarvisActionExecutor {
    private final Context context;
    public JarvisActionExecutor(Context c){context=c.getApplicationContext();}
    public String execute(JarvisAction a){
        switch(a.type){
            case HOME:return JarvisAccessibilityService.home()?"Home opened.":needAccessibility();
            case BACK:return JarvisAccessibilityService.back()?"Back completed.":needAccessibility();
            case RECENTS:return JarvisAccessibilityService.recents()?"Recent apps opened.":needAccessibility();
            case NOTIFICATIONS:return JarvisAccessibilityService.notifications()?"Notifications opened.":needAccessibility();
            case QUICK_SETTINGS:return JarvisAccessibilityService.quickSettings()?"Quick settings opened.":needAccessibility();
            case SCROLL_DOWN:return JarvisAccessibilityService.scroll(true)?"Scrolled down.":needAccessibility();
            case SCROLL_UP:return JarvisAccessibilityService.scroll(false)?"Scrolled up.":needAccessibility();
            case VOLUME_UP:return volume(AudioManager.ADJUST_RAISE);
            case VOLUME_DOWN:return volume(AudioManager.ADJUST_LOWER);
            case MUTE:return volume(AudioManager.ADJUST_MUTE);
            case CLICK_TEXT:return JarvisAccessibilityService.clickText(a.value)?"Clicked "+a.value+".":needAccessibility();
            case TYPE_TEXT:return JarvisAccessibilityService.typeText(a.value)?"Typed it.":needAccessibility();
            case OPEN_APP:return openApp(a.value);
            default:return "Action is handled by the command/AI layer.";
        }
    }
    private String volume(int direction){
        AudioManager am=(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if(am==null)return "Volume control unavailable.";
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC,direction,AudioManager.FLAG_SHOW_UI);
        return direction==AudioManager.ADJUST_MUTE?"Muted.":direction==AudioManager.ADJUST_RAISE?"Volume increased.":"Volume decreased.";
    }
    private String sendSms(String raw){
        String text=raw==null?"" : raw.trim();
        Intent i=new Intent(Intent.ACTION_SENDTO);
        i.setData(Uri.parse("smsto:"));
        i.putExtra("sms_body",text);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try{context.startActivity(i);return "SMS composer opened. Please review and send.";}catch(Exception e){return "SMS app unavailable.";}
    }
    private String needAccessibility(){
        context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        return "Please enable JARVIS Accessibility Service, then try again.";
    }
    private String openApp(String name){
        String pkg=packageFor(name);
        if(pkg==null)return "I don't know that app yet.";
        Intent i=context.getPackageManager().getLaunchIntentForPackage(pkg);
        if(i==null)return "That app is not installed.";
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        try{
            context.startActivity(i);
            return "Opening "+name+".";
        }catch(Exception e){
            return "I couldn't open "+name+".";
        }
    }
    private String packageFor(String name){
        String s=name.toLowerCase(Locale.ROOT);
        if(s.contains("youtube")||s.contains("ইউটিউব"))return installed("com.google.android.youtube");
        if(s.contains("facebook")||s.contains("ফেসবুক")){
            String p=installed("com.facebook.katana");
            if(p!=null)return p;
            return installed("com.facebook.lite");
        }
        if(s.contains("messenger"))return installed("com.facebook.orca");
        if(s.contains("whatsapp")||s.contains("হোয়াটসঅ্যাপ")||s.contains("হোয়াটসঅ্যাপ"))return installed("com.whatsapp");
        if(s.contains("telegram"))return installed("org.telegram.messenger");
        if(s.contains("instagram"))return installed("com.instagram.android");
        if(s.contains("chrome"))return installed("com.android.chrome");
        if(s.contains("camera"))return installed("com.android.camera");
        if(s.contains("gallery")){String p=installed("com.google.android.apps.photos");if(p!=null)return p;return installed("com.android.gallery3d");}
        if(s.contains("files")){String p=installed("com.google.android.documentsui");if(p!=null)return p;return installed("com.google.android.apps.nbu.files");}
        if(s.contains("settings")||s.contains("সেটিংস"))return installed("com.android.settings");
        return null;
    }
    private String installed(String... packages){
        PackageManager pm=context.getPackageManager();
        for(String p:packages)try{
            if(pm.getLaunchIntentForPackage(p)!=null)return p;
        }catch(Exception ignored){}
        return null;
    }
}
