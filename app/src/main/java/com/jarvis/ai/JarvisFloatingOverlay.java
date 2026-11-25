package com.jarvis.ai;

import android.app.Service;
import android.content.*;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.ImageView;

public class JarvisFloatingOverlay {
    private final Service service;
    private final WindowManager wm;
    private View bubble;
    private WindowManager.LayoutParams lp;
    private float downX, downY;
    private int startX, startY;
    private boolean moved;
    private final int size;

    public JarvisFloatingOverlay(Service s) {
        service = s;
        wm = (WindowManager)s.getSystemService(Context.WINDOW_SERVICE);
        size = (int)(56 * s.getResources().getDisplayMetrics().density);
    }

    public void show() {
        if (bubble != null || Build.VERSION.SDK_INT < 23 || !Settings.canDrawOverlays(service)) return;

        ImageView icon = new ImageView(service);
        icon.setImageResource(com.jarvis.ai.R.drawable.jarvis_icon);
        icon.setPadding(7,7,7,7);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(Color.argb(235, 18, 22, 30));
        bg.setStroke(2, Color.argb(220, 90, 190, 255));
        icon.setBackground(bg);
        icon.setElevation(12f);

        lp = new WindowManager.LayoutParams(
                size, size,
                Build.VERSION.SDK_INT >= 26
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.x = getX();
        lp.y = getY();

        icon.setOnTouchListener((v,e) -> {
            switch(e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    moved = false;
                    downX = e.getRawX();
                    downY = e.getRawY();
                    startX = lp.x;
                    startY = lp.y;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    int nx = startX + (int)(e.getRawX() - downX);
                    int ny = startY + (int)(e.getRawY() - downY);
                    if (Math.abs(nx-startX) > 8 || Math.abs(ny-startY) > 8) moved = true;
                    lp.x = Math.max(0,nx);
                    lp.y = Math.max(0,ny);
                    try { wm.updateViewLayout(bubble,lp); } catch(Exception ignored) {}
                    return true;
                case MotionEvent.ACTION_UP:
                    savePosition(lp.x,lp.y);
                    if (!moved) openJarvis();
                    return true;
            }
            return false;
        });

        bubble = icon;
        try { wm.addView(bubble,lp); } catch(Exception e) { bubble=null; }
    }

    private void openJarvis() {
        Intent i = new Intent(service, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        service.startActivity(i);
    }

    public void remove() {
        if (bubble != null) {
            try { wm.removeView(bubble); } catch(Exception ignored) {}
            bubble = null;
        }
    }

    private int getX() {
        return service.getSharedPreferences("jarvis_overlay",Context.MODE_PRIVATE).getInt("x",16);
    }
    private int getY() {
        return service.getSharedPreferences("jarvis_overlay",Context.MODE_PRIVATE).getInt("y",180);
    }
    private void savePosition(int x,int y) {
        service.getSharedPreferences("jarvis_overlay",Context.MODE_PRIVATE)
                .edit().putInt("x",x).putInt("y",y).apply();
    }
}
