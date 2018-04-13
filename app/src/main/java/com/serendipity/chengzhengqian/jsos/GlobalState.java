package com.serendipity.chengzhengqian.jsos;

import android.graphics.Color;

import java.util.LinkedList;
import java.util.List;

public class GlobalState {
    static MainActivity currentActivity;
    static int info=Color.BLUE;
    static int error=Color.RED;
    static int normal=Color.GREEN;
    static int babelCode=Color.GRAY;
    static int infoDebug=Color.rgb(80,40,80);
    static int caretBackground= Color.BLUE;
    public static boolean isUIRunning;
    public static String serverIndexHtml;
    //public static long ctx;
    public static boolean isServerRunning=false;
    public static CommandLock commandLock;
    public static List<JsThread> threads=new LinkedList<>();
    public static void killThread(int index){
        if(index>=0&&index<threads.size()){
            threads.get(index).interrupt();
            threads.remove(index);
        }
    }
    public static void updateUI(){
        currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                currentActivity.updateUI();
            }
        });
    }
    public static void showThreadInfo(){
        currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                StringBuilder result=new StringBuilder("\n");
                int index=0;
                for(JsThread t : threads){
                    result.append(
                            String.format("%d. avail: %b state: %d io: %s\n",
                                    index,t.c.isAvailableForNewCommand,
                                    t.c.state,
                                    t.ioLock.isBlocked
                                    )
                    );
                    index+=1;
                }
                currentActivity.addLogWithColor(result.toString(),infoDebug);
            }
        });
    }
    static void printToLog( String s, int color){
         currentActivity.addStringWithColor(s,color);
    }
}
