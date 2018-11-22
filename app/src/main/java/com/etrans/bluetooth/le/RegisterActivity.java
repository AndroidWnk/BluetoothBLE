package com.etrans.bluetooth.le;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.etrans.bluetooth.le.utils.ToastFactory;
import com.etrans.bluetooth.le.utils.demo.AES;

/**
 * 注册界面
 */
public class RegisterActivity extends Activity implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private final static String TAG = "LoginActivity";
    public final static int REQUEST_READ_PHONE_STATE = 1;
    private EditText edit_name, edit_pass;
    private Button btn_login, btn_getname;
    private String IMEI = "";
//    private String AESpasswrod = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //获取资源ID
        edit_name = (EditText) findViewById(R.id.edit_name);
        edit_pass = (EditText) findViewById(R.id.edit_pass);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_getname = (Button) findViewById(R.id.btn_getname);
        btn_login.setOnClickListener(this);
        btn_getname.setOnClickListener(this);
        //动态获取权限
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        } else {
            IMEI = UUIDUtils.getIMEI(this);
            //设置密码
            getpassword(IMEI);
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
                byte[] mBytes = null;
                AES mAes = new AES();
                try {
                    mBytes = name.getBytes("UTF8");
                } catch (Exception e) {
                    Log.i("qing", "MainActivity----catch");
                }
                String enString = mAes.encrypt(mBytes);
                edit_pass.setText(enString.substring(0,5));
                String deString = mAes.decrypt(enString);

                Log.i(TAG, "AESKey: 加密前 = "+deString+",加密后 = "+enString);

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

}
