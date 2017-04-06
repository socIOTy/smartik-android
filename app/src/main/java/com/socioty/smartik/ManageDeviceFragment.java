package com.socioty.smartik;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

/**
 * Created by serhiipianykh on 2017-03-29.
 */

public class ManageDeviceFragment extends DialogFragment {

    private EditText deviceName;
    private Spinner deviceTypes;
    private ImageButton deleteDeviceBtn;

    public static ManageDeviceFragment newInstance() {

        Bundle args = new Bundle();

        ManageDeviceFragment fragment = new ManageDeviceFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.fragment_device_dialog, null);

        deleteDeviceBtn = (ImageButton)v.findViewById(R.id.device_delete_btn);
        deleteDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResult(20);
            }
        });

        deviceName = (EditText)v.findViewById(R.id.new_device_name);
        deviceTypes = (Spinner)v.findViewById(R.id.device_type_spinner);
        return new AlertDialog.Builder(getActivity()).setView(v)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(10);
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
    }

    private void sendResult(int resultCode) {
        if(getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(DeviceListFragment.KEY_DEVICE_NAME, deviceName.getText().toString());
        intent.putExtra(DeviceListFragment.KEY_DEVICE_TYPE, deviceTypes.getSelectedItemPosition());
        getTargetFragment().onActivityResult(getTargetRequestCode(),resultCode, intent);
    }

}
