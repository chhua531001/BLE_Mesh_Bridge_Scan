package com.example.chhua.ble_mesh_bridge_scan;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.spec.AlgorithmParameterSpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Hua_Home_1 on 2017/8/5.
 */

public class Tools {
//    //12小時制
//    DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss a");
    //    24小時制
    DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    String targetID = this.getClass().getSimpleName();

    public ArrayList<Transaction> parseJSON(String s) {
        Log.println(Log.INFO, targetID, "parseJSON");
        ArrayList<Transaction> warningMessage = new ArrayList<>();
        String priority = "";
        String type = "";
        String subject = "";
        String detail = "";
        Long timestamp = null;

//        Log.println(Log.INFO, targetID, "parseJSON");

        try {
            JSONArray array = new JSONArray(s);
            for (int i=0; i<array.length(); i++){
                JSONObject obj = array.getJSONObject(i);

                //   讀出JSONObject的所有key
                Iterator<String> iter = obj.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    Log.d("Key ",key);
                    switch (key) {
                        case "priority":
                            priority = obj.getString(key);
                            Log.d("priority : ", priority);;
                            break;
                        case "type":
                            type = obj.getString(key);
                            Log.d("type : ", type);;
                            break;
                        case "subject":
                            subject = obj.getString(key);
                            Log.d("subject : ", subject);;
                            break;
                        case "detail":
                            detail = obj.getString(key);
                            Log.d("detail : ", detail);;
                            break;
                        case "timestamp":
                            timestamp = obj.getLong(key);
                            Log.d("timestamp : ", Long.toString(timestamp));;
                            break;
                    }
                }

                Log.println(Log.DEBUG, targetID, "JSON:"+priority+"/"+type+"/"+subject+"/"+detail+"/"+timestamp);
                Transaction t = new Transaction(priority, type, subject, detail, timestamp);
                warningMessage.add(t);
            }

            for (Transaction s1 : warningMessage)
            {
                Log.println(Log.DEBUG, targetID, "priority: " + s1.priority);
                Log.println(Log.DEBUG, targetID, "type: " + s1.type);
                Log.println(Log.DEBUG, targetID, "subject: " + s1.subject);
                Log.println(Log.DEBUG, targetID, "detail: " + s1.detail);
                Log.println(Log.DEBUG, targetID, "timestamp: " + s1.timestamp);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return warningMessage;
    }

    public ArrayList<Transaction> parseJSONArray(JSONArray array) {
        ArrayList<Transaction> warningMessage = new ArrayList<>();
        String priority = "";
        String type = "";
        String subject = "";
        String detail = "";
        Long timestamp = null;

        try {
            for (int i=0; i<array.length(); i++){
                JSONObject obj = array.getJSONObject(i);

                //   讀出JSONObject的所有key
                Iterator<String> iter = obj.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    Log.d("Key ",key);
                    switch (key) {
                        case "priority":
                            priority = obj.getString(key);
                            Log.d("priority : ", priority);;
                            break;
                        case "type":
                            type = obj.getString(key);
                            Log.d("type : ", type);;
                            break;
                        case "subject":
                            subject = obj.getString(key);
                            Log.d("subject : ", subject);;
                            break;
                        case "detail":
                            detail = obj.getString(key);
                            Log.d("detail : ", detail);;
                            break;
                        case "timestamp":
                            timestamp = obj.getLong(key);
                            Log.d("timestamp : ", Long.toString(timestamp));;
                            break;
                    }
                }

                Log.println(Log.DEBUG, targetID, "JSON:"+priority+"/"+type+"/"+subject+"/"+detail+"/"+timestamp);
                Transaction t = new Transaction(priority, type, subject, detail, timestamp);
                warningMessage.add(t);
            }

            for (Transaction s1 : warningMessage)
            {
                Log.println(Log.DEBUG, targetID, "priority: " + s1.priority);
                Log.println(Log.DEBUG, targetID, "type: " + s1.type);
                Log.println(Log.DEBUG, targetID, "subject: " + s1.subject);
                Log.println(Log.DEBUG, targetID, "detail: " + s1.detail);
                Log.println(Log.DEBUG, targetID, "timestamp: " + s1.timestamp);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return warningMessage;
    }

    public String timeStampMS2dateTimeString(Long timeStampMS) {
        Date dateTime = new Date(timeStampMS);
        String dateTimeString = formatter.format(dateTime);
        Log.println(Log.DEBUG, targetID, "DateTime : " + dateTime);
        return dateTimeString;
    }

    public HashMap<String, String> jsonObjectConvertToHashMap(String jsonString) {
        Log.println(Log.INFO, targetID, "zzz convertToHashMap"+jsonString);
        HashMap<String, String> myHashMap = new HashMap<String, String>();
        try {
//            JSONArray jArray = new JSONArray(jsonString);
//            JSONObject jObject = null;
//            String keyString=null;
//            Log.println(Log.DEBUG, targetID, "zzz jArray.length"+jArray.length());
//            for (int i = 0; i < jArray.length(); i++) {
//                jObject = jArray.getJSONObject(i);
//                Log.println(Log.DEBUG, targetID, "zzz jObject = "+jObject);
//                // beacuse you have only one key-value pair in each object so I have used index 0
//                keyString = (String)jObject.names().get(0);
//                Log.println(Log.DEBUG, targetID, "zzz keyString = "+keyString);
//                myHashMap.put(keyString, jObject.getString(keyString));
//            }

            JSONObject  jObject = new JSONObject(jsonString);
//            Log.println(Log.DEBUG, targetID, "zzz jObject = "+jObject);
//            // beacuse you have only one key-value pair in each object so I have used index 0
//            keyString = (String)jObject.names().get(0);
//            Log.println(Log.DEBUG, targetID, "zzz keyString = "+keyString);
//            myHashMap.put(keyString, jObject.getString(keyString));

            String name = "";
            String url = "";
            String msg = "";

            Iterator<String> iter = jObject.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                Log.d("Key ",key);
                switch (key) {
                    case "name":
                        name = jObject.getString(key);
                        Log.d("name : ", name);
                        myHashMap.put(key, jObject.getString(key));
                        break;
                    case "url":
                        url = jObject.getString(key);
                        Log.d("url : ", url);
                        myHashMap.put(key, jObject.getString(key));
                        break;
                    case "msg":
                        msg = jObject.getString(key);
                        Log.d("msg : ", msg);
                        myHashMap.put(key, jObject.getString(key));
                        break;
                }
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.println(Log.DEBUG, targetID, "zzz error = "+e);
        }
        return myHashMap;
    }

    public HashMap<String, String> jsonArrayConvertToHashMap(String jsonString) {
        Log.println(Log.INFO, targetID, "zzz convertToHashMap"+jsonString);
        HashMap<String, String> myHashMap = new HashMap<String, String>();
        try {
            JSONArray jArray = new JSONArray(jsonString);
            JSONObject jObject = null;
//            String keyString=null;

            String name = "";
            String url = "";
            String msg = "";

            Log.println(Log.DEBUG, targetID, "zzz jArray.length"+jArray.length());
            for (int i = 0; i < jArray.length(); i++) {
                jObject = jArray.getJSONObject(i);

                Iterator<String> iter = jObject.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    Log.d("Key ",key);
                    switch (key) {
                        case "name":
                            name = jObject.getString(key);
                            Log.d("name : ", name);
                            myHashMap.put(key, jObject.getString(key));
                            break;
                        case "url":
                            url = jObject.getString(key);
                            Log.d("url : ", url);
                            myHashMap.put(key, jObject.getString(key));
                            break;
                        case "msg":
                            msg = jObject.getString(key);
                            Log.d("msg : ", msg);
                            myHashMap.put(key, jObject.getString(key));
                            break;
                    }
                }

//                Log.println(Log.DEBUG, targetID, "zzz jObject = "+jObject);
//                // beacuse you have only one key-value pair in each object so I have used index 0
//                keyString = (String)jObject.names().get(0);
//                Log.println(Log.DEBUG, targetID, "zzz keyString = "+keyString);
//                myHashMap.put(keyString, jObject.getString(keyString));
            }



        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.println(Log.DEBUG, targetID, "zzz error = "+e);
        }
        return myHashMap;
    }







    public static void toastNow(Context context, String message, int txtcolor)
    {
//        Toast toast = Toast.makeText(context,message, Toast.LENGTH_LONG);
//        toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);

        //        設定Toast的Radius & Background color
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius(20);
//        shape.setColor(context.getResources().getColor(R.color.LightYellow));
        shape.setColor(context.getResources().getColor(R.color.red));
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);

        TextView textView = new TextView(context);

//        textView.setBackgroundColor(context.getResources().getColor(R.color.LightBlue));
//        textView.setBackgroundColor(Color.YELLOW);
//        textView.setTextColor(Color.YELLOW);
        textView.setTextColor(txtcolor);
        //        設定Toast的字型大小
        textView.setTextSize(16);
        //        設定Toast的字型樣式
        Typeface typeface = Typeface.create("serif", Typeface.BOLD);
        textView.setTypeface(typeface);
        textView.setPadding(10, 10, 10, 10);
        textView.setBackground(shape);
//        textView.setGravity(Gravity.CENTER);
        textView.setText(message);

        toast.setView(textView);
        toast.show();
    }


