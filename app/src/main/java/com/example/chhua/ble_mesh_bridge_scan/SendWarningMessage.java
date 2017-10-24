package com.example.chhua.ble_mesh_bridge_scan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

/*
    BroadcastReceiver

        Base class for code that will receive intents sent by sendBroadcast().
        If you don't need to send broadcasts across applications, consider using this class with
        LocalBroadcastManager instead of the more general facilities described below. This will give
        you a much more efficient implementation (no cross-process communication needed) and allow
        you to avoid thinking about any security issues related to other applications being able to
        receive or send your broadcasts.

        You can either dynamically register an instance of this class with Context.registerReceiver()
        or statically publish an implementation through the <receiver> tag in your AndroidManifest.xml.
*/

public class SendWarningMessage extends BroadcastReceiver {

    Tools tools = new Tools();
    /*
        public abstract void onReceive (Context context, Intent intent)
            This method is called when the BroadcastReceiver is receiving an Intent broadcast.
            During this time you can use the other methods on BroadcastReceiver to view/modify
            the current result values.

        Parameters
            context : The Context in which the receiver is running.
            intent : The Intent being received.
    */
    @Override
    public void onReceive(Context context, Intent intent){
        // Receive the broadcast Warning Message
        String[] warningMsg = intent.getStringArrayExtra("warningMsg");

        // Display the received Warning Message
//        Toast.makeText(context, "收到的訊息內容 :\n\n " + warningMsg[0] + "\n" + warningMsg[1] + "\n"
//        + warningMsg[2] + "\n" + warningMsg[3] + "\n" + warningMsg[4], Toast.LENGTH_SHORT).show();

        tools.toastNow(context, "收到的訊息內容 :\n\n " + warningMsg[0] + "\n" + warningMsg[1] + "\n"
                + warningMsg[2] + "\n" + warningMsg[3] + "\n" + warningMsg[4], Color.WHITE);

    }
}
