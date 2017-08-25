package lt.razgunas.mraceandroid;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.MAVLink.Parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.jar.Manifest;

public class BluetoothConnection extends RacelinkConnection {
    private static final String TAG = BluetoothConnection.class.getSimpleName();

    private static final String UUID_BT = "00001101-0000-1000-8000-00805F9B34FB";

    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothSocket mBluetoothSocket;

    private OutputStream out;
    private InputStream in;

    public BluetoothConnection(Context context) {
        mContext = context;
    }

    @Override
    protected void openConnection() throws IOException{
        BluetoothDevice device = null;
        if(mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        if(mBluetoothAdapter == null) return;

        device = mBluetoothAdapter.getRemoteDevice(btAddress);
        if(device == null) {
            Toast.makeText(mContext, "Bluetooth device not avaliable", Toast.LENGTH_LONG).show();
        }

        mBluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(UUID_BT));
        mBluetoothSocket.connect();
        out = mBluetoothSocket.getOutputStream();
        in = mBluetoothSocket.getInputStream();
    }

    @Override
    protected int readDataBlock(byte[] buffer) throws IOException {
        return in.read(buffer);
    }

    @Override
    protected void sendBuffer(byte[] buffer) throws IOException {
        if(out != null) {
            out.write(buffer);
        }
    }

    @Override
    protected void closeConnection() throws IOException {
        if(in != null) {
            in.close();
        }

        if(out != null) {
            out.close();
        }
    }

    @Override
    protected void loadPreferences() {

    }
}
