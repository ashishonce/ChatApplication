package com.tutorial.chatapps;

import android.app.Activity;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.Date;


public class MainActivity extends Activity {
	  
    private static final String TAG = "ChatActivity";

    private ChatArrayAdapter adp;
    private  ChatArrayAdapter adp0;
    private  ChatArrayAdapter adp1;
    private  ChatArrayAdapter adp2;
    private ListView list;
    private EditText chatText;
    private Button send;


    Intent intent;
    private boolean side = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        setContentView(R.layout.main);
        String[] items = new String[] { "Contact1", "Contact2", "Contact3" };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, items);
        Spinner dynamicSpinner = (Spinner) findViewById(R.id.dynamic_spinner);
        dynamicSpinner.setAdapter(adapter);

        dynamicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Log.v("item", (String) parent.getItemAtPosition(position));
                if (position == 0) {
                    adp = adp0;
                    list.setAdapter(adp0);
                }
                else if(position ==1) {
                    adp = adp1;
                    list.setAdapter(adp1);
                }
                else if(position == 2) {
                    adp = adp2;
                    list.setAdapter(adp2);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });


        send = (Button) findViewById(R.id.btn);

        list = (ListView) findViewById(R.id.listview);

        adp0 = new ChatArrayAdapter(getApplicationContext(), R.layout.chat);
        adp1 = new ChatArrayAdapter(getApplicationContext(), R.layout.chat);
        adp2 = new ChatArrayAdapter(getApplicationContext(), R.layout.chat);
        adp = adp0;
        list.setAdapter(adp);

        chatText = (EditText) findViewById(R.id.chat_text);
        chatText.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return sendChatMessage();
                }
                return false;
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendChatMessage();
            }
        });

        list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        list.setAdapter(adp);

        adp.registerDataSetObserver(new DataSetObserver() {
        	
        	public void OnChanged(){
        		
        		super.onChanged();
        		
        		list.setSelection(adp.getCount() -1);
        		
        		
        		
        	}
        	
        	
		});
        
    }

    private boolean sendChatMessage(){
        adp.add(new ChatMessage(true, chatText.getText().toString()));
        chatText.setText("");
        return true;
    }

    private boolean receiveChatMessage(){
        adp.add(new ChatMessage(false, chatText.getText().toString()));
        chatText.setText("");
        return true;
    }
}