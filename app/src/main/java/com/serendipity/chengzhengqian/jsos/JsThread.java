package com.serendipity.chengzhengqian.jsos;

public class JsThread extends Thread {
    Command c;
    MainActivity app;
    long ctx;
    IOLocker ioLocker;
    JsThread(Command c, MainActivity app,IOLocker ioLocker){
        this.c=c;
        this.app=app;
        this.ioLocker=ioLocker;
    }
    public static String oldCode="__old_code__";
    public static String newCode="__new_code__";
    public static String transformFunc="__babel_func__";
    private void initBabel(){
        String babeljs=this.app.getRawResource(R.raw.babeljs);
        JsNative.safeEvalString(ctx,babeljs);
        String s=JsNative.safeToString(ctx, -1);
        GlobalState.printToLog("\nbabel loaded: "+ s+"\n", GlobalState.info);
        JsNative.pushString(ctx,"function(old){return (Babel.transform(old,{presets:['es2015']}).code);}");
        JsNative.pushString(ctx,transformFunc);
        JsNative.pCompile(ctx,JsNative.DUK_COMPILE_FUNCTION);
        JsNative.putGlobalString(ctx,transformFunc);

    }


    private void initJsCtx() {
        ctx=JsNative.createHeapDefault();
        JsNative.registerJavaHandle(ctx);
        JsNative.registerProxyHandleGet(ctx);
        JsNative.registerProxyHandleSet(ctx);
        JsNative.registerJsObjectFinalizer(ctx);
        JsNative.registerJsObejctProperties(ctx);
        JsNative.registerFunctionHandle(ctx);
        JsNative.pushObject(ctx,new JsJava(this.app,ioLocker),JsJava.name);
        initBabel();
    }
    private void delJsCtx()
    {
        JsNative.releaseJavaHandle(ctx);
        JsNative.destroyHeap(ctx);
    }

    public void run(){
        initJsCtx();
        boolean isContinue=true;
        GlobalState.printToLog("js thread start!\n",GlobalState.info);
        while(isContinue){
            synchronized (c){
                try {
                    c.wait();
                    if(c.state==Command.running)
                        runCode(c.code,c.id,c.useBabel);
                    else if(c.state==Command.hint){
                        c.hintResult=getCurrentVariableHint(c.parsedFrom);
                        c.notify();
                    }
                    else if(c.state==Command.stop){
                        isContinue=false;
                    }
                } catch (InterruptedException e) {
                    GlobalState.printToLog(e.toString(),GlobalState.error);
                }

            }
        }
        GlobalState.printToLog("js thread is stopped", GlobalState.info);
        delJsCtx();

    }
    public static String babletransformError="'bable error'";
    /* transform the code the stored in global variables*/
    private String transformCode(String codeInput){
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
        return babletransformError;
    }
    private boolean runCode(String codeInput, int id,boolean useBable) {
        try {
            if(useBable) {
                codeInput = transformCode(codeInput);
                if(codeInput.length()>13){
                    codeInput=codeInput.substring(14);
                }
                GlobalState.printToLog(codeInput+"\n",GlobalState.babelCode);
            }
            JsNative.safeEvalString(ctx,codeInput);
            String s=JsNative.safeToString(ctx, -1);
            if(s!=null&&id>=0)
                GlobalState.printToLog("\nOut:["+id+"]: "+ s+"\n", GlobalState.info);
        }
        catch (Exception e){
            GlobalState.printToLog(e.toString(), GlobalState.info);
        }
        return true;
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
