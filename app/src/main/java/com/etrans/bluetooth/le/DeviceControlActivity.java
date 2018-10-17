/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.etrans.bluetooth.le;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.etrans.bluetooth.le.bean.ResultQuerybean;
import com.etrans.bluetooth.le.bean.ResultSetbean;
import com.etrans.bluetooth.le.fragment.FragmentOne;
import com.etrans.bluetooth.le.fragment.FragmentThree;
import com.etrans.bluetooth.le.fragment.FragmentTwo;
import com.etrans.bluetooth.le.utils.BaseBiz;
import com.etrans.bluetooth.le.utils.ByteUtils;
import com.etrans.bluetooth.le.utils.CheckUtils;
import com.etrans.bluetooth.le.utils.HexUtil;
import com.etrans.bluetooth.le.utils.ToastFactory;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("NewApi")
//对于给定的ble设备，这个activity提供接口去连接，展示数据，service和characteris。---设备详情
//The Activity communicates with {@code BluetoothLeService}, which in turn interacts with the Bluetooth LE API.
public class DeviceControlActivity extends Activity implements View.OnClickListener {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public String extras_device_name = "DEVICE_NAME";
    public String extras_device_address = "DEVICE_ADDRESS";
    public static final int MSG_SENDALLORDER = 201;
    private static final int REQUEST_CODE_LOCATION_SETTINGS = 2;
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private static final int REQUEST_ENABLE_BT = 1;
    private TextView device_state, device_nameid;
    private ImageView img_device_state;
    private Button btn_showconnection;    //接受定时开关指令

    private String mDeviceName;
    private String mDeviceAddress;
    /*private ExpandableListView mGattServicesList;*/
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    //    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic, mBluetoothGattCharacteristicNotify, mBluetoothGattCharacteristicName;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUIDUtils";

    private Fragment fraOne, fraTwo, fraThree;
    private Button btn_query, btn_set, btn_about;
    private FragmentTransaction mFragmentTransaction;
    private LinearLayout ll_deviceinfo, ll_deviceconnection;

    private Button btn_scan;
    private ListView listview;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private boolean showdeviceinfo = false;
    private boolean showScan = false;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private Myapplication myapp;
    private Handler mHandler;
    //    private static final long SCAN_PERIOD = 5000;
    private KProgressHUD dialog;
    private boolean connectstate = false;
    private Handler connectHandler = new Handler();
    ;
    private static final int TIME_DELAY = 10000;//10秒超时处理
    private Runnable runnableconnect = new Runnable() {
        public void run() {
            if (connectstate) { //30秒后如果还是正在关闭状态则恢复原来状态
                connectstate = false;
                ToastFactory.showToast(DeviceControlActivity.this, "连接失败！");
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        }
    };


    public static Handler hand = null;

    public static Handler getHandler() {
        return hand;
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case DeviceControlActivity.MSG_SENDALLORDER://
                    String Senddata = (String) msg.obj;
                    if (myapp.ismConnected()) {
                        sendMsg2(Senddata);
                    } else {
                        ToastFactory.showToast(DeviceControlActivity.this, "蓝牙断开发送失败");
                    }
                    break;
            }
        }
    };
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //BluetoothLeScanner mBluetoothLeScanner = new BluetoothLeScanner();
//                    mBluetoothAdapter.stopLeScan(DeviceScanActivity.this.mLeScanCallback);
            scanLeDevice(false);
        }
    };
    // Code to manage Service lifecycle.
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // 在成功启动初始化时自动连接到设备。
//            try {
//                mBluetoothLeService.connect(mDeviceAddress); //到服务里面的方法去连接
//                updateConnectionState(R.string.Connection);
//            } catch (Exception e) {
//                System.out.println(e.getMessage());
//            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    //BroadcastReceiver：从service中传回来的参数，从BluetoothLeService中Broadcast中传回来的参数
    //若广播是service状态的改变（连接，未连接，发现，获得data）
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            //若广播是service状态的改变（连接，未连接，发现，获得data）
           /* if ((mNotifyCharacteristic == null) || ((0x10 | mNotifyCharacteristic.getProperties()) <= 0)){
                return;
            }*/
            /* DeviceControlActivity.this.mBluetoothLeService.setCharacteristic(mNotifyCharacteristic, true);*/

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
//                mConnected = true;
                ShowConnectionbtn(false); //下面的断开或连接状态
                updateConnectionState(R.string.connected);//已连接状态
                connectHandler.removeCallbacks(runnableconnect);//释放超时处理
                if (dialog != null) {
                    dialog.dismiss();
                }
