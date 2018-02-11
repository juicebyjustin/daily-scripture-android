package com.mooneylabs.android.dailyscripture;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Justin
 * Date: 3/1/13
 * Time: 6:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateWidgetViews extends Service {
    private String LOG = "Daily";
    private Context context;

    @Override
    public void onStart(Intent intent, int startId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
                .getApplicationContext());

        int[] allWidgetIds = intent
                .getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

        this.context = getApplicationContext();

        ComponentName thisWidget = new ComponentName(this.context,
                WidgetBroadcastReceiver.class);
        int[] allWidgetIds2 = appWidgetManager.getAppWidgetIds(thisWidget);
        Log.w(LOG, "From Intent" + String.valueOf(allWidgetIds.length));
        Log.w(LOG, "Direct" + String.valueOf(allWidgetIds2.length));

        for (int widgetId : allWidgetIds) {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            this.setVerseObject(year, month, day);

            /*
            RemoteViews remoteViews = new RemoteViews(this
                    .getApplicationContext().getPackageName(),
                    R.layout.widget_layout);

            String textDate = intent.getStringExtra("date");
            String verse = intent.getStringExtra("verse");
            String book = intent.getStringExtra("book");

            Log.v(LOG, "Updating widget id: " + widgetId);

            *//***
             * set the text view if the context is not null, else don't
             *//*
            if(this.getApplicationContext() != null){
                remoteViews.setTextViewText(R.id.textView_widget_date, textDate);
                remoteViews.setTextViewText(R.id.textView_widget_book, book);
                remoteViews.setTextViewText(R.id.textView_widget_verse, verse);

                Log.v("Daily", "set the verse details in the widget: " + widgetId);
            }
            else{
                Log.v("Daily", "context is null not updating the verse details: " + widgetId);
            }*/
        }
        stopSelf();

        super.onStart(intent, startId);
    }

    /**
     * set the verse. will set language from the settings
     */
    public void setVerseObject(int year, int month, int day){
        SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        String lang = appPreferences.getString(this.context.getString(R.string.pref_key_language), null);

        //create the verse with the calendar if calendar has been changed
        Verse verse = null;
        verse = new Verse(year, month, day, lang);

        downloadVerse(verse);
    }

    /***
     * download the verse of the given date
     * @return
     */
    protected Verse downloadVerse(Verse verse) {
        Intent intent = new Intent(context, DailyScriptureFetcher.class);

        //need to subtract one from the month when getting it because it adds one because of how datetime works in java
        int iMonth = Integer.parseInt(verse.getMonth()) - 1;
        //craete messenger
        Messenger messenger = new Messenger(handler);
        intent.putExtra("MESSENGER", messenger);
        intent.putExtra("year", Integer.parseInt(verse.getYear()));
        intent.putExtra("month", iMonth);
        intent.putExtra("day", Integer.parseInt(verse.getDay()));
        intent.putExtra("lang", verse.getLanguage());

        context.startService(intent);

        Log.v("Daily", "called start service from widget");

        return verse;
    }

    /**
     * do service stuff
     * @param text
     * @param duration
     */
    public Handler handler = new Handler(){
        public void handleMessage(Message message){
            Verse vrs = (Verse)message.obj;
            if(message.arg1 == android.app.Activity.RESULT_OK && vrs != null){
                Log.v("Daily", "widget, set verse details of textviews");
            }
            else{
                vrs = new Verse(0, 0, 0, "");
                vrs.book = "";
                vrs.verse = "Error downloading today's scripture...";
                vrs.textDate = "";
            }

            setVerseDetails(vrs);
        };
    };

    public void setVerseDetails(Verse verse){
        //fill the layout details

        /***
         * set the text view if the context is not null, else don't
         */
        if(context != null){
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
          //  remoteViews.setTextViewText(R.id.textView_widget_date, verse.textDate);
          //  remoteViews.setTextViewText(R.id.textView_widget_book, verse.book);
          //  remoteViews.setTextViewText(R.id.textView_widget_verse, verse.verse);
        }
        else{
            Log.v("Daily", "context is null not updating the verse details");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
