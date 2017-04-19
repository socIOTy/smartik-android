package com.socioty.smartik;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

    public static ManageRoomFragment newInstance() {

        Bundle args = new Bundle();

        ManageRoomFragment fragment = new ManageRoomFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.fragment_room_dialog, null);

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
        intent.putExtra(RoomsFragment.KEY_FLOOR_NUMBER, floorNumber != null ? floorNumber : floorNumbers.getSelectedItemPosition());
        intent.putExtra(RoomsFragment.KEY_ROOM_NAME, roomName.getText().toString());

        getTargetFragment().onActivityResult(getTargetRequestCode(),resultCode, intent);
    }

}
