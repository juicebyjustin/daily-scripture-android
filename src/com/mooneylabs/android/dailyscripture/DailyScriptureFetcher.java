package com.mooneylabs.android.dailyscripture;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: Justin
 * Date: 2/13/13
 * Time: 5:30 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * This fetches the daily scripture.
 * TODO: search in the database first for the scripture. Say they open app multiple times in a single day.
  */
public class DailyScriptureFetcher extends IntentService {
    public Verse verse;
    private int result = Activity.RESULT_CANCELED;
    //public HiddenPreferences hiddenPreferences;

    public DailyScriptureFetcher(){
        super("DailyScriptureFetcher");

        //hiddenPreferences = new HiddenPreferences();
        //hiddenPreferences.setAppLaunches(15);
    }

    @Override
    protected void onHandleIntent(Intent intent){
        Calendar cal = Calendar.getInstance();

        /*
        get the date and language for what the user wants to see
        defaults to today if an error occurs
         */
        int year = intent.getIntExtra("year", cal.get(Calendar.YEAR));
        int month = intent.getIntExtra("month", cal.get(Calendar.MONTH));
        int day = intent.getIntExtra("day", cal.get(Calendar.DAY_OF_MONTH));


        SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = "";
        lang = appPreferences.getString(this.getString(R.string.pref_key_language), null);

        if(lang.equals("")){
            lang = "en";
        }

        verse = new Verse(year, month, day, lang);
        try{
            this.verse.downloadAndParseHtmlSource(); //download verse
            result = Activity.RESULT_OK; //finished good
        }
        catch(Exception e){
            Log.v("Daily", "failed to download verse. Ex: " + e.toString());
        }

        Bundle extras = intent.getExtras();

        //return the verse object
        if(extras != null){
            Messenger messenger = (Messenger) extras.get("MESSENGER");
            Message msg = Message.obtain();
            msg.arg1 = result;
            msg.obj = this.verse;
            try {
                messenger.send(msg);
            }
            catch (android.os.RemoteException e1) {
                Log.w("Daily", "Exception sending message", e1);
            }

        }
    }
}
