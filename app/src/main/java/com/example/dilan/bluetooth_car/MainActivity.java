package com.example.dilan.bluetooth_car;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

        private static final String TAG = "bluetooth1";

        Button btnW, btnS, btnA, btnD;

        private static final int REQUEST_ENABLE_BT = 1;
        private BluetoothAdapter btAdapter = null;
        private BluetoothSocket btSocket = null;
        private OutputStream outStream = null;

        // SPP UUID сервиса
        private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        // MAC-адрес Bluetooth модуля
        private static String address = "98:D3:31:30:23:15";

        /** Called when the activity is first created. */
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.activity_main);

            btnW = (Button) findViewById(R.id.btnGo);
            btnS = (Button) findViewById(R.id.btnBack);
            btnA = (Button) findViewById(R.id.btnRight);
            btnD = (Button) findViewById(R.id.btnLeft);

            btAdapter = BluetoothAdapter.getDefaultAdapter();
            checkBTState();

            btnW.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {

                        case MotionEvent.ACTION_DOWN:
                            sendData("W");
                            break;
                        case MotionEvent.ACTION_UP:
                            sendData("T");
                            break;
                    }
                    return true;
                }
            });

            btnS.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            sendData("S");
                            break;
                        case MotionEvent.ACTION_UP:
                            sendData("T");
                            break;
                    }
                    return true;
                }
            });

            btnA.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            sendData("A");
                            break;
                        case MotionEvent.ACTION_UP:
                            sendData("N");
                            break;
                    }
                    return true;
                }
            });

            btnD.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            sendData("D");
                            break;
                        case MotionEvent.ACTION_UP:
                            sendData("N");
                            break;
                    }
                    return true;
                }
            });
        }

        @Override
        public void onResume() {
            super.onResume();

            Log.d(TAG, "...onResume - попытка соединения...");


            BluetoothDevice device = btAdapter.getRemoteDevice(address);


            try {
                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
            }

            btAdapter.cancelDiscovery();

            Log.d(TAG, "...Соединяемся...");
            try {
                btSocket.connect();
                Log.d(TAG, "...Соединение установлено и готово к передачи данных...");
            } catch (IOException e) {
                try {
                    btSocket.close();
                } catch (IOException e2) {
                    errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
                }
            }

            Log.d(TAG, "...Создание Socket...");

            try {
                outStream = btSocket.getOutputStream();
            } catch (IOException e) {
                errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
            }
        }

        @Override
        public void onPause() {
            super.onPause();

            Log.d(TAG, "...In onPause()...");

            if (outStream != null) {
                try {
                    outStream.flush();
                } catch (IOException e) {
                    errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
                }
            }

            try     {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
            }
        }

        private void checkBTState() {
            if(btAdapter==null) {
                errorExit("Fatal Error", "Bluetooth не поддерживается");
            } else {
                if (btAdapter.isEnabled()) {
                    Log.d(TAG, "...Bluetooth включен...");
                } else {
                    Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            }
        }

        private void errorExit(String title, String message){
            Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
            finish();
        }

        private void sendData(String message) {
            byte[] msgBuffer = message.getBytes();

            Log.d(TAG, "...Посылаем данные: " + message + "...");

            try {
                outStream.write(msgBuffer);
            } catch (IOException e) {
                String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
                if (address.equals("00:00:00:00:00:00"))
                    msg = msg + ".\n\nВ переменной address у вас прописан 00:00:00:00:00:00, вам необходимо прописать реальный MAC-адрес Bluetooth модуля";
                msg = msg +  ".\n\nПроверьте поддержку SPP UUID: " + MY_UUID.toString() + " на Bluetooth модуле, к которому вы подключаетесь.\n\n";

                errorExit("Fatal Error", msg);
            }
        }
    }