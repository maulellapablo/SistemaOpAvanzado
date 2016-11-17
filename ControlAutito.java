package com.led.controlAutito;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.led.led.R;

import java.io.IOException;
import java.util.UUID;


public class ControlAutito extends AppCompatActivity {

    Button btnAD, btnAT, btnDis,btnAI,btnADe, btnLed,btnSensor;
    SeekBar velocidad;
    TextView comandos;
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
        //view of the ControlAutito
        setContentView(R.layout.activity_control_autito);

        //call the widgtes
        btnAD = (Button)findViewById(R.id.buttonAD);
        btnAT = (Button)findViewById(R.id.buttonAT);
        btnAI = (Button)findViewById(R.id.buttonAI);
        btnADe = (Button)findViewById(R.id.buttonADe);
        btnDis = (Button)findViewById(R.id.buttonDisc);
        btnLed = (Button)findViewById(R.id.buttonLed);
        btnLed.setBackgroundColor(Color.GREEN);
        velocidad = (SeekBar)findViewById(R.id.barraVel);
        comandos = (TextView)findViewById(R.id.textComando);

        new ConnectBT().execute(); //Call the class to connect

        //commands to be sent to bluetooth
        // Avanzando
        btnAD.setOnTouchListener    (new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    enviaComand("1");      //method to turn on
                    msg("Avanzado");
                }else if (event.getAction() == MotionEvent.ACTION_UP) {
                    //que pare
                    enviaComand("3");   //method to turn off
                    msg("Detenido");
                }
                return true;
            }

        });

        //Luz

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


        //Izquierdando

        btnAI.setOnTouchListener    (new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                   // ((BTCom)getApplication()).
                            enviaComand("4");      //method to turn on
                    msg("Girando a la Izquierda");
                }else if (event.getAction() == MotionEvent.ACTION_UP) {
                    //que pare
                    enviaComand("3");   //method to turn off
                    msg("Detenido");
                }
                return true;
            }

        });

        //Derechando

        btnADe.setOnTouchListener    (new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    enviaComand("5");      //method to turn on
                    msg("Girando a la Derecha");
                }else if (event.getAction() == MotionEvent.ACTION_UP) {
                    //que pare
                    enviaComand("3");   //method to turn off
                    msg("Detenido");
                }
                return true;
            }

        });

        //Atraquiando



        btnAT.setOnTouchListener    (new View.OnTouchListener() {

            private Handler mHandler;
            private int count = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {

                    if (mHandler != null)
                        return true;
                    mHandler = new Handler();
                    mHandler.postDelayed(mAction, 100);

                   /* while(event.getAction()!= MotionEvent.ACTION_UP) {

                    }*/

                }else if (event.getAction() == MotionEvent.ACTION_UP) {

                    if (mHandler == null)
                        return true;
                    mHandler.removeCallbacks(mAction);
                    mHandler = null;
                    //que pare
                    enviaComand("3");   //method to turn off
                    msg("Detenido");
                }
                return true;


                    }
            Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    enviaComand("2");     //method to turn on
                    msg("Retrocediendo");
                    mHandler.postDelayed(this, 100);

            }
            };
        });

        btnDis.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect();
                 //close connection
            }
        });
        velocidad.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                    msg("Velocidad1: "+vel); //cuando soltas el seekbar
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                    msg("Velocidad2: "+vel); //nunca se usa
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                    // TODO Auto-generated method stub
                    vel=progress;
                    msg("Velocidad3: "+vel); //cuando se esta modificando
                    if(vel>=0&&vel<=100){
                        enviaComand("6");
                    }else if(vel<=150){
                        enviaComand("7");
                    }else if(vel<=200){
                        enviaComand("8");
                    }else{
                        enviaComand("9");
                    }
                }
            });


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
            progress = ProgressDialog.show(ControlAutito.this, "Conectando: ", "Por favor, espere...");  //show a progress dialog
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
