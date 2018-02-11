package com.mooneylabs.android.dailyscripture;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.preference.Preference;
import android.util.Log;
import com.actionbarsherlock.app.ActionBar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockPreferenceActivity;

import com.actionbarsherlock.view.MenuItem;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: justin
 * Date: 1/1/13
 * Time: 9:19 PM
 */

/*
I used the code from this question at stack overflow:
        http://stackoverflow.com/a/11336098/814891

Works all the way to 2.1
 */
public class PreferencesActivity extends SherlockPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    protected Method mLoadHeaders = null;
    protected Method mHasHeaders = null;

    private String beforeLanguage;
    private String beforeTheme;

    /**
     * Checks to see if using new v11+ way of handling PrefFragments.
     * @return Returns false pre-v11, else checks to see if using headers.
     */
    public boolean isNewV11Prefs() {
        if (mHasHeaders!=null && mLoadHeaders!=null) {
            try {
                return (Boolean)mHasHeaders.invoke(this);
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
        }
        return false;
    }

    @Override
    public void onCreate(Bundle aSavedState) {
        /**
         * set the theme based on preferences
         */
        SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = appPreferences.getString(this.getString(R.string.pref_key_theme), null);

        this.beforeTheme = theme;

        this.setTheme();
        /*
        end setting theme
         */

        //onBuildHeaders() will be called during super.onCreate()
        try {
            mLoadHeaders = getClass().getMethod("loadHeadersFromResource", int.class, List.class );
            mHasHeaders = getClass().getMethod("hasHeaders");
        } catch (NoSuchMethodException e) {
        }
        super.onCreate(aSavedState);
        if (!isNewV11Prefs()) {
            addPreferencesFromResource(R.xml.preferences);
        }

        /**
         * set action bar as up
         *
         */
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        /**
         *
        set the currently selected langauge
         */
        this.beforeLanguage = this.getLanguage();

        /**
         * onclicklistener
         */
        //link for about
        Preference aboutPref = findPreference(getString(R.string.pref_key_about));
        aboutPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getBaseContext(), AboutActivity.class));
                return true;
            }
        });

        appPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * set the theme for the activity
     */
    protected void setTheme(){
        String theme = getAppTheme();

        if(theme.equals(getString(R.string.pref_theme_holo_dark)))
            this.setTheme(com.actionbarsherlock.R.style.Theme_Sherlock);
        else
            this.setTheme(com.actionbarsherlock.R.style.Theme_Sherlock_Light_DarkActionBar);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
        Log.v("Daily", key);

        String keyTime = getString(R.string.pref_key_daily_reminder_time);
        String keyHour = getString(R.string.pref_key_daily_reminder_time);
        String keyMinute = getString(R.string.pref_key_daily_reminder_time);
        String keyTheme = getString(R.string.pref_key_theme);

        int minute = sharedPreferences.getInt(keyMinute + ".minute", 0);
        int hour = sharedPreferences.getInt(keyMinute + ".hour", 0);

        boolean mIsAlarmSet = sharedPreferences.getBoolean(getString(R.string.pref_key_daily_reminder), false);

                /* this is a double notification, one for when minute sets and another for when hour sets */
        if(key.equals(keyTime + ".hour") || key.equals(keyTime + ".minute")){
            /**
             * set the alarm
             */

            /**
             * alarm manager stuff
             */

            if(! mIsAlarmSet) return;

            AlarmManagerBroadcastReceiver alarm;

            alarm = new AlarmManagerBroadcastReceiver();
            Context context = getApplicationContext();

            if(alarm != null){
                alarm.SetAlarm(getBaseContext(), hour, minute);
            }else{
                Toast.makeText(context, "Failed to set reminder.", Toast.LENGTH_SHORT).show();
            }
        }
        if(key.equals(getString(R.string.pref_key_daily_reminder))){

            AlarmManagerBroadcastReceiver alarm;
            alarm = new AlarmManagerBroadcastReceiver();

            if(!mIsAlarmSet){
                alarm.CancelAlarm(getBaseContext());
                Toast.makeText(getBaseContext(), "Reminder is canceled.", Toast.LENGTH_SHORT).show();
            }
            else{
                alarm.SetAlarm(getBaseContext(), hour, minute);
            }
        }

        if(key.equals(keyTheme)){
            String theme = sharedPreferences.getString(getString(R.string.pref_key_theme), "");

            /**
             * if user changes theme and changes back with no restart, they will not be asked to
             * restart again because the theme will not change
             */
            if(!theme.equals(this.beforeTheme)){
                try{
                    this.restartApp();
                }
                catch(Exception ex){
                    Log.v("Daily", "Failed at setting theme in setting. Ex: " + ex);
                }
            }
        }
    }

    /**
     * get the language out of settings
     */
    private String getLanguage(){
        SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return appPreferences.getString(this.getString(R.string.pref_key_language), null);
    }

    /**
     * get the theme out of settings
     */
    private String getAppTheme(){
        SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return appPreferences.getString(this.getString(R.string.pref_key_theme), null);
    }

    /**
     * ask to restart the app when the theme changes.
     */
    private void restartApp(){
        //source: http://www.mkyong.com/android/android-alert-dialog-example/
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle("Restart Required");

        // set dialog message
        alertDialogBuilder
                .setMessage("We need to restart to set the app theme Do you want to restart Daily Scripture?")
                .setCancelable(false)
                .setPositiveButton("Restart",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    @Override
    public void onBuildHeaders(List<Header> aTarget) {
        try {
            mLoadHeaders.invoke(this,new Object[]{R.xml.preferences,aTarget});
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                /*if(this.beforeLanguage == this.getLanguage()){
                    super.onBackPressed();
                }
                else{ //user changed their selected language
                    startActivity(new Intent(PreferencesActivity.this, MyActivity.class)
                          .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

                }*/
                this.onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(this.beforeLanguage != this.getLanguage()){
//                ||
//           this.beforeTheme != this.getAppTheme()){
            //user changed their selected language
            startActivity(new Intent(PreferencesActivity.this, MyActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }

        super.onBackPressed();    //To change body of overridden methods use File | Settings | File Templates.
    }
}