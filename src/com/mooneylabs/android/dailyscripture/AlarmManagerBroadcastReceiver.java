package com.mooneylabs.android.dailyscripture;

import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: justin
 * Date: 1/14/13
 * Time: 6:33 PM
 * Sourced from: I lost the link :'( :'(
 */
public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {

    final public static String ONE_TIME = "onetime";
    int mId = 1;
    Context context;
    Verse verse;

    @Override
    public void onReceive(Context context, Intent intent) {
        try{
            /*
            get the current time to see what time the notification was fired
             */
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR);
            int minute = cal.get(Calendar.MINUTE);

            if(hour < 12) hour += 12;

            //milliseconds per hour: 3600000
            //milliseconds per minu: 60000
            //long milli = hour * 3600000 + minute * 60000;
            long milli = cal.getTimeInMillis();

            /**
             * get the users langauage
             */

            /**
             * get the verse from the website
             */
            Intent serviceIntent = new Intent(context, DailyScriptureFetcher.class);
            Messenger messenger = new Messenger(handler);
            serviceIntent.putExtra("MESSENGER", messenger);

            this.context = context;
            this.context.startService(serviceIntent);

            Log.v("Daily", "service created in notification receiver");
        }
        catch(Exception ex){
            Log.v("Ex", "Could not notify..." + ex.toString());
        }
        /*
        end the notification code
         */
    }

    /**
     * do service stuff
     * @param text
     * @param duration
     */
    public Handler handler = new Handler(){
        public void handleMessage(Message message){
            Verse vrs = (Verse)message.obj;
            if(message.arg1 == Activity.RESULT_OK && vrs != null){
                createNotification(vrs);
                Log.v("Daily", "notification created");
            }
        };
    };

    protected void createNotification(Verse vrs){
        this.verse = vrs;

        if(this.verse.book.equals("failed to get")){
            this.verse.book = "Time for Your Daily Bread";
            this.verse.verse = "connect to the internet";
        }




        //build notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this.context)
                        .setSmallIcon(R.drawable.ic_stat_notification)
                        .setContentTitle(this.verse.book)
                        .setContentText(this.verse.verse)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(this.verse.verse))
                        .setAutoCancel(true);
        mBuilder.setDefaults(Notification.FLAG_AUTO_CANCEL);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, MyActivity.class);
        resultIntent.setAction(Intent.ACTION_SEND);

        /**
         * put verse information into the intent
         */
        resultIntent.putExtra("verse", this.verse.verse);
        resultIntent.putExtra("book", this.verse.book);
        resultIntent.putExtra("thoughts", this.verse.thoughts);
        resultIntent.putExtra("prayer", this.verse.prayer);
        resultIntent.putExtra("date", this.verse.textDate);
        /**
         * end the information into the intent
         */

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MyActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(956956, mBuilder.build());
    }

    public void SetAlarm(Context context, int hour, int minute) {
        //long millis = 1000 * (long) ((hour * 60 * 60) + (minute * 60));

        /*
        the following three lines set the time in milliseconds of the day
        correctly based on the users time.
         */
        long hourMillis = hour * 3600000;
        long minuteMillis = minute * 60000;
        long millis = hourMillis + minuteMillis;

        long millisPerDay = 86400000;


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

        if(hour >= 12){
            if(hour > 12)
                hour -= 12;
            mZone = "PM";
        }
        if(mZone == "AM"){
            if(hour == 0)
                hour = 12;
        }


        if(minute < 10){
            sMinute = "0" + minute; //add 0 to beg of minute
        }

        CharSequence text = "Reminder set for: " + hour + ":" + sMinute + " " + mZone;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        Log.v("DS", "alarm set");
    }

    public void CancelAlarm(Context context) {
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    public void setOnetimeTimer(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        intent.putExtra(ONE_TIME, Boolean.TRUE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pi);
    }
}
