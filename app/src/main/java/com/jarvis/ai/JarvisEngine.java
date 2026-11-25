package com.jarvis.ai;
import android.content.Context;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;
import org.json.*;

public class JarvisEngine {
    private final Context context;
    private final JarvisMemory memory;
    private String lastError = "";
    private String activeProvider = "";

    public JarvisEngine(Context c){
        context=c.getApplicationContext();
        memory=new JarvisMemory(c);
    }

    public String ask(String user){
        if(!"false".equals(SecureStore.get(context,"memory_enabled"))) memory.add("user",user);
        String history=memory.context(6);
        String prompt="You are JARVIS, a concise Android voice assistant. Detect whether the user is speaking Bengali or English from the current message and reply in the same language. If Bengali script is present, answer naturally in Bangla (বাংলা), not English. If English is used, answer in English. For mixed Bengali-English, preserve the user's mix naturally. Do not translate unless asked. Do not claim you performed an action unless the app actually did it.\nRecent context:\n"+history+"\nCurrent user: "+user;
        String result=callProvider(prompt);
        if(result==null){ String detail=lastError==null?"" : lastError.trim(); result=detail.isEmpty()? "দুঃখিত, AI উত্তর দিতে পারেনি। API key/provider settings পরীক্ষা করুন." : "দুঃখিত, AI উত্তর দিতে পারেনি।\n"+detail; }
        if(!"false".equals(SecureStore.get(context,"memory_enabled"))) memory.add("assistant",result);
        return result;
    }

    /*
     * Automatic failover:
     * 1) Try the user's selected provider first.
     * 2) On quota/rate-limit (429), timeout/network error, server error (5xx),
     *    or an invalid provider response, immediately try the next configured provider.
     * 3) Continue until a provider returns a valid answer.
     */
    private String callProvider(String prompt){
        lastError="";
        activeProvider="";
        String primary=SecureStore.get(context,"primary");
        if(primary==null || primary.trim().isEmpty()) primary="Gemini";

        String[] providers="false".equals(SecureStore.get(context,"auto_failover"))?new String[]{primary}:new String[]{primary,"Gemini","OpenAI-compatible","Groq","OpenRouter","DeepSeek","Grok","Custom"};

        java.util.HashSet<String> tried=new java.util.HashSet<>();
        for(String p:providers){
            if(p==null || p.trim().isEmpty() || tried.contains(p)) continue;
            tried.add(p);

            String key=getKey(p);
            if(key==null || key.trim().isEmpty()) continue;

            try{
                String result;
                if(p.equals("Gemini")) result=gemini(key,prompt);
                else result=openaiLike(key,prompt,p);

                if(result!=null && !result.trim().isEmpty()){
                    activeProvider=p;
                    return result.trim();
                }

                lastError=p+" returned an empty response";
            }catch(Exception e){
                lastError=p+" failed: "+e.getMessage();
                // Deliberately continue to the next provider.
            }
        }
        return null;
    }

    private String getKey(String p){
        if(p.equals("Gemini")) return SecureStore.get(context,"gemini");
        if(p.equals("Groq")) return SecureStore.get(context,"groq");
        if(p.equals("OpenAI-compatible")) return SecureStore.get(context,"openai");
        if(p.equals("OpenRouter")) return SecureStore.get(context,"openrouter");
        if(p.equals("DeepSeek")) return SecureStore.get(context,"deepseek");
        if(p.equals("Grok")) return SecureStore.get(context,"xai");
        return SecureStore.get(context,"custom");
    }

