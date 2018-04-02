package com.serendipity.chengzhengqian.jsos;
/**
 * a instance of it will push to javascript as a global variable java
 * */
public class JsJava {
    /**
     * the name wil appear in js enviroment
     */
    public static String name="java";
    public MainActivity app;
    public static final String version="1.0.0";
    public JsJava(MainActivity m){
        this.app=m;
    }
    public static void print(Object s){
        GlobalState.printToLog(s.toString(),GlobalState.normal);
    }
    public static Class<?> load(String name){
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            GlobalState.printToLog(e.toString(),GlobalState.error);
        }
        return JsJava.class;
    }

}
