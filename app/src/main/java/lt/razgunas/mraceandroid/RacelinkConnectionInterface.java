package lt.razgunas.mraceandroid;

import com.MAVLink.MAVLinkPacket;

public interface RacelinkConnectionInterface {
    void onReceivePacket(MAVLinkPacket packet);

    void onConnectionStatus(int connectionStatus);
}
