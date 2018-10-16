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
    private TextView tv_ID, tv_vin, tv_devicenum, tv_callnum, tv_ip, tv_port, tv_HardVersion, tv_PorVersion;
    private Button btn_query;
    //    private StringBuilder mOutput = new StringBuilder();
    private Myapplication myapp;
//    private String data;

    private ResultQuerybean showdata;
    private KProgressHUD dialog;
    public static Handler hand = null;

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
                        tv_port.setText(ByteUtils.HexStringTointeger(showdata.getPort1()) + "");
                        tv_HardVersion.setText(HexUtil.hexStringToString(showdata.getHardware_ver()));
                        tv_PorVersion.setText(HexUtil.hexStringToString(showdata.getSoftware_ver()));

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
                        tv_port.setText("");
                        tv_HardVersion.setText("");
                        tv_PorVersion.setText("");
                    }
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
        tv_port = (TextView) view.findViewById(R.id.tv_port);
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
                }else{
                    ToastFactory.showToast(getActivity(),"蓝牙断开发送失败");
                }

//                dialog.show();
                break;

        }

    }

//    public void query() {
//        String str = "0" + IConstants.QUERYALL.length() / 2;//个数
//        String SendData = IConstants.QUERY +
//                "00" + ByteUtils.integerToHexString(IConstants.QUERYALL.length() / 2 + str.length() / 2) + //长度hex值
////                        ByteUtils.Decimal0(IConstants.QUERYALL.length())+
//                str +//个数
//                IConstants.QUERYALL;
//        //2a2a 03 fe 01 //上文
////                String str1 = IConstants.QUERY3;//01 02 03  //下文
////                String str2 = str1.length()+"";//01 02 03  //下文
////                ByteUtils.Decimal0(str1.length());
//
//        //2a2a03fe0103010203cs 查询前面三个参数
//        String validate_code = ByteUtils.checkXor(SendData.substring(4, SendData.length()));//验证码   cs
//        SendData += validate_code;//补上验证码 2a2a03FE010801020304050a0f10E0
//
//        /**
//         * 232300 1a 01 0000000000000000000000000000 ff
//         2b2b00 2a2a03FE010801020304050a0f10E000 ff
//         */
//        /**
//         * 起始符 232300
//         * 长度  1a
//         * 包数  01
//         * 补零  000000....
//         * 校验 ff
//         */
//
//        StringBuilder headInfo = new StringBuilder();
//        headInfo.delete(0, headInfo.length());//删除之前的StringBuilder
//        headInfo.append("232300");
//        headInfo.append(ByteUtils.integerToHexString(SendData.length() / 2));//长度hex
//        headInfo.append(ByteUtils.integerToHexString((int) Math.ceil(SendData.length() / 32.0))); //包数
//        String data = ByteUtils.addZeroForNum(headInfo.toString(), 38);//补零
//
//        String validate_code1 = ByteUtils.checkXor(headInfo.toString().substring(4, headInfo.toString().length()));//验证码   cs
//        data += validate_code1;
//
//
//        /**
//         * 2b2b00 2a2a03FE010801020304050a0f10E000 ff
//         */
//        /**
//         * 起始符 2b2b00
//         * 单元数据2a2a03FE010801020304050a0f10E0
//         * 补零  000000....
//         * 校验 ff
//         */
//        int index = 0;
//        StringBuilder contentInfo = new StringBuilder();
//        for (int i = 0; i < SendData.length(); i = i + 32) {
//            final int finalI = i;
//            if (SendData.length() - i >= 32) {
//                StringBuilder info = new StringBuilder();
//                info.delete(0, info.length());//删除之前的StringBuilder
//                info.append("2b2b0");
//                info.append(index + "");
//                info.append(SendData.substring(finalI, finalI + 32));
//                String validate_code2 = ByteUtils.checkXor(info.toString().substring(4, info.toString().length()));//验证码   cs
//                info.append(validate_code2);
//                index++;
//                contentInfo.append(info.toString());
//                Log.i(TAG, "onClick: OK");
////                        shortOrder[0] = finalHexdata.substring(finalI, finalI + 32);
//            } else {
//                StringBuilder info = new StringBuilder();
//                info.delete(0, info.length());//删除之前的StringBuilder
//                info.append("2b2b0");
//                info.append(index + "");
//                info.append(SendData.substring(finalI, SendData.length()));
//                String data3 = ByteUtils.addZeroForNum(info.toString(), 38);//补零
//                String validate_code3 = ByteUtils.checkXor(info.toString().substring(4, info.toString().length()));//验证码   cs
//                data3 += validate_code3;
//                contentInfo.append(data3);
//                Log.i(TAG, "onClick: OK");
////                        shortOrder[0] = finalHexdata.substring(finalI, finalHexdata.length());
//            }
//        }
//
//        Log.i(TAG, "onClick: contentInfo最终 = " + contentInfo.toString());
//        data += contentInfo.toString();
//
//        Log.i(TAG, "onClick: data最终 = " + data);
//
//
//        Log.i(TAG, "发送设置数据: SendData = " + SendData);
//        Handler handler = DeviceControlActivity.getHandler();
//        if (handler != null) {
//            Message msg = Message.obtain();
//            msg.what = DeviceControlActivity.MSG_SENDALLORDER;
//            msg.obj = data;
//            handler.sendMessage(msg);
//        }
//        Log.i(TAG, "onClick: OK");
//    }
}
