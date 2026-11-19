package com.jarvis.ai;

import android.content.*;
import android.os.Build;

public class JarvisBootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        String action = intent.getAction();
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action) && !Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) return;
        if ("false".equals(SecureStore.get(context, "wake_enabled"))) return;
        try {
            Intent service = new Intent(context, JarvisWakeService.class);
            if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(service);
            else context.startService(service);
        } catch (Exception ignored) {}
    }
}