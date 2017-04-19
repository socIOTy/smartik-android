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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.socioty.smartik.model.Floor;
import com.socioty.smartik.model.Room;
import com.socioty.smartik.model.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by serhiipianykh on 2017-03-29.
 */

public class ManageDeviceFragment extends DialogFragment {

    public static String KEY_EDITION_MODE = "EDITION_MODE";
    public static String KEY_DEVICE_ID = "DEVICE_ID";
    public static String KEY_FLOOR_NUMBER = "FLOOR_NUMBER";
    public static String KEY_ROOM_NAME = "ROOM_NAME";
    public static String KEY_DEVICE_NAME = "DEVICE_NAME";

    private Spinner floorSpinner;
    private Spinner roomSpinner;
    private EditText deviceName;
    private Spinner deviceTypes;
    private ImageButton deleteDeviceBtn;

    private String deviceId;

    public static ManageDeviceFragment newInstance() {

        Bundle args = new Bundle();

        ManageDeviceFragment fragment = new ManageDeviceFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.fragment_device_dialog, null);


        final Bundle bundle = getArguments();
        roomSpinner = (Spinner) v.findViewById(R.id.room_spinner);

        floorSpinner = (Spinner) v.findViewById(R.id.floor_spinner);

        final List<Floor> floorsWithRooms = new ArrayList<>();
        for (final Floor floor : Token.sToken.getDeviceMap().getFloors()) {
            if (!floor.getRooms().isEmpty()) {
                floorsWithRooms.add(floor);
            }
        }

        FloorSpinnerAdapter adapter = new FloorSpinnerAdapter(this.getContext(),
                android.R.layout.simple_spinner_item, floorsWithRooms);
        floorSpinner.setAdapter(adapter);
        floorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final RoomSpinnerAdapter adapter = new RoomSpinnerAdapter(ManageDeviceFragment.this.getContext(),
                        android.R.layout.simple_spinner_item, ((Floor)floorSpinner.getSelectedItem()).getRoomsList());
                roomSpinner.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        deleteDeviceBtn = (ImageButton)v.findViewById(R.id.device_delete_btn);
        deleteDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Intent intent = new Intent();
                intent.putExtra(DeviceListFragment.KEY_DEVICE_ID, deviceId);
                sendResult(30, intent);
                getDialog().dismiss();
            }
        });

        deviceName = (EditText)v.findViewById(R.id.new_device_name);
        deviceTypes = (Spinner)v.findViewById(R.id.device_type_spinner);

        final boolean editionMode;
        if (bundle.containsKey(KEY_EDITION_MODE) && bundle.getBoolean(KEY_EDITION_MODE)) {
            editionMode = true;
            deviceId = bundle.getString(KEY_DEVICE_ID);
            final int floorNumber = bundle.getInt(KEY_FLOOR_NUMBER);
            final String roomName = bundle.getString(KEY_ROOM_NAME);
            final String deviceName = bundle.getString(KEY_DEVICE_NAME);

            this.floorSpinner.setSelection(floorNumber);
            final List<Room> roomsList = ((Floor)this.floorSpinner.getSelectedItem()).getRoomsList();
            for (int k = 0; k < roomsList.size(); k++) {
                if (roomsList.get(k).getName().equals(roomName)) {
                    this.roomSpinner.setSelection(k);
                    break;

                }
            }

            this.deviceName.setText(deviceName);
            deleteDeviceBtn.setVisibility(View.VISIBLE);
            deviceTypes.setEnabled(false);
        } else {
            editionMode = false;
            deleteDeviceBtn.setVisibility(View.GONE);
            deviceTypes.setEnabled(true);
        }

        return new AlertDialog.Builder(getActivity()).setView(v)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final int resultCode;
                        final Intent intent = new Intent();
                        intent.putExtra(DeviceListFragment.KEY_FLOOR_NUMBER, floorSpinner.getSelectedItemPosition());
                        intent.putExtra(DeviceListFragment.KEY_ROOM_NAME, ((Room)roomSpinner.getSelectedItem()).getName());
                        intent.putExtra(DeviceListFragment.KEY_DEVICE_NAME, deviceName.getText().toString());
                        intent.putExtra(DeviceListFragment.KEY_DEVICE_TYPE, deviceTypes.getSelectedItemPosition());
                        if (editionMode) {
                            resultCode = 20;
                            intent.putExtra(DeviceListFragment.KEY_DEVICE_ID, deviceId);
                        } else {
                            resultCode = 10;
                        }
                        sendResult(resultCode, intent);

                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
    }

    private void sendResult(final int resultCode, final Intent intent) {
        if(getTargetFragment() == null) {
            return;
        }


        getTargetFragment().onActivityResult(getTargetRequestCode(),resultCode, intent);
    }

}
