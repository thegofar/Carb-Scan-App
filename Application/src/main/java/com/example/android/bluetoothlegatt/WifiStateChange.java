package com.example.android.bluetoothlegatt;

/**
 * Created by craig on 07/12/2017.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

public class WifiStateChange extends BroadcastReceiver {
    boolean mWifiConnected;
    File mPath;

    public WifiStateChange(File p)
    {
        mPath = p;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            boolean connected = info.isConnected();
            if (connected && !mWifiConnected){
                //Toast.makeText(getApplicationContext(), "We found some WiFi!", Toast.LENGTH_LONG).show();
                Log.w("MY_DEBUG_TAG", "WIFI connected");
                mWifiConnected=true;
                EmailLogs emailLogs = new EmailLogs(context, mPath);
                emailLogs.processLogs();
            } else {
                mWifiConnected=false;
                Log.w("MY_DEBUG_TAG", "WIFI disconnected");
            }
        }
    }
}
