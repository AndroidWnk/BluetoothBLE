package com.etrans.bluetooth.le.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.etrans.bluetooth.le.R;
import com.etrans.bluetooth.le.utils.APKVersionCodeUtils;

/**
 * 项目名称：蓝牙BLE项目
 * 创建人：王楠魁
 * 创建时间：2018.9.29 10:35
 * 修改备注：
 */
public class FragmentThree extends Fragment {

    private TextView tv_version;


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
        View view=inflater.inflate(R.layout.fragment_three,container,false);
        hand = handler;
        initView(view);
        return view;
    }

    private void initView(View view){

        tv_version = (TextView) view.findViewById(R.id.tv_version);

        setData();
    }
    private void setData(){
        tv_version.setText(APKVersionCodeUtils.getVerName(getActivity())+"");
    }

}
