package com.jarvis.ai;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.os.Bundle;
import java.util.List;

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
    public static boolean scroll(boolean down){
        if(instance==null)return false; Path p=new Path();
        if(down){p.moveTo(540,1200);p.lineTo(540,350);}else{p.moveTo(540,350);p.lineTo(540,1200);}
        GestureDescription g=new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(p,0,500)).build();
        return instance.dispatchGesture(g,null,null);
    }
    @Override public void onDestroy(){instance=null;super.onDestroy();}
}
