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
import android.Manifest;
import android.database.Cursor;
import java.util.Locale;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.content.ContentValues;
import android.graphics.Bitmap;
import java.io.OutputStream;

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
            case FLASHLIGHT_ON:return flashlight(true);
            case FLASHLIGHT_OFF:return flashlight(false);
            case BRIGHTNESS_UP:return brightness(25);
            case BRIGHTNESS_DOWN:return brightness(-25);
            case SCREENSHOT:return JarvisAccessibilityService.takeScreenshotToGallery(context)?"Screenshot saved.":"Screenshot is unavailable. Enable JARVIS Accessibility Service.";
            case READ_NOTIFICATIONS:return readNotifications();
            case WIFI_SETTINGS:return openSettings("android.settings.WIFI_SETTINGS");
            case BLUETOOTH_SETTINGS:return openSettings("android.settings.BLUETOOTH_SETTINGS");
            case MOBILE_NETWORK_SETTINGS:return openSettings("android.settings.WIRELESS_SETTINGS");
            case AIRPLANE_MODE_SETTINGS:return openSettings("android.settings.AIRPLANE_MODE_SETTINGS");
            case WHATSAPP_MESSAGE:return openMessagingApp("com.whatsapp");
            case MESSENGER_MESSAGE:return openMessagingApp("com.facebook.orca");
            case CALL:return callContactOrNumber(a.value);
            case CLICK_TEXT:return JarvisAccessibilityService.clickText(a.value)?"Clicked "+a.value+".":needAccessibility();
            case TYPE_TEXT:return JarvisAccessibilityService.typeText(a.value)?"Typed it.":needAccessibility();
            case OPEN_APP:return openApp(a.value);
            default:return "Action is handled by the command/AI layer.";
        }
    }
    private String openSettings(String action){ try{Intent i=new Intent(action);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);context.startActivity(i);return "Settings opened.";}catch(Exception e){return "That setting is unavailable.";}}
    private String openMessagingApp(String pkg){try{Intent i=context.getPackageManager().getLaunchIntentForPackage(pkg);if(i==null)return "That messaging app is not installed.";i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);context.startActivity(i);return "Messaging app opened. I can continue with screen automation when the accessibility service is enabled.";}catch(Exception e){return "Could not open messaging app.";}}
    private String flashlight(boolean on){
        if(Build.VERSION.SDK_INT<23)return "Flashlight is not supported on this Android version.";
        try{
            CameraManager cm=(CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
            if(cm==null)return "Camera service unavailable.";
            String id=null; for(String x:cm.getCameraIdList()){android.hardware.camera2.CameraCharacteristics c=cm.getCameraCharacteristics(x);Boolean flash=c.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE);Integer facing=c.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING);if(Boolean.TRUE.equals(flash)){id=x;if(facing==null||facing==android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK)break;}}
            if(id==null)return "No flashlight found.";
            cm.setTorchMode(id,on); return on?"Flashlight on.":"Flashlight off.";
        }catch(Exception e){return "Could not control the flashlight.";} 
    }
    private String brightness(int delta){
        if(!Settings.System.canWrite(context)){try{Intent i=new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,Uri.parse("package:"+context.getPackageName()));i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);context.startActivity(i);return "Please allow JARVIS to modify system settings, then try again.";}catch(Exception e){return "Brightness permission is unavailable.";}}
        try{int cur=Settings.System.getInt(context.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS);int v=Math.max(10,Math.min(255,cur+delta));Settings.System.putInt(context.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,v);return "Brightness adjusted.";}catch(Exception e){return "Could not change brightness.";} 
    }
    private String readNotifications(){
        String n=SecureStore.get(context,"latest_notifications");
        return n==null||n.trim().isEmpty()?"No notification data available. Enable JARVIS Notification Access in Settings.":n;
    }
    private String volume(int direction){
        AudioManager am=(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if(am==null)return "Volume control unavailable.";
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC,direction,AudioManager.FLAG_SHOW_UI);
        return direction==AudioManager.ADJUST_MUTE?"Muted.":direction==AudioManager.ADJUST_RAISE?"Volume increased.":"Volume decreased.";
    }
    private String callContactOrNumber(String raw){
        String q=raw==null?"":raw.trim();
        if(q.isEmpty())return "Please tell me the contact or phone number.";
        String number=q.replaceAll("[^0-9+]", "");
        if(number.length()<6){
            Cursor c=null;
            try{
                c=context.getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" LIKE ?",
                        new String[]{"%"+q+"%"},
                        null);
                if(c!=null&&c.moveToFirst())number=c.getString(0);
            }catch(Exception ignored){} finally{if(c!=null)c.close();}
        }
        if(number==null||number.trim().isEmpty())return "I couldn't find that contact.";
        try{
            Intent i=new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+Uri.encode(number)));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
            return "Calling "+q+".";
        }catch(SecurityException ex){
            return "Phone permission is required to place calls.";
        }catch(Exception ex){
            return "I couldn't start the call.";
        }
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
