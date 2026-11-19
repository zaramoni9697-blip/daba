package com.jarvis.ai;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;

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
            case OPEN_APP:return openApp(a.value);
            default:return "Action is handled by the command/AI layer.";
        }
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
