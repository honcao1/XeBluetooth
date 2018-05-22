package dhcn.honcao.xebluetooth;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button btnTien, btnLui, btnTrai, btnPhai, btnScan, btnChupHinh;
    ImageView imvHinh;
    TextView textView;
    RelativeLayout manhinh;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int CONNECT_BT = 2;
    private static final int MESSAGE_READ = 3;

    private static int fileIndex = 0;
    byte[] imageBuffer = new byte[15360];  // 15KB reserved

    Handler mHandler;
    StringBuilder dataBluetoorh = new StringBuilder();

    ConnectedThread bluetooth;

    BluetoothAdapter mBluetoothAdapter = null;
    BluetoothDevice mBluetoothDevice = null;
    BluetoothSocket mBluetoothSocket = null;

    UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    boolean connect = false;

    private static String MAC = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initBluetooth();
        addControls();
        addEvents();
    }

    private void initBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "App stop", Toast.LENGTH_LONG).show();
        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void addEvents() {
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (connect) {
                    //disconnect
                    try {
                        mBluetoothSocket.close();
                        Toast.makeText(getApplicationContext(), "DISCONNECT", Toast.LENGTH_LONG).show();
                        connect = false;
                        btnScan.setText("scan");
                    } catch (IOException err){}
                } else {
                    //connect
                    Intent abreLista = new Intent(MainActivity.this, ListDevices.class);
                    startActivityForResult(abreLista, CONNECT_BT);
                }
            }
        });

        btnTien.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction()==MotionEvent.ACTION_DOWN) {
                    //tvXe.setText("XE TIEN");
                    bluetooth.write("1");
                } else if (motionEvent.getAction()==MotionEvent.ACTION_UP) {
                    //tvXe.setText("XE DUNG");
                    bluetooth.write("0");
                }
                return false;
            }
        });

        btnLui.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction()==MotionEvent.ACTION_DOWN) {
                    //tvXe.setText("XE LUI");
                    bluetooth.write("2");
                } else if (motionEvent.getAction()==MotionEvent.ACTION_UP) {
                    //tvXe.setText("XE DUNG");
                    bluetooth.write("0");
                }
                return false;
            }
        });

        btnTrai.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction()==MotionEvent.ACTION_DOWN) {
                    //tvXe.setText("XE TRAI");
                    bluetooth.write("3");
                } else if (motionEvent.getAction()==MotionEvent.ACTION_UP) {
                    //tvXe.setText("XE DUNG");
                    bluetooth.write("0");
                }
                return false;
            }
        });

        btnPhai.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction()==MotionEvent.ACTION_DOWN) {
                    //tvXe.setText("XE PHAI");
                    bluetooth.write("4");
                } else if (motionEvent.getAction()==MotionEvent.ACTION_UP) {
                    //tvXe.setText("XE DUNG");
                    bluetooth.write("0");
                }
                return false;
            }
        });
    }

    private void addControls() {
        btnTien = (Button)findViewById(R.id.btnTien);
        btnLui = (Button)findViewById(R.id.btnLui);
        btnTrai = (Button)findViewById(R.id.btnTrai);
        btnPhai = (Button)findViewById(R.id.btnPhai);
        btnScan = (Button)findViewById(R.id.btnScan);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(getApplicationContext(),"Bluetooth on", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),"Bluetooth off", Toast.LENGTH_LONG).show();
                }
                break;
            case CONNECT_BT:
                if (resultCode == Activity.RESULT_OK) {
                    MAC = data.getExtras().getString(ListDevices.CONNECT_MAC);

                    //Toast.makeText(getApplicationContext(),"MAC" + MAC, Toast.LENGTH_LONG).show();
                    mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(MAC);
                    try {
                        mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(mUUID);

                        mBluetoothSocket.connect();

                        bluetooth = new ConnectedThread(mBluetoothSocket);
                        bluetooth.start();

                        connect = true;

                        btnScan.setText("disconnect");

                        Toast.makeText(getApplicationContext(), "CONNECT", Toast.LENGTH_LONG).show();
                    } catch (IOException err){
                        Toast.makeText(getApplicationContext(),"ERR" + err, Toast.LENGTH_LONG).show();
                    }
                } else {
                    //Toast.makeText(getApplicationContext(),"Bluetooth off", Toast.LENGTH_LONG).show();
                }
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {}

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] inBuffer = new byte[1024];
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    bytes = mmInStream.read(inBuffer);
                    //mHandler.obtainMessage(MESSAGE_READ, bytes, -1, inBuffer).sendToTarget();

                    for (int i = 0; i < bytes; i++) {
                        imageBuffer[fileIndex] = inBuffer[i];
                        fileIndex++;
                        //Log.d("red", bytes+"="+String.format("%02X", inBuffer[i]));
                        if (i > 0) {
                            if (inBuffer[i] == (byte) 0xD9) {
                                if (inBuffer[i - 1] == (byte) 0xFF) {
                                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, inBuffer).sendToTarget();
                                }
                            }
                        }
                    }

                } catch (IOException e) {

                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(String dataEvent) {
            try {
                byte[] msgBuffer = dataEvent.getBytes();
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {

            }
        }
    }
}

