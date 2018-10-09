package com.etrans.bluetooth.le.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.etrans.bluetooth.le.Myapplication;
import com.etrans.bluetooth.le.R;

/**
 * 项目名称：蓝牙BLE项目
 * 创建人：王楠魁
 * 创建时间：2018.9.29 10:35
 * 修改备注：
 */
public class FragmentTwo extends Fragment {
    private static final String TAG = "FragmentTwo";

//    private StringBuilder mOutput = new StringBuilder();
    private Myapplication myapp;
    private EditText et_ID;

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


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_two,container,false);
        hand = handler;
        myapp = (Myapplication) getActivity().getApplication();
        initView(view);
        return view;
    }

    private void initView(View view){
        et_ID = (EditText) view.findViewById(R.id.et_ID);
    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        setData();
        super.onViewStateRestored(savedInstanceState);
    }
    private void setData(){
//        ShowData(myapp.getData());
        et_ID.setText(myapp.getData());
    }




}
