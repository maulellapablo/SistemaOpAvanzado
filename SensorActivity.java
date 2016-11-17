package com.led.controlAutito;


import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.led.led.R;

import java.io.IOException;
import java.util.UUID;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {
    /**
     * Constants for sensors
     */
    private static final float SHAKE_THRESHOLD = 1.1f;
    private static final int SHAKE_WAIT_TIME_MS = 250;
    private static final float ROTATION_THRESHOLD = 2.0f;
    private static final int ROTATION_WAIT_TIME_MS = 100;
    int cambioAd=0,cambioAt=0,cambioIz=0,cambioDr=0;

    /**
     * Sensors
     */
    private SensorManager mSensorManager;
    private Sensor mSensorAcc;
    private long mShakeTime = 0;
    private long mRotationTime = 0;
    /**
     * UI
     */
    private TextView mAccx,mAccz,mAccy,comandos;
    private Button botonInicio,salida,btnLed;
    /**
     * BT
     */
     public static String address = null;
     private ProgressDialog progress;
     private int vel;
     BluetoothAdapter myBluetooth = null;
     static BluetoothSocket btSocket = null;
     private boolean isBtConnected = false;
     //SPP UUID. Look for it
     static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); //receive the address of the bluetooth device
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_sensor);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mAccx = (TextView) findViewById(R.id.accele_x);
        mAccy = (TextView) findViewById(R.id.accele_y);
        mAccz = (TextView) findViewById(R.id.accele_z);
        comandos = (TextView) findViewById(R.id.comandos);
        botonInicio = (Button)findViewById(R.id.button);
        botonInicio.setBackgroundColor(Color.GREEN);
        botonInicio.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (botonInicio.getText().equals("Iniciar")){
                    botonInicio.setText("Detener");
                    mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                    onResume();
                    botonInicio.setBackgroundColor(Color.RED);
                }else{
                    botonInicio.setText("Iniciar");
                    onPause();
                    botonInicio.setBackgroundColor(Color.GREEN);
                }

            }
        });
        salida = (Button)findViewById(R.id.salir);
        salida.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect();
                // cerrar activity sensor
            }
        });
        btnLed = (Button)findViewById(R.id.buttonLed);
        btnLed.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (btnLed.getText().equals("Luz On")){
                    enviaComand("0");
                    btnLed.setText("Luz Off");
                    btnLed.setBackgroundColor(Color.RED);
                }else{
                    enviaComand("0");
                    btnLed.setText("Luz On");
                    btnLed.setBackgroundColor(Color.GREEN);
                }

            }
        });
        new ConnectBT().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorAcc, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mAccx.setText(R.string.act_main_no_acuracy);
                mAccy.setText(R.string.act_main_no_acuracy);
                mAccz.setText(R.string.act_main_no_acuracy);
            }
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mAccx.setText("x = " + Float.toString(event.values[0]));
            mAccy.setText("y = " + Float.toString(event.values[1]));
            mAccz.setText("z = " + Float.toString(event.values[2]));
            if((event.values[2]<2.5&&event.values[2]>-2.5)&&(event.values[1]<1.5&&event.values[1]>-1.5)){
                msg("Detenido.");
                enviaComand("3");
                //habria que enviar comandos
            }else if (event.values[2]>2.5&& event.values[1]<1.5&&event.values[1]>-1.5) {
                msg("Avanzando.");
                if (event.values[2] > 2.5 && event.values[2] < 4.5&&cambioAd!=7) {
                    cambioAd=7;
                    enviaComand("7");
                } else if (event.values[2] > 4.5 && event.values[2] < 6.5&&cambioAd!=8){
                    enviaComand("8");
                    cambioAd=8;
                }else if(event.values[2]>6.5&&cambioAd!=9){
                    cambioAd=9;
                    enviaComand("9");
                }
                enviaComand("1");
                //habria que enviar comandos y modificar la velocidad segun aumenta
            }else if(event.values[2]<-2.5&& event.values[1]<1.5&&event.values[1]>-1.5){
                msg("Retrocediendo.");
                if (event.values[2] < -2.5 && event.values[2] > -4.5 && cambioAt!=7) {
                    cambioAt=7;
                    enviaComand("7");
                } else if (event.values[2] < -4.5 && event.values[2] > -6.5 && cambioAt!=8){
                    cambioAt=8;
                    enviaComand("8");
                } else if (event.values[2]<-6.5 && cambioAt!=9){
                    cambioAt=9;
                    enviaComand("9");
                }
                enviaComand("2");
                //habria que enviar comandos y modificar la velocidad segun aumenta
            }else if(event.values[1]<-1.5){
                msg("Izquierdando.");
                if(event.values[1]<-1.5&&event.values[1]>-2.5&&cambioIz!=6){
                    cambioIz=6;
                    enviaComand("6");
                }else if(event.values[1]<-2.5&&event.values[1]>-4.5&&cambioIz!=7){
                    cambioIz=7;
                    enviaComand("7");
                }else if(event.values[1]<-4.5&&event.values[1]>-6.5&&cambioIz!=8){
                    cambioIz=8;
                    enviaComand("8");
                }else if(event.values[1]<-6.5&&cambioIz!=9){
                    cambioIz=9;
                    enviaComand("9");
                }
                enviaComand("4");
                //habria que enviar comandos y modificar la velocidad segun aumenta
            }else if (event.values[1]>1.5){
                msg("Derecheando.");
                if(event.values[1]>1.5&&event.values[1]<2.5&&cambioIz!=6){
                    cambioIz=6;
                    enviaComand("6");
                }else if(event.values[1]>2.5&&event.values[1]<4.5&&cambioIz!=7){
                    cambioIz=7;
                    enviaComand("7");
                }else if(event.values[1]>4.5&&event.values[1]<6.5&&cambioIz!=8){
                    cambioIz=8;
                    enviaComand("8");
                }else if(event.values[1]>6.5&&cambioIz!=9){
                    cambioIz=9;
                    enviaComand("9");
                }
                enviaComand("5");
                //habria que enviar comandos y modificar la velocidad segun aumenta
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void msg(String s)
    {
        //Toast.makeText(getApplicationContext(),s, Toast.LENGTH_LONG).show();
        comandos.setText(s);
    }
    public static void enviaComand(String n)
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write(n.toString().getBytes());
            }
            catch (IOException e)
            {
                //msg("Error");
            }
        }
    }

    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout

    }



    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(SensorActivity.this, "Conectando: ", "Por favor, espere...");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("La conexión falló.");
                finish();
            }
            else
            {
                msg("Conectado.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}