package com.socioty.smartik;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import com.socioty.smartik.model.Floor;
import com.socioty.smartik.model.Room;

import java.util.List;

/**
 * Created by Willian on 2017-04-19.
 */

public class RoomSpinnerAdapter extends ArrayAdapter {

    private final List<Room> rooms;

    public RoomSpinnerAdapter(Context context, int textViewResourceId, List<Room> rooms) {
        super(context, textViewResourceId, rooms);
        this.rooms = rooms;
    }

    public View getCustomView(int position, View convertView,
                              ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View layout = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);

        CheckedTextView textView = (CheckedTextView) layout
                .findViewById(android.R.id.text1);

        textView.setText(rooms.get(position).getName());
        return layout;
    }
    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Nullable
    @Override
    public Room getItem(int position) {
        return rooms.get(position);
    }
}
