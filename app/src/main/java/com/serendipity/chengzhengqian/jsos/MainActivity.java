package com.serendipity.chengzhengqian.jsos;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Guideline;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;

import static com.serendipity.chengzhengqian.jsos.GlobalState.ctx;

public class MainActivity extends Activity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("js-native");
    }
    ConstraintLayout mainCL;
    private String runCommand="##run";
    private boolean markon =false;

    /*
    orientation 0; vertical , others: horizontal
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private Guideline createGuideLine(int orientation, float ratio, ViewGroup parent){
        Guideline gl=new Guideline(this);
        ConstraintLayout.LayoutParams p;
        p =new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT);
        if(orientation==0) {
            p.orientation = ConstraintLayout.LayoutParams.VERTICAL;
        }
        else{
            p.orientation = ConstraintLayout.LayoutParams.HORIZONTAL;
        }
        p.guidePercent=ratio;
        gl.setLayoutParams(p);
        int id=View.generateViewId();
        gl.setId(id);
        parent.addView(gl);
        return gl;
    }

    class MultiButton extends Button{
        //name to show
        //code internal symbol (inital apparence)
        public MultiButton(Context context, String name, String code) {
            super(context);
            this.name=name; this.code=code;
        }
        public void showHints(String pressedCode){
            String command=pressedCode+code;
            if(shapeInputMap.containsKey(command)){
                this.setText(shapeInputMap.get(command));
                this.setTextColor(hintKeyColor);
            }

        }
        public void hideHints(){
            this.setText(name);
            this.setTextColor(normalKeyColor);
        }
        String name;
        String code;
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private Button createButton(String name,String code,Guideline leftId, Guideline  rightId,
                                Guideline topId, Guideline bottomId){
        ConstraintLayout.LayoutParams p=
                new ConstraintLayout.LayoutParams(
                        //ViewGroup.LayoutParams.WRAP_CONTENT,
                        0 ,
                        //ViewGroup.LayoutParams.MATCH_PARENT,
                        0);
        p.leftToLeft=leftId.getId();
        p.rightToRight=rightId.getId();
        p.topToTop=topId.getId();
        p.bottomToBottom=bottomId.getBottom();

        Button b=new MultiButton(this,name,code);
        b.setBackgroundColor(Color.TRANSPARENT);
        b.setTextColor(normalKeyColor);
        b.setLayoutParams(p);
        b.setText(name);
        b.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return touchHandle(view,motionEvent);
            }
        });
        code=codes.get(name);
        if(code.equals(shift)){
            shiftKey=b;
        }
        else if(code.equals(ctrl)){
            ctrlKey=b;
        }
        else if(code.equals(alt)){
            altKey=b;
        }
        else if(code.equals(meta)){
            metaKey=b;
        }

        return b;
    }
    public static String shift=",";
    public static String ctrl =".";
    public static String alt ="@";
    public static String meta ="#";
    public static String backspace="<";
    public static String enter="+";
    public static boolean isShiftOn=false;
    public static boolean isMetaOn=false;
    public static boolean isCtrOn=false;
    public static boolean isAltOn=false;
    public static boolean isShiftUsed=false;
    public static boolean isMetaUsed=false;
    public static boolean isCtrUsed=false;
    public static boolean isAltUsed=false;
    public static int normalKeyColor=Color.DKGRAY;
    private int hintKeyColor=Color.GRAY;
    public static int specialOnKeyColor=Color.GREEN;
    public void toggleMeta(){
        if(metaKey!=null){
            if(isMetaOn)
                metaKey.setTextColor(normalKeyColor);
            else
                metaKey.setTextColor(specialOnKeyColor);
            isMetaOn=!isMetaOn;
        }

    }
    public void toggleShift(){
        if(shiftKey!=null){
            if(isShiftOn)
                shiftKey.setTextColor(normalKeyColor);
            else
                shiftKey.setTextColor(specialOnKeyColor);
            isShiftOn=!isShiftOn;
        }

    }
    public void toggleAlt(){
        if(altKey!=null){
            if(isAltOn)
                altKey.setTextColor(normalKeyColor);
            else
                altKey.setTextColor(specialOnKeyColor);
            isAltOn=!isAltOn;
        }

    }
    public void toggleCtr() {
        if (ctrlKey != null) {
            if (isCtrOn)
                ctrlKey.setTextColor(normalKeyColor);
            else
                ctrlKey.setTextColor(specialOnKeyColor);
            isCtrOn = !isCtrOn;
        }

    }
    /*when usr pressed two keys, it is either valid or not, this number indicates the inserted
    * number of character in a session*/

    public int currentInsertNumber=0;
    public HashMap<String, String> correction=new HashMap<>();

    public boolean touchHandle(View view, MotionEvent event){
        MultiButton b = (MultiButton) view;
        String code = b.code;
        if(event.getAction()==MotionEvent.ACTION_MOVE){
//            GlobalState.printToLog("enter "+code+":"
//                    +event.getX()+","+event.getY()+"\n",GlobalState.info);
            if(keyPressed.keySet().size()==1){
                if(keyPressed.containsKey(enter)){
                    //addString("\n");
                    PressedPoint p=keyPressed.get(enter);
                    if(p.step(event.getX()-p.x,1)>0){
                        cursorRight();
                        keyPressed.put(enter,new PressedPoint(event.getX(),event.getY()));
                    }
                    if(p.step(event.getX()-p.x,1)<0){
                        cursorLeft();
                        keyPressed.put(enter,new PressedPoint(event.getX(),event.getY()));
                    }
                    if(p.step(event.getY()-p.y,2)>0){
                        cursorDown();
                        keyPressed.put(enter,new PressedPoint(event.getX(),event.getY()));
                    }
                    if(p.step(event.getY()-p.y,2)<0){
                        cursorUp();
                        keyPressed.put(enter,new PressedPoint(event.getX(),event.getY()));
                    }

                }
                if(keyPressed.containsKey(backspace)){
                    deleteLeft();
                }
            }
            if(keyPressed.keySet().size()>=2){
                if(keyPressed.containsKey(code)){
                    PressedPoint p=keyPressed.get(code);
                    String newCode=p.isUpdated(event.getX(),event.getY(),code);
                    if(!newCode.equals("")) {
                        correction.put(code,newCode);
                    }
                    else{
                        correction.put(code,code);
                    }

//                         GlobalState.printToLog(code + "->" + newCode + "\n",
//                                GlobalState.info);
                    String command="";
                    for(String s:keyPressed.keySet()){
                        if(correction.containsKey(s))
                            command+=correction.get(s);
                        else
                            command+=s;
                    }
                    if(shapeInputMap.containsKey(command)) {
//                            GlobalState.printToLog("correction: "+command+"\n",
//                                    GlobalState.info);
                            deleteLeft(currentInsertNumber);
                            currentInsertNumber=handleInput(shapeInputMap.get(command),
                                    isShiftOn,isCtrOn, isAltOn,isMetaOn,true);
                    }
                    else{
                        GlobalState.printToLog("unknown correction: "+command+"\n",
                                GlobalState.info);
                    }


                }
            }
        }
        if(event.getAction()==MotionEvent.ACTION_DOWN) {
            /*orginally, there is indeed a order to tell which key is, but this
            * turns out quite difficult to use, in practice, all code is defined
            * so that order does not matter*/
            if (!keyPressed.keySet().contains(code)) {
                keyPressed.put(code,new PressedPoint(event.getX(),event.getY()));
            }
            /* when we simutanousely have two keys, we can deal with the combination*/

            int N_key=2;
            if (keyPressed.size() >= N_key) {
                String command="";
                for (String s : keyPressed.keySet()) {
                    command+=s;
                    if(s.equals(shift))  isShiftUsed = true;
                    else if(s.equals(ctrl))isCtrUsed=true;
                    else if(s.equals(alt))isAltUsed=true;
                    else if(s.equals(meta))isMetaUsed=true;
                }
                if(shapeInputMap.containsKey(command)) {
                   currentInsertNumber=handleInput(shapeInputMap.get(command),
                           isShiftOn,isCtrOn, isAltOn, isMetaOn, false);
                }
                else
                    addLogWithColor("undefined: "+(command) + "\n", GlobalState.info);

            }
            /*deal with special keys*/
            if(keyPressed.keySet().size()==1) {
                if (keyPressed.keySet().contains(backspace)) {
                    deleteLeft();
                }
                else{
                    showHints(code);
                }
            }
        }
        if(event.getAction()==MotionEvent.ACTION_UP){
            keyPressed.remove(code);
            if(keyPressed.keySet().size()==0){
                hideHints();
            }
            if(code.equals(shift)) {
                if(!isShiftUsed)
                    toggleShift();
                isShiftUsed=false;
            }
            else if(code.equals(ctrl)) {
                if(!isCtrUsed)
                    toggleCtr();
                isCtrUsed=false;
            }
            else if(code.equals(alt)) {
                if(!isAltUsed)
                    toggleAlt();
                isAltUsed=false;
            }
            else if(code.equals(meta)) {
                if(!isMetaUsed)
                    toggleMeta();
                isMetaUsed=false;
            }
            if(keyPressed.keySet().size()<2){
                currentInsertNumber=0;//end input session.
                addString(automatic);
                cursorLeft(automatic.length());
                automatic="";
            }

        }

        return false;
    }
    private void showHints(String code){
        for(int i=0;i<mainCL.getChildCount();i++){
            View v=mainCL.getChildAt(i);
            if(v instanceof MultiButton){
                ((MultiButton) v).showHints(code);
            }
        }
    }

    private void hideHints(){
        for(int i=0;i<mainCL.getChildCount();i++){
            View v=mainCL.getChildAt(i);
            if(v instanceof MultiButton){
                ((MultiButton) v).hideHints();
            }
        }
    }
    //return the number of added characters
    private int handleInput(String s,
                            boolean isShiftOn, boolean isCtrOn, boolean isAltOn, boolean isMetaOn, boolean IsEdit) {
        if(isShiftOn){
            s=s.toUpperCase();
        }
        if (isCtrOn) {
            if(!IsEdit) {
                if (s.equals("f"))
                    cursorRight();
                else if (s.equals("b"))
                    cursorLeft();
                else if (s.equals("r"))
                    runCode();
            }
            return 0;
        }


        if (s.equals(runCommand)) {
            if(!IsEdit)
                runCode();
            return 0;
        }
        addString(s);
        return s.length();
    }
    public static float MoveCorrectionThred=100;
    private class PressedPoint{
        float x;
        float y;
        PressedPoint(float x, float y){
            this.x=x; this.y=y;
        }
        private int sign(float x){
            if(x>MoveCorrectionThred*4) return 2;
            else if(x>MoveCorrectionThred/2) return 1;
            else if(x<-MoveCorrectionThred*4) return -2;
            else if(x<-MoveCorrectionThred/2) return -1;
            else return 0;
        }

        /**
         * 5 vertical 1 horizontal
         * @param x
         * @param type
         * @return
         */
        private int step(float x,int type){
            return (int) (x/15)/type;
        }
        String isUpdated(float new_x, float new_y, String code){
            if(Math.abs(new_x-x)+Math.abs(new_y-y)>MoveCorrectionThred){
                int dx=0; int dy=0;

                dx=sign(new_x-x);

                dy=sign(new_y-y);
                KeyPosition kp=codePositions.get(code);
                if(kp!=null)
                    for(String newcode: codePositions.keySet()){
                    KeyPosition newkp=codePositions.get(newcode);
                    if((newkp.x-kp.x)==dx&&(newkp.y-kp.y)==dy&&(newkp.part==kp.part))
                        return newcode;
                    }

                //return String.format("%d,%d",dx,dy);
            }

            return "";
        }
    }
    public static HashMap<String, PressedPoint> keyPressed=new HashMap<>();

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void initUI(){
        initMainUI();
        initLog();
        initKeys();
        initShapeInputMap();
        buildKeys();
    }
    private void initMainUI(){
        mainCL=findViewById(R.id.main);
    }
    private void initLog(){
        jsLog =  findViewById(R.id.sample_text);
        setTextViewScrollable(jsLog);
        currentInput.append("function test(x,y)\n{\na=123;\nb=123;\nreturn a+b;}\n");
        updateUI();
    }
    class KeyPosition {
        int x; //0,1, ->
        int y; // 0, 1, 2 down
        int part; // 0, left, 1, right
        KeyPosition(int x, int y, int part){
            this.x=x; this.y=y; this.part=part;
        }
    }
    public static HashMap<String, KeyPosition> positions=new HashMap<String, KeyPosition>();
    public static HashMap<String, KeyPosition> codePositions=new HashMap<String, KeyPosition>();
    // name (appear in button with space mark) code a single char
    public static HashMap<String , String> codes=new HashMap<>();
    public static HashMap<String, String> shapeInputMap=new HashMap<>();
    public Button shiftKey=null;
    public Button metaKey=null;
    public Button ctrlKey=null;
    public Button altKey=null;

    private void addKey(String c, int x, int y, int part){
        String mark="               ";
        String o=String.format(mark+"\n%s\n"+mark,c);
        //String o=c;
        KeyPosition op=new KeyPosition(x,y,part);
        positions.put(o,op);
        codes.put(o,c);
        codePositions.put(c,op);
    }
    private void initKeys(){
        positions.clear();codes.clear();
        addKey("\\",0,0,0);
        addKey("|",0,1,0);
        addKey("j",0,2,0);

        addKey("/",1,0,0);
        addKey("-",1,1,0);
        addKey(ctrl,1,2,0);

        addKey(">",0,0,1);
        addKey("c",0,1,1);
        addKey(shift,0,2,1);

        addKey("u",1,0,1);
        addKey("o",1,1,1);
        addKey("n",1,2,1);

        addKey(backspace,2,0,2);
        addKey(enter,1,0,2);
        addKey(meta,0,0,2);
        addKey(alt,3,0,2);
    }
    private void addShapeInput(String a, String b, String result){
        shapeInputMap.put(a+b,result);
        shapeInputMap.put(b+a,result);
    }
    private void initShapeInputMap(){
        shapeInputMap.clear();
        String[][] lists=new String[][]{
                new String[]{"c","\\","a"},
                new String[]{"\\","o","b"},
                new String[]{"c","j","c"},
                new String[]{"o","/","d"},
                new String[]{"c","-","e"},
                new String[]{"/",">","f"},
                new String[]{"o","j","g"},
                new String[]{"|","n","h"},
                new String[]{"j","n","i"},
                new String[]{"j",">","j"},
                new String[]{"|","c","k"},
                new String[]{"|","u","l"},
                new String[]{"n","\\","m"},
                new String[]{"n","-","n"},
                new String[]{"o","-","o"},
                new String[]{"|","o","p"},
                new String[]{"n","/","q"},
                new String[]{">","-","r"},
                new String[]{"/","c","s"},
                new String[]{"|",">","t"},
                new String[]{"u","-","u"},
                new String[]{"\\","u","v"},
                new String[]{"u","/","w"},
                new String[]{"\\","/","x"},
                new String[]{"\\",alt,"x"},
                new String[]{"u","j","y"},
                new String[]{"\\",">","z"},
                new String[]{shift, ctrl," "},
                new String[]{ctrl,alt,"."},
                new String[]{meta,shift,","},
                new String[]{meta,alt,runCommand},
                
                new String[]{shift,"\\","1"},
                new String[]{shift,"/","2"},
                new String[]{shift,"|","3"},
                new String[]{shift,"-","4"},
                new String[]{shift,"j","5"},

                new String[]{ctrl,">","6"},
                new String[]{ctrl,"u","7"},
                new String[]{ctrl,"c","8"},
                new String[]{ctrl,"o","9"},
                new String[]{ctrl,"n","0"},
                
                new String[]{alt,"/","/"},
                new String[]{alt,"|","+"},
                new String[]{alt,"-","-"},
                new String[]{alt,"j","*"},


                new String[]{meta,">",">"},
                new String[]{meta,"u","<"},
                new String[]{meta,"c","="},
                new String[]{meta,"o","("},
                new String[]{meta,"n","["},

                new String[]{"n",enter,"\n"},
        };

        // so beautiful the design
        for(String[] s: lists){
            addShapeInput(s[0],s[1],s[2]);
        }

    }

    private void buildKeys(){
        /*create guidline*/
        float w= (float) 0.25;
        float hStart= (float) -0.6;
        float h= (float) 0.3;


        Guideline[] pv=new Guideline[3];
        Guideline[] pv_=new Guideline[3];
        Guideline[] ph=new Guideline[5];
        Guideline[] pv_additional=new Guideline[5];
        Guideline[] ph_additional=new Guideline[2];


        for(int i=0;i<3;i++){
            pv[i]=createGuideLine(0, (float) i*w,mainCL);
            pv_[i]=createGuideLine(0, (float) 1-(2-i)*w,mainCL);
        }

        for(int i=0;i<5;i++){
            ph[i]=createGuideLine(1, (float) hStart+i*h,mainCL);
            pv_additional[i]=createGuideLine(0, (float) i*w,mainCL);
        }
        for(int i=3;i<5;i++){
            ph_additional[i-3]=createGuideLine(1, (float) hStart+i*h,mainCL);
        }

        Guideline[] pv_temp=null;
        Guideline[] ph_temp=null;
        for(String name: positions.keySet()){
            KeyPosition p=positions.get(name);
            if(p.part==0){ pv_temp=pv; ph_temp=ph;}
            else if((p).part==1) { pv_temp=pv_; ph_temp=ph;}
            //else pv_temp=pv_center;
            else {
                pv_temp=pv_additional;
                ph_temp=ph_additional;
            }
            mainCL.addView(createButton(name,codes.get(name),pv_temp[p.x],pv_temp[p.x+1],
                    ph_temp[p.y],ph_temp[p.y+1]));
        }


    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GlobalState.currentActivity=this;

        initUI();
        initJsCtx();
        if(ISAUTOMATICALLYSTARTSERVER)
            startServer();
        //runTutorials(); this will show some examples that use duktape api
    }

    protected void onDestroy(){
        super.onDestroy();
        delJsCtx();
    }
    public boolean ISAUTOMATICALLYSTARTSERVER=true;
    protected void onResume(){
        super.onResume();
        registerState();
//        runTest();
//        runTest2();
    }
    protected void onPause(){
        super.onPause();
        unRegisterState();
    }

    private void unRegisterState() {
        GlobalState.isUIRunning=false;
    }

    private void delJsCtx()
    {
        JsNative.releaseJavaHandle(ctx);
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
        JsNative.registerProxyHandleSet(ctx);
        JsNative.registerJsObjectFinalizer(ctx);
        JsNative.registerJsObejctProperties(ctx);
        JsNative.registerFunctionHandle(ctx);
        JsNative.pushObject(ctx,new JsJava(this),JsJava.name);
    }
    String modelLine="------------------cursor:%d------------------\n";
    public void updateUI(){
        /* show the current input again*/
        SpannableStringBuilder sb=new SpannableStringBuilder();
        sb.append(output);
        sb.append(String.format(modelLine,currentCaret));
        sb.append(currentInput);
        if(currentCaret==currentInput.length()){
            sb.append(" ");
            sb.setSpan(new BackgroundColorSpan(GlobalState.caretBackground),
                    sb.length()-1,sb.length(),0
                    );
        }

        jsLog.setText(sb);

    }

    private void updateInput() {
        updateUI();
    }
    SpannableStringBuilder output=new SpannableStringBuilder();

    public void addLogWithColor( String text, int color) {
//        setOnlyOutput();
//        jsLog.append(text);
//        int newOutputIndex = jsLog.getText().length();
//        Spannable spannableText = (Spannable) jsLog.getText();
//        spannableText.setSpan(
//                new ForegroundColorSpan(color), outputIndex, newOutputIndex, 0);
//        outputIndex=newOutputIndex;
//
        output.append(text);
        int newOutputIndex = output.length();
        output.setSpan(new ForegroundColorSpan(color),
                outputIndex, newOutputIndex, 0);
        outputIndex=newOutputIndex;
        updateUI();
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
    public SpannableStringBuilder currentInput=new SpannableStringBuilder();
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
                "for(i=0;i<4000;i++)\n" +
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
    private void runTest2(){
        jsLog.append(Utils.getCurrentTime()+" start test\n");
        JsNative.safeEval(ctx,
                "s=0; c=java.load(\"com.serendipity.chengzhengqian.jsos.TestClass\");\n" +
                        "for(i=0;i<4000;i++)\n" +
                        "{\n" +
                        " \n" +
                        " b=c.new(i)\n" +
                        " s=s+b.intMethod();\n" +
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
        if(c=='(')
        {
            addString("()");
            cursorLeft();
            return;
        }

        currentInput.insert(currentCaret,String.valueOf(c));

        currentCaret+=1;
        setSelected();
        updateInput();
    }
    String automatic="";
    public void addString(String s){
        currentInput.insert(currentCaret,s);
        currentCaret+=s.length();
        if(s.equals("(")){
            automatic=")";
        }
        else if(s.equals("[")){
            automatic="]";
        }
        else if(s.equals("{")){
            automatic="{";
        }
        setSelected();
        updateInput();
    }

    int outputIndex=0;

    public boolean onKeyUp(int keycode, KeyEvent event){
        if(keycode==KeyEvent.KEYCODE_DPAD_RIGHT)
            isEffectiveCtrlOn=false;
        return true;
    }
    boolean isEffectiveCtrlOn=false;
    public boolean onKeyDown(int keycode, KeyEvent event){
        if(keycode==KeyEvent.KEYCODE_DPAD_RIGHT)
            isEffectiveCtrlOn=true;
        else if(event.isCtrlPressed()||isEffectiveCtrlOn){
            if(keycode==KeyEvent.KEYCODE_B)
                return cursorLeft();
            else if(keycode==KeyEvent.KEYCODE_F)
                return cursorRight();
            else if(keycode==KeyEvent.KEYCODE_R)
                return runCode();
            else if(keycode==KeyEvent.KEYCODE_M)
                return setMark();
            else if(keycode==KeyEvent.KEYCODE_P)
                return cursorUp();
            else if(keycode==KeyEvent.KEYCODE_N)
                return cursorDown();
            else if(keycode==KeyEvent.KEYCODE_W)
                return cut();
            else if(keycode==KeyEvent.KEYCODE_Y)
                return paste();
            else if(keycode==KeyEvent.KEYCODE_E)
                return endOfLine();

        }
        else if(keycode>=KeyEvent.KEYCODE_A && keycode<=KeyEvent.KEYCODE_Z){
            char base='a';
            if(event.isShiftPressed()){
                base='A';
            }
            addChar((char) (((char)(keycode-KeyEvent.KEYCODE_A))+base));
            return true;
        }
        else if(keycode>=KeyEvent.KEYCODE_0&&keycode<=KeyEvent.KEYCODE_9){
            int i=keycode-KeyEvent.KEYCODE_0;
            String number="0123456789";
            String shirtNumber=")!@#$%^&*(";
            if(event.isShiftPressed()){
                addChar(shirtNumber.charAt(i));
            }
            else
                addChar(number.charAt(i));
            return true;
        }
        else if(keycode==KeyEvent.KEYCODE_PERIOD){
            addChar('.');
            //addLogWithColor(getCurrentVariableHint()+"\n",GlobalState.info);
            return true;
        }
        else if(keycode==KeyEvent.KEYCODE_ALT_RIGHT){
            addChar(' ');
            return true;
        }
        else if(keycode==KeyEvent.KEYCODE_LEFT_BRACKET){
            if(event.isShiftPressed())
                addString("{}");
            else
                addString("[]");
            cursorLeft();
            return true;
        }
        else if(keycode==KeyEvent.KEYCODE_APOSTROPHE){
            if(event.isShiftPressed()){
                addString("\"\"");
            }
            else {
                addString("''");
            }
            cursorLeft();
            return true;
        }

        else if(keycode==KeyEvent.KEYCODE_EQUALS){
            if(event.isShiftPressed()){
                addChar('+');
            }
            else {
                addChar('=');
                //addLogWithColor(getCurrentVariableValue()+"\n",GlobalState.info);
            }
            return true;
        }
        else if(keycode==KeyEvent.KEYCODE_DEL){
            return deleteLeft();
        }
        else if(keycode==KeyEvent.KEYCODE_SEMICOLON){
            if(event.isShiftPressed()){
                addChar(':');
            }else
                addChar(';');
            return true;
        }
        else if(keycode==KeyEvent.KEYCODE_TAB){
            addLogWithColor(getCurrentVariableHint()+"\n",GlobalState.info);
        }
        else if(!(keycode==KeyEvent.KEYCODE_CTRL_LEFT||keycode==KeyEvent.KEYCODE_SHIFT_LEFT
                ||keycode==KeyEvent.KEYCODE_DPAD_RIGHT
        ))
            addString("["+keycode+"]");

        return true;
    }

    private String getCurrentVariableValue() {
        String s=getCurrentVariable();
        JsNative.safeEval(ctx,s);
        if(JsNative.isError(ctx,-1)){
            return "undefined variable";
        }
        String result=JsNative.safeToString(ctx,-1);
        JsNative.pop(ctx);
        return result;
    }

    private boolean isSymbol(char a){
        if(a>='a'&&a<='z')
            return true;
        if(a>='A'&&a<='Z'){
            return true;
        }
        if(a=='.'){
            return true;
        }
        return false;
    }

    private String getCurrentVariable() {
        //notice currentCaret is equal the length of string, so we need -1
        //we allow general eitehr a. or a.b or more
        int i=currentCaret-1;
        for(;i>=0;i--){
            if(!isSymbol(currentInput.charAt(i)))
                break;
        }
        return  currentInput.subSequence(i+1,currentCaret).toString();
    }

    /**
     * name has a form of a.b or a. a.c.d
     * corerspong a.c and d or a.c ""
     * @param name
     * @return
     */
    private String[] parseVariable(String name){
        int length=name.length();
        if(name.endsWith("."))
            return new String[]{name.substring(0,length-1),""} ;
        else{
            int i;
            for(i=length-2;i>0;i--){
                if(name.charAt(i)=='.'){
                    break;
                }
            }
            if(i>0)
                return new String[]{name.substring(0,i),name.substring(i+1,length)};
            else{
                return new String[]{"",name};
            }
        }
    }
    private String getCurrentVariableHint(){
        JsNative.clearStack(ctx);
        String variable=getCurrentVariable();
        String result="";
        String[] parsedForm=parseVariable(variable);
        variable=parsedForm[0];
        String hint=parsedForm[1];
        if(variable.equals("")){
            JsNative.getGlobalString(ctx,JsNative.GETJSOBJECTPROPERTIES);
            JsNative.pushGlobalObject(ctx);
            JsNative.call(ctx,1);
            result=JsNative.safeToString(ctx,-1);
            JsNative.pop(ctx);
            return result;
        }
        JsNative.safeEval(ctx,variable);
        int type=JsNative.getType(ctx,-1);
        if(type!=JsNative.DUK_TYPE_OBJECT){
            return variable+" is not object!";
        }
        else {
            try {
                JsNative.getPropString(ctx,-1,JsNative.PROXYGETBAREOBJECTKEY);
                if(JsNative.getType(ctx,-1)==JsNative.DUK_TYPE_OBJECT) {
                    JsNative.pop(ctx);
                    Object b = JsNative.getProxyObject(ctx, -1);
                    JsNative.pop(ctx);
                    return JsReflection.showMethods(b,hint);
                }
                else{
                    JsNative.pop(ctx);
                    JsNative.getGlobalString(ctx,JsNative.GETJSOBJECTPROPERTIES);
                    JsNative.insert(ctx,-2);
                    JsNative.call(ctx,1);
//                    JsNative.pushContextDump(ctx);
                    result=JsNative.safeToString(ctx,-1);
                    JsNative.clearStack(ctx);
//                    JsNative.call(ctx,1);
//                    result=JsNative.safeToString(ctx,-1);
//                    JsNative.pop(ctx);
                    return result;
                }


            }catch (Exception e) {
                return "null";
            }
        }

    }
    private void emptyInput(){
        currentInput=new SpannableStringBuilder();
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
            currentInput.delete(currentCaret-1,currentCaret);
        }
        cursorLeft();
        return true;
    }
    private boolean deleteLeft(int n) {
        for(int i=0;i<n;i++)
            deleteLeft();
        return true;
    }

    private boolean cursorRight() {
        if(currentCaret<currentInput.length()){

            currentCaret+=1;
            setSelected();
        }

        updateInput();
        return true;
    }

    private void setSelected(){
        currentInput.clearSpans();
        if(currentCaret<currentInput.length()&&currentCaret>=0) {
            if(markon) {
                if (currentCaret < markPosition)
                    currentInput.setSpan(new BackgroundColorSpan(Color.BLUE), currentCaret, markPosition + 1, 0);
                else
                    currentInput.setSpan(new BackgroundColorSpan(Color.BLUE), markPosition, currentCaret + 1, 0);
            }
            else{
                currentInput.setSpan(new BackgroundColorSpan(Color.BLUE), currentCaret, currentCaret + 1, 0);
            }
        }
        if(currentCaret==currentInput.length()){
            if(markon)
                currentInput.setSpan(new BackgroundColorSpan(Color.BLUE), markPosition, currentInput.length(), 0);
        }
        updateUI();
    }
    private boolean cursorDown(){
        if(currentCaret<currentInput.length()-1){
            currentCaret+=1;
        }
        while (currentCaret<currentInput.length()-1&&
                !(currentInput.charAt(currentCaret-1)=='\n')){
            currentCaret+=1;
        }
        setSelected();
        //GlobalState.printToLog("down"+currentCaret+"\n",GlobalState.info);
        //cursorRight();
        return true;
    }
    private boolean cursorUp(){
        if(currentCaret>1){
            currentCaret-=1;
        }
        while(currentCaret>0){
            if(!(currentInput.charAt(currentCaret-1)=='\n'))
                    currentCaret-=1;
            else
                break;
        }
        setSelected();
        //GlobalState.printToLog("up"+currentCaret+"\n",GlobalState.info);
        //cursorLeft();
        return true;
    }
    private boolean cursorLeft() {
        if(currentCaret>0){
            currentCaret-=1;
            setSelected();
        }

        updateInput();
        return true;
    }
    private boolean cursorLeft(int n) {
        if(currentCaret>n-1){
            currentCaret-=n;
            setSelected();
        }
        updateInput();
        return true;
    }
    int markPosition=0;
    private boolean setMark(){
        markPosition=currentCaret;
        markon =!markon;
        return true;
    }
    private LinkedList<String> copyboards=new LinkedList<>();
    private boolean cut(){
        if(markPosition>currentCaret) {
            copyboards.add((String) currentInput.subSequence(currentCaret,markPosition).toString());
            currentInput=currentInput.delete(currentCaret, markPosition);
        }
        else{
            copyboards.add((String) currentInput.subSequence(markPosition,currentCaret).toString());
            currentInput=currentInput.delete(markPosition,currentCaret);
            currentCaret=markPosition;
        }
        markon=!markon;
        updateUI();
        return true;
    }
    private boolean copy(){
        if(markPosition>currentCaret) {
            copyboards.add((String) currentInput.subSequence(currentCaret,markPosition).toString());
            markPosition=currentCaret;
        }
        else{
            copyboards.add((String) currentInput.subSequence(markPosition,currentCaret).toString());
            currentCaret=markPosition;
        }
        markon=!markon;
        updateUI();
        return true;
    }
    private boolean paste(){
        addString(copyboards.get(copyboards.size()-1));
        return true;
    }
    private boolean endOfLine(){
        cursorDown();
        cursorLeft();
        return true;
    }
}
