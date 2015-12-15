package com.aware.plugin.template;

import android.content.ContentValues;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.utils.Aware_Plugin;

import java.util.ArrayList;
import java.util.List;

import com.aware.plugin.template.Provider.Template_Data;

public class Plugin extends Aware_Plugin implements SensorEventListener{


    //get a thread to collect real time accelerometer without using AWARE
    /**
     * Sensor update frequency in microseconds, default 200000
     */
    private static int SAMPLING_RATE = 200000;

    private static SensorManager mSensorManager;
    private static Sensor mAccelerometer;


    private List<ContentValues> data_values = new ArrayList<ContentValues>();

    //do GPS

    //store data into AWARE database

    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub
    }

    //get ACCelerometer first. then get GPS

    @Override
    public void onSensorChanged(SensorEvent event) {
        ContentValues rowData = new ContentValues();
        rowData.put(Template_Data.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
        rowData.put(Template_Data.TIMESTAMP, System.currentTimeMillis());
        rowData.put(Template_Data.Accelerometer_X, event.values[0]);
        rowData.put(Template_Data.Accelerometer_Y, event.values[1]);
        rowData.put(Template_Data.Accelerometer_Z, event.values[2]);



        /*
         Template_Data.LATITUDE + " real default 0," +
                    Template_Data.LONGITUDE + " real default 0," +
                    Template_Data.BEARING + " real default 0," +
                    Template_Data.SPEED + " real default 0," +
                    Template_Data.ALTITUDE + " real default 0," +
         */
    }

    @Override
    public void onCreate() {
        super.onCreate();

        TAG = "AWARE::"+getResources().getString(R.string.app_name);
        DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");



        //Activate programmatically any sensors/plugins you need here
        //e.g., Aware.setSetting(this, Aware_Preferences.STATUS_ACCELEROMETER,true);
        //NOTE: if using plugin with dashboard, you can specify the sensors you'll use there.

        //Any active plugin/sensor shares its overall context using broadcasts
        CONTEXT_PRODUCER = new ContextProducer() {
            @Override
            public void onContext() {
                //Broadcast your context here
            }
        };

        //Add permissions you need (Support for Android M) e.g.,
        //REQUIRED_PERMISSIONS.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //To sync data to the server, you'll need to set this variables from your ContentProvider
        //DATABASE_TABLES = Provider.DATABASE_TABLES
        //TABLES_FIELDS = Provider.TABLES_FIELDS
        //CONTEXT_URIS = new Uri[]{ Provider.Table_Data.CONTENT_URI }

        //Activate plugin
        Aware.startPlugin(this, "com.aware.plugin.template");
    }

    //This function gets called every 5 minutes by AWARE to make sure this plugin is still running.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Check if the user has toggled the debug messages
        DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Deactivate any sensors/plugins you activated here
        //e.g., Aware.setSetting(this, Aware_Preferences.STATUS_ACCELEROMETER, false);

        //Stop plugin
        Aware.stopPlugin(this, "com.aware.plugin.template");
    }
}
