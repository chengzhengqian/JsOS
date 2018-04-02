package com.serendipity.chengzhengqian.jsos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Spannable;
import android.text.method.ScrollingMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.widget.Space;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.serendipity.chengzhengqian.jsos.GlobalState.ctx;

public class MainActivity extends Activity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("js-native");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GlobalState.currentActivity=this;
        jsLog =  findViewById(R.id.sample_text);
        setTextViewScrollable(jsLog);
        //runTutorials(); this will show some examples that use duktape api
    }
    public boolean ISAUTOMATICALLYSTARTSERVER=false;
    protected void onResume(){
        super.onResume();
        initJsCtx();
        registerState();
        if(ISAUTOMATICALLYSTARTSERVER)
            startServer();
        runTest();
    }
    protected void onPause(){
        super.onPause();
        delJsCtx();
        unRegisterState();
    }

    private void unRegisterState() {
        GlobalState.isUIRunning=false;
    }

    private void delJsCtx() {
        JsNative.destroyHeap(ctx);
    }


    private void startServer() {
        if(!GlobalState.isServerRunning) {
            Intent intent = new Intent(getBaseContext(), JsService.class);
            startService(intent);
            GlobalState.serverIndexHtml=getRawResource(R.raw.index);
            GlobalState.isServerRunning = true;
        }
    }
    public String getRawResource(int id){
        InputStream input= getResources().openRawResource(id);
        String content=(readFromInputStream(input));
        return content;
    }
    private String readFromInputStream(InputStream input) {
        BufferedReader reader=new BufferedReader(new InputStreamReader(input));
        String content="";
        StringBuilder builder=new StringBuilder();
        try {
            while ((content = reader.readLine()) != null) {
                builder.append(content + "\n");
            }
        }
        catch (Exception e)
        {
            GlobalState.printToLog(e.toString(),GlobalState.error);
        }
        return builder.toString();
    }
    private void stopServer() {
        if(GlobalState.isServerRunning) {
            Intent intent = new Intent(getBaseContext(), JsService.class);
            stopService(intent);
            GlobalState.isServerRunning = false;
        }
    }

    private void registerState() {
        GlobalState.isUIRunning=true;
    }

    private void initJsCtx() {
        ctx=JsNative.createHeapDefault();
        JsNative.registerJavaHandle(ctx);
        JsNative.registerProxyHandleGet(ctx);
        JsNative.registerJsObjectFinalizer(ctx);
        JsNative.registerFunctionHandle(ctx);
        JsNative.pushObject(ctx,new JsJava(this),JsJava.name);
    }

    public void addInput(){
        /* show the current input again*/
        if(currentInput.length()>0) {
            jsLog.append(currentInput);
            if(currentCaret==currentInput.length())
                jsLog.append(" ");
            Spannable spannableText= (Spannable) jsLog.getText();
            spannableText.setSpan(new BackgroundColorSpan(GlobalState.caretBackground),
                    outputIndex+currentCaret,outputIndex+currentCaret+1,0);
        }
    }

    private void updateInput() {
        setOnlyOutput();
        addInput();
    }
    private void setOnlyOutput(){
        /*remove the input first*/
        Spannable spannableText= (Spannable) jsLog.getText();
        jsLog.setText(spannableText.subSequence(0,outputIndex));
    }
    public void addLogWithColor( String text, int color) {
        setOnlyOutput();
        jsLog.append(text);
        int newOutputIndex = jsLog.getText().length();
        Spannable spannableText = (Spannable) jsLog.getText();
        spannableText.setSpan(
                new ForegroundColorSpan(color), outputIndex, newOutputIndex, 0);
        outputIndex=newOutputIndex;
        addInput();
    }

    TextView jsLog;
    public int maxLineNumbers=1000;
    private void setTextViewScrollable(TextView tv){
        tv.setMaxLines(maxLineNumbers);
        tv.setMovementMethod(new ScrollingMovementMethod());
    }
    /* to mimic a edit text, one should buffer the content and then put it to text
    * current implementation has flaw when the whole buffer is large and frequently take substring
    * */
    public StringBuilder currentInput=new StringBuilder();
    public int currentCaret=0;

    /**
     * v1: 6s for 20000
     * there are severa places to improves the code,
     * io,  (have not consider yet)
     * js side (improve)
     * java side
     */
    private void runTest(){
        jsLog.append(Utils.getCurrentTime()+" start test\n");
        JsNative.safeEval(ctx,
                "s=0; c=java.load(\"com.serendipity.chengzhengqian.jsos.TestClass\");\n" +
                "for(i=0;i<20000;i++)\n" +
                "{\n" +
                " \n" +
                " b=c.new(i)\n" +
                " s=s+b.intField\n" +
                "}\n" +
                "s"
                );
        jsLog.append(JsNative.safeToString(ctx,-1)+"\n");
        jsLog.append(Utils.getCurrentTime()+" start test\n");

    }
    private void runTutorials(){

        JsNativeExamples.init();
        jsLog.append((JsNativeExamples.tutorial1()));
        jsLog.append((JsNativeExamples.tutorial2()));
        jsLog.append((JsNativeExamples.tutorial3()));
        jsLog.append((JsNativeExamples.tutorial4()));
        jsLog.append((JsNativeExamples.tutorial5()));
        jsLog.append((JsNativeExamples.tutorial6()));
        jsLog.append((JsNativeExamples.tutorial7()));
        jsLog.append((JsNativeExamples.tutorial8()));
        jsLog.append((JsNativeExamples.tutorial9()));
        jsLog.append((JsNativeExamples.tutorial10()));
        jsLog.append((JsNativeExamples.tutorial11()));
        jsLog.append((JsNativeExamples.tutorial12()));
        JsNativeExamples.close();
    }
    public void addChar(char c){
        currentInput.insert(currentCaret,c);
        currentCaret+=1;
        updateInput();
    }
    public void addString(String s){
        currentInput.insert(currentCaret,s);
        currentCaret+=s.length();
        updateInput();
    }

    int outputIndex=0;

    public boolean onKeyDown(int keycode, KeyEvent event){
        if(event.isCtrlPressed()){
            if(keycode==KeyEvent.KEYCODE_B)
                return cursorLetf();
            if(keycode==KeyEvent.KEYCODE_F)
                return cursorRight();
            if(keycode==KeyEvent.KEYCODE_R)
                return runCode();

        }
        if(keycode>=KeyEvent.KEYCODE_A && keycode<=KeyEvent.KEYCODE_Z){
            char base='a';
            if(event.isShiftPressed()){
                base='A';
            }
            addChar((char) (((char)(keycode-KeyEvent.KEYCODE_A))+base));
            return true;
        }
        if(keycode==KeyEvent.KEYCODE_DEL){
            return deleteLeft();
        }
        if(!(keycode==KeyEvent.KEYCODE_CTRL_LEFT))
            addString("["+keycode+"]");

        return true;
    }
    private void emptyInput(){
        currentInput=new StringBuilder();
        currentCaret=0;
    }

    /**
     * execute code. Notice this must be execute in UI thread.
     * @return
     */
    private boolean runCode() {
        try {
            addLogWithColor(">>>" + currentInput.toString() + "\n", GlobalState.normal);
            JsNative.safeEval(ctx, currentInput.toString());
            String s=JsNative.safeToString(ctx, -1);
            if(s!=null)
                addLogWithColor( s+"\n", GlobalState.info);
            emptyInput();
        }
        catch (Exception e){
            addLogWithColor(e.toString()+"\n",GlobalState.error);
        }
        return true;
    }

    private boolean deleteLeft() {
        if(currentCaret>0) {
            currentInput.deleteCharAt(currentCaret - 1);
        }
        cursorLetf();
        return true;
    }

    private boolean cursorRight() {
        if(currentCaret<currentInput.length()){
            currentCaret+=1;
        }
        updateInput();
        return true;
    }

    private boolean cursorLetf() {
        if(currentCaret>0){
            currentCaret-=1;
        }
        updateInput();
        return true;
    }
}
