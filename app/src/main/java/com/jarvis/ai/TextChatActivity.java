package com.jarvis.ai;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.content.Intent;
import android.net.Uri;
import android.provider.OpenableColumns;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
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
    private JarvisCommandRouter router;
    private JarvisActionExecutor executor;
    private Button attachButton;
    private Uri attachedFileUri;
    private String attachedFileName="";
    private int BG=Color.rgb(10,9,14), CARD=Color.rgb(25,23,32), MUTED=Color.rgb(150,145,160), CYAN=Color.rgb(0,225,255);

    private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
    private GradientDrawable bg(int c,float r){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp(r));return g;}
    private TextView tv(String s,float z,int c){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);return t;}

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        engine=new JarvisEngine(this);
        router=new JarvisCommandRouter();
        executor=new JarvisActionExecutor(this);
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
        composer.setPadding(dp(10),dp(8),dp(10),dp(14));
        composer.setGravity(Gravity.CENTER_VERTICAL);
        composer.setBackgroundColor(BG);

        LinearLayout inputShell=new LinearLayout(this);
        inputShell.setOrientation(LinearLayout.HORIZONTAL);
        inputShell.setGravity(Gravity.CENTER_VERTICAL);
        inputShell.setPadding(dp(6),dp(6),dp(6),dp(6));
        inputShell.setBackground(bg(Color.rgb(28,27,35),28));

        Button attach=new Button(this);
        attach.setText("+");
        attach.setTextColor(Color.WHITE);
        attach.setTextSize(25);
        attach.setAllCaps(false);
        attach.setPadding(0,0,0,0);
        attach.setBackground(bg(Color.TRANSPARENT,22));
        inputShell.addView(attach,new LinearLayout.LayoutParams(dp(48),dp(48)));
        attachButton=attach;

        input=new EditText(this);
        input.setHint("Message JARVIS…");
        input.setHintTextColor(Color.rgb(135,132,145));
        input.setTextColor(Color.WHITE);
        input.setTextSize(16);
        input.setSingleLine(false);
        input.setMaxLines(5);
        input.setMinLines(1);
        input.setGravity(Gravity.CENTER_VERTICAL);
        input.setPadding(dp(8),0,dp(8),0);
        input.setBackgroundColor(Color.TRANSPARENT);
        inputShell.addView(input,new LinearLayout.LayoutParams(0,dp(52),1));

        Button send=new Button(this);
        send.setText("↑");
        send.setTextColor(Color.BLACK);
        send.setTextSize(23);
        send.setTypeface(null,1);
        send.setAllCaps(false);
        send.setPadding(0,0,0,0);
        send.setBackground(bg(CYAN,24));
        inputShell.addView(send,new LinearLayout.LayoutParams(dp(48),dp(48)));

        composer.addView(inputShell,new LinearLayout.LayoutParams(-1,-2));
        root.addView(composer);
        setContentView(root);

        addBubble("JARVIS","Text chat ready. লিখুন, আমি শুনছি না—এবার সরাসরি text-এ উত্তর দেব।",false);
        loadRecent();
        attach.setOnClickListener(v->pickFile());
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

    private boolean isWebsiteRequest(String q){
        String s=q.toLowerCase(java.util.Locale.ROOT);
        return s.contains("website")||s.contains("web site")||s.contains("ওয়েবসাইট")||s.contains("ওয়েবসাইট")
                ||s.contains("ওয়েব সাইট")||s.contains("ওয়েব সাইট")||s.contains("site বানাও")
                ||s.contains("সাইট বানাও")||s.contains("সাইট তৈরি")||s.contains("ওয়েব পেজ")||s.contains("ওয়েব পেজ");
    }

    private boolean isMessageAction(JarvisAction.Type t){
        return t==JarvisAction.Type.WHATSAPP_SEND||t==JarvisAction.Type.MESSENGER_SEND||t==JarvisAction.Type.SMS;
    }

    private void finishAction(String q,String answer){
        if(messages.getChildCount()>0)messages.removeViewAt(messages.getChildCount()-1);
        addBubble("JARVIS",answer,false);
        JarvisHistory.add(this,q,answer);
        if(!"false".equals(SecureStore.get(this,"voice_replies")))tts.speak(answer);
    }

