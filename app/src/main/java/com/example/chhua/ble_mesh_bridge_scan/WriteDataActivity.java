package com.example.chhua.ble_mesh_bridge_scan;

/**
 * Created by Hua_Home_1 on 2017/8/25.
 */


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class WriteDataActivity extends AppCompatActivity {

    private final static String TAG = WriteDataActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private String mDeviceName;
    private String mDeviceAddress;
    private String mPostData;

    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothAdapter mBluetoothAdapter;
    public static BluetoothGatt mBluetoothGatt;

    TextView textViewState;
    private ExpandableListView mGattServicesList;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

    WebView mWebView;
    EditText outputData;
    Button sendData, reconnect;
    Button webviewBtn, commandBtn;
    Tools tools = new Tools();
    Context mContext = this;
    BluetoothGattCharacteristic mReadCharacteristic, mWriteCharacteristic;
    String readUUID = "468e6033-aa75-2215-88ca-f9cfbb2575d5";
    String writeUUID = "468e6032-aa75-2215-88ca-f9cfbb2575d5";

    ArrayList<Transaction> warningMessage = new ArrayList<>();
    DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    boolean writeLogFile = true;
    boolean sendInquireCommand = false;

    ArrayList<HashMap<String,String>> inquireResponse = new ArrayList<HashMap<String,String>>();
    String BorardParameter = "item";

    boolean back2MainActivity = true;
    String postResult = "";

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            if(writeLogFile) {
                tools.appendLog("onServiceConnected");
            }
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                if(writeLogFile) {
                    tools.appendLog("Unable to initialize Bluetooth");
                }
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "BroadcastReceiver");
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState("CONNECTED");
                Log.d(TAG, "CONNECTED");
                if(writeLogFile) {
                    tools.appendLog("CONNECTED");
                }
//                sendData.setEnabled(true);
//                outputData.setEnabled(true);
                reconnect.setVisibility(View.INVISIBLE);
                webviewBtn.setEnabled(true);
                commandBtn.setEnabled(true);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState("DISCONNECTED");
                Log.d(TAG, "DISCONNECTED1");
                if(writeLogFile) {
                    tools.appendLog("DISCONNECTED");
                }
                tools.toastNow(mContext, "藍芽已經中斷連接, 請重新連接藍芽再使用", Color.WHITE);
//                sendData.setEnabled(false);
//                outputData.setEnabled(false);
                reconnect.setVisibility(View.VISIBLE);
                webviewBtn.setEnabled(false);
                commandBtn.setEnabled(false);