    public static byte[] EncryptAES(byte[] iv, byte[] key,byte[] text)
    {
        try
        {
            AlgorithmParameterSpec mAlgorithmParameterSpec = new IvParameterSpec(iv);
            SecretKeySpec mSecretKeySpec = new SecretKeySpec(key, "AES");
            Cipher mCipher = null;
            mCipher = Cipher.getInstance("AES/CBC/NoPadding");
//            mCipher = Cipher.getInstance("AES");
            mCipher.init(Cipher.ENCRYPT_MODE,mSecretKeySpec,mAlgorithmParameterSpec);

            return mCipher.doFinal(text);
        }
        catch(Exception ex)
        {
            return null;
        }
    }

    //AES解密，帶入byte[]型態的16位英數組合文字、32位英數組合Key、需解密文字
    public static byte[] DecryptAES(byte[] iv,byte[] key,byte[] text)
    {
        try
        {
            AlgorithmParameterSpec mAlgorithmParameterSpec = new IvParameterSpec(iv);
            SecretKeySpec mSecretKeySpec = new SecretKeySpec(key, "AES");
            Cipher mCipher = Cipher.getInstance("AES/CBC/NoPadding");
//            Cipher mCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//            Cipher mCipher = Cipher.getInstance("CBC");
//            Cipher mCipher = Cipher.getInstance("AES");
            mCipher.init(Cipher.DECRYPT_MODE,
                    mSecretKeySpec,
                    mAlgorithmParameterSpec);

            return mCipher.doFinal(text);
        }
        catch(Exception ex)
        {
            System.out.println("Error");
            return null;
        }
    }

