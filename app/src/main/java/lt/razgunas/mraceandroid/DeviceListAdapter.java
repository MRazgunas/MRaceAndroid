package lt.razgunas.mraceandroid;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {
    private LayoutInflater mLayoutInflater;
    private ArrayList<BluetoothDevice> mDevices;
    private int mViewRecourceId;

    public DeviceListAdapter(Context context, int resource, ArrayList<BluetoothDevice> devices) {
        super(context, resource, devices);
        this.mDevices = devices;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewRecourceId = resource;
    }

    public View getView(int position, View converView, ViewGroup parent) {
        converView = mLayoutInflater.inflate(mViewRecourceId, null);

        BluetoothDevice device = mDevices.get(position);

        if(device != null) {
            TextView deviceName = (TextView) converView.findViewById(R.id.tvDeviceName);
            TextView deviceAddress = (TextView) converView.findViewById(R.id.tvDeviceAddress);
            if(deviceName != null) {
                deviceName.setText(device.getName());
            }
            if(deviceAddress != null) {
                deviceAddress.setText(device.getAddress());
            }
        }
        return converView;
    }
}
