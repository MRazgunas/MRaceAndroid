package lt.razgunas.mraceandroid;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class LapsResulsListAdapter extends ArrayAdapter<LapResult>{
    private LayoutInflater mLayoutInflater;
    private ArrayList<LapResult> mLaps;
    private int mViewRecourceId;

    public LapsResulsListAdapter(Context context, int resource, ArrayList<LapResult> laps) {
        super(context, resource, laps);
        this.mLaps = laps;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewRecourceId = resource;
    }

    public View getView(int position, View converView, ViewGroup parent) {
        converView = mLayoutInflater.inflate(mViewRecourceId, null);

        LapResult lap = mLaps.get(position);

        if(lap != null) {
            TextView lapNumber = (TextView) converView.findViewById(R.id.txtLapNumber);
            TextView lapTime = (TextView) converView.findViewById(R.id.txtLapTime);
            if(lapNumber != null) {
                lapNumber.setText(Integer.toString(position + 1));
            }
            if(lapTime != null) {
                lapTime.setText(lap.getDisplayLapTime());
            }
        }
        return converView;
    }
}
