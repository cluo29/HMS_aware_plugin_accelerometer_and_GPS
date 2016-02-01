package com.aware.plugin.template;



import android.content.ContentValues;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.utils.Aware_Plugin;

import java.util.ArrayList;
import java.util.List;

//this service collects data and sends intent.
public class Plugin extends Aware_Plugin implements SensorEventListener{

    public static final String ACTION_AWARE_PLUGIN_CHARGING_MONITOR = "ACTION_AWARE_PLUGIN_CHARGING_MONITOR";

    public static final String EXTRA_DATA = "data";

    //how many bits do we use in a container
    final static int BITS = 1;

    //how many rows of container data are needed?
    //32 float
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

    private static int counterEmbedded;//how many bits embedded

    //how many heart rate rows are got
    private static int heart_rate_count=0;

    private static long timestamp_start=0;

    private static boolean do_test=true;

    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub
    }

    //single thread
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;


        if (sensor.getType() == Sensor.TYPE_HEART_RATE&&do_test) {//&&1==0 not doing heart rate alone
            heart_rate = (int) event.values[0];

            heart_rate_count++;

            Log.d("SENSORS10","heart_rate_count = "+heart_rate_count);

            if(timestamp_start==0) {
                timestamp_start=System.currentTimeMillis();
            }
            if(System.currentTimeMillis()-timestamp_start>=1000*300) //*3600)
            {
                Log.d("SENSORS10", "5 min of heart_rate count = "+heart_rate_count);
                do_test=false;
            }
        }

        //embedding is HR

        if (sensor.getType() == Sensor.TYPE_HEART_RATE && !embeddingReady && do_test&&1==0) { //&&1==0 not doing embedding


            heart_rate = (int) event.values[0];

            embeddingReady=true;
            embeddingComplete=0;
            counterEmbedded =0;
            bits_heart_rate=String.format("%32s", Integer.toBinaryString(heart_rate)).replace(' ', '0');
            if(timestamp_start==0) {
                timestamp_start=System.currentTimeMillis();
            }

            if(System.currentTimeMillis()-timestamp_start>=1000*300) //*3600)
            {
                Log.d("SENSORS10", "5 min of heart_rate wm count = "+heart_rate_count);
                do_test=false;
            }
            heart_rate_count++;
        }


        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER && embeddingReady && embeddingComplete<rowsOfContainer&&do_test&&1==0) {//&&1==0 not doing embedding
            // accelerometer data
            //only use x for this testing
            float accelerometer_x = event.values[0];


            //float accelerometer_y = event.values[1];
            //float accelerometer_z = event.values[2];

            //Log.d("SENSORS", "x= " + accelerometer_x);
            //Log.d("SENSORS", "y= " + accelerometer_y);
            //Log.d("SENSORS", "z= " + accelerometer_z);


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

            //watermarking starts here

            int Acceleromter_X_bits = Float.floatToIntBits(accelerometer_x);

            String Acceleromter_X_binaryString = String.format("%32s", Integer.toBinaryString(Acceleromter_X_bits)).replace(' ', '0');

            char[] Acceleromter_X_binaryChar  = Acceleromter_X_binaryString.toCharArray();

            //flag, if 1, the sensor reading is used for watermarking
            Acceleromter_X_binaryChar[Float.SIZE-1] = '1';

            //embed BITS digits to from the digit next to flag
            for (int i = 0; i < BITS;i++){

                Acceleromter_X_binaryChar[Float.SIZE-2-i] = bits_heart_rate.charAt(counterEmbedded);

                counterEmbedded++;
            }

            //marked signal float
            float accelerometer_x_w;

            //if the signal is negative
            if(Acceleromter_X_binaryChar[0] == '1')
            {
                Acceleromter_X_binaryChar[0] = '0';
                Acceleromter_X_binaryString = String.valueOf(Acceleromter_X_binaryChar);
                Acceleromter_X_bits = Integer.parseInt(Acceleromter_X_binaryString,2);
                accelerometer_x_w = -Float.intBitsToFloat(Acceleromter_X_bits);

            }
            else
            {
                Acceleromter_X_binaryString = String.valueOf(Acceleromter_X_binaryChar);
                Acceleromter_X_bits = Integer.parseInt(Acceleromter_X_binaryString,2);
                accelerometer_x_w = Float.intBitsToFloat(Acceleromter_X_bits);
            }

            embeddingComplete++;

            if(embeddingComplete==rowsOfContainer)
            {
                embeddingReady=false;
            }
        }
    }

    /**
     * Database I/O on different thread
     */
    /*
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
*/
    @Override
    public void onCreate() {
        super.onCreate();

        TAG = "AWARE::"+getResources().getString(R.string.app_name);
        DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");


        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mHeartRate = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        mSensorManager.registerListener(this, mHeartRate, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);


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
        //context_producer = CONTEXT_PRODUCER;

        startService(new Intent(Plugin.this, Procedure.class));

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

        //Stop plugin
        Aware.stopPlugin(this, "com.aware.plugin.template");
    }
}
