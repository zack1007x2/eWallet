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

public class PasscodeSetting extends Activity{
	
	private EditText etPassword;
	private String passcode;
	private int length;
	private SharedPreferences Profile;
	private Button btconfirm;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.passcodesetting);
		
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
    		    Editor editor = Profile.edit();
    		    editor.putString("PassCode",  passcode);
    		    editor.commit();
    		    Intent gotoSettine = new Intent();
				gotoSettine.setClass(PasscodeSetting.this, PasscodeConfirm.class);
	    		startActivity(gotoSettine);
    			finish();
    		}
    		else{
    			Toast.makeText(getApplicationContext(), "請輸入4個數字的密碼",Toast.LENGTH_SHORT).show();
    			etPassword.setText("");
    		}
	
    	}
    };
    
    @Override  
    protected void onRestart() {  
        super.onRestart();  
        SharedPreferences profile = getSharedPreferences("Profile",0);
		boolean hasPassCode = profile.getBoolean("hasPassCode", false);
		if(hasPassCode){
        Intent gotoMenu = new Intent();
		gotoMenu.setClass(PasscodeSetting.this, Passcode.class);
		startActivity(gotoMenu);
		}
    }
}
