package com.jarvis.ai;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public final class JarvisCloudStt {
    private JarvisCloudStt(){}

    public static String transcribe(Context context,String apiKey){
        final int rate=16000, channels=AudioFormat.CHANNEL_IN_MONO, format=AudioFormat.ENCODING_PCM_16BIT;
        int min=AudioRecord.getMinBufferSize(rate,channels,format);
        if(min<=0)return null;
        AudioRecord record=null; AutomaticGainControl agc=null; NoiseSuppressor ns=null; File wav=null;
        try{
            record=new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,rate,channels,format,Math.max(min*4,16384));
            if(record.getState()!=AudioRecord.STATE_INITIALIZED){
                try{record.release();}catch(Exception ignored){}
                record=new AudioRecord(MediaRecorder.AudioSource.MIC,rate,channels,format,Math.max(min*4,16384));
            }
            if(record.getState()!=AudioRecord.STATE_INITIALIZED)return null;

            int session=record.getAudioSessionId();
            try{if(AutomaticGainControl.isAvailable())agc=AutomaticGainControl.create(session);}catch(Exception ignored){}
            try{if(NoiseSuppressor.isAvailable())ns=NoiseSuppressor.create(session);}catch(Exception ignored){}
            try{if(agc!=null)agc.setEnabled(true);}catch(Exception ignored){}
            try{if(ns!=null)ns.setEnabled(true);}catch(Exception ignored){}

            wav=File.createTempFile("jarvis_voice_"," .wav".replace(" ",""),context.getCacheDir());
            try(FileOutputStream out=new FileOutputStream(wav)){
                writeHeader(out,0,rate,1,16);
                record.startRecording();
                long start=System.currentTimeMillis(), maxEnd=start+9000, speechStarted=0, lastVoice=start;
                double noiseFloor=120.0;
                byte[] b=new byte[4096]; int total=0;

                while(System.currentTimeMillis()<maxEnd){
                    int n=record.read(b,0,b.length);
                    if(n<=0)continue;

                    double sum=0;
                    for(int p=0;p+1<n;p+=2){
                        int sample=(short)(((b[p+1]&0xff)<<8)|(b[p]&0xff));
                        sum+=Math.abs(sample);
                    }
                    double avg=sum/Math.max(1,n/2);
                    if(speechStarted==0){
                        noiseFloor=noiseFloor*0.88+avg*0.12;
                    }
                    double target=Math.max(700.0,noiseFloor*3.0);
                    double gain=avg<1?6.0:Math.min(10.0,Math.max(1.8,2200.0/Math.max(avg,180.0)));
                    for(int p=0;p+1<n;p+=2){
                        int sample=(short)(((b[p+1]&0xff)<<8)|(b[p]&0xff));
                        double centered=sample;
                        double boosted=centered*gain;
                        if(Math.abs(boosted)<noiseFloor*1.15 && speechStarted==0)boosted=0;
                        boosted=Math.max(-32767,Math.min(32767,boosted));
                        short s=(short)boosted;
                        b[p]=(byte)(s&0xff); b[p+1]=(byte)((s>>8)&0xff);
                    }
                    out.write(b,0,n); total+=n;

                    long now=System.currentTimeMillis();
                    if(avg>target){
                        if(speechStarted==0)speechStarted=now;
                        lastVoice=now;
                    }
                    if(speechStarted>0 && now-lastVoice>1400 && now-speechStarted>900)break;
                }
                if(speechStarted==0 || total<rate*2)return null;
                record.stop();
                try(RandomAccessFile raf=new RandomAccessFile(wav,"rw")){
                    raf.seek(0);writeHeader(raf,total,rate,1,16);
                }
            }
            return upload(wav,apiKey);
        }catch(Exception e){return null;}
        finally{
            if(agc!=null)try{agc.release();}catch(Exception ignored){}
            if(ns!=null)try{ns.release();}catch(Exception ignored){}
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
            part(o,boundary,"prompt","JARVIS Android voice command. Preserve Bengali and English mixed speech. Do not translate or paraphrase. Prefer exact command words and app names. Common phrases: JARVIS, hey jarvis, open, খুলে দাও, খোলো, চালু কর, চালু করো, করো, Facebook, YouTube, Messenger, WhatsApp, Telegram, Settings, Chrome, Camera, Gallery, home, back, volume, flashlight, torch, brightness, screenshot, notifications, Wi-Fi, Bluetooth, mobile data, scroll, click, tap, type, call, message, হোম, ব্যাক, ভলিউম, টর্চ, লাইট, স্ক্রিনশট, নোটিফিকেশন, ওয়াইফাই, ব্লুটুথ, মেসেজ. Keep the original language and wording as closely as possible.");
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
        o.writeBytes("--"+boundary+"\r\n");
        o.writeBytes("Content-Disposition: form-data; name=\""+name+"\"\r\n\r\n");
        o.writeBytes(value+"\r\n");
    }
    private static String read(InputStream in)throws IOException{
        if(in==null)return "";StringBuilder s=new StringBuilder();byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1)s.append(new String(b,0,n,"UTF-8"));return s.toString();
    }
}