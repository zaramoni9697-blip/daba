package com.jarvis.ai;

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
    private LinearLayout root;
    private Spinner provider;
    private EditText gemini, openai, groq, openrouter, deepseek, xai, custom, endpoint;
    private EditText openrouterModel, deepseekModel, xaiModel;
    private Switch autoFailover, voiceReplies, saveMemory;
    private TextView wakeState, phoneState;
    private final String[] providers={"Gemini","OpenAI-compatible","Groq","OpenRouter","DeepSeek","Grok","Custom"};

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        build();
    }

    private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
    private TextView tv(String text,float size,int color){
        TextView t=new TextView(this); t.setText(text); t.setTextSize(size); t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL); return t;
    }
    private GradientDrawable bg(int color,float radius){
        GradientDrawable g=new GradientDrawable(); g.setColor(color); g.setCornerRadius(dp(radius)); return g;
    }
    private LinearLayout card(){
        LinearLayout c=new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(16),dp(14),dp(16),dp(14)); c.setBackground(bg(Color.rgb(20,19,28),20));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,dp(8),0,dp(8)); c.setLayoutParams(p); return c;
    }
    private EditText key(String hint,String store){
        EditText e=new EditText(this); e.setHint(hint); e.setHintTextColor(Color.rgb(125,121,135)); e.setTextColor(Color.WHITE);
        e.setSingleLine(true); e.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        e.setPadding(dp(12),0,dp(12),0); e.setBackground(bg(Color.rgb(11,10,16),14));
        String saved=SecureStore.get(this,store); if(saved!=null&&!saved.isEmpty()) e.setHint(hint+"  •  SAVED"); return e;
    }
    private EditText field(String hint,String store){
        EditText e=new EditText(this); e.setHint(hint); e.setHintTextColor(Color.rgb(125,121,135)); e.setTextColor(Color.WHITE);
        e.setSingleLine(true); e.setPadding(dp(12),0,dp(12),0); e.setBackground(bg(Color.rgb(11,10,16),14));
        String saved=SecureStore.get(this,store); if(saved!=null&&!saved.isEmpty()) e.setText(saved); return e;
    }
    private void addField(LinearLayout c,EditText e){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(50));p.setMargins(0,dp(6),0,0);c.addView(e,p);}
    private TextView section(String s){
        TextView t=tv(s,12,Color.rgb(155,149,175));t.setText(s.toUpperCase());t.setTypeface(null,1);t.setPadding(2,dp(8),0,dp(2));return t;
    }

    private void build(){
        ScrollView scroll=new ScrollView(this); scroll.setBackgroundColor(Color.rgb(11,10,15));
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(18),dp(12),dp(18),dp(28)); scroll.addView(root);
        setContentView(scroll);

        LinearLayout head=new LinearLayout(this); head.setGravity(Gravity.CENTER_VERTICAL);
        TextView back=tv("‹",38,Color.WHITE); back.setGravity(Gravity.CENTER); head.addView(back,new LinearLayout.LayoutParams(dp(48),dp(52)));
        LinearLayout titles=new LinearLayout(this); titles.setOrientation(LinearLayout.VERTICAL); titles.setPadding(dp(4),0,0,0);
        TextView h=tv("JARVIS",25,Color.WHITE);h.setTypeface(null,1);titles.addView(h);
        TextView sub=tv("CORE SETTINGS  •  SECURE",11,Color.rgb(0,225,255));titles.addView(sub);
        head.addView(titles,new LinearLayout.LayoutParams(0,-2,1));
        TextView dot=tv("● ONLINE",11,Color.rgb(0,225,255));head.addView(dot,new LinearLayout.LayoutParams(-2,dp(48)));
        root.addView(head); back.setOnClickListener(v->finish());

        LinearLayout status=card();
        TextView st=tv("AI CONTROL CENTER",13,Color.rgb(0,225,255));st.setTypeface(null,1);status.addView(st);
        TextView info=tv("Choose your primary brain. API keys stay encrypted on this device.",13,Color.rgb(190,186,200));info.setPadding(0,dp(5),0,dp(8));status.addView(info);
        provider=new Spinner(this); provider.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,providers));
        String saved=SecureStore.get(this,"primary");if(saved!=null)for(int i=0;i<providers.length;i++)if(providers[i].equals(saved))provider.setSelection(i);
        status.addView(provider,new LinearLayout.LayoutParams(-1,dp(48)));root.addView(status);

        LinearLayout keys=card();keys.addView(section("Encrypted API vault"));
        gemini=key("Gemini API key","gemini");addField(keys,gemini);
        openai=key("OpenAI API key","openai");addField(keys,openai);
        groq=key("Groq API key","groq");addField(keys,groq);
        openrouter=key("OpenRouter API key","openrouter");addField(keys,openrouter);
        deepseek=key("DeepSeek API key","deepseek");addField(keys,deepseek);
        xai=key("Grok / xAI API key","xai");addField(keys,xai);
        custom=key("Custom API key","custom");addField(keys,custom);
        endpoint=field("Custom OpenAI-compatible endpoint","custom_endpoint");addField(keys,endpoint);root.addView(keys);

        LinearLayout models=card();models.addView(section("Model routing"));
        openrouterModel=field("OpenRouter model","openrouter_model");addField(models,openrouterModel);
        deepseekModel=field("DeepSeek model","deepseek_model");addField(models,deepseekModel);
        xaiModel=field("Grok / xAI model","xai_model");addField(models,xaiModel);
        root.addView(models);

        LinearLayout automation=card();automation.addView(section("Automation"));
        autoFailover=new Switch(this);autoFailover.setText("Automatic AI failover");autoFailover.setTextColor(Color.WHITE);autoFailover.setTextSize(15);
        autoFailover.setChecked(!"false".equals(SecureStore.get(this,"auto_failover")));automation.addView(autoFailover);
        voiceReplies=new Switch(this);voiceReplies.setText("Voice replies / TTS");voiceReplies.setTextColor(Color.WHITE);voiceReplies.setTextSize(15);
        voiceReplies.setChecked(!"false".equals(SecureStore.get(this,"voice_replies")));automation.addView(voiceReplies);
        saveMemory=new Switch(this);saveMemory.setText("Conversation memory");saveMemory.setTextColor(Color.WHITE);saveMemory.setTextSize(15);
        saveMemory.setChecked(!"false".equals(SecureStore.get(this,"memory_enabled")));automation.addView(saveMemory);
        root.addView(automation);

        LinearLayout actions=card();actions.addView(section("Phone & system"));
        Button phone=new Button(this);phone.setText("⚡  PHONE CONTROL  •  ACCESSIBILITY");phone.setTextColor(Color.WHITE);phone.setBackground(bg(Color.rgb(32,28,48),16));actions.addView(phone,new LinearLayout.LayoutParams(-1,dp(52)));
        phoneState=tv("Accessibility permission can be enabled manually.",12,Color.rgb(145,140,155));phoneState.setPadding(0,dp(5),0,dp(5));actions.addView(phoneState);
        phone.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        Button wake=new Button(this);wake.setText("◉  WAKE LISTENER");wake.setTextColor(Color.WHITE);wake.setBackground(bg(Color.rgb(20,45,52),16));actions.addView(wake,new LinearLayout.LayoutParams(-1,dp(52)));
        wakeState=tv(JarvisWakeService.isRunning()?"Wake listener is ACTIVE":"Wake listener is OFF",12,Color.rgb(145,140,155));actions.addView(wakeState);
        wake.setOnClickListener(v->{if(JarvisWakeService.isRunning()){stopService(new Intent(this,JarvisWakeService.class));wakeState.setText("Wake listener is OFF");}else{startForegroundService(new Intent(this,JarvisWakeService.class));wakeState.setText("Wake listener is ACTIVE");}});
        root.addView(actions);

        LinearLayout memory=card();memory.addView(section("Maintenance"));
        Button clear=new Button(this);clear.setText("CLEAR LOCAL MEMORY");clear.setTextColor(Color.WHITE);clear.setBackground(bg(Color.rgb(55,25,35),16));memory.addView(clear,new LinearLayout.LayoutParams(-1,dp(52)));
        clear.setOnClickListener(v->{new AlertDialog.Builder(this).setTitle("Clear JARVIS memory?").setMessage("Conversation memory stored locally will be deleted.").setNegativeButton("Cancel",null).setPositiveButton("Clear",(d,w)->{new JarvisEngine(this).memory().clear();Toast.makeText(this,"Memory cleared.",Toast.LENGTH_SHORT).show();}).show();});
        Button save=new Button(this);save.setText("SAVE ALL SETTINGS");save.setTextColor(Color.BLACK);save.setBackground(bg(Color.rgb(0,225,255),16));LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(56));sp.setMargins(0,dp(10),0,0);memory.addView(save,sp);
        save.setOnClickListener(v->saveAll());
        root.addView(memory);
    }

    private void saveAll(){
        putIf(gemini,"gemini");putIf(openai,"openai");putIf(groq,"groq");putIf(openrouter,"openrouter");putIf(deepseek,"deepseek");putIf(xai,"xai");putIf(custom,"custom");
        putText(openrouterModel,"openrouter_model");putText(deepseekModel,"deepseek_model");putText(xaiModel,"xai_model");putText(endpoint,"custom_endpoint");
        SecureStore.put(this,"primary",provider.getSelectedItem().toString());
        SecureStore.put(this,"auto_failover",String.valueOf(autoFailover.isChecked()));
        SecureStore.put(this,"voice_replies",String.valueOf(voiceReplies.isChecked()));
        SecureStore.put(this,"memory_enabled",String.valueOf(saveMemory.isChecked()));
        Toast.makeText(this,"JARVIS settings saved securely.",Toast.LENGTH_SHORT).show();
    }
    private void putIf(EditText e,String key){if(e.getText().length()>0)SecureStore.put(this,key,e.getText().toString());}
    private void putText(EditText e,String key){if(e.getText().length()>0)SecureStore.put(this,key,e.getText().toString());}
}
