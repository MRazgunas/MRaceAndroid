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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_command_long;
import com.MAVLink.common.msg_param_request_list;
import com.MAVLink.common.msg_param_value;
import com.MAVLink.common.msg_racer_lap;
import com.MAVLink.common.msg_racer_pass;
import com.MAVLink.common.msg_vrx_status;
import com.MAVLink.enums.VTS_CMD;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.jar.Manifest;

import lt.razgunas.mraceandroid.RacelinkService.RacelinkServiceApi;
import lt.razgunas.mraceandroid.AppState;

import static com.MAVLink.common.msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT;
import static com.MAVLink.common.msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE;
import static com.MAVLink.common.msg_racer_lap.MAVLINK_MSG_ID_RACER_LAP;
import static com.MAVLink.common.msg_racer_pass.MAVLINK_MSG_ID_RACER_PASS;
import static com.MAVLink.common.msg_vrx_status.MAVLINK_MSG_ID_VRX_STATUS;

public class MainActivity extends AppCompatActivity{

    private static final String TAG = MainActivity.class.getSimpleName();

    private RacelinkServiceApi mRaceLink;

    private static final int PERMISSON_COARSE_LOCATION = 0;


    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDevice> mBTDevice = new ArrayList<>();
    private DeviceListAdapter mDeviceListAdapter;
    private ListView lvNewDevices;
    private Dialog btDevicesDialog;

    private LapsResulsListAdapter mLapsResulsListAdapter;

    private Menu mMenu;
    private Button startRaceBtn;
    private TextView rssiView;

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

