package com.socioty.smartik;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.socioty.smartik.model.Token;

/**
 * Created by serhiipianykh on 2017-03-29.
 */

public class ManageRoomFragment extends DialogFragment {

    public static final String KEY_FLOOR_NUMBER = "floorNumber";

    private EditText roomName;
    private Spinner floorNumbers;
    private Integer floorNumber;

    private Dialog dialog;

    public static ManageRoomFragment newInstance() {
        Bundle args = new Bundle();

        ManageRoomFragment fragment = new ManageRoomFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View v = LayoutInflater.from(getContext()).inflate(R.layout.fragment_room_dialog, null);

        roomName = (EditText) v.findViewById(R.id.new_room_name);
        floorNumbers = (Spinner) v.findViewById(R.id.floor_number_spinner);

        final Bundle bundle = getArguments();
        if (bundle.containsKey(KEY_FLOOR_NUMBER)) {
            floorNumber = bundle.getInt(KEY_FLOOR_NUMBER);
            floorNumbers.setVisibility(View.GONE);
        } else {
            floorNumbers.setVisibility(View.VISIBLE);
            floorNumber = null;
        }

        dialog = new AlertDialog.Builder(getActivity()).setView(v)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       //nothing to do
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                final AlertDialog mDialog = (AlertDialog) dialog;

                mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (validateRoomName(roomName.getText().toString())) {
                            sendResult(10);
                            dialog.dismiss();
                        } else {
                            System.out.println("Room exists");
                            Snackbar.make(v, R.string.room_name_must_be_unique, Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
        return dialog;
    }

    private boolean validateRoomName(final String name) {
        return Token.sToken.getDeviceMap().getRoom(name) == null;
    }

    private void sendResult(int resultCode) {
        if(getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(RoomsFragment.KEY_FLOOR_NUMBER, floorNumber != null ? floorNumber : floorNumbers.getSelectedItemPosition());
        intent.putExtra(RoomsFragment.KEY_ROOM_NAME, roomName.getText().toString());

        getTargetFragment().onActivityResult(getTargetRequestCode(),resultCode, intent);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
