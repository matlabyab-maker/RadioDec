package com.example.kam;

import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.text.InputType;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.TypedValue;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class KeyboardService extends InputMethodService {
    private TextView sendButton;
    private boolean siteTextField = false;
    private boolean caps = false;
    private final StringBuilder history = new StringBuilder();
    private static final Set<String> BROWSERS = new HashSet<>(Arrays.asList(
        "com.android.chrome", "org.mozilla.firefox", "com.sec.android.app.sbrowser",
        "com.microsoft.emmx", "com.brave.browser", "com.opera.browser",
        "com.opera.mini.native", "com.kiwibrowser.browser", "com.yandex.browser",
        "com.google.android.googlequicksearchbox", "org.mozilla.fenix"
    ));
    private final int cream = Color.rgb(250,249,241);
    private final int ink = Color.rgb(40,53,69);

    @Override public View onCreateInputView() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(226,226,218));
        root.setPadding(dp(1),dp(1),dp(1),dp(1));

        LinearLayout top = row(root);
        String[] first = {"Paste","Copy","Copy\nScreen","Select","Undo","Redo","8\nHistory","Tool","Muse","Resize"};
        for (String label : first) {
            TextView b = key(label, label.equals("Select")||label.equals("Tool")||label.equals("Muse")?19:14, cream);
            b.setOnClickListener(v -> topAction(label));
            top.addView(b, params(0,dp(62),1));
        }

        LinearLayout second = row(root);
        for(int i=0;i<5;i++){ View spacer=new View(this); second.addView(spacer,params(0,dp(48),1)); }
        TextView up=key("↑",29,cream); up.setOnClickListener(v->moveCursor(-1)); second.addView(up,params(0,dp(48),.5f));
        TextView down=key("↓",29,cream); down.setOnClickListener(v->moveCursor(1)); second.addView(down,params(0,dp(48),.5f));
        TextView file=key("File",24,cream); file.setOnClickListener(v->pasteFromClipboard()); second.addView(file,params(0,dp(48),1));
        sendButton=key("Send",23,cream); sendButton.setTypeface(Typeface.DEFAULT_BOLD); sendButton.setOnClickListener(v->sendFromSite()); second.addView(sendButton,params(0,dp(48),1));

        LinearLayout nums=row(root);
        String[] num={"!","۱\n@","۲\n#","۳\n$","۴\n%","۵\n^","۶\n&","۷\n*","۸\n(","۹\n)","۰","⌫"};
        for(int i=0;i<num.length;i++){final String value=num[i]; final boolean isBackspace=(i==11); TextView k=key(value,18,isBackspace?Color.rgb(250,218,222):cream); k.setGravity(Gravity.CENTER); k.setOnClickListener(v->{if(isBackspace) deleteOne(); else typeText(value.contains("\n")?value.substring(0,value.indexOf('\n')):value);}); nums.addView(k,params(0,dp(40),isBackspace?1.45f:1));}

        addLetters(root,new String[]{"ض","ص","ث","ق","ف","غ","ع","ه","خ","ح","ج","⌫"},true);
        LinearLayout letters2=row(root);
        String[] r2={"ش","س","ی","ب","ل","ا","ت","ن","م","ک","گ","Enter"};
        for(String s:r2){TextView k=key(s,21,s.equals("Enter")?Color.rgb(211,230,255):cream); k.setOnClickListener(v->{if(s.equals("Enter")) typeText("\n"); else typeText(s);}); letters2.addView(k,params(0,dp(43),s.equals("Enter")?1.35f:1));}

        LinearLayout letters3=row(root);
        String[] r3={"Caps","ظ","ط","ژ","ز","ر","ذ","د","پ","و","چ","=","-"};
        for(String s:r3){TextView k=key(s,19,cream); k.setOnClickListener(v->{if(s.equals("Caps")){caps=!caps;k.setText(caps?"CAPS":"Caps");}else if(s.equals("="))typeText("=");else if(s.equals("-"))typeText("-");else typeText(s);}); letters3.addView(k,params(0,dp(40),s.equals("Caps")?1.2f:1));}

        LinearLayout bottom=row(root);
        String[] last={"😀","&@#\n*¥✓","اعراب","🌐 FA","Space","،",".","؟","⬅","➡","↑","↓","[","]"};
        for(String s:last){TextView k=key(s,s.equals("Space")?16:16,cream); k.setOnClickListener(v->{switch(s){case "Space":typeText(" ");break;case "⬅":moveCursor(-1);break;case "➡":moveCursor(1);break;case "↑":moveVertical(-1);break;case "↓":moveVertical(1);break;case "😀":typeText("😀");break;case "&@#\n*¥✓":typeText("&@#*¥✓");break;case "اعراب":typeText("ًٌٍَُِّْ");break;case "🌐 FA":typeText("");break;default:typeText(s);}}); float w=s.equals("Space")?2.5f:1; if(s.equals("🌐 FA"))w=1.3f; bottom.addView(k,params(0,dp(48),w));}
        refreshSendState(); return root;
    }

    private void topAction(String label){
        InputConnection ic=getCurrentInputConnection(); if(ic==null)return;
        switch(label){
            case "Paste": pasteFromClipboard(); break;
            case "Copy": ic.performContextMenuAction(android.R.id.copy); break;
            case "Copy\nScreen": ic.performContextMenuAction(android.R.id.copy); break;
            case "Select": ic.performContextMenuAction(android.R.id.selectAll); break;
            case "Undo": ic.sendKeyEvent(new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN,android.view.KeyEvent.KEYCODE_Z)); ic.sendKeyEvent(new android.view.KeyEvent(android.view.KeyEvent.ACTION_UP,android.view.KeyEvent.KEYCODE_Z)); break;
            case "Redo": break;
            case "8\nHistory": if(history.length()>0)typeText(history.toString()); break;
            case "Tool": break;
            case "Muse": break;
            case "Resize": break;
        }
    }
    private void addLetters(LinearLayout root,String[] arr,boolean back){LinearLayout r=row(root);for(int i=0;i<arr.length;i++){String s=arr[i];TextView k=key(s,21,s.equals("⌫")?Color.rgb(250,218,222):cream);k.setOnClickListener(v->{if(s.equals("⌫"))deleteOne();else typeText(s);});r.addView(k,params(0,dp(43),s.equals("⌫")?1.45f:1));}}
    private LinearLayout row(LinearLayout parent){LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.HORIZONTAL);parent.addView(r,new LinearLayout.LayoutParams(-1,-2));return r;}
    private LinearLayout.LayoutParams params(int width,int height,float weight){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(width,height,weight);p.setMargins(dp(1),dp(1),dp(1),dp(1));return p;}
    private TextView key(String text,int size,int color){TextView t=new TextView(this);t.setText(text);t.setTextSize(TypedValue.COMPLEX_UNIT_SP,size);t.setTextColor(ink);t.setGravity(Gravity.CENTER);t.setMaxLines(2);t.setIncludeFontPadding(true);GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(5));d.setStroke(dp(1),Color.rgb(210,210,202));t.setBackground(d);t.setPadding(dp(1),0,dp(1),0);return t;}
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
    private void typeText(String s){if(s==null||s.isEmpty())return;InputConnection ic=getCurrentInputConnection();if(ic!=null){String out=caps?s.toUpperCase():s;ic.commitText(out,1);history.append(out);if(history.length()>300)history.delete(0,history.length()-300);if(caps)caps=false;}}
    private void deleteOne(){InputConnection ic=getCurrentInputConnection();if(ic!=null)ic.deleteSurroundingText(1,0);}
    private void moveCursor(int n){InputConnection ic=getCurrentInputConnection();if(ic!=null){int code=n<0?android.view.KeyEvent.KEYCODE_DPAD_LEFT:android.view.KeyEvent.KEYCODE_DPAD_RIGHT;ic.sendKeyEvent(new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN,code));ic.sendKeyEvent(new android.view.KeyEvent(android.view.KeyEvent.ACTION_UP,code));}}
    private void moveVertical(int n){InputConnection ic=getCurrentInputConnection();if(ic!=null){int code=n<0?android.view.KeyEvent.KEYCODE_DPAD_UP:android.view.KeyEvent.KEYCODE_DPAD_DOWN;ic.sendKeyEvent(new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN,code));ic.sendKeyEvent(new android.view.KeyEvent(android.view.KeyEvent.ACTION_UP,code));}}
    private void pasteFromClipboard(){ClipboardManager cm=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);if(cm!=null&&cm.hasPrimaryClip()){ClipData c=cm.getPrimaryClip();if(c!=null&&c.getItemCount()>0){CharSequence t=c.getItemAt(0).coerceToText(this);if(t!=null)typeText(t.toString());}}}
    @Override public void onStartInputView(EditorInfo info,boolean restarting){super.onStartInputView(info,restarting);refreshSendState();}
    @Override public void onWindowShown(){super.onWindowShown();refreshSendState();}
    private void refreshSendState(){EditorInfo info=getCurrentInputEditorInfo();siteTextField=false;if(info!=null&&BROWSERS.contains(info.packageName)){int variation=info.inputType&InputType.TYPE_MASK_VARIATION;int cls=info.inputType&InputType.TYPE_MASK_CLASS;boolean text=cls==InputType.TYPE_CLASS_TEXT;boolean uri=variation==InputType.TYPE_TEXT_VARIATION_URI||variation==InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS;boolean password=variation==InputType.TYPE_TEXT_VARIATION_PASSWORD||variation==InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD||variation==InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD;siteTextField=text&&!uri&&!password;}
      if(sendButton!=null){sendButton.setEnabled(siteTextField);sendButton.setAlpha(siteTextField?1f:.35f);sendButton.setTextColor(siteTextField?ink:Color.GRAY);}}
    private void sendFromSite() {
        // This key is deliberately limited to ordinary text editors inside browser apps.
        // Never send a system-wide broadcast, launch another app, or submit a device form.
        refreshSendState();
        if (!siteTextField) return;

        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;

        EditorInfo info = getCurrentInputEditorInfo();
        if (info == null) return;

        // Honor an explicit action supplied by the website/editor (e.g. a chat editor
        // configured with "Send"). Do not invent IME_ACTION_SEND for unrelated fields.
        int action = info.imeOptions & EditorInfo.IME_MASK_ACTION;
        if (info.actionLabel != null && info.actionId != 0) {
            ic.performEditorAction(info.actionId);
            return;
        }
        if (action == EditorInfo.IME_ACTION_SEND) {
            ic.performEditorAction(EditorInfo.IME_ACTION_SEND);
            return;
        }

        // Web editors commonly handle Enter themselves when they don't expose a
        // dedicated IME action. Send Enter only to the currently focused site editor.
        android.view.KeyEvent down = new android.view.KeyEvent(
            android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER);
        android.view.KeyEvent up = new android.view.KeyEvent(
            android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_ENTER);
        ic.sendKeyEvent(down);
        ic.sendKeyEvent(up);
    }
}
