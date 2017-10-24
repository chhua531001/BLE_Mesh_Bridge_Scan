package com.example.chhua.ble_mesh_bridge_scan;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;

    private boolean mScanning = false;

    private static final int RQS_ENABLE_BLUETOOTH = 51;
    private static final int RQS_ENABLE_ACCESS_FINE_LOCATION = 52;

//    Button scanButton, turnOnPermission;
    Button scanButton;
    ListView listViewLE;

//    List<BluetoothDevice> listBluetoothDevice;
    List<BluetoothDevice> listBluetoothDevice = new ArrayList<>();
    ListAdapter adapterLeScanResult;

    ArrayList<HashMap<String,Object>> deviceInfo = new ArrayList<HashMap<String,Object>>();
    ArrayList<HashMap<String,String>> requestParameter = new ArrayList<HashMap<String,String>>();

    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000;

    String targetID = this.getClass().getSimpleName();
    Tools tools = new Tools();
    Context mContext = this;
    Activity currentActivity = this;
    boolean btn4Scan = true;
    boolean writeLogFile = true;

    String Device = "Device";
    String DeviceID = "DeviceID";
    String ProductName = "ProductName";
    String UrlID = "UrlID";
    String RSSI = "rssi";
//    String RSSI1 = "rssi";
    String Eddystone = "beacon_id";
//    String BorardParameter = "item";

    LinearLayout title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //隱藏ActionBar(App最上面的工具欄)
        android.support.v7.app.ActionBar m_myActionBar = getSupportActionBar();
        m_myActionBar.hide();

        title = (LinearLayout) findViewById(R.id.llBorder);
//        turnOnPermission = (Button) findViewById(R.id.turnPermission);
        scanButton = (Button) findViewById(R.id.scanButton);
//        scanButton.setOnClickListener(scanDevice);

        //Check  ACCESS_FINE_LOCATION  Permission
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)+ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        Log.println(Log.INFO, targetID, "permissionCheck -->"+permissionCheck);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED){
            Log.println(Log.INFO, targetID, "PERMISSION_GRANTED11");
            Log.println(Log.INFO, targetID, "howRequestPermissionRationale -->"+ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION));

            //ACCESS_FINE_LOCATION在turn on時, 其ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) = true
            //WRITE_EXTERNAL_STORAGE在turn on時, 其ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) = false
            if ((!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
                    || (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, RQS_ENABLE_ACCESS_FINE_LOCATION);
                    Log.println(Log.INFO, targetID, "PERMISSION_GRANTED13");
                }
            }
        }else{
//            Toast.makeText(this,  "已經同意使用位置資訊訊息了", Toast.LENGTH_SHORT).show();
            tools.toastNow(mContext, "已經同意使用位置資訊訊息了", Color.WHITE);
        }

//        //Check  ACCESS_FINE_LOCATION  Permission
//        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
//        if (permissionCheck != PackageManager.PERMISSION_GRANTED){
//            Log.println(Log.INFO, targetID, "PERMISSION_GRANTED11");
//            if(writeLogFile) {
//                tools.appendLog("PERMISSION_GRANTED11");
//            }
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
////                Toast.makeText(this, "被要求授權BLE位置資料", Toast.LENGTH_SHORT).show();
//                tools.toastNow(mContext, "被要求授權BLE位置資料", Color.WHITE);
//                Log.println(Log.INFO, targetID, "PERMISSION_GRANTED12");
//                if(writeLogFile) {
//                    tools.appendLog("PERMISSION_GRANTED12");
//                }
//            }else{
//                if (Build.VERSION.SDK_INT >= 23) {
//                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, RQS_ENABLE_ACCESS_FINE_LOCATION);
//                    Log.println(Log.INFO, targetID, "PERMISSION_GRANTED13");
//                    if(writeLogFile) {
//                        tools.appendLog("PERMISSION_GRANTED13");
//                    }
//                }
//            }
//        }else{
////            Toast.makeText(this,  "已經同意使用位置資訊訊息了", Toast.LENGTH_SHORT).show();
//            tools.toastNow(mContext, "已經同意使用位置資訊訊息了", Color.WHITE);
//        }

        // Check if BLE is supported on the device.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
