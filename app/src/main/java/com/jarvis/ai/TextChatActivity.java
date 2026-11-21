package com.jarvis.ai;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;

public class TextChatActivity extends AppCompatActivity {
    private LinearLayout messages;
    private EditText input;
    private JarvisEngine engine;
    private JarvisTts tts;
    private int BG=Color.rgb(10,9,14), CARD=Color.rgb(25,23,32), MUTED=Color.rgb(150,145,160), CYAN=Color.rgb(0,225,255);

    private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
    private GradientDrawable bg(int c,float r){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp(r));return g;}
    private TextView tv(String s,float z,int c){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);return t;}

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        engine=new JarvisEngine(this);
        tts=new JarvisTts(this);

        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        LinearLayout head=new LinearLayout(this);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setPadding(dp(12),dp(8),dp(12),dp(8));
        TextView back=tv("‹",38,Color.WHITE);back.setGravity(Gravity.CENTER);
        head.addView(back,new LinearLayout.LayoutParams(dp(48),dp(54)));
        LinearLayout titleBox=new LinearLayout(this);titleBox.setOrientation(LinearLayout.VERTICAL);
        TextView title=tv("JARVIS",24,Color.WHITE);title.setTypeface(null,1);titleBox.addView(title);
        TextView sub=tv("TEXT CHAT",10,CYAN);sub.setTypeface(null,1);titleBox.addView(sub);
        head.addView(titleBox,new LinearLayout.LayoutParams(0,-2,1));
        TextView clear=tv("⌫",24,MUTED);clear.setGravity(Gravity.CENTER);
        head.addView(clear,new LinearLayout.LayoutParams(dp(48),dp(54)));
        root.addView(head);
        back.setOnClickListener(v->finish());
        clear.setOnClickListener(v->{JarvisHistory.clear(this);messages.removeAllViews();addBubble("JARVIS","New chat started.",false);});

        ScrollView scroll=new ScrollView(this);
        messages=new LinearLayout(this);messages.setOrientation(LinearLayout.VERTICAL);messages.setPadding(dp(12),dp(10),dp(12),dp(12));
        scroll.addView(messages);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout composer=new LinearLayout(this);
        composer.setPadding(dp(10),dp(8),dp(10),dp(12));composer.setGravity(Gravity.CENTER_VERTICAL);
        input=new EditText(this);input.setHint("Message JARVIS…");input.setHintTextColor(MUTED);input.setTextColor(Color.WHITE);
        input.setTextSize(15);input.setSingleLine(false);input.setMaxLines(4);input.setPadding(dp(16),0,dp(16),0);input.setBackground(bg(CARD,24));
        composer.addView(input,new LinearLayout.LayoutParams(0,dp(56),1));
        Button send=new Button(this);send.setText("➤");send.setTextColor(Color.BLACK);send.setTextSize(18);send.setAllCaps(false);send.setBackground(bg(CYAN,24));
        LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(dp(56),dp(56));sp.setMargins(dp(8),0,0,0);composer.addView(send,sp);
        root.addView(composer);
        setContentView(root);

        addBubble("JARVIS","Text chat ready. লিখুন, আমি শুনছি না—এবার সরাসরি text-এ উত্তর দেব।",false);
        loadRecent();
        send.setOnClickListener(v->sendMessage());
        input.setOnEditorActionListener((v,a,e)->{if(e!=null){sendMessage();return true;}return false;});
    }

    private void loadRecent(){
        JSONArray a=JarvisHistory.read(this);
        int start=Math.max(0,a.length()-20);
        for(int i=a.length()-1;i>=start;i--){
            try{
                JSONObject o=a.getJSONObject(i);
                String u=o.optString("user","");
                String r=o.optString("assistant","");
                if(!u.isEmpty()) addBubble("YOU",u,true);
                if(!r.isEmpty()) addBubble("JARVIS",r,false);
            }catch(Exception ignored){}
        }
    }

    private void sendMessage(){
        String q=input.getText().toString().trim();
        if(q.isEmpty())return;
        input.setText("");
        addBubble("YOU",q,true);
        addBubble("JARVIS","Thinking…",false);
        new Thread(()->{
            String answer=engine.ask(q);
            if(answer==null||answer.trim().isEmpty())answer="দুঃখিত, এখন উত্তর দিতে পারছি না।";
            final String finalAnswer=answer;
            runOnUiThread(()->{
                if(messages.getChildCount()>0)messages.removeViewAt(messages.getChildCount()-1);
                addBubble("JARVIS",finalAnswer,false);
                if(!"false".equals(SecureStore.get(this,"voice_replies")))tts.speak(finalAnswer);
            });
        }).start();
    }

    private void addBubble(String who,String text,boolean user){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(14),dp(10),dp(14),dp(10));box.setBackground(bg(user?Color.rgb(30,29,39):Color.rgb(20,19,27),20));
        TextView label=tv(who,10,user?Color.WHITE:CYAN);label.setTypeface(null,1);box.addView(label);
        TextView body=tv(text,15,Color.WHITE);body.setPadding(0,dp(4),0,0);box.addView(body);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(5),0,dp(5));box.setLayoutParams(p);
        messages.addView(box);
    }

    @Override protected void onDestroy(){if(tts!=null)tts.shutdown();super.onDestroy();}
}
