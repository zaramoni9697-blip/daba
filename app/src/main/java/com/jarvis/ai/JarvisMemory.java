package com.jarvis.ai;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;

public class JarvisMemory {
    private static final String PREF="jarvis_memory", KEY="history";
    private final Context context;
    public JarvisMemory(Context c){context=c.getApplicationContext();}
    public synchronized void add(String role,String text){
        try{
            JSONArray a=read(); JSONObject o=new JSONObject();
            o.put("role",role); o.put("text",text); o.put("time",System.currentTimeMillis()); a.put(o);
            while(a.length()>40)a.remove(0);
            context.getSharedPreferences(PREF,0).edit().putString(KEY,a.toString()).apply();
        }catch(Exception ignored){}
    }
    public synchronized String context(int max){
        try{
            JSONArray a=read(); StringBuilder s=new StringBuilder();
            for(int i=Math.max(0,a.length()-max);i<a.length();i++){
                JSONObject o=a.getJSONObject(i);
                s.append(o.optString("role")).append(": ").append(o.optString("text")).append("\n");
            }
            return s.toString().trim();
        }catch(Exception e){return "";}
    }
    public synchronized void clear(){context.getSharedPreferences(PREF,0).edit().remove(KEY).apply();}
    private JSONArray read(){return new JSONArray(context.getSharedPreferences(PREF,0).getString(KEY,"[]"));}
}
