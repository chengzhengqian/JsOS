package com.serendipity.chengzhengqian.jsos;

import java.util.zip.CheckedOutputStream;

public class JsThread extends Thread {
    CommandLock c;
    MainActivity app;
    long ctx;
    IOLock ioLock;
    JsThread(CommandLock c, MainActivity app, IOLock ioLock){
        this.c=c;
        this.app=app;
        this.ioLock = ioLock;
    }
    public static String oldCode="__old_code__";
    public static String newCode="__new_code__";
    public static String transformFunc="__babel_func__";
    private void initBabel(){
        String babeljs=this.app.getRawResource(R.raw.babeljs);
        JsNative.safeEvalString(ctx,babeljs);
        String s=JsNative.safeToString(ctx, -1);
        JsNative.pushString(ctx,"function(old){return (Babel.transform(old,{presets:['es2015']}).code);}");
        JsNative.pushString(ctx,transformFunc);
        JsNative.pCompile(ctx,JsNative.DUK_COMPILE_FUNCTION);
        JsNative.putGlobalString(ctx,transformFunc);
        GlobalState.printToLog("\nbabel loaded!\n", GlobalState.info);

    }


    private void initJsCtx(CommandLock c) {
        ctx=JsNative.createHeapDefault();
        c.mainCtx=ctx;
        //we have the registerJavaHandle in run, as we now support multithread share heap
        JsNative.registerProxyHandleGet(ctx);
        JsNative.registerProxyHandleSet(ctx);
        JsNative.registerJsObjectFinalizer(ctx);
        JsNative.registerJsObejctProperties(ctx);
        JsNative.registerFunctionHandle(ctx);
        JsNative.pushObject(ctx,new JsJava(this.app, ioLock,c),JsJava.name);
        initBabel();
    }
    private void delJsCtx()
    {
        JsNative.destroyHeap(ctx);
    }

    public void run(){
        synchronized (c){
            initJsCtx( c);
            boolean isContinue=true;
            GlobalState.printToLog("js thread start!\n",GlobalState.info);

            while(isContinue){
                JsNative.registerJavaHandle(ctx);
                try {
                    c.isAvailableForNewCommand=true;
                    c.wait();
                    c.isAvailableForNewCommand=false;
                    if(c.state== CommandLock.RUNCODE)
                        runCode(ctx,c.code,c.id,c.useBabel);
                    else if(c.state== CommandLock.GETHINT){
                        c.hintResult=getCurrentVariableHint(c.parsedFrom);
                        c.notify();
                    }
                    else if(c.state== CommandLock.STOP){
                        isContinue=false;
                    }
                    GlobalState.updateUI();
                } catch (InterruptedException e) {
                    //GlobalState.printToLog(e.toString(),GlobalState.error);
                    JsNative.releaseJavaHandle(ctx);//as we allow other thread inject in
                    delJsCtx();
                    c.state= CommandLock.STOP;
                    GlobalState.printToLog("thread exit by interrupt!\n",GlobalState.info);
                    return;
                }
                JsNative.releaseJavaHandle(ctx);//as we allow other thread inject in
            }
        }
        delJsCtx();
        GlobalState.printToLog("js thread is stopped", GlobalState.info);
    }
    public static String babletransformError="'bable_err'";

    /* transform the code the stored in global variables*/
    public static String transformCode(long ctx,String codeInput){
        JsNative.pushString(ctx,codeInput);
        JsNative.putGlobalString(ctx,oldCode);
        int sucess;
        sucess=JsNative.safeEvalString(ctx,String.format("%s(%s)",transformFunc,oldCode));
        /*this is equivalent to the following two line code*/
//        JsNative.pushString(ctx,String.format("%s(%s)",transformFunc,oldCode));
//        sucess=JsNative.safeEval(ctx);
        if(sucess==JsNative.DUK_EXEC_SUCCESS) {
            JsNative.putGlobalString(ctx, newCode);
            JsNative.getGlobalString(ctx, newCode);
            String result = JsNative.getString(ctx, -1);
            JsNative.pop(ctx);
            return result;
        }
        GlobalState.printToLog("\nsyntax error in processing within babel!\n",
                GlobalState.error);
        return babletransformError;
    }
    public static final int bableCodeHeadSize=13;
    public static String runCode(long ctx,String codeInput, int id,boolean useBable) {
        try {
            if(useBable) {
                codeInput = transformCode(ctx,codeInput);
                if(codeInput.length()>=bableCodeHeadSize){
                    codeInput=codeInput.substring(bableCodeHeadSize);
                }
                if(codeInput.startsWith("\n")){
                    codeInput=codeInput.substring(1);
                }
                if(JsNative.ISDEBUG)
                    GlobalState.printToLog(codeInput+"\n",GlobalState.babelCode);
            }
            JsNative.safeEvalString(ctx,codeInput);
            String s=JsNative.safeToString(ctx, -1);
            if(CommandLock.isShowOutput)
                GlobalState.printToLog(String.format(
                        "\nOut[%d]: %s\n",id,s
                ),GlobalState.info);
            return s;
        }
        catch (Exception e){
            GlobalState.printToLog(e.toString(), GlobalState.info);
        }
        return "";
    }

    private String getCurrentVariableHint(String[] parsedForm){
        JsNative.clearStack(ctx);
        String result="";
        String variable=parsedForm[0];
        String hint=parsedForm[1];
        if(variable.equals("")){
            JsNative.getGlobalString(ctx,JsNative.GETJSOBJECTPROPERTIES);
            JsNative.pushGlobalObject(ctx);
            JsNative.call(ctx,1);
            result=JsNative.safeToString(ctx,-1);
            JsNative.pop(ctx);
            return result;
        }
        JsNative.safeEvalString(ctx,variable);
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

}
