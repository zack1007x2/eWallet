package com.example.nfcwallet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.nfcwallet.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore.Images;
import android.util.Base64;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class CardsView extends Activity implements CreateNdefMessageCallback, OnNdefPushCompleteCallback{

	NfcAdapter mNfcAdapter;
	private String url = "http://zack1007x2.com/nfcwallet_demo/ewallet/mobilerequest.php";
	private String jsonResult;
	Vector cardinfo = new Vector();
	Bundle inform;
	private SharedPreferences plus;
	
	private SharedPreferences profile;
	private String ID,client_id,PWD,ePWD,time;
	private String offer_act_id, offer_com_id;
	SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm:ss");

	

	AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			CardInfo ci = (CardInfo)cardinfo.elementAt(position);
//			System.out.println( ci.info );
			inform = new Bundle();
			inform.putString("info", ci.info );
			inform.putString("date", ci.date );
			inform.putString("act_id", ci.act_id );
			inform.putString("com_id", ci.com_id );
			
			CardDialogFragment cdf = CardDialogFragment.newInstance();
			cdf.setArguments(inform);
			cdf.show(getFragmentManager(), "carddialog");
			// TODO 有效日期
		}
	};
	
	class CardInfo
	{
		public int img;
		public String date;
		public String info;
		public String act_id,com_id;
		
		CardInfo(int i, String d, String in, String act, String com)
		{
			img = i;
			date = d;
			info = in;
			act_id = act;
			com_id = com;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cardview);

		
		accessWebService();


		 	profile = getSharedPreferences("Profile",0);
			ID = profile.getString("UserID", "");
			PWD = profile.getString("UserPWD", "");
			client_id = profile.getString("client_id", "");
			
			
			
			
			
			
			mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
			if (mNfcAdapter == null) {
				Toast.makeText(getApplicationContext(),
						"NFC is not available on this device.", Toast.LENGTH_LONG).show();
			}
			
			
			
			// Register callback to set NDEF message
			mNfcAdapter.setNdefPushMessageCallback(this, this);
			// Register callback to listen for message-sent success
			mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
			
	}

	@Override
	public void onResume() {
		super.onResume();
				
		// Check to see that the Activity started due to an Android Beam
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			processIntent(getIntent());
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				Toast.makeText(getApplicationContext(), "Message sent!",
						Toast.LENGTH_LONG).show();
				break;
			}
		}
	};

	@Override
	public void onNdefPushComplete(NfcEvent arg0) {
		// A handler is needed to send messages to the activity when this
		// callback occurs, because it happens from a binder thread
		mHandler.obtainMessage(1).sendToTarget();
	}

	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		
		plus = getSharedPreferences("Plus",0);
		offer_act_id = plus.getString("offer_act_id", null);
		offer_com_id = plus.getString("offer_com_id", null);
		time = sDateFormat.format(new java.util.Date());
		ePWD = md5(md5(PWD)+time);
		String text = client_id+"@"+ePWD+"@"+time+"@false@"+offer_com_id+"@"+offer_act_id;
		
		byte[] data = text.getBytes();
		String base64 = Base64.encodeToString(data, Base64.DEFAULT);
		NdefMessage msg = new NdefMessage(new NdefRecord[] { createMimeRecord(
				"FJU_A7_NFCWallet@", base64.getBytes()) });
		
		return msg;
	}

	public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
		byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
		NdefRecord mimeRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
				mimeBytes, new byte[0], payload);
		return mimeRecord;
	}

	void processIntent(Intent intent) {
		Parcelable[] rawMsgs = intent
				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		// only one message sent
		NdefMessage msg = (NdefMessage) rawMsgs[0];
		// record 0 contains the MIME type, record 1 is the AAR, if present
		Toast.makeText(getApplicationContext(),
					new String(msg.getRecords()[0].getPayload()), Toast.LENGTH_LONG).show();
	}

	
	//HTTP
	
	// Async Task to access the web
		private class JsonReadTask extends AsyncTask<String, Void, String> {
			@Override
			protected String doInBackground(String... params) {
				SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm:ss");
	    		time = sDateFormat.format(new java.util.Date());
	    		ePWD = md5(md5(PWD)+time);
				
				
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(params[0]+"?client_id="+client_id+"&epw="+ePWD+"&time="+time+"&act_id=ALL"+"&BASE64=1");
//				System.out.println(params[0]+"?client_id="+client_id+"&epw="+ePWD+"&time="+time+"&act_id=ALL"+"&BASE64=1");
				try {
					HttpResponse response = httpclient.execute(httppost);
					String responseTXT = inputStreamToString(response.getEntity().getContent()).toString();
					System.out.println(responseTXT);
					byte[] data = Base64.decode(responseTXT.getBytes(), Base64.DEFAULT);
					jsonResult = new String(data);
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

			@Override
			protected void onPostExecute(String result) {
				ListDrwaer();
			}
		}// end async task

		public void accessWebService() {
			JsonReadTask task = new JsonReadTask();
			// passes values for the urls string array
			task.execute(new String[] { url });
		}

		// build hash set for list view
		public void ListDrwaer() {
			cardinfo.clear();
			try {
				JSONObject jsonResponse = new JSONObject(jsonResult);
				JSONArray jsonMainNode = jsonResponse.optJSONArray("activity");
				for (int i = 0; i < jsonMainNode.length(); i++) {
					JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
					int recharge = jsonChildNode.optInt("recharge");
					if (recharge == 0){
						String act_id = jsonChildNode.optString("act_id");
						String com_id = jsonChildNode.optString("com_id");
						String name = jsonChildNode.optString("name");
						String infor = jsonChildNode.optString("infor");
						String endtime = jsonChildNode.optString("endtime");
						JSONArray jsonCard = jsonResponse.optJSONArray("act_client");
						cardinfo.add( new CardInfo (getResources().getIdentifier("img"+com_id+act_id, "drawable", getPackageName() ),
								 endtime, infor, act_id, com_id) );

//						for (int j = 0; j < jsonCard.length(); j++) {
//							JSONObject jsonChildCard = jsonCard.getJSONObject(j);
//							String check_act_id = jsonChildCard.optString("act_id");
//							if(check_act_id.equals(act_id)){
//								
//								
//								String endtime = jsonChildCard.optString("endtime");
//								
//
//								//System.out.println("img"+com_id+act_id +"@"+getPackageName());
//								
//
//							}
//						}
					
						
					}
				}
				
				int[] images = new int[cardinfo.size()];
				for (int i = 0; i<cardinfo.size(); i++)
					images[i] = ((CardInfo)cardinfo.elementAt(i)).img;
				
				ImageAdapter adapter = new ImageAdapter(this, images);
				adapter.createReflectedImages();

				GalleryFlow galleryFlow = (GalleryFlow) findViewById(R.id.gallery_flow);
				galleryFlow.setAdapter(adapter);
				galleryFlow.setOnItemClickListener(listener);

			} catch (JSONException e) {
				Toast.makeText(getApplicationContext(), "Error" + e.toString(),
						Toast.LENGTH_SHORT).show();
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
	    
	    @Override  
	    protected void onRestart() {  
	        super.onRestart();  
	        profile = getSharedPreferences("Profile",0);
			boolean hasPassCode = profile.getBoolean("hasPassCode", false);
			if(hasPassCode){
	        Intent gotoMenu = new Intent();
			gotoMenu.setClass(CardsView.this, Passcode.class);
    		startActivity(gotoMenu);
			}
	    }  
	    
	     
	

}
