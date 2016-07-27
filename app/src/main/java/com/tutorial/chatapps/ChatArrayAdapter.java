package com.tutorial.chatapps;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChatArrayAdapter extends ArrayAdapter<ChatMessage>{
	private TextView chatText;
	private List<ChatMessage> MessageList = new ArrayList<ChatMessage>();
    private LinearLayout layout;
	
	
    public ChatArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}
	

	public void add(ChatMessage object) {
		// TODO Auto-generated method stub
		
		MessageList.add(object);
		super.add(object);
		
	}
	
	 public int getCount()
	 {
		return this.MessageList.size(); 
		 
	 }
	
	 public ChatMessage getItem(int index){



		return this.MessageList.get(index);


	 }

	  public View getView(int position,View ConvertView, ViewGroup parent){
		  
		   View v = ConvertView;
		   if(v==null){
			   
			  LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			   v =inflater.inflate(R.layout.chat, parent,false);
			   
			   
		   }
		  
		  layout = (LinearLayout)v.findViewById(R.id.Message1);
		  ChatMessage messageobj = getItem(position);
		  chatText =(TextView)v.findViewById(R.id.SingleMessage);
		  chatText.setText(messageobj.message);
		  chatText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		  LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				  LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		  layoutParams.setMargins(30, 20, 30, 0);
		  chatText.setLayoutParams(layoutParams);
		  chatText.setBackgroundResource(messageobj.left ? R.drawable.bubble_b : R.drawable.bubble_a);
		 // chatText.setBackgroundColor(messageobj.left ? Color.parseColor("#DCF2FA") : Color.parseColor("#BFE9F9"));
		   layout.setGravity(messageobj.left?Gravity.LEFT:Gravity.RIGHT);
		  
		return v;
		  
		  
	  }
	  
	  public Bitmap decodeToBitmap(byte[] decodedByte) {
			return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
		}
	  
	 }
