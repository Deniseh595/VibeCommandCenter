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
import android.os.SystemClock;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.UUID;

public class Chameleon extends Activity {

    //Control strings
    private final String ONE = "choiceone";
    private final String MATRIXOFF = "choiceoff";
    private final String TWO = "choicetwo";
    private final String THREE = "choicethree";
    private final String FOUR = "choicefour";
    private final String FIVE = "choicefive";
    private final String SIX = "choicesix";
    private final String SEVEN = "choiceseven";
    private final String EIGHT = "choiceeight";




    // GUI Components
    private BluetoothAdapter mBTAdapter;
    private ImageButton choiceone,turnoff,choicetwo,choicethree,choicefour,choicefive,choicesix,choiceseven,choiceeight;
    private TextView chamtxt;

    private final String TAG = "CHAMDEBUG";
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Typeface tx = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/work_sans_med.ttf");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chameleon);

        choiceone = (ImageButton)findViewById(R.id.choice_one);
        choicetwo = (ImageButton)findViewById(R.id.choice_two);
        choicethree = (ImageButton)findViewById(R.id.choice_three);
        choicefour = (ImageButton)findViewById(R.id.choice_four);
        choicefive = (ImageButton)findViewById(R.id.choice_five);
        choicesix = (ImageButton)findViewById(R.id.choice_six);
        choiceseven = (ImageButton)findViewById(R.id.choice_seven);
        choiceeight = (ImageButton)findViewById(R.id.choice_eight);

        turnoff = (ImageButton)findViewById(R.id.matrixoff);

        chamtxt = (TextView)findViewById(R.id.chameleon_title);


        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio
        chamtxt.setTypeface(tx);


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
            // Device does not support Bluetooth
          //  mBluetoothStatus.setText(R.string.bt_not_found);
            Log.d(TAG, "BT not found..");
        }
        else {

            choiceone.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(mConnectedThread != null){ //First check to make sure thread created
                        mConnectedThread.write(ONE);}
                        Toast.makeText(getApplicationContext(),"choiceone",Toast.LENGTH_SHORT).show();
                }
            });

            choicetwo.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(mConnectedThread != null){ //First check to make sure thread created
                    mConnectedThread.write(TWO);}
                        Toast.makeText(getApplicationContext(),"choicetwo",Toast.LENGTH_SHORT).show();

                }
            });


            choicethree.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(mConnectedThread != null){ //First check to make sure thread created
                        mConnectedThread.write(THREE);}
                    Toast.makeText(getApplicationContext(),"choicethree",Toast.LENGTH_SHORT).show();

                }
            });

            choicefour.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(mConnectedThread != null){ //First check to make sure thread created
                        mConnectedThread.write(FOUR);}
                    Toast.makeText(getApplicationContext(),"choicefour",Toast.LENGTH_SHORT).show();

                }
            });

            choicefive.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(mConnectedThread != null){ //First check to make sure thread created
                        mConnectedThread.write(FIVE);}
                    Toast.makeText(getApplicationContext(),"choicefive",Toast.LENGTH_SHORT).show();

                }
            });

            choicesix.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(mConnectedThread != null){ //First check to make sure thread created
                        mConnectedThread.write(SIX);}
                    Toast.makeText(getApplicationContext(),"choicesix",Toast.LENGTH_SHORT).show();

                }
            });

            choiceseven.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(mConnectedThread != null){ //First check to make sure thread created
                        mConnectedThread.write(SEVEN);}
                    Toast.makeText(getApplicationContext(),"choiceseven",Toast.LENGTH_SHORT).show();

                }
            });

            choiceeight.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(mConnectedThread != null){ //First check to make sure thread created
                        mConnectedThread.write(EIGHT);}
                    Toast.makeText(getApplicationContext(),"choiceeight",Toast.LENGTH_SHORT).show();

                }
            });

            turnoff.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    if (mConnectedThread != null){
                        mConnectedThread.write(MATRIXOFF);}
                        Toast.makeText(getApplicationContext(),"choiceoff",Toast.LENGTH_SHORT).show();

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
