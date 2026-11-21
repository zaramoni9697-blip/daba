package com.jarvis.ai;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.*;

public final class JarvisHistory {
    private static final String KEY="chat_history";
    private static final int MAX=80;

    public static void add(Context c,String user,String assistant){
        try{
            JSONArray old=read(c);
            JSONArray out=new JSONArray();
            JSONObject item=new JSONObject();
            item.put("time",new SimpleDateFormat("dd MMM, HH:mm",Locale.getDefault()).format(new Date()));
            item.put("user",user==null?"":user.trim());
            item.put("assistant",assistant==null?"":assistant.trim());
            out.put(item);
            for(int i=0;i<old.length() && out.length()<MAX;i++){
                out.put(old.getJSONObject(i));
            }
            SecureStore.put(c,KEY,out.toString());
        }catch(Exception ignored){}
    }

    public static JSONArray read(Context c){
        try{
            String raw=SecureStore.get(c,KEY);
            return raw==null||raw.trim().isEmpty()?new JSONArray():new JSONArray(raw);
        }catch(Exception e){return new JSONArray();}
    }

    public static void clear(Context c){
        SecureStore.put(c,KEY,"[]");
    }
}
