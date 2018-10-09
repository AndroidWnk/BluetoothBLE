package com.etrans.bluetooth.le.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.etrans.bluetooth.le.BluetoothLeService;

/**
 * 测试专用
 * adb shell am broadcast -a "android.media.testtesk"
 */
public class XxTestReceiver extends BroadcastReceiver {
    private static final String TAG = "XxTestReceiver";
    public final static String EXTRA_DATA = "com.etrans.bluetooth.le.EXTRA_DATA";
    private StringBuilder mreadInfo = new StringBuilder();
    private String[] strings = new String[]{"2323002103000000000000000000000000000022",
            "2b2b003132333435363738393061626364656606",
            "2b2b016768696a6b6c6d6e6f7071727374750d6a",
            "2b2b020a00000000000000000000000000000008"};
    public XxTestReceiver() {
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.media.testtesk")) { //打开界面
            Intent intent1 = new Intent(BluetoothLeService.ACTION_DATA_AVAILABLE);
            for(String str:strings){
                if(str.indexOf("232300") != -1){
                    mreadInfo.delete(0, mreadInfo.length());//删除之前的StringBuilder
                }

                mreadInfo.append(str).append("\n");//否则直接写
                Log.i(TAG, "stateNKbroadcastUpdate: 接收回来的数据：" + mreadInfo.toString());


                if (str.indexOf("0a") != -1) {
                    intent1.putExtra(EXTRA_DATA, mreadInfo.toString());
                    context.sendBroadcast(intent1);
                }
            }



            //test2

//            XxAudioManager.setCallRing(4);//来电


            //test1
//            if(XxTaskUtil.isTopTask(context, context.getPackageName(), "com.etrans.bluetooth.activity.MainActivity")){
//                Log.i(TAG, "onResume: 在当前界面");
//            }else{
//                Log.i(TAG, "onResume: 不在当前界面");
//            }
//
//            Intent intent0 = new Intent(context, MainActivity.class);
//            intent0.putExtra("is_mode", true);
//            intent0.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);//不会返回两次
//            context.startActivity(intent0);
        }
    }
}
