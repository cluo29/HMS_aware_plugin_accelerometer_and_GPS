package com.aware.plugin.template;

import android.content.ContentValues;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.utils.Aware_Plugin;

import java.util.ArrayList;
import java.util.List;

import com.aware.plugin.template.Provider.Template_Data;
import com.aware.plugin.template.Provider.Template_Data2;

public class Plugin extends Aware_Plugin implements SensorEventListener{


    //get a thread to collect real time accelerometer without using AWARE
    /**
     * Sensor update frequency in microseconds, default 200000
     */
    private static int SAMPLING_RATE = 200000;

    private static SensorManager mSensorManager;
    private static Sensor mAccelerometer;


    private List<ContentValues> data_values = new ArrayList<ContentValues>();
    private List<ContentValues> water_values = new ArrayList<ContentValues>();


    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        // accelerometer data
        float accelerometer_x=event.values[0];
        float accelerometer_y=event.values[1];
        float accelerometer_z=event.values[2];

        Log.d("SENSORS", "x= "+accelerometer_x);
        Log.d("SENSORS", "y= "+accelerometer_y);
        Log.d("SENSORS", "z= "+accelerometer_z);

        //fake some GPS

        double GPS_latitude=77.456789; //sixth decimal place is worth up to 0.11 m:
        double GPS_longitude=123.456789;
        double GPS_altitude=8888.0; //If this location does not have an altitude then 0.0 is returned.
        float GPS_bearing= 1.1f; //(0.0, 360.0]
        float GPS_speed= 1.1f; // If this location does not have a speed then 0.0 is returned.
        //in android, altitude, latitude, longitude are double. bearing and speed are float.
        //http://developer.android.com/reference/android/location/Location.html

        //water here

        ContentValues rowData = new ContentValues();
        rowData.put(Template_Data.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
        rowData.put(Template_Data.TIMESTAMP, System.currentTimeMillis());
        rowData.put(Template_Data.Accelerometer_X, accelerometer_x);
        rowData.put(Template_Data.Accelerometer_Y, accelerometer_y);
        rowData.put(Template_Data.Accelerometer_Z, accelerometer_z);
        rowData.put(Template_Data.LATITUDE, GPS_latitude);
        rowData.put(Template_Data.LONGITUDE, GPS_longitude);
        rowData.put(Template_Data.BEARING, GPS_bearing);
        rowData.put(Template_Data.SPEED, GPS_speed);
        rowData.put(Template_Data.ALTITUDE, GPS_altitude);

        ContentValues waterData = new ContentValues();
        waterData.put(Template_Data2.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
        waterData.put(Template_Data2.TIMESTAMP, System.currentTimeMillis());
        waterData.put(Template_Data2.Accelerometer_X, accelerometer_x);
        waterData.put(Template_Data2.Accelerometer_Y, accelerometer_y);
        waterData.put(Template_Data2.Accelerometer_Z, accelerometer_z);


        if( data_values.size() < 250 ) {
            data_values.add(rowData);
            water_values.add(waterData);
            return;
        }

        /*
         Template_Data.LATITUDE + " real default 0," +
                    Template_Data.LONGITUDE + " real default 0," +
                    Template_Data.BEARING + " real default 0," +
                    Template_Data.SPEED + " real default 0," +
                    Template_Data.ALTITUDE + " real default 0," +
         */


        ContentValues[] data_buffer = new ContentValues[data_values.size()];
        data_values.toArray(data_buffer);

        try {
                new AsyncStore().execute(data_buffer);
        }catch( SQLiteException e ) {
            if(Aware.DEBUG) Log.d(TAG,e.getMessage());
        }catch( SQLException e ) {
            if(Aware.DEBUG) Log.d(TAG,e.getMessage());
        }
        data_values.clear();

        ContentValues[] data_buffer2 = new ContentValues[water_values.size()];
        water_values.toArray(data_buffer2);

        try {
            new AsyncStore().execute(data_buffer2);
        }catch( SQLiteException e ) {
            if(Aware.DEBUG) Log.d(TAG,e.getMessage());
        }catch( SQLException e ) {
            if(Aware.DEBUG) Log.d(TAG,e.getMessage());
        }
        water_values.clear();
    }

    /**
     * Database I/O on different thread
     */
    private class AsyncStore extends AsyncTask<ContentValues[], Void, Void> {
        @Override
        protected Void doInBackground(ContentValues[]... data) {
            getContentResolver().bulkInsert(Template_Data.CONTENT_URI, data[0]);
            return null;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();

        TAG = "AWARE::"+getResources().getString(R.string.app_name);
        DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");


        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorManager.registerListener(this, mAccelerometer, SAMPLING_RATE);

        //Activate programmatically any sensors/plugins you need here
        //Aware.setSetting(this, Aware_Preferences.STATUS_ACCELEROMETER,true);
        //Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_ACCELEROMETER, SAMPLING_RATE);
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

        //Aware.setSetting(this, Aware_Preferences.STATUS_ACCELEROMETER, false);

        //Stop plugin
        Aware.stopPlugin(this, "com.aware.plugin.template");
    }
}
