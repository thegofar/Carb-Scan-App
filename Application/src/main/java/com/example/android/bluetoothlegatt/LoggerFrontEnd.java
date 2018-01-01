package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoggerFrontEnd extends Activity {


    private List<String> mRxBtData;
    Location location;
    LocationManager locationManager;
    ArrayList<String> permissions = new ArrayList<>();
    ArrayList<String> permissionsToRequest;
    ArrayList<String> permissionsRejected = new ArrayList<>();
    boolean isGPS = false;
    boolean isNetwork = false;
    float mVss;
    double mLat;
    double mLon;
    ToggleButton mLogToggleButton;
    File mLogPath;
    File mLogFile;



    private LocationListener mListner = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mVss = location.getSpeed()*(float)2.23694;
            ((EditText) findViewById(R.id.vssEditText)).setText(String.valueOf(mVss));
            mLat = location.getLatitude();
            mLon = location.getLongitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logger_front_end);

        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        permissionsToRequest = findUnAskedPermissions(permissions);
        requestLocation(); //set up the location listener
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        mLogToggleButton = (ToggleButton) findViewById(R.id.logToggleButton);
        setupToggleListener();
        setupWifiListener();

    }

    private void setupWifiListener() {
        WifiStateChange broadcastReceiver = new WifiStateChange(mLogPath);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        //registerReceiver(broadcastReceiver, intentFilter);

    }

    // attach an OnClickListener
    private void setupToggleListener()
    {
        mLogPath = new File(Environment.getExternalStorageDirectory(), "/Data Logs/");
        mLogToggleButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mLogToggleButton.isChecked())
                {
                    Toast.makeText(getApplicationContext(), "Logging started", Toast.LENGTH_SHORT).show();

                    EmailLogs emailLogs = new EmailLogs(v.getContext(),mLogPath);
                    String fname = emailLogs.createUniqueFile();
                    mLogFile = new File(mLogPath, fname);
                    if (!mLogPath.isDirectory())
                    {
                        try {
                            Log.d("STATE", Environment.getExternalStorageState());
                            mLogPath.mkdirs();
                        }
                        catch (Exception e)
                        {
                            Log.e("could not create dir", e.toString());
                        }
                    }
                    Log.e("LoggerFE", mLogFile.toString());
                    Toast.makeText(getApplicationContext(),mLogFile.toString(),Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Logging ended", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void LogCSVData(String dataString)
    {
        dataString=dataString.replaceAll(System.getProperty("line.separator"), "");
        if (!mLogFile.exists()) {
            try {
                mLogFile.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(mLogFile, true));
                writer.append("STM_t [msec],Engine Speed [RPM],MAP [kpa],Unfiltered MAP [kpa], Vss [mph], Latitude, Longitude");
                writer.newLine();
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mLogFile.exists()) try
        {
            //dataString.replaceAll(System.getProperty("line.separator"), "");
            BufferedWriter writer = new BufferedWriter(new FileWriter(mLogFile, true));
            writer.append(dataString+"," + mVss +"," + mLat+","+mLon+"\n");
            Log.d("csv string",dataString+"," + mVss +"," + mLat+","+mLon);
            writer.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }


    private void requestLocation()
    {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,mListner);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
           if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    public enum packedData {
        TICK_COUNT,
        ENGINE_SPEED,
        MAP,
        UNFILTERED_MAP,
        LAMBDA
    }

    private void displayData(byte[] data) {
        if (data != null) {
            BytePackager bp = new BytePackager(data,data.length);
            int stx = bp.getByte(0); // should be 0x2 <STX>
            int etx = bp.getByte(18); // should be 0x3 <ETX>
            String dataString;
            if(stx==0x2 && data.length==19 && etx==0x3)
            {
                int rpm = bp.getU16(1); //should be rpm
                int map = bp.getU16(3);
                long ts = bp.getU32(5);
                int unfilteredMap = bp.getU16(9);
                int lambdaVolts = bp.getByte(11);

                dataString = new String(Long.toString(ts) + "," +
                        Integer.toString(rpm) + "," +
                        Integer.toString(map) + "," +
                        Integer.toString(unfilteredMap) + "," +
                        Integer.toString(lambdaVolts)
                        );
            }
            else
            {
                dataString = "incomplete";
            }
            //String dataString = new String("2,123,123,12345\n");
            mRxBtData =  Arrays.asList(dataString.split("\\s*,\\s*"));
            if (mRxBtData.size() != 5)
            {
                // mishapen...
            }
            else {
                ((EditText) findViewById(R.id.rpmEditText)).setText(mRxBtData.get(packedData.ENGINE_SPEED.ordinal()));
                ((EditText) findViewById(R.id.mapEditText)).setText(mRxBtData.get(packedData.MAP.ordinal()));
                ((EditText) findViewById(R.id.mapUFEditText)).setText(mRxBtData.get(packedData.UNFILTERED_MAP.ordinal()));
                ((EditText) findViewById(R.id.ticksEditText)).setText(mRxBtData.get(packedData.TICK_COUNT.ordinal()));
                ((EditText) findViewById(R.id.lambdaVoltsEditText)).setText(mRxBtData.get(packedData.LAMBDA.ordinal()));
                dataLogHandler(mRxBtData);
            }
        }
    }

    private void dataLogHandler(List<String> mRxBtData) {
        if(mLogToggleButton.isChecked())
        {
            String btString = TextUtils.join(",", mRxBtData);
            LogCSVData(btString);
        }
    }

    private ArrayList findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList result = new ArrayList();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }
    private boolean hasPermission(String permission) {
        if (canAskPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }
    private boolean canAskPermission() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

}
