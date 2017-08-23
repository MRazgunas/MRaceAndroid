package lt.razgunas.mraceandroid;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.Manifest;

import lt.razgunas.mraceandroid.RacelinkService.RacelinkServiceApi;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private RacelinkServiceApi mRaceLink;

    private static final int PERMISSON_COARSE_LOCATION = 0;

    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDevice> mBTDevice = new ArrayList<>();
    private DeviceListAdapter mDeviceListAdapter;
    private ListView lvNewDevices;
    private Dialog btDevicesDialog;

    private ServiceConnection raclinkConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mRaceLink = (RacelinkServiceApi) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mRaceLink = null;
        }
    };

    private final BroadcastReceiver mDeviceFound = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevice.add(device);
                Log.i(TAG, "Device found " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.bt_device_list, mBTDevice);
                lvNewDevices.setAdapter(mDeviceListAdapter);
                return;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent racelinkService = new Intent(this, RacelinkService.class);
        bindService(racelinkService, raclinkConnection, BIND_AUTO_CREATE);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Button connectVts = (Button) findViewById(R.id.btnConnectVts);
        connectVts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mRaceLink != null) {
                    if(mBluetoothAdapter == null) {
                        Toast.makeText(view.getContext(), "No bluetooth found", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(ContextCompat.checkSelfPermission(view.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Log.i(TAG, "We do not have permission for Bluetooth discovery");
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSON_COARSE_LOCATION);
                    } else
                        startDiscovery();
                }

            }
        });
    }

    private void startDiscovery() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "We do not have permission for Bluetooth discovery");
        } else {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            mBTDevice.clear();
            btDevicesDialog = new Dialog(this);
            btDevicesDialog.setContentView(R.layout.bt_device_select_dialog);
            lvNewDevices = (ListView) btDevicesDialog.findViewById(R.id.lwBTDevices);
            btDevicesDialog.setCancelable(true);
            btDevicesDialog.setTitle("Devices found: ");
            btDevicesDialog.show();
            mBluetoothAdapter.startDiscovery();
            IntentFilter discoveryIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mDeviceFound, discoveryIntent);
            Log.i(TAG, "Bluetooth discovery started");
            lvNewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    mBluetoothAdapter.cancelDiscovery();
                    mRaceLink.setBTaddress(mBTDevice.get(i).getAddress());
                    mRaceLink.startConnection();
                    btDevicesDialog.dismiss();

                }
            });
            //Now we will pop up list and it will be further handled in mDeviceFound
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSON_COARSE_LOCATION: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mRaceLink.startConnection();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
