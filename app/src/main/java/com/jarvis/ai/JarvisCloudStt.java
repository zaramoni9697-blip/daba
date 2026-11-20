package com.jarvis.ai;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public final class JarvisCloudStt {
    private JarvisCloudStt(){}

    public static String transcribe(Context context, String apiKey){
        int rate=16000, channels=AudioFormat.CHANNEL_IN_MONO, format=AudioFormat.ENCODING_PCM_16BIT;
        int min=AudioRecord.getMinBufferSize(rate,channels,format);
        if(min<=0)return null;
        AudioRecord record=null; File wav=null;
        try{
            record=new AudioRecord(MediaRecorder.AudioSource.MIC,rate,channels,format,Math.max(min*2,8192));
            if(record.getState()!=AudioRecord.STATE_INITIALIZED)return null;
            wav=File.createTempFile("jarvis_voice_",".wav",context.getCacheDir());
            try(FileOutputStream out=new FileOutputStream(wav)){
                writeHeader(out,0,rate,1,16);
                record.startRecording();
                long start=System.currentTimeMillis();
                long maxEnd=start+9000;
                long speechStarted=0;
                long lastVoice=start;
                byte[] b=new byte[4096]; int total=0;
                while(System.currentTimeMillis()<maxEnd){
                    int n=record.read(b,0,b.length);
                    if(n>0){
                        out.write(b,0,n);total+=n;
                        long sum=0;
                        for(int p=0;p+1<n;p+=2){
                            int lo=b[p]&0xff, hi=b[p+1];
                            short sample=(short)((hi<<8)|lo);
                            sum+=Math.abs((int)sample);
                        }
                        double avg=sum/Math.max(1,n/2);
                        long now=System.currentTimeMillis();
                        if(avg>900){
                            if(speechStarted==0)speechStarted=now;
                            lastVoice=now;
                        }
                        if(speechStarted>0 && now-lastVoice>1200 && now-speechStarted>900)break;
                    }
                }
                if(speechStarted==0 || total<rate*2) return null;
                record.stop();
                try(RandomAccessFile raf=new RandomAccessFile(wav,"rw")){raf.seek(0);writeHeader(raf,total,rate,1,16);}
            }
            return upload(wav,apiKey);
        }catch(Exception e){return null;}
        finally{
            if(record!=null)try{record.release();}catch(Exception ignored){}
            if(wav!=null)try{wav.delete();}catch(Exception ignored){}
        }
    }

    private static void writeHeader(OutputStream o,int n,int rate,int ch,int bits)throws IOException{
        DataOutputStream d=new DataOutputStream(o);
        d.writeBytes("RIFF");d.writeInt(Integer.reverseBytes(36+n));d.writeBytes("WAVE");
        d.writeBytes("fmt ");d.writeInt(Integer.reverseBytes(16));d.writeShort(Short.reverseBytes((short)1));
        d.writeShort(Short.reverseBytes((short)ch));d.writeInt(Integer.reverseBytes(rate));
        int br=rate*ch*bits/8;d.writeInt(Integer.reverseBytes(br));d.writeShort(Short.reverseBytes((short)(ch*bits/8)));
        d.writeShort(Short.reverseBytes((short)bits));d.writeBytes("data");d.writeInt(Integer.reverseBytes(n));d.flush();
    }
    private static void writeHeader(RandomAccessFile o,int n,int rate,int ch,int bits)throws IOException{
        o.writeBytes("RIFF");o.writeInt(Integer.reverseBytes(36+n));o.writeBytes("WAVE");
        o.writeBytes("fmt ");o.writeInt(Integer.reverseBytes(16));o.writeShort(Short.reverseBytes((short)1));
        o.writeShort(Short.reverseBytes((short)ch));o.writeInt(Integer.reverseBytes(rate));
        int br=rate*ch*bits/8;o.writeInt(Integer.reverseBytes(br));o.writeShort(Short.reverseBytes((short)(ch*bits/8)));
        o.writeShort(Short.reverseBytes((short)bits));o.writeBytes("data");o.writeInt(Integer.reverseBytes(n));
    }
    private static String upload(File wav,String key)throws Exception{
        String boundary="----JARVIS"+System.currentTimeMillis();
        HttpURLConnection c=(HttpURLConnection)new URL("https://api.groq.com/openai/v1/audio/transcriptions").openConnection();
        c.setConnectTimeout(15000);c.setReadTimeout(30000);c.setDoOutput(true);c.setRequestMethod("POST");
        c.setRequestProperty("Authorization","Bearer "+key);
        c.setRequestProperty("Content-Type","multipart/form-data; boundary="+boundary);
        try(DataOutputStream o=new DataOutputStream(c.getOutputStream())){
            part(o,boundary,"model","whisper-large-v3-turbo");
            part(o,boundary,"language","bn");
            o.writeBytes("--"+boundary+"\r\n");
            o.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"voice.wav\"\r\n");
            o.writeBytes("Content-Type: audio/wav\r\n\r\n");
            try(FileInputStream in=new FileInputStream(wav)){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)o.write(b,0,n);}
            o.writeBytes("\r\n--"+boundary+"--\r\n");
        }
        int code=c.getResponseCode();InputStream in=code>=200&&code<300?c.getInputStream():c.getErrorStream();
        String body=read(in);if(code<200||code>=300)return null;
        return new JSONObject(body).optString("text","").trim();
    }
    private static void part(DataOutputStream o,String boundary,String name,String value)throws IOException{
        o.writeBytes("--"+boundary+"\r\n");o.writeBytes("Content-Disposition: form-data; name=\""+name+"\"\r\n\r\n");o.writeBytes(value+"\r\n");
    }
    private static String read(InputStream in)throws IOException{
        if(in==null)return "";StringBuilder s=new StringBuilder();byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1)s.append(new String(b,0,n,"UTF-8"));return s.toString();
    }
}