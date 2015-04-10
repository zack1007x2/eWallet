package com.example.nfcwallet;

import com.example.nfcwallet.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class SaveDialogFragment extends DialogFragment {
	static SaveDialogFragment newInstance() {
        return new SaveDialogFragment();
    }
	
	private boolean hasText = false;
	private int selectedButton = -1;
	private AlertDialog mAlertDialog;
	
	/* (non-Javadoc)
	 * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

        
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.save_dialog, null, false);
        final EditText nameBox = (EditText) view.findViewById(R.id.saveNameEditText);        
        
		mAlertDialog = new AlertDialog.Builder(getActivity())
     	.setTitle(R.string.save_to_db)
     	.setView(view)
     	.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener () {

 			public void onClick(DialogInterface dialog, int which) {
 				String name = nameBox.getText().toString();	
 					((NFCProxyActivity)getActivity()).saveRun(name, DBHelper.REPLAY_TAG);
 				
			}	 			
		})
         .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener () {
             public void onClick(DialogInterface dialog, int which) {
             }
		}).create();
				 
//		mAlertDialog.setOnShowListener(new OnShowListener() {
//
//			@Override
//			public void onShow(DialogInterface dialog) {
//				mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
//				
//			}
//			
//		});


		nameBox.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() > 0 ) {
					hasText = true;
					if (selectedButton != -1) {
						mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
					}
				}
				else {
					hasText = false;
					mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
				}				
			}
        	
        });		
		
		return mAlertDialog;
	}
	
	void disableOK() {
		if (mAlertDialog != null) {
			mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);			
		}
	}

}
