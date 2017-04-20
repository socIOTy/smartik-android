package com.socioty.smartik;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.socioty.smartik.model.DeviceAdapter;

import java.util.ArrayList;
import java.util.List;

import cloud.artik.model.Device;

/**
 * Created by Willian on 2017-02-15.
 */

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {

    private final ManageDeviceFragment manageDeviceFragment;
    private final FragmentManager fragmentManager;
    private final List<DeviceAdapter> devices;
    private final String accessToken;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;

        public ViewHolder(View view) {
            super(view);
            mTextView = (TextView) view.findViewById(R.id.device_name);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public DeviceListAdapter(final ManageDeviceFragment manageDeviceFragment, final FragmentManager fragmentManager, final List<Device> devices, final String accessToken) {
        final List<DeviceAdapter> adapters = new ArrayList<>(devices.size());
        for (final Device device : devices) {
            adapters.add(new DeviceAdapter(device));
        }

        this.manageDeviceFragment = manageDeviceFragment;
        this.fragmentManager = fragmentManager;
        this.devices = adapters;
        this.accessToken = accessToken;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public DeviceListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_adapter, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTextView.setText(devices.get(position).getDevice().getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                switch (devices.get(position).getDevice().getDtid()) {
                    case DeviceListFragment.LED_SMART_LIGHT_DEVICE_TYPE_ID: {
                        intent = new Intent(v.getContext(), LedSmartLightActivity.class);
                        break;
                    }
                    case DeviceListFragment.NEST_THERMOSTAT_DEVICE_TYPE_ID: {
                        intent = new Intent(v.getContext(), NestThermostatActivity.class);
                        break;
                    }
                    default: {
                        intent = null;
                        break;
                    }
                }
                if (intent != null) {
                    intent.putExtra(LedSmartLightActivity.KEY_ACCESS_TOKEN, accessToken);
                    intent.putExtra(LedSmartLightActivity.KEY_DEVICE_ID, devices.get(position).getDevice().getId());
                    v.getContext().startActivity(intent);
                }

            }
        });

        if (manageDeviceFragment != null) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final Bundle bundle = new Bundle();
                    bundle.putBoolean(ManageDeviceFragment.KEY_EDITION_MODE, true);
                    bundle.putString(ManageDeviceFragment.KEY_DEVICE_ID, devices.get(position).getDevice().getId());
                    bundle.putInt(ManageDeviceFragment.KEY_FLOOR_NUMBER, devices.get(position).getFloorNumber());
                    bundle.putString(ManageDeviceFragment.KEY_ROOM_NAME, devices.get(position).getRoom().getName());
                    bundle.putString(ManageDeviceFragment.KEY_DEVICE_NAME, devices.get(position).getDevice().getName());
                    manageDeviceFragment.setArguments(bundle);
                    manageDeviceFragment.show(fragmentManager, "BLA");
                    return true;
                }
            });
        }


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return devices.size();
    }
}
