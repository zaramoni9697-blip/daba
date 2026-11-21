package com.jarvis.ai;

import android.app.AlertDialog;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    private LinearLayout root, content, apiPanel;
    private Spinner provider, speechLanguage;
    private EditText gemini, openai, groq, openrouter, deepseek, xai, custom, endpoint;
    private EditText openrouterModel, deepseekModel, xaiModel;
    private Switch autoFailover, voiceReplies, saveMemory, farVoice;
    private TextView wakeState, phoneState, modeTitle, modeHint, micDiagnostic;\n    private LinearLayout micPanel;\n    private JarvisMicDiagnostic micTest;
    private final String[] providers={"Gemini","OpenAI-compatible","Groq","OpenRouter","DeepSeek","Grok","Custom"};
    private final int BG=Color.rgb(10,9,14), CARD=Color.rgb(20,19,28), INPUT=Color.rgb(12,11,17), CYAN=Color.rgb(0,225,255), PURPLE=Color.rgb(150,135,255), MUTED=Color.rgb(155,149,175);

    @Override protected void onCreate(Bundle b){super.onCreate(b);build();}
    @Override protected void onResume(){super.onResume();if(phoneState!=null)updatePhoneState();}

    private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
    private TextView tv(String s,float z,int c){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);t.setGravity(Gravity.CENTER_VERTICAL);return t;}
    private GradientDrawable bg(int c,float r){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp(r));return g;}
    private LinearLayout card(){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(16),dp(14),dp(16),dp(14));c.setBackground(bg(CARD,22));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(7),0,dp(7));c.setLayoutParams(p);return c;}
    private TextView label(String s){TextView t=tv(s.toUpperCase(),11,MUTED);t.setTypeface(null,1);t.setPadding(0,dp(4),0,dp(4));return t;}
    private EditText key(String hint,String store){EditText e=field(hint,store);e.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);String v=SecureStore.get(this,store);if(v!=null&&!v.isEmpty())e.setHint(hint+"  •  SAVED");return e;}
    private EditText field(String hint,String store){EditText e=new EditText(this);e.setHint(hint);e.setHintTextColor(Color.rgb(105,101,115));e.setTextColor(Color.WHITE);e.setSingleLine(true);e.setPadding(dp(14),0,dp(14),0);e.setBackground(bg(INPUT,15));String v=SecureStore.get(this,store);if(v!=null&&!v.isEmpty())e.setText(v);return e;}
    private void add(LinearLayout c,View v,int h){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(h));p.setMargins(0,dp(5),0,dp(2));c.addView(v,p);}
    private Button action(String text,int color){Button b=new Button(this);b.setText(text);b.setTextColor(Color.WHITE);b.setTextSize(12);b.setAllCaps(false);b.setBackground(bg(color,15));return b;}

    private void build(){
        ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(BG);
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(16),dp(10),dp(16),dp(30));scroll.addView(root);setContentView(scroll);

        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);
        TextView back=tv("‹",38,Color.WHITE);back.setGravity(Gravity.CENTER);head.addView(back,new LinearLayout.LayoutParams(dp(45),dp(55)));
        LinearLayout ht=new LinearLayout(this);ht.setOrientation(LinearLayout.VERTICAL);
        TextView h=tv("JARVIS",25,Color.WHITE);h.setTypeface(null,1);ht.addView(h);
        ht.addView(tv("PERSONAL CONTROL CENTER",10,CYAN));head.addView(ht,new LinearLayout.LayoutParams(0,-2,1));
        TextView online=tv("● SECURE",10,CYAN);head.addView(online,new LinearLayout.LayoutParams(-2,dp(45)));root.addView(head);
        back.setOnClickListener(v->finish());

        LinearLayout hero=card();
        TextView badge=tv("⚙  SETTINGS MATRIX",11,CYAN);badge.setTypeface(null,1);hero.addView(badge);
        modeTitle=tv("GENERAL MODE",21,Color.WHITE);modeTitle.setTypeface(null,1);modeTitle.setPadding(0,dp(7),0,0);hero.addView(modeTitle);
        modeHint=tv("Language, memory and JARVIS behaviour.",12,MUTED);hero.addView(modeHint);
        root.addView(hero);

        LinearLayout modes=new LinearLayout(this);modes.setPadding(0,dp(4),0,dp(4));
        String[] names={"GENERAL","AI / API","VOICE","PHONE"};
        for(String n:names){Button b=action(n,Color.rgb(29,27,39));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(46),1);p.setMargins(dp(3),0,dp(3),0);modes.addView(b,p);b.setOnClickListener(v->showMode(n));}root.addView(modes);

        content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);root.addView(content);
        buildGeneral();buildApi();buildVoice();buildPhoneControl();

        LinearLayout saveCard=card();
        TextView safe=tv("All changes are stored locally. API keys are encrypted on this device.",11,MUTED);saveCard.addView(safe);
        Button save=action("✓  SAVE ALL SETTINGS",CYAN);save.setTextColor(Color.BLACK);add(saveCard,save,54);save.setOnClickListener(v->saveAll());root.addView(saveCard);
        showMode("GENERAL");
    }

    private LinearLayout generalPanel, voicePanel, phonePanel;
    private void buildGeneral(){
        generalPanel=new LinearLayout(this);generalPanel.setOrientation(LinearLayout.VERTICAL);
        LinearLayout c=card();c.addView(label("GENERAL MODE"));
        c.addView(tv("How should I understand and speak with you?",14,Color.WHITE));
        speechLanguage=new Spinner(this);
        String[] langs={"AUTO • বাংলা + English","bn-BD • শুধু বাংলা","en-US • শুধু English"};
        speechLanguage.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,langs));
        String sl=SecureStore.get(this,"speech_language");speechLanguage.setSelection("bn-BD".equals(sl)?1:"en-US".equals(sl)?2:0);add(c,speechLanguage,48);
        TextView l=tv("AUTO mode lets JARVIS follow the language you are using. Bengali text gets a Bengali reply; English gets English.",11,MUTED);l.setPadding(0,dp(4),0,dp(4));c.addView(l);
        autoFailover=new Switch(this);autoFailover.setText("Automatic AI failover");autoFailover.setTextColor(Color.WHITE);autoFailover.setTextSize(14);autoFailover.setChecked(!"false".equals(SecureStore.get(this,"auto_failover")));c.addView(autoFailover);
        saveMemory=new Switch(this);saveMemory.setText("Conversation memory");saveMemory.setTextColor(Color.WHITE);saveMemory.setTextSize(14);saveMemory.setChecked(!"false".equals(SecureStore.get(this,"memory_enabled")));c.addView(saveMemory);
        generalPanel.addView(c);content.addView(generalPanel);
    }

    private void buildApi(){
        apiPanel=new LinearLayout(this);apiPanel.setOrientation(LinearLayout.VERTICAL);
        LinearLayout c=card();c.addView(label("AI / API MODE"));
        c.addView(tv("Choose a brain, then enter its API key.",14,Color.WHITE));
        provider=new Spinner(this);provider.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,providers));
        String saved=SecureStore.get(this,"primary");if(saved!=null)for(int i=0;i<providers.length;i++)if(providers[i].equals(saved))provider.setSelection(i);
        add(c,provider,48);c.addView(tv("Only the selected provider's credentials are shown below.",11,MUTED));
        apiPanel.addView(c);

        LinearLayout keyCard=card();keyCard.addView(label("API KEY VAULT"));
        gemini=key("Gemini API key","gemini");openai=key("OpenAI API key","openai");groq=key("Groq API key","groq");openrouter=key("OpenRouter API key","openrouter");deepseek=key("DeepSeek API key","deepseek");xai=key("Grok / xAI API key","xai");custom=key("Custom API key","custom");
        endpoint=field("Custom OpenAI-compatible endpoint","custom_endpoint");
        openrouterModel=field("OpenRouter model","openrouter_model");deepseekModel=field("DeepSeek model","deepseek_model");xaiModel=field("Grok / xAI model","xai_model");
        apiPanel.addView(keyCard);
        provider.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){}public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){refreshApiFields(keyCard);}});
        content.addView(apiPanel);
    }

    private void refreshApiFields(LinearLayout c){
        while(c.getChildCount()>1)c.removeViewAt(1);
        String p=provider.getSelectedItem().toString();EditText key=gemini;
        if("OpenAI-compatible".equals(p))key=openai;else if("Groq".equals(p))key=groq;else if("OpenRouter".equals(p))key=openrouter;else if("DeepSeek".equals(p))key=deepseek;else if("Grok".equals(p))key=xai;else if("Custom".equals(p))key=custom;
        c.addView(tv("API KEY • "+p,11,CYAN));add(c,key,52);
        if("OpenRouter".equals(p)){c.addView(tv("MODEL",11,MUTED));add(c,openrouterModel,50);}
        if("DeepSeek".equals(p)){c.addView(tv("MODEL",11,MUTED));add(c,deepseekModel,50);}
        if("Grok".equals(p)){c.addView(tv("MODEL",11,MUTED));add(c,xaiModel,50);}
        if("Custom".equals(p)){c.addView(tv("ENDPOINT",11,MUTED));add(c,endpoint,50);}
        c.addView(tv("🔒 Stored encrypted locally • never displayed in plain text after saving.",10,MUTED));
    }

    private void buildVoice(){
        voicePanel=new LinearLayout(this);voicePanel.setOrientation(LinearLayout.VERTICAL);
        LinearLayout c=card();c.addView(label("VOICE & WAKE MODE"));
        farVoice=new Switch(this);
        farVoice.setText("Far-field voice mode • দূর থেকেও শোনার চেষ্টা");
        farVoice.setTextColor(Color.WHITE);farVoice.setTextSize(14);
        farVoice.setChecked(!"false".equals(SecureStore.get(this,"far_voice")));
        c.addView(farVoice);
        c.addView(tv("Uses enhanced microphone gain + noise suppression and Groq Whisper when a Groq key is saved. This can use more data and battery.",11,MUTED));
                voiceReplies=new Switch(this);voiceReplies.setText("JARVIS voice replies / TTS");voiceReplies.setTextColor(Color.WHITE);voiceReplies.setTextSize(14);voiceReplies.setChecked(!"false".equals(SecureStore.get(this,"voice_replies")));c.addView(voiceReplies);
        wakeState=tv(JarvisWakeService.isRunning()?"● WAKE LISTENER ACTIVE":"○ WAKE LISTENER OFF",12,CYAN);wakeState.setPadding(0,dp(6),0,dp(5));c.addView(wakeState);
        Button wake=action("◉  START / STOP HEY JARVIS",Color.rgb(28,45,52));add(c,wake,52);wake.setOnClickListener(v->{if(JarvisWakeService.isRunning()){stopService(new Intent(this,JarvisWakeService.class));SecureStore.put(this,"wake_enabled","false");}else{SecureStore.put(this,"wake_enabled","true");startForegroundService(new Intent(this,JarvisWakeService.class));}updateWakeState();});
        TextView hint=tv("If Android Speech Recognition is unavailable, JARVIS can use Groq Whisper cloud voice input. Save your Groq API key in AI / API mode. Wake Listener keeps a microphone foreground service running so “Hey JARVIS” can work outside the app.",11,MUTED);hint.setPadding(0,dp(5),0,dp(4));c.addView(hint);
        voicePanel.addView(c);content.addView(voicePanel);\n        buildMicDiagnostic();
    }

    private void buildMicDiagnostic(){
        LinearLayout d=card();
        d.addView(label("MICROPHONE DIAGNOSTIC"));
        d.addView(tv("এখানে JARVIS-এর microphone capture, noise floor এবং Android audio effects সরাসরি পরীক্ষা করুন।",12,MUTED));
        micDiagnostic=tv("● READY\nPermission এবং microphone test শুরু করুন.",12,Color.WHITE);
        micDiagnostic.setGravity(Gravity.TOP);
        micDiagnostic.setPadding(0,dp(10),0,dp(10));
        d.addView(micDiagnostic,new LinearLayout.LayoutParams(-1,dp(150)));
        LinearLayout row=new LinearLayout(this);
        Button test=action("🎙  LIVE MIC TEST",Color.rgb(22,48,55));
        Button stop=action("■  STOP",Color.rgb(48,30,40));
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,dp(50),1);bp.setMargins(dp(3),0,dp(3),0);
        row.addView(test,bp);row.addView(stop,bp);d.addView(row);
        test.setOnClickListener(v->{
            if(micTest==null)micTest=new JarvisMicDiagnostic(this);
            String s=micTest.start((text,rms,peak,voice)->{
                if(micDiagnostic!=null)micDiagnostic.setText(text);
            });
            micDiagnostic.setText(s);
        });
        stop.setOnClickListener(v->{
            if(micTest!=null)micTest.stop();
            if(micDiagnostic!=null)micDiagnostic.setText("● TEST STOPPED");
        });
        TextView tip=tv("কথা বলার সময় RMS/Peak বাড়বে এবং VOICE DETECTED দেখাবে। Noise-এর মধ্যেও যদি RMS না বাড়ে, সমস্যা recognition নয়—microphone capture/source-এ।",11,MUTED);
        tip.setPadding(0,dp(7),0,0);d.addView(tip);
        content.addView(d);
    }

    private void buildPhoneControl(){
        phonePanel=new LinearLayout(this);phonePanel.setOrientation(LinearLayout.VERTICAL);
        LinearLayout c=card();c.addView(label("PHONE CONTROL MODE"));
        phoneState=tv("",12,MUTED);phoneState.setPadding(0,dp(3),0,dp(6));c.addView(phoneState);
        Button enable=action("⚡  ENABLE ACCESSIBILITY CONTROL",Color.rgb(32,28,48));add(c,enable,52);enable.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        c.addView(tv("Enable JARVIS Accessibility once. Then JARVIS can operate Home, Back, Recents, Notifications, Quick Settings and scrolling.",11,MUTED));
        LinearLayout r1=new LinearLayout(this);addPhoneButton(r1,"HOME",JarvisAction.Type.HOME);addPhoneButton(r1,"BACK",JarvisAction.Type.BACK);addPhoneButton(r1,"RECENTS",JarvisAction.Type.RECENTS);c.addView(r1);
        LinearLayout r2=new LinearLayout(this);addPhoneButton(r2,"NOTIFY",JarvisAction.Type.NOTIFICATIONS);addPhoneButton(r2,"QUICK",JarvisAction.Type.QUICK_SETTINGS);addPhoneButton(r2,"SCROLL ↓",JarvisAction.Type.SCROLL_DOWN);c.addView(r2);
        phonePanel.addView(c);content.addView(phonePanel);updatePhoneState();
    }

    private void addPhoneButton(LinearLayout row,String text,JarvisAction.Type type){Button b=action(text,Color.rgb(28,27,38));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(45),1);p.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(b,p);b.setOnClickListener(v->doPhone(type));}
    private void doPhone(JarvisAction.Type type){if(!JarvisAccessibilityService.isEnabled()){Toast.makeText(this,"আগে Accessibility-তে JARVIS চালু করুন।",Toast.LENGTH_LONG).show();startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));return;}String r=new JarvisActionExecutor(this).execute(new JarvisAction(type,null,false));Toast.makeText(this,r,Toast.LENGTH_SHORT).show();}
    private void updatePhoneState(){if(phoneState!=null)phoneState.setText(JarvisAccessibilityService.isEnabled()?"● PHONE CONTROL ACTIVE":"○ PHONE CONTROL OFF  •  Accessibility required");}
    private void updateWakeState(){if(wakeState!=null)wakeState.setText(JarvisWakeService.isRunning()?"● WAKE LISTENER ACTIVE":"○ WAKE LISTENER OFF");}

    private void showMode(String mode){
        generalPanel.setVisibility("GENERAL".equals(mode)?View.VISIBLE:View.GONE);
        apiPanel.setVisibility("AI / API".equals(mode)?View.VISIBLE:View.GONE);
        voicePanel.setVisibility("VOICE".equals(mode)?View.VISIBLE:View.GONE);\n        if(micPanel!=null)micPanel.setVisibility("VOICE".equals(mode)?View.VISIBLE:View.GONE);
        phonePanel.setVisibility("PHONE".equals(mode)?View.VISIBLE:View.GONE);
        modeTitle.setText(mode+" MODE");
        if("GENERAL".equals(mode))modeHint.setText("Language, memory and JARVIS behaviour.");
        else if("AI / API".equals(mode))modeHint.setText("Choose your AI provider and securely enter its API key.");
        else if("VOICE".equals(mode))modeHint.setText("Voice replies, wake listener, “Hey JARVIS” and microphone diagnostics.");
        else modeHint.setText("Accessibility-based control of your Android phone.");
        if("AI / API".equals(mode))refreshApiFields((LinearLayout)apiPanel.getChildAt(1));
    }

    private void saveAll(){
        putIf(gemini,"gemini");putIf(openai,"openai");putIf(groq,"groq");putIf(openrouter,"openrouter");putIf(deepseek,"deepseek");putIf(xai,"xai");putIf(custom,"custom");putText(openrouterModel,"openrouter_model");putText(deepseekModel,"deepseek_model");putText(xaiModel,"xai_model");putText(endpoint,"custom_endpoint");
        SecureStore.put(this,"primary",provider.getSelectedItem().toString());
        SecureStore.put(this,"speech_language",speechLanguage.getSelectedItemPosition()==1?"bn-BD":speechLanguage.getSelectedItemPosition()==2?"en-US":"AUTO");
        SecureStore.put(this,"auto_failover",String.valueOf(autoFailover.isChecked()));SecureStore.put(this,"voice_replies",String.valueOf(voiceReplies.isChecked()));SecureStore.put(this,"memory_enabled",String.valueOf(saveMemory.isChecked()));SecureStore.put(this,"far_voice",String.valueOf(farVoice.isChecked()));
        Toast.makeText(this,"✓ JARVIS configuration saved.",Toast.LENGTH_SHORT).show();
    }
    private void putIf(EditText e,String k){if(e!=null&&e.getText().length()>0)SecureStore.put(this,k,e.getText().toString());}
    private void putText(EditText e,String k){if(e!=null&&e.getText().length()>0)SecureStore.put(this,k,e.getText().toString());}
}