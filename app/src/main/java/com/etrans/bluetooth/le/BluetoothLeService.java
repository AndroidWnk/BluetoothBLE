package com.etrans.bluetooth.le;

//引用

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.etrans.bluetooth.le.utils.HexUtil;

import java.util.UUID;

//管理连接和数据交换的 与GATTserver绑定的service---服务
@SuppressLint("NewApi")
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();
    //蓝牙模块的某个服务的UUID
    public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);
    //t_box两个UUID
    private static final UUID SPECIFIC_SERVICE_UUID = UUID.fromString("00005500-d102-11e1-9b23-e9025b00a5a5");
    private static final UUID SPECIFIC_CHARCTER_UUID = UUID.fromString("00005501-d102-11e1-9b23-e9025b00a5a5");
    private static final UUID SPECIFIC_CHARCTER_Notify_UUID = UUID.fromString("00005503-d102-11e1-9b23-e9025b00a5a5");

    //TboxUUID
    //服务uuid
    private static final String serviceUuidStr = "00002a50-0000-1000-8000-00805f9b34fb";
    //写通道 uuid
    private static final String writeCharactUuid = "00005501-d102-11e1-9b23-e9025b00a5a5";
    //通知通道 uuid
    private static final String notifyCharactUuid = "00005503-d102-11e1-9b23-e9025b00a5a5";

    ////
    private static final String SERVICE_CHARCTER_STR = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private static final String SERVICE_UUID_STR = "0000ffe0-0000-1000-8000-00805f9b34fb";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "com.etrans.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.etrans.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.etrans.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.etrans.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.etrans.bluetooth.le.EXTRA_DATA";

    private BluetoothLeService mBluetoothLeService;
    private StringBuilder mOutputInfo = new StringBuilder();
    private StringBuilder mOutput = new StringBuilder();
    public int number = 0;

    // GATT返回值，例如连接状态和service的改变 etc
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        //连接状态改变
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) { //连接了nk

                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                //连接上之后发现服务为返回true
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) { //断开了nk

                disConnect();//多次创建gatt连接对象的直接结果是创建过6个以上gatt后就会再也连接不上任何设备，原因应该是android中对BLE限制了同时连接的数量为6个 nk

                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        //发现到服务之后回调到此处nk
        //发现设备，遍历服务，初始化特征
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);//连接成功试图获取数据，
                Log.i(TAG, "onServicesDiscovered: 连接成功");
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
                Log.i(TAG, "onServicesDiscovered: 连接失败");
            }
        }

        //被读----读取特征值nk-------//characteristic.getValue()是数据，byte数组类型
        //接收消息nk-----接收消息
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                // number += 1;
            }
        }

        //特性书写-----写入特征值nk
        //发送信息nk
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            number = 0;
        }

        //特性改变 setCharacteristic(mNotifyCharacteristic, true);
        //使能通知，使能属性为Notify的特征值之后，以后特征值改变时候，就会回调到 onCharacteristicChanged()中----特征值的变化通知nk
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            //TODO
           /* if ( number == 19){
                number = 0;
            }else {*/
            number += 1;
            //}
        }


    };

    //广播连接状态的改变
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);

    }

    //广播的更新，包括数据的处理,读取heart的数据
    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        //这是openbulb处理和解析数据的方法
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
            intent.putExtra(EXTRA_DATA, characteristic.getValue());
        } else if(SPECIFIC_CHARCTER_Notify_UUID.equals(characteristic.getUuid())){ //这是接收的数据
            //对于所有的profile，都是利用HEX来进行传递的
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                mOutput.append(HexUtil.encodeHexStr(data)).append("\n");//在缓冲区做判断
                if(mOutput.toString().indexOf("232300")!=-1){ //如果缓冲区里包含起始符
                    mOutputInfo.delete(0,mOutputInfo.length());//删除之前的StringBuilder
                    mOutputInfo.append(HexUtil.encodeHexStr(data)).append("\n");//重新写进去
                }else{
                    mOutputInfo.append(HexUtil.encodeHexStr(data)).append("\n");//否则直接写
                }

                mOutput.delete(0,mOutput.length());//清空缓冲区
//                final StringBuilder stringBuilder = new StringBuilder(data.length);
//                for (byte byteChar : data) {
//                    stringBuilder.append(String.format("%02X ", byteChar));
//                }
//                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
//                intent.putExtra(EXTRA_DATA, new String(data));
                intent.putExtra(EXTRA_DATA, mOutputInfo.toString());
                Log.i(TAG, "stateNKbroadcastUpdate: 接收回来的数据："+mOutputInfo.toString());

                //之前
//                final StringBuilder stringBuilder = new StringBuilder(data.length);
//                for (byte byteChar : data) {
//                    stringBuilder.append(String.format("%02X ", byteChar));
//                }
//                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
//                intent.putExtra(EXTRA_DATA, new String(data));


            }
            //TODO 处理数据


        }else if(SPECIFIC_CHARCTER_UUID.equals(characteristic.getUuid())){ //这是发送出去的数据
            //对于所有的profile，都是利用HEX来进行传递的
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                mOutput.append(HexUtil.encodeHexStr(data)).append("\n");//在缓冲区做判断
                if(mOutput.toString().indexOf("232300")!=-1){ //如果缓冲区里包含起始符
                    mOutputInfo.delete(0,mOutputInfo.length());//删除之前的StringBuilder
                    mOutputInfo.append(HexUtil.encodeHexStr(data)).append("\n");//重新写进去
                }else{
                    mOutputInfo.append(HexUtil.encodeHexStr(data)).append("\n");//否则直接写
                }

                mOutput.delete(0,mOutput.length());//清空缓冲区
//                final StringBuilder stringBuilder = new StringBuilder(data.length);
//                for (byte byteChar : data) {
//                    stringBuilder.append(String.format("%02X ", byteChar));
//                }
//                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
//                intent.putExtra(EXTRA_DATA, new String(data));
                intent.putExtra(EXTRA_DATA, mOutputInfo.toString());
                Log.i(TAG, "stateNKbroadcastUpdate: 发送出去的数据："+mOutputInfo.toString());

                //之前
//                final StringBuilder stringBuilder = new StringBuilder(data.length);
//                for (byte byteChar : data) {
//                    stringBuilder.append(String.format("%02X ", byteChar));
//                }
//                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
//                intent.putExtra(EXTRA_DATA, new String(data));


            }
            //TODO 处理数据


        }
        sendBroadcast(intent);
    }

    public static int byteArrayToInt(byte[] b) {
        byte[] a = new byte[4];
        int i = a.length - 1, j = b.length - 1;
        for (; i >= 0; i--, j--) {//从b的尾部(即int值的低位)开始copy数据
            if (j >= 0)
                a[i] = b[j];
            else
                a[i] = 0;//如果b.length不足4,则将高位补0
        }
        int v0 = (a[0] & 0xff) << 24;//&0xff将byte值无差异转成int,避免Java自动类型提升后,会保留高位的符号位
        int v1 = (a[1] & 0xff) << 16;
        int v2 = (a[2] & 0xff) << 8;
        int v3 = (a[3] & 0xff);
        return v0 + v1 + v2 + v3;
    }

    //初始化本地iBinder
    public class LocalBinder extends Binder { //Binder
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    //初始化本地蓝牙适配器，如果初始化成功，返回true
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    //连接远程GATTserver，如果初始化成功，返回true。回调触发函数BluetoothGattCallback#onConnectionStateChange
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        //先前连接的设备。尝试重新连接。
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            Log.d(TAG, "尝试使用现有的mbluestotgatt进行连接");
            if (mBluetoothGatt.connect()) { //注意到我们App的旧代码中也有对于初次连接失败的处理，它调用了BluetoothGatt.connect()
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "没有找到设备。无法连接.");
            return false;
        }
        // 我们想要直接连接到设备，所以我们正在设置自动连接。
        // parameter to false.
        //这个mBluetoothGatt很关键，后面要调用这个类的方法。
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);//尝试连接设备nk,mGattCallback回调
        Log.d(TAG, "尝试创建新连接.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;

        return true;
    }

    //断开连接远程GATTserver，回调触发函数BluetoothGattCallback#onConnectionStateChange
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    //结束连接ble设备后，释放资源
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    //获取C2541的特性服务 直接获取设备数据
    public BluetoothGattCharacteristic getBluetoothGattCharacteristic() {
        // 先获取BluetoothGattService,
        // 再通过BluetoothGattService获取BluetoothGattCharacteristic特征值(UUID查询数据)nk
        //有了这两个Service和characteristic的UUID，就可以对蓝牙发送数据，并发出通知（当写数据发生改变时发出）。nk
        return mBluetoothGatt.getService(SPECIFIC_SERVICE_UUID).getCharacteristic(SPECIFIC_CHARCTER_UUID);

    }
    public BluetoothGattCharacteristic getBluetoothGattCharacteristicNotify() {
        // 先获取BluetoothGattService,
        // 再通过BluetoothGattService获取BluetoothGattCharacteristic特征值(UUID查询数据)nk
        //有了这两个Service和characteristic的UUID，就可以对蓝牙发送数据，并发出通知（当写数据发生改变时发出）。nk
        return mBluetoothGatt.getService(SPECIFIC_SERVICE_UUID).getCharacteristic(SPECIFIC_CHARCTER_Notify_UUID);

    }

    //开启或者关闭notification  虽然里面有heart 的内容
    //开启通知:设置开启之后，才能在onCharacteristicRead()这个方法中收到数据。
    public void setCharacteristic(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (0 != (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY)) {
            mBluetoothGatt.setCharacteristicNotification(characteristic, true);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }else{
            mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);//发送消息,在onCharacteristicRead接收信息
        }

        //TODO  这里都是需要改变的
        // This is specific to Heart Rate Measurement.这是特定的心率测量
