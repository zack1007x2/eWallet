package com.example.nfcwallet;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.example.nfcwallet.R;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

public class MainMenu extends Activity{
	
	ImageButton btAccount,btCards,btCardStore,btSetting;
	 private SharedPreferences profile,trans,plus;
	 private String ID,PWD,ePWD,client_id;
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);
		
		
		
		
		btAccount = (ImageButton)findViewById(R.id.btAccount);
		btCards = (ImageButton)findViewById(R.id.btCards);
		btCardStore = (ImageButton)findViewById(R.id.btCardStore);
		btSetting = (ImageButton)findViewById(R.id.btSetting);
		
		btAccount.setOnClickListener(btAccountListener);
		btCards.setOnClickListener(btCardsListener);
		btCardStore.setOnClickListener(btCardStoreListener);
		btSetting.setOnClickListener(btSettingListener);

	    

		
	}
	

	private OnClickListener btAccountListener = new OnClickListener() {    
    	public void onClick(View v) {  
    		
    		trans = getSharedPreferences("Trans",0);
		    Editor editor = trans.edit();
    		editor.putString("offer_act_id", null);
    		editor.putString("offer_com_id", null);
    		editor.commit();
    		
    		plus = getSharedPreferences("Plus",0);
		    Editor editorplus = plus.edit();
		    editorplus.putString("offer_act_id", null);
		    editorplus.putString("offer_com_id", null);
		    editorplus.commit();
    		
    		
    		Intent gotoMyAccount = new Intent();
    		gotoMyAccount.setClass(MainMenu.this, CreditSelectorActivity.class);
    		startActivity(gotoMyAccount);
    		
			
    	}
    };
    
    private OnClickListener btCardsListener = new OnClickListener() {    
    	public void onClick(View v) {  
    		Intent gotoMyCard = new Intent();
    		gotoMyCard.setClass(MainMenu.this, CardsView.class);
    		startActivity(gotoMyCard);
    		
			
    	}
    };
    
    private OnClickListener btCardStoreListener = new OnClickListener() {    
    	public void onClick(View v) {  
    		profile = getSharedPreferences("Profile",0);
    		ID = profile.getString("UserID", null);
		    PWD = profile.getString("UserPWD", null);
		    client_id = profile.getString("client_id", null);
		    ePWD = md5(PWD);
    		
    		Uri uri = Uri.parse("http://zack1007x2.com/nfcwallet_demo/store/detail.php?client_id="+client_id+"&password="+ePWD);
			Intent it = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(it);
    		
			
    	}
    };
    
    private OnClickListener btSettingListener = new OnClickListener() {    
    	public void onClick(View v) {
    		//TODO 進入NFCPROXY判定式
    		SharedPreferences prefs = getSharedPreferences(NFCVars.PREFERENCES, Context.MODE_PRIVATE);
			prefs.edit().putBoolean("relayPref", false).commit();
    		
    		Intent gotoSetting = new Intent();
    		gotoSetting.setClass(MainMenu.this, Setting.class);
    		startActivity(gotoSetting);
    		
			
    	}
    };
    
    @Override  
    protected void onResume() {  
        
    	super.onResume();  
        plus = getSharedPreferences("Plus",0);
	    Editor editplus = plus.edit();
	    editplus.putString("offer_act_id",  null);
	    editplus.putString("offer_com_id",  null);
	    editplus.commit(); 
	    
	    
    }  
    
    @Override  
    protected void onRestart() {  
        super.onRestart();  
        profile = getSharedPreferences("Profile",0);
		boolean hasPassCode = profile.getBoolean("hasPassCode", false);
		if(hasPassCode){
        Intent gotoMenu = new Intent();
		gotoMenu.setClass(MainMenu.this, Passcode.class);
		startActivity(gotoMenu);
		}
    }
    
    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

}
