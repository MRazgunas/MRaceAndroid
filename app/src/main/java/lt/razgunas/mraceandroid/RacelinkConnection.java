package lt.razgunas.mraceandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;

import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class RacelinkConnection {

    private static final String TAG = RacelinkConnection.class.getSimpleName();

    private final LinkedBlockingQueue<byte[]> mPacketsToSend = new LinkedBlockingQueue<>();

    private Thread mConnectionThread;

    protected String btAddress;
    protected Context mContext;

    /*
     * Racelink connection states
     */
    public static final int RACELINK_DISCONNECTED = 0;
    public static final int RACELINK_CONNECTING = 1;
    public static final int RACELINK_CONNECTED = 2;

    private static final String PACKAGE_NAME = "lt.razgunas.mraceandroid";

    public static final String BROADCAST_RACELINK_PACKET = PACKAGE_NAME + ".racelinkpacket";
    public static final String BROADCAST_RACELINK_CONN_STATUS = PACKAGE_NAME + ".racelinkstatus";

    private static final int READ_BUFFER_SIZE = 4096;

    private int mConnectionStatus = RACELINK_DISCONNECTED;


    private final Runnable mConnectionTask = new Runnable() {
        @Override
        public void run() {
            Thread sendingThread = null;
            try {
                openConnection();

                sendingThread = new Thread(mSendingTask, "RacelinkConnection-Sending thread");
                sendingThread.start();

                if(mConnectionStatus == RACELINK_CONNECTING) {
                    mConnectionStatus = RACELINK_CONNECTED;
                    notifyListenerLinkStatus();
                }

                Parser parser = new Parser();
                parser.stats.resetStats();

                byte[] readBuffer = new byte[READ_BUFFER_SIZE];

                while (mConnectionStatus == RACELINK_CONNECTED) {
                    int bufferSize = readDataBlock(readBuffer);
                    handleData(parser, bufferSize, readBuffer);

                }
            } catch (IOException e) {
                Log.i(TAG, e.toString());
            } finally {
                if(sendingThread != null && sendingThread.isAlive()) {
                    sendingThread.interrupt();
                }
                disconnect();
            }
        }
    };

    private final Runnable mSendingTask = new Runnable() {
        @Override
        public void run() {
            try {
                while(mConnectionStatus == RACELINK_CONNECTED) {
                    byte[] buffer = mPacketsToSend.take();
                    try {
                        sendBuffer(buffer);
                    } catch (IOException e) {
                        Log.i(TAG, e.toString()) ;                       
                    }
                    
                }
            } catch (InterruptedException e) {
                Log.i(TAG, e.toString());
            } finally {
                disconnect();
            }

        }
    };

    public void sendRacelinkPacket(MAVLinkPacket packet) {
        final byte[] packetData = packet.encodePacket();
        if(!mPacketsToSend.offer(packetData)) {
            Log.i(TAG, "Packet sending buffer is full");
        }
    }

    private void handleData(Parser parser, int bufferSize, byte[] readBuffer) {
        if(bufferSize < 1)
            return;
        for(int i = 0; i < bufferSize; i++) {
            MAVLinkPacket packet = parser.mavlink_parse_char(readBuffer[i] & 0x00ff);
            if(packet != null) {
                notifyListenerNewMessage(packet);
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
            notifyListenerLinkStatus();
        }
    }

    public void disconnect() {
        if(mConnectionStatus == RACELINK_DISCONNECTED || mConnectionThread == null) {
            return;
        }

        try {
            mConnectionStatus = RACELINK_DISCONNECTED;
            if(mConnectionThread.isAlive() && !mConnectionThread.isInterrupted()) {
                mConnectionThread.interrupt();
            }
            closeConnection();
            notifyListenerLinkStatus();
        } catch (IOException e) {
            Log.i(TAG, e.toString());
        }
    }

    public int getConnectionStatus() {
        return mConnectionStatus;
    }


    private void notifyListenerLinkStatus() {
/*        for(RacelinkConnectionInterface i: mListeners.values()) {
            i.onConnectionStatus(mConnectionStatus);
        }*/
        Intent intent = new Intent();
        intent.setAction(BROADCAST_RACELINK_CONN_STATUS);
        intent.putExtra("status", mConnectionStatus);
        mContext.sendBroadcast(intent);
    }

    private void notifyListenerNewMessage(MAVLinkPacket packet) {
        /*for(RacelinkConnectionInterface i: mListeners.values()) {
            i.onReceivePacket(packet);
        }*/
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(BROADCAST_RACELINK_PACKET);
        Bundle vtsPack = new Bundle();
        vtsPack.putSerializable("packet", packet.unpack());
        broadcastIntent.putExtras(vtsPack);
        mContext.sendBroadcast(broadcastIntent);
    }



    protected abstract void openConnection() throws IOException;
    protected abstract int readDataBlock(byte[] buffer) throws IOException;
    protected abstract void sendBuffer(byte[] buffer) throws  IOException;
    protected abstract void closeConnection() throws IOException;
    protected abstract void loadPreferences();
}
