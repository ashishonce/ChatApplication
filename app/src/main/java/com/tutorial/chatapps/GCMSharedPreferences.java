package com.tutorial.chatapps;

import android.content.BroadcastReceiver;

/**
 * Created by mibe on 7/25/2016.
 */
public class GCMSharedPreferences {

    public static final String SENT_TOKEN_TO_SERVER = "SENT_TOKEN_TO_SERVER";
    public static final String REG_SUCCESS = "REG_SUCCESS";
    public static final String SENT_UNREG_REQUEST_TO_SERVER = "SENT_UNREG_REQUEST_TO_SERVER";
    public static final String GOT_TOKEN_FROM_GCM = "GOT_TOKEN_FROM_GCM";
    public static final String REGISTRATION_COMPLETE = "REGISTRATION_COMPLETE";
    public static final String REG_ID = "";
    public static final boolean Init = true;
    public static BroadcastReceiver mRegistrationBroadcastReceiver;

}