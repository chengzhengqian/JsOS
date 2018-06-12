package com.serendipity.chengzhengqian.jsos;

public class CommandLock {
    public String code;
    /*indicates what's the main thread shoudl do after been wake up*/
    public int state;
    public boolean useBabel;
    public static boolean isShowOutput=true;
    public int id=0;
    public static int STOP =0;
    public static int RUNCODE =1;
    public static int GETHINT =2;
    public long mainCtx;

    /**
     * create a new context associate with current thread and run the code, isBabel
     * specify whether we use babel to transform code first
     * @param code
     * @param isBabel
     */
    public String runInCurrentThread(String code,boolean isBabel) {
        synchronized (this) {
            this.isShowOutput=false;
            this.isAvailableForNewCommand = false;
            int index = JsNative.pushThread(mainCtx);
            long currentCtx = JsNative.getContext(mainCtx, index);
            JsNative.registerJavaHandle(currentCtx); //as js handle will need j env, which is set from this.
            String result=JsThread.runCode(currentCtx, code, RUNCODE, true);
            JsNative.releaseJavaHandle(currentCtx);
            JsNative.pop(mainCtx);
            this.isAvailableForNewCommand = true;
            return result;
        }
        // clear currentCtx; notice as __java__handle__ essentially contains
        // a jnienv and some frequent used class and method, this make it thread sensitve. As
        // duktape heap also require single thread executation (but allow several thread share the
        // the heap, currently, the strategy is, any java thread try to execuate a code, register its
        // own callback,if finised, remember to release the refernece hold by javahandle.
    }
    public static final String argForm="__arg%d__";
    public void callJsObjectInCurrentThread(String objectName, String methodName, Object[] args){
        synchronized (this){
            this.isAvailableForNewCommand = false;
            int index = JsNative.pushThread(mainCtx);
            long currentCtx = JsNative.getContext(mainCtx, index);
            JsNative.registerJavaHandle(currentCtx); //as js handle will need j env, which is set from this.
            StringBuilder command=new StringBuilder();
            command.append(objectName);
            command.append(".");
            command.append(methodName);
            command.append("(");
            for(int i=0;i<args.length;i++){
                String argname=String.format(argForm,i);
                if(i>0){
                    command.append(",");
                }
                JsNative.pushJavaObject(currentCtx,args[i]);
                JsNative.putGlobalString(currentCtx,argname);
                command.append(argname);
            }
            command.append(")");

            JsThread.runCode(currentCtx,command.toString(),RUNCODE,false);

            JsNative.releaseJavaHandle(currentCtx);
            JsNative.pop(mainCtx);
            this.isAvailableForNewCommand = true;

        }

    }

    public CommandLock(){
        this.state= RUNCODE;
    }
    public boolean isAvailableForNewCommand=false;
    public void setCommand(String code){
        this.code=code;
    }
    public void setId(int id){this.id=id;};
    public String[] parsedFrom;
    public String hintResult;
    public void setHint(String[] parsedForm){this.state= GETHINT;this.parsedFrom=parsedForm;}
}
