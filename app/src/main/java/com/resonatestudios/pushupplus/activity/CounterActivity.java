package com.resonatestudios.pushupplus.activity;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.resonatestudios.pushupplus.R;

import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class CounterActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {
    //<editor-fold desc="Sensor Components and Variables">
    private SensorManager sensorManager;
    private Sensor sensorProximity;
    private double prevValue;
    private double currValue;
    private int count = 0;
    //</editor-fold>
    //<editor-fold desc="Timer Variables">
    private long MillisecondTime, StartTime, TimeBuff;
    private Handler handler;
    //</editor-fold>
    //<editor-fold desc="UI Components">
    private TextView textViewTimer;
    private TextView textViewCounter;
    private Button buttonStart;
    private Button buttonStop;
    //</editor-fold>

    private Runnable runnableTimer = new Runnable() {
        @SuppressLint({"DefaultLocale", "SetTextI18n"})
        @Override
        public void run() {
            // count time
            MillisecondTime = SystemClock.uptimeMillis() - StartTime;
            long updateTime = TimeBuff + MillisecondTime;
            int seconds = (int) (updateTime / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            int milliSeconds = (int) (updateTime % 1000);

            textViewTimer.setText("" + minutes + ":"
                    + String.format("%02d", seconds) + ":"
                    + String.format("%03d", milliSeconds));

            handler.postDelayed(this, 0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Counter");

        handler = new Handler();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorProximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        textViewTimer = findViewById(R.id.text_view_timer);
        textViewCounter = findViewById(R.id.text_view_counter);
        buttonStart = findViewById(R.id.button_start);
        buttonStart.setOnClickListener(this);
        buttonStop = findViewById(R.id.button_stop);
        buttonStop.setOnClickListener(this);

        currValue = 0;
        prevValue = sensorProximity.getMaximumRange();

        if (sensorProximity != null) {
            // ada sensor accelerometer
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setMessage("Press start to begin\nPress stop to end and return to Home");
            alertDialog.show();
        } else {
            // tidak ada sensor accelerometer
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setMessage("Proximity sensor not found\nUnfortunately, this means you cannot use this app");
            alertDialog.show();
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        currValue = event.values[0];
        if (currValue == sensorProximity.getMaximumRange() && prevValue == 0) {
            count++;
            String newValue = String.valueOf(count);
            textViewCounter.setText(newValue);
        }
        prevValue = currValue;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_start:
                // start sensor manager
                sensorManager.registerListener(this, sensorProximity, SensorManager.SENSOR_DELAY_NORMAL);
                buttonStart.setEnabled(false);
                buttonStop.setEnabled(true);
                // start timer
                StartTime = SystemClock.uptimeMillis();
                handler.postDelayed(runnableTimer, 0);
                break;
            case R.id.button_stop:
                // stop sensor manager and insert values to sqlite(?)
                sensorManager.unregisterListener(this);
                buttonStart.setEnabled(true);
                buttonStop.setEnabled(false);
                // stop timer
                TimeBuff += MillisecondTime;
                handler.removeCallbacks(runnableTimer);
                break;
        }
    }
}