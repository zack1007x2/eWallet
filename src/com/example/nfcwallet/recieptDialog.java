package com.example.nfcwallet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


public class recieptDialog extends DialogFragment {
	static recieptDialog newInstance() {
        return new recieptDialog();
    }
	
	private AlertDialog mAlertDialog;
//	private TextView tvDate,tvMessage;
	private String ter_address;
	
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

    
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.reciept_dialog, null, false);
        
        final TextView tvName = (TextView)view.findViewById(R.id.tvName);
        final TextView tvPrice = (TextView)view.findViewById(R.id.tvPrice);
        final TextView tvTer_name = (TextView)view.findViewById(R.id.tvTer_name);
        final TextView tvTer_address = (TextView)view.findViewById(R.id.tvTer_address);
        final ListView select = (ListView) view.findViewById(R.id.lvMap); 
        String[] Opt= new String[]{"                                                   查看地圖"};
        ArrayAdapter<String> selectadapter= new  ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,Opt);
        select.setAdapter(selectadapter);
        select.setOnItemClickListener(listener);
        
		mAlertDialog = new AlertDialog.Builder(getActivity())
     	.setTitle(R.string.reciept_frg)
     	.setView(view)
     	.setNegativeButton(R.string.goback, new DialogInterface.OnClickListener () {
             public void onClick(DialogInterface dialog, int which) {
             }
		}).create();
				 
		
		Bundle ri=getArguments();
		String name = ri.getString("name");
		String price = ri.getString("price");
		String ter_name = ri.getString("ter_name");
		ter_address = ri.getString("ter_address");
		
		
		tvName.setText(name);
		tvPrice.setText(price);
		tvTer_name.setText(ter_name);
		tvTer_address.setText(ter_address);
		return mAlertDialog;
		
		
	}
	
	private ListView.OnItemClickListener listener = new ListView.OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
//			Uri uri = Uri.parse("https://www.google.com.tw/maps/preview#!q="+ter_address);
//			System.out.println("https://www.google.com.tw/maps/preview#!q="+ter_address);
//			Intent it = new Intent(Intent.ACTION_VIEW, uri);
//			startActivity(it);
			
			Uri uri = Uri.parse("https://maps.google.com.tw/maps?f=q&hl=zh-TW&geocode=&q="+ter_address+"&z=16&output=embed&t=");
			System.out.println("https://maps.google.com.tw/maps?f=q&hl=zh-TW&geocode=&q="+ter_address+"&z=16&output=embed&t=");
			Intent it = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(it);	
		}
	};
}
