package com.etrans.bluetooth.le.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.etrans.bluetooth.le.DeviceControlActivity;
import com.etrans.bluetooth.le.Myapplication;
import com.etrans.bluetooth.le.R;
import com.etrans.bluetooth.le.app.IConstants;
import com.etrans.bluetooth.le.utils.ByteUtils;

/**
 * 项目名称：蓝牙BLE项目
 * 创建人：王楠魁
 * 创建时间：2018.9.29 10:35
 * 修改备注：
 */
public class FragmentOne extends Fragment implements View.OnClickListener {
    private static final String TAG = "FragmentOne";


    public static final int MSG_DATA = 29;//
    private TextView tv_ID;
    private Button btn_query;
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

    private void initView(View view) {

        tv_ID = (TextView) view.findViewById(R.id.tv_ID);
        btn_query = view.findViewById(R.id.btn_query);
        btn_query.setOnClickListener(this);

        setData();
    }

    private void setData() {
//            ShowData(myapp.getData());
        tv_ID.setText(myapp.getData());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_query:
                String str = IConstants.QUERY+
                        ByteUtils.Decimal0(IConstants.QUERYALL.length())+
                        IConstants.QUERYALL;
                //2a2a 03 fe 01 //上文
//                String str1 = IConstants.QUERY3;//01 02 03  //下文
//                String str2 = str1.length()+"";//01 02 03  //下文
//                ByteUtils.Decimal0(str1.length());




                //2a2a03fe0103010203cs 查询前面三个参数
                String validate_code = ByteUtils.checkXor(str.substring(4,str.length()));//验证码   cs
                str += validate_code;
                Log.i(TAG, "发送查询数据: str = "+str);
                Handler handler = DeviceControlActivity.getHandler();
                if (handler != null) {
                    Message msg = Message.obtain();
                    msg.what = DeviceControlActivity.MSG_SENDALLORDER;
                    msg.obj = str;
                    handler.sendMessage(msg);
                }
                break;

        }

    }
}
