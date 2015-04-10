package com.example.nfcwallet;



import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;



public class CardDialogFragment extends DialogFragment {


	static CardDialogFragment newInstance() {
        return new CardDialogFragment();
    }
	
	private AlertDialog mAlertDialog;
	
	Bundle info;
	private String offer_act_id, offer_com_id;
	private SharedPreferences trans,plus;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		

    
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.card_dialog, null, false);
        
        
        final ListView select = (ListView) view.findViewById(R.id.lvSelect);  
        String[] Opt= new String[]{"                                                       查看" ,"                                                   搭配帳戶"};
        ArrayAdapter<String> selectadapter= new  ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,Opt);
        select.setAdapter(selectadapter);

        
        info=getArguments();
		offer_act_id = info.getString("act_id");
		offer_com_id = info.getString("com_id");
		
         
        select.setOnItemClickListener(listener);
        
		mAlertDialog = new AlertDialog.Builder(getActivity())
     	.setTitle(R.string.select_frg)
     	.setView(view)
     	.setNegativeButton(R.string.look_over, new DialogInterface.OnClickListener () {
             public void onClick(DialogInterface dialog, int which) {
            	 plus = getActivity().getSharedPreferences("Plus",0);
				 Editor editplus = plus.edit();
				 editplus.putString("offer_act_id",  offer_act_id);
				 editplus.putString("offer_com_id",  offer_com_id);
				 editplus.commit();
             }
		})
        .create();
		

		
        
				 	
		
		return mAlertDialog;
		
		
	}
	

	private ListView.OnItemClickListener listener = new ListView.OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
		
				switch(arg2){
			    case 0 :
			    	
			    	Message_Dialog cdf = Message_Dialog.newInstance();
					cdf.setArguments(info);
					cdf.show(getFragmentManager(), "message_dialog");
			    	
			                      
			      break;
			  
//			    case 1 : 
//			    	
//			    	Intent gotoCard = new Intent();
//			    	gotoCard.setClass(getActivity(), CardsView.class);
//	
//			    	plus = getActivity().getSharedPreferences("Plus",0);
//				    Editor editplus = plus.edit();
//				    editplus.putString("offer_act_id",  act_id);
//				    editplus.putString("offer_com_id",  act_id);
//				    editplus.commit();
//			    	
//			    	
//		    		startActivity(gotoCard);
//			    	
//    
//			      break;
			      
			      
			    case 1 : 
			    	Intent gotoImport = new Intent();
			    	gotoImport.setClass(getActivity(), MyAccount.class);
	
			    	trans = getActivity().getSharedPreferences("Trans",0);
				    Editor editor = trans.edit();
		    		editor.putString("offer_act_id",  offer_act_id);
		    		editor.putString("offer_com_id",  offer_com_id);
		    		editor.commit();
			    	
			    	
		    		startActivity(gotoImport);
			      break;
			  
	         
			    }
		
		}

		};
	


}