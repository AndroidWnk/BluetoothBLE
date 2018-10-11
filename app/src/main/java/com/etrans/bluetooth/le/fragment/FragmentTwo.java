package com.etrans.bluetooth.le.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
·import android.text.Editable;
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
import com.etrans.bluetooth.le.utils.ByteUtils;
import com.etrans.bluetooth.le.utils.JSONUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 项目名称：蓝牙BLE项目
 * 创建人：王楠魁
 * 创建时间：2018.9.29 10:35
 * 修改备注：
 */
public class FragmentTwo extends Fragment implements View.OnClickListener{
    private static final String TAG = "FragmentTwo";

//    private StringBuilder mOutput = new StringBuilder();
    private Myapplication myapp;
    private EditText et_ID,et_callnum;
    private boolean ID_Changed,callnum_Changed;
    private Button btn_add,btn_reduce,btn_upgrade,btn_set;
    private ImageView img_add,img_reduce;
    private LinearLayout ll_ip1,ll_ip2;

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
        et_callnum = (EditText) view.findViewById(R.id.et_callnum);
        btn_add = view.findViewById(R.id.btn_add);
        img_add = view.findViewById(R.id.img_add);
        btn_reduce = view.findViewById(R.id.btn_reduce);
        img_reduce = view.findViewById(R.id.img_reduce);
        btn_upgrade = view.findViewById(R.id.btn_upgrade);
        btn_set = view.findViewById(R.id.btn_set);
        ll_ip1 = view.findViewById(R.id.ll_ip1);
        ll_ip2 = view.findViewById(R.id.ll_ip2);

        btn_add.setOnClickListener(this);
        btn_reduce.setOnClickListener(this);
        img_add.setOnClickListener(this);
        img_reduce.setOnClickListener(this);
        btn_set.setOnClickListener(this);
        btn_upgrade.setOnClickListener(this);
        ll_ip2.setVisibility(View.GONE);
    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        setData();
        super.onViewStateRestored(savedInstanceState);
    }
    private void setData(){
//        ShowData(myapp.getData());
        et_ID.setText(myapp.getData());

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
    }




    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_add:
                ll_ip2.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_reduce:
                ll_ip2.setVisibility(View.GONE);
                break;
            case R.id.img_add:
                ll_ip2.setVisibility(View.VISIBLE);
                break;
            case R.id.img_reduce:
                ll_ip2.setVisibility(View.GONE);
                break;
            case R.id.btn_upgrade:

                break;
            case R.id.btn_set:

                Map<String, Object> map = new HashMap<String, Object>();
                if(ID_Changed){
                    map.put(IConstants.IDNUM, et_ID.getText().toString().trim());
                }
                if(callnum_Changed){
                    map.put(IConstants.PHONENUM, et_callnum.getText().toString().trim());
                }

                Log.i(TAG, "JSONUtils.getString(map) = "+ JSONUtils.getString(map));

                Log.i(TAG, "onClick: OK");














                //{"vin":"VFGBH12584LKDHF","id":"VERG12345678","callnum":"13725645879"}
//                Map<String, Object> map = new HashMap<String, Object>();
                map.clear();
                map.put("id", "VERG12345678");
                map.put("vin", "VFGBH12584LKDHF");
//                map.put("devicenum", "粤A98764");
                map.put("callnum", "13725645879");
                Log.i(TAG, "JSONUtils.getJSONString(map) = "+ JSONUtils.getJSONString(map));
//                byte[] processStatus = JSONUtils.getJSONString(map).getBytes();
//                String str = new String(processStatus);
                String str = JSONUtils.getJSONString(map);
                str = "**03fe010403010203cs";
                str = "**3fe143123cs";
                String str1 = ByteUtils.checkXor("03fe0103010203");//异或校验 这个值和数据应答回来的值一样则数据正确

                Handler handler = DeviceControlActivity.getHandler();
                if (handler != null) {
                    Message msg = Message.obtain();
                    msg.what = DeviceControlActivity.MSG_SENDALLORDER;
                    msg.obj = str;
                    handler.sendMessage(msg);
                }
                Log.i(TAG, "onClick: OK");
                break;
        }
    }
}
