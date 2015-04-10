package com.example.nfcwallet;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class Setting extends Activity{
	
	private SharedPreferences Profile;
	
	private ListView lvSetting;
	String[] study= new String[]{"匯入信用卡","密碼設定", "密碼清除", "登出"};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.setting);
	
	
	lvSetting = (ListView)findViewById(R.id.lvSetting);
	ArrayAdapter<String> studyadapter= new  ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,study);
	lvSetting.setAdapter(studyadapter);
	lvSetting.setOnItemClickListener(listener);

	}
	private ListView.OnItemClickListener listener = new ListView.OnItemClickListener(){
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
	
			switch(arg2){
		    case 0 :
		    	Intent gotoImport = new Intent();
		    	gotoImport.setClass(Setting.this, SelectorActivity.class);
	    		startActivity(gotoImport);
		                      
		      break;
		  
		    case 1 : 
		    	Intent gotoPasscode = new Intent();
		    	gotoPasscode.setClass(Setting.this, PasscodeSetting.class);
	    		startActivity(gotoPasscode);
		        finish();
		      break;
		      
		    case 2 : 
		    	Profile = getSharedPreferences("Profile",0);
			    Editor CodeEditor = Profile.edit();
			    CodeEditor.putString("PassCode",  null);
			    CodeEditor.putBoolean("hasPassCode", false);
			    CodeEditor.commit();
			    Toast.makeText(getApplicationContext(), "密碼已清除",Toast.LENGTH_SHORT).show();
		      break;
		  
		    case 3 : 
		    	Profile = getSharedPreferences("Profile",0);
			    Editor editor = Profile.edit();
	    		editor.putString("UserID",  null);
	    		editor.putString("UserPWD",  null);
	    		editor.putString("UserPhoneNum",  null);
	    		editor.putString("client_id",  null);
	    		editor.putBoolean("hasLoggedIn", false);
	    		editor.putBoolean("hasPassCode", false);
	    		editor.commit();
		    	
		    	Intent logout = new Intent();
		    	logout.setClass(Setting.this, login.class);
	    		startActivity(logout);   
		      break;
		      
         
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
		gotoMenu.setClass(Setting.this, Passcode.class);
		startActivity(gotoMenu);
		}
    }

}
