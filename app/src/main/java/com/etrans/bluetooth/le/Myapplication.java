package com.etrans.bluetooth.le;

import android.app.Application;
import android.os.Handler;

import com.etrans.bluetooth.le.bean.Resultbean;
import com.etrans.bluetooth.le.fragment.FragmentOne;

public class Myapplication extends Application {
    public static final int MSG_APP_DATA = 1001;//
    public static String AES_PASSWROD = "_PASSWROD";//
    public static Handler hand = null;
    public String data;
    Resultbean showdata;
    private String IMEI = "";//设备串号  唯一ID号

    public static Handler getHandler() {
        return hand;
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_APP_DATA:// 蓝牙设备名称
                    showdata = (Resultbean) msg.obj;
                    Handler handler = FragmentOne.getHandler();
                    if (handler != null) {
                        handler.sendEmptyMessage(FragmentOne.MSG_DATA);
                    }
                    break;

            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        hand = handler;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Resultbean getShowdata() {
        return showdata;
    }

    public void setShowdata(Resultbean showdata) {
        this.showdata = showdata;
    }
}