//                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
                Log.d(TAG, "ACTION_GATT_SERVICES_DISCOVERED");
                if(writeLogFile) {
                    tools.appendLog("ACTION_GATT_SERVICES_DISCOVERED");
                }
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
//                displayData(intent.getExtras().getString("RAW_DATA_VALUE"));
                Log.d(TAG, "ACTION_GATT_SERVICES_DISCOVERED");
                if(writeLogFile) {
                    tools.appendLog("ACTION_GATT_SERVICES_DISCOVERED");
                }
            }

        }
    };

    private void clearUI() {
        Log.d(TAG, "clearUI");
        if(writeLogFile) {
            tools.appendLog("clearUI");
        }
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
    }

    private void updateConnectionState(final String st) {
        Log.d(TAG, "updateConnectionState");
        if(writeLogFile) {
            tools.appendLog("updateConnectionState");
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewState.setText(st);
                Log.d(TAG, "updateConnectionState = " + st);
                if(writeLogFile) {
                    tools.appendLog("updateConnectionState = " + st);
                }
            }
        });
    }

    private void displayData(String data) {
        Log.d(TAG, "displayData");
        if(writeLogFile) {
            tools.appendLog("displayData");
        }
        String chkString = "";
        if (data != null) {
//            textViewState.setText(data);
            //3個character+"/n"+9個hex character = 13
            String realHexString = "";
            if(data.length() >= 13) {
                int startPos = data.indexOf("\n");
                String retValue = data.substring(startPos + 7, startPos + 9);
                chkString = data.substring(startPos + 10, startPos + 15).replace(" ", "");
                realHexString = (data.substring(startPos+1, data.length()).replace(" ", ""));

                Log.d(TAG, "displayData  length= " + data.length());
                Log.d(TAG, "displayData  length1= " + startPos);
                Log.d(TAG, "displayData  length2= " + retValue);
                Log.d(TAG, "displayData  length3= " + realHexString);
                Log.d(TAG, "displayData  length4= " + chkString);
                if(writeLogFile) {
                    tools.appendLog("displayData  length= " + data.length());
                    tools.appendLog("displayData  length1= " + startPos);
                    tools.appendLog("displayData  length2= " + retValue);
                    tools.appendLog("displayData  length3= " + realHexString);
                    tools.appendLog("displayData  length4= " + chkString);
                }
            }

            Log.d(TAG, "displayData = " + data);
            if(writeLogFile) {
                tools.appendLog("displayData = " + data);
            }

            if(sendInquireCommand) {
//                if(chkString.equals("FFFF")) {
//                    mBluetoothLeService.readCharacteristic(mReadCharacteristic);
//                    if (writeLogFile) {
//                        tools.appendLog("set setCharacteristicNotification");
//                    }
//                    mBluetoothLeService.setCharacteristicNotification(mReadCharacteristic, true);
//                }
                Log.d(TAG, "Get item list Data"+realHexString);
                if(writeLogFile) {
                    tools.appendLog("Get item list Data"+realHexString);
                }
                HashMap<String, String> requestItem = new HashMap<String, String>();
                requestItem.put(BorardParameter, realHexString);
                inquireResponse.add(requestItem);
            }
            if(chkString.equals("FFFF")) {
                sendInquireCommand = false;
            }

        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        Log.d(TAG, "displayGattServices");
        if(writeLogFile) {
            tools.appendLog("displayGattServices");
        }

        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = "Unknown Service";
        String unknownCharaString = "Unknown Characteristic";
        ArrayList<HashMap<String, String>> gattServiceData =
                new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();

                if(uuid.equals(writeUUID)) {
                    Log.d(TAG, "Got Write Channel  ID");
                    if(writeLogFile) {
                        tools.appendLog("Got Write Channel  ID");
                    }
                    mWriteCharacteristic = gattCharacteristic;
                }
                else if(uuid.equals(readUUID)) {
                    Log.d(TAG, "Got Read Channel ID");
                    if(writeLogFile) {
                        tools.appendLog("Got Read Channel ID");
                    }
                    mReadCharacteristic = gattCharacteristic;

                    mBluetoothLeService.setCharacteristicNotification(mReadCharacteristic, true);
//                    tools.delayMS(400);
//                    writeData2BLE("0004");
                }

                currentCharaData.put(
                        LIST_NAME, lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);

            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
//                mGattServicesList.setAdapter(gattServiceAdapter);

//        BluetoothGattCharacteristic mWriteCharacteristic;
//
//        //以下是新的藍芽裝置的Write Channel
//        System.out.println("XXXXXZZ--------------->X600a");
//        String systemId = "468e6032-aa75-2215-88ca-f9cfbb2575d5";
//        for(int i =0; i<mGattCharacteristics.size(); i++){
//            for(int j =0; j<mGattCharacteristics.get(i).size(); j++){
//                if(mGattCharacteristics.get(i).get(j).getUuid().toString().equals(systemId)){
//                    System.out.println("ttXXXXXZZ--------------->X60");
//                    mWriteCharacteristic = mGattCharacteristics.get(i).get(j);
////                    byte[] dataBytes = new byte[2];
////                    dataBytes[0] = ( byte)61;          //0xff 的 bit 7 Wake-On-Motion Enable
////                    dataBytes[1] = ( byte)61;          //設定三軸加速路的Base 0=2G, 1=4G, 2=8G, 3=16G
//
//                    String example = "abcdefghij";
//                    String aa1 = "000009436F636120436F6C61";
//
//                    byte[] aa2 = hexToByteArray(aa1);
//                    System.out.println("XXXXX--------------->X61  "+ Arrays.toString(aa2));
//                    //String to Bytes
//                    byte[] dataBytes = example.getBytes();
//                    System.out.println("XXXXX--------------->X61  "+ Arrays.toString(dataBytes));
//                    mWriteCharacteristic.setValue(dataBytes);
//                    mWriteCharacteristic.setWriteType(1);
//                    mBluetoothLeService.writeCharacteristic(mWriteCharacteristic);
//                    System.out.println("XXXXX--------------->X62");
//                }
//            }
//        }

//        delayMS(400);
//
//        //以下是新的藍芽裝置的Read Channel
//        System.out.println("XXXXXZZ--------------->X601");
//        systemId = "468e6033-aa75-2215-88ca-f9cfbb2575d5";
//        for (int i = 0; i < mGattCharacteristics.size(); i++) {
//            for (int j = 0; j < mGattCharacteristics.get(i).size(); j++) {
//                if (mGattCharacteristics.get(i).get(j).getUuid().toString().equals(systemId)) {
//                    System.out.println("XXXXXZZ--------------->XA65");
//                    final BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(i).get(j);
//                    System.out.println("XXXXXZZ--------------->X601A  "+ characteristic);
//                    mBluetoothLeService.readCharacteristic(characteristic);
//                    mBluetoothLeService.setCharacteristicNotification(characteristic, true);
//                    System.out.println("XXXXXZZ--------------->XA67");
//                }
//            }
//        }


    }

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    Log.d(TAG, "ExpandableListView.OnChildClickListener");
//                    writeData2BLE();
//                    if (mGattCharacteristics != null) {
//                        final BluetoothGattCharacteristic characteristic =
//                                mGattCharacteristics.get(groupPosition).get(childPosition);
//                        final int charaProp = characteristic.getProperties();
//                        Log.d(TAG, "ExpandableListView.OnChildClickListener1");
//                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//                            // If there is an active notification on a characteristic, clear
//                            // it first so it doesn't update the data field on the user interface.
//                            if (mNotifyCharacteristic != null) {
//                                mBluetoothLeService.setCharacteristicNotification(
//                                        mNotifyCharacteristic, false);
//                                mNotifyCharacteristic = null;
//                            }
//                            mBluetoothLeService.readCharacteristic(characteristic);
//                        }
//                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
//                            mNotifyCharacteristic = characteristic;
//                            mBluetoothLeService.setCharacteristicNotification(
//                                    characteristic, true);
//                        }
//                        return true;
//                    }
                    return false;
                }
            };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.functionlist);

        //隱藏ActionBar(App最上面的工具欄)
        android.support.v7.app.ActionBar m_myActionBar = getSupportActionBar();
        m_myActionBar.hide();

        Log.d(TAG, "WriteDataActivity onCreate");
        if(writeLogFile) {
            tools.appendLog("WriteDataActivity onCreate");
        }
