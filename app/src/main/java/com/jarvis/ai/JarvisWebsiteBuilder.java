package com.jarvis.ai;

import android.content.Context;
import android.util.Base64;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class JarvisWebsiteBuilder {
    public interface Callback { void onDone(String message); }

    private final Context context;
    private final JarvisEngine engine;

    public JarvisWebsiteBuilder(Context c){
        context=c.getApplicationContext();
        engine=new JarvisEngine(context);
    }

    public void buildAndPublish(String request, Callback cb){
        new Thread(()->{
            try{
                String token=SecureStore.get(context,"github_pages_token");
                String repo=SecureStore.get(context,"github_pages_repo");
                if(token==null||token.trim().isEmpty()){
                    cb.onDone("WEBSITE_SETUP_REQUIRED|GitHub token সেট করুন: Settings → AI / API → WEBSITE PUBLISHING.");
                    return;
                }
                if(repo==null||repo.trim().isEmpty())repo="zaramoni9697-blip/daba";
                String prompt =
                    "You are JARVIS Website Architect. Build a COMPLETE, polished, responsive static website from the user's request. " +
                    "Return ONLY these four marked sections, with no markdown fences and no commentary:\n" +
                    "===TITLE===\nshort title\n===END_TITLE===\n" +
                    "===SLUG===\nshort-kebab-slug\n===END_SLUG===\n" +
                    "===HTML===\ncomplete index.html only\n===END_HTML===\n" +
                    "===CSS===\ncomplete style.css only\n===END_CSS===\n" +
                    "===JS===\ncomplete script.js only\n===END_JS===\n" +
                    "The HTML must link ./style.css and ./script.js. Use semantic HTML, mobile-first responsive CSS, accessible controls, modern visual design, and working client-side interactions. " +
                    "Do not use server-side code. If images are needed, use stable public image URLs or tasteful CSS placeholders. " +
                    "User request: "+request;
                String raw=engine.ask(prompt);
                Site site=parse(raw);
                if(site.html.isEmpty()||site.css.isEmpty())throw new Exception("AI did not return complete website files.");
                if(site.slug.isEmpty())site.slug="jarvis-site-"+System.currentTimeMillis();
                site.slug=slug(site.slug);
                String path="websites/"+site.slug+"/";
                put(repo,token,path+"index.html",site.html);
                put(repo,token,path+"style.css",site.css);
                put(repo,token,path+"script.js",site.js);
                ensurePages(repo,token);
                requestBuild(repo,token);
                String[] rr=repo.split("/");
                if(rr.length!=2)throw new Exception("Repository must be owner/repository.");
                String url="https://"+rr[0].toLowerCase(Locale.ROOT)+".github.io/"+rr[1]+"/"+path;
                cb.onDone("WEBSITE_READY|"+site.title+"\n"+url+"\n\nWebsite files published to GitHub Pages. প্রথমবার publish হতে কয়েক মিনিট লাগতে পারে।");
            }catch(Throwable e){
                cb.onDone("WEBSITE_ERROR|Website publish failed: "+safe(e.getMessage()));
            }
        },"JarvisWebsiteBuilder").start();
    }

    private static class Site{
        String title="",slug="",html="",css="",js="";
    }

    private Site parse(String s)throws Exception{
        Site x=new Site();
        x.title=part(s,"TITLE");
        x.slug=part(s,"SLUG");
        x.html=part(s,"HTML");
        x.css=part(s,"CSS");
        x.js=part(s,"JS");
        if(x.html.isEmpty())throw new Exception("Missing HTML");
        return x;
    }

    private String part(String s,String key){
        String a="==="+key+"===";
        String b="===END_"+key+"===";
        int i=s.indexOf(a),j=s.indexOf(b);
        if(i<0||j<0||j<=i)return "";
        return s.substring(i+a.length(),j).trim();
    }

    private String slug(String s){
        String x=s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+","-").replaceAll("^-+|-+$","");
        return x.isEmpty()?"jarvis-site-"+System.currentTimeMillis():x;
    }

    private void put(String repo,String token,String path,String content)throws Exception{
        JSONObject body=new JSONObject();
        body.put("message","JARVIS website: "+path);
        body.put("content",Base64.encodeToString(content.getBytes(StandardCharsets.UTF_8),Base64.NO_WRAP));
        body.put("branch","main");
        request("PUT","https://api.github.com/repos/"+repo+"/contents/"+encPath(path),token,body.toString(),new int[]{200,201});
    }

    private void ensurePages(String repo,String token)throws Exception{
        JSONObject body=new JSONObject();
        body.put("build_type","legacy");
        JSONObject source=new JSONObject();
        source.put("branch","main"); source.put("path","/");
        body.put("source",source);
        int code=requestAny("POST","https://api.github.com/repos/"+repo+"/pages",token,body.toString());
        if(code!=201 && code!=409){
            throw new IOException("GitHub Pages setup returned HTTP "+code);
        }
    }

    private void requestBuild(String repo,String token)throws Exception{
        int code=requestAny("POST","https://api.github.com/repos/"+repo+"/pages/builds",token,"{}");
        if(code!=201 && code!=409 && code!=404)throw new IOException("Pages build returned HTTP "+code);
    }

    private int requestAny(String method,String url,String token,String body)throws Exception{
        HttpURLConnection c=null;
        try{
            c=(HttpURLConnection)new URL(url).openConnection();
            c.setRequestMethod(method);c.setConnectTimeout(15000);c.setReadTimeout(30000);
            c.setRequestProperty("Accept","application/vnd.github+json");
            c.setRequestProperty("Authorization","Bearer "+token.trim());
            c.setRequestProperty("X-GitHub-Api-Version","2026-03-10");
            c.setRequestProperty("Content-Type","application/json; charset=UTF-8");
            c.setDoOutput(true);
            try(OutputStream o=c.getOutputStream()){o.write(body.getBytes(StandardCharsets.UTF_8));}
            int code=c.getResponseCode();
            if(code>=400){
                InputStream is=c.getErrorStream();
                String detail="";
                if(is!=null)detail=read(is);
                throw new IOException("GitHub HTTP "+code+(detail.isEmpty()?"":" • "+detail));
            }
            return code;
        }finally{if(c!=null)c.disconnect();}
    }

    private void request(String method,String url,String token,String body,int[] ok)throws Exception{
        int code=requestAny(method,url,token,body);
        for(int x:ok)if(x==code)return;
        throw new IOException("GitHub returned HTTP "+code);
    }

    private String encPath(String p)throws Exception{
        String[] a=p.split("/");
        StringBuilder b=new StringBuilder();
        for(String x:a){if(b.length()>0)b.append('/');b.append(URLEncoder.encode(x,"UTF-8").replace("+","%20"));}
        return b.toString();
    }

    private String read(InputStream is)throws Exception{
        BufferedReader r=new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8));
        StringBuilder b=new StringBuilder();String line;
        while((line=r.readLine())!=null)b.append(line);
        return b.toString();
    }

    private String safe(String s){
        if(s==null||s.trim().isEmpty())return "unknown error";
        return s.length()>500?s.substring(0,500):s;
    }
}
