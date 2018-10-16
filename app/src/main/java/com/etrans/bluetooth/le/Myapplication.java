package com.etrans.bluetooth.le;

import android.app.Application;
import android.os.Handler;

import com.etrans.bluetooth.le.bean.ResultQuerybean;
import com.etrans.bluetooth.le.fragment.FragmentOne;
import com.tencent.bugly.crashreport.CrashReport;

public class Myapplication extends Application {
    public static final int MSG_APP_DATA = 1001;//
    private boolean mConnected = false;
    public static String AES_PASSWROD = "_PASSWROD";//
    public static Handler hand = null;
    public String data;
    ResultQuerybean showdata;
    private String IMEI = "";//设备串号  唯一ID号

    public static Handler getHandler() {
        return hand;
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_APP_DATA://
                    showdata = (ResultQuerybean) msg.obj;
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
        CrashReport.initCrashReport(getApplicationContext(), "fbc9f9ce3b", false);

    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public ResultQuerybean getShowdata() {
        return showdata;
    }

    public void setShowdata(ResultQuerybean showdata) {
        this.showdata = showdata;
    }

    public boolean ismConnected() {
        return mConnected;
    }

    public void setmConnected(boolean mConnected) {
        this.mConnected = mConnected;
    }
}