//            Toast.makeText(this, "BLUETOOTH_LE 不支援這個裝置!", Toast.LENGTH_SHORT).show();
            tools.toastNow(mContext, "BLUETOOTH_LE 不支援這個裝置!", Color.WHITE);
            finish();
        }

        getBluetoothAdapterAndLeScanner();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
//            Toast.makeText(this, "bluetoothManager.getAdapter()==null", Toast.LENGTH_SHORT).show();
            tools.toastNow(mContext, "bluetoothManager.getAdapter()==null", Color.WHITE);
            finish();
            return;
        }

//        scanButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                scanLeDevice(true);
//            }
//        });

        if(writeLogFile) {
            File logFile = new File("sdcard/Log/log.file");

            if (logFile.exists()) {
                logFile.delete();
            }
        }

        listViewLE = (ListView)findViewById(R.id.lelist);

//        listBluetoothDevice = new ArrayList<>();
//        adapterLeScanResult = new ArrayAdapter<BluetoothDevice>(
//                this, android.R.layout.simple_list_item_1, listBluetoothDevice);
//        adapterLeScanResult = new SimpleAdapter(
//                this, deviceInfo, android.R.layout.simple_list_item_2, new String[] { DeviceID, RSSI }, new int[] { android.R.id.text1, android.R.id.text2 });
        adapterLeScanResult = new SimpleAdapter(
                this, deviceInfo, R.layout.style_listview, new String[] { DeviceID, ProductName, UrlID, RSSI }, new int[]{R.id.deviceID, R.id.PdName, R.id.urlID, R.id.rssiData});
        listViewLE.setAdapter(adapterLeScanResult);
        listViewLE.setOnItemClickListener(scanResultOnItemClickListener);

        mHandler = new Handler();

    }

    AdapterView.OnItemClickListener scanResultOnItemClickListener =
            new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

//                    tools.appendLog("scanResultOnItemClickListener");
//                    final BluetoothDevice device =
//                            (BluetoothDevice) parent.getItemAtPosition(position);
                    final BluetoothDevice device =
                            (BluetoothDevice) ((HashMap) parent.getItemAtPosition(position)).get(Device);
//                    final BluetoothDevice device = (BluetoothDevice) deviceInfo.get(position).get(Device);
//                    final BluetoothDevice device = listBluetoothDevice.get(position);

                    String msg = device.getAddress() + "\n"
                            + device.getBluetoothClass().toString() + "\n"
                            + getBTDevieType(device);

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(device.getName())
                            .setMessage(msg)
                            .setPositiveButton("了解, 先不要連接", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(writeLogFile) {
                                        tools.appendLog("先不要連接");
                                    }
                                }
                            })
                            .setNeutralButton("連接", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(writeLogFile) {
                                        tools.appendLog("連接");
                                    }
                                    final Intent intent = new Intent(MainActivity.this,
                                            WriteDataActivity.class);
                                    intent.putExtra(ControlActivity.EXTRAS_DEVICE_NAME,
                                            device.getName());
                                    intent.putExtra(ControlActivity.EXTRAS_DEVICE_ADDRESS,
                                            device.getAddress());

                                    if (mScanning) {
                                        mBluetoothLeScanner.stopScan(scanCallback);
                                        mScanning = false;
//                                        scanButton.setEnabled(true);
                                    }

                                    JSONArray jsonArray = tools.HashMapArray2JSONArray(requestParameter);

                                    String jsonString = "";

                                    Log.println(Log.DEBUG, targetID, "deviceInfo0 test--> "+requestParameter);
                                    Log.println(Log.DEBUG, targetID, "deviceInfo3 test--> "+jsonArray.toString());
                                    try {
                                        jsonString = jsonArray.get(position).toString();
                                        Log.println(Log.DEBUG, targetID, "deviceInfo1 test--> " + jsonString);
                                    }catch (JSONException e) {
                                        Log.println(Log.INFO, targetID, "JSONException Error");
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                    intent.putExtra("POST_DATA", jsonString);

                                    startActivity(intent);

//                                    finish();  //後來自己加上的
                                }
                            })
                            .show();

                }
            };

