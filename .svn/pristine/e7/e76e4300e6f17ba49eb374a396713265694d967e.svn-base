package com.mooneylabs.android.dailyscripture;
import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.*;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Justin
 * Date: 3/7/13
 * Time: 9:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class AlarmSetterOnBoot extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

//        CharSequence text = "daily scripture on boot";
//        int duration = Toast.LENGTH_LONG;
//
//        Toast toast = Toast.makeText(context, text, duration);
//        toast.show();

        SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean mIsAlarmSet = appPreferences.getBoolean(context.getString(R.string.pref_key_daily_reminder), false);

        if(!mIsAlarmSet) return;

        int minute = appPreferences.getInt(context.getString(R.string.pref_key_daily_reminder_time) + ".minute", 0);
        int hour = appPreferences.getInt(context.getString(R.string.pref_key_daily_reminder_time) + ".hour", 0);

        this.setAlarm(context, hour, minute);
    }

    final public static String ONE_TIME = "onetime";
    private void setAlarm(Context context, int hour, int minute){
        /*
        the following three lines set the time in milliseconds of the day
        correctly based on the users time.
         */
        long hourMillis = hour * 3600000;
        long minuteMillis = minute * 60000;
        long millis = hourMillis + minuteMillis;

        /*
        at 5:52 PM getTimeInMillis returns 1,358,380,341,435
         */
        Calendar cal = Calendar.getInstance();
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);
        int currentMin = cal.get(Calendar.MINUTE);

        //set the alarm time in the calendar
        if(hour > 12) cal.set(Calendar.AM_PM, Calendar.PM);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.HOUR_OF_DAY, hour);

        /*
        only add a day

        if the notification time is in the past of today then make it go tomorrow
         */
        if(currentHour == hour){
            if(currentMin >= minute)
                cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        else if(currentHour >= hour)
            cal.add(Calendar.DAY_OF_YEAR, 1);

        millis = cal.getTimeInMillis();

        cal.set(Calendar.HOUR_OF_DAY, 24);
        cal.set(Calendar.MINUTE, 0);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        intent.putExtra(ONE_TIME, Boolean.FALSE);

        //set the action to be performed when the alarm goes off
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //After after 3 minutes = 180,000
        am.setRepeating(AlarmManager.RTC, millis, AlarmManager.INTERVAL_DAY, pi);

        String mZone = "AM";
        String sMinute = String.valueOf(minute);

        if(hour > 12){
            hour -= 12;
            mZone = "PM";
        }
        if(minute < 10){
            sMinute = "0" + minute; //add 0 to beg of minute
        }

//        CharSequence text = "Reminder set for: " + hour + ":" + sMinute + " " + mZone;
//        int duration = Toast.LENGTH_LONG;
//
//        Toast toast = Toast.makeText(context, text, duration);
//        toast.show();
//
//        Log.v("DS", "alarm set");
    }
}