    private String gemini(String key,String prompt)throws Exception{
        URL u=new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="+key);
        HttpURLConnection c=(HttpURLConnection)u.openConnection();
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setConnectTimeout(6000);
        c.setReadTimeout(15000);
        c.setRequestProperty("Content-Type","application/json");

        JSONObject body=new JSONObject()
            .put("contents",new JSONArray().put(new JSONObject()
                .put("parts",new JSONArray().put(new JSONObject().put("text",prompt)))))
            .put("generationConfig",new JSONObject()
                .put("maxOutputTokens",256));

        write(c,body.toString());
        String response=readChecked(c);
        JSONObject r=new JSONObject(response);
        JSONArray candidates=r.optJSONArray("candidates");
        if(candidates==null || candidates.length()==0) throw new IOException("No candidates returned");
        JSONObject content=candidates.getJSONObject(0).optJSONObject("content");
        if(content==null) throw new IOException("No content returned");
        JSONArray parts=content.optJSONArray("parts");
        if(parts==null || parts.length()==0) throw new IOException("No response text returned");
        StringBuilder answer=new StringBuilder();
        for(int i=0;i<parts.length();i++){
            JSONObject part=parts.optJSONObject(i);
            if(part==null) continue;
            String t=part.optString("text","");
            if(t!=null && !t.trim().isEmpty()){
                if(answer.length()>0) answer.append("\n");
                answer.append(t.trim());
            }
        }
        String text=answer.toString().trim();
        if(text.isEmpty()) throw new IOException("Empty response");
        return text;
    }

    private String openaiLike(String key,String prompt,String provider)throws Exception{
        String endpoint=provider.equals("Custom")?SecureStore.get(context,"custom_endpoint"):
            provider.equals("Groq")?"https://api.groq.com/openai/v1/chat/completions":
            provider.equals("OpenRouter")?"https://openrouter.ai/api/v1/chat/completions":
            provider.equals("DeepSeek")?"https://api.deepseek.com/chat/completions":
            provider.equals("Grok")?"https://api.x.ai/v1/chat/completions":
            "https://api.openai.com/v1/chat/completions";

        if(endpoint==null || endpoint.trim().isEmpty())
            endpoint="https://api.openai.com/v1/chat/completions";

        URL u=new URL(endpoint);
        HttpURLConnection c=(HttpURLConnection)u.openConnection();
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setConnectTimeout(6000);
        c.setReadTimeout(15000);
        c.setRequestProperty("Content-Type","application/json");
        c.setRequestProperty("Authorization","Bearer "+key);

        String model=provider.equals("Groq")?"llama-3.3-70b-versatile":
            provider.equals("OpenRouter")?getModel("openrouter","openai/gpt-6.2-sol"):
            provider.equals("DeepSeek")?getModel("deepseek","deepseek-flash"):
            provider.equals("Grok")?getModel("xai","grok-4.7"):
            getModel("openai","gpt-4o-mini");

        JSONObject body=new JSONObject()
            .put("model",model)
            .put("messages",new JSONArray().put(new JSONObject()
                .put("role","user")
                .put("content",prompt)))
            .put("max_tokens",256);

        write(c,body.toString());
        String response=readChecked(c);
        JSONObject r=new JSONObject(response);
        JSONArray choices=r.optJSONArray("choices");
        if(choices==null || choices.length()==0) throw new IOException("No choices returned");
        JSONObject message=choices.getJSONObject(0).optJSONObject("message");
        if(message==null) throw new IOException("No message returned");
        String text=message.optString("content","");
        if(text.trim().isEmpty()) throw new IOException("Empty response");
        return text;
    }

    private void write(HttpURLConnection c,String s)throws Exception{
        try(OutputStream o=c.getOutputStream()){
            o.write(s.getBytes("UTF-8"));
        }
    }

    private String readChecked(HttpURLConnection c)throws Exception{
        int code=c.getResponseCode();
        InputStream in=code>=200 && code<300?c.getInputStream():c.getErrorStream();
        StringBuilder s=new StringBuilder();
        if(in!=null){
            try(BufferedReader b=new BufferedReader(new InputStreamReader(in))){
                String l;
                while((l=b.readLine())!=null) s.append(l);
            }
        }

        if(code<200 || code>=300){
            String detail=s.toString();
            if(detail.length()>300) detail=detail.substring(0,300);
            throw new IOException("HTTP "+code+(detail.isEmpty()?"":" - "+detail));
        }
        return s.toString();
    }

    private String getModel(String key,String def){
        String v=SecureStore.get(context,key+"_model");
        return v==null || v.trim().isEmpty()?def:v.trim();
    }

    public String getActiveProvider(){ return activeProvider; }
    public String getLastError(){ return lastError; }
    public JarvisMemory memory(){return memory;}
}