//    AdapterView.OnItemClickListener scanResultOnItemClickListener =
//            new AdapterView.OnItemClickListener(){
//
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    final BluetoothDevice device = (BluetoothDevice) parent.getItemAtPosition(position);
//
//                    String msg = device.getAddress() + "\n"
//                            + device.getBluetoothClass().toString() + "\n"
//                            + getBTDevieType(device);
//
//                    new AlertDialog.Builder(MainActivity.this)
//                            .setTitle(device.getName())
//                            .setMessage(msg)
//                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//
//                                }
//                            })
//                            .show();
//
//                }
//            };

    private String getBTDevieType(BluetoothDevice d){
        String type = "";

        switch (d.getType()){
            case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                type = "DEVICE_TYPE_CLASSIC";
                break;
            case BluetoothDevice.DEVICE_TYPE_DUAL:
                type = "DEVICE_TYPE_DUAL";
                break;
            case BluetoothDevice.DEVICE_TYPE_LE:
                type = "DEVICE_TYPE_LE";
                break;
            case BluetoothDevice.DEVICE_TYPE_UNKNOWN:
                type = "DEVICE_TYPE_UNKNOWN";
                break;
            default:
                type = "unknown...";
        }

        return type;
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.println(Log.INFO, targetID, "System onResume");
        if(writeLogFile) {
            tools.appendLog("System onResume");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, RQS_ENABLE_BLUETOOTH);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.println(Log.INFO, targetID, "System onPause");
        if(writeLogFile) {
            tools.appendLog("System onPause");
        }
//        if(mScannerView != null) {
////            mScannerView.removeAllViews(); //這是必須要作的, 否則會造成system reset
////            mScannerView.stopCamera();  // then stop the camera
//            mScannerView = null;
//            setContentView(R.layout.activity_main);  // and set the View again.
//        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        Log.println(Log.INFO, targetID, "System onSaveInstanceState");
        if(writeLogFile) {
            tools.appendLog("System onSaveInstanceState");
        }

    }

    @Override
    protected void onRestoreInstanceState (Bundle savedInstanceState) {
        Log.println(Log.INFO, targetID, "System onRestoreInstanceState");
        if(writeLogFile) {
            tools.appendLog("System onRestoreInstanceState");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RQS_ENABLE_BLUETOOTH && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }

        getBluetoothAdapterAndLeScanner();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
//            Toast.makeText(this, "bluetoothManager.getAdapter()==null", Toast.LENGTH_SHORT).show();
            tools.toastNow(mContext, "bluetoothManager.getAdapter()==null", Color.WHITE);
            finish();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.println(Log.INFO, targetID, "System onRequestPermissionsResult");
        if(writeLogFile) {
            tools.appendLog("System onRequestPermissionsResult");
        }
        switch (requestCode) {
            case RQS_ENABLE_ACCESS_FINE_LOCATION: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.println(Log.INFO, targetID, "PERMISSION_GRANTED");
                    if(writeLogFile) {
                        tools.appendLog("PERMISSION_GRANTED");
                    }

                    if(!btn4Scan) {
                        scanButton.setText("掃描藍芽");
//                        scanButton.setOnClickListener(scanDevice);
                        btn4Scan = true;
                    }

//                    scanButton.setEnabled(true);
//                    turnOnPermission.setVisibility(View.INVISIBLE);
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.println(Log.INFO, targetID, "PERMISSION_CANCEL");
                    if(writeLogFile) {
                        tools.appendLog("PERMISSION_CANCEL");
                    }
//                    Toast.makeText(MainActivity.this, "沒有啟用相機的授權, 無法使用QR-Code的Scan功能", Toast.LENGTH_SHORT).show();
                    tools.toastNow(mContext, "沒有啟用位置資訊訊息的授權, 無法使用的Scan功能", Color.WHITE);

                    if(btn4Scan) {
                        scanButton.setText("請求啟用藍芽位置訊息授權");
//                        scanButton.setOnClickListener(trunOnPermission);
                        btn4Scan = false;
                    }
//                    scanButton.setEnabled(false);
//                    turnOnPermission.setVisibility(View.VISIBLE);
                }
                break;
//                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.println(Log.INFO, targetID, "System onMonitor Rotate");
        if(writeLogFile) {
            tools.appendLog("System onMonitor Rotate");
        }
    }

    @Override
    public void onStop(){
        Log.println(Log.INFO, targetID, "System onStop");
        super.onStop();

    }

    @Override
    public void onDestroy(){
        Log.println(Log.INFO, targetID, "System onDestory");
        super.onDestroy();

    }

    private void getBluetoothAdapterAndLeScanner(){
        // Get BluetoothAdapter and BluetoothLeScanner.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        mScanning = false;
    }

    /*
    to call startScan (ScanCallback callback),
    Requires BLUETOOTH_ADMIN permission.
    Must hold ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission to get results.
     */
    private void scanLeDevice(final boolean enable) {
        if (enable  && !mScanning) {
            title.setVisibility(View.GONE);
            if(!deviceInfo.isEmpty()) {
                Log.println(Log.INFO, targetID, "deviceInfo.isNotEmpty");
                if(writeLogFile) {
                    tools.appendLog("deviceInfo.isNotEmpty");
                }
                deviceInfo.clear();
                listViewLE.invalidateViews();
            }
//            listBluetoothDevice.clear();
//            listViewLE.invalidateViews();

            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                if(mScanning) {
                    mBluetoothLeScanner.stopScan(scanCallback);
                    listViewLE.invalidateViews();

                    tools.toastNow(mContext, "掃描藍芽裝置結束", Color.WHITE);

//                    Toast.makeText(MainActivity.this,
//                            "Scan timeout",
//                            Toast.LENGTH_LONG).show();

                    mScanning = false;
                }

                }
            }, SCAN_PERIOD);