//        tools.appendLog("WriteDataActivity onCreate");

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mPostData = intent.getStringExtra("POST_DATA");

        Log.d(TAG, "deviceInfo6 test--> "+mPostData);
//        TextView textViewDeviceName = (TextView)findViewById(R.id.textDeviceName);
//        TextView textViewDeviceAddr = (TextView)findViewById(R.id.textDeviceAddress);
        textViewState = (TextView)findViewById(R.id.fconnectStatus);
        webviewBtn = (Button) findViewById(R.id.webviewButton);
        commandBtn = (Button) findViewById(R.id.commandButton);
//        outputData = (EditText) findViewById(R.id.writeData);
//        sendData = (Button) findViewById(R.id.sendButton);
        reconnect = (Button) findViewById(R.id.freconnect);

//        textViewDeviceName.setText(mDeviceName);
//        textViewDeviceAddr.setText(mDeviceAddress);

//        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
//        mGattServicesList.setOnChildClickListener(servicesListClickListner);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        HashMap<String, String> requestItem = new HashMap<String, String>();
        requestItem.put(BorardParameter, "00040000030000054170706C65");
        inquireResponse.add(requestItem);
        requestItem = new HashMap<String, String>();
        requestItem.put(BorardParameter, "000400000300010642616E616E61");
        inquireResponse.add(requestItem);
        requestItem = new HashMap<String, String>();
        requestItem.put(BorardParameter, "0004000003000206436865727279");
        inquireResponse.add(requestItem);


        
