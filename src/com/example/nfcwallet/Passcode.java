package com.example.nfcwallet;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Passcode extends Activity{

	private EditText etPassword;
	private String passcode,confirm;
	private int length;
	private SharedPreferences Profile;
	private Button btconfirm;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.passcode);
		
		etPassword = (EditText)findViewById(R.id.etPassword);
		btconfirm = (Button)findViewById(R.id.btconfirm);
		btconfirm.setOnClickListener(btconfirmListener);
	}
	
	private OnClickListener btconfirmListener = new OnClickListener() {    
    	public void onClick(View v) {  
    		length = etPassword.getText().length();
    		if(length == 4){
    			passcode = etPassword.getText().toString();

				Profile = getSharedPreferences("Profile",0);
				confirm = Profile.getString("PassCode", null);
				if(confirm.equals(passcode)){
					
					
					
					Intent gotoMenu = new Intent();
					gotoMenu.setClass(Passcode.this, MainMenu.class);
		    		startActivity(gotoMenu);
		    		finish();
				}
				else{
					Toast.makeText(getApplicationContext(), "密碼錯誤請重新輸入",Toast.LENGTH_SHORT).show();
					etPassword.setText("");

				}
						
    		}
    		else{
    			Toast.makeText(getApplicationContext(), "請輸入4個數字的密碼",Toast.LENGTH_SHORT).show();
    		}
    		

    		
    	}
    };
    
}
