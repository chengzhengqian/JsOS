package com.serendipity.chengzhengqian.jsos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
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

        // Example of a call to a native method
        GlobalState.currentActivity=this;
        jsLog =  findViewById(R.id.sample_text);
        setTextViewScrollable(jsLog);
        //runTutorials();
    }
    protected void onResume(){
        super.onResume();
        initJsCtx();
        registerState();
        startServer();
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
    }


    public void addLogWithColor( String text, int color) {

        int start = jsLog.getText().length();
        jsLog.append(text);
        int end = jsLog.getText().length();

        Spannable spannableText = (Spannable) jsLog.getText();
        spannableText.setSpan(new ForegroundColorSpan(color), start, end, 0);
    }

    TextView jsLog;
    public int maxLineNumbers=1000;
    private void setTextViewScrollable(TextView tv){
        tv.setMaxLines(maxLineNumbers);
        tv.setMovementMethod(new ScrollingMovementMethod());
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


}
