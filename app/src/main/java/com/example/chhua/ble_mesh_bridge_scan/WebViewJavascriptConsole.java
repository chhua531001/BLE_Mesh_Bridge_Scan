package com.example.chhua.ble_mesh_bridge_scan;

import android.app.Activity;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class WebViewJavascriptConsole implements ValueCallback<String> {

    public interface Listener {
        public void run(JSONObject data);
    };

    protected Activity mActivity;
    protected WebView mWebView;

    private HashMap<String,Listener> mCommands;
    private String targetID = this.getClass().getSimpleName();
    private String currentCommand = "";
    
    public WebViewJavascriptConsole(Activity _activity, WebView _webView)  {
        mActivity = _activity;
        mWebView = _webView;
        mCommands = new HashMap<String,Listener>();
        currentCommand = "";
    }

    public void register(String cmd, Listener listener) {
        Log.println(Log.INFO, targetID, "register");
        mCommands.put(cmd,listener); }


    public void load(String url) {
        final String u = url;
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mWebView.loadUrl(u);
			}
		});
    }

    public void evaluate(String javascript) {
        mWebView.evaluateJavascript(javascript,this);
    }
    @Override
    public void onReceiveValue(String value) {
        Log.println(Log.DEBUG, targetID, "Receive String: " + value);
    }

    @JavascriptInterface
	public void command(String json) {
        Log.println(Log.INFO, targetID, "JavaScriptHandler.command is called.");
        Log.println(Log.DEBUG, targetID, "Receive JSON: " + json);
        try {
            JSONObject meta = new JSONObject(json);
            Log.println(Log.DEBUG, targetID, "meta: " + meta);
            currentCommand = meta.getString("command");
            Log.println(Log.DEBUG, targetID, "Command: " + currentCommand);
            if( !mCommands.containsKey(currentCommand) ) {
                Log.println(Log.DEBUG, targetID, "There is no Command: " + currentCommand);
                return;
            }
            //mCommands.get(currentCommand)會回傳 String 對應的 Listener,  而Listener 可以被呼叫 run
            //mCommands.get(currentCommand), 代表key之後的value
            //利用 register 的那個 new WebViewJavascriptConsole.Listener
            //那個 value 是一個 class 實體，只是這個 class 是 implements Interface WebViewJavascriptConsole.Lisntener 的，所以一定有 run 這個 function
            mCommands.get(currentCommand).run(meta);
        } catch(JSONException e) {
            Log.println(Log.INFO, targetID, "Fail to parse JSON.");
            return;
        }
	}

}
