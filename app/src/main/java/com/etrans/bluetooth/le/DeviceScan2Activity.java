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
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.etrans.bluetooth.le.utils.ByteUtils;

import java.util.ArrayList;
import java.util.List;

//用来扫描可用设备并将其展示出来的activity--主界面
@SuppressLint("NewApi")
public class DeviceScan2Activity extends Activity implements View.OnClickListener {
    private static final String TAG = "DeviceScanActivity";
    private static final int REQUEST_CODE_LOCATION_SETTINGS = 2;
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private Handler mHandler;
    private Button btn_scan, btn_stop;
    private ListView lv_devicelist;
    private static final int REQUEST_ENABLE_BT = 1;
    // 10秒后停止查找搜索.
    private static final long SCAN_PERIOD = 10000;


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //BluetoothLeScanner mBluetoothLeScanner = new BluetoothLeScanner();
//                    mBluetoothAdapter.stopLeScan(DeviceScanActivity.this.mLeScanCallback);
            if (bluetoothLeScanner != null) {
                bluetoothLeScanner.stopScan(scanCallback);
            }
            Showbtn(false);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setView();

        mHandler = new Handler();
        IntentFilter statusFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);//蓝牙模块开关模式变化广播
        registerReceiver(mStatusReceive, statusFilter);
        // 检查当前手机是否支持ble 蓝牙,如果不支持退出程序
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

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
        btn_scan = (Button) findViewById(R.id.btn_scan);
        btn_stop = (Button) findViewById(R.id.btn_stop);
        btn_scan.setOnClickListener(this);
        btn_stop.setOnClickListener(this);
        lv_devicelist = (ListView) findViewById(R.id.lv_devicelist);
        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        lv_devicelist.setAdapter(mLeDeviceListAdapter);
//        lv_devicelist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            System.out.println("==position==" + position);
//        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
//        if (device == null) return;
//        final Intent intent = new Intent(DeviceScan2Activity.this, DeviceControlActivity.class);
//        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
//        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
//                Showbtn(false);
////            mBluetoothAdapter.stopLeScan(mLeScanCallback); //停止扫描，连接前，必须停止扫描，不然，失败率很高nk
//            if (bluetoothLeScanner != null) {
//                bluetoothLeScanner.stopScan(scanCallback); //停止扫描，连接前，必须停止扫描，不然，失败率很高nk
//            }
//        startActivity(intent);
//            }
//        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        //如果当前手机蓝牙未开启，弹出dialog提示用户开启nk
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        //扫描周边BLE设备，获取扫描到的设备。nk
        scanLeDevice(true); // 为了确保设备上蓝牙能使用, 如果当前蓝牙设备没启用,弹出对话框向用户要求授予权限来启用

    }

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

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
        mHandler.removeCallbacks(runnable);
    }


    //搜索函数，反馈是mLeScanCallback
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            Showbtn(true);
            // Stops scanning after a pre-defined scan period.
            mHandler.removeCallbacks(runnable);
            mHandler.postDelayed(runnable, SCAN_PERIOD);

            //扫描很费电，要预设扫描周期，扫描一定周期就停止扫描
//            mBluetoothAdapter.startLeScan(DeviceScanActivity.this.mLeScanCallback);
            if (bluetoothLeScanner != null) {
                bluetoothLeScanner.startScan(scanCallback);//扫描全部


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
            Showbtn(false);
//            mBluetoothAdapter.stopLeScan(DeviceScanActivity.this.mLeScanCallback);
            if (bluetoothLeScanner != null) {
                bluetoothLeScanner.stopScan(scanCallback);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan:
                mLeDeviceListAdapter.clear();
                //如果当前手机蓝牙未开启，弹出dialog提示用户开启nk
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    scanLeDevice(true);
                }
                break;
            case R.id.btn_stop:
                scanLeDevice(false);
                mHandler.removeCallbacks(runnable);
                break;
        }
    }

    private void Showbtn(boolean mScanning) {
        if (mScanning) {
            btn_scan.setVisibility(View.GONE);
            btn_stop.setVisibility(View.VISIBLE);
        } else {
            btn_scan.setVisibility(View.VISIBLE);
            btn_stop.setVisibility(View.GONE);
        }
    }


    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = DeviceScan2Activity.this.getLayoutInflater();
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
                    Intent intent = new Intent(DeviceScan2Activity.this, DeviceControl3Activity.class);
                    intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, finalName);
                    intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                    Showbtn(false);
                    if (bluetoothLeScanner != null) {
                        bluetoothLeScanner.stopScan(scanCallback); //停止扫描，连接前，必须停止扫描，不然，失败率很高nk
                    }
                    startActivity(intent);
                }
            });

            return view;
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
//            if(result.getDevice().getName()!= null){
                mLeDeviceListAdapter.addDevice(result.getDevice());
                mLeDeviceListAdapter.notifyDataSetChanged();
//            }
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


    // 搜索函数更新到主线程来更新UI界面
    //作为BLE扫描结果的接口，实现如下。nk
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            String str2 = ByteUtils.getInstance().bytes2HexString(scanRecord);

            Log.i(TAG, "onLeScan: str2 = " + str2 + "\n");
//            String str = new String(scanRecord);
            //TODO 网上说不一样
            if (Looper.myLooper() == Looper.getMainLooper()) {
                // Android 5.0 及以上
                mLeDeviceListAdapter.addDevice(device);
                mLeDeviceListAdapter.notifyDataSetChanged();
            } else {
                // Android 5.0 以下
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLeDeviceListAdapter.addDevice(device);
                        mLeDeviceListAdapter.notifyDataSetChanged();
                    }
                });
            }
            /*runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeDeviceListAdapter.addDevice(device);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                    Toast.makeText(DeviceScanActivity.this, "hhhh", Toast.LENGTH_SHORT).show();
                }
            });*/
        }
    };

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        LinearLayout ll_info;
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
}