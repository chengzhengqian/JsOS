package com.serendipity.chengzhengqian.jsos;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class JsService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private static int WEBSERVER_PORT = 11000;
    private JsServer server;
    public static String wrapServiceInfo(String s){
        return Utils.getCurrentTime()+": "+s+"\n";
    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            server=new JsServer(WEBSERVER_PORT);
            server.start();
            GlobalState.printToLog(
                    wrapServiceInfo("server is started"),GlobalState.info);
        }
        catch (Exception e){
            GlobalState.printToLog(
                   e.toString(),GlobalState.error);
        }
        return START_STICKY;

    }
    @TargetApi(26)
    public void onDestroy() {
        super.onDestroy();
        server.stop();
        GlobalState.printToLog(
                wrapServiceInfo("server is stopped!"),GlobalState.info);
    }

}
