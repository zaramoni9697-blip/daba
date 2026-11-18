package com.jarvis.ai;
import android.content.Context;
import android.content.Intent;
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
        String pkg=packageFor(name); if(pkg==null)return "I don't know that app yet.";
        Intent i=context.getPackageManager().getLaunchIntentForPackage(pkg);
        if(i==null)return "That app is not installed.";
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); context.startActivity(i); return "Opening "+name+".";
    }
    private String packageFor(String name){
        String s=name.toLowerCase();
        if(s.contains("youtube")||s.contains("ইউটিউব"))return "com.google.android.youtube";
        if(s.contains("facebook")||s.contains("ফেসবুক"))return "com.facebook.katana";
        if(s.contains("messenger"))return "com.facebook.orca";
        if(s.contains("whatsapp")||s.contains("হোয়াটসঅ্যাপ"))return "com.whatsapp";
        if(s.contains("telegram"))return "org.telegram.messenger";
        if(s.contains("settings"))return "com.android.settings";
        return null;
    }
}
