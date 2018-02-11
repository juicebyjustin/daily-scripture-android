package com.mooneylabs.android.dailyscripture;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: justin
 * Date: 1/2/13
 * Time: 7:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class Verse {
    /*
    date information
     */
    private String year;
    private String month;
    private String day;

    /*
    verse info
     */
    public String textDate;
    public String verse;
    public String book;
    public String thoughts;
    public String prayer;
    public String author;

    /*
    url info used to fetch verse
     */
    private static String partUrl = "http://www.verseoftheday.com/";
    private String language = "";


    public Verse(int year, int month, int day, String _lang){

        this.setDay(day);
        this.setMonth(month);
        this.setYear(year);
        this.setLanguage(_lang);
    }

    /**
     * set the year in string
     * @param year
     */
    private void setYear(int year){
        String sYear = String.valueOf(year);
        this.year = sYear;
    }

    /**
     * set the month in string
     * @param month
     */
    private void setMonth(int month){
        month++;

        String sMonth = String.valueOf(month);

        //add a 0 if < 10
        if(month < 10){
            sMonth = "0" + sMonth;
        }

        this.month = sMonth;
    }

    /**
     * set the day in string. appends 0 if < 10
     * @param day
     */
    public void setDay(int day){
        String sDay = String.valueOf(day);

        //add 0 if < 10
        if(day < 10){
            sDay = "0" + sDay;
        }

        this.day = sDay;
    }

    /**
     * set the language, use value from preference
     * @param language
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * get a string of date to be used in the url
     * for the verse
     * @return datestring for the url
     */
    private String getUrlDate(){
        //return "MMDDYYY"
        return this.getMonth() + this.getDay() + this.getYear();
    }

    public String getFullUrl() {
        String tmp = "";

        tmp = this.getPartUrl() + this.getLanguage() + "/" + this.getUrlDate();
        return tmp;
    }

    public String getLanguage() {
        return language;
    }

    public String getYear() {
        return year;
    }

    public String getMonth() {
        return month;
    }

    public String getDay() {
        return day;
    }

    public String getPartUrl() {
        return partUrl;
    }

    /**
     * this method downloads and parses the html source code for a given verse
     * object. an exception is thrown if something goes wrong. catch me :)
     */
    public void downloadAndParseHtmlSource(){
        /*
            1. download html source
            2. parse source code
            3. return
         */
        String htmlSource = this.downloadHtmlSource(); //download source
        this.parseHtmlSource(htmlSource); //parse source code
    }

    /**
     * parse the html source code for the date, verse, book, thoughts, and prayer
     * @param html
     */
    public void parseHtmlSource(String html){
        /*
            1. use regex to get (order of appearance in code):
                a. date
                b. verse
                c. book
                d. thought
                e. prayer
                f. author
         */

        this.textDate = this.getDateFromHtml(html);
        this.verse = this.getVerseFromHtml(html);
        this.book = this.getBookFromHtml(html);
        this.thoughts = this.getThoughtsFromHtml(html);
        this.prayer = this.getPrayerFromHtml(html);
    }

    /**
     * get the verse out of the html
     * @param html source for verse
     * @return the verse
     */
    public String getVerseFromHtml(String html){

        String verse = "";

        try{
            //Pattern pattern = Pattern.compile("<p id=\"slogan\">for (.*?)<a href=\"");
            Pattern pattern = Pattern.compile("<div class=\"scripture\">(.*?)<div class=\"reference\">");

            Matcher matcher = pattern.matcher(html);

            // search the input strings
            while (matcher.find()) {
                verse = matcher.group(1).replace("<div class=\"bilingual-left\">", "").trim();
            }
        }
        catch (Exception ex){
            verse = "";
        }

        return verse;
    }

    /**
     * get the verse out of the html
     *
     * learned from: http://www.vogella.com/articles/JavaRegularExpressions/article.html#introduction_languages
     *
     * @param html source for verse
     * @return the verse
     */
    public String getBookFromHtml(String html){

        String book = "";

        try{
            //Pattern pattern = Pattern.compile("<p id=\"slogan\">for (.*?)<a href=\"");
            Pattern pattern = Pattern.compile("<div class=\"reference\">&mdash;(.*?)</a></div></div>");

            Matcher matcher = pattern.matcher(html);

            // search the input strings
            while (matcher.find()) {
                book = matcher.group(1);
                book = book.substring(book.lastIndexOf(">")).replace(">", "").trim();
            }
        }
        catch (Exception ex){
            book = "failed to get";
        }

        return book;
    }

    /**
     * return a date string from html
     * @param html
     * @return
     */
    public String getDateFromHtml(String html){
        String date = "";

        try{
            Pattern pattern = Pattern.compile("<p id=\"slogan\">(.*?)<span id=\"archive-link\">");

            Matcher matcher = pattern.matcher(html);

            // search the input strings
            while (matcher.find()) {
                date = matcher.group(1).replace("[", "").replace("for", "").trim();
                //date = matcher.group(1).replace("[", "").trim();
            }
        }
        catch (Exception ex){
            date = "failed to get";
        }

        return date;
    }

    /**
     * get the thoughts out of the html
     * @param html source for verse
     * @return the verse
     */
    public String getThoughtsFromHtml(String html){

        String thoughts = "";
        try{
            //Pattern pattern = Pattern.compile("<p id=\"slogan\">for (.*?)<a href=\"");
            Pattern pattern = Pattern.compile("<p id=\"thought\">(.*?)</p></div>");

            Matcher matcher = pattern.matcher(html);

            // search the input strings
            while (matcher.find()) {
                thoughts = matcher.group(1).trim();
                //thoughts = thoughts.substring(thoughts.lastIndexOf(">")).replace(">", "");
            }

            thoughts = thoughts.replace("&#8212;", "-");

        }
        catch (Exception ex){
            thoughts = "failed to get";
        }

        return thoughts;
    }

    /**
     * get the prayer out of the html
     * @param html source for verse
     * @return the verse
     */
    public String getPrayerFromHtml(String html){

        String prayer = "";

        try{
            //Pattern pattern = Pattern.compile("<p id=\"slogan\">for (.*?)<a href=\"");
            Pattern pattern;

            if(this.language.equals("de")){
                pattern = Pattern.compile("<h3>Mein Gebet</h3>(.*?)</p></div>");
            }
            else if(this.language.endsWith("es")){
                pattern = Pattern.compile("<h3>Mi oración</h3>(.*?)</p></div>");
            }
            else if(this.language.equals("pt")){
                pattern = Pattern.compile("<h3>Oração:</h3>(.*?)</p></div>");
            }
            else if(this.language.equals("ru")){
                pattern = Pattern.compile("<h3>Моя молитва</h3>(.*?)</p></div>");
            }
            else{
                pattern = Pattern.compile("<h3>My Prayer...</h3>(.*?)</p></div>");
            }

            Matcher matcher = pattern.matcher(html);

            // search the input strings
            while (matcher.find()) {
                prayer = matcher.group(1);
                prayer = prayer.replace("<p>", "").trim();
            }

        }
        catch (Exception ex){
            prayer = "failed to get";
        }

        return prayer;
    }

    /**
     * This method does the downloading of the html source code form Verseoftheday.com
     *
     * source: http://stackoverflow.com/questions/2423498/how-to-get-the-html-source-of-a-page-from-a-html-link-in-android
     * @return html source code
     */
    private String downloadHtmlSource(){
        String url = this.getFullUrl();

        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);

        HttpResponse response;
        try{
            response = client.execute(request);
        }
        catch (Exception ex){
            return null;
        }

        String html = "";
        InputStream in = null;
        try {
            in = response.getEntity().getContent();
        }
        catch (Exception ex){
            return null;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder str = new StringBuilder();

        try{
            String line = null;
            while((line = reader.readLine()) != null)
            {
                str.append(line);
            }

            in.close();
        }
        catch(Exception ex){
            return null;

        }

        html = str.toString();

        return html;
    }
}
