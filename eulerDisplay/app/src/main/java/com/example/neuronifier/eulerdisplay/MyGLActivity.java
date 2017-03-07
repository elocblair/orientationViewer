package com.example.neuronifier.eulerdisplay;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.util.Log;

import android.widget.TextView;

import java.util.List;
import java.util.UUID;

public class MyGLActivity extends AppCompatActivity
{

    private MyGLSurfaceView mGLView;
    private static final String TAG = "Cole";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothDevice peripheral = null;
    private static Context context;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyGLActivity.context = getApplicationContext();
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        setContentView(R.layout.activity_my_gl);
        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity
        mGLView = new MyGLSurfaceView(this);
        //setContentView(mGLView);
        //mGLView.setAngles(0,0,0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
        //mGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
        //mGLView.onResume();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        //check status
        mBluetoothLeScanner.stopScan(mScanCallback);
        if (mBluetoothGatt != null){
            mBluetoothGatt.close();
        }
        Log.v("onDestroy ", "destroyed");
    }
    @Override
    protected void onStop(){
        super.onStop();
        mBluetoothLeScanner.stopScan(mScanCallback);
        if (mBluetoothGatt != null){
            mBluetoothGatt.close();
        }
    }
    public static Context getAppContext(){
        return MyGLActivity.context;
    }

    public ScanCallback mScanCallback = new ScanCallback()
    {
        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {
            Log.d(TAG, "onScanResult");

            processResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results)
        {
            Log.d(TAG, "onBatchScanResults: " + results.size() + " results");
            for (ScanResult result : results)
            {
                processResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode)
        {
            Log.d(TAG, "LE Scan Failed: " + errorCode);
        }

        private void processResult(ScanResult device)
        {
            Log.i(TAG, "New LE Device: " + device.getDevice().getName() + " @ " + device.getRssi());
            Log.d(TAG, "Address " + device.getDevice().getAddress());
            String addressDevice;
            addressDevice = device.getDevice().getAddress();
            //myDevices[i] = addressDevice;
            /*if (i == 1){//i1

                populateListView();
                i++;
            }
            else {
                if (addressDevice != myDevices[i-1]){

                    populateListView();
                }
                i++;
            }*/
            if (addressDevice.equals("EA:AF:48:9B:35:F7")) // ShockClock
            //if (addressDevice.equals("B0:B4:48:C9:93:01")) // MPU9250
            {
                // GATT CONNECT
                mBluetoothLeScanner.stopScan(mScanCallback);
                peripheral = device.getDevice();
                mBluetoothGatt = peripheral.connectGatt(getAppContext(),false,mGattCallback);
            }
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case PERMISSION_REQUEST_COARSE_LOCATION:
            {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Log.d(TAG, "coarse location permission granted");
                    mBluetoothLeScanner.startScan(mScanCallback);
                    //final String stringState = "" THIS IS WHERE YOU WERE
                    //setContentView(mGLView);
                    //Connect without scanning
                    //BluetoothDevice device = mBluetoothAdapter.getRemoteDevice("B0:B4:48:C3:EE:01");
                    //mBluetoothGatt = device.connectGatt(this,false,mGattCallback);

                } else
                {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener()
                    {

                        @Override
                        public void onDismiss(DialogInterface dialog)
                        {

                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    //private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback()
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = STATE_CONNECTED;

                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" +
                        gatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                mBluetoothLeScanner.startScan(mScanCallback);

            }
        }
        BluetoothGattCharacteristic Char1;
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            super.onServicesDiscovered(gatt, status);
            if(status == BluetoothGatt.GATT_SUCCESS){
                BluetoothGattService service = gatt.getService(UUID.fromString("0000f00d-1212-efde-1523-785fef13d123"));
                Log.d("Service:",service.getUuid().toString());
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for (int i = 0; i <characteristics.size(); i++)
                {
                    Log.v("Characteristic", characteristics.get(i).getUuid().toString());
                    if(characteristics.get(i).getUuid().toString().equals("0000beef-1212-efde-1523-785fef13d123"))//ShockClock
                    {
                        Char1 = characteristics.get(i);
                        boolean readStatus = mBluetoothGatt.readCharacteristic(Char1);
                        Log.v("readStatus ", "" + readStatus);
                    }
                }
            }
        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            super.onCharacteristicRead(gatt, characteristic, status);
            if( gatt == mBluetoothGatt){
                if(status == BluetoothGatt.GATT_SUCCESS){
                    byte [] temp = characteristic.getValue();
                    int MSB = temp[1] << 8;
                    int LSB = temp[0]&0x000000FF;
                    int val = MSB|LSB;
                    float gyroW = val/16384.0f;
                    MSB = temp[3] << 8;
                    LSB = temp[2]&0x000000FF;
                    val = MSB|LSB;
                    float gyroX = val/16384.0f;
                    MSB = temp[5] << 8;
                    LSB = temp[4]&0x000000FF;
                    val = MSB|LSB;
                    float gyroY = val/16384.0f;
                    MSB = temp[5] << 8;
                    LSB = temp[4]&0x000000FF;
                    val = MSB|LSB;
                    float gyroZ = val/16384.0f;

                    mGLView.setAngles(gyroW,gyroX,gyroY, gyroZ);

                    final String xString = Float.toString(gyroX) + "\n "+ Float.toString(gyroY) + "\n " + Float.toString(gyroZ) + "\n";
                    /*runOnUiThread(new Runnable(){
                        @Override
                        public void run()
                        {
                            updateTextView(xString);
                        }
                    });*/

                    Log.v("Sensor Values", " " + gyroW + ", "  + gyroX + ", " + gyroY + ", " + gyroZ);

                    mBluetoothGatt.readCharacteristic(Char1);
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            super.onCharacteristicWrite(gatt, characteristic, status);


        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {
            super.onCharacteristicChanged(gatt, characteristic);

        }
    };


}
