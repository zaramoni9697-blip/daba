package com.jarvis.ai;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.os.Handler;
import android.os.Looper;

public class JarvisMicDiagnostic {
    public interface Callback { void onUpdate(String text, float rms, float peak, boolean voice); }
    private final Context context;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private AudioRecord recorder;
    private Thread thread;
    private volatile boolean running;
    private Callback callback;
    private int sourceUsed=-1;
    private float noiseFloor=0f;
    private long samples=0;

    public JarvisMicDiagnostic(Context c){context=c.getApplicationContext();}

    public String start(Callback cb){
        callback=cb;
        if(android.os.Build.VERSION.SDK_INT>=23 && context.checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)
            return "MIC PERMISSION: OFF";
        stop();
        int sr=16000;
        int min=AudioRecord.getMinBufferSize(sr,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
        if(min<=0)min=2048;
        int buffer=Math.max(min*2,4096);
        int[] sources={MediaRecorder.AudioSource.VOICE_RECOGNITION,MediaRecorder.AudioSource.MIC};
        for(int src:sources){
            try{
                AudioRecord r=new AudioRecord(src,sr,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,buffer);
                if(r.getState()==AudioRecord.STATE_INITIALIZED){recorder=r;sourceUsed=src;break;}
                r.release();
            }catch(Throwable ignored){}
        }
        if(recorder==null)return "AUDIORECORD: FAILED • microphone capture unavailable";
        try{recorder.startRecording();}catch(Throwable e){recorder.release();recorder=null;return "AUDIORECORD: START FAILED";}
        if(recorder.getRecordingState()!=AudioRecord.RECORDSTATE_RECORDING){stop();return "AUDIORECORD: NOT RECORDING";}
        final String sourceName=sourceUsed==MediaRecorder.AudioSource.VOICE_RECOGNITION?"VOICE_RECOGNITION":"MIC";
        final boolean agc=AutomaticGainControl.isAvailable();
        final boolean ns=NoiseSuppressor.isAvailable();
        final boolean aec=AcousticEchoCanceler.isAvailable();
        try{if(agc){AutomaticGainControl x=AutomaticGainControl.create(recorder.getAudioSessionId());if(x!=null)x.setEnabled(true);}}catch(Throwable ignored){}
        try{if(ns){NoiseSuppressor x=NoiseSuppressor.create(recorder.getAudioSessionId());if(x!=null)x.setEnabled(true);}}catch(Throwable ignored){}
        running=true; samples=0; noiseFloor=0f;
        thread=new Thread(()->{
            short[] buf=new short[1600];
            while(running&&recorder!=null){
                int n=recorder.read(buf,0,buf.length);
                if(n<=0)continue;
                double sum=0;int peak=0;
                for(int i=0;i<n;i++){int v=Math.abs(buf[i]);sum+=(double)v*v;if(v>peak)peak=v;}
                float rms=(float)Math.sqrt(sum/Math.max(1,n));
                samples++;
                if(samples<=12)noiseFloor=(noiseFloor*(samples-1)+rms)/samples;
                else noiseFloor=noiseFloor*0.995f+rms*0.005f;
                final float nf=noiseFloor;
                final int pk=peak;
                final boolean voice=rms>Math.max(250f,nf*1.45f);
                handler.post(()->{
                    if(callback!=null) callback.onUpdate(
                        "AudioRecord: OK • source="+sourceName+
                        "\nRMS: "+Math.round(rms)+" • Peak: "+pk+
                        "\nNoise floor: "+Math.round(nf)+
                        "\nSignal: "+(voice?"VOICE DETECTED":"NO VOICE / LOW SIGNAL")+
                        "\nAGC: "+(agc?"available":"unavailable")+" • NS: "+(ns?"available":"unavailable")+
                        " • AEC: "+(aec?"available":"unavailable"),rms,pk,voice);
                });
            }
        },"JarvisMicDiagnostic");
        thread.start();
        return "MIC TEST STARTED • "+sourceName;
    }

    public void stop(){
        running=false;
        if(thread!=null){try{thread.interrupt();}catch(Throwable ignored){}thread=null;}
        if(recorder!=null){try{recorder.stop();}catch(Throwable ignored){}try{recorder.release();}catch(Throwable ignored){}recorder=null;}
    }
}