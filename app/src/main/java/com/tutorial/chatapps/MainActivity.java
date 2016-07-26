package com.tutorial.chatapps;

import android.app.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import android.widget.Spinner;
import android.widget.ViewSwitcher;

import java.util.Date;
import java.util.List;
import java.util.Vector;


public class MainActivity extends Activity {
	  
    private static final String TAG = "ChatActivity";

    private ChatArrayAdapter adp;
    private TextView currentuser;
    private ListView list;
    private EditText chatText;
    private Button send;
    private LinearLayout suggestions;
    private ImageView suggestionsImage;
    private TextSwitcher textSwitcher;
    private Vector<String> suggestionList;
    private int index;
    private int size;
    private String suggestedText;

    Intent intent;
    private boolean side = false;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter("MyCustom"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        setContentView(R.layout.main);
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
               receiveChatMessage(intent.getStringExtra("Message"));
            }
        };

        startRegistrationService(true, false);
        currentuser = (TextView)findViewById(R.id.textView1);
        currentuser.setText("Anonymous");
        currentuser.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        suggestionsImage = (ImageView)findViewById(R.id.imageView2);

        send = (Button) findViewById(R.id.btn);
        list = (ListView) findViewById(R.id.listview);
        adp = new ChatArrayAdapter(getApplicationContext(), R.layout.chat);
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

        suggestions = (LinearLayout) findViewById(R.id.suggestionsView);

        textSwitcher = (TextSwitcher) findViewById(R.id.switcher);

        suggestionList = new Vector<String>();

        this.initializeTextSwitcher();

        Vector<String> temp = new Vector<String>();
        temp.add("Text Content 1");
        temp.add("Text Content 2");
        temp.add("Text Content 3");
        temp.add("Text Content 4");

        this.setSuggestionTextItems(temp);

    }

    public void startRegistrationService(boolean reg, boolean tkr) {

        if (GCMCommonUtils.checkPlayServices(this)) {
            Toast.makeText(this, "Background service started...", Toast.LENGTH_LONG).show();
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, MyGCMRegistrationIntentService.class);
            intent.putExtra("register", reg);
            intent.putExtra("tokenRefreshed", tkr);
            startService(intent);
        }

    }

    private boolean sendChatMessage(){
        adp.add(new ChatMessage(false, chatText.getText().toString()));

        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    // Create a new HttpClient and Post Header
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(GCMCommonUtils.SERVER_URL);
                    // Add your data
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                    nameValuePairs.add(new BasicNameValuePair("UserID", GCMCommonUtils.SenderID));
                    nameValuePairs.add(new BasicNameValuePair("Name", chatText.getText().toString()));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HTTP Post Request
                    HttpResponse response = httpclient.execute(httppost);

                    Log.e("POSTSEND",response.toString());

                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
        thread.start();
        chatText.setText("");
        return true;
    }

    public boolean receiveChatMessage(String message){
        adp.add(new ChatMessage(true, message ));
        return true;
    }

    private void setSuggestionTextItems(Vector<String> textSuggestions){
        this.index = 0;
        this.suggestionList.addAll(textSuggestions);
        size = this.suggestionList.size();
        if (size > 0){
            textSwitcher.setText(suggestionList.elementAt(index));
            suggestedText = suggestionList.elementAt(index);
        }
    }

    private void initializeTextSwitcher(){
        size = 0;
        textSwitcher = (TextSwitcher) findViewById(R.id.switcher);

        textSwitcher.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                TextView myText = new TextView(MainActivity.this);
                myText.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                myText.setTextSize(24);
                myText.setTextColor(Color.WHITE);
                return myText;
            }
        });

        textSwitcher.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this){
            public void onSwipeRight(){
                textSwitcher.setInAnimation(Slide.inFromLeftAnimation());
                textSwitcher.setOutAnimation(Slide.outToRightAnimation());

                if (size > 0){
                    index--;
                    index = index < 0 ? size - 1 : index;
                    textSwitcher.setText(suggestionList.elementAt(index));
                    suggestedText = suggestionList.elementAt(index);
                }
            }

            public void onSwipeLeft(){
                textSwitcher.setInAnimation(Slide.inFromRightAnimation());
                textSwitcher.setOutAnimation(Slide.outToLeftAnimation());

                if (size > 0){
                    index++;
                    index = index % size;
                    textSwitcher.setText(suggestionList.elementAt(index));
                    suggestedText = suggestionList.elementAt(index);
                }
            }

            public void onClick(){
                chatText.setText(suggestedText);
            }
        });
    }

    private void hideSuggestions(){
        suggestionsImage.setVisibility(View.INVISIBLE);
        suggestions.setVisibility(View.INVISIBLE);
    }

    private void showSuggestions(){
        suggestionsImage.setVisibility(View.VISIBLE);
        suggestions.setVisibility(View.VISIBLE);
    }

    private void deleteSuggestions(){
        suggestionList.removeAllElements();
    }
}