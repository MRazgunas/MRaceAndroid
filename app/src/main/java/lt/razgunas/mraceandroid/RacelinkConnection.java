package lt.razgunas.mraceandroid;

import android.util.Log;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;

import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

public abstract class RacelinkConnection {

    private static final String TAG = RacelinkConnection.class.getSimpleName();

    private final ConcurrentHashMap<String, RacelinkConnectionInterface> mListeners = new ConcurrentHashMap<>();

    private Thread mConnectionThread;

    protected String btAddress;

    /*
     * Racelink connection states
     */
    public static final int RACELINK_DISCONNECTED = 0;
    public static final int RACELINK_CONNECTING = 1;
    public static final int RACELINK_CONNECTED = 2;

    private static final int READ_BUFFER_SIZE = 4096;

    private int mConnectionStatus = RACELINK_DISCONNECTED;

    private Thread mTaskThread;

    private final Runnable mConnectionTask = new Runnable() {
        @Override
        public void run() {
            try {
                openConnection();
            } catch (IOException e) {
                Log.i(TAG, e.toString());
            }
        }
    };

    private final Runnable mSendingTask = new Runnable() {
        @Override
        public void run() {
//            while(mConnectionStatus == RACELINK_CONNECTED) {
//            }

        }
    };

    private final Runnable mManagerTask = new Runnable() {
        @Override
        public void run() {
            Thread ioThread = null;
            ioThread = new Thread(mSendingTask, "RacelinkConnection-Sending thread");
            ioThread.start();
            try {
                Parser parser = new Parser();
                parser.stats.resetStats();

                byte[] readBuffer = new byte[READ_BUFFER_SIZE];

                while (mConnectionStatus == RACELINK_CONNECTED) {
                    int bufferSize = readDataBlock(readBuffer);
                    handleData(parser, bufferSize, readBuffer);

                }
            } catch (IOException e) {
                Log.i(TAG, e.toString());
            }
        }
    };

    protected void onConnectionOpened() {
        if(mConnectionStatus == RACELINK_CONNECTING) {
            mConnectionStatus = RACELINK_CONNECTED;
            mTaskThread = new Thread(mManagerTask, "RacelinkConnection-manager task");
            mTaskThread.start();
        }

    }

    private void handleData(Parser parser, int bufferSize, byte[] readBuffer) {
        if(bufferSize < 1)
            return;
        for(int i = 0; i < bufferSize; i++) {
            MAVLinkPacket packet = parser.mavlink_parse_char(readBuffer[i] & 0x00ff);
            if(packet != null) {
                Log.i(TAG, packet.toString());
            }

        }


    }

    public void connect(String address) {
        btAddress = address;
        if(mConnectionStatus == RACELINK_DISCONNECTED) {
            Log.i(TAG, "Starting connection thread");
            mConnectionThread = new Thread(mConnectionTask, "RacelinkConnection-Connection Thread");
            mConnectionThread.start();
            mConnectionStatus = RACELINK_CONNECTING;
        }
    }

    public int getConnectionStatus() {
        return mConnectionStatus;
    }



    protected abstract void openConnection() throws IOException;
    protected abstract int readDataBlock(byte[] buffer) throws IOException;
    protected abstract void sendBuffer(byte[] buffer) throws  IOException;
    protected abstract void closeConnection();
    protected abstract void loadPreferences();
}