//            //scan specified devices only with ScanFilter
//            ScanFilter scanFilter =
//                    new ScanFilter.Builder()
//                            .setServiceUuid(BluetoothLeService.ParcelUuid_GENUINO101_ledService)
//                            .build();
//            List<ScanFilter> scanFilters = new ArrayList<ScanFilter>();
//            scanFilters.add(scanFilter);
//
//            ScanSettings scanSettings =
//                    new ScanSettings.Builder().build();
//
//            mBluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback);

            mBluetoothLeScanner.startScan(scanCallback);
            mScanning = true;
//            scanButton.setEnabled(false);
        } else {
            mBluetoothLeScanner.stopScan(scanCallback);
            mScanning = false;
//            scanButton.setEnabled(true);
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            String uuid = "468e";
            String resultString = result.toString();
            System.out.println("result => " + resultString);
            if(writeLogFile) {
                tools.appendLog("result => " + resultString);
            }
            int rssiData = result.getRssi();
            System.out.println("rssiData => " + rssiData);
            if(writeLogFile) {
                tools.appendLog("rssiData => " + rssiData);
            }
            SparseArray<byte[]> manufacturerSpecificData = result.getScanRecord().getManufacturerSpecificData();
            int key = manufacturerSpecificData.keyAt(0);
            System.out.println("key : " + key);
            if(writeLogFile) {
                tools.appendLog("key : " + key);
            }

            if(!(manufacturerSpecificData == null || key == 0)) {
//                int key = manufacturerSpecificData.keyAt(0);
//                System.out.println("key : " + key);
//                if(writeLogFile) {
//                    tools.appendLog("key : " + key);
//                }


                byte[] broadcastData = manufacturerSpecificData.get(key);

//
//            System.out.println("broadcastData[0] : " + broadcastData[0]);
//            System.out.println("broadcastData[1] : " + broadcastData[1]);
//
//            String xx0 = Integer.toHexString(broadcastData[0]);
//            String xx1 = broadcastData[1] < 0
//                    ? Integer.toHexString(broadcastData[1]+65536).substring(2) :    Integer.toHexString(broadcastData[1]);
//
//            System.out.println("broadcastData[0] : " + xx0);
//            System.out.println("broadcastData[1] : " + xx1);


                //以下的兩個值就是uuid 468e
//            if(broadcastData[0] == 70 && broadcastData[1] == -114) {
//
//            //Copy byte[0] & byte[1]  to  uuidByte Array
//            byte[] uuidByte = Arrays.copyOfRange(broadcastData,0,2);
//            String uuidHexString = String.valueOf(Hex.encodeHex(uuidByte));
                //Copy byte[0] & byte[1]  然後轉換成HexString
                String uuidHexString = String.valueOf(Hex.encodeHex(Arrays.copyOfRange(broadcastData, 0, 2)));

//            uuidHexString = "02766f7264657203676738394d7a";
                System.out.println("uuidHexString : " + uuidHexString);
                if (writeLogFile) {
                    tools.appendLog("uuidHexString : " + uuidHexString);
                }

                if (uuidHexString.equals(uuid)) {

                    System.out.println("--------------->95 : Got It");
                    if (writeLogFile) {
                        tools.appendLog("--------------->95 : Got It");
                    }

                    String eddyStone = String.valueOf(Hex.encodeHex(Arrays.copyOfRange(broadcastData, 2, broadcastData.length)));
                    System.out.println("eddyStone : " + eddyStone);
                    if (writeLogFile) {
                        tools.appendLog("eddyStone : " + eddyStone);
                    }

//                    HashMap<String, String> requestItem = new HashMap<String, String>();
//                    requestItem.put(Eddystone, eddyStone);
//                    requestItem.put(RSSI1, String.valueOf(result.getRssi()));
//                    requestParameter.add(requestItem);
//
//
//                    JSONArray test = tools.HashMapArray2JSONArray(requestParameter);
//                    Log.println(Log.DEBUG, targetID, "deviceInfo1 test--> "+test.toString());


//                    OkHttpPostHandler locationTask = new OkHttpPostHandler();
//                    String url = "http://vorder.net/demo/url_test.php";
////        locationTask.execute(url, textString);
//
//                    //以下的方式可以拿到async task執行後回傳的result
//                    String asyncResult = "";
//                    try {
////                        asyncResult = locationTask.execute(url, eddyStone).get();
//                        asyncResult = locationTask.execute(url, test.toString()).get();
//                        Log.println(Log.DEBUG, targetID, "asyncResult = " + asyncResult);
//
//                    } catch (InterruptedException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                        Log.println(Log.ERROR, targetID, "InterruptedException Error" + e);
//                    } catch (ExecutionException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                        Log.println(Log.ERROR, targetID, "ExecutionException Error" + e);
//                    }
//
//                    Log.println(Log.INFO, targetID, "asyncResult Data : "+asyncResult);
//                    HashMap<String, String> asyncResult2JSON = tools.jsonObjectConvertToHashMap(asyncResult);
//
//                    requestParameter.clear();

//                    Log.println(Log.DEBUG, targetID, "asyncResult2JSON = " + asyncResult2JSON.toString());
//                    Log.println(Log.DEBUG, targetID, "asyncResult name = " + asyncResult2JSON.get("name"));
//                    Log.println(Log.DEBUG, targetID, "asyncResult url = " + asyncResult2JSON.get("url"));


                    String aaa = "02766f7264657203676738394d7a";
                    aaa = "02766f7264657203504b3576387a";

                    //轉換方法三:
                    String asciiString = convertHexByteArray2UrlString(broadcastData);
                    String asciiString1 = convertHexToString(aaa);
                    System.out.println("asciiString : " + asciiString);
                    System.out.println("asciiString1: " + asciiString1);
                    if (writeLogFile) {
                        tools.appendLog("asciiString : " + asciiString);
                        tools.appendLog("asciiString1: " + asciiString1);
                    }

                    HashMap<String, Object> item = new HashMap<String, Object>();
                    item.put(Device, result.getDevice());
                    item.put(DeviceID, result.getDevice().toString());
//                    item.put(ProductName, asyncResult2JSON.get("name"));
//                    item.put(UrlID, asyncResult2JSON.get("url"));
                    item.put(RSSI, String.valueOf(result.getRssi()) + " db");
                    item.put(Eddystone, eddyStone);

                    if (title.getVisibility() == View.GONE) {
                        title.setVisibility(View.VISIBLE);
                    }

//                    addBluetoothDevice(result.getDevice());
                    addBluetoothDevice(item);
                }
//                }

//            //以下是之前的作法
//            String hexByte = String.valueOf(Hex.encodeHex(broadcastData));
//            System.out.println("broadcastData : " + hexByte);
//
//            if(hexByte.contains(uuid)) {
//                System.out.println("--------------->95 : Got It");
//                String urlHexString = hexByte.replace(uuid, "");
//                System.out.println("urlString : " + urlHexString);
//
//                //轉換方法一:
//                String asciiString = convertHexToString(urlHexString);
//                System.out.println("asciiString : "+asciiString);
//
//                //轉換方法二:
//                String newAsciiString = convertHexString2UrlString(urlHexString);
//                System.out.println("newAsciiString : "+newAsciiString);
//
//                addBluetoothDevice(result.getDevice());
//
//            }
//
//
            }
        }

