package com.serendipity.chengzhengqian.jsos;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

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
        tv=  findViewById(R.id.sample_text);
        setTextViewScrollable(tv);
        runTutorials();
    }
    TextView tv;
    public int maxLineNumbers=1000;
    private void setTextViewScrollable(TextView tv){
        tv.setMaxLines(maxLineNumbers);
        tv.setMovementMethod(new ScrollingMovementMethod());
    }
    private void runTutorials(){

        JsNativeExamples.init();
        tv.append((JsNativeExamples.tutorial1()));
        tv.append((JsNativeExamples.tutorial2()));
        tv.append((JsNativeExamples.tutorial3()));
        tv.append((JsNativeExamples.tutorial4()));
        tv.append((JsNativeExamples.tutorial5()));
        tv.append((JsNativeExamples.tutorial6()));
        tv.append((JsNativeExamples.tutorial7()));
        tv.append((JsNativeExamples.tutorial8()));
        tv.append((JsNativeExamples.tutorial9()));
        tv.append((JsNativeExamples.tutorial10()));
        tv.append((JsNativeExamples.tutorial11()));
        tv.append((JsNativeExamples.tutorial12()));
        JsNativeExamples.close();
    }


}
