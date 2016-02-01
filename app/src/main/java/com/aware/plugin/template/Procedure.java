package com.aware.plugin.template;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Comet on 01/02/16.
 */
public class Procedure extends Service implements SensorEventListener {

    private static SensorManager mSensorManager;
    private static Sensor mAccelerometer;


    public Thread receive_thread = new Thread() {
        public void run() {
            while (true) {

                //should not sleep
                /*
                try {
                    Thread.sleep(6000);
                    //detect once every 6 secs
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/

            }
        }
    };




            public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float accelerometer_x = event.values[0];

            Log.d("SENSORS10", "accelerometer_x = " + accelerometer_x);


        }
    }



    @Override
    public void onCreate() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        //SENSOR_DELAY_NORMAL 200ms
        //SENSOR_DELAY_UI     60ms
        //SENSOR_DELAY_GAME   20ms
        //SENSOR_DELAY_FASTEST 0ms

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return 0;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {

    }
}
