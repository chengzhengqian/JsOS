package com.serendipity.chengzhengqian.jsos;

import android.graphics.Color;

public class GlobalState {
    static MainActivity currentActivity;
    static int info=Color.BLUE;
    static int error=Color.RED;
    static int normal=Color.GREEN;
    static int caretBackground= Color.BLUE;
    public static boolean isUIRunning;
    public static String serverIndexHtml;
    public static long ctx;
    public static boolean isServerRunning=false;

    static void printToLog(final String s, final int Color){
        currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                currentActivity.addLogWithColor(s,Color);
            }
        });
    }
}