private void sendMessage(){
        String q=input.getText().toString().trim();
        if(q.isEmpty())return;
        input.setText("");
        String fileContext="";
        String shownFileName=attachedFileName;
        if(attachedFileUri!=null){
            String content=readAttachedFile();
            if(!content.isEmpty()) fileContext="\n\n[Attached file: "+shownFileName+"]\n"+content;
            else fileContext="\n\n[Attached file: "+shownFileName+"] (content could not be read as text.)";
            clearAttachment();
        }
        addBubble("YOU",q+(shownFileName.isEmpty()?"":"\n📎 "+shownFileName),true);
        addBubble("JARVIS","Thinking…",false);

        if(isWebsiteRequest(q)){
            JarvisWebsiteBuilder builder=new JarvisWebsiteBuilder(this);
            new Thread(()->builder.buildAndPublish(q,message->runOnUiThread(()->{
                if(messages.getChildCount()>0)messages.removeViewAt(messages.getChildCount()-1);
                if(message.startsWith("WEBSITE_READY|")){
                    String out=message.substring("WEBSITE_READY|".length());
                    addBubble("JARVIS","✓ WEBSITE CREATED\n\n"+out,false);
                    JarvisHistory.add(this,q,out);
                    if(!"false".equals(SecureStore.get(this,"voice_replies")))tts.speak("Website তৈরি হয়েছে। লিংক text chat-এ দেওয়া হয়েছে।");
                }else{
                    String out=message.contains("|")?message.substring(message.indexOf("|")+1):message;
                    addBubble("JARVIS",out,false);
                    JarvisHistory.add(this,q,out);
                }
            }))).start();
            return;
        }

        JarvisAction action=router.route(q);
        if(action.type!=JarvisAction.Type.ASK_AI && !isMessageAction(action.type)){
            final JarvisAction directAction=action;
            new Thread(()->{
                String answer=executor.execute(directAction);
                runOnUiThread(()->finishAction(q,answer));
            }).start();
            return;
        }
        if(isMessageAction(action.type)){
            String[] parts=executor.parseMessageForVoice(action.value);
            if(parts[0].trim().isEmpty()||parts[1].trim().isEmpty()){
                finishAction(q,"Contact name এবং message দুটোই বলুন।");
                return;
            }
            java.util.List<JarvisActionExecutor.ContactCandidate> matches=executor.findContactCandidates(parts[0]);
            if(matches.size()>1){
                StringBuilder b=new StringBuilder("একই নামে একাধিক contact পেয়েছি। কোনজনকে message দেব? ");
                for(int i=0;i<matches.size();i++){
                    if(i>0)b.append(" অথবা ");
                    b.append(i+1).append(" নম্বর, ").append(matches.get(i).display());
                }
                finishAction(q,b.toString());
                return;
            }
            if(matches.size()==1){
                action=new JarvisAction(action.type,executor.messageCommandForContact(matches.get(0).name,parts[1]),true);
            }
            final JarvisAction confirmedAction=action;
            new android.app.AlertDialog.Builder(this)
                    .setTitle("JARVIS Confirmation")
                    .setMessage("এই contact-কে message পাঠানোর জন্য প্রস্তুত:\n"+parts[0]+"\n\n"+parts[1])
                    .setNegativeButton("Cancel",(d,w)->finishAction(q,"Message cancelled."))
                    .setPositiveButton("Confirm",(d,w)->{
                        String answer=executor.execute(confirmedAction);
                        finishAction(q,answer);
                    }).show();
            return;
        }

        final String finalPrompt=q+fileContext;
        final String finalQuestion=q;
        new Thread(()->{
            String answer=engine.ask(finalPrompt);
            if(answer==null||answer.trim().isEmpty())answer="দুঃখিত, এখন উত্তর দিতে পারছি না।";
            final String finalAnswer=answer;
            runOnUiThread(()->finishAction(finalQuestion,finalAnswer));
        }).start();
    }

    private void pickFile(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false);
        startActivityForResult(i,7001);
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode!=7001||resultCode!=RESULT_OK||data==null||data.getData()==null)return;
        attachedFileUri=data.getData();
        attachedFileName=getFileName(attachedFileUri);
        if(attachedFileName==null||attachedFileName.isEmpty())attachedFileName="selected_file";
        attachButton.setText("✓");
        attachButton.setTextColor(CYAN);
        input.setHint("File attached • Message JARVIS…");
    }

    private String getFileName(Uri uri){
        try{
            android.database.Cursor c=getContentResolver().query(uri,null,null,null,null);
            if(c!=null){
                int n=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if(c.moveToFirst()&&n>=0){String s=c.getString(n);c.close();return s;}
                c.close();
            }
        }catch(Exception ignored){}
        return uri!=null&&uri.getLastPathSegment()!=null?uri.getLastPathSegment():"";
    }

    private String readAttachedFile(){
        if(attachedFileUri==null)return "";
        InputStream in=null;
        try{
            in=getContentResolver().openInputStream(attachedFileUri);
            if(in==null)return "";
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            byte[] buf=new byte[8192];
            int total=0,n;
            while((n=in.read(buf))!=-1&&total<1024*1024){
                int take=Math.min(n,1024*1024-total);
                out.write(buf,0,take);
                total+=take;
                if(total>=1024*1024)break;
            }
            return new String(out.toByteArray(),java.nio.charset.StandardCharsets.UTF_8);
        }catch(Exception e){return "";}
        finally{try{if(in!=null)in.close();}catch(Exception ignored){}}
    }

    private void clearAttachment(){
        attachedFileUri=null;
        attachedFileName="";
        if(attachButton!=null){
            attachButton.setText("+");
            attachButton.setTextColor(Color.WHITE);
        }
        if(input!=null)input.setHint("Message JARVIS…");
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
