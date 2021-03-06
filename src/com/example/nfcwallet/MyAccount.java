package com.example.nfcwallet;


import com.example.nfcwallet.NFCVars;
import com.example.nfcwallet.R;
import com.example.nfcwallet.utils.BasicTagTechnologyWrapper;
import com.example.nfcwallet.utils.CryptoHelper;
import com.example.nfcwallet.utils.IOUtils;
import com.example.nfcwallet.utils.LogHelper;
import com.example.nfcwallet.utils.TagHelper;
import com.example.nfcwallet.utils.TextHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.util.Base64;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

@SuppressLint("NewApi")
public class MyAccount extends Activity {
    
	private static final int PROXY_MODE = 0;
	private static final int REPLAY_PCD_MODE = 1;
	private static final int REPLAY_TAG_MODE = 2;				
	private final int CONNECT_TIMEOUT = 5000;
	private final String DEFAULT_SALT = "kAD/gd6tvu8=";
	
	
	private ScrollView mStatusTab;
	private TextView mStatusView;
	private TextView mDataView;
	private TextView CardName,tvMoney;
	private ScrollView mDataTab;
	private TableLayout mDataTable;	
	private TabHost mTabHost;
	private ListView mSavedList;
	private ListView mAccountList;
	private Menu mOptionsMenu;
	private ActionMode mActionMode;
	
	private InetSocketAddress mSockAddr;
	private DBHelper mDBHelper;
	private SecretKey mSecret = null;
	private String mSalt = null;
		
	private View mSelectedSaveView;
	private int mSelectedId = 0;		
	private Bundle mSessions = new Bundle();
	private Bundle mReplaySession;	
	private Bundle activity_id;
	
	private WakeLock mWakeLock;
	private int mMode = PROXY_MODE;
	private boolean LONGCLICK = false;
	
	private boolean mDebugLogging = false;
	private int mServerPort;
	private String mServerIP;
	private boolean mEncrypt = true;
	private boolean mMask = false;
	
	private String jsonResult,walletResult;
	private String url = "http://zack1007x2.com/nfcwallet_demo/ewallet/mobilerequest.php";
	private String act_id,com_id,client_id,walletmoney;
	private String offer_act_id,offer_com_id;
	private String ID,ePWD,PWD,time;
	private SharedPreferences trans,profile,wallet;
	
	Vector Accountinfo = new Vector();
	Vector cardinfo = new Vector();
	
	private ActionMode.Callback mTransactionsActionModeCallback = new ActionMode.Callback() {

	    @Override
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	        MenuInflater inflater = mode.getMenuInflater();
	        inflater.inflate(R.menu.data_context_menu, menu);
	        return true;
	    }

	    @Override
	    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	        return false; // Return false if nothing is done
	    }

	    @Override
	    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	    	mReplaySession = mSessions.getBundle(String.valueOf(mSelectedId));
	        switch (item.getItemId()) {
//	            case R.id.replayPcd:
//					enablePCDReplay();
//	                mode.finish();
//	                return true;
//	            case R.id.replayTag:
//	            	enableTagReplay();
//	                mode.finish();
//	                return true;	       
	            case R.id.delete:
					deleteRun();
					mode.finish();									
	            	return true;
	            case R.id.save:
	            	SaveDialogFragment.newInstance().show(getFragmentManager(), "savedialog");
	            	mode.finish();
	            	return true;	            	
//	            case R.id.export:
//	            	ExportDialogFragment.newInstance().show(getFragmentManager(), "exportdialog");
//	            	mode.finish();
//	            	return true;
	            default:
	                return false;
	        }
	    }

	    @Override
	    public void onDestroyActionMode(ActionMode mode) {
	        mActionMode = null;
	    }
	};		

	private ActionMode.Callback mSavedActionModeCallback = new ActionMode.Callback() {

	    @Override
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	        MenuInflater inflater = mode.getMenuInflater();
	        inflater.inflate(R.menu.saved_context_menu, menu);
	        return true;
	    }

	    @Override
	    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	        return false; // Return false if nothing is done
	    }

	    @Override
	    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	        switch (item.getItemId()) {
	            case R.id.deleteSaved:
					deleteSaved();
					mode.finish();									
	            	return true;
	            default:
	                return false;
	        }
	    }

	    @Override
	    public void onDestroyActionMode(ActionMode mode) {
	        mActionMode = null;
	    }
	};		
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    mOptionsMenu = menu;	    
        if (mMode == PROXY_MODE) {
        	mOptionsMenu.getItem(0).setVisible(false);
	        mOptionsMenu.getItem(1).setVisible(false);
	        mOptionsMenu.getItem(2).setVisible(false);        
        }
        else if (mMode == REPLAY_PCD_MODE) {
        	mOptionsMenu.getItem(0).setVisible(false);
	        mOptionsMenu.getItem(1).setVisible(true);
	        mOptionsMenu.getItem(2).setVisible(true);        	
        }
        else if (mMode == REPLAY_TAG_MODE) {
        	mOptionsMenu.getItem(0).setVisible(true);
	        mOptionsMenu.getItem(1).setVisible(false);
	        mOptionsMenu.getItem(2).setVisible(true);        	        	
        }	    
	    return true;
	}	
	
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//    	switch(item.getItemId()) {
//    		case R.id.settingsButton:
//    			if (Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB) {
//					Toast.makeText(this, "settings", Toast.LENGTH_SHORT).show();    				
//    			}
//    			else {
//    				startActivity(new Intent(this, SettingsActivity.class));
//    			}
//    			return true;
//    		case R.id.proxyButton:
//    	        mMode = PROXY_MODE;
//    	        mOptionsMenu.getItem(0).setVisible(false);
//    	        mOptionsMenu.getItem(1).setVisible(false);
//    	        mOptionsMenu.getItem(2).setVisible(false);
//    	        Toast.makeText(this,getString(R.string.switching_to_proxy_mode), Toast.LENGTH_LONG).show();        
//    			return true;
//			default:
//				return false;    		
//    	} 
//    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	SharedPreferences prefs = getSharedPreferences(NFCVars.PREFERENCES, Context.MODE_PRIVATE);
		prefs.edit().putBoolean("relayPref", false).commit();
		
    	log("onCreate start");    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account);
        getActionBar().setDisplayShowHomeEnabled(false);
        
        
        trans = getSharedPreferences("Trans", 0);
        offer_act_id = trans.getString("offer_act_id", null);
        offer_com_id = trans.getString("offer_com_id", null);
        profile = getSharedPreferences("Profile", 0);
        PWD = profile.getString("UserPWD", null);
        client_id = profile.getString("client_id", null);
        ID = profile.getString("UserID", null);
        

        tvMoney = (TextView) findViewById(R.id.tvMoney);
        CardName = (TextView) findViewById(R.id.tvCardName);
        mStatusView = (TextView) findViewById(R.id.statusView);
        mStatusTab = (ScrollView) findViewById(R.id.statusTab);

        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();
        
        mTabHost.addTab(mTabHost.newTabSpec("account_tab").setContent(R.id.accountTab).setIndicator(getString(R.string.myAccount)));
        mTabHost.addTab(mTabHost.newTabSpec("saved_tab").setContent(R.id.saveTab).setIndicator(getString(R.string.saved_Credit)));
        mTabHost.addTab(mTabHost.newTabSpec("data_tab").setContent(R.id.dataTab).setIndicator(getString(R.string.pay)));
       // mTabHost.addTab(mTabHost.newTabSpec("status_tab").setContent(R.id.statusTab).setIndicator(getString(R.string.status)));        
        
        
        mDataTab = (ScrollView) findViewById(R.id.dataTab);
        mDataTable = (TableLayout) findViewById(R.id.dataTable);
          
        //mAccountList = (ListView) findViewById(R.id.AccountView);

        mSavedList = (ListView) findViewById(R.id.savedListView);
        mDBHelper = new DBHelper(this);
        
        final int MasterCard = R.drawable.master;
        final int Visa = R.drawable.visa;
        final int AmericanExpress = R.drawable.ame;
        final int Discover = R.drawable.jcb;
        
        
        
        
        mSavedList.setAdapter(new CursorAdapter(this, mDBHelper.getNEWReplays(), 0) {

			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				TextView saveView = (TextView)view.findViewById(R.id.savedTextView);
				ImageView initCard = (ImageView)view.findViewById(R.id.imageView1);
				saveView.setOnLongClickListener(getSavedTextViewLongClickListener());
				int nameIdx = cursor.getColumnIndex("name");
				String name = cursor.getString(nameIdx);
				byte[] tBytes = cursor.getBlob(cursor.getColumnIndex("transactions"));
				byte[][] transactions = null;
				ByteArrayInputStream bais = new ByteArrayInputStream(tBytes);
				try {
					ObjectInputStream ois = new ObjectInputStream(bais);
					transactions = (byte[][])ois.readObject();
				} catch (StreamCorruptedException e) {
					log(e);
				} catch (IOException e) {
					log(e);
				} catch (ClassNotFoundException e) {
					log(e);
				}
				
				Bundle tag = new Bundle();
				tag.putSerializable("transactions", transactions);				
				tag.putString("name", name);
				saveView.setTag(tag);
				
				if(name.equals("MasterCard")){
					initCard.setImageResource(MasterCard);
				}
				else if(name.equals("AmericanExpress")){
					initCard.setImageResource(AmericanExpress);
				}
				else if(name.equals("Visa")){
					initCard.setImageResource(Visa);
				}
				else if(name.equals("Discover")){
					initCard.setImageResource(Discover);
				}
				
				saveView.setText(getString(R.string.name) + ": " + name + "\n");
				int type = cursor.getInt(cursor.getColumnIndex("type"));				
//				if (type == DBHelper.REPLAY_PCD) {
//					//saveView.append(getString(R.string.type_pcd));
//					saveView.setOnClickListener(new OnClickListener() {
//						@Override
//						public void onClick(View v) {
//							Bundle t = (Bundle)v.getTag();
//							byte[][] tr = (byte[][])t.getSerializable("transactions");
//							Bundle reqs = new Bundle(); 
//							for (int i = 0; i < tr.length; i ++) {
//								reqs.putByteArray(String.valueOf(i), tr[i]);
//							}
//							Bundle b = new Bundle();
//							b.putBundle("requests", reqs);
//							mReplaySession = b;
//							enablePCDReplay();
//						}					
//					});									
//				}
//				else 
					if (type == DBHelper.REPLAY_TAG) {
					//saveView.append(getString(R.string.type_tag));
					saveView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Bundle t = (Bundle)v.getTag();
							byte[][] tr = (byte[][])t.getSerializable("transactions");
							Bundle resps = new Bundle(); 
							for (int i = 0; i < tr.length; i ++) {
								resps.putByteArray(String.valueOf(i), tr[i]);
							}
							Bundle b = new Bundle();
							b.putBundle("responses", resps);
							mReplaySession = b;
							enableTagReplay();
						}					
					});
				}				
