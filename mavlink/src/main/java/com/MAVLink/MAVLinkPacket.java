/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */
        
package com.MAVLink;

import java.io.Serializable;
import com.MAVLink.Messages.MAVLinkPayload;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.CRC;
import com.MAVLink.common.*;
import com.MAVLink.common.*;

/**
* Common interface for all MAVLink Messages
* Packet Anatomy
* This is the anatomy of one packet. It is inspired by the CAN and SAE AS-4 standards.

* Byte Index  Content              Value       Explanation
* 0            Packet start sign  v1.0: 0xFE   Indicates the start of a new packet.  (v0.9: 0x55)
* 1            Payload length      0 - 255     Indicates length of the following payload.
* 2            Packet sequence     0 - 255     Each component counts up his send sequence. Allows to detect packet loss
* 3            System ID           1 - 255     ID of the SENDING system. Allows to differentiate different MAVs on the same network.
* 4            Component ID        0 - 255     ID of the SENDING component. Allows to differentiate different components of the same system, e.g. the IMU and the autopilot.
* 5            Message ID          0 - 255     ID of the message - the id defines what the payload means and how it should be correctly decoded.
* 6 to (n+6)   Payload             0 - 255     Data of the message, depends on the message id.
* (n+7)to(n+8) Checksum (low byte, high byte)  ITU X.25/SAE AS-4 hash, excluding packet start sign, so bytes 1..(n+6) Note: The checksum also includes MAVLINK_CRC_EXTRA (Number computed from message fields. Protects the packet from decoding a different version of the same packet but with different variables).

* The checksum is the same as used in ITU X.25 and SAE AS-4 standards (CRC-16-CCITT), documented in SAE AS5669A. Please see the MAVLink source code for a documented C-implementation of it. LINK TO CHECKSUM
* The minimum packet length is 8 bytes for acknowledgement packets without payload
* The maximum packet length is 263 bytes for full payload
*
*/
public class MAVLinkPacket implements Serializable {
    private static final long serialVersionUID = 2095947771227815314L;

    public static final int MAVLINK_STX = 254;

    /**
    * Message length. NOT counting STX, LENGTH, SEQ, SYSID, COMPID, MSGID, CRC1 and CRC2
    */
    public final int len;

    /**
    * Message sequence
    */
    public int seq;

    /**
    * ID of the SENDING system. Allows to differentiate different MAVs on the
    * same network.
    */
    public int sysid;

    /**
    * ID of the SENDING component. Allows to differentiate different components
    * of the same system, e.g. the IMU and the autopilot.
    */
    public int compid;

    /**
    * ID of the message - the id defines what the payload means and how it
    * should be correctly decoded.
    */
    public int msgid;

    /**
    * Data of the message, depends on the message id.
    */
    public MAVLinkPayload payload;

    /**
    * ITU X.25/SAE AS-4 hash, excluding packet start sign, so bytes 1..(n+6)
    * Note: The checksum also includes MAVLINK_CRC_EXTRA (Number computed from
    * message fields. Protects the packet from decoding a different version of
    * the same packet but with different variables).
    */
    public CRC crc;

    public MAVLinkPacket(int payloadLength){
        len = payloadLength;
        payload = new MAVLinkPayload(payloadLength);
    }

    /**
    * Check if the size of the Payload is equal to the "len" byte
    */
    public boolean payloadIsFilled() {
        return payload.size() >= len;
    }

    /**
    * Update CRC for this packet.
    */
    public void generateCRC(){
        if(crc == null){
            crc = new CRC();
        }
        else{
            crc.start_checksum();
        }
        
        crc.update_checksum(len);
        crc.update_checksum(seq);
        crc.update_checksum(sysid);
        crc.update_checksum(compid);
        crc.update_checksum(msgid);

        payload.resetIndex();

        final int payloadSize = payload.size();
        for (int i = 0; i < payloadSize; i++) {
            crc.update_checksum(payload.getByte());
        }
        crc.finish_checksum(msgid);
    }

    /**
    * Encode this packet for transmission.
    *
    * @return Array with bytes to be transmitted
    */
    public byte[] encodePacket() {
        byte[] buffer = new byte[6 + len + 2];
        
        int i = 0;
        buffer[i++] = (byte) MAVLINK_STX;
        buffer[i++] = (byte) len;
        buffer[i++] = (byte) seq;
        buffer[i++] = (byte) sysid;
        buffer[i++] = (byte) compid;
        buffer[i++] = (byte) msgid;

        final int payloadSize = payload.size();
        for (int j = 0; j < payloadSize; j++) {
            buffer[i++] = payload.payload.get(j);
        }

        generateCRC();
        buffer[i++] = (byte) (crc.getLSB());
        buffer[i++] = (byte) (crc.getMSB());
        return buffer;
    }

    /**
    * Unpack the data in this packet and return a MAVLink message
    *
    * @return MAVLink message decoded from this packet
    */
    public MAVLinkMessage unpack() {
        switch (msgid) {
                         
            case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:
                return  new msg_heartbeat(this);
                 
            case msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS:
                return  new msg_sys_status(this);
                 
            case msg_system_time.MAVLINK_MSG_ID_SYSTEM_TIME:
                return  new msg_system_time(this);
                 
            case msg_param_request_read.MAVLINK_MSG_ID_PARAM_REQUEST_READ:
                return  new msg_param_request_read(this);
                 
            case msg_param_request_list.MAVLINK_MSG_ID_PARAM_REQUEST_LIST:
                return  new msg_param_request_list(this);
                 
            case msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE:
                return  new msg_param_value(this);
                 
            case msg_param_set.MAVLINK_MSG_ID_PARAM_SET:
                return  new msg_param_set(this);
                 
            case msg_racer_pass.MAVLINK_MSG_ID_RACER_PASS:
                return  new msg_racer_pass(this);
                 
            case msg_racer_lap.MAVLINK_MSG_ID_RACER_LAP:
                return  new msg_racer_lap(this);
                 
            case msg_vrx_status.MAVLINK_MSG_ID_VRX_STATUS:
                return  new msg_vrx_status(this);
                 
            case msg_command_long.MAVLINK_MSG_ID_COMMAND_LONG:
                return  new msg_command_long(this);
                 
            case msg_command_ack.MAVLINK_MSG_ID_COMMAND_ACK:
                return  new msg_command_ack(this);
                 
            case msg_radio_status.MAVLINK_MSG_ID_RADIO_STATUS:
                return  new msg_radio_status(this);
            
            
            default:
                return null;
        }
    }

}
        
        