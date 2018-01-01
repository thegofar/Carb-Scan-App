package com.example.android.bluetoothlegatt;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

/**
 * Created by craig on 08/12/2017.
 */

public class EmailLogs {
    private final Context mContext;
    File mLogPath;
    private static final String mLogExt = ".csv";
    String mPathForEmail;
    boolean mFilesToProcess;
    File mFileToDelete;

    public EmailLogs(Context c, File logP){
        mContext = c;
        mLogPath = logP;
        mFilesToProcess = true;
    }

    public void processLogs()
    {
        // this is called when we find some decent WiFi
        findAttachments();
        //sendMail();
        mFileToDelete.delete();
        //removeSentFile();
    }
    static final int REMOVE_FILE_CODE_RTN=-1;

    private void removeSentFile()
    {
        Intent i = new Intent(mContext, DeleteLogFiles.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("FILE_PATH",mFileToDelete);
        //i.putExtra("FILES_REMAINING",mFilesToProcess);
        mContext.startActivity(i);
    }


    private void findAttachments()
    {
        // search inside of mLogPaths
        File[] file = mLogPath.listFiles();

        for (File f : file)
        {
            if (f.isFile() && f.getPath().endsWith(mLogExt))
            {
                //... do stuff
                Log.w("attaching file", "file " + f);
                mPathForEmail = f.getAbsolutePath();
                mFileToDelete = f;
            }
        }
        if (file.length > 0 ){mFilesToProcess=true;}else{mFilesToProcess=false;}
    }

    public String createUniqueFile() {
        SimpleDateFormat s =  new SimpleDateFormat("ddMMyy-hhmmss");
        String fname = "GPZLog_" + s.format(new Date()) + mLogExt;
        return fname;
    }

    private void sendMail(){
        Uri attachPath = Uri.parse("file://" + mPathForEmail);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        // set the type to 'email'
        emailIntent.setType("vnd.android.cursor.dir/email");
        String to[] = {"mr.craigbarry@gmail.com"};
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        // the attachment
        emailIntent.putExtra(Intent.EXTRA_STREAM, attachPath);
        // the mail subject
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, mFileToDelete.getName());

        if (mPathForEmail!=null) {
            mContext.startActivity(Intent.createChooser(emailIntent, "Send email"));
        }
    }

}
