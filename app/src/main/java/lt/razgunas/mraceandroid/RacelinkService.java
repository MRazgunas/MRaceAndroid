package lt.razgunas.mraceandroid;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.MAVLink.MAVLinkPacket;

public class RacelinkService extends Service {
    private static final String TAG = RacelinkService.class.getSimpleName();

    private RacelinkServiceApi mRacelinkApi = new RacelinkServiceApi(this);
    private BluetoothConnection mConnection;

    private String mBTAddress;

    public RacelinkService() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mConnection != null) {
            mConnection.disconnect();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mRacelinkApi;
    }

    private void connectToVTS() {
        if(mConnection == null) {
            Log.i(TAG, "Creating new Racelink connection");
            mConnection = new BluetoothConnection(this);
        }

        if(mConnection.getConnectionStatus() == RacelinkConnection.RACELINK_DISCONNECTED) {
            mConnection.connect(mBTAddress);
        }
    }

    private void disconnectFromVTS() {
        if(mConnection != null) {
            mConnection.disconnect();
        }
    }

    private void setBTAddress (String address) {
        mBTAddress = address;
    }


    public static class RacelinkServiceApi extends Binder {
        private final RacelinkService mService;

        RacelinkServiceApi(RacelinkService service) {
            mService = service;
        }

        public void sendData(MAVLinkPacket packet) {
            if(mService == null) return;
            mService.mConnection.sendRacelinkPacket(packet);
        }

        public void startConnection() {
            if(mService != null) {
                mService.connectToVTS();
            }
        }

        public void disconnect() {
            if(mService != null) {
                mService.disconnectFromVTS();
            }
        }

        public void setBTaddress(String address) {
            if(mService != null)
                mService.setBTAddress(address);
        }


        public int getConnectionStatus() {
            if(mService != null && mService.mConnection != null) {
                return mService.mConnection.getConnectionStatus();
            }
            return RacelinkConnection.RACELINK_DISCONNECTED;
        }
    }
}
