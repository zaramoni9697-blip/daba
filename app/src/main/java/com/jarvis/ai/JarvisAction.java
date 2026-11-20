package com.jarvis.ai;
public class JarvisAction {
    public enum Type { NONE, OPEN_APP, HOME, BACK, RECENTS, NOTIFICATIONS, QUICK_SETTINGS, SCROLL_UP, SCROLL_DOWN, VOLUME_UP, VOLUME_DOWN, MUTE, CLICK_TEXT, TYPE_TEXT, CALL, SMS, ASK_AI }
    public final Type type; public final String value; public final boolean confirmation;
    public JarvisAction(Type t,String v,boolean c){type=t;value=v;confirmation=c;}
}
