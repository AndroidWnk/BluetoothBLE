package com.etrans.bluetooth.le;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.etrans.bluetooth.le.utils.SpUtil;
import com.etrans.bluetooth.le.utils.ToastFactory;
import com.etrans.bluetooth.le.utils.demo.AES;

public class LoginActivity extends Activity implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private final static String TAG = "LoginActivity";
    public final static int REQUEST_READ_PHONE_STATE = 1;
    private EditText edit_name, edit_pass;
    private CheckBox check_remember, check_automatic;
    private Button btn_login, btn_getname;
    private ImageView img_logo;
    private String IMEI = "";
//    private String AESpasswrod = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //获取资源ID
        edit_name = (EditText) findViewById(R.id.edit_name);
        edit_pass = (EditText) findViewById(R.id.edit_pass);
        check_remember = (CheckBox) findViewById(R.id.check_pass);
        check_automatic = (CheckBox) findViewById(R.id.check_login);
        btn_login = (Button) findViewById(R.id.btn_login);
        img_logo = (ImageView) findViewById(R.id.img_logo);
        btn_getname = (Button) findViewById(R.id.btn_getname);
        btn_login.setOnClickListener(this);
        btn_getname.setOnClickListener(this);
        img_logo.setOnClickListener(this);
        //动态获取权限
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        } else {
            IMEI = UUIDUtils.getIMEI(this);
            //设置密码
            getpassword(IMEI);
        }
        //******************
        //4.从SharedPreferences中取出记住密码的状态值
        boolean isremember = SpUtil.getBoolean(this, "isremember", false);
        //5.判断状态值 //记住密码
        if (isremember) {
            //取出账号密码
            String names = SpUtil.getString(this, "name", null);//这个是之前保存好的IMEI
            String passs = SpUtil.getString(this, "pass", null);
            //设置复选框的状态是勾选的状态
            check_remember.setChecked(true);
            edit_name.setText(names);
            edit_pass.setText(passs);
        }

        //取出自动登录的状态值
        boolean isautomatic = SpUtil.getBoolean(this, "isautomatic", false);
        if (isautomatic) {
            //跳转
            Intent it = new Intent(this, DeviceControlActivity.class);
            startActivity(it);
            //销毁页面
            finish();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_getname:
                edit_name.setText(IMEI);
                break;
            //2.对登录按钮添加监听
            case R.id.btn_login:
                //得到输入框的内容
                String name = edit_name.getText().toString();
                String pass = edit_pass.getText().toString();
                if (name.equals("")) {
                    ToastFactory.showToast(this, "请输入账号");
                    return;
                }
                if (pass.equals("")) {
                    ToastFactory.showToast(this, "请输入密码");
                    return;
                }
                else if(!pass.equals(Myapplication.AES_PASSWROD.substring(0, 5))){
                    ToastFactory.showToast(this, "密码错误！");
                    return;
                }

//                else if (!pass.equals(IMEI.substring(0, 5))) {  //输入错误
//                    ToastFactory.showToast(this, "密码错误！");
//                    return;
//                }
                //3.判断记住密码的复选框是否勾选如果勾选就在SharedPreferences中存入账号片密码，状态值
                if (check_remember.isChecked()) {
                    //存入帐号密码
                    SpUtil.putString(LoginActivity.this, "name", name);
                    SpUtil.putString(LoginActivity.this, "pass", pass);
                    //存入状态值，（代表如果已经勾选了，那么就存一个true，的值）
                    SpUtil.putBoolean(LoginActivity.this, "isremember", true);
                }

                //自动登录
                if (check_automatic.isChecked()) {
                    //当勾选了自动登录 存一个为true的状态值
                    SpUtil.putBoolean(LoginActivity.this, "isautomatic", true);
                }

                //跳转
                Intent it = new Intent(LoginActivity.this, DeviceControlActivity.class);
                startActivity(it);
                //销毁页面
                finish();
                break;
            case R.id.img_logo:

                onDisplaySettingButton();

                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    IMEI = UUIDUtils.getIMEI(this);
                    //设置密码
                    getpassword(IMEI);
                }
                break;

            default:
                break;
        }
    }
    private void getpassword(String Imei)  {
        byte[] mBytes = null;
        AES mAes = new AES();
        try {
            mBytes = Imei.getBytes("UTF8");
        } catch (Exception e) {
            Log.i("qing", "MainActivity----catch");
        }
        Myapplication.AES_PASSWROD = mAes.encrypt(mBytes);
//        textView1.setText("加密后：" + enString);
        String deString = mAes.decrypt(Myapplication.AES_PASSWROD);
//        textView2.setText("解密后：" + deString);

        Log.i(TAG, "AESKey: 加密前 = "+deString+",加密后 = "+Myapplication.AES_PASSWROD);


        //加密
//        try {
////            AESpasswrod = AESCipher.aesEncryptString(Imei, "16BytesLengthKey");
//            Myapplication.AES_PASSWROD = AESCipher.aesEncryptString(Imei, "16BytesLengthKey");
//        } catch (InvalidKeyException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (NoSuchPaddingException e) {
//            e.printStackTrace();
//        } catch (InvalidAlgorithmParameterException e) {
//            e.printStackTrace();
//        } catch (IllegalBlockSizeException e) {
//            e.printStackTrace();
//        } catch (BadPaddingException e) {
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        Log.i(TAG, "AESKey: 加密前 = "+Imei+",加密后 = "+Myapplication.AES_PASSWROD);
    }



    //进入秘密通道
    // 需要点击几次 就设置几
    long [] mHits = null;
    public void onDisplaySettingButton() {
        if (mHits == null) {
            mHits = new long[8];
        }
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);//把从第二位至最后一位之间的数字复制到第一位至倒数第一位
        mHits[mHits.length - 1] = SystemClock.uptimeMillis();//记录一个时间
        if (SystemClock.uptimeMillis() - mHits[0] <= 1000) {//一秒内连续点击。
            mHits = null;	//这里说明一下，我们在进来以后需要还原状态，否则如果点击过快，第六次，第七次 都会不断进来触发该效果。重新开始计数即可

            //跳转
            Intent it = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(it);


//            if (mShow) {
//           //这里是你具体的操作
//                mShow = false;
//            } else {
//           //这里也是你具体的操作
//                mShow = true;
//            }
//            //这里一般会把mShow存储到sp中。
        }
    }

}
