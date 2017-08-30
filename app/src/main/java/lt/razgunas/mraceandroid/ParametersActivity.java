package lt.razgunas.mraceandroid;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.MAVLink.common.msg_param_set;

import java.util.ArrayList;

public class ParametersActivity extends AppCompatActivity implements AppState.ParamUpdateInterface {
    private final String TAG = ParametersActivity.class.getSimpleName();

    ListView paramLV;
    ParameterListAdapter mParamAdapter;

    private RacelinkService.RacelinkServiceApi mRaceLink;

    private ServiceConnection raclinkConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mRaceLink = (RacelinkService.RacelinkServiceApi) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mRaceLink = null;
        }
    };

    @Override
    public void paramListUpdated() {
        mParamAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameters);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent racelinkService = new Intent(this, RacelinkService.class);
        bindService(racelinkService, raclinkConnection, BIND_AUTO_CREATE);

        AppState.getInstance().registerParamListener(TAG, this);

        mParamAdapter = new ParameterListAdapter(this, R.layout.param_list, AppState.getInstance().getAllParams());
        paramLV = (ListView) findViewById(R.id.lvParameters);
        paramLV.setAdapter(mParamAdapter);

        paramLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ParametersActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                builder.setTitle("Change value");
                View layout = ParametersActivity.this.getLayoutInflater().inflate(R.layout.edit_param_dialog, null);
                final EditText paramValue = (EditText) layout.findViewById(R.id.txtDialogParam);
                paramValue.setText(Float.toString(mParamAdapter.getItem(i).getParamValue()));
                builder.setView(layout);
                builder.setPositiveButton("Set", new OnParamSetDialogClickListener(i, paramValue));
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onDestroy() {
        AppState.getInstance().unregisterParamListener(TAG);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.param_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_read_param) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class OnParamSetDialogClickListener implements DialogInterface.OnClickListener {
        int i;
        EditText text;
        public OnParamSetDialogClickListener(int i, EditText text) {
            this.i = i;
            this.text = text;
        }

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            float value = Float.parseFloat(text.getText().toString());
            if(mRaceLink != null) {
                msg_param_set packet = new msg_param_set();
                packet.setParam_Id(mParamAdapter.getItem(this.i).getParamName());
                packet.param_value = value;
                packet.param_type = mParamAdapter.getItem(this.i).getParamType();
                mRaceLink.sendData(packet.pack());
            }
        }
    }

    class ParameterListAdapter extends ArrayAdapter<ParamValue> {
        private LayoutInflater mLayoutInflater;
        private ArrayList<ParamValue> mParams;
        private int mViewRecourceId;

        public ParameterListAdapter(Context context, int resource, ArrayList<ParamValue> params) {
            super(context, resource, params);
            this.mParams = params;
            mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mViewRecourceId = resource;
        }

        public View getView(int position, View converView, ViewGroup parent) {
            converView = mLayoutInflater.inflate(mViewRecourceId, null);

            ParamValue param = mParams.get(position);

            if(param != null) {
                TextView paramName = (TextView) converView.findViewById(R.id.txtParamName);
                TextView paramValue = (TextView) converView.findViewById(R.id.txtParamValue);
                if(param != null) {
                    paramName.setText(param.getParamName());
                    paramValue.setText(Float.toString(param.getParamValue()));
                }
            }
            return converView;
        }
    }
}
