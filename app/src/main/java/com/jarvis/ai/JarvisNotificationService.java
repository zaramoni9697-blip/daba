package com.jarvis.ai;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

public class JarvisNotificationService extends NotificationListenerService {
    @Override public void onListenerConnected(){
        super.onListenerConnected();
        updateSnapshot();
    }

    @Override public void onNotificationPosted(StatusBarNotification sbn){
        updateSnapshot();
    }

    @Override public void onNotificationRemoved(StatusBarNotification sbn){
        updateSnapshot();
    }

    private void updateSnapshot(){
        try{
            StatusBarNotification[] all=getActiveNotifications();
            if(all==null||all.length==0){
                SecureStore.put(this,"latest_notifications","No active notifications.");
                return;
            }
            StringBuilder out=new StringBuilder();
            int count=0;
            for(int i=all.length-1;i>=0 && count<8;i--){
                StatusBarNotification s=all[i];
                Notification n=s.getNotification();
                if(n==null)continue;
                CharSequence title=n.extras.getCharSequence(Notification.EXTRA_TITLE);
                CharSequence text=n.extras.getCharSequence(Notification.EXTRA_TEXT);
                if(TextUtils.isEmpty(title)&&TextUtils.isEmpty(text))continue;
                out.append("• ").append(s.getPackageName());
                if(!TextUtils.isEmpty(title))out.append(": ").append(title);
                if(!TextUtils.isEmpty(text))out.append(" — ").append(text);
                out.append("\n");
                count++;
            }
            SecureStore.put(this,"latest_notifications",out.length()==0?"No readable notifications.":out.toString().trim());
        }catch(Exception e){
            SecureStore.put(this,"latest_notifications","Notification access is unavailable.");
        }
    }
}
