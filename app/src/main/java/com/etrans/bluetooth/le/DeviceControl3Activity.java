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

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.etrans.bluetooth.le.fragment.FragmentOne;
import com.etrans.bluetooth.le.fragment.FragmentThree;
import com.etrans.bluetooth.le.fragment.FragmentTwo;

import java.util.ArrayList;


//对于给定的ble设备，这个activity提供接口去连接，展示数据，service和characteris。---设备详情
//The Activity communicates with {@code BluetoothLeService}, which in turn interacts with the Bluetooth LE API.
public class DeviceControl3Activity extends Activity implements View.OnClickListener {
    private final static String TAG = DeviceControl3Activity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    private Button openBulb;
    //    private EditText setTimeText;   //接受数据输入句柄123
    private Button setTimeBtn, btn_connect, btn_disconnect;    //接受定时开关指令

    private String mDeviceName;
    private String mDeviceAddress;
    /*private ExpandableListView mGattServicesList;*/
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUIDUtils";
    private TextView tv_device_address, tv_DeviceName;

    private Fragment fraOne, fraTwo, fraThree;
    private Button btn_query, btn_set, btn_about;
    private FragmentTransaction mFragmentTransaction;
    private LinearLayout ll_deviceinfo;

    private Button btn_scan;
    private boolean showdeviceinfo = false;

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
            try {
                mBluetoothLeService.connect(mDeviceAddress); //到服务里面的方法去连接
                updateConnectionState(R.string.Connection);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
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
            /* DeviceControlActivity.this.mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);*/

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                Showbtn();
                updateConnectionState(R.string.connected);
//                invalidateOptionsMenu();
                //TODO 刚加上
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                Showbtn();
                updateConnectionState(R.string.disconnected);
//                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) { //GATT行动服务发现nk
                //到这里就可以去获取数据了，可以去显示获取数据的按钮
                Toast.makeText(DeviceControl3Activity.this, "连接成功可以主动获取数据了", Toast.LENGTH_LONG).show();
                /*displayGattServices(mBluetoothLeService.getSupportedGattServices());*/
                //TODO在此处修改了，使得发现服务后直接开启获得数据,连接成功了就根据UUID获取数据
//                mNotifyCharacteristic = mBluetoothLeService.getBluetoothGattCharacteristic();//根据写UUID找到写特征,暂时先注释，有UUID的时候打开并获取
                if ((mNotifyCharacteristic == null) || ((0x10 | mNotifyCharacteristic.getProperties()) <= 0)) {
                    return;
                }
                //得到这两个Service和characteristic就可以向蓝牙发送数据了。nk
                mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);//设置开启之后，才能在onCharacteristicRead()这个方法中收到数据。
                //TODO 刚加上
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //TODO在此处修改了，使得发现服务后直接开启获得数据
                mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                //使得定时的textview里面不显示数据
                /*String str2 = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                setTimeText.setText(str2);*/
            }
        }
    };

    //设备未连接时清除界面内容
    private void clearUI() {
        /* mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);*/
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics3);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        setView();//初始化监听nk
        tv_device_address.setText(mDeviceAddress);
        tv_DeviceName.setText(mDeviceName);
        Intent gattIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattIntent, mServiceConnection, BIND_AUTO_CREATE);//绑定服务
    }


    private void setView() {
        tv_device_address = (TextView) findViewById(R.id.device_address);
        tv_device_address.setText(mDeviceAddress);
        tv_DeviceName = (TextView) findViewById(R.id.device_name);
        tv_DeviceName.setText(mDeviceName);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);
        openBulb = (Button) findViewById(R.id.openBulb);
