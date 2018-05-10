package vibe.remote2;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.support.v7.widget.AppCompatTextView;
import android.widget.Toast;


import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.UUID;

public class LEDControl extends Activity {

    //Control variables
    private final String LEDON="ledon";
    private final String LEDOFF = "ledoff";
    private final String BRIGHTEN="brighten";
    private final String DIM = "dim";
    private final String CLOUDON = "cloudon";
    private final String CLOUDOFF = "cloudoff";


    // GUI Components
    private BluetoothAdapter mBTAdapter;
    private Switch neopixel,cloud;
    private Button brightenbtn,dimbtn;
    private ProgressBar loading;
    private TextView mood,cabintxt,brighttxt,cloudtxt;

    private final String TAG = "bluetoothdebug";
    private Handler mHandler; // Main handler that will receive callback notifications
    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier
    private static String ADDRESS = "98:D3:32:20:C8:7A";


    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"IN ONCREATE...");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Typeface tx = ResourcesCompat.getFont(getApplicationContext(), R.font.work_sans_semibold);
        Typeface tx2 = ResourcesCompat.getFont(getApplicationContext(),R.font.work_sans);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led);

        neopixel = (Switch) findViewById(R.id.switch_neopixel);
        cloud = (Switch) findViewById(R.id.btn_cloud);
        brightenbtn = (Button) findViewById(R.id.btn_brighten);
        dimbtn = (Button) findViewById(R.id.btn_dim);
        loading = (ProgressBar)findViewById(R.id.pBar);
        mood = (TextView) findViewById(R.id.cabin_lighting);
        cabintxt = (TextView) findViewById(R.id.cabin_text);
        brighttxt = (TextView)findViewById(R.id.bright_text);
        cloudtxt = (TextView)findViewById(R.id.cloud_text);



        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio
        mood.setTypeface(tx);
        cabintxt.setTypeface(tx2);
        brighttxt.setTypeface(tx2);
        cloudtxt.setTypeface(tx2);

        mHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                if(msg.what == MESSAGE_READ){
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    // mReadBuffer.setText(readMessage);
                }

                if(msg.what == CONNECTING_STATUS){
                    if(msg.arg1 == 1){
                       // mBluetoothStatus.setText("Connected to Device: " + (String)(msg.obj));
                        findViewById(R.id.pBar).setVisibility(View.GONE);
                    }
                    else {}
                    Toast.makeText(getApplicationContext(),"Connection Failed",Toast.LENGTH_SHORT).show();
                    // mBluetoothStatus.setText("Connection Failed");
                    findViewById(R.id.pBar).setVisibility(View.GONE);

                }
            }
        };

        if (mBTAdapter == null) {
            //Device does not support Bluetooth
            //mBluetoothStatus.setText(R.string.bt_not_found);
            Log.d(TAG, "BT device not found...");
        }
        else {
             neopixel.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                if(mConnectedThread != null)
                                mConnectedThread.write(LEDON);
                                Toast.makeText(getApplicationContext(),"ledon",Toast.LENGTH_SHORT).show();

                            } else {
                                if(mConnectedThread != null)
                                mConnectedThread.write(LEDOFF);
                                Toast.makeText(getApplicationContext(),"ledoff",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
             brightenbtn.setOnClickListener(
                     new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             if(mConnectedThread != null)
                             mConnectedThread.write(BRIGHTEN);
                             Toast.makeText(getApplicationContext(),"brighten",Toast.LENGTH_SHORT).show();
                         }
                     }
             );
             dimbtn.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(mConnectedThread != null)
                            mConnectedThread.write(DIM);
                            Toast.makeText(getApplicationContext(),"dim",Toast.LENGTH_SHORT).show();
                        }
                    }
            );
            cloud.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                if(mConnectedThread != null)
                                mConnectedThread.write(CLOUDON);
                                Toast.makeText(getApplicationContext(),"cloudon",Toast.LENGTH_SHORT).show();

                            } else {
                                if(mConnectedThread != null)
                                mConnectedThread.write(CLOUDOFF);
                                Toast.makeText(getApplicationContext(),"cloudoff",Toast.LENGTH_SHORT).show();
                            }
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
        loading = (ProgressBar)findViewById(R.id.pBar);
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
            Log.d(TAG, "Bluetooth enabled..");
        }
        else{
           Log.d(TAG, "Bluetooth already on..");
        }

    }


    private void bluetoothOff(){
        Log.d(TAG,"..IN BT OFF..");
        mBTAdapter.disable(); // turn off
       // mBluetoothStatus.setText(R.string.bt_disabled);
        Log.d(TAG, "BT not on...");
    }



    public void connectDevice() {

            if(!mBTAdapter.isEnabled()) {
                Log.d(TAG, "BT not on..");
                return;
            }

          //  mBluetoothStatus.setText(R.string.connecting);

            new Thread()
            {
                public void run() {
                    boolean fail = false;

                    BluetoothDevice device = mBTAdapter.getRemoteDevice(ADDRESS);

                    try {
                        mBTSocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        fail = true;
                        Log.d(TAG, "Socket creation failed...");
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

                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1,"HC-05")
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
            Log.d(TAG,"SENDING VALUE:" + input);
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
