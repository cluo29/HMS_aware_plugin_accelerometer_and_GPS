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

    public static final String ACTION_AWARE_PLUGIN_CHARGING_MONITOR = "ACTION_AWARE_PLUGIN_CHARGING_MONITOR";

    public static final String EXTRA_DATA = "data";

    public static ContextProducer context_producer;

    //get a thread to collect real time accelerometer without using AWARE
    /**
     * Sensor update frequency in microseconds, default 200000
     */

    private static int SAMPLING_RATE = 20000; //200000= 0.2sec, 20000=20ms

    //how many rows of container data are needed?
    final static int BITS = 1;
    private static int rowsOfContainer = 32 / BITS;


    private static SensorManager mSensorManager;
    private static Sensor mAccelerometer;
    private static Sensor mHeartRate;
    private static int heart_rate;
    private List<ContentValues> data_values = new ArrayList<ContentValues>();
    private List<ContentValues> water_values = new ArrayList<ContentValues>();

    private static boolean embeddingReady = false;
    private static int embeddingComplete = 0;

    String bits_heart_rate;

    private static int cntr;//how many bits embedded

    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        //embedding is HR
        if (sensor.getType() == Sensor.TYPE_HEART_RATE && !embeddingReady) {
            heart_rate = (int) event.values[0];
            embeddingReady=true;
            embeddingComplete=0;
            Log.d("SENSORS", "heart_rate = " + heart_rate);
            cntr=0;
            bits_heart_rate=String.format("%32s", Integer.toBinaryString(heart_rate)).replace(' ', '0');
        }

        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER && embeddingReady && embeddingComplete<rowsOfContainer) {
            // accelerometer data
            float accelerometer_x = event.values[0];
            float accelerometer_y = event.values[1];
            float accelerometer_z = event.values[2];

            Log.d("SENSORS", "x= " + accelerometer_x);
            Log.d("SENSORS", "y= " + accelerometer_y);
            Log.d("SENSORS", "z= " + accelerometer_z);


            //HR as embedding
            double GPS_latitude = 0; //sixth decimal place is worth up to 0.11 m:
            double GPS_longitude = 0;
            double GPS_altitude = 0; //If this location does not have an altitude then 0.0 is returned.
            float GPS_bearing = 0f; //(0.0, 360.0]
            float GPS_speed = 0f;


            /*
            //GPS as embedding
            //fake some GPS
            double GPS_latitude = 77.456789; //sixth decimal place is worth up to 0.11 m:
            double GPS_longitude = 123.456789;
            double GPS_altitude = 8888.0; //If this location does not have an altitude then 0.0 is returned.
            float GPS_bearing = 1.1f; //(0.0, 360.0]
            float GPS_speed = 1.1f; // If this location does not have a speed then 0.0 is returned.
            int heart_rate = 0;
            //in android, altitude, latitude, longitude are double. bearing and speed are float.
            //http://developer.android.com/reference/android/location/Location.html
            */

            //water here

            int bits = Float.floatToIntBits(accelerometer_x);
            String sbits = Long.toBinaryString(bits);
            char[] cbits  = sbits.toCharArray();
            cbits[Float.SIZE-1] = 1;
            for (int i = 0; i < BITS;i++){
                cbits[Float.SIZE-1-BITS] = bits_heart_rate.charAt(cntr);
                cntr++;
            }
            sbits = String.valueOf(cbits);
            bits = Integer.parseInt(sbits);
            float accelerometer_x_w = Float.intBitsToFloat(bits);
            //float  = Float.intBitsToFloat(bits);
            //float accelerometer_x_w = Float.intBitsToFloat(bits);
            //save data in DB

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
            rowData.put(Template_Data.HeartRate, heart_rate);

            ContentValues waterData = new ContentValues();
            waterData.put(Template_Data2.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
            waterData.put(Template_Data2.TIMESTAMP, System.currentTimeMillis());
            waterData.put(Template_Data2.Accelerometer_X, accelerometer_x_w);
            waterData.put(Template_Data2.Accelerometer_Y, accelerometer_y);
            waterData.put(Template_Data2.Accelerometer_Z, accelerometer_z);

            getContentResolver().insert(Provider.Template_Data.CONTENT_URI, rowData);
            getContentResolver().insert(Provider.Template_Data2.CONTENT_URI, waterData);

            //Share context
            //context_producer.onContext();
            /*

            if (data_values.size() < 20) {
                data_values.add(rowData);
                water_values.add(waterData);
                return;
            }
            Log.d("SENSORS2", "20");
            */


        /*
         Template_Data.LATITUDE + " real default 0," +
                    Template_Data.LONGITUDE + " real default 0," +
                    Template_Data.BEARING + " real default 0," +
                    Template_Data.SPEED + " real default 0," +
                    Template_Data.ALTITUDE + " real default 0," +
         */


            /*
            ContentValues[] data_buffer = new ContentValues[data_values.size()];
            data_values.toArray(data_buffer);
            Log.d("SENSORS2", "148");
            try {
                new AsyncStore().execute(data_buffer);
                Log.d("SENSORS2", "151");
            } catch (SQLiteException e) {
                if (Aware.DEBUG) Log.d(TAG, e.getMessage());
                Log.d("SENSORS2", "154");
            } catch (SQLException e) {
                Log.d("SENSORS2", "156");
                if (Aware.DEBUG) Log.d(TAG, e.getMessage());
            }
            data_values.clear();

            ContentValues[] data_buffer2 = new ContentValues[water_values.size()];
            water_values.toArray(data_buffer2);

            try {
                new AsyncStore2().execute(data_buffer2);
            } catch (SQLiteException e) {
                if (Aware.DEBUG) Log.d(TAG, e.getMessage());
            } catch (SQLException e) {
                if (Aware.DEBUG) Log.d(TAG, e.getMessage());
            }
            water_values.clear();
            */


            embeddingComplete++;

            Log.d("SENSORS", "embeddingComplete = " + embeddingComplete);

            if(embeddingComplete==rowsOfContainer)
            {
                embeddingReady=false;
            }
        }
    }

    /**
     * Database I/O on different thread
     */
    private class AsyncStore extends AsyncTask<ContentValues[], Void, Void> {
        @Override
        protected Void doInBackground(ContentValues[]... data) {
            getContentResolver().bulkInsert(Template_Data.CONTENT_URI, data[0]);
            Log.d("SENSORS2", "XXXX1");
            return null;
        }
    }

    private class AsyncStore2 extends AsyncTask<ContentValues[], Void, Void> {
        @Override
        protected Void doInBackground(ContentValues[]... data) {
            getContentResolver().bulkInsert(Template_Data2.CONTENT_URI, data[0]);
            Log.d("SENSORS2", "XXXX2");
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
        mHeartRate = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        mSensorManager.registerListener(this, mHeartRate, SensorManager.SENSOR_DELAY_NORMAL);
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

                /*
                Intent context_solar_charger = new Intent();
                context_solar_charger.setAction(ACTION_AWARE_PLUGIN_CHARGING_MONITOR);
                context_solar_charger.putExtra(EXTRA_DATA, data);
                sendBroadcast(context_solar_charger);
                */
            }
        };
       // context_producer = CONTEXT_PRODUCER;

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
