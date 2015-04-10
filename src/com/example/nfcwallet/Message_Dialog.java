package com.example.nfcwallet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


public class Message_Dialog extends DialogFragment {
	static Message_Dialog newInstance() {
        return new Message_Dialog();
    }
	
	private AlertDialog mAlertDialog;
//	private TextView tvDate,tvMessage;
	
	
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

    
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.message_dialog, null, false);
        
        final TextView tvDate = (TextView)view.findViewById(R.id.tvDate);
        final TextView tvMessage = (TextView)view.findViewById(R.id.tvMessage);
        
		mAlertDialog = new AlertDialog.Builder(getActivity())
     	.setTitle(R.string.message_frg)
     	.setView(view)
     	.setNegativeButton(R.string.goback, new DialogInterface.OnClickListener () {
             public void onClick(DialogInterface dialog, int which) {
             }
		}).create();
				 
		
		//////////////
		Bundle ci=getArguments();
		String info=ci.getString("info");
		String date=ci.getString("date");  
		
		tvDate.setText(date);
		tvMessage.setText(info);
		return mAlertDialog;
		
		
	}
}