//                invalidateOptionsMenu();
                //TODO 刚加上
                Log.i(TAG, "onReceive: 获取到数据1");
                TboxData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                myapp.setmConnected(false);
                ShowConnectionbtn(true);//显示断开还是连接按钮
                updateConnectionState(R.string.disconnected);//断开


//                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) { //GATT行动服务发现nk
                //到这里就可以去获取数据了，可以去显示获取数据的按钮
                ToastFactory.showToast(DeviceControlActivity.this, "连接成功可以主动获取数据了");
                myapp.setmConnected(true);
                /*displayGattServices(mBluetoothLeService.getSupportedGattServices());*/
                //TODO在此处修改了，使得发现服务后直接开启获得数据,连接成功了就根据UUID获取数据
                mNotifyCharacteristic = mBluetoothLeService.getBluetoothGattCharacteristic();//根据写UUID找到写特征,暂时先注释，有UUID的时候打开并获取
                mBluetoothGattCharacteristicNotify = mBluetoothLeService.getBluetoothGattCharacteristicNotify();//根据写UUID找到写特征,暂时先注释，有UUID的时候打开并获取
//                mNotifyCharacteristic = mBluetoothLeService.getBluetoothGattCharacteristic();//根据写UUID找到写特征,暂时先注释，有UUID的时候打开并获取
                if ((mNotifyCharacteristic == null) || ((0x10 | mNotifyCharacteristic.getProperties()) <= 0)) {
                    return;
                }
                //得到这两个Service和characteristic就可以向蓝牙发送数据了。nk
                mBluetoothLeService.setCharacteristic(mNotifyCharacteristic, true);//设置开启之后，才能在 onCharacteristicRead() 这个方法中收到数据。//发送数据
                mBluetoothLeService.setCharacteristic(mBluetoothGattCharacteristicNotify, true);//设置开启之后，才能在 onCharacteristicRead() 这个方法中收到数据。//接收数据
                //TODO 刚加上
                Log.i(TAG, "onReceive: 获取到数据2");
                TboxData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //TODO在此处修改了，使得发现服务后直接开启获得数据
                mBluetoothLeService.setCharacteristic(mNotifyCharacteristic, true);
                Log.i(TAG, "onReceive: 获取到数据3");
                TboxData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                //使得定时的textview里面不显示数据
                /*String str2 = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                setTimeText.setText(str2);*/
            }
        }
    };

    //设备未连接时清除界面内容
    private void clearUI() {
        Handler handler = Myapplication.getHandler();
        if (handler != null) {
            Message msg = Message.obtain();
            msg.what = Myapplication.MSG_APP_DATA;
            msg.obj = null;
            handler.sendMessage(msg);
        }
        mFragmentTransaction = getFragmentManager().beginTransaction();
        if (fraOne == null) {
            fraOne = new FragmentOne();
        }
        mFragmentTransaction.replace(R.id.fl_main, fraOne).commit();
        showquery();


        /* mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);*/
//        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics3);
        hand = handler;
        myapp = (Myapplication) getApplication();
//        dialog = CheckUtils.showDialog(this);
        setView();//初始化监听nk
//        final Intent intent = getIntent();
//        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
//        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        dialog = CheckUtils.showDialog(this);
        mHandler = new Handler();

        // 检查当前手机是否支持ble 蓝牙,如果不支持退出程序
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        Intent gattIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattIntent, mServiceConnection, BIND_AUTO_CREATE);//绑定服务

        // 检查设备上是否支持蓝牙
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //TODO  //andoird 6.0需要开启定位请求
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//如果 API level 是大于等于 23(Android 6.0) 时
            //判断是否具有权限
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //判断是否需要向用户解释为什么需要申请该权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Toast.makeText(this, "自Android 6.0开始需要打开位置权限才可以搜索到Ble设备", Toast.LENGTH_SHORT).show();
                }
                //请求权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_ACCESS_COARSE_LOCATION);
            }
        }

        Boolean haha = isLocationEnable(this);
        if (!haha) {
            setLocationService();
        }
    }

    private void setView() {
        device_state = (TextView) findViewById(R.id.device_state);
        img_device_state = findViewById(R.id.img_device_state);
        device_nameid = (TextView) findViewById(R.id.device_nameid);
        btn_showconnection = (Button) findViewById(R.id.btn_showconnection);
        btn_showconnection.setOnClickListener(this);
        listview = (ListView) findViewById(R.id.listview);
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        listview.setAdapter(mLeDeviceListAdapter);

        //fragment
        //主要逻辑在MainActivity的onClick中
        btn_query = (Button) findViewById(R.id.btn_query);
        btn_query.setOnClickListener(this);
        btn_set = (Button) findViewById(R.id.btn_set);
        btn_set.setOnClickListener(this);
        btn_about = (Button) findViewById(R.id.btn_about);
        btn_about.setOnClickListener(this);
        ll_deviceinfo = (LinearLayout) findViewById(R.id.ll_deviceinfo);
        ll_deviceconnection = (LinearLayout) findViewById(R.id.ll_deviceconnection);
        btn_scan = (Button) findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(this);
        ll_deviceconnection.setOnClickListener(this);


        mFragmentTransaction = getFragmentManager().beginTransaction();
        if (fraOne == null) {
            fraOne = new FragmentOne();
        }
        mFragmentTransaction.replace(R.id.fl_main, fraOne).commit();
        showquery();
    }

    /**
     * 按键显示
     *
     * @param connection 断开还是连接
     */
    private void ShowConnectionbtn(boolean connection) {
        if (connection) {
            btn_showconnection.setText("连接");
        } else {
            btn_showconnection.setText("断开");
        }
    }

    private void ShowScanbtn(boolean scan) {
        if (scan) {
            btn_scan.setText("取消搜索");
        } else {
            btn_scan.setText("搜索");
        }
    }

    //重启时、开始时注册广播
    @Override
    protected void onResume() {
        super.onResume();
        //注册广播
        IntentFilter statusFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);//蓝牙模块开关模式变化广播
        registerReceiver(mStatusReceive, statusFilter);
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());


        //如果当前手机蓝牙未开启，弹出dialog提示用户开启nk
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        //扫描周边BLE设备，获取扫描到的设备。nk
        if (!myapp.ismConnected()) {
            scanLeDevice(true); // 为了确保设备上蓝牙能使用, 如果当前蓝牙设备没启用,弹出对话框向用户要求授予权限来启用
        }

        //连接中