//        setTimeText = (EditText) findViewById(R.id.setTimeText);123
        setTimeBtn = (Button) findViewById(R.id.setTimeBtn);
        btn_disconnect = (Button) findViewById(R.id.btn_disconnect);
        btn_connect = (Button) findViewById(R.id.btn_connect);
        btn_connect.setOnClickListener(this);
        btn_disconnect.setOnClickListener(this);
        Showbtn();

        //fragment
        //主要逻辑在MainActivity的onClick中
        btn_query = (Button) findViewById(R.id.btn_query);
        btn_query.setOnClickListener(this);
        btn_set = (Button) findViewById(R.id.btn_set);
        btn_set.setOnClickListener(this);
        btn_about = (Button) findViewById(R.id.btn_about);
        btn_about.setOnClickListener(this);
        ll_deviceinfo = (LinearLayout) findViewById(R.id.ll_deviceinfo);
        btn_scan = (Button) findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(this);


        mFragmentTransaction = getFragmentManager().beginTransaction();
        if (fraOne == null) {
            fraOne = new FragmentOne();
        }
        mFragmentTransaction.replace(R.id.fl_main, fraOne).commit();
    }

    private void Showbtn() {
        if (mConnected) {
            btn_connect.setVisibility(View.GONE);
            btn_disconnect.setVisibility(View.VISIBLE);
        } else {
            btn_connect.setVisibility(View.VISIBLE);
            btn_disconnect.setVisibility(View.GONE);
        }
    }

    //重启时、开始时注册广播
    @Override
    protected void onResume() {
        super.onResume();
        //注册广播
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            updateConnectionState(R.string.Connection);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    //停止时，注销广播
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    //关闭activity
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    //判断是否连接，若未连接就展示没有连接的设备
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.gatt_services, menu);
//        if (mConnected) {
//            menu.findItem(R.id.menu_connect).setVisible(false);
//            menu.findItem(R.id.menu_disconnect).setVisible(true);
//        } else {
//            menu.findItem(R.id.menu_connect).setVisible(true);
//            menu.findItem(R.id.menu_disconnect).setVisible(false);
//        }
//        return true;
//    }

    //主菜单item被点击之后，intend获取来自deviceScanActivity的address和name从而进行连接
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.menu_connect:
//                mBluetoothLeService.connect(mDeviceAddress);
//                return true;
//            case R.id.menu_disconnect:
//                mBluetoothLeService.disconnect();
//                return true;
//            case android.R.id.home:
//                onBackPressed();
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    //显示当前连接状态
    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    //显示传来的数据
    private void displayData(String data) {
        if (data != null) {
            /* mDataField.append(data);*/
            mDataField.setText(data);
        }
    }

    //开灯按键响应函数
    public void onSwitchBulbClicked(View v) {
        if (mConnected == false) {
            Toast.makeText(DeviceControl3Activity.this, "请先连接设备", Toast.LENGTH_SHORT).show();
        } else {
            if (openBulb.getText() == "开灯") {
                /* sendMsg("Sapp_mode1E");*/
                sendMsg("Sopen_led1E");
              /*  openBulb.getBackground().setAlpha(50);
                setTimeBtn.getBackground().setAlpha(50);*/
                openBulb.setText("关灯");
                setTimeBtn.setText("定时关灯");
            } else/* if (openBulb.getText() == "关灯")*/ {
                /*sendMsg("Sapp_mode1E");*/
                sendMsg("Sclose_led1E");
                /*openBulb.getBackground().setAlpha(200);
                setTimeBtn.getBackground().setAlpha(200);*/
                openBulb.setText("开灯");
                setTimeBtn.setText("定时开灯");
            }

        }
    }

    //定时发送开关灯命令
    public void onSetTimeOpenBulb(View v) {
        //转化成是string类型
//        String mText = setTimeText.getText().toString();123
//        sendMsg(mText);123
        //判断用户输入的定时是否是数字
        //TODO 对用户定时的时间是否有要求
        /*Pattern p = Pattern.compile("[^6]");//[0-9]*
        Matcher m = p.matcher(mText);
        if (mText.equals("")){
            Toast.makeText(DeviceControlActivity.this, "请先输入时间", Toast.LENGTH_SHORT).show();
        }else{
            if (m.matches()){
                if (setTimeBtn.getText() == "定时开灯"){
                    sendMsg("Sopen:"+mText+"E");
                    Toast.makeText(DeviceControlActivity.this, "在"+mText+"分钟后"+setTimeBtn.getText(), Toast.LENGTH_SHORT).show();
                    setTimeText.setText(null);
                    openBulb.getBackground().setAlpha(50);
                    setTimeBtn.getBackground().setAlpha(50);
                    setTimeBtn.setText("定时关灯");
                    openBulb.setText("关灯");
                }else*//* if (setTimeBtn.getText() == "定时开灯")*//*{
                    sendMsg("Sclose:"+mText+"E");
                    Toast.makeText(DeviceControlActivity.this, "在"+mText+"分钟后"+setTimeBtn.getText(), Toast.LENGTH_SHORT).show();
                    setTimeText.setText(null);
                    openBulb.getBackground().setAlpha(200);
                    setTimeBtn.getBackground().setAlpha(200);
                    setTimeBtn.setText("定时开灯");
                    openBulb.setText("开灯");
                }
            }else{
                Toast.makeText(DeviceControlActivity.this, "请输入数字", Toast.LENGTH_SHORT).show();
            }
        }*/
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
            case R.id.btn_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                updateConnectionState(R.string.Connection);
                break;
            case R.id.btn_disconnect:
                mBluetoothLeService.disconnect();
                break;
            case R.id.btn_scan:
                showdeviceinfo = !showdeviceinfo;
                if (showdeviceinfo) {
                    ll_deviceinfo.setVisibility(View.GONE);
                } else {
                    ll_deviceinfo.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.btn_query:
                if (fraOne == null) {
                    fraOne = new FragmentOne();
                }
                mFragmentTransaction.replace(R.id.fl_main, fraOne).commit();
//                mFragmentTransaction.addToBackStack(null);//添加fragment到返回栈
//                mFragmentTransaction.commit();
                break;
            case R.id.btn_set:
                if (fraTwo == null) {
                    fraTwo = new FragmentTwo();
                }
                mFragmentTransaction.replace(R.id.fl_main, fraTwo).commit();
//                mFragmentTransaction.addToBackStack(null);//添加fragment到返回栈
//                mFragmentTransaction.commit();
                break;
            case R.id.btn_about:
                if (fraThree == null) {
                    fraThree = new FragmentThree();
                }
                mFragmentTransaction.replace(R.id.fl_main, fraThree).commit();
//                mFragmentTransaction.addToBackStack(null);//添加fragment到返回栈
//                mFragmentTransaction.commit();
                break;
        }
    }
}


