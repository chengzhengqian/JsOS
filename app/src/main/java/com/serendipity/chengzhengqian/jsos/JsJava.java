package com.serendipity.chengzhengqian.jsos;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * a instance of it will push to javascript as a global variable java
 * */
public class JsJava {
    /**
     * the name wil appear in js enviroment as this.name set
     */


    public static String name="java";
    private final IOLocker ioLocker;
    public MainActivity app;
    public static final String version="1.0.0";
    public JsJava(MainActivity m, IOLocker s){
        this.app=m;this.ioLocker=s;
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
    public static boolean toggleDebug(){
        JsNative.ISDEBUG=!JsNative.ISDEBUG;
        return JsNative.ISDEBUG;
    }
    public String read(){
        synchronized (ioLocker){
            if(!ioLocker.isBlocked) {
                try {
                    ioLocker.isBlocked=true;
                    ioLocker.wait();
                    ioLocker.isBlocked=false;
                    return ioLocker.content.toString();
                } catch (InterruptedException e) {
                    GlobalState.printToLog(e.toString(), GlobalState.error);
                }
            }
            return "";
        }

    }

}
