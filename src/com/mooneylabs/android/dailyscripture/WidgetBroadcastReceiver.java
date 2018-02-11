package com.mooneylabs.android.dailyscripture;

/**
 * Created with IntelliJ IDEA.
 * User: Justin
 * Date: 3/1/13
 * Time: 5:29 PM
 * To change this template use File | Settings | File Templates.
 */

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RemoteViews;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Random;

public class WidgetBroadcastReceiver extends AppWidgetProvider {
    protected Context context;
    protected int[] allWidgetIds;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {// Get all ids
        this.context = context;

        ComponentName thisWidget = new ComponentName(context, WidgetBroadcastReceiver.class);

        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        this.allWidgetIds = allWidgetIds;

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.widget_layout);
        Log.w("WidgetExample", String.valueOf(289078));
        // Set the text
        remoteViews.setTextViewText(R.id.textView_widget_verse, String.valueOf(289078));
        remoteViews.setTextViewText(R.id.textView_widget_book, String.valueOf(289078));
        remoteViews.setTextViewText(R.id.textView_widget_date, String.valueOf(289078));


        // Build the intent to call the service
        Intent intent = new Intent(context.getApplicationContext(),
                UpdateWidgetViews.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

        // Update the widgets via the service
        context.startService(intent);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);    //To change body of overridden methods use File | Settings | File Templates.
    }
}