//        if (mBluetoothLeService != null) { //先注释
//            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
//            updateConnectionState(R.string.Connection);
//            Log.d(TAG, "Connect request result=" + result);
//        }
    }

    //停止时，注销广播
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mStatusReceive);
        unregisterReceiver(mGattUpdateReceiver);
    }

    //关闭activity
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    /**
     * 显示当前连接状态
     *
     * @param resourceId 图标
     */
    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (resourceId) {
                    case R.string.connected: //连接
                        img_device_state.setImageResource(R.drawable.img_bluetooth_connect);
                        device_state.setVisibility(View.GONE);
                        break;
                    case R.string.disconnected: //断开
                        img_device_state.setImageResource(R.drawable.img_bluetooth_disconnect);
                        device_state.setVisibility(View.VISIBLE);
                        break;
                }
//                device_state.setText(resourceId);//蓝牙名称右边
            }
        });
    }

    //显示传来的数据
    private void TboxData(String data) {
        if (data != null) {
            /* mDataField.append(data);*/

            Log.i(TAG, "TboxData: 原始数据 data = " + data);
            String str = HexUtil.ShowData(data);//返回2a2a数据
            String type = str.substring(4, 6);
            if (type.equals("02")) { //设置 2A2A0201010009 0401000201030104000B
                ResultSetbean resultSETbean = HexUtil.HexsetData(str);
                Handler handler = FragmentTwo.getHandler();
                if (handler != null) {
                    Message msg = Message.obtain();
                    msg.what = FragmentTwo.MSG_SET_DATA;
                    msg.obj = resultSETbean;
                    handler.sendMessage(msg);
                }

            } else if (type.equals("03")) { //查询 2a2a02fe0114 0103343536020331333503033132330403373839eb
                ResultQuerybean showdata = HexUtil.HexqueryData(str);//解析2a2a查询数据
                Log.i(TAG, "TboxData: 获取到主动查询返回数据！");
                Handler handler = Myapplication.getHandler();
                if (handler != null) {
                    Message msg = Message.obtain();
                    msg.what = Myapplication.MSG_APP_DATA;
                    msg.obj = showdata;
                    handler.sendMessage(msg);
                }
            }

//            ResultQuerybean showdata = HexUtil.HexqueryData(str);//解析2a2a数据
//            ResultQuerybean showdata = HexUtil.HexqueryData("2a2a03FE010801020304050a0f10E0");//解析不正确，暂时先注释
//            String str = "";

//            Handler handler = FragmentOne.getHandler();
//            if (handler != null) {
//                Message msg = Message.obtain();
//                msg.what = FragmentOne.MSG_DATA;
//                msg.obj = data;
//                handler.sendMessage(msg);
//            }


        }
    }


    //发送消息
    //发送数据时，如果一包数据超过20字节，需要分包发送，一次最多发送二十字节。nk
    public void sendMsg(String paramString) {
       /* if ((mNotifyCharacteristic == null) || (paramString == null)){
            return;
        }
        if ((0x8 | mNotifyCharacteristic.getProperties()) <= 0){
            return;
        }*/
        byte[] arrayOfByte1 = new byte[20];
        byte[] arrayOfByte2 = new byte[20];
        arrayOfByte2[0] = 0;
        if (paramString.length() > 0) {
            arrayOfByte1 = paramString.getBytes();
        }
        //向蓝牙设备发送数据  nk
        mNotifyCharacteristic.setValue(arrayOfByte2[0], 17, 0);
        mNotifyCharacteristic.setValue(arrayOfByte1);
        this.mBluetoothLeService.writeCharacteristic(mNotifyCharacteristic);



      /*  byte[] arrayOfByte1 = new byte[20];
        byte[] arrayOfByte2 = new byte[20];
        arrayOfByte2[0] = 0;
        if (paramString.length() > 0)
            arrayOfByte1 = paramString.getBytes();
        //TODO我不懂这是啥意思
        mNotifyCharacteristic.setValue(arrayOfByte2[0], 0x11, 0);
        mNotifyCharacteristic.setValue(arrayOfByte1);
        this.mBluetoothLeService.writeCharacteristic(mNotifyCharacteristic);*/
    }

    /**
     * android ble 发送
     * 每条数据长度应保证在20个字节以内
     * 2条数据至少要空15ms
     *
     * @param Hexdata
     */
    byte[] sData = null;

    public void sendMsg2(String Hexdata) { //新发送代码
//        Hexdata = ByteUtils.toHexString(Hexdata.getBytes());//转换成16进制
        if (Hexdata.length() > 0) {
            final boolean[] isSuccess = new boolean[1];
            if (Hexdata.length() <= 40) {
                sData = HexUtil.hex2byte(Hexdata);
                mNotifyCharacteristic.setValue(sData);
                isSuccess[0] = this.mBluetoothLeService.writeCharacteristic(mNotifyCharacteristic);
            } else {
                final String finalHexdata = Hexdata;
                BaseBiz.dataEs.execute(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < finalHexdata.length(); i = i + 40) {
                            final String[] shortOrder = {""};
                            final int finalI = i; //获取到当前长度位置

                            if (finalHexdata.length() - i >= 40) {
                                shortOrder[0] = finalHexdata.substring(finalI, finalI + 40); //长度位置到指定截取长度
                            } else {
                                shortOrder[0] = finalHexdata.substring(finalI, finalHexdata.length()); //长度位置到末尾
                            }

                            Log.e("--->", "shortOrder[0]2：" + shortOrder[0]);
                            sData = HexUtil.hex2byte(shortOrder[0]);//如果是16进制的字符串就要转成byte数组
                            mNotifyCharacteristic.setValue(sData);
                            try {
                                isSuccess[0] = mBluetoothLeService.writeCharacteristic(mNotifyCharacteristic);
                                Thread.sleep(150);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }
    }

    //注册广播时定义intent的各种属性
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    public void onClick(View v) {

        //每次点击事件都会初始化FragmentTransaction
        mFragmentTransaction = getFragmentManager().beginTransaction();

        switch (v.getId()) {
            case R.id.ll_deviceconnection:
                if (myapp.ismConnected()) {
//                    ll_deviceinfo.setVisibility(View.VISIBLE);
//                    listview.setVisibility(View.GONE);
                }
                break;
            case R.id.btn_scan:
                if (!showScan) {

                    mLeDeviceListAdapter.clear();
                    mLeDeviceListAdapter.notifyDataSetChanged();
                    //如果当前手机蓝牙未开启，弹出dialog提示用户开启nk
                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    } else {
                        scanLeDevice(true);
                    }
                } else {
                    scanLeDevice(false);
//                    mHandler.removeCallbacks(runnable);//屏蔽自动停止搜索
                }

                break;

            case R.id.btn_query:
                if (fraOne == null) {
                    fraOne = new FragmentOne();
                }
                mFragmentTransaction.replace(R.id.fl_main, fraOne).commit();
                showquery();
//                mFragmentTransaction.addToBackStack(null);//添加fragment到返回栈
//                mFragmentTransaction.commit();
                break;
            case R.id.btn_set:

                if (myapp.getShowdata() != null) {
                    if (fraTwo == null) {
                        fraTwo = new FragmentTwo();
                    }
                    mFragmentTransaction.replace(R.id.fl_main, fraTwo).commit();
                    showset();
                } else {
                    ToastFactory.showToast(this, "没有查询数据！");
                }


//                mFragmentTransaction.addToBackStack(null);//添加fragment到返回栈
//                mFragmentTransaction.commit();
                break;
            case R.id.btn_about:
                if (fraThree == null) {
                    fraThree = new FragmentThree();
                }
                mFragmentTransaction.replace(R.id.fl_main, fraThree).commit();
                showabout();
//                mFragmentTransaction.addToBackStack(null);//添加fragment到返回栈
//                mFragmentTransaction.commit();
                break;
            case R.id.btn_showconnection:

                if (myapp.ismConnected()) { //断开连接
                    mBluetoothLeService.disconnect();
                } else { //尝试连接
                    final boolean result = mBluetoothLeService.connect(extras_device_address);
                    dialog.show();
                    connectstate = true;
                    connectHandler.postDelayed(runnableconnect, TIME_DELAY);//30秒后如果还是正在关闭状态则恢复状态
                }

                break;
        }
    }


    private void showquery() {
        btn_query.setBackground(getDrawable(R.drawable.setbtn_pressed_bg));
        btn_set.setBackground(getDrawable(R.drawable.setbtn_selected_bg));
        btn_about.setBackground(getDrawable(R.drawable.setbtn_selected_bg));

        btn_query.setTextColor(getResources().getColor(R.color.black));
        btn_set.setTextColor(getResources().getColor(R.color.white));
        btn_about.setTextColor(getResources().getColor(R.color.white));
    }

    private void showset() {
        btn_query.setBackground(getDrawable(R.drawable.setbtn_selected_bg));
        btn_set.setBackground(getDrawable(R.drawable.setbtn_pressed_bg));
        btn_about.setBackground(getDrawable(R.drawable.setbtn_selected_bg));

        btn_query.setTextColor(getResources().getColor(R.color.white));
        btn_set.setTextColor(getResources().getColor(R.color.black));
        btn_about.setTextColor(getResources().getColor(R.color.white));
    }

    private void showabout() {
        btn_query.setBackground(getDrawable(R.drawable.setbtn_selected_bg));
        btn_set.setBackground(getDrawable(R.drawable.setbtn_selected_bg));
        btn_about.setBackground(getDrawable(R.drawable.setbtn_pressed_bg));

        btn_query.setTextColor(getResources().getColor(R.color.white));
        btn_set.setTextColor(getResources().getColor(R.color.white));
        btn_about.setTextColor(getResources().getColor(R.color.black));
    }


    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = DeviceControlActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int position, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.ll_info = (LinearLayout) view.findViewById(R.id.ll_info);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            final BluetoothDevice device = mLeDevices.get(position);
            final String deviceName = device.getName();
            String name = "";
            if (deviceName != null && deviceName.length() > 0) {
                name = deviceName;
            } else {
                name = getString(R.string.unknown_device);
            }
            viewHolder.deviceName.setText(name);
            viewHolder.deviceAddress.setText(device.getAddress());
            final String finalName = name;
            viewHolder.ll_info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("==position==" + position);
                    if (device == null) return;
                    extras_device_name = finalName;
                    extras_device_address = device.getAddress();
                    if (mBluetoothLeService != null) { //先注释
                        final boolean result = mBluetoothLeService.connect(extras_device_address);
                        updateConnectionState(R.string.Connection);//连接中
                        device_nameid.setText(extras_device_name);//设备名
                        scanLeDevice(false);
                        Log.d(TAG, "Connect request result=" + result);
                    }

//                    Intent intent = new Intent(DeviceControlActivity.this, DeviceControlActivity.class);
//                    intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, finalName);
//                    intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
//                    startActivity(intent);


                }
            });

            return view;
        }
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        LinearLayout ll_info;
    }

    //扫描蓝牙设备
    //搜索函数，反馈是mLeScanCallback
    private void scanLeDevice(final boolean enable) {
        showScan = enable;
        if (enable) {
            ShowScanbtn(true);
        } else {
            ShowScanbtn(false);
        }
        if (enable) {
            ll_deviceinfo.setVisibility(View.GONE);
            listview.setVisibility(View.VISIBLE);
            // Stops scanning after a pre-defined scan period.
//            mHandler.removeCallbacks(runnable);//屏蔽自动停止搜索
//            mHandler.postDelayed(runnable, SCAN_PERIOD);//屏蔽自动停止搜索

            //扫描很费电，要预设扫描周期，扫描一定周期就停止扫描
//            mBluetoothAdapter.startLeScan(DeviceScanActivity.this.mLeScanCallback);
            if (bluetoothLeScanner != null) {
                bluetoothLeScanner.startScan(scanCallback);//扫描全部
                Log.i(TAG, "scanLeDevice: 正在搜索...");


                //测试过滤nk------test1 过滤UUID
//            List<ScanFilter> filters = new ArrayList<>();
//                ScanFilter filter = new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString("00005500-d102-11e1-9b23-00025b00a5a6")).build();
//            filters.add(filter);
//            bluetoothLeScanner.startScan(filters,new ScanSettings.Builder().build(),scanCallback);
                //------------------------------
                //测试过滤nk------test2过滤设备地址 = EF:66:61:F0:5D:4B
//            List<ScanFilter> filters2 = new ArrayList<>();
//            ScanFilter filter2 = new ScanFilter.Builder().setDeviceAddress("EF:66:61:F0:5D:4B").build();
//            filters2.add(filter2);
//            bluetoothLeScanner.startScan(filters2,new ScanSettings.Builder().build(),scanCallback);
                //------------------------------
                //测试过滤nk------test3过滤设备名称，需要固定名称不能模糊搜索
//            List<ScanFilter> filters3 = new ArrayList<>();
//            ScanFilter filter3 = new ScanFilter.Builder().setDeviceName("e-trans#007").build();
//            filters3.add(filter3);
//            bluetoothLeScanner.startScan(filters3,new ScanSettings.Builder().build(),scanCallback);
                //------------------------------
                //测试过滤nk------test3过滤设备名称，需要固定名称不能模糊搜索
//            List<ScanFilter> filters4 = new ArrayList<>();
//            ScanFilter filter4 = new ScanFilter.Builder().set.build();
//            filters4.add(filter4);
//            bluetoothLeScanner.startScan(filters4,new ScanSettings.Builder().build(),scanCallback);
                //------------------------------
            }
        } else {
            ll_deviceinfo.setVisibility(View.VISIBLE);
            listview.setVisibility(View.GONE);
//            mBluetoothAdapter.stopLeScan(DeviceScanActivity.this.mLeScanCallback);
            if (bluetoothLeScanner != null) {
                bluetoothLeScanner.stopScan(scanCallback);
                Log.i(TAG, "scanLeDevice: 停止搜索...");

            }
        }
    }

    //作为BLE扫描结果的接口，实现如下。nk
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            byte[] scanData = result.getScanRecord().getBytes();
            //把byte数组转成16进制字符串，方便查看
            //获取到scanRecord后就能根据协议解析广播包了，根据需求提取对应字段。scanRecord的数据必须进行格式转换，否则列表显示会出现乱码，格式不匹配！nk
            Log.e("TAG", "onScanResult ByteUtils.getInstance().bytes2HexString:" + ByteUtils.getInstance().bytes2HexString(scanData));
            Log.e("TAG", "onScanResult result.toString():" + result.toString());
            //test

            //////
            if (result.getDevice().getName() != null) {
                mLeDeviceListAdapter.addDevice(result.getDevice());
                mLeDeviceListAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };


    //设置权限
    @Override
    //跳转到devicecontrol进行连接的返回值
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 用户没有开启蓝牙
        //有没有定位回传nk
        if (requestCode == REQUEST_CODE_LOCATION_SETTINGS) {
            if (isLocationEnable(this)) {
                //定位已打开的处理
                Toast.makeText(this, "定位已经打开", Toast.LENGTH_SHORT).show();

            } else {
                //定位依然没有打开的处理
                Toast.makeText(this, "定位没有打开", Toast.LENGTH_SHORT).show();
            }
        }
        //有没有打开蓝牙回传nk
        else if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }

        /*if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }*/

        super.onActivityResult(requestCode, resultCode, data);
    }

    //判断定位
    public static final boolean isLocationEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean networkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean gpsProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (networkProvider || gpsProvider) return true;
        return false;
    }

    private void setLocationService() {
        Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        this.startActivityForResult(locationIntent, REQUEST_CODE_LOCATION_SETTINGS);
    }

    //TODO 执行完上面的请求权限后，系统会弹出提示框让用户选择是否允许改权限。选择的结果可以在回到接口中得知：
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户允许改权限，0表示允许，-1表示拒绝 PERMISSION_GRANTED = 0， PERMISSION_DENIED = -1
                //permission was granted, yay! Do the contacts-related task you need to do.
                //这里进行授权被允许的处理
            } else {
                //permission denied, boo! Disable the functionality that depends on this permission.
                //这里进行权限被拒绝的处理
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * 蓝牙模块开关模式变化广播
     */
    private BroadcastReceiver mStatusReceive = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_ON:
                            break;
                        case BluetoothAdapter.STATE_ON:
                            //开始扫描
                            scanLeDevice(true);
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            bluetoothLeScanner = null;
                            mLeDeviceListAdapter.clear();
                            mLeDeviceListAdapter.notifyDataSetChanged();
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            break;
                    }
                    break;
            }
        }
    };


    //定义一个变量，来标识是否退出
    private static boolean isExit = false;
    Handler handler2 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isExit = false;
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exit() {
        if (!isExit) {
            isExit = true;
            ToastFactory.showToast(getApplicationContext(), "再按一次退出");
            //利用handler延迟发送更改状态信息
            handler2.sendEmptyMessageDelayed(0, 2000);
        } else {
            finish();
            System.exit(0);
        }
    }

}


