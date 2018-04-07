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
    private void initJsCtx() {
        ctx=JsNative.createHeapDefault();
        JsNative.registerJavaHandle(ctx);
        JsNative.registerProxyHandleGet(ctx);
        JsNative.registerProxyHandleSet(ctx);
        JsNative.registerJsObjectFinalizer(ctx);
        JsNative.registerJsObejctProperties(ctx);
        JsNative.registerFunctionHandle(ctx);
        JsNative.pushObject(ctx,new JsJava(this.app,ioLocker),JsJava.name);
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
                        runCode(c.code,c.id);
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

    private boolean runCode(String codeInput, int id) {
        try {
            JsNative.safeEval(ctx,codeInput);
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

}
