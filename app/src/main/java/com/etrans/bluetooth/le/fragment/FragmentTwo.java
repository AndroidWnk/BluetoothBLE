package com.etrans.bluetooth.le.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.etrans.bluetooth.le.DeviceControlActivity;
import com.etrans.bluetooth.le.Myapplication;
import com.etrans.bluetooth.le.R;
import com.etrans.bluetooth.le.app.IConstants;
import com.etrans.bluetooth.le.bean.ResultQuerybean;
import com.etrans.bluetooth.le.bean.ResultSetbean;
import com.etrans.bluetooth.le.utils.ByteUtils;
import com.etrans.bluetooth.le.utils.CheckUtils;
import com.etrans.bluetooth.le.utils.GbkCode;
import com.etrans.bluetooth.le.utils.HexUtil;
import com.etrans.bluetooth.le.utils.JSONUtils;
import com.etrans.bluetooth.le.utils.ToastFactory;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * 项目名称：蓝牙BLE项目
 * 创建人：王楠魁
 * 创建时间：2018.9.29 10:35
 * 修改备注：
 */
public class FragmentTwo extends Fragment implements View.OnClickListener {
    private static final String TAG = "FragmentTwo";
    public static final int MSG_SET_DATA = 1001;//
    public static final int MSG_DATA = 29;//
    //    private StringBuilder mOutput = new StringBuilder();
    private Myapplication myapp;
    private EditText et_ID, et_callnum, et_vin, et_devicenum;
    //    private TextView tv_ip, tv_port;
    private EditText et_ip, et_port;
    private boolean ID_Changed, callnum_Changed, vin_Changed, devicenum_Changed, port_Changed, ip_Changed;
    private Button btn_upgrade, btn_set;
    private ImageView img_add, img_reduce;
    private LinearLayout ll_ip1, ll_ip2;
    private ResultQuerybean showdata;
    private KProgressHUD dialog;

    public static Handler hand = null;

