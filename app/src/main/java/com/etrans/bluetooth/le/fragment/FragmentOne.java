package com.etrans.bluetooth.le.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.etrans.bluetooth.le.Myapplication;
import com.etrans.bluetooth.le.R;

/**
 * 项目名称：蓝牙BLE项目
 * 创建人：王楠魁
 * 创建时间：2018.9.29 10:35
 * 修改备注：
 */
public class FragmentOne extends Fragment {
    private static final String TAG = "FragmentOne";


    public static final int MSG_DATA = 29;//
    private TextView tv_ID;
//    private StringBuilder mOutput = new StringBuilder();
    private Myapplication myapp;
//    private String data;

    public static Handler hand = null;

    public static Handler getHandler() {
        return hand;
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DATA:// 蓝牙设备名称
//                    app.getData();
//                    Myapplication.app.data = (String) msg.obj;
                    tv_ID.setText(myapp.getData());
//                    ShowData(myapp.getData());
                    break;

            }
        }
    };



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_one, container, false);
        hand = handler;
        myapp = (Myapplication) getActivity().getApplication();
        initView(view);
        return view;
    }

    private void initView(View view){

        tv_ID = (TextView) view.findViewById(R.id.tv_ID);

        setData();
    }
    private void setData(){
//            ShowData(myapp.getData());
        tv_ID.setText(myapp.getData());
    }

}
