package com.jarvis.ai;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.os.Bundle;
import java.util.List;
import android.os.Build;
import android.view.Display;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.HardwareBuffer;
import android.media.Image;
import android.os.Environment;
import android.provider.MediaStore;
import android.content.ContentValues;
import java.io.OutputStream;

public class JarvisAccessibilityService extends AccessibilityService {
    private static JarvisAccessibilityService instance;
    public static boolean isEnabled(){return instance!=null;}
    @Override protected void onServiceConnected(){instance=this;}
    @Override public void onAccessibilityEvent(AccessibilityEvent event){}
    @Override public void onInterrupt(){}
    public static boolean home(){return instance!=null&&instance.performGlobalAction(GLOBAL_ACTION_HOME);}
    public static boolean back(){return instance!=null&&instance.performGlobalAction(GLOBAL_ACTION_BACK);}
    public static boolean recents(){return instance!=null&&instance.performGlobalAction(GLOBAL_ACTION_RECENTS);}
    public static boolean notifications(){return instance!=null&&instance.performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS);}
    public static boolean quickSettings(){return instance!=null&&instance.performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS);}
    public static boolean clickText(String text){
        if(instance==null||text==null||text.trim().isEmpty())return false;
        List<AccessibilityNodeInfo> nodes=instance.getRootInActiveWindow().findAccessibilityNodeInfosByText(text);
        if(nodes==null)return false;
        for(AccessibilityNodeInfo n:nodes){
            AccessibilityNodeInfo cur=n;
            while(cur!=null){
                if(cur.isClickable() && cur.performAction(AccessibilityNodeInfo.ACTION_CLICK))return true;
                cur=cur.getParent();
            }
        }
        return false;
    }
    public static boolean typeText(String text){
        if(instance==null||text==null)return false;
        AccessibilityNodeInfo root=instance.getRootInActiveWindow();
        if(root==null)return false;
        AccessibilityNodeInfo focused=root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
        if(focused==null||!focused.isEditable())return false;
        Bundle args=new Bundle();args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,text);
        return focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,args);
    }
    public static boolean takeScreenshotToGallery(final android.content.Context context){
        if(instance==null||Build.VERSION.SDK_INT<30)return false;
        try{
            instance.takeScreenshot(Display.DEFAULT_DISPLAY, context.getMainExecutor(), new TakeScreenshotCallback(){
                @Override public void onSuccess(ScreenshotResult result){
                    try{
                        HardwareBuffer hb=result.getHardwareBuffer(); Bitmap bm=Bitmap.wrapHardwareBuffer(hb,result.getColorSpace());
                        if(bm==null){hb.close();return;}
                        ContentValues v=new ContentValues();v.put(MediaStore.Images.Media.DISPLAY_NAME,"JARVIS_"+System.currentTimeMillis()+".png");v.put(MediaStore.Images.Media.MIME_TYPE,"image/png");v.put(MediaStore.Images.Media.RELATIVE_PATH,Environment.DIRECTORY_PICTURES+"/JARVIS");
                        android.net.Uri uri=context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v);
                        if(uri!=null){OutputStream os=context.getContentResolver().openOutputStream(uri);bm.compress(Bitmap.CompressFormat.PNG,100,os);if(os!=null)os.close();}
                        bm.recycle();hb.close();
                    }catch(Exception ignored){}
                }
                @Override public void onFailure(int errorCode){}
            }); return true;
        }catch(Exception e){return false;}
    }
    public static boolean scroll(boolean down){
        if(instance==null)return false; Path p=new Path();
        if(down){p.moveTo(540,1200);p.lineTo(540,350);}else{p.moveTo(540,350);p.lineTo(540,1200);}
        GestureDescription g=new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(p,0,500)).build();
        return instance.dispatchGesture(g,null,null);
    }
    @Override public void onDestroy(){instance=null;super.onDestroy();}
}