//				for (int i = 0; i < transactions.length; i++) {
//					saveView.append(i + ": " + TextHelper.byteArrayToHexString(transactions[i]) + "\n");	
//				}			
			}

			@Override
			public View newView(Context context, Cursor cursor, ViewGroup parent) {				
                final View view = LayoutInflater.from(MyAccount.this).inflate(R.layout.save_tab, parent, false);
                return view;						
			}
        });
        
        setListViewHeightBasedOnChildren(mSavedList);
        
//        final SharedPreferences prefs = getSharedPreferences(NFCVars.PREFERENCES, MODE_PRIVATE);
        prefs = getSharedPreferences(NFCVars.PREFERENCES, MODE_PRIVATE);
        if (!prefs.contains("saltPref")) {
        	prefs.edit().putString("saltPref", CryptoHelper.generateSalt()).commit();
        }
        
        
        log("onCreate end"); 
        
        
        
        accessWebService();
        
        
    }
    
	/* (non-Javadoc)
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {		
		log("onResume start");
		super.onResume();
		
		
		NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
		if (adapter != null) {
			IntentFilter intentFilter[] = { new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED) };
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()), 0);
			adapter.enableForegroundDispatch(this, pendingIntent, intentFilter, new String[][] { new String[] { NFCVars.ISO_PCDA_CLASS } });
		}
		
		Intent intent = getIntent(); 
		
		
		SharedPreferences prefs = getSharedPreferences(NFCVars.PREFERENCES, Context.MODE_PRIVATE);		
		try {
			Class.forName(NFCVars.ISO_PCDA_CLASS);
		} catch (ClassNotFoundException e) {

			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				prefs.edit().putBoolean("relayPref", true).commit();
				Toast.makeText(this, getString(R.string.pcd_na_switch), Toast.LENGTH_LONG).show();
			}
			else {
				Toast.makeText(this, getString(R.string.pcd_na_unpredict), Toast.LENGTH_LONG).show();
			}
		}
		
        if (prefs.getBoolean("relayPref", false)) {
        	Intent forwardIntent = new Intent(intent);
        	forwardIntent.setClass(this, NFCRelayActivity.class);
        	startActivity(forwardIntent);
        	finish();
        }
        
        if (prefs.getBoolean("screenPref", true)) {
	        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, getString(R.string.app_name));
	        mWakeLock.acquire();
        }
        
        mDebugLogging = prefs.getBoolean("debugLogPref", false);
        mSalt = prefs.getString("saltPref", DEFAULT_SALT);        
        mServerPort = prefs.getInt("portPref", Integer.parseInt(getString(R.string.default_port)));
		mServerIP = prefs.getString("ipPref", getString(R.string.default_ip));
		mEncrypt = prefs.getBoolean("encryptPref", true);
               
		Tag extraTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);	//required
		Parcelable[] extraNdefMsg = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);	//optional
		byte[] extraId = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);	//optional
		
        if (extraTag != null) {	
			updateStatus(getString(R.string.reader) + " " + extraTag.toString());
    		if (mDataView == null) {

				TableRow row = new TableRow(this);
				row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				mDataView = new TextView(this);
				mDataView.setFreezesText(true);
				mDataView.setId(mSessions.size());
				row.addView(mDataView);
				mDataTable.addView(row);
    		}				
			
			if (mMode == REPLAY_PCD_MODE) {
				log("pcd mode");
				doReplayPCD(extraTag,  mReplaySession.getBundle("requests"), mReplaySession.getBundle("responses"));
			}
			else {
		    	boolean isPCD = false;
				String[] tech = extraTag.getTechList();	    	
		    	for (String s: tech) {
		    		if (s.equals(NFCVars.ISO_PCDA_CLASS)) {
	    				isPCD = true;
	    				break;
			        }	            		            
		    		else if  (s.equals(NFCVars.ISO_PCDB_CLASS)) {
		    			Toast.makeText(this, getString(R.string.report_pcdb), Toast.LENGTH_LONG).show();
		    		}
	    		}    
		    	
		    	if (isPCD) {
	    			log("Found PCD");	
					if (mMode == REPLAY_TAG_MODE) {
						log("tag mode");	
						doReplayTag(extraTag, mReplaySession.getBundle("responses"), mReplaySession.getBundle("requests"));
					}
					else {
						log("proxy mode");						
			    		new ProxyTask().execute(extraTag);
					}	    		
		    	}
		    	else {
		    		log("no PCD tag");
		    	}
			}
        }		
        else {
        	log("no extratag");
        }
        log("onResume end");	        
	}		
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		
		NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
		if (adapter != null) {
			adapter.disableForegroundDispatch(this);
		}

		if (mWakeLock != null) {			
			mWakeLock.release();
		}
	}
	    
    private void addLineBreak(int id) {
		TableRow line = new TableRow(this);
		line.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, 1));
		line.setBackgroundColor(Color.GREEN);		
		TextView ltv = new TextView(this);				
		ltv.setHeight(1);
		line.addView(ltv);
		line.setTag(id);
		mDataTable.addView(line);
    }
    
	private void storeTransactionsAndBreak(Bundle requests, Bundle responses) {
		final Bundle session = new Bundle();
		session.putBundle("requests", requests);
		session.putBundle("responses", responses);
        mDataView.setOnLongClickListener(getTransactionsTextViewLongClickListener());
		mDataTable.post(new Runnable() {
			@Override
			public void run() {
				if (mDataView!= null && mDataView.getText().length() > 0) {
					mSessions.putBundle(String.valueOf(mSessions.size()), session);										
					addLineBreak(mSessions.size() - 1);
					mDataTab.fullScroll(ScrollView.FOCUS_DOWN);
					mDataView = null;
				}					
			}
		});			
	}
	
	private void updateStatus(CharSequence msg) {
		mStatusView.append(TextUtils.concat(msg, "\n"));
		mStatusTab.post(new Runnable() {
				@Override
				public void run() {					
					mStatusTab.fullScroll(ScrollView.FOCUS_DOWN);
				}    			
    		});
	}

	private void updateData(CharSequence msg) {
		mDataView.append(TextUtils.concat(msg, "\n"));
		mDataTab.post(new Runnable() {
				@Override
				public void run() {					
					mDataTab.fullScroll(ScrollView.FOCUS_DOWN);
				}    			
    		});
	}		
	
//	private void enablePCDReplay() {
//		mMode = REPLAY_PCD_MODE;
//    	updateStatus(getString(R.string.waiting));
//    	mOptionsMenu.getItem(0).setVisible(false);
//    	mOptionsMenu.getItem(1).setVisible(true);
//    	mOptionsMenu.getItem(2).setVisible(true);
//		mTabHost.setCurrentTab(2); //TODO Data tab SAVE傳送到DATA頁面
//		Toast.makeText(this, getString(R.string.replay_pcd_on_next), Toast.LENGTH_LONG).show();		
//	}
	
	private void enableTagReplay() {
    	mMode = REPLAY_TAG_MODE;
    	updateStatus("Waiting for PCD");
    	mOptionsMenu.getItem(0).setVisible(true);
    	mOptionsMenu.getItem(1).setVisible(false);
    	mOptionsMenu.getItem(2).setVisible(true);
		mTabHost.setCurrentTab(2); //Data tab
    	Toast.makeText(this, getString(R.string.replay_tag_on_next), Toast.LENGTH_LONG).show();
	}
	
	//TODO: do in separate thread
	private void doReplayPCD(Tag tag, Bundle pcdRequests, Bundle tagTransactions) {
		Bundle responses = new Bundle();
		BasicTagTechnologyWrapper tagTech = null;
		try {
			//TODO:add support for more tag types
			Class[] supportedTags = new Class[] { IsoDep.class };			
			String[] tech = tag.getTechList();	    	
	    	for (String s: tech) {
	
	    		for(Class c: supportedTags) {
	    			if (s.equals(c.getName())) {
	    				try {
							tagTech = new BasicTagTechnologyWrapper(tag, c.getName());
							break;
						} catch (IllegalArgumentException e) {
							log(e);
						} catch (ClassNotFoundException e) {
							log(e);
						} catch (NoSuchMethodException e) {
							log(e);
						} catch (IllegalAccessException e) {
							log(e);
						} catch (InvocationTargetException e) {
							log(e);
						}    				
	    			}
	    		}    		
			}
	    	if (tagTech != null) {
	    		tagTech.connect();
	    		boolean connected = tagTech.isConnected(); 
	    		log("isConnected: " + connected);
	    		if (!connected) return;
	    		
	    		//first store ID
	    		responses.putByteArray(String.valueOf(0), tag.getId());
	    		String tagStr = getString(R.string.tag) + ": ";
	    		String pcdStr = getString(R.string.pcd) + ": ";
	    		SpannableString msg = new SpannableString(tagStr + TextHelper.byteArrayToHexString(tag.getId()));
		    	msg.setSpan(new UnderlineSpan(), 0, 4, 0);				    				    	
				updateData(msg);	    	
				boolean foundCC = false;
	    		for(int i=0; i < pcdRequests.size(); i++) {
	    			if (foundCC) {
	    				updateData(""); //print newline. this will probably cause formatting problems later
	    			}
	    			byte[] tmp = pcdRequests.getByteArray(String.valueOf(i));
					msg = new SpannableString(pcdStr + TextHelper.byteArrayToHexString(tmp));
			    	msg.setSpan(new UnderlineSpan(), 0, 4, 0);				    				    	
					updateData(msg);
	    			byte[] reply = tagTech.transceive(tmp);

	    			responses.putByteArray(String.valueOf(i+1), reply);
	    			if (mMask && reply != null && reply[0] == 0x70) {
	    				msg = new SpannableString(tagStr + getString(R.string.masked));
	    			}
	    			else {
	    				msg = new SpannableString(tagStr + TextHelper.byteArrayToHexString(reply));
	    			}
			    	msg.setSpan(new UnderlineSpan(), 0, 4, 0);				    				    	
					updateData(msg);	    				    		
	    			
					if (tagTransactions != null) {
						if (i + 1 < tagTransactions.size() ) {
							if (Arrays.equals(reply, tagTransactions.getByteArray(String.valueOf(i + 1)))) {
								log(getString(R.string.equal));
								updateStatus(getString(R.string.equal));
							}
							else {
								log(getString(R.string.not_equal));
log("org: " + TextHelper.byteArrayToHexString(tagTransactions.getByteArray(String.valueOf(i + 1))));
log("new : " + TextHelper.byteArrayToHexString(reply));
								updateStatus(getString(R.string.not_equal));
updateStatus("org: " + TextHelper.byteArrayToHexString(tagTransactions.getByteArray(String.valueOf(i + 1))));
updateStatus("new : " + TextHelper.byteArrayToHexString(reply));					
							}
						}
						else {
							log("index to responses out of bounds");
							updateStatus(getString(R.string.index_out_bounds));
						}
					}

	    			if (reply != null && reply[0] == 0x70) {
	    				updateData("\n" + TagHelper.parseCC(reply, pcdRequests.getByteArray(String.valueOf(i - 1)), mMask));
	    				foundCC = true;
	    				if (i == pcdRequests.size() - 1) {
		    				log(getString(R.string.finished_reading));
		    				updateStatus(getString(R.string.finished_reading));
	    				}
	    			}
					else if (reply != null && reply.length > 3 && reply[0] == 0x77 && reply[2] == (byte)0x9f) {
						updateData("\n" + TagHelper.parseCryptogram(reply, tmp)); //previous pcdRequest
	    				log(getString(R.string.finished_reading));
	    				updateStatus(getString(R.string.finished_reading));
					}

	    		}
	    		
	    	}
	    	else {
	    		log(getString(R.string.unsupported_tag));
	    		updateStatus(getString(R.string.unsupported_tag));
	    	}
		} catch (IllegalStateException e) {
			log(e);
			updateStatus(e.toString());
		} catch (IOException e) {
			log(e);
			updateStatus(e.toString());
		}
		finally {			
			storeTransactionsAndBreak(pcdRequests, responses);
			if (tagTech != null) {
				try {
					tagTech.close();
				} catch (IOException e) {
					log(e);
				}
			}
		}

		//log(getString(R.string.lost_connection));
		//updateStatus(getString(R.string.lost_connection));
	}	

	//TODO: do in separate thread
	private void doReplayTag(Tag tag, Bundle tagTransactions, Bundle pcdRequests) {
		Bundle requests = new Bundle();

		try {
			//TODO:PCD hack. Add support for PCD B
			Class cls = Class.forName(NFCVars.ISO_PCDA_CLASS);    				
			Method meth = cls.getMethod("get", new Class[]{Tag.class});
			Object ipcd = meth.invoke(null, tag);
			meth = cls.getMethod("connect", null);
			meth.invoke(ipcd, null);
			meth = cls.getMethod("isConnected", null);
			boolean connected = (Boolean) meth.invoke(ipcd, null);
			log("isConnected: " + connected);
			if (!connected) {
				log("Not connected to PCD");
				//updateStatus("Not connected to PCD");
				//return;				
			}
			else {
				
				meth = cls.getMethod("transceive", new Class[]{byte[].class});
	    		String tagStr = getString(R.string.tag) + ": ";
	    		String pcdStr = getString(R.string.pcd) + ": ";					

				for (int i=0; i < tagTransactions.size(); i++) {
					byte []tmp = tagTransactions.getByteArray(String.valueOf(i));
					SpannableString msg;
	    			if (mMask && tmp != null && tmp[0] == 0x70) {
	    				msg = new SpannableString(tagStr + getString(R.string.masked));
	    			}
	    			else {
	    				msg = new SpannableString(tagStr + TextHelper.byteArrayToHexString(tmp));
	    			}
					
			    	msg.setSpan(new UnderlineSpan(), 0, 4, 0);				    				    	
					updateData(msg);
					byte[] reply = (byte[]) meth.invoke(ipcd, tmp);
					
					requests.putByteArray(String.valueOf(i), reply);
					msg = new SpannableString(pcdStr + TextHelper.byteArrayToHexString(reply));
			    	msg.setSpan(new UnderlineSpan(), 0, 4, 0);				    				    	
					updateData(msg);
					
					if (pcdRequests != null) {
						if (i < pcdRequests.size() ) {
							if (Arrays.equals(reply, pcdRequests.getByteArray(String.valueOf(i)))) {
								log(getString(R.string.equal));
								updateStatus(getString(R.string.equal));
							}
							else {
								log(getString(R.string.not_equal));
log("org: " + TextHelper.byteArrayToHexString(pcdRequests.getByteArray(String.valueOf(i))));
log("new : " + TextHelper.byteArrayToHexString(reply));								
								updateStatus(getString(R.string.equal));
updateStatus("org: " + TextHelper.byteArrayToHexString(pcdRequests.getByteArray(String.valueOf(i))));
updateStatus("new : " + TextHelper.byteArrayToHexString(reply));					
	
								//TODO: 
								//attempt to find a matching response if it exists. This probably doesn't work.
								//this will also break replay mode unless new sequences are added to tagTransactions
								for (int k = 0; k < pcdRequests.size(); k++ ) {
									if (k == i) continue;
									if (Arrays.equals(reply, pcdRequests.getByteArray(String.valueOf(k)))) {
										i = k;
										i = k - 1;
										log("found matching response. replay of this run is probably broken.");										
										break;
									}
								}
							}
						}
						else {
							log("index to requests out of bounds");
							updateStatus("index to requests out of bounds");
						}
					}
				}				
			} 
		}
		catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			if (cause instanceof IOException && cause.getMessage() != null && cause.getMessage().equals("Transceive failed")) {
				log("transaction complete");
				updateStatus("transaction complete");
				return;
			}
			else {
				log(e);
			}
		}
		catch (Exception e) { //TODO:
			log(e);
			updateStatus(e.toString());	
		}
		finally { 			
			storeTransactionsAndBreak(requests, tagTransactions);
		}
		log("Lost connection to PCD?");
		updateStatus("Lost connection to PCD?");
	}	

	private class ProxyTask extends AsyncTask<Tag, Void, Void> {

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}
		
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onCancelled(java.lang.Object)
		 */
		@Override
		protected void onCancelled(Void result) {
			// TODO Auto-generated method stub
			super.onCancelled(result);
		}

		@Override
		protected Void doInBackground(Tag... params) {
			log("doInBackground start");				
			Socket clientSocket = null;
			BufferedOutputStream clientOS = null;
			BufferedInputStream clientIS = null;						
			long startTime = System.currentTimeMillis();
			
            try {   
            	
				log(getString(R.string.connecting_to_relay));
				updateStatusUI(getString(R.string.connecting_to_relay));				
				
		        mSockAddr = new InetSocketAddress(mServerIP, mServerPort);
        		clientSocket = new Socket();
				clientSocket.connect(mSockAddr, CONNECT_TIMEOUT);
				clientOS = new BufferedOutputStream (clientSocket.getOutputStream());
				clientIS = new BufferedInputStream(clientSocket.getInputStream());
				log(getString(R.string.connected_to_relay));
				updateStatusUI(getString(R.string.connected_to_relay));
			
				Bundle requests = new Bundle();
				Bundle responses = new Bundle();
				try {    			
					log("sending ready");
					IOUtils.sendSocket((NFCVars.READY + "\n").getBytes("UTF-8"), clientOS, null, false);
					String line = IOUtils.readLine(clientIS);
					log("command: " + line);        
					if (line.equals(NFCVars.NOT_READY)) {
			    		updateStatusUI(getString(R.string.nfcrelay_not_ready));
			    		log(getString(R.string.nfcrelay_not_ready));    		
			    		return null;    						
					} else if (!line.equals(NFCVars.OPTIONS)) {
			    		updateStatusUI(getString(R.string.unknown_command));
			    		log(getString(R.string.unknown_command));    		
						return null;
					}
					if (mEncrypt) {					
						
						IOUtils.sendSocket((NFCVars.ENCRYPT + "\n").getBytes("UTF-8"), clientOS, null, false);
						IOUtils.sendSocket(Base64.decode(mSalt, Base64.DEFAULT), clientOS, null, false);
						
						if (mSecret == null) {
							mSecret = generateSecretKey();
						}
		    	    	byte[] verify = IOUtils.readSocket(clientIS, mSecret, mEncrypt);
		    	    	if (verify == null) {
				    		updateStatusUI(getString(R.string.unexpected_response_encrypting));
				    		log(getString(R.string.unexpected_response_encrypting));
				    		log(TextHelper.byteArrayToHexString(verify));
		    				return null;		    	    		
		    	    	}
		    	    	else if (!new String(verify, "UTF-8").equals(NFCVars.VERIFY)) {
				    		updateStatusUI(getString(R.string.bad_password));
				    		log(getString(R.string.bad_password));
				    		log(TextHelper.byteArrayToHexString(verify));
				    		IOUtils.sendSocket(NFCVars.BAD_PASSWORD.getBytes("UTF-8"), clientOS, mSecret, mEncrypt);
				    		return null;
    	    	    	}	    	    	    	
		    	    	IOUtils.sendSocket(NFCVars.OK.getBytes("UTF-8"), clientOS, mSecret, mEncrypt);
						
					}
					else {
						IOUtils.sendSocket((NFCVars.CLEAR + "\n").getBytes("UTF-8"), clientOS, null, false);
					}
	    	    	
					log("getting id");				
					byte[] id = IOUtils.readSocket(clientIS, mSecret, mEncrypt);
					if (id == null) {
			    		updateStatusUI(getString(R.string.error_getting_id));
			    		log(getString(R.string.error_getting_id));    		
				    		return null;
				    	}    		
log("response: " + TextHelper.byteArrayToHexString(id));
log(new String(id));

					byte[] pcdRequest = null;
					byte[] cardResponse = null;
		    		String tagStr = getString(R.string.tag) + ": ";
		    		String pcdStr = getString(R.string.pcd) + ": ";					
			    	try {
			    		//TODO:PCD hack. Add support for PCD B
	    				Class cls = Class.forName(NFCVars.ISO_PCDA_CLASS);
		            	/*
		            	methods
	    		        05-14 16:49:03.229: D/NFCProxy(3642): close
	    		        05-14 16:49:03.229: D/NFCProxy(3642): connect
	    		        05-14 16:49:03.229: D/NFCProxy(3642): equals
	    		        05-14 16:49:03.229: D/NFCProxy(3642): get
	    		        05-14 16:49:03.229: D/NFCProxy(3642): getClass
	    		        05-14 16:49:03.229: D/NFCProxy(3642): getMaxTransceiveLength
	    		        05-14 16:49:03.229: D/NFCProxy(3642): getTag
	    		        05-14 16:49:03.229: D/NFCProxy(3642): hashCode
	    		        05-14 16:49:03.229: D/NFCProxy(3642): isConnected
	    		        05-14 16:49:03.229: D/NFCProxy(3642): notify
	    		        05-14 16:49:03.232: D/NFCProxy(3642): notifyAll
	    		        05-14 16:49:03.232: D/NFCProxy(3642): reconnect
	    		        05-14 16:49:03.232: D/NFCProxy(3642): toString
	    		        05-14 16:49:03.232: D/NFCProxy(3642): transceive
	    		        05-14 16:49:03.232: D/NFCProxy(3642): wait
	    		        05-14 16:49:03.232: D/NFCProxy(3642): wait
	    		        05-14 16:49:03.232: D/NFCProxy(3642): wait
	    		        
	    		        https://github.com/CyanogenMod/android_frameworks_base/blob/ics/core/java/android/nfc/tech/IsoPcdA.java
	    		        */	    				
		            	Method meth = cls.getMethod("get", new Class[]{Tag.class});
		            	Object ipcd = meth.invoke(null, params[0]);
		            	meth = cls.getMethod("connect", null);
		            	meth.invoke(ipcd, null);
		            	meth = cls.getMethod("isConnected", null);
		            	boolean connected = (Boolean) meth.invoke(ipcd, null);
	            		log("isConnected: " + connected);
						if (!connected) {
							log(getString(R.string.not_connected_to_pcd));
							updateStatusUI(getString(R.string.not_connected_to_pcd));
							return null;
						}
		            	
		            	meth = cls.getMethod("transceive", new Class[]{byte[].class});	//TODO: check against getMaxTransceiveLength()

	            		pcdRequest = (byte[])meth.invoke(ipcd, id);
	            		
						SpannableString msg = new SpannableString(tagStr + TextHelper.byteArrayToHexString(id));
				    	msg.setSpan(new UnderlineSpan(), 0, 4, 0);				    	
				    	responses.putByteArray(String.valueOf(responses.size()), id);
						updateDataUI(msg);
log("sent id to pcd: " + TextHelper.byteArrayToHexString(id));
												
						msg = new SpannableString(pcdStr + TextHelper.byteArrayToHexString(pcdRequest));
					    msg.setSpan(new UnderlineSpan(), 0, 4, 0);					    
					    requests.putByteArray(String.valueOf(requests.size()), pcdRequest);
						updateDataUI(msg);												
log("response from PCD: " + TextHelper.byteArrayToHexString(pcdRequest));
log(new String(pcdRequest));
						do {

							IOUtils.sendSocket(pcdRequest, clientOS, mSecret, mEncrypt);
log("sent response to relay/card");				

							cardResponse = IOUtils.readSocket(clientIS, mSecret, mEncrypt);
							if (cardResponse != null) {

								if (new String(cardResponse, "UTF-8").equals("Relay lost tag") ) {
									updateStatusUI(getString(R.string.relay_lost_tag));
									log(getString(R.string.relay_lost_tag));	
										break;										
									}
								}
								else {
									updateStatusUI(getString(R.string.bad_crypto));
									log(getString(R.string.bad_crypto));	
									break;
								}
								
log("relay/card response: " + TextHelper.byteArrayToHexString(cardResponse));						
log(new String(cardResponse));
								
								
							log("sending card response to PCD");
			    			if (mMask && cardResponse[0] == 0x70) {
			    				msg = new SpannableString(tagStr + getString(R.string.masked));
			    			}
			    			else {
			    				msg = new SpannableString(tagStr + TextHelper.byteArrayToHexString(cardResponse));
			    			}
							
						    msg.setSpan(new UnderlineSpan(), 0, 4, 0);					    
						    responses.putByteArray(String.valueOf(responses.size()), cardResponse);
						    updateDataUI(msg);										    						
							if (cardResponse[0] == 0x70 || cardResponse[0] == 0x77) {
								try {
									pcdRequest = (byte[])meth.invoke(ipcd, cardResponse);
								} catch (InvocationTargetException e) {
									if (e.getCause() instanceof IOException && e.getCause().getMessage().equals("Transceive failed")) {										
										//update UI only after sending cardResponse to PCD
										if (cardResponse[0] == 0x70) {
											updateDataUI("\n" + TagHelper.parseCC(cardResponse, requests.getByteArray(String.valueOf(requests.size() - 2)), mMask));
										}
										else if (cardResponse.length > 3 && cardResponse[0] == 0x77 && cardResponse[2] == (byte)0x9f) {
											updateDataUI("\n" + TagHelper.parseCryptogram(cardResponse, pcdRequest)); //previous pcdRequest
										}
										updateDataUI(getString(R.string.time) + ": " + (System.currentTimeMillis() - startTime));
										log(getString(R.string.transaction_complete));
										updateStatusUI(getString(R.string.transaction_complete));
										break;												
									}
									throw e;
								}
								if (cardResponse[0] == 0x70) {
									updateDataUI("\n" + TagHelper.parseCC(cardResponse, requests.getByteArray(String.valueOf(requests.size() - 2)), mMask) + "\n");
								}
							}		    						
							else {
								pcdRequest = (byte[])meth.invoke(ipcd, cardResponse);
							}					
							requests.putByteArray(String.valueOf(requests.size()), pcdRequest);
    						msg = new SpannableString(pcdStr + TextHelper.byteArrayToHexString(pcdRequest));
						    msg.setSpan(new UnderlineSpan(), 0, 4, 0);
							updateDataUI(msg);									

log("response from PCD: " + TextHelper.byteArrayToHexString(pcdRequest));									
							
						} while (pcdRequest != null);
	            	}
            		catch(ClassNotFoundException e) {
	                	log(e);    
	                	updateStatusUI("ClassNotFoundException");
            		}
            		catch(NoSuchMethodException e) {
            			log(e);
            			updateStatusUI("NoSuchMethodException");
            		}
            		catch (InvocationTargetException e) {
            			
						if (e instanceof InvocationTargetException) {									
							if (((InvocationTargetException) e).getCause() instanceof TagLostException) {
								log(getString(R.string.lost_pcd));
								updateStatusUI(getString(R.string.lost_pcd));
							}
						}
						else {
							log(e);
							updateStatusUI("InvocationTargetException");
						}	            			            			
            		}
            		catch(IllegalAccessException e) {
            			log(e);
            			updateStatusUI("IllegalAccessException");
            		} catch (IOException e) {
            			log(e);
            			updateStatusUI(getString(R.string.ioexception_error_writing_socket));
					}		    			
	            	finally {
	            		try {
	            			//TODO:PCD hack. Add support for PCD B
	            			Class cls = Class.forName(NFCVars.ISO_PCDA_CLASS);
	            			Method meth = cls.getMethod("get", new Class[]{Tag.class});
	            			Object ipcd = meth.invoke(null, params[0]);
			            	meth = cls.getMethod("close", null);
			            	meth.invoke(ipcd, null);
	            		}
	            		catch(ClassNotFoundException e) {
		                	log(e);            	
		                	updateStatusUI("ClassNotFoundException");
	            		}
	            		catch(NoSuchMethodException e) {
	            			log(e);
	            			updateStatusUI("NoSuchMethodException");
	            		}
	            		catch (InvocationTargetException e) {
	            			log(e);
	            			updateStatusUI("InvocationTargetException");
	            		}
	            		catch(IllegalAccessException e) {
	            			log(e);
	            			updateStatusUI("IllegalAccessException");
	            		}
	            	}    			    	
				}
				catch (UnsupportedEncodingException e) {
					log(e);
					updateStatusUI("UnsupportedEncodingException");
				}

				if (mDataView == null) { 
					log("mDataView null"); //??? happens on quick reads? activity is recreated with
				}	
				else {
					//Finish
					storeTransactionsAndBreak(requests, responses);
				}					
            }
            catch (SocketTimeoutException e) {
            	log(e);
            	updateStatusUI(getString(R.string.connection_to_relay_timed_out));
            }
            catch (ConnectException e) {
            	log(getString(R.string.connection_to_relay_failed));
            	updateStatusUI(getString(R.string.connection_to_relay_failed));            	
            } 
            catch (SocketException e) {
            	log(e);
            	updateStatusUI(getString(R.string.socket_error) + " " + e.getLocalizedMessage());            	
            }
            catch (UnknownHostException e) {
            	updateStatusUI(getString(R.string.unknown_host));
            }
            catch (IOException e) {
            	log(e);
            	updateStatusUI("IOException: " + e.getLocalizedMessage());            	
			}
            catch (final Exception e) 
            {
            	StringWriter sw = new StringWriter();
            	e.printStackTrace(new PrintWriter(sw));
                log(getString(R.string.something_happened) + ": " + e.toString() + " " + sw.toString());            	
                updateStatusUI(getString(R.string.something_happened) + ": " + e.toString() + " " + sw.toString());
            }
            finally {            	            	
        		try 
        		{
        			log ("Closing connection to NFCRelay...");
        			if (clientSocket != null)
        				clientSocket.close();
        		}
                catch (IOException e) 
                {
                	log("error closing socket: " + e);             
                }
            	log("doInBackground end");                
            }            
			return null;
		}
		
		private void updateStatusUI(final CharSequence msg) {
			mStatusView.post(new Runnable() {
				@Override
				public void run() {					
					mStatusView.append(TextUtils.concat( msg, "\n"));
					mStatusTab.post(new Runnable() {
						@Override
						public void run() {					
							mStatusTab.fullScroll(ScrollView.FOCUS_DOWN);
						}    			
		    		});
				}    			
    		});						
		}
		
		private void updateDataUI(final CharSequence msg) {				
			mDataView.post(new Runnable() {
				@Override
				public void run() {
					mDataView.append(TextUtils.concat(msg, "\n"));
					mDataTab.fullScroll(ScrollView.FOCUS_DOWN);
				}    			
    		});
		}		
	}
	
	private SecretKey generateSecretKey() throws IOException {
		try {
    		SharedPreferences prefs = getSharedPreferences(NFCVars.PREFERENCES, MODE_PRIVATE);
    		SecretKeyFactory f;
			f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] salt = Base64.decode(mSalt, Base64.DEFAULT);
	        KeySpec ks = new PBEKeySpec(prefs.getString("passwordPref", getString(R.string.default_password)).toCharArray(), salt, 2000, 256);
	        SecretKey tmp = f.generateSecret(ks);
	        return new SecretKeySpec(tmp.getEncoded(), "AES");
		} catch (Exception e) {
			log(e);
			throw new IOException(e);
		}        			
	}
	
	protected void saveRun(String name, int type) {
		Bundle transactions = null;
		if (type == DBHelper.REPLAY_PCD) {
			transactions = mReplaySession.getBundle("requests");
		}
		else { //if (type == DBHelper.REPLAY_TAG) {
			transactions = mReplaySession.getBundle("responses");
		}		
		byte[][] data = new byte[transactions.size()][];
		for (int i = 0; i < transactions.size(); i ++) {
			data[i] = transactions.getByteArray(String.valueOf(i));
		}
//		long inserted = mDBHelper.saveTransactions(name, data, type );
		long inserted = mDBHelper.saveNEWTransactions(name, data, type );
		
		if (inserted != -1) {
			//TODO: should be done in new thread
			((CursorAdapter)mSavedList.getAdapter()).swapCursor(mDBHelper.getNEWReplays());
			Toast.makeText(this, "已儲存", Toast.LENGTH_LONG).show();
		}
		else {
			Toast.makeText(this, "Not saved. Duplicate name.", Toast.LENGTH_LONG).show();
		}
	}
	
	private void deleteSaved() {
		String name = ((Bundle)mSelectedSaveView.getTag()).getString("name");
		int num = mDBHelper.deleteNEWReplay(name);
		if (num != 1) {
			Toast.makeText(this, "Error deleting replay", Toast.LENGTH_SHORT).show();
		}
		//TODO: should be done in new thread
		((CursorAdapter)mSavedList.getAdapter()).swapCursor(mDBHelper.getNEWReplays());
	}	
	
	protected void exportRun(String filename) {
		try {					 
		    String state = Environment.getExternalStorageState();
		    if (Environment.MEDIA_MOUNTED.equals(state)) {
		        File dir = Environment.getExternalStorageDirectory();		       
		        String dirPath = dir.getAbsolutePath();
		        File exportPath = new File(dirPath + File.separator + NFCVars.STORAGE_PATH);
		        if (!exportPath.exists()) {
		        	if (!exportPath.mkdir()) {
		        		Toast.makeText(this, "Error creating storage directory", Toast.LENGTH_LONG).show();		        		
		        		return;
		        	}
		        }
		        //let user store where ever they want
		        File exportFile = new File(exportPath + File.separator + filename);
		        //TODO: make sure filename is valid filename. also warn if file exists.
				FileWriter writer = new FileWriter(exportFile);
				
				Bundle session = mSessions.getBundle(String.valueOf(mSelectedId));
				Bundle requests = session.getBundle("requests");
				Bundle responses = session.getBundle("responses");

				StringBuilder sbRequests = new StringBuilder("byte[][] pcdRequests = new byte[][] {");
				StringBuilder sbResponses = new StringBuilder("byte[][] tagResponses = new byte[][] {");
				for(int i = 0; i < requests.size(); i ++) {
					byte req[] = requests.getByteArray(String.valueOf(i));
					sbRequests.append("{").append(TextHelper.byteArrayToHexString(req, "0x", ", ", true)).append("}");
					if (i +1 != requests.size()) {
						sbRequests.append(", ");
					}
				}
				sbRequests.append("};\n");
				for(int i = 0; i < responses.size(); i ++) {
					byte resp[] = responses.getByteArray(String.valueOf(i));
					sbResponses.append("{").append(TextHelper.byteArrayToHexString(resp, "0x", ", ", true)).append("}");
					if (i +1 != responses.size()) {
						sbResponses.append(", ");
					}
				}
				sbResponses.append("};\n");
				writer.write(sbRequests.toString());
				writer.write(sbResponses.toString());
				writer.close();						
            	Toast.makeText(this, "Saved to:\n" + exportFile.getAbsolutePath(), Toast.LENGTH_LONG).show();						
				return;
		        
		    } else {
		        Toast.makeText(this, "Error writing to external storage", Toast.LENGTH_LONG).show();
		    }						
		} catch (IOException e) {
			log(e);
		}			
	}

	private void deleteRun() {	
			View v = mDataTable.findViewById(mSelectedId);
			TableRow row = (TableRow)v.getParent();
			row.setVisibility(View.GONE);		
			TableRow line = (TableRow)mDataTable.findViewWithTag(mSelectedId);
			line.setVisibility(View.GONE);
			Toast.makeText(this, "已刪除", Toast.LENGTH_LONG).show();
			//don't re-adjust IDs
	}
	
	private View.OnLongClickListener getTransactionsTextViewLongClickListener() {
		return new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View view) {
		        if (mActionMode != null) {
		            return false;
		        }
		        view.setSelected(true);		        
		        mSelectedId = view.getId();
		        log("selectedID: " + mSelectedId);		        
		        mActionMode = MyAccount.this.startActionMode(mTransactionsActionModeCallback);
		        return true;
			}
		};
	}

	private View.OnLongClickListener getSavedTextViewLongClickListener() {
		return new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View view) {
		        if (mActionMode != null) {
		            return false;
		        }
		        
		        view.setSelected(true);
		        mSelectedSaveView = view; 
		        mActionMode = MyAccount.this.startActionMode(mSavedActionModeCallback);
		        return true;
			}
		};
	}
	
	private void cutSessionAt(int id) {
	
		int size = mSessions.size();
		for(int i = id; i < size; i++) {
			if (i == id) {
				mSessions.remove(String.valueOf(id));
			}
			else {
				Bundle b = mSessions.getBundle(String.valueOf(i));
				mSessions.remove(String.valueOf(i));
				mSessions.putBundle(String.valueOf(i - 1), b);	//i will always be > 0
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);		

		ArrayList<CharSequence> rows = new ArrayList<CharSequence>(); 
		for (int i = 0; i < mDataTable.getChildCount(); i++) {
			TableRow tr = (TableRow)mDataTable.getChildAt(i);
			if (tr.getVisibility() == View.GONE) {
				cutSessionAt(i);
				continue;
			}
			TextView tv = (TextView)(tr).getChildAt(0);
			if (tv.getText().length() > 0 ) {
				rows.add(tv.getText());
			}
						
		}
		outState.putCharSequenceArray("rows", rows.toArray(new CharSequence[rows.size()]));	//TODO: this is not encrypted
		outState.putInt("tab", mTabHost.getCurrentTab());
		outState.putBundle("sessions", mSessions);	//TODO: this is not encrypted
		outState.putBundle("replaySession", mReplaySession);	//TODO: this is not encrypted
		outState.putInt("mode", mMode);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		CharSequence[] rows = savedInstanceState.getCharSequenceArray("rows");
		if (rows != null) {
			for(int i = 0 ; i < rows.length; i++) {			
				TableRow row = new TableRow(this);
				row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				TextView tv = new TextView(this);				
				tv.setFreezesText(true);
				tv.setText(rows[i]);								
				tv.setOnLongClickListener(getTransactionsTextViewLongClickListener());
				tv.setId(i);
				row.addView(tv);
				mDataTable.addView(row);

				addLineBreak(i);
			}
		}
		mTabHost.setCurrentTab(savedInstanceState.getInt("tab"));
        mSessions = savedInstanceState.getBundle("sessions");
        mReplaySession = savedInstanceState.getBundle("replaySession");
        mMode = savedInstanceState.getInt("mode");
        if (mOptionsMenu != null && mMode == PROXY_MODE) {
        	mOptionsMenu.getItem(0).setVisible(false);
	        mOptionsMenu.getItem(1).setVisible(false);
	        mOptionsMenu.getItem(2).setVisible(false);        
        }
        else if (mOptionsMenu != null && mMode == REPLAY_PCD_MODE) {
        	mOptionsMenu.getItem(0).setVisible(false);
	        mOptionsMenu.getItem(1).setVisible(true);
	        mOptionsMenu.getItem(2).setVisible(true);        	
        }
        else if (mOptionsMenu != null && mMode == REPLAY_TAG_MODE) {
        	mOptionsMenu.getItem(0).setVisible(true);
	        mOptionsMenu.getItem(1).setVisible(false);
	        mOptionsMenu.getItem(2).setVisible(true);        	        	
        }
	}
	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mDBHelper != null) {
			mDBHelper.close();
		}
	}

    private void log(Object msg) {    	
    	if (mDebugLogging) {
    		LogHelper.log(this, msg);
    	}
    }
    
    
    //HTTP
    
 // Async Task to access the web
 	private class JsonReadTask extends AsyncTask<String, Void, String> {
 		@Override
 		protected String doInBackground(String... params) {
 			SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm:ss");
    		time = sDateFormat.format(new java.util.Date());
    		ePWD = md5(md5(PWD)+time);
			///////////////////////////////////
    		HttpClient httpwallet = new DefaultHttpClient();
			HttpPost walletpost = new HttpPost(params[0]+"?name="+ID +"&epw="+ePWD+"&time="+time+"&BASE64=1");
			System.out.println(params[0]+"?name="+ID +"&epw="+ePWD+"&time="+time+"&BASE64=1");
 			try {
					
					HttpResponse responsew = httpwallet.execute(walletpost);
					String responseTXT = inputStreamToString(responsew.getEntity().getContent()).toString();
					System.out.println(responseTXT);
					byte[] data = Base64.decode(responseTXT.getBytes(), Base64.DEFAULT);
					walletResult = new String(data);
 					
 			}
 			catch (ClientProtocolException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
    		
    		///////////////////////////////
    		
			
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(params[0]+"?client_id="+client_id+"&epw="+ePWD+"&time="+time+"&act_id=ALL"+"&BASE64=1");
			System.out.println(params[0]+"?client_id="+client_id+"&epw="+ePWD+"&time="+time+"&act_id=ALL"+"&BASE64=1");
 			try {
 					HttpResponse response = httpclient.execute(httppost);
					String responseTXT = inputStreamToString(response.getEntity().getContent()).toString();
					System.out.println(responseTXT);
					byte[] data = Base64.decode(responseTXT.getBytes(), Base64.DEFAULT);
					jsonResult = new String(data);
 					
 					System.out.println(jsonResult);
 					
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
 			ListDrwaer00();
 			if(!jsonResult.equals("EMPTY")){
 				System.out.println("00000000000000000000000000000000000");
 			ListDrwaer();
 			}
 			
 		}
 	}// end async task

 	public void accessWebService() {
 		JsonReadTask task = new JsonReadTask();
 		// passes values for the urls string array
 		task.execute(new String[] { url });
 	}
 	
 	public void ListDrwaer00() {
 		try {
			JSONObject wResponse = new JSONObject(walletResult);
			walletmoney = wResponse.getString("money");
			System.out.println(walletmoney);
			if(walletmoney.equals("null")){
				walletmoney = "0";
				int[] images = new int[1];
				images[0] = getResources().getIdentifier("img000", "drawable", getPackageName());
				ImageAdapter adapter = new ImageAdapter(this, images);
				adapter.createReflectedImages();

				GalleryFlow galleryFlow = (GalleryFlow) findViewById(R.id.galleryFlowAC);
				galleryFlow.setAdapter(adapter);
				galleryFlow.setOnItemClickListener(lvlistener);
				galleryFlow.setOnItemLongClickListener(lvlistenerLOOK);
			}
			else{
				int[] images = new int[1];
				images[0] = getResources().getIdentifier("img000", "drawable", getPackageName());
				ImageAdapter adapter = new ImageAdapter(this, images);
				adapter.createReflectedImages();

				GalleryFlow galleryFlow = (GalleryFlow) findViewById(R.id.galleryFlowAC);
				galleryFlow.setAdapter(adapter);
				galleryFlow.setOnItemClickListener(lvlistener);
				galleryFlow.setOnItemLongClickListener(lvlistenerLOOK);
			}
			//System.out.println(client_id+"@"+money);
			
		} catch (JSONException e) {
			Toast.makeText(getApplicationContext(), "Error" + e.toString(),
					Toast.LENGTH_SHORT).show();
		}
 	}
 	

 	// build hash set for list view
 	public void ListDrwaer() {
 		List<Map<String, String>> AccountList = new ArrayList<Map<String, String>>();
 		Accountinfo.clear();
 		cardinfo.clear();
 		String money, benifit;
 		try {
 			JSONObject jsonResponse = new JSONObject(jsonResult);
			JSONArray jsonMainNode = jsonResponse.optJSONArray("activity");
			for (int i = 0; i < jsonMainNode.length(); i++) {
				JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
				int recharge = jsonChildNode.optInt("recharge");
				System.out.println(recharge);
				if (recharge ==1)
				{
					String act_id = jsonChildNode.optString("act_id");
					String com_id = jsonChildNode.optString("com_id");
					String name = jsonChildNode.optString("name");
					JSONArray jsonCard = jsonResponse.optJSONArray("act_client");
					for (int j = 0; j < jsonCard.length(); j++) {
						JSONObject jsonChildCard = jsonCard.getJSONObject(j);
						String check_act_id = jsonChildCard.optString("act_id");
						String check_com_id = jsonChildCard.optString("com_id");
						if(check_act_id.equals(act_id)&&check_com_id.equals(com_id)){
							if(jsonChildCard.has("money")){
								money = jsonChildCard.optString("money");
							}
							else{
								money = "0";
							}

							if(jsonChildCard.has("benifit")){
								benifit = jsonChildCard.optString("benifit");
							}
							else{
								benifit = "0";
							}
							String outPut = name;
							System.out.println(name+"@"+money+"@"+act_id+"@"+com_id);
							//帳戶輸出 name@money@benifit@act_id@com_id
							Accountinfo.add(name+"@"+money+"@"+act_id+"@"+com_id);
							AccountList.add(createAccount("Account", outPut));
							cardinfo.add( new CardInfo (getResources().getIdentifier("img"+com_id+act_id, "drawable", getPackageName())));
						}
					}
				
					
				}
			}
			int[] images = new int[cardinfo.size()+1];
			
			images[0] = getResources().getIdentifier("img000", "drawable", getPackageName());
			for (int i = 1; i<cardinfo.size()+1; i++){
				images[i] = ((CardInfo)cardinfo.elementAt(i-1)).img;
			}
			
			ImageAdapter adapter = new ImageAdapter(this, images);
			adapter.createReflectedImages();

			GalleryFlow galleryFlow = (GalleryFlow) findViewById(R.id.galleryFlowAC);
			galleryFlow.setAdapter(adapter);
			galleryFlow.setOnItemClickListener(lvlistener);
			galleryFlow.setOnItemLongClickListener(lvlistenerLOOK);
			
 		} catch (JSONException e) {
 			Toast.makeText(getApplicationContext(), "Error: " + e.toString(),
 					Toast.LENGTH_SHORT).show();
 		}

// 		SimpleAdapter simpleAdapter = new SimpleAdapter(this, AccountList,
// 				android.R.layout.simple_list_item_1,
// 				new String[] { "Account" }, new int[] { android.R.id.text1 });
// 		mAccountList.setAdapter(simpleAdapter);
// 		setListViewHeightBasedOnChildren(mAccountList);
// 		mAccountList.setOnItemClickListener(lvlistener);
 		
 		
 		
 	}
 	private HashMap<String, String> createAccount(String name, String number) {
 		HashMap<String, String> AccountNameDeadLine = new HashMap<String, String>();
 		AccountNameDeadLine.put(name, number);
 		return AccountNameDeadLine;
 	}
 	
 	private OnItemLongClickListener lvlistenerLOOK  = new OnItemLongClickListener(){
 		@Override
 		public boolean onItemLongClick(AdapterView<?> arg0, View view, int position,
 				long id) {
 			
 			if(position ==0){
 				System.out.println(walletmoney+"                                00000000000000000000000000000");
 				CardName.setText("帳戶名稱 :Ewallet");
 	 			tvMoney.setText("餘額 :"+ walletmoney);
 			}
 			else{
 			String Selected = (String)Accountinfo.elementAt(position-1);
 			String[] fields = Selected.split("@");
 			CardName.setText("帳戶名稱 :"+ fields[0]);
 			tvMoney.setText("餘額 :"+ fields[1]);
 			}
			return LONGCLICK;
 			
 			
 		}
 		};
 	private OnItemClickListener lvlistener  = new OnItemClickListener(){
 		@Override
 		public void onItemClick(AdapterView<?> arg0, View view, int position,
 				long id) {
 			
 			if(position ==0){
 				Intent gotoEwallet = new Intent();
 				Bundle Selected = new Bundle();
 	     		Selected.putString("account", "MyWallet");
 	     		Selected.putString("money", walletmoney);
 	     		Selected.putString("offer_act_id", offer_act_id);
 	     		Selected.putString("offer_com_id", offer_com_id);
 	     		gotoEwallet.putExtras(Selected);
 				gotoEwallet.setClass(MyAccount.this, WalletDetail.class);
 	 			startActivity(gotoEwallet);
 	 			finish();
 			}
 			else{
 			String Selected = (String)Accountinfo.elementAt(position-1);
 			String[] fields = Selected.split("@");
// 			String Selected = mAccountList.getItemAtPosition(position).toString();
// 			String[] fields = Selected.substring(Selected.indexOf("=", 1)+1, Selected.length()-1).split("           ");
 			
 			
 			Intent gotoAccountDetail = new Intent();
	 			if(offer_act_id != null && offer_com_id != null){
	 				//帶優惠
	 	     		Bundle SelectedAccount = new Bundle();
	 	     		SelectedAccount.putString("account", fields[0]);
	 	     		SelectedAccount.putString("money", fields[1]);
	 	     		SelectedAccount.putString("act_id", fields[2]);
	 	     		SelectedAccount.putString("com_id", fields[3]);
	 	     		SelectedAccount.putString("offer_act_id", offer_act_id);
	 	     		SelectedAccount.putString("offer_com_id", offer_com_id);
	 	     		gotoAccountDetail.putExtras(SelectedAccount);
	 				
	 			}
	 			else{
	 	 			
	 	     		Bundle SelectedAccount = new Bundle();
	 	     		SelectedAccount.putString("account", fields[0]);
	 	     		SelectedAccount.putString("money", fields[1]);
	 	     		SelectedAccount.putString("act_id", fields[2]);
	 	     		SelectedAccount.putString("com_id", fields[3]);
	 	     		gotoAccountDetail.putExtras(SelectedAccount);
	 	 		
	 			}
 			gotoAccountDetail.setClass(MyAccount.this, AccountDetail.class);
 			startActivity(gotoAccountDetail);
 			finish();
 			}
 		}
 	};
 	
 	//deal with scrollview+listview
 	
 	public void setListViewHeightBasedOnChildren(ListView listView) {   
        // 获取ListView对应的Adapter   
        ListAdapter listAdapter = listView.getAdapter();   
        if (listAdapter == null) {   
            return;   
        }   
   
        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {   
            // listAdapter.getCount()返回数据项的数目   
            View listItem = listAdapter.getView(i, null, listView);   
            // 计算子项View 的宽高   
            listItem.measure(0, 0);    
            // 统计所有子项的总高度   
            totalHeight += listItem.getMeasuredHeight();    
        }   
   
        ViewGroup.LayoutParams params = listView.getLayoutParams();   
        params.height = totalHeight+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));   
        // listView.getDividerHeight()获取子项间分隔符占用的高度   
        // params.height最后得到整个ListView完整显示需要的高度   
        listView.setLayoutParams(params);   
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
 	
 	class CardInfo
	{
		public int img;
		
		
		CardInfo(int i)
		{
			img = i;
			
		}
	}
 	
 	@Override  
    protected void onRestart() {  
        super.onRestart();  
        profile = getSharedPreferences("Profile",0);
		boolean hasPassCode = profile.getBoolean("hasPassCode", false);
		if(hasPassCode){
        Intent gotoMenu = new Intent();
		gotoMenu.setClass(MyAccount.this, Passcode.class);
		startActivity(gotoMenu);
		}
    }
}