//        if (SPECIFIC_CHARCTER_Notify_UUID.equals(characteristic.getUuid())) {
//            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
//                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            mBluetoothGatt.writeDescriptor(descriptor);
//        }
//        else if (0 != (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY)) {
//            mBluetoothGatt.setCharacteristic(characteristic, true);
//            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
//                    UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            mBluetoothGatt.writeDescriptor(descriptor);
//
//        }
    }





    //读取characteristic，回调触发函数BluetoothGattCallback#onCharacteristicRead
    //读取蓝牙中数据nk
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    //发送characteristic，回调触发函数BluetoothGattCallback#onCharacteristicWrite
    //向蓝牙中写入数据。nk
    //TODO 添加write的函数
    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
        return true;
    }



    // 断开连接
    public boolean disConnect() {
        Log.d(TAG, "mBluetoothGatt" + mBluetoothGatt);
        if (mBluetoothGatt != null) {
//            setEnableNotify(BleConnectUtil.mBluetoothGattCharacteristicNotify, false);
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
            mConnectionState = STATE_DISCONNECTED;
//            mDeviceAddress = "";
            return true;
        }
        return false;
    }

    //取回可以连接的GATTservice，在BluetoothGatt#discoverServices()运行成功后可以被调用，返回a list of supported services
    /*public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }*/


}
