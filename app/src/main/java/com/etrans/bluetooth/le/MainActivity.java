package com.etrans.bluetooth.le;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class MainActivity extends Activity {
	private EditText edit_name, edit_pass;
	private CheckBox check_remember, check_automatic;
	private Button button;
	private SharedPreferences sharedPreferences;
	private SharedPreferences.Editor editor;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


	}




}
