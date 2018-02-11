package com.mooneylabs.android.dailyscripture;

import android.app.*;
import android.content.*;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.*;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;


import android.widget.*;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

//import com.handmark.pulltorefresh.library.PullToRefreshBase;
//import com.handmark.pulltorefresh.library.PullToRefreshScrollView;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.security.MessageDigest;
import java.util.*;

import java.security.*;

//TODO: android backup settings auto: http://developer.android.com/google/backup/index.html

public class MyActivity extends SherlockFragmentActivity implements DatePickerDialog.OnDateSetListener {

    Verse verse;
    Calendar calendar;

    int year;
    int month;
    int day;

    //shareing
    private ShareActionProvider mShareActionProvider;

    /*
    scroll view
     */
    /*PullToRefreshScrollView mPullRefreshScrollView;
    ScrollView mScrollView;  */
    boolean onRestore = true;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v("state", "onCreate called");




        /*
        set default values only one time.
        This will be called only onece. Stack overflow link:
        http://stackoverflow.com/questions/2874276/initialize-preferences-from-xml-in-the-main-activity
         */
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        /**
         * set the theme based on preferences
         */
        SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = appPreferences.getString(this.getString(R.string.pref_key_theme), null);
        String holoDark = getString(R.string.pref_theme_holo_dark);

        if(Build.VERSION.SDK_INT < 14){
            if(theme != null){
                if(theme.equals(holoDark))
                    setTheme(com.actionbarsherlock.R.style.Theme_Sherlock);
                else
                    setTheme(com.actionbarsherlock.R.style.Theme_Sherlock_Light_DarkActionBar);
            }
        }
        else{
            if(theme != null){
                if(theme.equals(holoDark))
                    setTheme(android.R.style.Theme_Holo);
                else
                    setTheme(android.R.style.Theme_Holo_Light_DarkActionBar);
            }
        }

        super.onCreate(savedInstanceState);
        try{
        setContentView(R.layout.main);
        }
        catch(Exception e){
            Log.v("Ex", e.toString());
        }

        /*
        restore the state of the application
         */
        if(savedInstanceState != null){
            this.onRestoreInstanceState(savedInstanceState);
        }

        /*
        set calendar
         */
        this.calendar = Calendar.getInstance();
        this.year = this.calendar.get(Calendar.YEAR);
        this.month = this.calendar.get(Calendar.MONTH);
        this.day = this.calendar.get(Calendar.DAY_OF_MONTH);

        /**
         * get the extra from the intent
         */
        getIntentExtras();