//        writeData2BLE("0004");



    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if(writeLogFile) {
            tools.appendLog("onResume");
        }
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
            if(writeLogFile) {
                tools.appendLog("Connect request result=" + result);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        if(writeLogFile) {
            tools.appendLog("onPause");
        }
        unregisterReceiver(mGattUpdateReceiver);
    }


    @Override
    protected void onSaveInstanceState (Bundle outState) {
        Log.println(Log.INFO, TAG, "System onSaveInstanceState");
        if(writeLogFile) {
            tools.appendLog("System onSaveInstanceState");
        }

    }

    @Override
    protected void onRestoreInstanceState (Bundle savedInstanceState) {
        Log.println(Log.INFO, TAG, "System onRestoreInstanceState");
        if(writeLogFile) {
            tools.appendLog("System onRestoreInstanceState");
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.println(Log.INFO, TAG, "System onMonitor Rotate");
        if(writeLogFile) {
            tools.appendLog("System onMonitor Rotate");
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        // your code.
        Log.println(Log.INFO, TAG, "System onBackPressed");
        if(writeLogFile) {
            tools.appendLog("System onBackPressed");
        }
        if(back2MainActivity) {
            mBluetoothLeService.disconnect();
            super.onBackPressed();
        }
        else {
            back2MainActivity = true;
            doBackReturn();
        }
    }

    @Override
    public void onStop(){
        Log.println(Log.INFO, TAG, "System onStop");
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        Log.println(Log.INFO, TAG, "System onDestroy");
        if(writeLogFile) {
            tools.appendLog("System onDestroy");
        }
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        super.onDestroy();
    }

//    @Override
//    public void onBackPressed() {
//        // your code.
//        Log.println(Log.INFO, TAG, "System onBackPressed");
//        mBluetoothLeService.disconnect();
//        final Intent intent = new Intent(ControlActivity.this,
//                MainActivity.class);
//        startActivity(intent);
//    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        Log.d(TAG, "IntentFilter makeGattUpdateIntentFilter");
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private static HashMap<String, String> attributes = new HashMap();

    public static String lookup(String uuid, String defaultName) {
        Log.d(TAG, "lookup");
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }

//    public void delayMS(int delayValue) {
//
//        try
//        {
//            Thread.sleep(delayValue); // do nothing for 1000 miliseconds (1 second)
//        }
//        catch(InterruptedException e)
//        {
//            e.printStackTrace();
//        }
//    }

    public static byte[] hexToByteArray(String hex) {
        hex = hex.length()%2 != 0?"0"+hex:hex;

        byte[] b = new byte[hex.length() / 2];

        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(hex.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    public void writeData2BLE(String writeData) {

        if(mConnected) {
            String inquireCommand = "0004";

            if (writeData.equals(inquireCommand)) {
//                sendInquireCommand = true;
                sendInquireCommand = true;
//                inquireResponse = new ArrayList<HashMap<String,String>>();
                inquireResponse.clear();
            } else {
                sendInquireCommand = false;
            }


//            mBluetoothLeService.setCharacteristicNotification(mReadCharacteristic, true);
//
//            tools.delayMS(400);

            byte[] hexData = hexToByteArray(writeData);
            System.out.println("XXXXX--------------->X61Hex  " + Arrays.toString(hexData));
            if (writeLogFile) {
                tools.appendLog("XXXXX--------------->X61Hex  " + Arrays.toString(hexData));
            }
            mWriteCharacteristic.setValue(hexData);
            mWriteCharacteristic.setWriteType(1);
            mBluetoothLeService.writeCharacteristic(mWriteCharacteristic);

//            if(!sendInquireCommand) {
//                tools.delayMS(400);
//                mBluetoothLeService.readCharacteristic(mReadCharacteristic);
//            }

//            tools.delayMS(400);
//
//            mBluetoothLeService.setCharacteristicNotification(mReadCharacteristic, true);
//
//            mBluetoothLeService.readCharacteristic(mReadCharacteristic);

//            tools.delayMS(2000);
//
//            mBluetoothLeService.readCharacteristic(mReadCharacteristic);
//            if (writeLogFile) {
//                tools.appendLog("set setCharacteristicNotification");
//            }
//            mBluetoothLeService.setCharacteristicNotification(mReadCharacteristic, true);
        }
        else {

            tools.toastNow(mContext, "藍芽已經中斷連接, 請重新連接藍芽再使用", Color.WHITE);
        }
    }

    public void sendDataClick(View v) {
        Log.println(Log.INFO, TAG, "sendData Click");
        if(writeLogFile) {
            tools.appendLog("sendData Click");
        }

        String writeData = outputData.getText().toString();
        if(writeData.isEmpty()) {
            tools.toastNow(mContext, "不可以用空的指令字串寫入資料", Color.WHITE);
        }
        else {
            outputData.setText("");
            writeData2BLE(writeData);
        }



    }

    public void rescanClick(View v) {
        Log.println(Log.INFO, TAG, "rescan Click");
        if(writeLogFile) {
            tools.appendLog("rescan Click");
        }

        back2MainActivity = true;
        onBackPressed();
//        final Intent intent = new Intent(WriteDataActivity.this, MainActivity.class);
//        startActivity(intent);
//
//        finish(); //後來自己加上的

    }

    public void reconnectClick(View v) {
        Log.println(Log.INFO, TAG, "reconnect Click");
        if(writeLogFile) {
            tools.appendLog("reconnect Click");
        }

        mBluetoothLeService.connect(mDeviceAddress);


    }

    public void backReturnClick(View v) {
        Log.println(Log.INFO, TAG, "backReturn Click");
        if(writeLogFile) {
            tools.appendLog("backReturn Click");
        }
        back2MainActivity = false;
        onBackPressed();
//        tools.appendLog("backReturn Click");

//        setContentView(R.layout.functionlist);
//
//        webviewBtn = (Button) findViewById(R.id.webviewButton);
//        commandBtn = (Button) findViewById(R.id.commandButton);
//
//        textViewState = (TextView)findViewById(R.id.fconnectStatus);
//        reconnect = (Button) findViewById(R.id.freconnect);
//
//        if(mConnected) {
//            textViewState.setText("CONNECTED");
//            reconnect.setVisibility(View.INVISIBLE);
//            webviewBtn.setEnabled(true);
//            commandBtn.setEnabled(true);
//        }
//        else {
//            Log.println(Log.INFO, TAG, "backReturn Click DISCONNECTED");
//            textViewState.setText("DISCONNECTED");
//            reconnect.setVisibility(View.VISIBLE);
//            webviewBtn.setEnabled(false);
//            commandBtn.setEnabled(false);
//            tools.toastNow(mContext, "藍芽已經中斷連接, 請重新連接藍芽再使用", Color.WHITE);
//        }
//        doBackReturn();

    }

    public void commandClick(View v) {
        Log.println(Log.INFO, TAG, "command Click");
        if(writeLogFile) {
            tools.appendLog("command Click");
        }
//        tools.appendLog("command Click");

        setContentView(R.layout.command_page);

        textViewState = (TextView)findViewById(R.id.cconnectStatus);
        outputData = (EditText) findViewById(R.id.writeData);
        sendData = (Button) findViewById(R.id.sendButton);
        reconnect = (Button) findViewById(R.id.creconnect);
        back2MainActivity = false;

        if(mConnected) {
            textViewState.setText("CONNECTED");
            reconnect.setVisibility(View.INVISIBLE);
        }
        else {
            textViewState.setText("DISCONNECTED");
            reconnect.setVisibility(View.VISIBLE);
            tools.toastNow(mContext, "藍芽已經中斷連接, 請重新連接藍芽再使用", Color.WHITE);
        }

    }

    public void webviewClick(View v) {
        Log.println(Log.INFO, TAG, "Webview Click");
        if(writeLogFile) {
            tools.appendLog("Webview Click");
//            writeData2BLE("0004");
        }
//        tools.appendLog("Webview Click");

        setContentView(R.layout.webview_page);

        textViewState = (TextView)findViewById(R.id.wconnectStatus);
        reconnect = (Button) findViewById(R.id.wreconnect);
        back2MainActivity = false;

        if(mConnected) {
            textViewState.setText("CONNECTED");
            reconnect.setVisibility(View.INVISIBLE);
        }
        else {
            textViewState.setText("DISCONNECTED");
            reconnect.setVisibility(View.VISIBLE);
            tools.toastNow(mContext, "藍芽已經中斷連接, 請重新連接藍芽再使用xxx", Color.WHITE);
        }

        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setBackgroundColor(Color.parseColor("#d9f1d8"));
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(mWebViewClient);
        mWebView.setWebChromeClient(new WebChromeClient());

//        mWebView.loadUrl("https://xsize.net/air/enviornment.php?MonitorDEVID=0og29hvpvsm9y7vv");
//        mWebView.loadUrl("https://account.ziellon.net/chatroom.html");
//        mWebView.loadUrl("http://tools.ziellon.net/sdbis_demo.html");
//        mWebView.loadUrl("http://vorder.net/demo/update.php");
//        String postResult = "http://vorder.net/demo/product_list.php";
//        mWebView.loadUrl("http://vorder.net/demo/product_list.php");

//        url = "http://vorder.net/demo/product_list.php";
//        String postResult = tools.sendPostCoomad(url, asyncResult);

        mWebView.loadUrl("http://vorder.net/demo/product_list.php");

        JSONArray test1 = tools.HashMapArray2JSONArray(inquireResponse);
        Log.println(Log.DEBUG, TAG, "deviceInfo2 test1--> "+test1.toString());
        if(writeLogFile) {
            tools.appendLog("deviceInfo2 test1--> "+test1.toString());
        }

        String url =  "http://vorder.net/demo/app_command.php";
        postResult = tools.sendPostCoomad(url, test1.toString());

//        OkHttpPostHandler locationTask = new OkHttpPostHandler();
//        String url =  "http://vorder.net/demo/app_command.php";
////        String url = "http://vorder.net/demo/url_test.php";
//        //以下的方式可以拿到async task執行後回傳的result
//        String asyncResult = "";
//        try {
//            //                        asyncResult = locationTask.execute(url, eddyStone).get();
//            asyncResult = locationTask.execute(url, test1.toString()).get();
//            Log.println(Log.DEBUG, TAG, "asyncResult = " + asyncResult);
//            if(writeLogFile) {
//                tools.appendLog("asyncResult = " + asyncResult);
//            }
//
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            Log.println(Log.ERROR, TAG, "InterruptedException Error" + e);
//            if(writeLogFile) {
//                tools.appendLog("InterruptedException Error" + e);
//            }
//        } catch (ExecutionException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            Log.println(Log.ERROR, TAG, "ExecutionException Error" + e);
//            if(writeLogFile) {
//                tools.appendLog("InterruptedException Error" + e);
//            }
//        }
//
//        Log.println(Log.INFO, TAG, "asyncResult Data : "+asyncResult);
//        if(writeLogFile) {
//            tools.appendLog("asyncResult Data : "+asyncResult);
//        }

        Log.println(Log.INFO, TAG, "postResult Data : "+postResult);
        if(writeLogFile) {
            tools.appendLog("postResult Data : "+postResult);
        }

        mWebView.setWebViewClient(new WebViewClient() {
            //Waiting Webview Page Loading Finish
            public void onPageFinished(WebView view, String url) {
                // do your stuff here
                Log.println(Log.INFO, TAG, "onPageFinished");
                Log.println(Log.DEBUG, TAG, "postResult Data : "+postResult);

                if(!postResult.isEmpty()) {
                    mWebView.loadUrl("javascript: display(" + postResult + ");");
                    postResult = "";
                }

            }
        });



//        mWebView.loadDataWithBaseURL("same://ur/l/tat/does/not/work", "data", "text/html", "utf-8", null);

//        mWebView.addJavascriptInterface(new JavaScriptInterface(this, mWebView), "sdbisAppHandler");
//        mWebView.addJavascriptInterface(new JavaScriptInterface(this, mWebView), "BeaconsAppHandler");

        WebViewJavascriptConsole console = new WebViewJavascriptConsole(this, mWebView);
        console.register("esl", new WebViewJavascriptConsole.Listener() { public void run(JSONObject data){

            Log.println(Log.DEBUG, TAG, "JSONObject --> "+data);
            if(writeLogFile) {
                tools.appendLog("JSONObject --> "+data);
            }
//            System.out.println("JSONObject --> "+data);
            try {
                String bleCommand = data.getString("data");
                writeData2BLE(bleCommand);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }});



        mWebView.addJavascriptInterface(console,"BeaconsAppHandler");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

//        mWebView.reload();
    }

    WebViewClient mWebViewClient = new WebViewClient() {
//        @Override
//        public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
//
//            view.loadUrl(url);
//            return true;
//        }
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }
    };

    public void javascriptCallFinished(final String json){

////        Log.println(Log.VERBOSE, TAG,"MyActivity.javascriptCallFinished is called : "+json);
////        if(broadcastAction) {
////            warningMessage = tools.parseJSON(json);
//        Log.println(Log.VERBOSE, TAG,"javascriptCallFinished is called : " + json);
//        if(writeLogFile) {
//            tools.appendLog("javascriptCallFinished is called : " + json);
//        }
//        writeData2BLE(json);
////            String dateTimeString = tools.timeStampMS2dateTimeString(warningMessage.get(0).timestamp * 1000L);
//////        Toast.makeText(this, "收到的訊息內容 :\n\n " + warningMessage.get(0).priority + "\n" + warningMessage.get(0).type + "\n"
//////                + warningMessage.get(0).subject + "\n" + warningMessage.get(0).detail + "\n" + dateTimeString, Toast.LENGTH_SHORT).show();
////
////            //  Send Broadcast Action
//////        int nextRandomNumber = mRandom.nextInt(100);
////            // Initialize a new Intent object
////
////
////            Log.println(Log.VERBOSE, TAG,"MyActivity.javascriptCallFinished is called : 1 " + json);
////            Intent intent = new Intent();
////            // Set an action for the Intent
////            intent.setAction("WARNING_MESSAGE_INTENT");
////            // Put an integer value Intent to broadcast it
//////        intent.putExtra("RandomNumber",nextRandomNumber);
//////        intent.putExtra("priority", warningMessage.get(0).priority);
////            String[] warningMsg = {warningMessage.get(0).priority, warningMessage.get(0).type, warningMessage.get(0).subject,
////                    warningMessage.get(0).detail, dateTimeString};
////            intent.putExtra("warningMsg", warningMsg);
////            sendBroadcast(intent);
////        }
//
////        Log.d("mylog", "Date  : " + dateTime);
//
//        // I need to run set operation of UI on the main thread.
//        // therefore, the above parameter "val" must be final
////        runOnUiThread(new Runnable() {
////            public void run() {
////                myResultView.setText("Callback got val: " + json);
////            }
////        });
    }

    void doBackReturn() {

        setContentView(R.layout.functionlist);

        webviewBtn = (Button) findViewById(R.id.webviewButton);
        commandBtn = (Button) findViewById(R.id.commandButton);

        textViewState = (TextView)findViewById(R.id.fconnectStatus);
        reconnect = (Button) findViewById(R.id.freconnect);

        if(mConnected) {
            textViewState.setText("CONNECTED");
            reconnect.setVisibility(View.INVISIBLE);
            webviewBtn.setEnabled(true);
            commandBtn.setEnabled(true);
        }
        else {
            Log.println(Log.INFO, TAG, "backReturn Click DISCONNECTED");
            textViewState.setText("DISCONNECTED");
            reconnect.setVisibility(View.VISIBLE);
            webviewBtn.setEnabled(false);
            commandBtn.setEnabled(false);
            tools.toastNow(mContext, "藍芽已經中斷連接, 請重新連接藍芽再使用", Color.WHITE);
        }
    }


}
