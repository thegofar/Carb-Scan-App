package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import java.io.File;

public class DeleteLogFiles extends Activity {

    Intent intent = getIntent();
    File f =(File) intent.getExtras().get("FILE_PATH");
    //boolean callEmailLogsProcess = intent.getBooleanExtra("FILES_REMAINING",false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_delete_log_files);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle("Delete File")
                .setMessage("Are you sure you want to delete?")

                .setCancelable(false)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        // delete code lives here
                        f.delete();
                        dialog.dismiss();
                        //finishActivity(Activity.RESULT_OK);
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.cancel();
                        finishActivity(Activity.RESULT_OK);
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
