package com.tutorial.chatapps;

import java.text.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.CalendarContract;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class MainActivity extends Activity {

    enum CommandType {
        ContactCMD,
        CalendarCMD,
        MapCMD,
    }

    private static final String TAG = "ChatActivity";

    private ChatArrayAdapter adp;
    private TextView currentuser;
    private ImageView suggestionsImage;

    private ListView list;
    private ViewGroup.LayoutParams listParams;
    private EditText chatText;
    private Button send;
    private RelativeLayout suggestions;
    private TextSwitcher textSwitcher;
    private Vector<String> suggestionList;
    private int index;
    private int size;
    private String suggestedText;
    private TextView leftArrow;
    private TextView rightArrow;

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

        SharedPreferences prefs = getSharedPreferences("PREFS_NAME", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();


        currentuser = (TextView) findViewById(R.id.textView1);
        currentuser.setText(prefs.getString("senderID", "New"));
        currentuser.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        suggestionsImage = (ImageView) findViewById(R.id.imageView2);
        suggestionsImage.setVisibility(View.INVISIBLE);
        // initialization

        if (!prefs.getBoolean("firstTime", false)) {

            // mark first time has runned.
            LayoutInflater inflater = getLayoutInflater();
            final View dialoglayout = inflater.inflate(R.layout.register, null);

            new AlertDialog.Builder(this)
                    .setTitle("Register")
                    .setView(dialoglayout)
                    .setMessage("Please choose your destiny")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String clientID = ((EditText) dialoglayout.findViewById(R.id.UserID)).getText().toString();
                            String senderID = ((EditText) dialoglayout.findViewById(R.id.SenderID)).getText().toString();

                            SharedPreferences prefs = getSharedPreferences("PREFS_NAME", MODE_PRIVATE);

                            SharedPreferences.Editor preferencesEditor = prefs.edit();
                            preferencesEditor.putString("clientID", clientID);
                            preferencesEditor.putString("senderID", senderID);
                            preferencesEditor.commit();


                            Log.e("data:", clientID + " " + senderID);
                            currentuser.setText(senderID);
                            startRegistrationService(true, false);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            editor.putBoolean("firstTime", true);
            editor.commit();
        }

        send = (Button) findViewById(R.id.btn);
        list = (ListView) findViewById(R.id.listview);
        listParams = list.getLayoutParams();
        adp = new ChatArrayAdapter(getApplicationContext(), R.layout.chat);
        list.setAdapter(adp);

        chatText = (EditText) findViewById(R.id.chat_text);
        chatText.setBackgroundDrawable(getResources().getDrawable(R.drawable.back));
        chatText.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER) && chatText.getText().toString().length() > 0 ){
                    return sendChatMessage();
                }
                return false;
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(chatText.getText().toString().length() > 0)
                    sendChatMessage();
            }
        });

        list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        list.setAdapter(adp);

        adp.registerDataSetObserver(new DataSetObserver() {

            public void OnChanged() {
                super.onChanged();
                list.setSelection(adp.getCount() - 1);
            }


        });

        suggestions = (RelativeLayout) findViewById(R.id.suggestionsView);

        textSwitcher = (TextSwitcher) findViewById(R.id.switcher);

        leftArrow = (TextView) findViewById(R.id.leftArrow);

        rightArrow = (TextView) findViewById(R.id.rightArrow);

        suggestionList = new Vector<String>();
        this.initializeTextSwitcher();

        hideSuggestions();
    }

    public void startRegistrationService(boolean reg, boolean tkr) {

        if (GCMCommonUtils.checkPlayServices(this)) {
            Toast.makeText(this, "Registeration service started...", Toast.LENGTH_LONG).show();
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, MyGCMRegistrationIntentService.class);
            intent.putExtra("register", reg);
            intent.putExtra("tokenRefreshed", tkr);
            startService(intent);
        }

    }


    private boolean sendChatMessage() {
        adp.add(new ChatMessage(false, chatText.getText().toString()));
        final String msg = chatText.getText().toString();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferences prefs = getSharedPreferences("PREFS_NAME", MODE_PRIVATE);
                    ;

                    // Create a new HttpClient and Post Header
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(GCMCommonUtils.SERVER_URL);
                    // Add your data
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                    nameValuePairs.add(new BasicNameValuePair("UserID", prefs.getString("senderID", "Error")));
                    /// name has message her
                    nameValuePairs.add(new BasicNameValuePair("Name", msg));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HTTP Post Request
                    HttpResponse response = httpclient.execute(httppost);
                    Log.e("POST SEND", response.toString() + " to" + prefs.getString("senderID","Error") + "Msg = " + msg );

                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
        thread.start();
        chatText.setText("");

        return true;
    }

    public boolean receiveChatMessage(String message) {

        String[] receivedMsg = message.split("\\$");
        String chatMessage = receivedMsg[0];
        // try to get the command control if present in the message
        if (receivedMsg.length > 1) {
            Vector<String> temp = new Vector<String>();
            for (int i = 1; i < receivedMsg.length; i++) {
                temp.add(receivedMsg[i]);
            }

            suggestionList.clear();
            this.setSuggestionTextItems(temp);
            showSuggestions();
        } else {
            hideSuggestions();
        }



        if(message.split("\\~").length > 1)
        {
            // means some command text has come
            String[] receivedCommand = message.split("\\~");
            chatMessage = receivedCommand[0];
            String command = receivedCommand[1].split("\\:")[0];
            String commandValue = receivedCommand[1].split("\\:")[1];
            this.ParseLogic(command,commandValue);
            Log.i("command", command);
            Log.i("commandValue",commandValue);
        }

//        Log.i("message", message);
//        Log.i("split messag", receivedMsg[0]);

        if(chatMessage != "")
        {
            adp.add(new ChatMessage(true, chatMessage ));
        }



        return true;
    }

    private void ParseLogic(String cmd , String valueParam) {

        String suggestion = "";
        if( cmd.equals("Contact"))
        { String clean = valueParam.replaceAll("\\P{Print}", "");
            suggestion = valueParam + ":" + this.getPhoneNumber("Ashish", MainActivity.this);
            //chatText.setText(valueParam + ":" + contact);
        }
        else if(cmd.equals("Calendar"))
        {

            // we will hard code the daate for now.. also the calendar should be present
            suggestion = this.CalendarPicker(2016,06,27,00,00,00,2016,06,27,00,00,00,true);
        }

        if(!suggestion.equals(""))
        {
            Vector<String> temp = new Vector<String>();
            temp.add(suggestion);
            suggestionList.clear();
            this.setSuggestionTextItems(temp);
            showSuggestions();
        }
        else
        {
            hideSuggestions();
        }

    }

    public String CalendarPicker(int year, int month, int date, int hh, int mm,int ss,int endYear, int endMonth, int endDate, int endHH, int endMM,int endSS, Boolean day)
    {
        String eventName="";
        Calendar c_start= Calendar.getInstance();
        //c_start.set(2016,6,26,19,0,0); //Note that months start from 0 (January)
        Calendar c_end= Calendar.getInstance();
        // c_end.set(2016,6,26,19,30,0);
        String[] projection = new String[] { "calendar_id", "title", "description",
                "dtstart", "dtend", "eventLocation" };
        String selection ="";

        if(day)
        {
            c_start.set(year,month,date,hh,mm,ss);
            c_end.set(year,month,date,23,59,59);
        }
        else{
            c_start.set(year,month,date,hh,mm,ss);
            c_end.set(endYear,endMonth,endDate,endHH,endMM,endSS);

        }

        long startTime = c_start.getTimeInMillis()-999;
        long endTime= c_end.getTimeInMillis()+999;
        selection = "(("+ CalendarContract.Events.DTSTART+" >= "+startTime+") AND ("+ CalendarContract.Events.DTEND +"<= "+endTime +"))";

        try {
            Cursor cursor = getContentResolver().query( Uri.parse("content://com.android.calendar/events") ,
                    projection,
                    selection,
                    null,
                    null);
            if (cursor.getCount() > 0) {
                if(!day && cursor.moveToFirst())
                    eventName= cursor.getString(1);
                else {
                    while (cursor.moveToNext()) {
//                        Date startdate = new Date(Long.valueOf(cursor.getString(3)));
                        String startdate = new SimpleDateFormat("H:mm").format(new Date(Long.valueOf(cursor.getString(3))));
                        String enddate = new SimpleDateFormat("H:mm").format(new Date(Long.valueOf(cursor.getString(4))));
                        //Date enddate = new Date(Long.valueOf(cursor.getString(4)));

                        eventName += cursor.getString(1) + ": " + startdate + "-" + enddate  + "\n";

                    }
                }


            }
            cursor.close();
        }
        catch(Exception ex)
        {
            return "Sumit's BirthDay : 27-07-2016";
        }
        finally{

            return eventName;
        }
    }



    private void showContactList(String name, Context context) {
        String phoneNumber = getPhoneNumber(name, context);
    }

    public String getPhoneNumber(String name, Context context) {
        String ret = null;

        //String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "='" + name+"'";
        String selection = "(("+ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ='" + name + "') OR"
               +"("+ ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " ='" + name + "') OR" +
                "("+ ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_ALTERNATIVE + " ='" + name + "'))";
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
        try {
            Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection, selection, null, null);
            if (c.moveToFirst()) {
                ret = c.getString(0);
            }
            c.close();
            if (ret == null)
                ret = "";
        } catch (Exception ex) {
            ret = "";
        } finally {
            return ret;
        }
    }


    private void setSuggestionTextItems(Vector<String> textSuggestions) {
        this.index = 0;
        this.suggestionList.addAll(textSuggestions);
        rightArrow.setVisibility(View.INVISIBLE);
        leftArrow.setVisibility(View.INVISIBLE);

        size = this.suggestionList.size();
        if (size > 0) {
            textSwitcher.setText(suggestionList.elementAt(index));
            suggestedText = suggestionList.elementAt(index);
        }

        if (size > 1) {
            rightArrow.setVisibility(View.VISIBLE);
        }
    }

    private void initializeTextSwitcher() {
        size = 0;
        textSwitcher = (TextSwitcher) findViewById(R.id.switcher);

        textSwitcher.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                TextView myText = new TextView(MainActivity.this);
                myText.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                myText.setTextSize(18);
                myText.setTextColor(Color.BLACK);
                return myText;
            }
        });

        textSwitcher.setMeasureAllChildren(false);

        textSwitcher.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
            public void onSwipeRight() {
                textSwitcher.setInAnimation(Slide.inFromLeftAnimation());
                textSwitcher.setOutAnimation(Slide.outToRightAnimation());

                index--;

                if (size > 0 && index >= 0) {
                    textSwitcher.setText(suggestionList.elementAt(index));
                    suggestedText = suggestionList.elementAt(index);
                    if (index == 0) leftArrow.setVisibility(View.INVISIBLE);
                    if (index < size - 1) rightArrow.setVisibility(View.VISIBLE);
                } else if (index < 0) {
                    hideSuggestions();
                } else {
                    index++;
                }
            }

            public void onSwipeLeft() {
                textSwitcher.setInAnimation(Slide.inFromRightAnimation());
                textSwitcher.setOutAnimation(Slide.outToLeftAnimation());

                index++;

                if (size > 0 && index < size) {
                    textSwitcher.setText(suggestionList.elementAt(index));
                    suggestedText = suggestionList.elementAt(index);
                    if (index == size - 1) rightArrow.setVisibility(View.INVISIBLE);
                    if (index > 0) leftArrow.setVisibility(View.VISIBLE);
                } else if (index == size) {
                    hideSuggestions();
                } else {
                    index--;
                }
            }

            public void onClick() {
                chatText.setText(suggestedText);
                if (size > 1){
                    if (index < size - 1){
                        textSwitcher.setText(suggestionList.elementAt(index + 1));
                        suggestedText = suggestionList.elementAt(index + 1);
                        suggestionList.removeElementAt(index);
                        size --;
                        if (index == size - 1) rightArrow.setVisibility(View.INVISIBLE);
                    }
                    else {
                        textSwitcher.setText(suggestionList.elementAt(index - 1));
                        suggestedText = suggestionList.elementAt(index - 1);
                        suggestionList.removeElementAt(index);
                        index--;
                        size--;
                        if (index == 0) leftArrow.setVisibility(View.INVISIBLE);
                    }
                }
                else {
                    hideSuggestions();
                }
            }
        });
    }

    private void hideSuggestions() {
        suggestionsImage.setVisibility(View.INVISIBLE);
        suggestions.setVisibility(View.GONE);
    }

    private void showSuggestions() {
        suggestionsImage.setVisibility(View.VISIBLE);
        suggestions.setVisibility(View.VISIBLE);
    }

    private void deleteSuggestions() {
        suggestionList.removeAllElements();
    }
}