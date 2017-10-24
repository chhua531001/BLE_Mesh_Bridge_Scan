package com.example.chhua.ble_mesh_bridge_scan;

import android.os.AsyncTask;
import android.util.Log;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by chhua on 2017/8/24.
 */

public class OkHttpPostHandler extends AsyncTask<String, Void, String> {

    String targetID = this.getClass().getSimpleName();
    OkHttpClient client = new OkHttpClient();

    @Override
    protected String doInBackground(String...params) {
        Log.println(Log.INFO, targetID, "doInBackground");
//             //方法 一:
//            Request.Builder builder = new Request.Builder();
//            builder.url(params[0]);
//            builder.addHeader("Accept-Language", "zh-TW");
//            Request request = builder.build();

//             //方法 三:
//            HttpUrl url = new HttpUrl.Builder()
//                    .scheme("https")
//                    .host("maps.googleapis.com")
//                    .addPathSegment("maps")
//                    .addPathSegment("api")
//                    .addPathSegment("geocode")
//                    .addPathSegment("json")
//                    .addQueryParameter("latlng", "22.6259594,120.3208026")
//                    .addQueryParameter("language", "zh-TW")
//                    .build();
//
//            Log.d("My App", "url = "+url);
//
//            Request request = new Request.Builder()
//                    //給予想要解析網頁的網址, 要使用中文時, 請在querry中加入&language=zh-TW
////                .url("https://maps.googleapis.com/maps/api/geocode/json?latlng=22.6259594,120.3208026&language=zh-TW")
//                    .url(url)
//                    .build();

        //方法 二:

        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("data_pack", params[1]);
        RequestBody body = formBuilder.build();
        Log.println(Log.DEBUG, targetID, "params[0] = "+params[0]);
        Log.println(Log.DEBUG, targetID, "params[1] = "+params[1]);
        Log.println(Log.DEBUG, targetID, "body = "+body.toString().toString());
        Request request = new Request.Builder()
                .url(params[0])
                .post(body)
                .addHeader("Content-type", "text/x-markdown; charset=utf-8")
                .build();

        try {
            Response response = client.newCall(request).execute();
            String result = response.body().string();
//                        Log.d("OKHTTP", json);
            Log.println(Log.DEBUG, targetID, "Result = "+result);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            Log.println(Log.ERROR, targetID, "Exception Error"+e);
        }
        return null;
    }

    @Override
    //  若是使用String asyncResult = locationTask.execute(url, textString).get();的方式去拿到回傳的async data,
    // 以下的onPostExecute就可以不需要作了
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

//        byte[] decrypByte = tools.DecryptAES(ivByte, keyByte, Base64.decode(result.getBytes(), Base64.DEFAULT));
////        byte[] decrypByte = tools.DecryptAES(xx2, xx3, Base64.decode(textString.getBytes(), Base64.DEFAULT));
//        Log.println(Log.DEBUG, targetID, "DecryptAES 1= "+decrypByte);
//        String decrypString = new String(decrypByte);
//        Log.println(Log.DEBUG, targetID, "DecryptAES = "+decrypString);
    }
}
