package com.etrans.bluetooth.le.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.etrans.bluetooth.le.Myapplication;
import com.etrans.bluetooth.le.R;
import com.etrans.bluetooth.le.bean.ResultQuerybean;
import com.etrans.bluetooth.le.utils.ByteUtils;
import com.etrans.bluetooth.le.utils.CheckUtils;
import com.etrans.bluetooth.le.utils.GbkCode;
import com.etrans.bluetooth.le.utils.HexUtil;
import com.etrans.bluetooth.le.utils.ToastFactory;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.io.UnsupportedEncodingException;

/**
 * 项目名称：蓝牙BLE项目
 * 创建人：王楠魁
 * 创建时间：2018.9.29 10:35
 * 修改备注：
 */
public class FragmentOne extends Fragment implements View.OnClickListener {
    private static final String TAG = "FragmentOne";


    public static final int MSG_DATA = 29;//
    private TextView tv_ID, tv_vin, tv_devicenum, tv_callnum, tv_ip, tv_port, tv_ip2, tv_port2,tv_HardVersion, tv_PorVersion;
    private Button btn_query;
    //    private StringBuilder mOutput = new StringBuilder();
    private Myapplication myapp;
//    private String data;

    private ResultQuerybean showdata;
    private KProgressHUD dialog;
    public static Handler hand = null;
    private boolean querystate = false;
    private Handler mHandler = new Handler();;
    private static final int TIME_DELAY = 10000;//10秒超时处理
    private Runnable runnablequery = new Runnable() {
        public void run() {
            if (querystate) { //30秒后如果还是正在关闭状态则恢复原来状态
                querystate = false;
                ToastFactory.showToast(getActivity(),"查询失败！");
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        }
    };


    public static Handler getHandler() {
        return hand;
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DATA:// 蓝牙设备名称
                    //tv_vin,tv_devicenum,tv_callnum,tv_ip,tv_port,tv_HardVersion,tv_PorVersion
                    showdata = myapp.getShowdata();

                    if (showdata != null) {
//                    app.getData();
//                    Myapplication.app.data = (String) msg.obj;
                        tv_ID.setText(HexUtil.hexStringToString(showdata.getID_Num()));
                        tv_vin.setText(HexUtil.hexStringToString(showdata.getVin_Num()));
//                        tv_devicenum.setText(HexUtil.hexStringToString(showdata.getCar_Num()));
                        tv_callnum.setText(HexUtil.hexStringToString(showdata.getPhone_Num()));
                        tv_ip.setText(HexUtil.hexStringToString(showdata.getIP1()));
                        tv_ip2.setText(HexUtil.hexStringToString(showdata.getIP2()));
                        tv_port.setText(ByteUtils.HexStringTointeger(showdata.getPort1()) + "");
                        tv_port2.setText(ByteUtils.HexStringTointeger(showdata.getPort2()) + "");
                        tv_HardVersion.setText(HexUtil.hexStringToString(showdata.getHardware_ver()));
                        tv_PorVersion.setText(HexUtil.hexStringToString(showdata.getSoftware_ver()));

                        //设置车牌号
                        String str = showdata.getCar_Num();//D4A5414130303034
                        String data = str.substring(0,4);
                        try {
                            String decodedata = GbkCode.decode(data);//豫
                            tv_devicenum.setText(decodedata+HexUtil.hexStringToString(str.substring(data.length(),str.length())));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
//                        tv_devicenum.setText(str1+HexUtil.hexStringToString(showdata.getCar_Num()));
//                    ShowData(myapp.getData());
                    } else {
                        tv_ID.setText("");
                        tv_vin.setText("");
                        tv_devicenum.setText("");
                        tv_callnum.setText("");
                        tv_ip.setText("");
                        tv_ip2.setText("");
                        tv_port.setText("");
                        tv_port2.setText("");
                        tv_HardVersion.setText("");
                        tv_PorVersion.setText("");
                    }
                    mHandler.removeCallbacks(runnablequery);
                    if (dialog != null) {
                        dialog.dismiss();
                    }
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

        //tv_vin,tv_devicenum,tv_callnum,tv_ip,tv_port,tv_HardVersion,tv_PorVersion
        tv_ID = (TextView) view.findViewById(R.id.tv_ID);
        tv_vin = (TextView) view.findViewById(R.id.tv_vin);
        tv_devicenum = (TextView) view.findViewById(R.id.tv_devicenum);
        tv_callnum = (TextView) view.findViewById(R.id.tv_callnum);
        tv_ip = (TextView) view.findViewById(R.id.tv_ip);
        tv_ip2 = (TextView) view.findViewById(R.id.tv_ip2);
        tv_port = (TextView) view.findViewById(R.id.tv_port);
        tv_port2 = (TextView) view.findViewById(R.id.tv_port2);
        tv_HardVersion = (TextView) view.findViewById(R.id.tv_HardVersion);
        tv_PorVersion = (TextView) view.findViewById(R.id.tv_PorVersion);
        btn_query = view.findViewById(R.id.btn_query);
        dialog = CheckUtils.showDialog(getActivity());

        btn_query.setOnClickListener(this);
        setData();
    }

    private void setData() {
//        query();
//            ShowData(myapp.getData());
//        tv_ID.setText(myapp.getData());

        Handler handler = FragmentOne.getHandler();
        if (handler != null) {
            handler.sendEmptyMessage(FragmentOne.MSG_DATA);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_query:

                if(myapp.ismConnected()){
                    HexUtil.query();
                    dialog.show();
                    querystate = true;
                    mHandler.postDelayed(runnablequery, TIME_DELAY);//30秒后如果还是正在关闭状态则恢复状态
                }else{
                    ToastFactory.showToast(getActivity(),"蓝牙断开发送失败");
                }
                break;

        }

    }

}
