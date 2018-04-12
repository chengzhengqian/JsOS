package com.serendipity.chengzhengqian.jsos;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceConfigurationError;

/**
 * a instance of it will push to javascript as a global variable java
 * */
public class JsJava {
    /**
     * the name wil appear in js enviroment as this.name set
     */


    public static String name="java";
    private final IOLock ioLock;
    private final long ctx;
    public MainActivity app;
    public static final String version="1.0.0";
    public JsJava(MainActivity m, IOLock s, long ctx){
        this.app=m;this.ioLock =s; this.ctx=ctx;
    }
    public static void print(Object s){
        GlobalState.printToLog(s.toString(),GlobalState.normal);
    }
    public static List<String> PATH=new LinkedList<>(Arrays.asList(
            "com.serendipity.chengzhengqian.jsos.",
            "android.widget."
    ));
    public static Class<?> loadWithPATH(String name, List<String> path){
        for(String s:path){
            try{
                return Class.forName(s+name);
            }
            catch (Exception e){

            }
        }
        GlobalState.printToLog("class "+name+"not found!\n",GlobalState.error);
        return JsJava.class;
    }
    public static Class<?> load(String name){
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {

            return loadWithPATH(name,PATH);
        }

    }
    public void gc(){
        JsNative.gc(ctx,0);
    }
    public static boolean toggleDebug(){
        JsNative.ISDEBUG=!JsNative.ISDEBUG;
        return JsNative.ISDEBUG;
    }
    public String read(){
        synchronized (ioLock){
            if(!ioLock.isBlocked) {
                try {
                    ioLock.isBlocked=true;
                    ioLock.wait();
                    ioLock.isBlocked=false;
                    return ioLock.content.toString();
                } catch (InterruptedException e) {
                    GlobalState.printToLog(e.toString(), GlobalState.error);
                }
            }
            return "";
        }

    }
    public Object proxy(Class<?> c, String code){
        return Proxy.newProxyInstance(
                c.getClassLoader(),
                new Class[]{c},
                new JsInvocativeHandler(code)
        );
    }
}
