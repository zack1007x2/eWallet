package com.example.nfcwallet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import 	android.util.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class login extends Activity{
	
	private SharedPreferences Profile;
	private String ID,PWD,ePWD;
	private String time;
	private EditText etID, etPWD;
	private Button btToRegist,btLogin;
	private String url = "http://zack1007x2.com/nfcwallet_demo/ewallet/mobilerequest.php";
	private boolean clear;
	private String responseStr,client_id,money;
	
	public void onCreate(Bundle savedInstanceState) {
		
		Profile = getSharedPreferences("Profile",0);
		boolean hasLoggedIn = Profile.getBoolean("hasLoggedIn", false);
		
		if(hasLoggedIn)
		{
			Intent gotoMenu = new Intent();
			gotoMenu.setClass(login.this, MainMenu.class);
    		startActivity(gotoMenu);
    		finish();
		}
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		setTitle("登入");
		
		btToRegist = (Button)findViewById(R.id.btToRegist);
		btLogin = (Button)findViewById(R.id.btLogin);
		etID = (EditText)findViewById(R.id.etID);
		etPWD = (EditText)findViewById(R.id.etPWD);
		
		btToRegist.setOnClickListener(btToRegistListener);
		btLogin.setOnClickListener(btLoginListener);
		
	}
	
	private class JsonReadTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httppost = new HttpGet(params[0]+"?name="+ID +"&epw="+ePWD+"&time="+time+"&BASE64=1");
			System.out.println(params[0]+"?name="+ID +"&epw="+ePWD+"&time="+time+"&BASE64=1");
			clear = false;
			try {
				HttpResponse response = httpclient.execute(httppost);
				String responseTXT = inputStreamToString(response.getEntity().getContent()).toString();
				System.out.println(responseTXT);
				byte[] data = Base64.decode(responseTXT.getBytes(), Base64.DEFAULT);
				responseStr = new String(data);
				
				System.out.println(responseStr);
				if (!responseStr.equals("ERROR")){
					ListDrwaer();
		    		Profile = getSharedPreferences("Profile",0);
				    Editor editor = Profile.edit();
		    		editor.putString("UserID",  ID);
		    		editor.putString("UserPWD",  PWD);
		    		editor.putBoolean("hasLoggedIn", true);
		    		editor.commit();
					
					Intent gotoMenu = new Intent();
					gotoMenu.setClass(login.this, MainMenu.class);
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

		@Override
		protected void onPostExecute(String result) {
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

	
	}// end async task

	public void accessWebService() {
		JsonReadTask task = new JsonReadTask();
		// passes values for the urls string array
		task.execute(new String[] { url });
		
		try{
			task.get();
		}
		catch(Exception e)
		{}
		
		if (clear)
		{
		Toast.makeText(getApplicationContext(), "登入錯誤(密碼錯誤或您尚未註冊此帳號)",Toast.LENGTH_SHORT).show();
		etID.setText("");
		etPWD.setText("");
		}
	}
	
	public void ListDrwaer() {

		try {
			JSONObject jsonResponse = new JSONObject(responseStr);

			client_id = jsonResponse.getString("client_id");
			Profile = getSharedPreferences("Profile",0);
		    Editor editor = Profile.edit();
    		editor.putString("client_id",  client_id);
    		editor.commit();
			
			//System.out.println(client_id+"@"+money);
			
		} catch (JSONException e) {
			Toast.makeText(getApplicationContext(), "Error" + e.toString(),
					Toast.LENGTH_SHORT).show();
		}


	}
	
	private OnClickListener btToRegistListener = new OnClickListener() {    
    	public void onClick(View v) {  
    		Intent gotoregist = new Intent();
    		gotoregist.setClass(login.this, Register.class);
    		startActivity(gotoregist);
    		finish();
    	}
    };
    
	private OnClickListener btLoginListener = new OnClickListener() {    
    	public void onClick(View v) {  
    		
    		
    		SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm:ss");

    		time = sDateFormat.format(new java.util.Date());  
    		System.out.println(time);
    		ID = etID.getText().toString();
    		PWD = etPWD.getText().toString();
    		ePWD = md5(md5(PWD)+time); 
    		
    		accessWebService();    		
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