    public static byte[] hexToBin(String str)
    {
        int len = str.length();
        byte[] out = new byte[len / 2];
        int endIndx;

        for (int i = 0; i < len; i = i + 2)
        {
            endIndx = i + 2;
            if (endIndx > len)
                endIndx = len - 1;
            out[i / 2] = (byte) Integer.parseInt(str.substring(i, endIndx), 16);
        }
        return out;
    }
    public static String hexToString(String hex) throws UnsupportedEncodingException, DecoderException {
//        return new String(Hex.decodeHex(hex.toCharArray()), "US-ASCII");
        return new String(Hex.decodeHex(hex.toCharArray()));
    }

    //在non-Activity Class上使用, 就必須要傳一個Activity Class的context參數過來
    public boolean isNetworkOnline(Context context) {
        boolean status=false;
        try{
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState()==NetworkInfo.State.CONNECTED) {
                status= true;
            }else {
                netInfo = cm.getNetworkInfo(1);
                if(netInfo!=null && netInfo.getState()== NetworkInfo.State.CONNECTED)
                    status= true;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return status;

    }
//     //在Activity Class上使用
//    public boolean isNetworkOnline() {
//        boolean status=false;
//        try{
//            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo netInfo = cm.getNetworkInfo(0);
//            if (netInfo != null && netInfo.getState()==NetworkInfo.State.CONNECTED) {
//                status= true;
//            }else {
//                netInfo = cm.getNetworkInfo(1);
//                if(netInfo!=null && netInfo.getState()== NetworkInfo.State.CONNECTED)
//                    status= true;
//            }
//        }catch(Exception e){
//            e.printStackTrace();
//            return false;
//        }
//        return status;
//
//    }

    public void delayMS(int delayValue) {

        try
        {
            Thread.sleep(delayValue); // do nothing for 1000 miliseconds (1 second)
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    private static String convertStreamToString(InputStream is) throws UnsupportedEncodingException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public void appendLog(String text)
    {
        File logPath= new File("sdcard/Log");
        File logFile = new File("sdcard/Log/log.file");

        if (!logPath.exists()) { // 判斷目錄是否存在

            Log.println(Log.INFO, targetID, "Directory not exist");
            logPath.mkdirs(); // 建立目錄
        }
        else {

            Log.println(Log.INFO, targetID, "Directory exist");
        }

        if (!logFile.exists())
        {
            try
            {
                Log.println(Log.INFO, targetID, "Create File");
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                Log.println(Log.INFO, targetID, "Create File Failure");
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            Log.println(Log.INFO, targetID, "Directory exist, Write Data");
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            Log.println(Log.INFO, targetID, "Directory exist, Write Data error");
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public JSONArray HashMapArray2JSONArray(ArrayList<HashMap<String,String>> parameter) {
        List<JSONObject> jsonObj = new ArrayList<JSONObject>();

        for(HashMap<String, String> data : parameter) {
            JSONObject obj = new JSONObject(data);
            jsonObj.add(obj);
        }

        JSONArray result = new JSONArray(jsonObj);

        Log.println(Log.DEBUG, targetID, "JSON Array Result--> "+result.toString());

        return result;
    }

    public String sendPostCoomad(String url, String postData) {

        OkHttpPostHandler locationTask = new OkHttpPostHandler();
//        String url = "http://vorder.net/demo/url_test.php";
        //以下的方式可以拿到async task執行後回傳的result
        String asyncResult = "";
        try {
//                        asyncResult = locationTask.execute(url, eddyStone).get();
            asyncResult = locationTask.execute(url, postData).get();
            Log.println(Log.DEBUG, targetID, "asyncResult = " + asyncResult);

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.println(Log.ERROR, targetID, "InterruptedException Error" + e);
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.println(Log.ERROR, targetID, "ExecutionException Error" + e);
        }

        Log.println(Log.INFO, targetID, "asyncResult Data : " + asyncResult);
        return asyncResult;
    }

    public Pair<Integer,Integer> getDisplayParameter(Context curContext) {

        DisplayMetrics dm = curContext.getResources().getDisplayMetrics();
/*
        double x = Math.pow(dm.widthPixels/dm.xdpi,2);
        double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
        double screenInches = Math.sqrt(x + y);
        System.out.println("xxx : " + x);
        System.out.println("yyy : " + y);
        System.out.println("widthPixels : " + dm.widthPixels);
        System.out.println("heightPixels : " + dm.heightPixels);
        System.out.println("xdpi : " + dm.xdpi);
        System.out.println("ydpi : " + dm.ydpi);
        System.out.println("Screen inches : " + screenInches);
*/

        return new Pair<Integer,Integer> (dm.widthPixels, dm.heightPixels);
    }

    /**
     * Covert dp to px
     * @param dp
     * @param context
     * @return pixel
     */
    public static float convertDpToPixel(float dp, Context context){
        float px = dp * getDensity(context);
        return px;
    }
    /**
     * Covert px to dp
     * @param px
     * @param context
     * @return dp
     */
    public static float convertPixelToDp(float px, Context context){
        float dp = px / getDensity(context);
        return dp;
    }
    /**
     * 取得螢幕密度
     * 120dpi = 0.75
     * 160dpi = 1 (default)
     * 240dpi = 1.5
     * @param context
     * @return
     */
    public static float getDensity(Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.density;
    }


}
