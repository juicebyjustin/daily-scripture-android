package com.mooneylabs.android.dailyscripture;

/**
 * Created with IntelliJ IDEA.
 * User: justin
 * Date: 1/9/13
 * Time: 7:54 PM
 * To change this template use File | Settings | File Templates.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

/**
 * Source: http://www.twodee.org/weblog/?p=1037
 * Custom preference for time selection. Hour and minute are persistent and
 * stored separately as ints in the underlying shared preferences under keys
 * KEY.hour and KEY.minute, where KEY is the preference's key.
 */
/**
 * Custom preference for time selection. Hour and minute are persistent and
 * stored separately as ints in the underlying shared preferences under keys
 * KEY.hour and KEY.minute, where KEY is the preference's key.
 */
public class PreferenceTime extends DialogPreference {

    /** The widget for picking a time */
    private TimePicker timePicker;

    /** Default hour */
    private static final int DEFAULT_HOUR = 8;

    /** Default minute */
    private static final int DEFAULT_MINUTE = 0;

    /**
     * Creates a preference for choosing a time based on its XML declaration.
     *
     * @param context
     * @param attributes
     */
    public PreferenceTime(Context context,
                          AttributeSet attributes) {
        super(context, attributes);
        setPersistent(false);
    }

    /**
     * Initialize time picker to currently stored time preferences.
     *
     * @param view
     * The dialog preference's host view
     */
    @Override
    public void onBindDialogView(View view) {
        super.onBindDialogView(view);
        timePicker = (TimePicker) view.findViewById(R.id.prefTimePicker);
        timePicker.setCurrentHour(getSharedPreferences().getInt(getKey() + ".hour", DEFAULT_HOUR));
        timePicker.setCurrentMinute(getSharedPreferences().getInt(getKey() + ".minute", DEFAULT_MINUTE));
        //timePicker.setIs24HourView(DateFormat.is24HourFormat(timePicker.getContext()));

    }

    /**
     * Handles closing of dialog. If user intended to save the settings, selected
     * hour and minute are stored in the preferences with keys KEY.hour and
     * KEY.minute, where KEY is the preference's KEY.
     *
     * @param okToSave
     * True if user wanted to save settings, false otherwise
     */
    @Override
    protected void onDialogClosed(boolean okToSave) {
        super.onDialogClosed(okToSave);
        if (okToSave) {
            timePicker.clearFocus();
            SharedPreferences.Editor editor = getEditor();
            editor.putInt(getKey() + ".hour", timePicker.getCurrentHour());
            editor.putInt(getKey() + ".minute", timePicker.getCurrentMinute());
            editor.commit();
        }
    }
}