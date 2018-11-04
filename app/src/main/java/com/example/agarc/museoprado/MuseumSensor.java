package com.example.agarc.museoprado;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Pair;
import android.widget.Toast;

public abstract class  MuseumSensor implements SensorEventListener {


    private Context cxt;
    private static final float SHAKE_THRESHOLD = 1.2f;
    private static final int SHAKE_WAIT_TIME_MS = 250;
    private static final float ROTATION_THRESHOLD = 2.0f;
    private static final int ROTATION_WAIT_TIME_MS = 100;

    SensorManager mSensorManager;
    Sensor mSensorAcc;
    Sensor mSensorGyr;

    private long mShakeTime = 0;
    private long mRotationTime = 0;

    private Boolean mShake = false;
    private int mLastRotation = -1;

    private enum Rotation {x, y, z, noRotation};
    private enum LinearAcceleration{x,y,z, noAcceleration};
    private enum Orientation{left,right,no_orientation};

    Rotation rotation = Rotation.noRotation;
    LinearAcceleration linearAcceleration = LinearAcceleration.noAcceleration;
    Orientation orientation = Orientation.no_orientation;

    public MuseumSensor(Context cx){
        cxt=cx;
        init();
    }

    public void register(){
        mSensorManager.registerListener(this, mSensorAcc, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorGyr, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregister(){
        mSensorManager.unregisterListener(this);
    }

    private void init(){
        mSensorManager = (SensorManager) cxt.getSystemService(Context.SENSOR_SERVICE);
        mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorGyr = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            linearAcceleration = detectAcceleration(event);
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            Pair<Rotation,Orientation> shake = detectRotation(event);
            rotation = shake.first;
            orientation = shake.second;
        }

        if (linearAcceleration == linearAcceleration.y && rotation == Rotation.x){
            Sup();
        }
        else if (linearAcceleration == LinearAcceleration.x && rotation == Rotation.z){
            if(orientation == Orientation.left)
                Sprevious();
            else if(orientation == Orientation.right)
                Snext();
        }
    }

    private Pair<Rotation,Orientation> detectRotation(SensorEvent event) {
        long now = System.currentTimeMillis();
        Orientation orien = Orientation.no_orientation;
        Rotation rot = Rotation.noRotation;
        if ((now - mRotationTime) > ROTATION_WAIT_TIME_MS) {
            mRotationTime = now;

            if(Math.abs(event.values[0]) > ROTATION_THRESHOLD){
                rot = Rotation.x;
            }
            else if(Math.abs(event.values[1]) > ROTATION_THRESHOLD){
                rot = Rotation.y;
            }
            else if(Math.abs(event.values[2]) > ROTATION_THRESHOLD){
                if(event.values[2] > ROTATION_THRESHOLD)
                    orien = Orientation.right;
                else if(event.values[2] <= -ROTATION_THRESHOLD)
                    orien = Orientation.left;

                rot = Rotation.z;
            }
        }
        Pair<Rotation,Orientation> pair = new Pair<>(rot,orien);

        return pair;
    }


    private LinearAcceleration detectAcceleration(SensorEvent event) {
        long now = System.currentTimeMillis();

        if ((now - mShakeTime) > SHAKE_WAIT_TIME_MS) {
            mShakeTime = now;

            float gX = event.values[0] / SensorManager.GRAVITY_EARTH;
            float gY = event.values[1] / SensorManager.GRAVITY_EARTH;
            float gZ = event.values[2] / SensorManager.GRAVITY_EARTH;

            if(Math.abs(event.values[0]) > SHAKE_THRESHOLD){
                return LinearAcceleration.x;
            }
            else if (Math.abs(event.values[1]) > SHAKE_THRESHOLD){
                return LinearAcceleration.y;
            }
            else if(Math.abs(event.values[2]) > SHAKE_THRESHOLD){
                return LinearAcceleration.z;
            }
        }
        return LinearAcceleration.noAcceleration;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public abstract void Snext();

    public abstract void Sprevious();

    public abstract void Sup();
}
