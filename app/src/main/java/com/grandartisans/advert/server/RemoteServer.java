package com.grandartisans.advert.server;

import android.util.Log;

import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class RemoteServer extends NanoHTTPD {
    private final String TAG = "RemoteServer";
    public RemoteServer(int port) {
        super(port);
    }
    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();
        Map<String, String> parms = session.getParms();
        Log.i(TAG,"method :" + method + "uri :" + uri);
        if(uri.startsWith("/remote")) {
            if (parms.get("keycode") != null) {
                int keycode = Integer.valueOf(parms.get("keycode"));
                Log.i(TAG, "keycode = " + keycode);
                KeyResponse(keycode);
            }
        }
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html><body>");
        builder.append("suc");
        builder.append("</body></html>\n");
        return newFixedLengthResponse(builder.toString());
    }

    /**
     * 按键响应
     */
    public void KeyResponse(int keyValue) {
        try {
            String keyCommand = "input keyevent " + keyValue;
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec(keyCommand);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
