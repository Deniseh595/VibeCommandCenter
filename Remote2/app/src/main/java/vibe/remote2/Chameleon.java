package vibe.remote2;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.UUID;

public class Chameleon extends Activity {

    //Control strings
    private final String MATRIXON = "1";
    private final String MATRIXOFF = "0";


    // GUI Components
    private BluetoothAdapter mBTAdapter;
    private Button choiceone,turnoff,choicetwo,choicethree;

    private final String TAG = "bluetoothdebug";
    private Handler mHandler; // Main handler that will receive callback notifications
    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path

    private static final UUID BTMODULEUUID = UUID.fromString("7be1fcb3-5776-42fb-91fd-2ee7b5bbb86d"); // "random" unique identifier
    private static String ADDRESS = "00:1A:7D:DA:71:13";


    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"IN ONCREATE...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chameleon);


        choiceone = (Button)findViewById(R.id.matrixon);
        turnoff = (Button)findViewById(R.id.matrixoff);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio



        mHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                if(msg.what == MESSAGE_READ){
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    //mReadBuffer.setText(readMessage);
                }

                if(msg.what == CONNECTING_STATUS){
                    if(msg.arg1 == 1){}
                     //   mBluetoothStatus.setText("Connected to Device: " + (String)(msg.obj));
                    else{}
                    //   mBluetoothStatus.setText("Connection Failed");
                }
            }
        };

        if (mBTAdapter == null) {
            // Device does not support Bluetooth
          //  mBluetoothStatus.setText(R.string.bt_not_found);
            Log.d(TAG, "BT not found..");
        }
        else {

            choiceone.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(mConnectedThread != null) //First check to make sure thread created
                        mConnectedThread.write(MATRIXON);
                }
            });

            turnoff.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    if (mConnectedThread != null)
                        mConnectedThread.write(MATRIXOFF);
                }
            });

        }
    }


    @Override
    protected void onResume() {
        Log.d(TAG,"..IN ON RESUME..");
        super.onResume();
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        mBTAdapter.cancelDiscovery();
        bluetoothOn();
        connectDevice();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "..IN ON PAUSE..");
        try {
            mBTSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "..IN ON DESTROY..");
        super.onDestroy();
        // if (mConnectedThread !=null)     {
        //     mConnectedThread.cancel();  }
        // bluetoothOff();
    }

    private void bluetoothOn(){
        Log.d(TAG,"..IN BT ON...") ;
        if (!mBTAdapter.isEnabled()) {
            //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            mBTAdapter.enable();
           // mBluetoothStatus.setText(R.string.bt_enabled);
            Log.d(TAG, "BT turned on..");
        }
        else{
            Log.d(TAG, "BT is already on...");
        }

    }


    private void bluetoothOff(){
        Log.d(TAG,"..IN BT OFF..");
        mBTAdapter.disable(); // turn off
      //  mBluetoothStatus.setText(R.string.bt_disabled);
        Log.d(TAG,"Bluetooth turned off");
    }


    public void connectDevice() {

        if(!mBTAdapter.isEnabled()) {
            Log.d(TAG, "BT not on");
            return;
        }

        //mBluetoothStatus.setText(R.string.connecting);

        new Thread()
        {
            public void run() {
                boolean fail = false;

                BluetoothDevice device = mBTAdapter.getRemoteDevice(ADDRESS);

                try {
                    mBTSocket = createBluetoothSocket(device);
                } catch (IOException e) {
                    fail = true;
                    Log.d(TAG, "Socket creation failed..");
                }
                // Establish the Bluetooth socket connection.
                try {
                    mBTSocket.connect();
                } catch (IOException e) {
                    try {
                        fail = true;
                        mBTSocket.close();
                        mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                .sendToTarget();
                    } catch (IOException e2) {
                        Log.d(TAG, "Socket creation failed..");
                    }
                }
                if(!fail) {
                    mConnectedThread = new ConnectedThread(mBTSocket);
                    mConnectedThread.start();

                    mHandler.obtainMessage(CONNECTING_STATUS, 1, -1,"Chameleon")
                            .sendToTarget();
                }
            }
        }.start();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BTMODULEUUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection",e);
        }
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true)  {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if(bytes != 0) {
                        buffer = new byte[1024];
                        SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget(); // Send the obtained bytes to the UI activity
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { e.printStackTrace();}
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }


}
