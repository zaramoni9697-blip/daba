package com.jarvis.ai;
import android.content.Context;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;
import org.json.*;

public class JarvisEngine {
    private final Context context; private final JarvisMemory memory;
    public JarvisEngine(Context c){context=c.getApplicationContext();memory=new JarvisMemory(c);}
    public String ask(String user){
        memory.add("user",user); String history=memory.context(12);
        String prompt="You are JARVIS, a concise Android voice assistant. Reply in the user's language. Do not claim you performed an action unless the app actually did it.\nRecent context:\n"+history+"\nCurrent user: "+user;
        String result=callProvider(prompt); if(result==null)result="AI service is unavailable. Please check your API settings.";
        memory.add("assistant",result); return result;
    }
    private String lastError="";
    private String callProvider(String prompt){
        String primary=SecureStore.get(context,"primary"); if(primary==null)primary="Gemini";
        String[] providers={primary,"Gemini","OpenAI-compatible","Groq","Custom"};
        for(String p:providers){
            String key=p.equals("Gemini")?SecureStore.get(context,"gemini"):p.equals("Groq")?SecureStore.get(context,"groq"):p.equals("OpenAI-compatible")?SecureStore.get(context,"openai"):SecureStore.get(context,"custom");
            if(key==null||key.trim().isEmpty())continue;
            try{if(p.equals("Gemini"))return gemini(key,prompt);return openaiLike(key,prompt,p);}catch(Exception e){lastError=p+" error: "+e.getMessage();}
        } return lastError.isEmpty()?null:lastError;
    }
    private String gemini(String key,String prompt)throws Exception{
        URL u=new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key="+key);
        HttpURLConnection c=(HttpURLConnection)u.openConnection();c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");
        JSONObject body=new JSONObject().put("contents",new JSONArray().put(new JSONObject().put("parts",new JSONArray().put(new JSONObject().put("text",prompt)))));
        write(c,body.toString()); JSONObject r=new JSONObject(read(c));
        return r.getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text");
    }
    private String openaiLike(String key,String prompt,String provider)throws Exception{
        String endpoint=provider.equals("Custom")?SecureStore.get(context,"custom_endpoint"):provider.equals("Groq")?"https://api.groq.com/openai/v1/chat/completions":"https://api.openai.com/v1/chat/completions";
        if(endpoint==null||endpoint.trim().isEmpty())endpoint="https://api.openai.com/v1/chat/completions";
        URL u=new URL(endpoint);HttpURLConnection c=(HttpURLConnection)u.openConnection();c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");c.setRequestProperty("Authorization","Bearer "+key);
        String model=provider.equals("Groq")?"llama-3.3-70b-versatile":"gpt-4o-mini";
        JSONObject body=new JSONObject().put("model",model).put("messages",new JSONArray().put(new JSONObject().put("role","user").put("content",prompt)));
        write(c,body.toString());JSONObject r=new JSONObject(read(c));
        return r.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
    }
    private void write(HttpURLConnection c,String s)throws Exception{try(OutputStream o=c.getOutputStream()){o.write(s.getBytes("UTF-8"));}}
    private String read(HttpURLConnection c)throws Exception{InputStream in=c.getResponseCode()<400?c.getInputStream():c.getErrorStream();try(BufferedReader b=new BufferedReader(new InputStreamReader(in))){StringBuilder s=new StringBuilder(); String l;while((l=b.readLine())!=null)s.append(l);return s.toString();}}
    public JarvisMemory memory(){return memory;}
}
