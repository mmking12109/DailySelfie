package com.example.yang.dailyselfie;

import android.graphics.Bitmap;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Selfie {
    private Bitmap mPhoto;
    private String mTitle;
    private String mPath;
    private Date mDate;

    public Selfie(Bitmap photo, String title, String path, Date date) {
        this.mPhoto = photo;
        this.mTitle = title;
        this.mPath = path;
        this.mDate = date;
    }

    public Bitmap getPhoto() {
        return mPhoto;
    }

    public void setPhoto(Bitmap photo) {
        this.mPhoto = photo;
    }

    public String getTitle(){
        return mTitle;
    }

    public void setTitle(String title){
        this.mTitle = title;
    }

    public String getPath(){
        return mPath;
    }

    public void setPath(String path){
        this.mPath = path;
    }

//    public Date getDate(){
//        return mDate;
//    }

    // Get date as a string that humans can read
    public String getDate(){
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
        return dateFormat.format(mDate);
    }

    public void setDate(Date date){
        this.mDate = date;
    }
}