        /*
        set the date, this restores it
        if on restore, doesn't reload verse
         */
        if(this.verse == null){
            this.onDateSet(null, this.year, this.month,  this.day);
            this.onRestore = false;
        }
    }

    /**
     * get the language from the settings
     * @return
     */
    private String getLanguageFromSettings(){
        SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return appPreferences.getString(this.getString(R.string.pref_key_language), null);
    }

    private void getIntentExtras(){
        Intent receivedIntent = getIntent();

        if (!Intent.ACTION_SEND.equals(receivedIntent.getAction())) {
            return;
        }


        String textDate = receivedIntent.getStringExtra("date");
        String verse = receivedIntent.getStringExtra("verse");
        String thoughts = receivedIntent.getStringExtra("thoughts");
        String prayer = receivedIntent.getStringExtra("prayer");
        String book = receivedIntent.getStringExtra("book");

        if(book.equals("Time for Your Daily Bread")){
            return;
        }

        if(textDate.length() != 0){
            this.verse = new Verse(this.year, this.month, this.day,  this.getLanguageFromSettings());

            this.verse.textDate = textDate;
            this.verse.verse = verse;
            this.verse.thoughts = thoughts;
            this.verse.prayer = prayer;
            this.verse.book = book;

            this.onPostDownload(this.verse);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();

        //show a different menu because of the different iconds
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
            inflater.inflate(R.menu.main_menu_gingerbread, menu);
        }
        else{
            inflater.inflate(R.menu.main_menu, menu);
        }

        return super.onCreateOptionsMenu(menu);


//        //work with abs: http://stackoverflow.com/questions/10887929/only-four-options-for-shareactionprovider-with-actionbarsherlock
//        mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.menu_item_share).getActionProvider();
//
//        // If you use more than one ShareActionProvider, each for a different action,
//        // use the following line to specify a unique history file for each one.
//        // mShareActionProvider.setShareHistoryFileName("custom_share_history.xml");
//
//        // Set the default share intent
//        //don't do this here. wait until verse is ready
//        if(this.verse.verse != null) mShareActionProvider.setShareIntent(getDefaultShareIntent());
//
//        return true;
    }

    public Intent getDefaultShareIntent(){
        return BuildButtonDialog(verse);
    }

    /*
    credit: http://learnandroideasily.blogspot.com/2013/01/adding-radio-buttons-in-dialog.html
     */
    public Intent BuildButtonDialog(final Verse scripture){
        final AlertDialog levelDialog;
        final Intent sendIntent = new Intent();

        // Strings to Show In Dialog with Radio Buttons
        final CharSequence[] items = {" Scripture "," Scripture & Thoughts "," Everything "};
        //final Sequen bItems = { true, false, false };

        // Creating and Building the Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("What do you want to Share?");
        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String extraText = "";
                String option0 = "\"" + scripture.verse + "\" -" + scripture.book;
                String option1 = option0 + "\n\nThoughts:\n" + scripture.thoughts;
                String option2 = option0 + option1 + "\n\nPrayer:\n" + scripture.prayer;
                String copyRight = "\n\nAll Text © 1998-" +  Calendar.getInstance().get(Calendar.YEAR) + ", Heartlight, Inc.\n" +
                        "Provided by Daily Scripture on Android";

                switch(item)
                {
                    case 0:
                        extraText = option0;
                        break;
                    case 1:
                        extraText = option1;
                        break;
                    case 2:
                        extraText = option2;
                        break;
                }

                extraText += copyRight;

                dialog.dismiss();

                //sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, extraText);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });

        levelDialog = builder.create();

        levelDialog.show();

        return sendIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_choose_date:
                /*
                open a date picker to choose the date
                    1. get date
                    2. call setVerseObject();
                    3, display data
                 */
                this.showDatePicker();
                return true;
            case R.id.menu_view_on_web:
                /*
                open the web browser to the url for that date
                 */
                //this.setVerseObject();
                this.viewOnWeb();
                return true;
            case R.id.menu_settings:
                /*
                open preferences activity
                 */
                startActivity(new Intent(this, PreferencesActivity.class));
                return true;
            case R.id.menu_item_share:
                /*
                share the verse and book
                 */
                getDefaultShareIntent();
                return true;
            case R.id.menu_contact:
                String version = "";
                try{
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                version = "App Version: " + pInfo.versionName;
                }
                catch (Exception ex){ }

                /*
                 Source: http://www.androidsnippets.com/start-email-activity-with-preset-data-via-intents
                 */
                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                emailIntent.setType("plain/text");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{ getResources().getString(R.string.helpSenderAddress)});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.helpSubject));
                emailIntent.putExtra(Intent.EXTRA_TEXT, version);

                /* Send it off to the Activity-Chooser */
                startActivity(Intent.createChooser(emailIntent, "Contact via Email"));
                return true;
            case R.id.mene_rate:
                try{
                    //http://goo.gl/LpTYI
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=com.mooneylabs.android.dailyscripture"));
                    startActivity(intent);
                }
                catch(Exception ex){
                    Log.v("Daily", "trouble opening rate menu id. Ex: " + ex.toString());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showDatePicker(){
        Bundle b = new Bundle();
        b.putInt(DatePickerDialogFragment.YEAR, this.year);
        b.putInt(DatePickerDialogFragment.MONTH, this.month);
        b.putInt(DatePickerDialogFragment.DATE, this.day);
        DialogFragment picker = new DatePickerDialogFragment();
        picker.setArguments(b);
        picker.show(this.getSupportFragmentManager(), "fragment_date_picker");
    }


    /**
     * called when the date is set by the date picker and loads the scripture
     * @param year
     * @param month
     * @param day
     */
    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        if(year < 1998){
            //source: http://www.mkyong.com/android/android-alert-dialog-example/
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            // set title
            alertDialogBuilder.setTitle("Too Far Back");

            // set dialog message
            alertDialogBuilder
                .setMessage("VerseOfTheDay.com archives are not available before 1998.")
                .setCancelable(false)
                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                        // if this button is clicked, close
                        // current activity
                        //MyActivity.this.finish();
                    }
                })
                .setNegativeButton("Retry",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        //dialog.cancel();
                        showDatePicker();
                    }
                });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();

            return;
        }

        this.year = year;
        this.month = month;
        this.day = day;

        this.setVerseObject();
        //this.viewOnWeb();

        //download verse information
        this.doHtmlStuff();
    }

    /**
     * set the verse. will set language from the settings
     */
    public void setVerseObject(){
        SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = appPreferences.getString(this.getString(R.string.pref_key_language), null);

        //create the verse with the calendar if calendar has been changed
        this.verse = null;
        this.verse = new Verse(this.year, this.month, this.day, lang);
    }

    /**
     * open the url in the browser
     * from: http://www.mkyong.com/android/how-to-open-an-url-in-androids-web-browser/
     */
    public void viewOnWeb(){
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(this.verse.getFullUrl()));
        startActivity(intent);
    }

    /**
     * source: http://stackoverflow.com/questions/151777/saving-activity-state-in-android
     * @param savedInstanceState
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.v("state", "onSaveInstanceState called");

        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.

        //save date information
        savedInstanceState.putString(this.getString(R.string.saved_inst_year), this.verse.getYear());
        savedInstanceState.putString(this.getString(R.string.saved_inst_day), this.verse.getDay());
        savedInstanceState.putString(this.getString(R.string.saved_inst_month), this.verse.getMonth());

        //save verse info
        savedInstanceState.putString(this.getString(R.string.saved_inst_verse), this.verse.verse);
        savedInstanceState.putString(this.getString(R.string.saved_inst_book), this.verse.book);
        savedInstanceState.putString(this.getString(R.string.saved_inst_prayer), this.verse.prayer);
        savedInstanceState.putString(this.getString(R.string.saved_inst_author), this.verse.author);
        savedInstanceState.putString(this.getString(R.string.saved_inst_thoughts), this.verse.thoughts);
        savedInstanceState.putString(this.getString(R.string.saved_inst_text_date), this.verse.textDate);
    }

    //onCreate -> onStart -> onRestoreInstanceState
    //@Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.v("state", "onRestoreInstanceState called");

        //not overridden anymoresuper.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.

        //loda date info
        String year = savedInstanceState.getString(this.getString(R.string.saved_inst_year));
        String month = savedInstanceState.getString(this.getString(R.string.saved_inst_month));
        String day = savedInstanceState.getString(this.getString(R.string.saved_inst_day));


        this.year = Integer.parseInt(year);

        /*
         subtract so we don't fubar it when saveing then setting again.
         verse setter adds 1
        */
        this.month = Integer.parseInt(month) - 1;
        this.day = Integer.parseInt(day);

        //renew verse
        setVerseObject();

        //load verse information
        String mVerse = savedInstanceState.getString(this.getString(R.string.saved_inst_verse));
        String mBook = savedInstanceState.getString(this.getString(R.string.saved_inst_book));
        String mThoughts = savedInstanceState.getString(this.getString(R.string.saved_inst_thoughts));
        String mPrayer = savedInstanceState.getString(this.getString(R.string.saved_inst_prayer));
        String mAuthor = savedInstanceState.getString(this.getString(R.string.saved_inst_author));
        String mTextDate = savedInstanceState.getString(this.getString(R.string.saved_inst_text_date));

        SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = appPreferences.getString(this.getString(R.string.pref_key_language), null);

        /*
        set verse after restore
         */
        this.verse = new Verse(this.year, this.month, this.day, lang);
        this.verse.verse = mVerse;
        this.verse.book = mBook;
        this.verse.thoughts = mThoughts;
        this.verse.prayer = mPrayer;
        this.verse.author = mAuthor;
        this.verse.textDate = mTextDate;

        /*
        set the view with the verse information
         */
        this.onPostDownload(this.verse);
    }

    @Override
    protected void onStart() {

        Log.v("state", "onstart called");
        super.onStart();
    }

    @Override
    protected void onResume() {

        Log.v("state", "onresume called");
        super.onResume();
    }

    @Override
    protected void onPause() {

        Log.v("state", "onpause called");
        super.onPause();
    }

    @Override
    protected void onStop() {

        Log.v("state", "onstop called");
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        Log.v("state", "destroy called");
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        Log.v("state", "back called");
        super.onBackPressed();
    }

    /**
     * set the connection for main app
     * @param layout
     */
    private void setLayout(int layout){
        //setContentView(layout);
    }

    /**
     * load the verse from the webs
     */
    public void doHtmlStuff(){

        /**
         * check if user is connect to internet first
         */
        String ip = new Helper().isConnectedToInternet();

        //set screen if no internet connection to retry button
        if(ip == null){
            //source: http://www.mkyong.com/android/android-alert-dialog-example/
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            // set title
            alertDialogBuilder.setTitle("No Internet");

            // set dialog message
            alertDialogBuilder
                    .setMessage("Daily Scripture only works with an internet connection.")
                    .setCancelable(false)
                    .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            dialog.cancel();
                            // if this button is clicked, close
                            // current activity
                            //MyActivity.this.finish();
                        }
                    });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            //alertDialog.show();


            this.setLayout(R.layout.main_no_connection);

            //show a toast
            this.ShowToast("Connection Failed", Toast.LENGTH_SHORT);

            final Button buttonRetry = (Button) findViewById(R.id.button_no_connection);
            buttonRetry.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //try to load again since retry was clicked
                    doHtmlStuff();
                }
            });

            return;
        }

        //download the scripture
        try{
            new LongOperation(this.verse, this).execute("");
        }
        catch(Exception ex){
            //source: http://www.mkyong.com/android/android-alert-dialog-example/
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            // set title
            alertDialogBuilder.setTitle("Trouble");

            // set dialog message
            alertDialogBuilder
                    .setMessage("Trouble loading the scripture.")
                    .setCancelable(false)
                    .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            dialog.cancel();
                            // if this button is clicked, close
                            // current activity
                            //MyActivity.this.finish();
                        }
                    });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();

            //log it
            Log.v("fail", "Failed to load html. Ex: " + ex.toString());
        }
    }

    /**
     * do service stuff
     * @param text
     * @param duration
     */
    public Handler handler = new Handler(){
        public void handleMessage(Message message){
            Verse vrs = (Verse)message.obj;
            if(message.arg1 == RESULT_OK && vrs != null){
                onPostDownload(vrs);
            }

            /*
            user was having issue viewing the application. progress dialog may not be supported
             */
            if(progressDialog != null){
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
            }
        };
    };

    private void ShowToast(String text, int duration){
        Context context = getApplicationContext();

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    /**
     * set the verse information on the screen
     */
    public void onPostDownload(Verse verse){
        try{ //this trys to fix a null pointer exception. it may be an issue with the language??
            //set the layout back to showing the verse
            setLayout(R.layout.main);

            this.verse = null;
            this.verse = verse;

            //try to fix a null pointer ANR exception adding =="" and ==null
            if(verse.textDate.equals("failed to get") || verse.textDate.equals("") || verse.textDate == null){
                this.setLayout(R.layout.main_no_connection);
            }

            //fill the layout
            TextView textViewDate = (TextView) findViewById(R.id.textView_date);
            textViewDate.setText(verse.textDate); // txt.setText(result);
            TextView textViewVerse = (TextView) findViewById(R.id.textView_verse);
            textViewVerse.setText(verse.verse); // txt.setText(result);
            TextView textViewBook = (TextView) findViewById(R.id.textView_book);
            textViewBook.setText(verse.book); // txt.setText(result);
            TextView textViewThoughts = (TextView) findViewById(R.id.textView_thoughts);
            textViewThoughts.setText(verse.thoughts); // txt.setText(result);
            TextView textViewPrayer = (TextView) findViewById(R.id.textView_prayer);
            textViewPrayer.setText(verse.prayer); // txt.setText(result);

            /*
            set titles based on the users selected language
             */
            TextView titlePrayer = (TextView) findViewById(R.id.textView_prayer_title);
            TextView titleThoughts = (TextView) findViewById(R.id.textView_thoughts_title);
            TextView copyright = (TextView) findViewById(R.id.textView_creator);
            copyright.setText(getString(R.string.label_copyright));

            if(this.verse.getLanguage().equals("en")){
                titlePrayer.setText(getString(R.string.label_prayer));
                titleThoughts.setText(getString(R.string.label_thoughts));
            }
            else if(this.verse.getLanguage().equals("es")){
                titlePrayer.setText(getString(R.string.label_prayer_es));
                titleThoughts.setText(getString(R.string.label_thoughts_es));
            }
            else if(this.verse.getLanguage().equals("de")){
                titlePrayer.setText(getString(R.string.label_prayer_de));
                titleThoughts.setText(getString(R.string.label_thoughts_de));
            }
            else if(this.verse.getLanguage().equals("pt")){
                titlePrayer.setText(getString(R.string.label_prayer_pt));
                titleThoughts.setText(getString(R.string.label_thoughts_pt));
            }
            else if(this.verse.getLanguage().equals("ru")){
                titlePrayer.setText(getString(R.string.label_prayer_ru));
                titleThoughts.setText(getString(R.string.label_thoughts_ru));
            }
            /*
            done setting titles
             */


            //scroll to the top
            ScrollView scrollView = (ScrollView) findViewById(R.id.scroll_view);
            scrollView.fullScroll(View.FOCUS_UP);

            /*
            set the share intent
             */
            if(this.mShareActionProvider != null){
                this.mShareActionProvider.setShareIntent(getDefaultShareIntent());
            }

            /**
             * log the date
             */
            //this.postToMySQL();
        }
        catch(Exception ex){
            Log.v("DailyScripture", "Post download fail. Ex: " + ex.toString());
            this.setLayout(R.layout.main_no_connection);
        }
    }


    /**
     * helpers
     */
    public static class Helper {

        /**
         * get if on internet
         * source: http://stackoverflow.com/questions/5764783/android-checking-if-the-device-is-connected-to-the-internet
         */
        public String isConnectedToInternet(){
            try{

                for (Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces(); enumeration.hasMoreElements();) {
                    NetworkInterface networkInterface = enumeration.nextElement();
                    for (Enumeration<InetAddress> enumIpAddress = networkInterface.getInetAddresses(); enumIpAddress.hasMoreElements();) {
                        InetAddress iNetAddress = enumIpAddress.nextElement();
                        if (!iNetAddress.isLoopbackAddress()) {
                            return iNetAddress.getHostAddress().toString();
                        }
                    }
                }
            }
            catch (Exception e){
                Log.v("Ex", "Not connected to internet");
            }
            return null;
        }
    }

    /**
     * unique id
     * http://stackoverflow.com/questions/2785485/is-there-a-unique-android-device-id
     */
    private static String uniqueID = null;
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
    public synchronized static String id(Context context){
        if (uniqueID == null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(
                    PREF_UNIQUE_ID, Context.MODE_PRIVATE);
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.commit();
            }
        }
        return uniqueID;
    }

    public static String md5(String string) {
        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            Log.v("Daily", "Huh, MD5 should be supported?", e);
            return "";
        } catch (Exception e) {
            Log.v("Daily", "Huh, UTF-8 should be supported?", e);

            return "";
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);

        for (byte b : hash) {
            int i = (b & 0xFF);
            if (i < 0x10) hex.append('0');
            hex.append(Integer.toHexString(i));
        }

        return hex.toString();
    }

   /* public void postToMySQL(){
        *//*if(Build.VERSION.SDK_INT > 8){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        String date = this.verse.getYear() + "-" + this.verse.getMonth() + "-" + this.verse.getDay();
        String androidVersion = Integer.toString(Build.VERSION.SDK_INT);

        *//**//**
         * get md5 hash of device id to keep it secure
         *//**//*
        String uniqueId = md5(id(this));
        String deviceId = md5(Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID));

        HttpURLConnection connection;
        OutputStreamWriter request = null;

        URL url = null;
        String response = null;
        String parameters = "deviceId=" + deviceId +
                            "&versedate=" + date +
                            "&androidversion=" + androidVersion +
                            "&installId=" + uniqueId;

        try
        {
            url = new URL("http://mooneylabs.com/android/dailyScriptureLogMe.php");
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestMethod("POST");

            request = new OutputStreamWriter(connection.getOutputStream());
            request.write(parameters);
            request.flush();
            request.close();
            String line = "";
            InputStreamReader isr = new InputStreamReader(connection.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
            // Response from server after login process will be stored in response variable.
            response = sb.toString();
            // You can perform UI operations here
            //Toast.makeText(this,"Message from Server: \n"+ response, 0).show();
            isr.close();
            reader.close();

        }
        catch(IOException e)
        {
            // Error
        }*//*

    }*/


    protected ProgressDialog progressDialog;
    /**
     * get the html and fill in the view
     * source: http://stackoverflow.com/questions/9671546/asynctask-android-example
     */
    private class LongOperation extends AsyncTask<String, Context, Verse> {
        //private ProgressDialog progressDialog;
        private Verse verse;
        private Context targetCtx;

        public LongOperation(Verse verse1, Context context){
            this.verse = verse1;

            this.targetCtx = context ;
            // this.needToShow = true;
            progressDialog = new ProgressDialog( targetCtx ) ;
            progressDialog.setCancelable ( false ) ;
            progressDialog.setMessage ( "This will take a few seconds.");
            progressDialog.setTitle ( "Downloading Bible Verse" ) ;
            progressDialog.setIndeterminate ( true ) ;
        }

        @ Override
        protected void onPreExecute ( ) {
            progressDialog.show ( ) ;
        }

        @Override
        protected Verse doInBackground(String... params) {
            //this.verse.downloadAndParseHtmlSource();

            Intent intent = new Intent(targetCtx, DailyScriptureFetcher.class);

            //need to subtract one from the month when getting it because it adds one because of how datetime works in java
            int iMonth = Integer.parseInt(verse.getMonth()) - 1;
            //craete messenger
            Messenger messenger = new Messenger(handler);
            intent.putExtra("MESSENGER", messenger);
            intent.putExtra("year", Integer.parseInt(verse.getYear()));
            intent.putExtra("month", iMonth);
            intent.putExtra("day", Integer.parseInt(verse.getDay()));
            intent.putExtra("lang", verse.getLanguage());

            startService(intent);

            Log.v("Daily", "called start service from long operation");

            return this.verse;
        }

        @Override
        protected void onPostExecute(Verse verse) {
            //onPostDownload(verse);
           // progressDialog.dismiss();
        }
    }
}
