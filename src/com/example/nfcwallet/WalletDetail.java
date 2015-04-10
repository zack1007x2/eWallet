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

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.nfcwallet.RefreshableView.PullToRefreshListener;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class WalletDetail extends Activity implements CreateNdefMessageCallback, OnNdefPushCompleteCallback{
	
	private TextView Balance,CardName;
	private ListView ExpenseList;
	private String accountname,money;
	NfcAdapter mNfcAdapter;
	TextView mInfoText;
	private SharedPreferences profile,trans;
	private String ID,client_id,PWD,ePWD,time,walletmoney;
	private String act_id,com_id, offer_act_id, offer_com_id;
	
	private String jsonResult;
	private String url = "http://zack1007x2.com/nfcwallet_demo/ewallet/mobilerequest.php";
	Vector Recieptinfo = new Vector();
	private RefreshableView refreshableView;
	SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm:ss");
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cash_account);

		
		
		ExpenseList = (ListView) findViewById(R.id.ExpenseList);
		Balance		= (TextView) findViewById(R.id.ShowBalance);
		CardName	= (TextView) findViewById(R.id.tvCardName);
		refreshableView = (RefreshableView) findViewById(R.id.refreshable_view);
		
		profile = getSharedPreferences("Profile",0);
		ID = profile.getString("UserID", null);
		PWD = md5(profile.getString("UserPWD", null));
		client_id = profile.getString("client_id", null);
		

		
		
		Bundle SelectedAccount = this.getIntent().getExtras();
		accountname = SelectedAccount.getString("account");
		//money = SelectedAccount.getString("money");
		offer_act_id = SelectedAccount.getString("offer_act_id");
		offer_com_id = SelectedAccount.getString("offer_com_id");
		System.out.println("!!!!!!!!!!!!!!!!!!!!"+money);
		
		
		
		CardName.setText(accountname);
		//Balance.setText(money);
		
		
		accessWebService();
		
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter == null) {
			mInfoText.setText("NFC is not available on this device.");
		}

		// Register callback to set NDEF message
		mNfcAdapter.setNdefPushMessageCallback(this, this);
		// Register callback to listen for message-sent success
		mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
		
		refreshableView.setOnRefreshListener(new PullToRefreshListener() {
			@Override
			public void onRefresh() {
				accessWebService();
				
				try {
					Thread.sleep(3000);
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				refreshableView.finishRefreshing();
			}
		}, 0);


	}
	
	public void onResume() {
		super.onResume();
		// Check to see that the Activity started due to an Android Beam
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			processIntent(getIntent());
		}

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
		
		time = sDateFormat.format(new java.util.Date());
		ePWD = md5(PWD+time);
		
		String text = client_id+"@"+ePWD+"@"+time+"@Wallet";
		if (offer_act_id!=null && offer_com_id!=null){
			text = client_id+"@"+ePWD+"@"+time+"@Wallet@"+offer_com_id+"@"+offer_act_id;
		}
		
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
		mInfoText.setText(new String(msg.getRecords()[0].getPayload()));
	}
	
	//HTTP
	
	private class JsonReadTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			
			
    		time = sDateFormat.format(new java.util.Date());
    		ePWD = md5(PWD+time);
			
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(params[0]+"?record_client_id="+client_id+"&epw="+ePWD+"&time="+time+"&record_id=ALL"+"&BASE64=1");
			System.out.println(params[0]+"?record_client_id="+client_id+"&epw="+ePWD+"&time="+time+"&record_id=ALL"+"&BASE64=1");
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
			if(!jsonResult.equals("EMPTY")){
				ListDrwaer();
			}
			else{
				Toast.makeText(getApplicationContext(),
						"無消費記錄", Toast.LENGTH_LONG).show();
			}
		}
	}// end async task

	public void accessWebService() {
		JsonReadTask task = new JsonReadTask();
		// passes values for the urls string array
		task.execute(new String[] { url });
	}

	// build hash set for list view
	public void ListDrwaer() {
		List<Map<String, String>> AccountList = new ArrayList<Map<String, String>>();
		Recieptinfo.clear();
		try {
			JSONObject jsonResponse = new JSONObject(jsonResult);
			walletmoney = jsonResponse.getString("money");
			Balance.setText(walletmoney);
			JSONArray jsonMainNode = jsonResponse.optJSONArray("record");
			for (int i = 0; i < jsonMainNode.length(); i++) {
				JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
				String record_com_id = jsonChildNode.optString("com_id");
				String record_id = jsonChildNode.optString("record_id");
				String ter_name = jsonChildNode.optString("ter_name");
				String ter_address = jsonChildNode.optString("ter_address");
				JSONArray jsonCard = jsonResponse.optJSONArray("record_product");
				for (int j = 0; j < jsonCard.length(); j++) {
					JSONObject jsonChildCard = jsonCard.getJSONObject(j);
					String record_idaa = jsonChildCard.optString("record_id");
					if(record_idaa.equals(record_id)){
						String name = jsonChildCard.optString("name");
						String price = jsonChildCard.optString("money");
						String record_act_id = jsonChildCard.optString("act_id");
						
						String outPut = "門市 :"+ter_name+" 消費金額:"+price;
						System.out.println(name+"@"+ter_name+"@"+ter_address+"@"+price);		
						AccountList.add(createAccount("Account",outPut));
						Recieptinfo.add(name+"@"+ter_name+"@"+ter_address+"@"+price);
						
					}
				}
			
					
				
			}
		} catch (JSONException e) {
			Toast.makeText(getApplicationContext(), "Error" + e.toString(),
					Toast.LENGTH_SHORT).show();
		}

		SimpleAdapter simpleAdapter = new SimpleAdapter(this, AccountList,
				android.R.layout.simple_list_item_1,
				new String[] { "Account" }, new int[] { android.R.id.text1 });
		ExpenseList.setAdapter(simpleAdapter);
		ExpenseList.setOnItemClickListener(lvlistener);
	}
	private HashMap<String, String> createAccount(String name, String number) {
		HashMap<String, String> AccountNameDeadLine = new HashMap<String, String>();
		AccountNameDeadLine.put(name, number);
		return AccountNameDeadLine;
	}
	
	private OnItemClickListener lvlistener  = new OnItemClickListener(){
 		@Override
 		public void onItemClick(AdapterView<?> arg0, View view, int position,
 				long id) {
 			String Selected = (String)Recieptinfo.elementAt(position);

 			String[] fields = Selected.split("@");
 			
 			
 			Bundle reciept = new Bundle();
 			reciept.putString("name",	fields[0]);
 			reciept.putString("ter_name", fields[1]);
 			reciept.putString("ter_address", fields[2] );
 			reciept.putString("price", fields[3] );
			
 			recieptDialog recieptf  = recieptDialog.newInstance();
 			recieptf.setArguments(reciept);
 			recieptf.show(getFragmentManager(), "recieptdialog");
 				
 			


 			
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
	
	@Override  
    protected void onRestart() {  
        super.onRestart();  
        profile = getSharedPreferences("Profile",0);
		boolean hasPassCode = profile.getBoolean("hasPassCode", false);
		if(hasPassCode){
        Intent gotoMenu = new Intent();
		gotoMenu.setClass(WalletDetail.this, Passcode.class);
		startActivity(gotoMenu);
		}
    }

}
