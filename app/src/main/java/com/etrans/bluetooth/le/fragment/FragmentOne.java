package com.etrans.bluetooth.le.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.etrans.bluetooth.le.R;

/**
 * 项目名称：蓝牙BLE项目
 * 创建人：王楠魁
 * 创建时间：2018.9.29 10:35
 * 修改备注：
 */
public class FragmentOne extends Fragment {

    public static Handler hand = null;

    public static Handler getHandler() {
        return hand;
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
//                case Config.MSG_DEVICENAME:// 蓝牙设备名称
//                    String name = (String) msg.obj;
//                    mLocalName = name;
//                    break;

            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_one, container, false);
        hand = handler;
        return view;
    }



//    发送方：
//    Handler handler = MainActivity.getHandler();
//
//if (handler != null) {
//        Message msg = Message.obtain();
//        msg.what = MainActivity.MSG_CURRENT_CONNECT_DEVICE_NAME;
//        msg.obj = name;
//        handler.sendMessage(msg);
//    }
//
//    public static final int MSG_CURRENT_CONNECT_DEVICE_NAME = 29;//获取手机设备名称
}