//        @Override
//        public void onBatchScanResults(List<ScanResult> results) {
//            super.onBatchScanResults(results);
//            for(ScanResult result : results){
////                String aaa = result.toString();
////                System.out.println("result ======XXXX> " + aaa);
//                addBluetoothDevice(result.getDevice());
//            }
//        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
//            Toast.makeText(MainActivity.this, "onScanFailed: " + String.valueOf(errorCode), Toast.LENGTH_LONG).show();
            tools.toastNow(mContext, "掃描藍芽裝置失敗", Color.WHITE);
        }

//        private void addBluetoothDevice(BluetoothDevice device){
//            if(!listBluetoothDevice.contains(device)){
//                listBluetoothDevice.add(device);
//                listViewLE.invalidateViews();
//            }
//        }
        private void addBluetoothDevice(HashMap item) {
            Log.println(Log.DEBUG, targetID, "deviceInfo --> "+deviceInfo.toString());

//            JSONArray test = tools.HashMapArray2JSONArray(requestParameter);
//            Log.println(Log.DEBUG, targetID, "deviceInfo1 test--> "+test.toString());

            if (!deviceInfo.toString().contains(item.get(DeviceID).toString())) {
                deviceInfo.add(item);

                HashMap<String, String> requestItem = new HashMap<String, String>();
                requestItem.put(RSSI, ((String) item.get(RSSI)).replace(" db",""));
                requestItem.put(Eddystone, (String) item.get(Eddystone));
                requestParameter.add(requestItem);

//                listBluetoothDevice.add(device);
                listViewLE.invalidateViews();
            }
        }
    };

    //方法一:
    //%%%%%%%%%%%%%%%%%%%%%% HEX to ASCII %%%%%%%%%%%%%%%%%%%%%%
    public String convertHexToString(String hex){

        String ascii="";
        String str = "";

        // Convert hex string to "even" length
        int rmd,length;
        length=hex.length();
        rmd =length % 2;
        if(rmd==1)
            hex = "0"+hex;

        // split into two characters
        for( int i=0; i<hex.length()-1; i+=2 ){

            //split the hex into pairs
            String pair = hex.substring(i, (i + 2));
            if(i==0) {
                switch(pair) {
                    case "00":
                        str = "http://www.";
                        break;
                    case "01":
                        str = "https://www.";
                        break;
                    case "02":
                        str = "http://";
                        break;
                    case "03":
                        str = "https://";
                        break;
                }

            }
            else {
                //convert hex to decimal
                int dec = Integer.parseInt(pair, 16);
                str=CheckCode(dec);
                if(str.equals("n/a")) {
                    switch (pair) {
                        case "00":
                            str = ".com/";
                            break;
                        case "01":
                            str = ".org/";
                            break;
                        case "02":
                            str = ".edu/";
                            break;
                        case "03":
                            str = ".net/";
                            break;
                        case "04":
                            str = ".info/";
                            break;
                        case "05":
                            str = ".biz/";
                            break;
                        case "06":
                            str = ".gov/";
                            break;
                        case "07":
                            str = ".com";
                            break;
                        case "08":
                            str = ".org";
                            break;
                        case "09":
                            str = ".edu";
                            break;
                        case "0a":
                            str = ".net";
                            break;
                        case "0b":
                            str = ".info";
                            break;
                        case "0c":
                            str = ".biz";
                            break;
                        case "0d":
                            str = ".gov";
                            break;
                    }
                }
            }
//            ascii=ascii+" "+str;
            ascii=ascii+str;
        }
        return ascii;
    }

    public String CheckCode(int dec){
        String str;

        //convert the decimal to character
        str = Character.toString((char) dec);

        if(dec<32 || dec>126 && dec<161)
            str="n/a";
        return str;
    }

    //方法二:
    public String convertHexString2UrlString(String urlHexString) {

        byte[] urlByte = new byte[0];

        try {
            urlByte = Hex.decodeHex(urlHexString.toCharArray());
        } catch (DecoderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//        String urlByteString = new String(urlByte);
//        Log.println(Log.DEBUG, "XXXXX", "test length = "+urlByte.length);
//        Log.println(Log.DEBUG, "XXXXX", "testString = "+urlByteString);

        for (byte theByte : urlByte)
        {
            System.out.println("Single Byte : "+Integer.toHexString(theByte));
            if(writeLogFile) {
                tools.appendLog("Single Byte : "+Integer.toHexString(theByte));
            }
        }

        String urlString="";
        String str = "";

        for(int i=0; i<urlByte.length; i++) {
            if(i==0) {
                switch (urlByte[i]) {
                    case 0x00:
                        str = "http://www.";
                        break;
                    case 0x01:
                        str = "https://www.";
                        break;
                    case 0x02:
                        str = "http://";
                        break;
                    case 0x03:
                        str = "https://";
                        break;
                }
            }
            else {
                //Single byte to String
                str = new String(new byte[] { urlByte[i] });
                if(urlByte[i]<32 || urlByte[i]>126 && urlByte[i]<161) {
                    switch (urlByte[i]) {
                        case 0x00:
                            str = ".com/";
                            break;
                        case 0x01:
                            str = ".org/";
                            break;
                        case 0x02:
                            str = ".edu/";
                            break;
                        case 0x03:
                            str = ".net/";
                            break;
                        case 0x04:
                            str = ".info/";
                            break;
                        case 0x05:
                            str = ".biz/";
                            break;
                        case 0x06:
                            str = ".gov/";
                            break;
                        case 0x07:
                            str = ".com";
                            break;
                        case 0x08:
                            str = ".org";
                            break;
                        case 0x09:
                            str = ".edu";
                            break;
                        case 0x0a:
                            str = ".net";
                            break;
                        case 0x0b:
                            str = ".info";
                            break;
                        case 0x0c:
                            str = ".biz";
                            break;
                        case 0x0d:
                            str = ".gov";
                            break;
                    }
                }
            }
            urlString=urlString+str;
//            Log.println(Log.DEBUG, "XXXXX", "ascii = "+ascii);
        }
        return urlString;
    }

    public String convertHexByteArray2UrlString(byte[] broadcastData) {

        String urlString="";
        String str = "";

        for(int i=2; i<broadcastData.length; i++) {
            if(i==2) {
                System.out.println("Single Byte : "+Integer.toHexString(broadcastData[i]));
                if(writeLogFile) {
                    tools.appendLog("Single Byte : "+Integer.toHexString(broadcastData[i]));
                }
                switch (broadcastData[i]) {
                    case 0x00:
                        str = "http://www.";
                        break;
                    case 0x01:
                        str = "https://www.";
                        break;
                    case 0x02:
                        str = "http://";
                        break;
                    case 0x03:
                        str = "https://";
                        break;
                }
            }
            else {
                //Single byte to String
                str = new String(new byte[] { broadcastData[i] });
                if(broadcastData[i]<32 || broadcastData[i]>126 && broadcastData[i]<161) {
                    switch (broadcastData[i]) {
                        case 0x00:
                            str = ".com/";
                            break;
                        case 0x01:
                            str = ".org/";
                            break;
                        case 0x02:
                            str = ".edu/";
                            break;
                        case 0x03:
                            str = ".net/";
                            break;
                        case 0x04:
                            str = ".info/";
                            break;
                        case 0x05:
                            str = ".biz/";
                            break;
                        case 0x06:
                            str = ".gov/";
                            break;
                        case 0x07:
                            str = ".com";
                            break;
                        case 0x08:
                            str = ".org";
                            break;
                        case 0x09:
                            str = ".edu";
                            break;
                        case 0x0a:
                            str = ".net";
                            break;
                        case 0x0b:
                            str = ".info";
                            break;
                        case 0x0c:
                            str = ".biz";
                            break;
                        case 0x0d:
                            str = ".gov";
                            break;
                    }
                }
            }
            urlString=urlString+str;
//            Log.println(Log.DEBUG, "XXXXX", "ascii = "+ascii);
        }
        return urlString;
    }

//    View.OnClickListener scanDevice = new View.OnClickListener() {
//        @Override
//        public void onClick(final View v) {
//            scanLeDevice(true);
//        }
//    };
//
////    public void trunOnPermission(View v) {
//    View.OnClickListener trunOnPermission = new View.OnClickListener() {
//        @Override
//        public void onClick(final View v) {
//            Log.println(Log.INFO, targetID, "trunOnPermission Button Click");
//    //        setContentView(R.layout.activity_main);
//
//            //Check  ACCESS_FINE_LOCATION  Permission
//            int permissionCheck = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION);
//            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(currentActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
////                    Toast.makeText(mContext, "被要求授權BLE位置資料", Toast.LENGTH_SHORT).show();
//                    tools.toastNow(mContext, "被要求授權BLE位置資料", Color.WHITE);
//                } else {
//                    if (Build.VERSION.SDK_INT >= 23) {
//                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, RQS_ENABLE_ACCESS_FINE_LOCATION);
//                    }
//                }
//            } else {
////                Toast.makeText(mContext, "已經同意使用位置資訊訊息了", Toast.LENGTH_SHORT).show();
//                tools.toastNow(mContext, "已經同意使用位置資訊訊息了", Color.WHITE);
//            }
//        }
//    };

    public void scan_permission(View v) {
        if(btn4Scan) {
            Log.println(Log.INFO, targetID, "Scan BLE Device");
            if(writeLogFile) {
                tools.appendLog("Scan BLE Device");
            }
//            title.setVisibility(View.GONE);
            requestParameter.clear();
            scanLeDevice(true);
        }
        else {
            Log.println(Log.INFO, targetID, "trunOnPermission");
            if(writeLogFile) {
                tools.appendLog("trunOnPermission");
            }

            //Check  ACCESS_FINE_LOCATION  Permission
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)+ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            Log.println(Log.INFO, targetID, "permissionCheck -->"+permissionCheck);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED){
                Log.println(Log.INFO, targetID, "PERMISSION_GRANTED11");
                Log.println(Log.INFO, targetID, "howRequestPermissionRationale -->"+ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION));

                //ACCESS_FINE_LOCATION在turn on時, 其ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) = true
                //WRITE_EXTERNAL_STORAGE在turn on時, 其ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) = false
                if ((!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
                        || (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, RQS_ENABLE_ACCESS_FINE_LOCATION);
                        Log.println(Log.INFO, targetID, "PERMISSION_GRANTED13");
                    }
                }
            }else{
//            Toast.makeText(this,  "已經同意使用位置資訊訊息了", Toast.LENGTH_SHORT).show();
                tools.toastNow(mContext, "已經同意使用位置資訊訊息了", Color.WHITE);
            }





//            //Check  ACCESS_FINE_LOCATION  Permission
//            int permissionCheck = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION);
//            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(currentActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
////                    Toast.makeText(mContext, "被要求授權BLE位置資料", Toast.LENGTH_SHORT).show();
//                    tools.toastNow(mContext, "被要求授權BLE位置資料", Color.WHITE);
//                } else {
//                    if (Build.VERSION.SDK_INT >= 23) {
//                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, RQS_ENABLE_ACCESS_FINE_LOCATION);
//                    }
//                }
//            } else {
////                Toast.makeText(mContext, "已經同意使用位置資訊訊息了", Toast.LENGTH_SHORT).show();
//                tools.toastNow(mContext, "已經同意使用位置資訊訊息了", Color.WHITE);
//            }
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






}


