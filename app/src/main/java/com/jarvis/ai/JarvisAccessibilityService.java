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

    public static boolean clickTextContains(String text){
        if(instance==null||text==null||text.trim().isEmpty())return false;
        AccessibilityNodeInfo root=instance.getRootInActiveWindow();
        if(root==null)return false;
        java.util.List<AccessibilityNodeInfo> all=new java.util.ArrayList<>();
        collectNodes(root,all);
        String q=text.toLowerCase(java.util.Locale.ROOT).trim();
        for(AccessibilityNodeInfo n:all){
            CharSequence t=n.getText();
            CharSequence d=n.getContentDescription();
            String ts=t==null?"":t.toString().toLowerCase(java.util.Locale.ROOT);
            String ds=d==null?"":d.toString().toLowerCase(java.util.Locale.ROOT);
            if(ts.contains(q)||ds.contains(q)){
                AccessibilityNodeInfo cur=n;
                while(cur!=null){
                    if(cur.isClickable()&&cur.performAction(AccessibilityNodeInfo.ACTION_CLICK))return true;
                    cur=cur.getParent();
                }
            }
        }
        return false;
    }
    private static void collectNodes(AccessibilityNodeInfo node,java.util.List<AccessibilityNodeInfo> out){
        if(node==null)return;
        out.add(node);
        for(int i=0;i<node.getChildCount();i++)collectNodes(node.getChild(i),out);
    }
    public static boolean clickAny(String... labels){
        if(instance==null)return false;
        for(String label:labels)if(clickTextContains(label))return true;
        return false;
    }
    public static boolean typeIntoFocused(String text){
        if(instance==null)return false;
        return typeText(text);
    }
    public static boolean isPackageActive(String pkg){
        if(instance==null)return false;
        AccessibilityNodeInfo root=instance.getRootInActiveWindow();
        if(root==null)return false;
        CharSequence p=root.getPackageName();
        return p!=null&&pkg.equals(p.toString());
    }
    public static boolean focusInput(){
        if(instance==null)return false;
        AccessibilityNodeInfo root=instance.getRootInActiveWindow();
        if(root==null)return false;
        AccessibilityNodeInfo focused=root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
        if(focused!=null&&focused.isEditable())return true;
        java.util.List<AccessibilityNodeInfo> all=new java.util.ArrayList<>();
        collectNodes(root,all);
        for(AccessibilityNodeInfo n:all){
            if(n.isEditable()&&n.isFocusable()){
                n.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                return true;
            }
        }
        return false;
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
