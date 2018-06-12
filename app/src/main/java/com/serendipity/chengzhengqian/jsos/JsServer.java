package com.serendipity.chengzhengqian.jsos;

import android.os.AsyncTask;
import fi.iki.elonen.NanoHTTPD;

import java.util.HashMap;
import java.util.Map;

public class JsServer extends NanoHTTPD {

    static class ShowIPInfo extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... port) {

            GlobalState.printToLog("host ip: "+NetUtil.getIPAddress(true)+
                            ", port: "+ port[0]+"\n",
                        GlobalState.info);
            return null;
        }
    }
    public JsServer(int port) {
        super(port);
        new ShowIPInfo().execute(port);
    }

    private String parseBodyForPostOrGet(NanoHTTPD.IHTTPSession session){
        Map<String, String> files = new HashMap<>();
        Method method = session.getMethod();
        if (Method.PUT.equals(method) || Method.POST.equals(method)) {
            try {
                session.parseBody(files);
            } catch (Exception e) {
                return (e.toString());
            }
        }
        return "";
    }
    @Override
    public Response serve(IHTTPSession session) {
        String url=session.getUri();
        GlobalState.printToLog(JsService.wrapServiceInfo(url),GlobalState.info);
        if(url.equals("/index.html")||
                session.getUri().equals("/")
                )return indexHtml();

        else if(session.getUri().equals("/runCode"))return runCode(session);
        else if(session.getUri().equals("/runCodeUI"))return runCodeUI(session);

        else if(session.getUri().equals("/createWindows"))return createWindows(session);

        return newFixedLengthResponse("Not Found!");
    }

    private Response createWindows(IHTTPSession session) {
        String result=parseBodyForPostOrGet(session);
        String response="";
        if(result.equals("")) {
            String name= session.getParms().get("name");
            if (GlobalState.isUIRunning) {
                response = SUCCESS;
//                GlobalState.sendToMain(GlobalState.ADDWINDOW, name);
            } else {
                response = NOTRUNNING;
            }
        }
        return newFixedLengthResponse(response);
    }

    public static String SUCCESS="success";
    public static String NOTRUNNING="not running";
    private Response runCode(IHTTPSession session) {
        String result=parseBodyForPostOrGet(session);
        String response="";
        if(result.equals("")) {
            final String content = session.getParms().get("code");
            //String id= session.getParms().get("id");
            if (GlobalState.isUIRunning) {
                //int i=Integer.valueOf(id);
                response = SUCCESS;
                GlobalState.currentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            /*
                            commandLock is notified to awake the js thread to run the code
                            * */
                            GlobalState.printToLog(">>>\n"+content+"\n<<<\n",GlobalState.info);
                            synchronized (GlobalState.commandLock){
                                CommandLock.isShowOutput=false;
                                if(GlobalState.commandLock.state== CommandLock.RUNCODE){
                                    GlobalState.commandLock.code=content;
                                    GlobalState.commandLock.id=-1;
                                    GlobalState.commandLock.notify();
                                }
                            }
                        }catch (Exception e){
                            GlobalState.printToLog(e.toString(),GlobalState.error);
                        }
                    }
                });

            } else {
                response = NOTRUNNING;
            }
        }

        return newFixedLengthResponse(response);
    }

    private Response runCodeUI(IHTTPSession session) {
        String result=parseBodyForPostOrGet(session);
        String response="";
        if(result.equals("")) {
            String content = session.getParms().get("code");
            if (GlobalState.isUIRunning) {

                response = SUCCESS;
                //GlobalState.sendToMain(GlobalState.RUNCURRENTWINDOW,content);

            } else {
                response = NOTRUNNING;
            }
        }

        return newFixedLengthResponse(response);
    }

    private Response indexHtml(){
        return newFixedLengthResponse(GlobalState.serverIndexHtml);
    }


}
