package com.example.t5_skylake.hcl.SensorManage;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.example.t5_skylake.hcl.MainActivity;

/**
 * Created by T5-SKYLAKE on 2017/6/9.
 */
public class OrientionSensor implements SensorEventListener {

    private OrientionSensor mOrientation;

    public OrientionSensor(){

    }
    private void init(){


    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        float a = event.values[0];//z
        float b = event.values[1];//x
        float c = event.values[2];//y
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }
}
