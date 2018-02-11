package com.mooneylabs.android.dailyscripture;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * Created with IntelliJ IDEA.
 * User: Justin
 * Date: 2/13/13
 * Time: 6:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class AboutActivity extends SherlockFragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        /**
         * set the theme based on preferences
         */
        SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = appPreferences.getString(this.getString(R.string.pref_key_theme), null);

        if(theme.equals(getString(R.string.pref_theme_holo_dark)))
            this.setTheme(com.actionbarsherlock.R.style.Theme_Sherlock);
        else
            this.setTheme(com.actionbarsherlock.R.style.Theme_Sherlock_Light_DarkActionBar);
        /**
         * end setting theme
         */

        /**
         * set action bar as up
         *
         */
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.about);

        try{
            //java.io.FileInputStream in = openFileInput("assets/heartLightLogo.png");
            //ImageView iv = (ImageView)findViewById(R.id.imageViewHeartLightLogo);
            //iv.setImageAlpha(R.drawable.ic_launcher);
            //iv.setImageBitmap(BitmapFactory.decodeStream(in));

            TextView thanks = (TextView) findViewById(R.id.textViewThanks);
            thanks.setText(Html.fromHtml(getString(R.string.about_first_row)));
            thanks.setMovementMethod(LinkMovementMethod.getInstance());
        }
        catch(Exception ex){
            Log.v("Daily", "Failed to load image. Ex: " + ex.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.about_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;
            /*case R.id.menu_about_donate:
                *//**
                 * donate code goes here
                 *//*
                *//**
                 * intent to donate paypal
                 * url: http://paypal.com/sendmoney?email=jmooney5115@gmail.com
                 *//*
                String url = "http://paypal.com/sendmoney?email=jmooney5115@gmail.com";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);*/

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
