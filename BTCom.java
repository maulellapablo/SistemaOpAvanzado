package com.led.controlAutito;

import android.app.Application;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class BTCom extends Application {
    public static String address = null;
    private ProgressDialog progress;
    private int vel;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    public BTCom(String a) {
        address=a;
        new ConnectBT().execute();
    }

    public void enviaComand(String n)
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

    public void Disconnect() {
        if (btSocket != null) //If the btSocket is busy
        {
            try {
                btSocket.close(); //close connection
            } catch (IOException e) { //msg("Error");}
            }
            isBtConnected=false;
            //return to the first layout

        }
    }

    public boolean connectDevice(String a) {
        address=a;
        CheckBt();
        BluetoothDevice device = myBluetooth.getRemoteDevice(address);
        Log.d(TAG, "Connecting to ... " + device);
        myBluetooth.cancelDiscovery();
        try {
            btSocket = device.createRfcommSocketToServiceRecord(myUUID);
            btSocket.connect();
            //outStream = btSocket.getOutputStream();
            Log.d(TAG, "Connection made.");
            return true;

        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                Log.d(TAG, "Unable to end the connection");
                return false;
            }
            Log.d(TAG, "Socket creation failed");
        }
        return false;

    }
    private void CheckBt() {
        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if (!myBluetooth.isEnabled()) {
            System.out.println("Bt dsbld");
        }

        if (myBluetooth == null) {
            System.out.println("Bt null");
        }
    }

    public class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(BTCom.this, "Conectando:", "Por favor, espere...");  //show a progress dialog
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
                //msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                //
            }
            else
            {
                //msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

}
