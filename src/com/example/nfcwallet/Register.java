package com.example.nfcwallet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.nfcwallet.R;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class Register extends Activity {

	private SharedPreferences Profile;
	public EditText etID, etPWD, etCellPhone;
	Button btRegist;
	private String ID,PWD,PhoneNUM,ePWD;
	
	private String client_id,jsonResult;
	
	boolean clear;
	
	private String url = "http://zack1007x2.com/nfcwallet_demo/ewallet/mobilerequest.php";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		
		btRegist = (Button)findViewById(R.id.btRegist);
		etID = (EditText)findViewById(R.id.etID);
		etPWD = (EditText)findViewById(R.id.etPWD);
		etCellPhone = (EditText)findViewById(R.id.etCellPhone);
		
		btRegist.setOnClickListener(btRegistListener);
		
	}
	//ACCESS DB
	private class JsonReadTask extends AsyncTask<String, Void, String> {
		
		
	
		
		@Override
		protected String doInBackground(String... params) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httppost = new HttpGet(params[0]+"?name="+ID+"&password="+ePWD+"&phone="+PhoneNUM+"&BASE64=1");
			System.out.println(params[0]+"?name="+ID+"&password="+ePWD+"&phone="+PhoneNUM+"&BASE64=1");
			clear = false;
			try {
				HttpResponse response = httpclient.execute(httppost);
				String responseTXT = inputStreamToString(response.getEntity().getContent()).toString();
				System.out.println(responseTXT);
				byte[] data = Base64.decode(responseTXT.getBytes(), Base64.DEFAULT);
				jsonResult = new String(data);
				System.out.println(jsonResult);
				if (!jsonResult.equals("REPEAT_NAME")){
					ListDrwaer();
					
					Profile = getSharedPreferences("Profile",0);
					Editor editor = Profile.edit();
		    		editor.putString("UserID",  ID);
		    		editor.putString("UserPWD",  PWD);
		    		editor.putString("UserPhoneNum",  PhoneNUM);
		    		editor.putString("client_id",  client_id);
		    		editor.putBoolean("hasLoggedIn", true);
		    		editor.commit();
					
					
					Intent gotoMenu = new Intent();
					gotoMenu.setClass(Register.this, MainMenu.class);
		    		startActivity(gotoMenu);
		    		finish();
				}
				else{
					clear = true;
					
				}
			}

			catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		private StringBuilder inputStreamToString(InputStream is) {
			String rLine = "";
			StringBuilder answer = new StringBuilder();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));

			try {
				while ((rLine = rd.readLine()) != null) {
					answer.append(rLine);
				}
			}

			catch (IOException e) {
				// e.printStackTrace();
				Toast.makeText(getApplicationContext(),
						"Error..." + e.toString(), Toast.LENGTH_LONG).show();
			}
			return answer;
		}
		

	
	}
	public void ListDrwaer() {

		try {
			JSONObject jsonResponse = new JSONObject(jsonResult);
			
			client_id = jsonResponse.getString("client_id");

		} catch (JSONException e) {
			Toast.makeText(getApplicationContext(), "Error: " + e.toString(),
					Toast.LENGTH_SHORT).show();
		}
	}
	// end async task

	public void accessWebService() {
		JsonReadTask task = new JsonReadTask();
		task.execute(new String[] { url });
		
		
		try{
			task.get();
		}
		catch(Exception e)
		{}
		
		if (clear)
		{
		Toast.makeText(getApplicationContext(), "帳號重複",Toast.LENGTH_SHORT).show();
		etID.setText("");
		etPWD.setText("");
		}
	}


	// build hash set for list view
	
	
	private OnClickListener btRegistListener = new OnClickListener() {    
    	public void onClick(View v) {  
    		
    		
    		
    		ID = etID.getText().toString();
    		PWD = etPWD.getText().toString();
    		ePWD = md5(PWD);
    		PhoneNUM = etCellPhone.getText().toString();
    		//TODO 	1.確認帳號名稱有無重複
    		accessWebService();
    		//		2.上傳新帳戶資料  建立帳戶
    		
    		
    	
    	}
    };
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
