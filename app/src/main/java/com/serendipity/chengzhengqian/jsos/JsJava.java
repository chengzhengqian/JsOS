package com.serendipity.chengzhengqian.jsos;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
    public static List<String> PATH=new LinkedList<>(Arrays.asList(
            "com.serendipity.chengzhengqian.jsos.",
            "android.widget."
    ));
    public static Class<?> loadWithLists(String name){
        for(String s:PATH){
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

            return loadWithLists(name);
        }

    }

}
