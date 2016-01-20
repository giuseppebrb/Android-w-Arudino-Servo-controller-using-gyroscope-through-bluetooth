package com.burb.arduino.servocontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * This app allow user to control a servo on Arudino by Bluetooth
 * @author Giuseppe Barbato
 *
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // Bluetooth stuff
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;

    // SPP UUID
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Bluetooth device MAC address -- YOU HAVE TO INSERT YOUR OWN!!
    private static String address = "00:00:00:00:00:00";

    private SensorManager mSensorManager;

    private TextView txtX, txtY, txtZ, dataSent;
    private ImageView rightArrow, leftArrow;

    /**
     * On Activity Creation
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // keep the screen awake
        toolbar.setTitle("Gyroscope + Arduino");
        setSupportActionBar(toolbar);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        txtX = (TextView) findViewById(R.id.txtX);
        txtY = (TextView) findViewById(R.id.txtY);
        txtZ = (TextView) findViewById(R.id.txtZ);
        dataSent = (TextView) findViewById(R.id.dataSent);

        rightArrow = (ImageView) findViewById(R.id.rArrow);
        leftArrow = (ImageView) findViewById(R.id.lArrow);

        // Hide imageview
        rightArrow.setVisibility(View.GONE);
        leftArrow.setVisibility(View.GONE);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();
    }

    /**
     * On Activity Resumed
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Create a pointer to the device by its MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            errorExit("Error", "In onResume() happened the following error: " + e.getMessage() + ".");
        }

        btAdapter.cancelDiscovery();

        // Resuming the bluetooth connection
        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Error", "Uunable to close connection after connection failure: " + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            errorExit("Error", "Stream creation failed: " + e.getMessage() + ".");
        }


        // Register the gyroscope sensor
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);

    }

    /**
     * On Activity Stopped
     */
    @Override
    public void onPause() {
        super.onPause();
        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
            }
        }

        try {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }

        mSensorManager.unregisterListener(this);
    }

    /**
     * When user moves, values change
     * @param se event
     */
    @Override
    public void onSensorChanged(SensorEvent se) {
        int prevVal = 0;

        int x = (int) se.values[0];
        int y = (int) se.values[1];
        int z = (int) se.values[2];
        txtX.setText("X value: " + x);
        txtY.setText("Y value: " + y);
        txtZ.setText("Z value: " + z);

        if (y > 0) {
            leftArrow.setVisibility(View.VISIBLE);
            rightArrow.setVisibility(View.GONE);
        } else if (y == 0) {
            rightArrow.setVisibility(View.GONE);
            leftArrow.setVisibility(View.GONE);
        } else {
            rightArrow.setVisibility(View.VISIBLE);
            leftArrow.setVisibility(View.GONE);
        }

        if (prevVal != y) {
            if (y % 5 == 0) {
                sendOrientation(y);
                dataSent.setText("Data Sent: " + y);
                prevVal = y;
            } else {
                dataSent.setText("No Data Sent: Not divisible");
                prevVal = y;
            }
        } else {
            dataSent.setText("No Data Sent: Same Value");
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * Checks the bluetooth connection between Android and Arduino.
     */
    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned
        // on

        if (btAdapter == null) {
            errorExit("Fatal Error", "Bluetooth Not supported. Aborting.");
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                // Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    /**
     * Create a {@link Toast} to notify user about an event.
     *
     * @param message to display
     */
    private void notifier(String message) {
        Toast msg = Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT);
        msg.show();
    }

    /**
     * Create a {@link Toast} to notify user that something went wrong and close
     * the application.
     *
     * @param title   of the error notify
     * @param message to display
     */
    private void errorExit(String title, String message) {
        Toast msg = Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_SHORT);
        msg.show();
        finish();
    }

    /**
     * Sends a string (through serial communication) to arduino to turn on a
     * LED. Ex. "RED ON:" ---> Note that char ':' is the escape I chose to end a
     * command with arduino.
     *
     * @param yPosition to turn on
     */
    private void sendOrientation(int yPosition) {
        if (btSocket != null) {
            try {
                // Create the command that will be sent to arduino.
                String value = yPosition + " :";

                // String must be converted in its bytes to be sent on serial
                // communication
                btSocket.getOutputStream().write(value.getBytes());
            } catch (IOException e) {
                dataSent.setText("Error passing data");
            }
        }
    }

}