    private BroadcastReceiver mRacelinkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case RacelinkConnection.BROADCAST_RACELINK_PACKET:
                    Bundle bundle = intent.getExtras();
                    MAVLinkMessage packet = (MAVLinkMessage) bundle.getSerializable("packet");
                    switch (packet.msgid) {
                        case MAVLINK_MSG_ID_RACER_PASS:
                            msg_racer_pass msg = (msg_racer_pass) packet;
                            if(AppState.getInstance().getLapCount() == 0) {
                                AppState.getInstance().playTone(AppState.TONE_LAP, AppState.TONE_LAP_DURATION);
                            }
                            break;
                        case MAVLINK_MSG_ID_RACER_LAP:
                            msg_racer_lap lap = (msg_racer_lap) packet;
                            LapResult lapTime = new LapResult((int) lap.lap_time_ms);
                            AppState.getInstance().notifyLapPassed(lapTime);
                            mLapsResulsListAdapter.notifyDataSetChanged();
                            break;
                        case MAVLINK_MSG_ID_VRX_STATUS:
                            msg_vrx_status status = (msg_vrx_status) packet;
                            rssiView.setText(Integer.toString(status.rssi));
                            break;
                        case MAVLINK_MSG_ID_PARAM_VALUE:
                            msg_param_value value = (msg_param_value) packet;
                            ParamValue param = new ParamValue(value.getParam_Id(), value.param_index, value.param_value, value.param_type);
                            AppState.getInstance().updateParameter(param);
                    }
                    break;
                case RacelinkConnection.BROADCAST_RACELINK_CONN_STATUS:
                    int connStatus = intent.getIntExtra("status", RacelinkConnection.RACELINK_DISCONNECTED);
                    switch (connStatus) {
                        case RacelinkConnection.RACELINK_CONNECTING:
                            Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show();
                            break;
                        case RacelinkConnection.RACELINK_CONNECTED:
                            mMenu.findItem(R.id.action_connect).setTitle("Disconnect");
                            Toast.makeText(context, "Connected", Toast.LENGTH_LONG).show();
                            msg_param_request_list req_param = new msg_param_request_list();
                            mRaceLink.sendData(req_param.pack());
                            break;
                        case RacelinkConnection.RACELINK_DISCONNECTED:
                            Toast.makeText(context, "Disconnected", Toast.LENGTH_LONG).show();
                            mMenu.findItem(R.id.action_connect).setTitle("Connect");
                            AppState.getInstance().textSpeaker.speak("Disconnected");
                            break;
                    }
                    break;
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

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RacelinkConnection.BROADCAST_RACELINK_CONN_STATUS);
        intentFilter.addAction(RacelinkConnection.BROADCAST_RACELINK_PACKET);
        registerReceiver(mRacelinkReceiver, intentFilter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        AppState.getInstance().textSpeaker = new TextSpeaker(getApplicationContext());

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mLapsResulsListAdapter = new LapsResulsListAdapter(this, R.layout.laps_list, AppState.getInstance().getAllLaps());
        ListView lapsView = (ListView) findViewById(R.id.lvLapTimes);
        lapsView.setAdapter(mLapsResulsListAdapter);

        rssiView = (TextView) findViewById(R.id.txtRssi);

        startRaceBtn = (Button) findViewById(R.id.btnStartRace);
        startRaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!AppState.getInstance().isRaceStarted()) {
                    startRaceBtn.setText("Stop race");
                    startRace();
                }
                else {
                    startRaceBtn.setText("Start race");
                    stopRace();
                }
            }
        });
    }

    private CountDownTimer mCountDownTimer;

    private void startRace() {
        if(mRaceLink.getConnectionStatus() == RacelinkConnection.RACELINK_CONNECTED) {
            int racePreStart = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("race_pre_start_time", "30"));
            if(racePreStart < 5) racePreStart = 5;
            AppState.getInstance().textSpeaker.speak(String.format("Starting race in %d seconds", racePreStart));
            AppState.getInstance().resetRace();
            mLapsResulsListAdapter.notifyDataSetChanged();
            msg_command_long command = new msg_command_long();
            command.command = VTS_CMD.VTS_CMD_START_RACE_COUNTDOWN;
            command.param1 = racePreStart;
            command.param2 = 0f;
            mRaceLink.sendData(command.pack());
            mCountDownTimer = new CountDownTimer(racePreStart * 1000, 1000) {
                @Override
                public void onTick(long l) {
                    TextView v = (TextView) findViewById(R.id.txtTimer);
                    long m = (long) Math.floor(l/1000/60);
                    long s = (long) Math.floor(l/1000) - m*60;
                    String time = String.format("-%02d:%02d", m, s);
                    v.setText("Race timer: " + time);

                    if(m == 0 && s == 5) {
                        AppState.getInstance().textSpeaker.speak("Race starting in 5 seconds");
                    }

                    if(m == 0 && s < 3) {
                        AppState.getInstance().playTone(AppState.TONE_PREPARE, AppState.PREPARE_DURATION);
                    }
                }

                @Override
                public void onFinish() {
                    AppState.getInstance().playTone(AppState.TONE_GO, AppState.GO_DURATION);
                    AppState.getInstance().setRaceStarted(true);
                }
            };
            mCountDownTimer.start();
        } else {
            Toast.makeText(this, "Not connected to timing", Toast.LENGTH_LONG).show();
        }
    }

    private void stopRace() {
        if(mCountDownTimer != null)
            mCountDownTimer.cancel();
        if(mRaceLink.getConnectionStatus() == RacelinkConnection.RACELINK_CONNECTED) {
            msg_command_long command = new msg_command_long();
            command.command = VTS_CMD.VTS_CMD_START_RACE_COUNTDOWN;
            command.param1 = -1;
            mRaceLink.sendData(command.pack());
        }
        AppState.getInstance().setRaceStarted(false);
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
        mMenu = menu;
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if(id == R.id.open_param) {
            Intent intent = new Intent(this, ParametersActivity.class);
            startActivity(intent);
        }

        if(id == R.id.action_connect) {
            if(mRaceLink != null) {
                if(mRaceLink.getConnectionStatus() == RacelinkConnection.RACELINK_DISCONNECTED) {
                    if (mBluetoothAdapter == null) {
                        Toast.makeText(this, "No bluetooth found", Toast.LENGTH_LONG).show();
                        return true;
                    }
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Log.i(TAG, "We do not have permission for Bluetooth discovery");
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSON_COARSE_LOCATION);
                    } else {
                        startDiscovery();
                    }
                } else {
                    mRaceLink.disconnect();
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
