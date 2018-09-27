package com.etrans.bluetooth.le;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.etrans.bluetooth.le.utils.SpUtil;

public class LoginActivity extends Activity {
    private EditText edit_name, edit_pass;
    private CheckBox check_remember, check_automatic;
    private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //获取资源ID
        edit_name = (EditText) findViewById(R.id.edit_name);
        edit_pass = (EditText) findViewById(R.id.edit_pass);
        check_remember = (CheckBox) findViewById(R.id.check_pass);
        check_automatic = (CheckBox) findViewById(R.id.check_login);
        button = (Button) findViewById(R.id.but_login);
        //4.从SharedPreferences中取出记住密码的状态值
        boolean isremember = SpUtil.getBoolean(this,"isremember",false);
        //5.判断状态值
        if (isremember) {
            //取出账号密码
            String names = SpUtil.getString(this,"name",null);
            String passs = SpUtil.getString(this,"pass",null);
            //设置复选框的状态是勾选的状态
            check_remember.setChecked(true);
            edit_name.setText(names);
            edit_pass.setText(passs);
        }

        //取出自动登录的状态值
        boolean isautomatic = SpUtil.getBoolean(this,"isautomatic",false);
        if (isautomatic) {
            //跳转
            Intent it = new Intent(this, LoginActivity.class);
            startActivity(it);
            //销毁页面
            finish();
        }
        //2.对登录按钮添加监听
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //3.判断记住密码的复选框是否勾选如果勾选就在SharedPreferences中存入账号片密码，状态值
                if (check_remember.isChecked()) {
                    //得到输入框的内容
                    String name = edit_name.getText().toString();
                    String pass = edit_pass.getText().toString();
                    //存入帐号密码
                    SpUtil.putString(LoginActivity.this,"name",name);
                    SpUtil.putString(LoginActivity.this,"pass",pass);
                    //存入状态值，（代表如果已经勾选了，那么就存一个true，的值）
                    SpUtil.putBoolean(LoginActivity.this,"isremember",true);
                }

                //自动登录
                if (check_automatic.isChecked()) {
                    //当勾选了自动登录 存一个为true的状态值
                    SpUtil.putBoolean(LoginActivity.this,"isautomatic",true);
                }

                //跳转
                Intent it = new Intent(LoginActivity.this, DeviceScanActivity.class);
                startActivity(it);
                //销毁页面
                finish();
            }
        });
    }
}