    public static Handler getHandler() {
        return hand;
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case FragmentTwo.MSG_DATA:

                    //tv_vin,tv_devicenum,tv_callnum,tv_ip,tv_port,tv_HardVersion,tv_PorVersion
                    showdata = myapp.getShowdata();

                    if (showdata != null) {
//                    app.getData();
//                    Myapplication.app.data = (String) msg.obj;
                        et_ID.setText(HexUtil.hexStringToString(showdata.getID_Num()));
                        et_vin.setText(HexUtil.hexStringToString(showdata.getVin_Num()));
                        et_devicenum.setText(HexUtil.hexStringToString(showdata.getCar_Num()));
                        et_callnum.setText(HexUtil.hexStringToString(showdata.getPhone_Num()));
                        et_ip.setText(HexUtil.hexStringToString(showdata.getIP1()));
                        et_port.setText(ByteUtils.HexStringTointeger(showdata.getPort1()) + "");
                        String str = showdata.getCar_Num();//D4A5414130303034
                        String data = str.substring(0, 4);
                        try {
                            String decodedata = GbkCode.decode(data);//豫
                            et_devicenum.setText(decodedata + HexUtil.hexStringToString(str.substring(data.length(), str.length())));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }


                        ID_Changed = false;
                        callnum_Changed = false;
                        vin_Changed = false;
                        devicenum_Changed = false;
                        ip_Changed = false;
                        port_Changed = false;
                        SetListener();
                    }
                    break;
                case FragmentTwo.MSG_SET_DATA://
                    ResultSetbean resultSetbean = new ResultSetbean();
                    resultSetbean = (ResultSetbean) msg.obj;
                    if (resultSetbean.isCar_Num()) {
                        et_devicenum.setTextColor(getResources().getColor(R.color.map_color));
                    }

                    if (resultSetbean.isID_Num()) {
                        et_ID.setTextColor(getResources().getColor(R.color.map_color));
                    }
                    if (resultSetbean.isPhone_Num()) {
                        et_callnum.setTextColor(getResources().getColor(R.color.map_color));
                    }
                    if (resultSetbean.isIP1()) {
//                        et_ID.setTextColor(Color.GREEN);
                        et_ip.setTextColor(getResources().getColor(R.color.map_color));
                    }
                    if (resultSetbean.isPort1()) {
//                        et.setTextColor(Color.GREEN);
                        et_port.setTextColor(getResources().getColor(R.color.map_color));
                    }

                    if (resultSetbean.isVin_Num()) {
                        et_vin.setTextColor(getResources().getColor(R.color.map_color));
                    }
                    if (resultSetbean.isSoftware_ver()) {
//                        et_ID.setTextColor(Color.GREEN);
                    }
                    if (resultSetbean.isHardware_ver()) {
//                        et.setTextColor(Color.GREEN);
                    }
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    HexUtil.query();
                    break;

            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_two, container, false);
        hand = handler;
        dialog = CheckUtils.showDialog(getActivity());
        myapp = (Myapplication) getActivity().getApplication();
        initView(view);
//        SetListener();
        return view;
    }

    private void initView(View view) {
        et_ID = (EditText) view.findViewById(R.id.et_ID);
        et_callnum = (EditText) view.findViewById(R.id.et_callnum);
        et_vin = (EditText) view.findViewById(R.id.et_vin);
        et_devicenum = (EditText) view.findViewById(R.id.et_devicenum);
        et_ip = (EditText) view.findViewById(R.id.et_ip);
        et_port = (EditText) view.findViewById(R.id.et_port);
        img_add = view.findViewById(R.id.img_add);
        img_reduce = view.findViewById(R.id.img_reduce);
        btn_upgrade = view.findViewById(R.id.btn_upgrade);
        btn_set = view.findViewById(R.id.btn_set);
        ll_ip1 = view.findViewById(R.id.ll_ip1);
        ll_ip2 = view.findViewById(R.id.ll_ip2);

        img_add.setOnClickListener(this);
        img_reduce.setOnClickListener(this);
        btn_set.setOnClickListener(this);
        btn_upgrade.setOnClickListener(this);
        ll_ip2.setVisibility(View.GONE);
    }

    private void SetListener() {
        et_ID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.e("输入前确认执行该方法", "开始输入");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.e("输入过程中执行该方法", "文字变化");
                ID_Changed = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.e("输入结束执行该方法", "输入结束");
            }
        });
        et_callnum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.e("输入前确认执行该方法", "开始输入");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.e("输入过程中执行该方法", "文字变化");
                callnum_Changed = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.e("输入结束执行该方法", "输入结束");
            }
        });
        et_vin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.e("输入前确认执行该方法", "开始输入");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.e("输入过程中执行该方法", "文字变化");
                vin_Changed = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.e("输入结束执行该方法", "输入结束");
            }
        });
        et_devicenum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.e("输入前确认执行该方法", "开始输入");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.e("输入过程中执行该方法", "文字变化");
                devicenum_Changed = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.e("输入结束执行该方法", "输入结束");
            }
        });
        et_ip.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.e("输入前确认执行该方法", "开始输入");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.e("输入过程中执行该方法", "文字变化");
                ip_Changed = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.e("输入结束执行该方法", "输入结束");
            }
        });
        et_port.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.e("输入前确认执行该方法", "开始输入");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.e("输入过程中执行该方法", "文字变化");
                port_Changed = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.e("输入结束执行该方法", "输入结束");
            }
        });
    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        setData();
        super.onViewStateRestored(savedInstanceState);
    }

    private void setData() {
//        ShowData(myapp.getData());
//        et_ID.setText(myapp.getData());
        Handler handler = FragmentTwo.getHandler();
        if (handler != null) {
            handler.sendEmptyMessage(FragmentTwo.MSG_DATA);
        }

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_add:
                ll_ip2.setVisibility(View.VISIBLE);
                break;
            case R.id.img_reduce:
                ll_ip2.setVisibility(View.GONE);
                break;
            case R.id.btn_upgrade:
                ToastFactory.showToast(getActivity(), "暂时未开发，敬请期待");
                break;
            case R.id.btn_set:

                Map<String, Object> map = new HashMap<String, Object>();
                map.clear();
                if (ID_Changed) {
                    if (et_ID.getText().toString().trim().length() > 5) {
                        map.put(IConstants.IDNUM, et_ID.getText().toString().trim());
                        ID_Changed = false;
                    } else {
                        ToastFactory.showToast(getActivity(), "终端号输入不正确！");
                    }
                }
                if (callnum_Changed) {
                    if (et_callnum.getText().toString().trim().length() > 10) {
                        map.put(IConstants.PHONENUM, et_callnum.getText().toString().trim());
                        callnum_Changed = false;
                    } else {
                        ToastFactory.showToast(getActivity(), "手机号输入不正确！");
                    }
                }
                if (vin_Changed) {
                    if (et_vin.getText().toString().trim().length() == 17) {
                        map.put(IConstants.VIN, et_vin.getText().toString().trim());
                        vin_Changed = false;
                    } else {
                        ToastFactory.showToast(getActivity(), "VIN号输入不正确！");

                    }
                }
                if (devicenum_Changed) {
                    if (et_devicenum.getText().toString().trim().length() == 7) {
                        map.put(IConstants.CARNUM, et_devicenum.getText().toString().trim());
                        devicenum_Changed = false;
                    } else {
                        ToastFactory.showToast(getActivity(), "车牌号输入不正确！");
                    }
                }
                if (ip_Changed) {
                    if (et_ip.getText().toString().trim().length() > 1) {
                        map.put(IConstants.IP01, et_ip.getText().toString().trim());
                        ip_Changed = false;
                    } else {
                        ToastFactory.showToast(getActivity(), "ip输入不正确！");
                    }
                }
                if (port_Changed) {
                    if (et_port.getText().toString().trim().length() > 4) {
                        map.put(IConstants.PORT01, et_port.getText().toString().trim());
                        port_Changed = false;
                    } else {
                        ToastFactory.showToast(getActivity(), "端口输入不正确！");
                    }
                }
                String contentdata = null;//获取编辑的数据
                try {
                    contentdata = JSONUtils.getString(map);
                    Log.i(TAG, "JSONUtils.getString(map) = " + JSONUtils.getString(map));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


                if (contentdata.length() > 6) {
                    String SendData = IConstants.SET + //添加起始符设置
                            "00" + ByteUtils.integerToHexString(contentdata.length() / 2) + //数据单元长度
//                        ByteUtils.Decimal0(contentdata.length())+ //
                            contentdata;//数据单元
                    String validate_code = ByteUtils.checkXor(SendData.substring(4, SendData.length()));//验证码   cs
                    SendData += validate_code; //添加验证码 2a2a02FE011301033435360203313335030331323304023738D4
                    /**
                     * 232300 1a 01 0000000000000000000000000000 ff
                     2b2b00 2a2a03FE010801020304050a0f10E000 ff
                     */
                    /**
                     * 起始符 232300
                     * 长度  1a
                     * 包数  01
                     * 补零  000000....
                     * 校验 ff
                     */

                    StringBuilder headInfo = new StringBuilder();
                    headInfo.delete(0, headInfo.length());//删除之前的StringBuilder
                    headInfo.append("232300");
                    headInfo.append(ByteUtils.integerToHexString(SendData.length() / 2));//长度hex
                    headInfo.append(ByteUtils.integerToHexString((int) Math.ceil(SendData.length() / 32.0))); //包数
                    String data = ByteUtils.addZeroForNum(headInfo.toString(), 38);//补零
                    String validate_code1 = ByteUtils.checkXor(headInfo.toString().substring(4, headInfo.toString().length()));//验证码   cs
                    data += validate_code1;

                    /**
                     * 2b2b00 2a2a03FE010801020304050a0f10E000 ff
                     */
                    /**
                     * 起始符 2b2b00
                     * 单元数据2a2a03FE010801020304050a0f10E0
                     * 补零  000000....
                     * 校验 ff
                     */
                    int index = 0;
                    StringBuilder contentInfo = new StringBuilder();
                    for (int i = 0; i < SendData.length(); i = i + 32) {
                        final int finalI = i;
                        if (SendData.length() - i >= 32) {
                            StringBuilder info = new StringBuilder();
                            info.delete(0, info.length());//删除之前的StringBuilder
                            info.append("2b2b0");
                            info.append(index + "");
                            info.append(SendData.substring(finalI, finalI + 32));
                            String validate_code2 = ByteUtils.checkXor(info.toString().substring(4, info.toString().length()));//验证码   cs
                            info.append(validate_code2);
                            index++;
                            contentInfo.append(info.toString());
                            Log.i(TAG, "onClick: OK");
//                        shortOrder[0] = finalHexdata.substring(finalI, finalI + 32);
                        } else {
                            StringBuilder info = new StringBuilder();
                            info.delete(0, info.length());//删除之前的StringBuilder
                            info.append("2b2b0");
                            info.append(index + "");
                            info.append(SendData.substring(finalI, SendData.length()));
                            String data3 = ByteUtils.addZeroForNum(info.toString(), 38);//补零
                            String validate_code3 = ByteUtils.checkXor(info.toString().substring(4, info.toString().length()));//验证码   cs
                            data3 += validate_code3;
                            contentInfo.append(data3);
                            Log.i(TAG, "onClick: OK");
//                        shortOrder[0] = finalHexdata.substring(finalI, finalHexdata.length());
                        }
                    }

                    Log.i(TAG, "onClick: contentInfo最终 = " + contentInfo.toString());
                    data += contentInfo.toString();

                    Log.i(TAG, "onClick: data最终 = " + data);


                    Log.i(TAG, "发送设置数据: SendData = " + SendData);
                    Handler handler = DeviceControlActivity.getHandler();
                    if (handler != null) {
                        Message msg = Message.obtain();
                        msg.what = DeviceControlActivity.MSG_SENDALLORDER;
                        msg.obj = data;
                        handler.sendMessage(msg);
                        dialog.show();
                    }
                } else {
                    ToastFactory.showToast(getActivity(), "数据没有变化");
                }
                Log.i(TAG, "onClick: OK");
                break;
        }
    }
}
